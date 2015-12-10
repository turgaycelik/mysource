package com.atlassian.jira.soap.axis;

import com.atlassian.jira.plugin.rpc.SoapModuleDescriptor;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.message.RPCElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.providers.java.RPCProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an {@link org.apache.axis.providers.java.RPCProvider} that
 * uses PICO to create SOAP service instances based on service class name
 */
@SuppressWarnings ({ "ValidExternallyBoundObject" })
public class JiraAxisJavaPicoRPCProvider extends RPCProvider
{
    private final List<JiraSoapTokenResolver> tokenResolvers = new ArrayList<JiraSoapTokenResolver>();
    private final SoapModuleDescriptor descriptor;

    public JiraAxisJavaPicoRPCProvider(SoapModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    protected Class getServiceClass(String clsName, SOAPService service, MessageContext msgContext)
    {
        return descriptor.getPublishedInterface();
    }


    /**
     * Called by AXIS to create a new service object instance for a specified class name
     *
     * @param msgContext the message context in play
     * @param clsName the class name to instantiate
     * @return a newly instantiated service object
     *
     * @throws Exception if something goes wrong
     */
    protected Object makeNewServiceObject(MessageContext msgContext, String clsName) throws Exception
    {
        Object soapServiceInstance = descriptor.getModule();

        if (soapServiceInstance instanceof JiraSoapTokenResolver)
        {
            tokenResolvers.add((JiraSoapTokenResolver) soapServiceInstance);
        }
        return soapServiceInstance;
    }

    /**
     * This allows access to the SOAP body given an AXIS message context.
     *
     * @param reqEnv     the outer soap envelope
     * @param msgContext the AXIS message context in play
     * @return an RPCElement that represents the SOAP body
     * @throws Exception
     */
    protected RPCElement getBody(final SOAPEnvelope reqEnv, final MessageContext msgContext) throws Exception
    {
        return super.getBody(reqEnv, msgContext);
    }

    /**
     * This can be called to resolve a token to user name.  This will call ALL the RPC plugins until one
     * of them responds with a non null user name.
     *
     * @param token the token to resolve
     * @return a user name or null if its cant be resolved
     */
    protected String resolveTokenToUserName(String token)
    {
        String userName = null;
        for (final JiraSoapTokenResolver jiraAxisTokenResolver : tokenResolvers)
        {
            userName = jiraAxisTokenResolver.resolveTokenToUserName(token);
            if (userName != null)
            {
                break;
            }
        }
        return userName;
    }

    /**
     * This is called to allow the RPC plugins to tell us what index the token occurs in the operation.  It can return -1
     * to indicate that no token is available
     *
     * @param operationName the name of the current SOAP operation
     * @return the index of the token parameter or -1 to indicate that its not known
     */
    protected int getTokenParameterIndex(String operationName)
    {
        int parameterIndex = -1;
        for (final JiraSoapTokenResolver jiraAxisTokenResolver : tokenResolvers)
        {
            parameterIndex = jiraAxisTokenResolver.getTokenParameterIndex(operationName);
            if (parameterIndex != -1)
            {
                break;
            }
        }
        return parameterIndex;
    }


}
