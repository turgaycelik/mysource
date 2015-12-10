package com.atlassian.jira.security.websudo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.JiraActionSupport;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;
import com.atlassian.jira.mock.servlet.MockHttpSession;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import org.junit.Before;
import org.junit.Test;

import webwork.action.Action;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class TestInternalWebSudoManagerImpl
{
    private ApplicationProperties settingsManager;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private HttpSession session;

    /* Class under test */
    private InternalWebSudoManagerImpl defaultWebSudoManager;

    private static final String WEBSUDO_REQUEST_ATTRIBUTE = "jira.websudo.request";
    private static final String WEBSUDO_SESSION_TIMESTAMP = "jira.websudo.timestamp";
    private static final long currentTimeMillis = System.currentTimeMillis();


    @Before
    public void setUp() throws Exception
    {
        session = new MockHttpSession();
        request = new MockHttpServletRequest(session);
        response = new MockHttpServletResponse();
        settingsManager = new MockApplicationProperties();
        settingsManager.setOption(APKeys.WebSudo.IS_DISABLED,false);
        settingsManager.setString(APKeys.WebSudo.TIMEOUT,"10000");
        defaultWebSudoManager = new InternalWebSudoManagerImpl(settingsManager){
            @Override
            long currentTimeMillis()
            {
                return currentTimeMillis;
            }
        };
    }

    private static class TestNoAnnotationAction extends JiraActionSupport
    {

        @Override
        public User getLoggedInUser()
        {
            return null;
        }

        @Override
        public String execute() throws Exception
        {
            return super.execute();
        }
    }

    @WebSudoRequired
    private static class TestClassRequiredAction extends JiraActionSupport
    {


        @Override
        public User getLoggedInUser()
        {
            return null;
        }

        @Override
        public String execute() throws Exception
        {
            return super.execute();
        }
    }

    @Test
    public void testMatchesRequired() throws Exception
    {
        assertNoMatch(TestNoAnnotationAction.class);
        assertMatch(TestClassRequiredAction.class);
    }

    @Test
    public void testStartSession()
    {
        session.setAttribute(WEBSUDO_SESSION_TIMESTAMP, currentTimeMillis);
        request.setAttribute(WEBSUDO_REQUEST_ATTRIBUTE, Boolean.TRUE);
        defaultWebSudoManager.startSession(request, response);
    }

    @Test
    public void testStartSessionEmptyRequest()
    {
        try
        {
            defaultWebSudoManager.startSession(null, null);
            fail("Should not work with null arguments");
        } catch (NullPointerException np)
        {
            // expected
        }
    }

    @Test
    public void testMarkWebSudoRequest()
    {
        request.setAttribute(WEBSUDO_REQUEST_ATTRIBUTE, Boolean.TRUE);
        defaultWebSudoManager.markWebSudoRequest(request);
    }

    @Test
    public void testMarkWebSudoRequestEmptyRequest()
    {
        request.setAttribute(WEBSUDO_REQUEST_ATTRIBUTE, Boolean.TRUE);
        defaultWebSudoManager.markWebSudoRequest(null);
    }

    @Test
    public void testIsNotAWebSudoRequest()
    {
        assertFalse(defaultWebSudoManager.isWebSudoRequest(request));
        request.setAttribute(WEBSUDO_REQUEST_ATTRIBUTE,false);
        assertFalse(defaultWebSudoManager.isWebSudoRequest(request));
    }

    @Test
    public void testIsWebSudoRequest()
    {
        request.setAttribute(WEBSUDO_REQUEST_ATTRIBUTE,true);
        assertTrue(defaultWebSudoManager.isWebSudoRequest(request));
    }

    @Test
    public void testInvalidateSession()
    {
        defaultWebSudoManager.invalidateSession(request, response);
        assertFalse(defaultWebSudoManager.hasValidSession(session));
    }

    @Test
    public void testNotHasValidSession()
    {
        assertFalse(defaultWebSudoManager.hasValidSession(session));
    }

    @Test
    public void testHasValidSessionExpired()
    {
        defaultWebSudoManager = new InternalWebSudoManagerImpl(settingsManager){
            @Override
            long currentTimeMillis()
            {
                return currentTimeMillis + 11 * 1000 * 60;
            }
        };

        assertFalse(defaultWebSudoManager.hasValidSession(session));
    }

    @Test
    public void testHasValidSession()
    {
        defaultWebSudoManager = new InternalWebSudoManagerImpl(settingsManager){
            @Override
            long currentTimeMillis()
            {
                return currentTimeMillis + 5 * 1000 * 60;
            }
        };
        session.setAttribute(WEBSUDO_SESSION_TIMESTAMP,currentTimeMillis);
        assertTrue(defaultWebSudoManager.hasValidSession(session));
    }


    /* ================================== helper methods ================================== */
    private void assertMatch(final Class<? extends Action> actionClass) throws NoSuchMethodException
    {
        assertTrue(defaultWebSudoManager.matches(actionClass));
    }

    private void assertNoMatch(final Class<? extends Action> actionClass) throws NoSuchMethodException
    {
        assertFalse(defaultWebSudoManager.matches(actionClass));
    }
}
