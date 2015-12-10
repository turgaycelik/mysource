package com.atlassian.jira.soap.axis;

import com.atlassian.jira.util.log.OneShotLogger;
import com.atlassian.jira.web.filters.accesslog.AccessLogBuilder;
import com.atlassian.jira.web.filters.accesslog.AtlassianSessionIdUtil;
import com.google.common.collect.ImmutableList;
import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.message.RPCElement;
import org.apache.axis.message.RPCParam;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_ASESSIONID;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_ID;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_RPC_SOAP_SESSIONID;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_RPC_SOAP_URLSUFFIX;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_RPC_SOAP_USERNAME;

/**
 * A class that can log SOAP invocation information.  They key here is that it can resolve the operation being invoked
 * and the user on whose behalf the request is being made.
 *
 * @since v3.13.2
 */
class JiraAxisSoapLog
{
    private final JiraAxisJavaPicoRPCProvider picoRpcProvider;

    private Logger log = Logger.getLogger(JiraAxisSoapLog.class);
    private Logger dumpLog = Logger.getLogger(log.getName() + "Dump");
    private Logger errorLog = Logger.getLogger("com.atlassian.jira.soap.axis");

    private OneShotLogger soapBodyAccessOneShot = new OneShotLogger(errorLog);
    private OneShotLogger unexpectedThrowableOneShot = new OneShotLogger(errorLog);

    private static final int MAX_URL_PARAM_WIDTH = 150;
    private static final String MASKED = "**masked**";
    private static final String TOOBIG = "**BIG**";
    private static final String BINARY = "**BINARY**";

    /**
     * Classes that have a known maximum fixed width when are serialised to a String. We don't even check the length of
     * these before logging.
     */
    private static final ImmutableList<Class<?>> FIXED_WIDTH_CLASSES = ImmutableList.<Class<?>>of(
            // primitive types
            Byte.class,
            Short.class,
            Integer.class, 
            Long.class, 
            Float.class, 
            Double.class, 
            Boolean.class, 
            Character.class,

            // dates
            Date.class,
            Calendar.class
    );

    public JiraAxisSoapLog(final JiraAxisJavaPicoRPCProvider picoRpcProvider)
    {
        this.picoRpcProvider = picoRpcProvider;
    }

    /**
     * Called to log the SOAP request details to a special atlassian-jira-soap-acess.log
     *
     * @param msgContext     the AXIS message context in play
     * @param httpStatusCode the HTTP status code
     * @param responseTimeMS the time it took to run the SOAP request
     */
    public void logMessage(MessageContext msgContext, int httpStatusCode, long responseTimeMS)
    {
        try
        {
            //
            // if we have an operation, we have a parsed and completed call
            final OperationDesc operationDesc = msgContext.getOperation();
            if (operationDesc != null)
            {
                final HttpServletRequest httpReq = (HttpServletRequest) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
                final String operationName = operationDesc.getName();
                final Message requestMessage = msgContext.getRequestMessage();

                // ask the plugin for the token parameter index
                int tokenIndex = picoRpcProvider.getTokenParameterIndex(operationName);
                //
                // finds the parameters that came in on the SOAP request
                final RPCParam[] rpcParams = getOperationParameterValues(msgContext, requestMessage);
                //
                // try and find out the user name.  We do this by resolving the token via the plugin itself
                String userName = null;
                String authToken = null;
                if (tokenIndex >= 0 && tokenIndex < rpcParams.length)
                {
                    authToken = rpcParams[tokenIndex].getValue();
                    userName = picoRpcProvider.resolveTokenToUserName(authToken);
                }
                //
                // now a special case for the login operation.  We know the first param is the user name
                if ("login".equals(operationName) && rpcParams.length > 0)
                {
                    userName = rpcParams[0].getValue();
                    authToken = getAuthTokenFromLoginResponse(msgContext);
                }
                //
                // soap client typically do not return cookies and hence the generated JIRA_REQUEST_ASESSIONID will be unique for every request
                // but the authToken is really the session id.  So we want to use it and place it into the different JIRA_RPC_SOAP_SESSIONID request attribute.
                String soapSessionId = (String) httpReq.getAttribute(JIRA_REQUEST_ASESSIONID);
                if (authToken != null)
                {
                    // generate one based on the authToken as is really a session thingy
                    soapSessionId = AtlassianSessionIdUtil.generateASESSIONID(authToken);
                }

                //
                // now stick some of this information away in the request object so we can access later up the request chain
                final String soapUrlSuffix = makeSoapUrlSuffix(operationDesc, rpcParams, operationName, tokenIndex);
                httpReq.setAttribute(JIRA_RPC_SOAP_USERNAME, userName);
                httpReq.setAttribute(JIRA_RPC_SOAP_URLSUFFIX, soapUrlSuffix);
                httpReq.setAttribute(JIRA_RPC_SOAP_SESSIONID, soapSessionId);

                if (log.isInfoEnabled())
                {
                    //
                    // now print all this information out to the access log
                    log.info(logInvocation(msgContext, userName, soapUrlSuffix, httpStatusCode, responseTimeMS, soapSessionId));
                }
                //
                // and if they have the dump log enabled then go for that as well
                if (dumpLog.isInfoEnabled())
                {
                    final String dumpMsg = logInvocationData(msgContext, userName, soapUrlSuffix, httpStatusCode, responseTimeMS, soapSessionId);
                    if (!StringUtils.isBlank(dumpMsg))
                    {
                        dumpLog.info(dumpMsg);
                    }
                }

            }
        }
        catch (RuntimeException t)
        {
            //
            // we NEVER EVER want the SOAP access log to interfere with the sending of SOAP data
            // so if we get any RuntimeExceptions during the execution of the logging then ignore it
            logUnexpectedException(t);
        }
    }

