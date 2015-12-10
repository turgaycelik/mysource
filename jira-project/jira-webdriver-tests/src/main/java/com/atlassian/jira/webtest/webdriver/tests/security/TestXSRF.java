package com.atlassian.jira.webtest.webdriver.tests.security;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.admin.configuration.EditGeneralConfigurationPage;
import com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.util.UserSessionHelper;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudo;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudoPage;
import com.atlassian.jira.pageobjects.xsrf.XsrfPage;
import org.junit.Test;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @since v6.1
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXSRF extends BaseJiraWebTest
{
    @Test
    public void testXSRFErrorPageForAnonymousUsersContainRequestParameters()
    {
        // Go to an admin form
        final EditGeneralConfigurationPage page = jira.quickLoginAsSysadmin(ViewGeneralConfigurationPage.class).edit();

        // Kill the session
        final UserSessionHelper bind = pageBinder.bind(UserSessionHelper.class);
        bind.destoryAllXsrfTokens();
        bind.deleteSession();

        // Submit the form
        final XsrfPage xsrfPage = page.submit(XsrfPage.class);

        assertTrue(xsrfPage.hasRequestParameters());
    }

    @Test
    public void testXSRFErrorPageForLoggedInUsersDoesNotContainRequestParameters()
    {
        // Go to an admin form
        final EditGeneralConfigurationPage page = jira.quickLoginAsSysadmin(ViewGeneralConfigurationPage.class).edit();

        // Delete the XSRF token but preserve the session
        final UserSessionHelper bind = pageBinder.bind(UserSessionHelper.class);
        bind.destoryAllXsrfTokens();

        // Submit the form
        final XsrfPage xsrfPage = page.submit(XsrfPage.class);

        assertFalse(xsrfPage.hasRequestParameters());
    }

    @Test
    public void testXSRFErrorPageAfterWebSudoDoesNotContainThePassword()
    {
        // Go to an admin form
        final EditGeneralConfigurationPage page = jira.quickLoginAsSysadmin(ViewGeneralConfigurationPage.class).edit();
        backdoor.websudo().enable();

        final UserSessionHelper bind = pageBinder.bind(UserSessionHelper.class);
        bind.clearWebSudo();

        // Submit the form, will display the WebSudo form
        final JiraWebSudo webSudo = page.submit(JiraWebSudoPage.class, page.getUrl());

        // Kill the session
        bind.destoryAllXsrfTokens();
        bind.deleteSession();

        // Submit the WebSudo form, will get a XSRF error page
        final XsrfPage xsrfPage = webSudo.authenticate(JiraLoginPage.PASSWORD_ADMIN, XsrfPage.class);

        assertTrue(xsrfPage.hasRequestParameters());
        assertFalse(xsrfPage.hasRequestParameter("webSudoPassword"));

        backdoor.websudo().disable();
    }

    @Test
    public void testSessionExpireErrorPageForLoggedInUsersDoesNotContainthePassword()
    {
        // Go to an admin form
        final EditGeneralConfigurationPage page = jira.quickLoginAsSysadmin(ViewGeneralConfigurationPage.class).edit();

        // Delete the XSRF token but preserve the session
        final UserSessionHelper bind = pageBinder.bind(UserSessionHelper.class);
        bind.invalidateSession();
        bind.destoryAllXsrfTokens();

        // Submit the form
        final XsrfPage sessionExpiredPage = page.submit(XsrfPage.class);

        assertTrue(sessionExpiredPage.hasRequestParameters());
        assertFalse(sessionExpiredPage.hasRequestParameter("webSudoPassword"));
    }
}
