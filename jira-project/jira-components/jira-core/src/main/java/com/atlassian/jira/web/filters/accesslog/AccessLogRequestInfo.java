package com.atlassian.jira.web.filters.accesslog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.atlassian.jira.bc.security.login.LoginLoggers;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.log.Log4jKit;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

/**
 * AccessLogRequestInfo is used to place request information into the HTTP request itself and to keep count of the
 * number of requests coming into and out of JIRA
 *
 * @since v4.1
 */
public class AccessLogRequestInfo
{
    /**
     * This constant is used by the SOAP code to place the SOAP username into the request
     */
    public static final String JIRA_RPC_SOAP_USERNAME = "jira.rpc.soap.username";
    /**
     * This constant is used by the SOAP code to place the SOAP method url suffix into the request
     */
    public static final String JIRA_RPC_SOAP_URLSUFFIX = "jira.rpc.soap.urlsuffix";
    /**
     * This constant is used by the SOAP code to place the SOAP session id into the request
     */
    public static final String JIRA_RPC_SOAP_SESSIONID = "jira.rpc.soap.soap.sessionid";

    /**
     * This constant is used by the SOAP code to place the SOAP username into the request
     */
    public static final String JIRA_REQUEST_USER_NAME = "jira.request.username";

    /**
     * This constant is used to indicate the start time of the request
     */
    public static final String JIRA_REQUEST_START_MILLIS = "jira.request.start.millis";
    /**
     * This constant is used to indicate the time the request took in microseconds
     */
    public static final String JIRA_REQUEST_TIME_MICROS = "jira.request.time.micros";
    /**
     * This constant is used to indicate the request id
     */
    public static final String JIRA_REQUEST_ID = "jira.request.id";
    /**
     * This constant is used to indicate the ASESSIONID
     */
    public static final String JIRA_REQUEST_ASESSIONID = "jira.request.assession.id";
    /**
     * This constant is used to indicate the last access time of the session
     */
    public static final String JIRA_SESSION_LAST_ACCESSED_TIME = "jira.session.last.accessed.time";
    /**
     * This constant is used to indicate the max inactive time for the current session configuration
     */
    public static final String JIRA_SESSION_MAX_INACTIVE_INTERVAL = "jira.session.max.inactive.interval";

    public static final String X_ASESSIONID_HEADER = "X-ASESSIONID";
    public static final String X_REQUESTID_HEADER = "X-AREQUESTID";
    public static final String X_NODEID_HEADER = "X-ANODEID";
    public static final String X_USER_NAME_HEADER = "X-AUSERNAME";

    /**
     * This atomic long counter increments for every new request that hits JIRA
     */
    public static final AtomicLong requestCounter = new AtomicLong(0);
    /**
     * This atomic long gauge goes up and down for every request that hits JIRA
     */
    public static final AtomicLong concurrentRequests = new AtomicLong(0);


    /**
     * Called to generate a request id and atlassian session id for the given request.  This can be called multiple
     * times and it it will only set the information in once per request.
     *
     * @param httpServletRequest  the HTTP request in play
     * @param httpServletResponse the HTTP response for setting the headers into
     */
    public void enterRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    {
        // don't do it twice for the same request
        if (httpServletRequest.getAttribute(JIRA_REQUEST_START_MILLIS) == null)
        {
            httpServletRequest.setAttribute(JIRA_REQUEST_START_MILLIS, System.currentTimeMillis());

            //
            // tweak counters and give this request and id
            final long concurrentCount = concurrentRequests.incrementAndGet();
            final long requestCount = requestCounter.incrementAndGet();
            final String requestId = generateUniqueRequestId(requestCount, concurrentCount);
            final String atlassianSessionId = AtlassianSessionIdUtil.generateAtlassianSessionHash(httpServletRequest, httpServletResponse);

            //
            // now set the magic values into the request
            httpServletRequest.setAttribute(JIRA_REQUEST_ID, requestId);
            httpServletRequest.setAttribute(JIRA_REQUEST_ASESSIONID, atlassianSessionId);
            httpServletRequest.setAttribute(JIRA_REQUEST_USER_NAME, null);
            httpServletRequest.setAttribute(JIRA_RPC_SOAP_SESSIONID, null);
            httpServletRequest.setAttribute(JIRA_RPC_SOAP_USERNAME, null);
            httpServletRequest.setAttribute(JIRA_RPC_SOAP_URLSUFFIX, null);

            setHeader(httpServletResponse, X_REQUESTID_HEADER, requestId);
            setHeader(httpServletResponse, X_ASESSIONID_HEADER, atlassianSessionId);

            recordNodeInformation(httpServletResponse);

            recordLog4JInformation(httpServletRequest, requestId, atlassianSessionId);

            recordSessionInformation(httpServletRequest);

            examineCookies(httpServletRequest);
        }
        //
        // on the first filter chain call for a request we may NOT have established a ASESSIONID yet
        // so we allow it to be called until we do establish it.  The reason this happens is that the Serpah SecurityFilter
        // establishes the session and yet its not first in the chain so AcessLogfilter has anothe got at it
        //
        if (httpServletRequest.getAttribute(JIRA_REQUEST_ASESSIONID) == null)
        {
            final String atlassianSessionId = AtlassianSessionIdUtil.generateAtlassianSessionHash(httpServletRequest, httpServletResponse);

            httpServletRequest.setAttribute(JIRA_REQUEST_ASESSIONID, atlassianSessionId);
            setHeader(httpServletResponse, X_ASESSIONID_HEADER, atlassianSessionId);

            Log4jKit.putASessionIdToMDC(atlassianSessionId);
        }
    }

