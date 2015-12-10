package com.atlassian.jira.webtests.ztests.security;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * These tests were written in response to SER-127, and SER-128, which were header injection and phishing attacks on
 * redirect after login.
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY })
public class TestRedirectAfterLogin extends FuncTestCase
{
    private String context;

    @Override
    public void setUpTest()
    {
        administration.restoreBlankInstance();

        final String xsrfTokenValue = page.getXsrfToken();
        navigation.logout();

        // Now we should be on page http://localhost:8090/jira/secure/Logout!default?atl_token=XXXXX.jspa
        String expectedLogoutPage = "/secure/Logout!default.jspa?" + XsrfCheck.ATL_TOKEN +"="+ xsrfTokenValue;
        assertTrue(getCurrentUrl().endsWith(expectedLogoutPage));

        // Find the context of the URL eg "http://localhost:8090/jira"
        context = getCurrentUrl().substring(0, getCurrentUrl().length() - expectedLogoutPage.length());
    }

    public void testRedirectWithRelativeUrl() throws Exception
    {
        // Login with a redirect
        tester.gotoPage("login.jsp?os_destination=%2Fsecure%2Fadmin%2Fuser%2FUserBrowser.jspa");
        tester.setFormElement("os_username", ADMIN_USERNAME);
        tester.setFormElement("os_password", ADMIN_USERNAME);
        tester.setWorkingForm("login-form");
        tester.submit();

        // We should redirect to the User Browser page
        assertEquals(context + "/secure/admin/user/UserBrowser.jspa", getCurrentUrl());
        tester.assertTextPresent("Filter Users");
    }

    public void testRedirectWithAbsoluteUrlSameContext() throws Exception
    {
        String absoluteUrl = context + "/secure/admin/user/UserBrowser.jspa";

        // Login with a redirect
        tester.gotoPage("login.jsp?os_destination=" + URLEncoder.encode(absoluteUrl, System.getProperty("file.encoding")));

        tester.setFormElement("os_username", ADMIN_USERNAME);
        tester.setFormElement("os_password", ADMIN_USERNAME);
        tester.setWorkingForm("login-form");
        tester.submit();

        // We should redirect to the User Browser page
        assertEquals(context + "/secure/admin/user/UserBrowser.jspa", getCurrentUrl());
        tester.assertTextPresent("Filter Users");
    }

    public void testRedirectWithAbsoluteUrlDifferentContext() throws Exception
    {        
        String absoluteUrl = "http://www.atlassian.com";

        // Login with a redirect
        tester.gotoPage("login.jsp?os_destination=" + URLEncoder.encode(absoluteUrl, System.getProperty("file.encoding")));

        tester.setFormElement("os_username", ADMIN_USERNAME);
        tester.setFormElement("os_password", ADMIN_USERNAME);
        tester.setWorkingForm("login-form");
        tester.submit();

        // The requested redirect should be rejected, and we will be taken to the JIRA homepage
        assertEquals(context + "/secure/Dashboard.jspa", getCurrentUrl());
        tester.assertTextPresent("jWebTest JIRA installation");
    }

    public void testRedirectWithHeaderInjectionCRLF() throws Exception
    {
        _testRedirectWithHeaderInjection("\r\n");
    }

    public void testRedirectWithHeaderInjectionCR() throws Exception
    {
        _testRedirectWithHeaderInjection("\r");
    }

    public void testRedirectWithHeaderInjectionLF() throws Exception
    {
        _testRedirectWithHeaderInjection("\n");
    }

    private void _testRedirectWithHeaderInjection(final String newlineChars) throws UnsupportedEncodingException
    {        
        // Login with a redirect
        tester.gotoPage("login.jsp?os_destination=%2Fsecure%2Fadmin%2Fuser%2FUserBrowser.jspa" +
                        URLEncoder.encode(newlineChars + "http://www.atlassian.com", System.getProperty("file.encoding")));

        tester.setFormElement("os_username", ADMIN_USERNAME);
        tester.setFormElement("os_password", ADMIN_USERNAME);
        tester.setWorkingForm("login-form");
        tester.submit();

        // The requested redirect should be rejected, and we will be taken to the JIRA homepage
        assertEquals(context + "/secure/Dashboard.jspa", getCurrentUrl());
        tester.assertTextPresent("jWebTest JIRA installation");
    }

    private String getCurrentUrl()
    {
        return tester.getDialog().getWebClient().getCurrentPage().getURL().toString();
    }
}
