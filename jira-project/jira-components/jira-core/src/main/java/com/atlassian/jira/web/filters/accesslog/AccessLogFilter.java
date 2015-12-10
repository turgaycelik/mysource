package com.atlassian.jira.web.filters.accesslog;

import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.instrumentation.InstrumentationName;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.http.request.CapturingRequestWrapper;
import com.atlassian.jira.util.http.response.CapturingResponseWrapper;
import com.atlassian.jira.util.http.response.ObservantResponseWrapper;
import com.atlassian.jira.util.log.Log4jKit;
import com.atlassian.jira.web.session.currentusers.JiraUserSessionTracker;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_ASESSIONID;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_ID;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_START_MILLIS;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_TIME_MICROS;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_USER_NAME;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_RPC_SOAP_SESSIONID;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_RPC_SOAP_URLSUFFIX;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_RPC_SOAP_USERNAME;

/**
 * Logs the JIRA user name and request URL via log4j to a special log file in Apache Combined Log Format (with response
 * time)
 * <p/>
 * This also works in concert with the com.atlassian.jira.soap.axis.JiraSoapAxisLogger to pass information between the
 * layers via HttpServletRequest attributes.
 */
public class AccessLogFilter implements Filter
{
    private static final Logger basicLog = Logger.getLogger(AccessLogFilter.class);
    private static final Logger dumpLog = Logger.getLogger(basicLog.getName() + "Dump");
    private static final Logger includeImagesLog = Logger.getLogger(basicLog.getName() + "IncludeImages");
    private static final String ALREADY_FILTERED = AccessLogFilter.class.getName() + "_already_filtered";
    private static final int MAX_CAPTURE_LEN = 1024 * 20;
    private static final String ANONYMOUS = "anonymous";

    /**
     * Does nothing but prints INFO log message
     *
     * @param filterConfig not used
     * @throws ServletException not thrown
     */
    public void init(final FilterConfig filterConfig) throws ServletException
    {
        basicLog.info("AccessLogFilter initialized. Look in atlassian-jira-http-access.log for HTTP access log entries");
    }

