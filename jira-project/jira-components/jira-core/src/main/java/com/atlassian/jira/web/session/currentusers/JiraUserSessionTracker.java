package com.atlassian.jira.web.session.currentusers;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.http.HttpRequestType;
import com.atlassian.jira.web.filters.accesslog.AccessLogIPAddressUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_ASESSIONID;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_RPC_SOAP_SESSIONID;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_RPC_SOAP_USERNAME;

/**
 * The JiraUserSessionTracker keeps track of the "users" that have a "session" with JIRA.
 *
 * @since v4.0
 */
public class JiraUserSessionTracker
{
    private final ConcurrentMap<String, MutableJiraUserSession> sessionMap;
    private final JiraUserSessionTrackerDeletionStrategy deletionStrategy;

    /**
     * A {@link java.util.Comparator} that sorts by last access time ascending
     */
    public static final Comparator<JiraUserSession> BY_LAST_ACCESS_TIME = new Comparator<JiraUserSession>()
    {
        public int compare(final JiraUserSession o1, final JiraUserSession o2)
        {
            Date lat1 = o1.getLastAccessTime();
            Date lat2 = o2.getLastAccessTime();
            if (lat1 == null && lat2 == null)
            {
                return 0;
            }
            else if (lat1 == null)
            {
                return 1;
            }
            else if (lat2 == null)
            {
                return -1;
            }
            return lat1.compareTo(lat2);
        }
    };

    public JiraUserSessionTracker()
    {
        sessionMap = new ConcurrentHashMap<String, MutableJiraUserSession>();
        deletionStrategy = new JiraUserSessionTrackerDeletionStrategy();
    }

    /**
     * Hooks into the ComponentManager to get a singelton instance of JiraUserSessionTracker
     *
     * @return an singleton instance of JiraUserSessionTracker
     */
    public static JiraUserSessionTracker getInstance()
    {
        return ComponentAccessor.getComponentOfType(JiraUserSessionTracker.class);
    }

    /**
     * This method (in partnership with {@link com.atlassian.jira.web.filters.accesslog.AccessLogFilter}) decides what
     * type of request has occurred and record the appropriate information
     *
     * @param httpServletRequest the http request in play
     */
    public static void recordInteraction(HttpServletRequest httpServletRequest)
    {
        JiraUserSessionTracker tracker = getInstance();
        if (tracker != null)
        {
            tracker.recordInteractionImpl(httpServletRequest);
        }
    }

    /**
     * A package protected back door that allows {@link JiraUserSessionDestroyListener} to remove entries when sessions
     * expire.
     *
     * @param sessionId the id of the user session to remove
     */
    void removeSession(String sessionId)
    {
        sessionMap.remove(sessionId);
    }

    /**
     * A little factory to iron out the differences in creating MutableJiraUserSession  objects.
     */
    private interface MutableSessionFactory
    {
        MutableJiraUserSession createUserSession();
    }

    /**
     * This method (in partnership with {@link com.atlassian.jira.web.filters.accesslog.AccessLogFilter}) decides what
     * type of request has occurred and record the appropriate information
     *
     * @param httpServletRequest the http request in play
     */
    void recordInteractionImpl(final HttpServletRequest httpServletRequest)
    {
        String soapSessionId = (String) httpServletRequest.getAttribute(JIRA_RPC_SOAP_SESSIONID);
        if (soapSessionId != null)
        {
            String userName = (String) httpServletRequest.getAttribute(JIRA_RPC_SOAP_USERNAME);
            recordSoapInteraction(httpServletRequest, soapSessionId, soapSessionId, userName);
        }
        else
        {
            String asessionId = (String) httpServletRequest.getAttribute(JIRA_REQUEST_ASESSIONID);
            recordHttpInteraction(httpServletRequest, asessionId);
        }
    }

