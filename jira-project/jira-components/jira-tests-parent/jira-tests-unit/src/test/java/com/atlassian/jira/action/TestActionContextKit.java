package com.atlassian.jira.action;

import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;
import com.atlassian.jira.mock.servlet.MockServletContext;

import org.junit.Test;

import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestActionContextKit
{
    /**
     * Test whether we clean stuff up correctly from the viral thread locals in ActionContext
     */
    @Test
    public void testReset()
    {
        final MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        final MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        final MockServletContext mockServletContext = new MockServletContext();

        ActionContextKit.setContext(mockHttpServletRequest, mockHttpServletResponse, mockServletContext, "someAction");

        assertEquals("someAction", ActionContext.getName());
        assertEquals(mockHttpServletRequest, ActionContext.getRequest());
        assertEquals(mockHttpServletResponse, ActionContext.getResponse());
        assertEquals(mockServletContext, ActionContext.getServletContext());

        assertEquals(mockHttpServletRequest, ServletActionContext.getRequest());
        assertEquals(mockHttpServletResponse, ServletActionContext.getResponse());
        assertEquals(mockServletContext, ServletActionContext.getServletContext());

        ActionContextKit.resetContext();
        assertNullActionContext();

        // can we call it twice??
        ActionContextKit.resetContext();
        assertNullActionContext();
    }

    private void assertNullActionContext()
    {
        assertNull(ActionContext.getName());
        assertNull(ActionContext.getRequest());
        assertNull(ActionContext.getResponse());
        assertNull(ActionContext.getServletContext());

        assertNull(ServletActionContext.getRequest());
        assertNull(ServletActionContext.getResponse());
        assertNull(ServletActionContext.getServletContext());
    }
}
