package com.atlassian.jira.web.session.currentusers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionEvent;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.util.http.HttpRequestType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_ASESSIONID;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_START_MILLIS;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_RPC_SOAP_SESSIONID;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_RPC_SOAP_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @since v4.1
 */
@RunWith(ListeningMockitoRunner.class)
public class TestJiraUserSessionTracker
{
    Date WAYBACK = new Date(1);

    @Mock
    private EventPublisher eventPublisher;

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                .addMock(EventPublisher.class, eventPublisher)
        );
    }

    @Test
    public void testBasicRecord()
    {
        MockHttpServletRequest request = HttpTestKit.makeSessionRequest("S123", "A123", WAYBACK.getTime(), "remoteUser");

        JiraUserSessionTracker sessionTracker = new JiraUserSessionTracker();
        sessionTracker.recordInteractionImpl(request);

        List<JiraUserSession> snapshot = sessionTracker.getSnapshot();
        assertNotNull(snapshot);
        assertEquals(1, snapshot.size());

        JiraUserSession jiraUserSession = snapshot.get(0);
        assertEquals("S123", jiraUserSession.getId());
        assertEquals("A123", jiraUserSession.getASessionId());
        assertEquals(HttpRequestType.HTTP, jiraUserSession.getType());
        assertEquals("127.0.0.1", jiraUserSession.getIpAddress());
        assertEquals(1, jiraUserSession.getRequestCount());


        // now make another request for that guy
        final long later = WAYBACK.getTime() + 10;
        request = HttpTestKit.makeSessionRequest("S123", "A123", later, "remoteUser");
        sessionTracker.recordInteractionImpl(request);

        snapshot = sessionTracker.getSnapshot();
        jiraUserSession = snapshot.get(0);
        assertEquals("S123", jiraUserSession.getId());
        assertEquals("A123", jiraUserSession.getASessionId());
        assertEquals(HttpRequestType.HTTP, jiraUserSession.getType());
        assertEquals(2, jiraUserSession.getRequestCount());

    }

    @Test
    public void testSoapRecord()
    {
        MockHttpServletRequest request = HttpTestKit.makeSoapSessionRequest("OAP123", "soapY");

        JiraUserSessionTracker sessionTracker = new JiraUserSessionTracker();
        sessionTracker.recordInteractionImpl(request);

        List<JiraUserSession> snapshot = sessionTracker.getSnapshot();
        assertNotNull(snapshot);
        assertEquals(1, snapshot.size());

        JiraUserSession jiraUserSession = snapshot.get(0);
        assertEquals("S-OAP123", jiraUserSession.getId());
        assertEquals("OAP123", jiraUserSession.getASessionId());
        assertEquals(HttpRequestType.SOAP, jiraUserSession.getType());
        assertEquals("127.0.0.1", jiraUserSession.getIpAddress());
        assertEquals(1, jiraUserSession.getRequestCount());

        assertNotNull(jiraUserSession.getCreationTime());
        assertNotNull(jiraUserSession.getLastAccessTime());

        // and another request
        request = HttpTestKit.makeSoapSessionRequest("OAP123", "soapY");
        sessionTracker.recordInteractionImpl(request);

        snapshot = sessionTracker.getSnapshot();
        assertEquals(1, snapshot.size());

        jiraUserSession = snapshot.get(0);
        assertEquals("S-OAP123", jiraUserSession.getId());
        assertEquals("OAP123", jiraUserSession.getASessionId());
        assertEquals(HttpRequestType.SOAP, jiraUserSession.getType());
        assertEquals(2, jiraUserSession.getRequestCount());
    }


    @Test
    public void testOrdering() throws InterruptedException
    {
        /*
        SOAP requests always use right now as their last access time. Its doe at the time the request is recorded 
         */
        MockHttpServletRequest soap1 = HttpTestKit.makeSoapSessionRequest("OAP1", "soapY");
        MockHttpServletRequest soap2 = HttpTestKit.makeSoapSessionRequest("OAP2", "soapY");
        MockHttpServletRequest http1 = HttpTestKit.makeSessionRequest("H1", "A567", WAYBACK.getTime() + 100, "remoteuser1");
        MockHttpServletRequest http2 = HttpTestKit.makeSessionRequest("H2", "A789", WAYBACK.getTime(), "remoteuser1");


        JiraUserSessionTracker sessionTracker = new JiraUserSessionTracker();

        Thread.sleep(100);
        sessionTracker.recordInteractionImpl(soap2);

        Thread.sleep(100);
        sessionTracker.recordInteractionImpl(soap1);

        Thread.sleep(100);
        sessionTracker.recordInteractionImpl(http1);

        Thread.sleep(100);
        sessionTracker.recordInteractionImpl(http2);

        List<JiraUserSession> snapshot = sessionTracker.getSnapshot();
        assertEquals(4, snapshot.size());

        List<String> expectedIds = Arrays.asList("H2", "H1", "S-OAP1", "S-OAP2");
        List<HttpRequestType> expectedTypes = Arrays.asList(HttpRequestType.HTTP, HttpRequestType.HTTP, HttpRequestType.SOAP, HttpRequestType.SOAP);
        int i = 0;
        for (JiraUserSession jiraUserSession : snapshot)
        {
            assertEquals(expectedIds.get(i), jiraUserSession.getId());
            assertEquals(expectedTypes.get(i), jiraUserSession.getType());
            i++;
        }
    }

    @Test
    public void testRemove()
    {
        MockHttpServletRequest request = HttpTestKit.makeSessionRequest("S123", "A123", WAYBACK.getTime(), "remoteUser");

        JiraUserSessionTracker sessionTracker = new JiraUserSessionTracker();
        sessionTracker.recordInteractionImpl(request);

        sessionTracker.removeSession("rubbish");
        List<JiraUserSession> snapshot = sessionTracker.getSnapshot();
        assertEquals(1, snapshot.size());

        sessionTracker.removeSession("S123");
        snapshot = sessionTracker.getSnapshot();
        assertEquals(0, snapshot.size());
    }


    @Test
    public void testHttpSessionListenerRemove()
    {
        MockHttpServletRequest request = TestJiraUserSessionTracker.HttpTestKit.makeSessionRequest("S123", "A123", System.currentTimeMillis(), "remoteUser");
        final JiraUserSessionTracker sessionTracker = new JiraUserSessionTracker();
        sessionTracker.recordInteractionImpl(request);

        JiraUserSessionDestroyListener destroyListener = new JiraUserSessionDestroyListener()
        {
            @Override
            JiraUserSessionTracker getJiraSessionTracker()
            {
                return sessionTracker;
            }

            @Override
            boolean isPluginsUp()
            {
                return true;
            }
        };

        HttpSessionEvent sessionEvent = new HttpSessionEvent(TestJiraUserSessionTracker.HttpTestKit.makeHttpSession(1, "rubbish"));
        destroyListener.sessionDestroyed(sessionEvent);


        List<JiraUserSession> snapshot = sessionTracker.getSnapshot();
        assertEquals(1, snapshot.size());

        sessionEvent = new HttpSessionEvent(TestJiraUserSessionTracker.HttpTestKit.makeHttpSession(1, "S123"));
        destroyListener.sessionDestroyed(sessionEvent);

        snapshot = sessionTracker.getSnapshot();
        assertEquals(0, snapshot.size());
    }

    @Test
    public void testHttpSessionListenerRemoveDoesNotRemoveSessionWhenPluginDown()
    {
        MockHttpServletRequest request = TestJiraUserSessionTracker.HttpTestKit.makeSessionRequest("S123", "A123", System.currentTimeMillis(), "remoteUser");
        final JiraUserSessionTracker sessionTracker = new JiraUserSessionTracker();
        sessionTracker.recordInteractionImpl(request);

        JiraUserSessionDestroyListener destroyListener = new JiraUserSessionDestroyListener()
        {
            @Override
            JiraUserSessionTracker getJiraSessionTracker()
            {
                return sessionTracker;
            }

            @Override
            boolean isPluginsUp()
            {
                return false;
            }
        };

        HttpSessionEvent sessionEvent = new HttpSessionEvent(TestJiraUserSessionTracker.HttpTestKit.makeHttpSession(1, "S123"));
        destroyListener.sessionDestroyed(sessionEvent);

        List<JiraUserSession> snapshot = sessionTracker.getSnapshot();
        assertEquals(1, snapshot.size());
    }


    static class HttpTestKit
    {

        private static List sessionList = new ArrayList();

        static MockHttpServletRequest makeSoapSessionRequest(final String sessionId, final String userName)
        {
            MockHttpServletRequest request = makeSessionRequest(sessionId, sessionId, 0, "");
            request.setAttribute(JIRA_RPC_SOAP_SESSIONID, sessionId);
            request.setAttribute(JIRA_RPC_SOAP_USERNAME, userName);
            return request;
        }

        static MockHttpServletRequest makeSessionRequest(final String sessionId, final String aSessionId, final long startMillis, final String remoteUser)
        {
            return new MockHttpServletRequest()
            {
                {
                    setAttribute(JIRA_REQUEST_ASESSIONID, aSessionId);
                    setAttribute(JIRA_REQUEST_START_MILLIS, startMillis);
                }

                @Override
                public Object getAttribute(final String key)
                {
                    return super.getAttribute(key);
                }

                @Override
                public String getRemoteUser()
                {
                    return remoteUser;
                }

                @Override
                public String getRemoteAddr()
                {
                    return "127.0.0.1";
                }

                @Override
                public HttpSession getSession(final boolean b)
                {
                    return makeHttpSession(startMillis, sessionId);
                }
            };

        }

        static HttpSession makeHttpSession(final long startMillis, final String sessionId)
        {
            HttpSession session = new HttpSession()
            {
                public long getCreationTime()
                {
                    return startMillis;
                }

                public String getId()
                {
                    return sessionId;
                }

                public long getLastAccessedTime()
                {
                    return startMillis;
                }

                public ServletContext getServletContext()
                {
                    return null;
                }

                public void setMaxInactiveInterval(final int i)
                {
                }

                public int getMaxInactiveInterval()
                {
                    return 0;
                }

                @SuppressWarnings ({ "deprecation" })
                public HttpSessionContext getSessionContext()
                {
                    return null;
                }

                public Object getAttribute(final String s)
                {
                    return null;
                }

                public Object getValue(final String s)
                {
                    return null;
                }

                public Enumeration getAttributeNames()
                {
                    return null;
                }

                public String[] getValueNames()
                {
                    return new String[0];
                }

                public void setAttribute(final String s, final Object o)
                {
                }

                public void putValue(final String s, final Object o)
                {
                }

                public void removeAttribute(final String s)
                {
                }

                public void removeValue(final String s)
                {
                }

                public void invalidate()
                {
                }

                public boolean isNew()
                {
                    return false;
                }
            };
            // we have to retain a reference since weak references are used and we dont want these to dry up
            //noinspection unchecked
            sessionList.add(session);
            return session;
        }
    }
}
