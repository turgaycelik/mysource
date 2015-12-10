package com.atlassian.jira.web.action.util.navigator;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.mock.servlet.MockHttpServletResponse;
import com.atlassian.jira.web.SessionKeys;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Simple test for {@link TestIssueNavigatorType}.
 *
 * @since v4.0
 */
public class TestIssueNavigatorType
{
    @Test
    public void testGetFromSession()
    {
        //Should return NULL when nothing is in the session.
        final MockControl mockHttpServletRequestControl = MockControl.createStrictControl(HttpServletRequest.class);
        final HttpServletRequest mockHttpServletRequest = (HttpServletRequest) mockHttpServletRequestControl.getMock();
        mockHttpServletRequest.getCookies();
        mockHttpServletRequestControl.setReturnValue(new Cookie []{new Cookie(SessionKeys.ISSUE_NAVIGATOR_TYPE, IssueNavigatorType.ADVANCED.name())});
        mockHttpServletRequestControl.replay();

        assertEquals(IssueNavigatorType.ADVANCED, IssueNavigatorType.getFromCookie(mockHttpServletRequest));
        mockHttpServletRequestControl.verify();
    }

    @Test
    public void testGetFromSessionNoCookie()
    {
        //Should return NULL when nothing is in the session.
        final MockControl mockHttpServletRequestControl = MockControl.createStrictControl(HttpServletRequest.class);
        final HttpServletRequest mockHttpServletRequest = (HttpServletRequest) mockHttpServletRequestControl.getMock();
        mockHttpServletRequest.getCookies();
        mockHttpServletRequestControl.setReturnValue(new Cookie []{});
        mockHttpServletRequestControl.replay();
        assertEquals(IssueNavigatorType.SIMPLE, IssueNavigatorType.getFromCookie(mockHttpServletRequest));
        mockHttpServletRequestControl.verify();
    }

    @Test
    public void testSetInSession()
    {
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicBoolean advancedCalled = new AtomicBoolean(false);
        final AtomicBoolean simpleCalled = new AtomicBoolean(false);
        final MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse()
        {
            @Override
            public void addCookie(final Cookie cookie)
            {
                if (count.get() == 0)
                {
                    advancedCalled.set(true);
                    assertEquals(IssueNavigatorType.ADVANCED.name(), cookie.getValue());
                    count.incrementAndGet();
                }
                else
                {
                    simpleCalled.set(true);
                    assertEquals(IssueNavigatorType.SIMPLE.name(), cookie.getValue());
                }
            }
        };

        //Make sure the tab is correctly updated in the session.
        IssueNavigatorType.setInCookie(mockHttpServletResponse, IssueNavigatorType.ADVANCED);

        //Make sure the tab is correctly updated in the session.
        IssueNavigatorType.setInCookie(mockHttpServletResponse, IssueNavigatorType.SIMPLE);

        assertTrue(advancedCalled.get());
        assertTrue(simpleCalled.get());
    }

    @Test
    public void testClearSession()
    {
        final Cookie cookie = new Cookie(SessionKeys.ISSUE_NAVIGATOR_TYPE, IssueNavigatorType.ADVANCED.name());

        final MockControl mockHttpServletRequestControl = MockControl.createStrictControl(HttpServletRequest.class);
        final HttpServletRequest mockHttpServletRequest = (HttpServletRequest) mockHttpServletRequestControl.getMock();
        mockHttpServletRequest.getCookies();
        mockHttpServletRequestControl.setReturnValue(new Cookie []{cookie});
        mockHttpServletRequestControl.replay();

        //Make sure the tab is removed from the session.
        IssueNavigatorType.clearCookie(mockHttpServletRequest);
        assertNull(cookie.getValue());
    }

    @Test
    public void testGetTabForString()
    {
        assertEquals(IssueNavigatorType.ADVANCED, IssueNavigatorType.getTypeFromString("AdVaNcEd"));
        assertEquals(IssueNavigatorType.ADVANCED, IssueNavigatorType.getTypeFromString("advanced"));
        assertEquals(IssueNavigatorType.ADVANCED, IssueNavigatorType.getTypeFromString("ADVANCED"));

        assertEquals(IssueNavigatorType.SIMPLE, IssueNavigatorType.getTypeFromString("SiMpLe"));
        assertEquals(IssueNavigatorType.SIMPLE, IssueNavigatorType.getTypeFromString("simple"));
        assertEquals(IssueNavigatorType.SIMPLE, IssueNavigatorType.getTypeFromString("SIMPLE"));
    }

    /**
     * Should return null if the show
     */
    @Test
    public void testGetTabForStringBad()
    {
        assertNull(IssueNavigatorType.getTypeFromString("shos"));
        assertNull(IssueNavigatorType.getTypeFromString("shossw"));
        assertNull(IssueNavigatorType.getTypeFromString("shows"));
    }
}
