package com.atlassian.jira.web.filters;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.local.MockControllerTestCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestJiraCachingStrategy extends MockControllerTestCase
{
    private JiraCachingFilter.JiraCachingStrategy strategy;
    private HttpServletRequest servletRequest;

    @Before
    public void setUp() throws Exception
    {
        strategy = new JiraCachingFilter.JiraCachingStrategy();
        servletRequest = mockController.getMock(HttpServletRequest.class);
    }

    //JSPA should not be cached.
    @Test
    public void testMatchesJSPA()
    {
        servletRequest.getServletPath();
        mockController.setReturnValue("/secure/MyJiraHome.jspa");
        servletRequest.isSecure();
        mockController.setReturnValue(false);
        servletRequest.getRequestURI();
        mockController.setReturnValue("/secure/MyJiraHome.jspa");

        mockController.replay();

        final boolean nonCachableUri = strategy.matches(servletRequest);
        assertTrue(nonCachableUri);
    }

    //JSP should also not be cached.
    @Test
    public void testMatchesJSP()
    {
        servletRequest.getServletPath();
        mockController.setReturnValue("/secure/viewissue.jsp");
        servletRequest.isSecure();
        mockController.setReturnValue(false);
        servletRequest.getRequestURI();
        mockController.setReturnValue("/secure/viewissue.jsp");

        mockController.replay();

        final boolean nonCachableUriJsp = strategy.matches(servletRequest);
        assertTrue(nonCachableUriJsp);
    }

    //attachments over secure should not add no-cache headers
    @Test
    public void testMatchesAttachmentSecure()
    {
        servletRequest.getServletPath();
        mockController.setReturnValue("/attachment/");
        servletRequest.isSecure();
        mockController.setReturnValue(true);

        mockController.replay();

        final boolean nonCachableUriAttachmentSecure = strategy.matches(servletRequest);
        assertFalse(nonCachableUriAttachmentSecure);
    }

    //attachments over non-secure should not add no cache headers
    @Test
    public void testMatchesAttachment()
    {
        servletRequest.getServletPath();
        mockController.setReturnValue("/attachment/");
        servletRequest.isSecure();
        mockController.setReturnValue(false);
        servletRequest.getRequestURI();
        mockController.setReturnValue("/attachment/");

        mockController.replay();

        final boolean nonCachableUriAttachment = strategy.matches(servletRequest);
        assertFalse(nonCachableUriAttachment);
    }

    //Browse should add no-cache headers
    @Test
    public void testMatchesBrowse()
    {
        servletRequest.getServletPath();
        mockController.setReturnValue("/browse/JRA-123123");
        servletRequest.isSecure();
        mockController.setReturnValue(false);

        mockController.replay();

        final boolean nonCachableUriBrowse = strategy.matches(servletRequest);
        assertTrue(nonCachableUriBrowse);
    }

     //Browse should add no-cache headers
    @Test
    public void testMatchesOther()
    {
        servletRequest.getServletPath();
        mockController.setReturnValue("/someotherurl");
        servletRequest.isSecure();
        mockController.setReturnValue(false);
        servletRequest.getRequestURI();
        mockController.setReturnValue("/someotherurl");

        mockController.replay();

        final boolean nonCachableUriBrowse = strategy.matches(servletRequest);
        assertFalse(nonCachableUriBrowse);
    }
}