    private String logInvocation(final MessageContext msgContext, final String userName, final String soapUrlSuffix, final int httpStatusCode, final long responseTimeMS, final String sessionId)
    {
        long responseContentLength = getContentLength(msgContext.getResponseMessage());

        final HttpServletRequest httpReq = (HttpServletRequest) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        final String transportURL = (String) msgContext.getProperty(MessageContext.TRANS_URL);
        String uniqueRequestId = (String) httpReq.getAttribute(JIRA_REQUEST_ID);
        if (uniqueRequestId != null)
        {
            uniqueRequestId = "o" + uniqueRequestId;
        }

        StringBuilder url = new StringBuilder(transportURL).append("/");
        if (soapUrlSuffix.length() > 0)
        {
            url.append(soapUrlSuffix);
        }

        return new AccessLogBuilder(httpReq)
                .setRequestId(uniqueRequestId)
                .setUrl(url.toString())
                .setUserName(userName)
                .setResponseContentLength(responseContentLength)
                .setHttpStatusCode(httpStatusCode)
                .setResponseTimeMS(responseTimeMS)
                .setSessionId(sessionId)
                .toApacheCombinedLogFormat();
    }

    private String logInvocationData(final MessageContext msgContext, final String userName, String soapUrlSuffix, final int httpStatusCode, final long responseTimeMS, final String sessionId)
    {
        Exception problemE;
        try
        {
            StringBuilder msg = new StringBuilder();
            msg.append(logInvocation(msgContext, userName, soapUrlSuffix, httpStatusCode, responseTimeMS, sessionId));
            msg.append("\n");

            SOAPEnvelope reqEnv = msgContext.getRequestMessage().getSOAPEnvelope();
            SOAPEnvelope resEnv = msgContext.getResponseMessage().getSOAPEnvelope();

            String requestEnv = "\tREQ : " + reqEnv + "\n";
            String responseEnv = "RES : " + resEnv + "\n";

            msg.append(indent(requestEnv));
            msg.append(indent(responseEnv));

            return msg.toString();
        }
        catch (AxisFault axisFault)
        {
            problemE = axisFault;
        }
        // we really dont care that much
        soapBodyAccessOneShot.error("Cant access SOAP request/response as expected", problemE);
        return "";
    }

    private String indent(CharSequence s)
    {
        return StringUtils.replace(s.toString(), "\n", "\n\t");
    }