    /**
     * Creates logs if INFO logging level is set and URL is "interesting" according to {@link
     * #isInterestingUrl(String)}.
     *
     * @param servletRequest request
     * @param servletResponse response
     * @param filterChain filter chain
     * @throws IOException if another filter in the filter chain throws it
     * @throws ServletException if another filter in the filter chain throws it
     */
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException
    {
        // Only apply this filter once per httpServletRequest
        if (servletRequest.getAttribute(ALREADY_FILTERED) != null)
        {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        else
        {
            servletRequest.setAttribute(ALREADY_FILTERED, Boolean.TRUE);
        }
        //
        // now look into the request and log it
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        executeRequest(httpServletRequest, httpServletResponse, filterChain);
    }

    private void executeRequest(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final FilterChain filterChain)
            throws IOException, ServletException
    {
        // ======================
        //
        // PRE filter chain steps.  Are we really interested at all?
        final FilterCallState filterCallState = preFilterCallStep(new FilterCallState(httpServletRequest, httpServletResponse));

        // ======================
        //
        // now execute further along the filter chain and time it as it goes past
        try
        {
            filterChain.doFilter(filterCallState.httpServletRequest, filterCallState.httpServletResponse);
        }
        catch (final IOException ioe)
        {
            filterCallState.requestException = ioe;
            throw ioe;
        }
        catch (final ServletException se)
        {
            filterCallState.requestException = se;
            throw se;
        }
        catch (final RuntimeException rte)
        {
            filterCallState.requestException = rte;
            throw rte;
        }
        finally
        {

            // ======================
            //
            // POST chain steps.  Did anything require logging?  We have put this in a finally block so that
            // if the request has had an exception and it manages to get back to the top of the filter, then
            // we still might get an access log record.
            postFilterCallStep(filterCallState);
        }
    }

    /**
     * A simple object we can use to put the various variables into and hence reduce our function parameters.
     */
    private class FilterCallState
    {
        private HttpServletResponse httpServletResponse;

        private HttpServletRequest httpServletRequest;

        private CapturingRequestWrapper capturingRequestWrapper = null;

        private ObservantResponseWrapper observantResponseWrapper = null;

        private CapturingResponseWrapper capturingResponseWrapper = null;

        private final String requestId;

        private final String atlassianSessionId;

        private String requestUrl = null;

        private String userName = null;

        private boolean interestedInUrl = false;

        private boolean performFullHttpDump = false;

        private final long startTimeMS;

        private Exception requestException = null;

        // we always need this information
        private FilterCallState(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
        {
            // we make an extra call here to the ensure that the ASESSIONID is put into play.  Its possible
            // when we don't have a session (until the request gets to here for example) that the ASESSIONID will not have been
            // generated so lets give it a go
            new AccessLogRequestInfo().enterRequest(httpServletRequest, httpServletResponse);

            this.httpServletResponse = httpServletResponse;
            this.httpServletRequest = httpServletRequest;
            this.requestId = (String) httpServletRequest.getAttribute(JIRA_REQUEST_ID);
            this.atlassianSessionId = (String) httpServletRequest.getAttribute(JIRA_REQUEST_ASESSIONID);
            startTimeMS = (Long) httpServletRequest.getAttribute(JIRA_REQUEST_START_MILLIS);
        }

        @Override
        public String toString()
        {
            return requestUrl;
        }
    }

    /**
     * This is called to setup all the various state needed before making the filter call.  Depending on what settings
     * are in play we set various variables.
     * <p/>
     * We wrap them up into one holder object to make life easier from a function parameter point of view.
     *
     * @param filterCallState a simple object containing the many parameters that would otherwise be required
     * @return the FilterCallState passed in
     */
    private FilterCallState preFilterCallStep(final FilterCallState filterCallState)
    {

        // place this information into the request so the code later in the chain can use it
        filterCallState.userName = filterCallState.httpServletRequest.getRemoteUser();
        filterCallState.requestUrl = filterCallState.httpServletRequest.getRequestURL().toString();

        // the user name may change later in the sense that SOAP users cant be known what user to use until
        // after the SOAP call is completed.
        filterCallState.userName = filterCallState.httpServletRequest.getRemoteUser();

        if (basicLog.isInfoEnabled())
        {
            if (isInterestingUrl(filterCallState.requestUrl))
            {
                filterCallState.observantResponseWrapper = new ObservantResponseWrapper(filterCallState.httpServletResponse);
                filterCallState.httpServletResponse = filterCallState.observantResponseWrapper;
                filterCallState.interestedInUrl = true;
            }
        }
        if (filterCallState.interestedInUrl)
        {
            final String requestId = "i" + filterCallState.requestId;

            final String msg = new AccessLogBuilder(filterCallState.httpServletRequest).setDateOfEvent(new DateTime(filterCallState.startTimeMS)).setRequestId(
                    requestId).setUrl(filterCallState.requestUrl).setUserName(filterCallState.userName).setSessionId(filterCallState.atlassianSessionId).toApacheCombinedLogFormat();
            basicLog.info(msg);

            if (dumpLog.isInfoEnabled())
            {
                filterCallState.capturingRequestWrapper = new CapturingRequestWrapper(filterCallState.httpServletRequest, MAX_CAPTURE_LEN);
                filterCallState.httpServletRequest = filterCallState.capturingRequestWrapper;

                filterCallState.capturingResponseWrapper = new CapturingResponseWrapper(filterCallState.httpServletResponse, MAX_CAPTURE_LEN);
                filterCallState.httpServletResponse = filterCallState.capturingResponseWrapper;

                filterCallState.performFullHttpDump = true;
                dumpLog.info(msg);
            }
        }
        recordUserInformation(filterCallState);
        return filterCallState;
    }

    private void recordUserInformation(final FilterCallState filterCallState)
    {
        /**
         * Add the new log4j MDC information we know about.  Others have put the request id and son on in there
         */
        final String userName = filterCallState.userName;
        Log4jKit.putUserToMDC(userName);
        if (!filterCallState.httpServletResponse.isCommitted())
        {
            // We need to encode userName before set to response header because response header doesn't
            // support not US-ASCII chars
            // Following GHS-10385 issue, we need to support not US-ASCII username
            filterCallState.httpServletResponse.setHeader(AccessLogRequestInfo.X_USER_NAME_HEADER, (userName == null) ? ANONYMOUS : JiraUrlCodec.encode(userName, true));
        }
    }

    /**
     * This is called after the request has finished executing.  The filterCallState has all the information we need in
     * order to decide what logging if any should take place.
     *
     * @param filterCallState a simple object containing the many parameters that would otherwise be required
     */
    private void postFilterCallStep(final FilterCallState filterCallState)
    {
        if (filterCallState.requestException != null)
        {
            Instrumentation.pullCounter(InstrumentationName.FIVE_HUNDREDS).incrementAndGet();
        }
        final long now = System.currentTimeMillis();
        final long responseTimeMS = now - filterCallState.startTimeMS;

        filterCallState.userName = adjustUserName(filterCallState.httpServletRequest, filterCallState.userName);
        filterCallState.httpServletRequest.setAttribute(JIRA_REQUEST_USER_NAME, filterCallState.userName);

        if (filterCallState.interestedInUrl)
        {
            int statusCode = filterCallState.observantResponseWrapper.getStatus();
            final long responseContentLen = filterCallState.observantResponseWrapper.getContentLen();

            filterCallState.requestUrl = adjustUrl(filterCallState.httpServletRequest, filterCallState.requestUrl);

            //
            // if an exception happened during the request, we want the access logs to reflect something about that!
            if (filterCallState.requestException != null)
            {
                //
                // If the response has not been committed yet then the user is going to get a HTTP 500 error.  But we dont know about that because at this stage
                // we still see a 200 OK status code.  So lets synthesize this!
                if (!filterCallState.httpServletResponse.isCommitted())
                {
                    statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                }
            }
            //
            // a soap call will typically not be assigned an ASESSIONID because it doesnt come in via a browser
            String sessionId = filterCallState.atlassianSessionId;
            if (sessionId == null)
            {
                sessionId = (String) filterCallState.httpServletRequest.getAttribute(JIRA_RPC_SOAP_SESSIONID);
            }

            final String msg = new AccessLogBuilder(filterCallState.httpServletRequest).setDateOfEvent(new DateTime()).setRequestId(
                    "o" + filterCallState.requestId).setUrl(filterCallState.requestUrl).setUserName(filterCallState.userName).setSessionId(sessionId).setResponseTimeMS(
                    responseTimeMS).setHttpStatusCode(statusCode).setResponseContentLength(responseContentLen).toApacheCombinedLogFormat();

            basicLog.info(msg);

            // do we want dumping as well
            if (filterCallState.performFullHttpDump)
            {
                final String dumpMsg = AccessLogDumpUtil.dumpRequestResponse(filterCallState.capturingRequestWrapper,
                        filterCallState.capturingResponseWrapper, msg, responseContentLen, filterCallState.requestException);
                dumpLog.info(dumpMsg);
            }
        }
        //
        // now stick the timing away (in microseconds) so someone up the chain (say a Tomcat Access Valve) can know about it
        filterCallState.httpServletRequest.setAttribute(JIRA_REQUEST_TIME_MICROS, String.valueOf(responseTimeMS / 1000));

        //
        // and finally keep track of the user
        JiraUserSessionTracker.recordInteraction(filterCallState.httpServletRequest);
    }


    private String adjustUrl(final HttpServletRequest httpServletRequest, final String url)
    {
        // The SOAP code of JIRA can work out what SOAP operation and parameters have been input.  However because they are
        // POST operations, we don't see that in access logs.  Therefore we can use the SOAP code to help us construct a more
        // meaningful url.  This relies of course on the SOAP code putting in the "magic" request parameters
        final Object soapUrlSuffix = httpServletRequest.getAttribute(JIRA_RPC_SOAP_URLSUFFIX);
        if (soapUrlSuffix != null)
        {
            final StringBuilder newUrl = new StringBuilder(url).append("/").append(soapUrlSuffix);
            return newUrl.toString();
        }
        else
        {
            return url;
        }
    }

    private String adjustUserName(final HttpServletRequest httpServletRequest, final String userName)
    {
        // Again the SOAP code can work out what the user name is that is making a SOAP HTTP request.
        // So if its there we can use it
        final Object soapUserName = httpServletRequest.getAttribute(JIRA_RPC_SOAP_USERNAME);
        if ((userName == null) && (soapUserName != null))
        {
            return String.valueOf(soapUserName);
        }
        else
        {
            return userName;
        }
    }


    /**
     * Returns true if the given URL is of our interest.
     * <p/>
     * Images (.gif, .png, .jpg, .ico) are potentially out of interest and if url ends with one of these extensions
     * false is returned.
     *
     * @param requestURL url to check
     * @return true if interesting
     */
    private boolean isInterestingUrl(final String requestURL)
    {
        if (requestURL != null)
        {
            // if its an image, consult the logger to decide whether we should be interested in it
            if (requestURL.endsWith(".gif") || requestURL.endsWith(".png") || requestURL.endsWith(".jpg") || requestURL.endsWith(".ico"))
            {
                return includeImagesLog.isInfoEnabled();
            }
        }
        return true;
    }

    /**
     * Does nothing
     */
    public void destroy()
    {
    }

}