    /**
     * Records a HTTP request as occurring.  It will detect if a new session has been estblished or not.
     *
     * @param httpServletRequest the HttpServletRequest that is executing
     * @param asessionId the ASESSIONID in play
     */
    private void recordHttpInteraction(final HttpServletRequest httpServletRequest, final String asessionId)
    {
        final HttpSession httpSession = httpServletRequest.getSession(false);
        if (httpSession != null)
        {
            final String httpSessionId = httpSession.getId();
            recordInteractionImpl(httpSessionId, httpServletRequest, httpServletRequest.getRemoteUser(), new MutableSessionFactory()
            {
                public MutableJiraUserSession createUserSession()
                {
                    return new MutableJiraUserSession(determineRequestType(httpServletRequest), httpSessionId, asessionId);
                }

                private HttpRequestType determineRequestType(HttpServletRequest httpServletRequest)
                {
                    if (AccessLogIPAddressUtil.getShortenedRequestUrl(httpServletRequest).startsWith("/rest/")) {
                        return HttpRequestType.REST;
                    }
                    return HttpRequestType.HTTP;
                }
            });
        }
    }

    /**
     * Records a SOAP interaction as occurring.   It will detect if a new session has been established
     *
     * @param httpServletRequest the http request in play
     * @param sessionId the SOAP session id
     * @param asessionId the ASESSIONID in play
     * @param userName the username executing the SOAP request
     */
    private void recordSoapInteraction(final HttpServletRequest httpServletRequest, final String sessionId, final String asessionId, final String userName)
    {
        /**
         * Technically its possible that the SOAP sessionId can clash with a Servlet session id.  So we add a little prefix.
         */
        final String newSessionId = "S-" + sessionId;
        recordInteractionImpl(newSessionId, httpServletRequest, userName, new MutableSessionFactory()
        {
            public MutableJiraUserSession createUserSession()
            {
                return new MutableJiraUserSession(HttpRequestType.SOAP, newSessionId, asessionId, userName);
            }
        });
    }

    /**
     * This record the interaction as occurring.  NOTE : this will construct a new object if it thinks it has to and the
     * call {@link com.atlassian.jira.web.session.currentusers.MutableJiraUserSession#recordInteraction(javax.servlet.http.HttpServletRequest, String)} 
     * on the object that makes it into the session map.
     * <p/>
     * Therefore the constructued object should have things like request count set to zero so that recordInteraction
     * doesnt double count.
     *
     * @param sessionId the sessionId to sue as a lookup key
     * @param httpServletRequest the http servlet request in play
     * @param userName the user name in play
     * @param sessionFactory the factory that will create the right type of MutableJiraUserSession object
     */
    private void recordInteractionImpl(String sessionId, HttpServletRequest httpServletRequest, final String userName, MutableSessionFactory sessionFactory)
    {
        //
        // Ask the deletion strategy to have a go at deleting entries
        //
        // This is time based and hence wont be expensive to run every interaction
        // (other than a method call that is)
        //
        deletionStrategy.deleteStaleSessions(sessionMap);

        MutableJiraUserSession userSession = sessionMap.get(sessionId);
        if (userSession == null)
        {
            // ok probably haven't seen it so lets construct an object and try to put it in
            MutableJiraUserSession newSession = sessionFactory.createUserSession();
            MutableJiraUserSession previousValue = sessionMap.putIfAbsent(sessionId, newSession);
            userSession = (previousValue != null ? previousValue : newSession);
        }
        //
        // now record the fact that another request has happened
        userSession.recordInteraction(httpServletRequest, userName);

    }

    /**
     * This returns a "snapshot" list of sessions that are currently in JIRA, sorted by last access time descending
     *
     * @return a "snapshot" list of sessions that are currently in JIRA, sorted by last access time descending
     */
    public List<JiraUserSession> getSnapshot()
    {
        List<JiraUserSession> userSessions = new ArrayList<JiraUserSession>();
        for (JiraUserSession jiraUserSession : sessionMap.values())
        {
            userSessions.add(new SnapshotJiraUserUserSession(jiraUserSession));
        }
        Collections.sort(userSessions, Collections.reverseOrder(BY_LAST_ACCESS_TIME));
        return userSessions;
    }


}