    private String makeSoapUrlSuffix(final OperationDesc operation, final RPCParam[] rpcParams, final String operationName, final int tokenIndex)
    {
        StringBuilder parameters = new StringBuilder(operation.getName());
        for (int i = 0; i < rpcParams.length; i++)
        {
            RPCParam rpcParam = rpcParams[i];
            String value = getLogValue(rpcParam);
            if ("login".equals(operationName) && i == 1)
            {
                value = MASKED;
            }
            else if (!"login".equals(operationName) && i == tokenIndex)
            {
                value = MASKED;
            }
            parameters.append(i == 0 ? "?" : "&").append(rpcParam.getName()).append("=").append(value);

            if (parameters.length() > MAX_URL_PARAM_WIDTH)
            {
                break; // already too big, it will be trimmed below
            }
        }
        return StringUtils.abbreviate(parameters.toString(), MAX_URL_PARAM_WIDTH);
    }

    private String getLogValue(RPCParam rpcParam) {
        Object obj = rpcParam.getObjectValue();

        if (isFixedWidthType(obj))
        {
            return rpcParam.getValue();
        }

        // print "small" strings
        if (obj instanceof String)
        {
            String string = (String) obj;
            if (string.length() > MAX_URL_PARAM_WIDTH)
            {
                return TOOBIG;
            }

            return rpcParam.getValue();
        }

        // smallish String arrays are OK too
        if (obj instanceof String[])
        {
            String[] strings = (String[]) obj;
            if (strings.length > MAX_URL_PARAM_WIDTH)
            {
                return TOOBIG;
            }

            long totalLength = 0;
            for (String s : strings)
            {
                totalLength += StringUtils.length(s);
                if (totalLength > MAX_URL_PARAM_WIDTH)
                {
                    return TOOBIG;
                }
            }

            return rpcParam.getValue();
        }

        // if it's not a String then don't log it!
        return BINARY;
    }

    /**
     * Returns true if <code>obj</code> is a wrapper around a primitive Java type
     *
     * @param obj an Object
     * @return true if <code>obj</code> is a wrapper around a primitive Java type
     */
    private boolean isFixedWidthType(Object obj)
    {
        for (Class cls : FIXED_WIDTH_CLASSES)
        {
            if (cls.isAssignableFrom(obj.getClass()))
            {
                return true;
            }
        }

        return false;
    }

    private RPCParam[] getOperationParameterValues(final MessageContext msgContext, final Message requestMessage)
    {
        try
        {
            SOAPEnvelope reqEnv = requestMessage.getSOAPEnvelope();
            RPCElement bodyE = picoRpcProvider.getBody(reqEnv, msgContext);
            Vector params = bodyE.getParams();
            return (RPCParam[]) params.toArray(new RPCParam[params.size()]);
        }
        catch (Exception e)
        {
            return new RPCParam[0];
        }
    }

    private long getContentLength(Message message)
    {
        try
        {
            return message.getContentLength();
        }
        catch (AxisFault axisFault)
        {
            return -1;
        }
    }

    /**
     * The reason we need this is that authToken (which we use as a source of the SOAP session id) is only know AFTER
     * the call is made.  So we need to have a squiz in the "login" operation response and see if we can extract a
     * value
     *
     * @param loginOperationMsgContext the response message for the "login" operation
     *
     * @return the authToken if it can be found
     */
    private String getAuthTokenFromLoginResponse(final MessageContext loginOperationMsgContext)
    {
        try
        {
            /*

            The response on this looks like for the login response

            <soapenv:Body>
                <ns1:loginResponse soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/" xmlns:ns1="http://soap.rpc.jira.atlassian.com">
                    <loginReturn xsi:type="xsd:string">P4FN0w5geE</loginReturn>
                </ns1:loginResponse>
            </soapenv:Body>
             */
            final NodeList loginReturn = loginOperationMsgContext.getResponseMessage().getSOAPBody().getElementsByTagName("loginReturn");
            if (loginReturn != null && loginReturn.getLength() > 0)
            {
                return loginReturn.item(0).getFirstChild().getNodeValue();
            }
        }
        catch (Exception unexpected)
        {
            logUnexpectedException(unexpected);
        }
        return null;
    }


    private void logUnexpectedException(final Exception t)
    {
        unexpectedThrowableOneShot.error("A problem was encountered during SOAP access logging.  Ignoring it!", t);
    }
}