    private void recordNodeInformation(final HttpServletResponse httpServletResponse)
    {
        final ClusterManager clusterManager = ComponentAccessor.getComponent(ClusterManager.class);
        if (clusterManager != null && clusterManager.isClustered())
        {
            setHeader(httpServletResponse, X_NODEID_HEADER, clusterManager.getNodeId());
        }
    }

    private static final String EXIT_CALLED = AccessLogRequestInfo.class.getName() + "exit.called";

    /**
     * This is called to exit the request.  It will decrement the number of concurrent requests in play assuming it has
     * not bee called before for this request
     *
     * @param httpServletRequest the HTTP request in play
     */
    public void exitRequest(HttpServletRequest httpServletRequest)
    {
        if (httpServletRequest.getAttribute(EXIT_CALLED) == null)
        {
            concurrentRequests.decrementAndGet();
            Log4jKit.clearMDC();

            // we know this is thread safe because each request is unique per thread!
            httpServletRequest.setAttribute(EXIT_CALLED, Boolean.TRUE);
        }
    }


    /**
     * ONLY sets a header value if its non null
     *
     * @param httpServletResponse the HTTP response in play
     * @param headerName          the name of the header to set
     * @param newValue            the header value
     */
    private void setHeader(final HttpServletResponse httpServletResponse, final String headerName, final String newValue)
    {
        // Put the value in the response as a header, perhaps for down stream processes
        // Note: This blows up on certain app servers such WebSphere 6.1 if the value is null. However, we ensure that
        // at this stage, the value is not null.
        if (newValue != null)
        {
            httpServletResponse.addHeader(headerName, newValue);
        }
    }

    private void recordLog4JInformation(final HttpServletRequest httpServletRequest, final String requestId, final String atlassianSessionId)
    {
        //
        // clear the log4j MDC ThreadLocal first
        Log4jKit.clearMDC();

        // now add information to it
        final String url = AccessLogIPAddressUtil.getShortenedRequestUrl(httpServletRequest);
        final String ipAddr = AccessLogIPAddressUtil.getRemoteAddr(httpServletRequest);
        Log4jKit.putToMDC(null, requestId, atlassianSessionId, url, ipAddr);
    }

    private void recordSessionInformation(final HttpServletRequest httpServletRequest)
    {
        final HttpSession httpSession = httpServletRequest.getSession(false);
        if (httpSession != null)
        {
            httpServletRequest.setAttribute(JIRA_SESSION_LAST_ACCESSED_TIME,httpSession.getLastAccessedTime());
            httpServletRequest.setAttribute(JIRA_SESSION_MAX_INACTIVE_INTERVAL,httpSession.getMaxInactiveInterval());
        }
    }


    /**
     * A unique request id based on date/time and atomic long value
     *
     * @param requestCount       the current request count
     * @param concurrentRequests the current concurrent count
     *
     * @return a unique ID
     */
    private String generateUniqueRequestId(final long requestCount, final long concurrentRequests)
    {
        // this gives us a time potion in the range 0 <= val <= 86400
        final long sinceEpoch = generateEpochValue();
        //
        // because we have an atomic request count, the combinations of time since epoch and request count and concurrent load
        // will generate a pretty unique value.  We want it to be short for reading but unique and increasing for debugging purposes
        //
        return String.valueOf(sinceEpoch) + 'x' + requestCount + 'x' + concurrentRequests;
    }

    /**
     * @return minutes since midnight today!
     */
    private long generateEpochValue()
    {
        return new DateTime().getMinuteOfDay();
    }

    private void examineCookies(final HttpServletRequest httpServletRequest)
    {
        final Logger log = LoginLoggers.LOGIN_COOKIE_LOG;
        if (log.isDebugEnabled())
        {
            // did they turn up without a JSESSION ID
            final String cookieHeader = httpServletRequest.getHeader("Cookie");
            final Cookie[] allCookies = httpServletRequest.getCookies();

            if (cookieHeader == null)
            {
                log.debug("There is no cookie header.");
            }
            else
            {
                log.debug("The cookie header is '" + cookieHeader.length() + "' characters : '" + cookieHeader + "'.");
            }

            final List<Cookie> jsessionCookies = getCookiesNamed("JSESSIONID", allCookies);
            if (jsessionCookies.size() == 0)
            {
                log.debug("The request has arrived WITHOUT a JSESSIONID cookie");
            }
            if (jsessionCookies.size() > 1)
            {
                log.debug("The request has arrived WITH MULTIPLE JSESSIONID cookies : " + jsessionCookies.size());
            }
        }
    }

    private List<Cookie> getCookiesNamed(final String targetCookieName, final Cookie[] allCookies)
    {
        final List<Cookie> namedCookies = new ArrayList<Cookie>();
        if (allCookies != null)
        {
            for (Cookie cookie : allCookies)
            {
                if (targetCookieName.equals(cookie.getName()))
                {
                    namedCookies.add(new Cookie(cookie.getName(), cookie.getValue()));
                }
            }
        }
        return namedCookies;
    }


}
