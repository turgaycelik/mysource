package com.atlassian.jira.webtest.webdriver.tests.websudo;

/**
 * @since v5.0
 */

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.PluginsOverviewPage;
import com.atlassian.jira.pageobjects.pages.admin.ViewProjectsPage;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudoBanner;
import com.atlassian.jira.pageobjects.config.EnableWebSudo;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudoPage;
import com.atlassian.jira.pageobjects.pages.admin.ViewAttachmentsSettingsPage;
import com.atlassian.jira.pageobjects.project.DeleteProjectPage;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.pageobjects.page.HomePage;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@WebTest ({ Category.WEBDRIVER_TEST })
@Ignore("JRADEV-18418 - Tests are incredibly slow and need to be revised for performance.")
public class TestWebSudo  extends BaseJiraWebTest
{
    private static final String PROJECT_KEY = "HSP";
    private String passwordForWebSudo;

    @Before
    public void setup()
    {
        backdoor.restoreBlankInstance();

        final JiraLoginPage jiraLoginPage = jira.gotoLoginPage();
        passwordForWebSudo = jiraLoginPage.PASSWORD_ADMIN;
        jiraLoginPage.loginAsSysAdmin(HomePage.class);
    }

    private void triggerWebSudo()
    {
        jira.visitDelayed(ViewAttachmentsSettingsPage.class);
        final JiraWebSudoPage websudoPage = pageBinder.bind(JiraWebSudoPage.class);

        assertTrue(websudoPage.isAt().now());

        final ViewAttachmentsSettingsPage viewAttachmentSettings = websudoPage.confirm(passwordForWebSudo, ViewAttachmentsSettingsPage.class);

        assertTrue(viewAttachmentSettings.isAt().now());
    }

//    https://jdog.atlassian.net/browse/FLAKY-470
//    @Test
//    @EnableWebSudo
//    public void testWebSudoDoesNotRedirectToParameterReturnUrl()
//    {
//        jira.visitDelayed(DeleteProjectPage.class, 10000L);
//        JiraWebSudoPage websudoPage = pageBinder.bind(JiraWebSudoPage.class);
//
//        assertTrue(websudoPage.isAt().now());
//
//        DeleteProjectPage deleteProjectPage = websudoPage.confirm(passwordForWebSudo, DeleteProjectPage.class);
//
//        assertTrue(deleteProjectPage.isAt().now());
//    }

    @Test
    @EnableWebSudo
    public void testInvalidateSessionOnNewWebSudo()
    {
        final String before = jira.getTester().getDriver().manage().getCookieNamed("JSESSIONID").getValue();

        jira.visitDelayed(DeleteProjectPage.class, 10000L);
        final JiraWebSudoPage websudoPage = pageBinder.bind(JiraWebSudoPage.class);
        assertTrue(websudoPage.isAt().now());
        websudoPage.confirm(passwordForWebSudo, DeleteProjectPage.class);

        final String after = jira.getTester().getDriver().manage().getCookieNamed("JSESSIONID").getValue();

        assertThat("Http Session was not invalidated when new web sudo session was started (core admin pages)", after, Matchers.not(before));
    }

    @Test
    @EnableWebSudo
    public void testInvalidateSessionOnNewWebSudoOnPluginAdminPage()
    {
        final String before = jira.getTester().getDriver().manage().getCookieNamed("JSESSIONID").getValue();

        jira.visitDelayed(PluginsOverviewPage.class);
        final JiraWebSudoPage websudoPage = pageBinder.bind(JiraWebSudoPage.class);
        assertTrue(websudoPage.isAt().now());
        websudoPage.confirm(passwordForWebSudo, PluginsOverviewPage.class);
        
        final String after = jira.getTester().getDriver().manage().getCookieNamed("JSESSIONID").getValue();

        assertThat("Http Session was not invalidated when new web sudo session was started (plugin admin pages)", after, Matchers.not(before));
    }

    @Test
    @EnableWebSudo
    public void testWebSudoLoginPageAppearsOnlyOnceForProtectedPages ()
    {

        triggerWebSudo();
        final ViewAttachmentsSettingsPage viewAttachmentSettings = jira.visit(ViewAttachmentsSettingsPage.class);
        assertTrue(viewAttachmentSettings.isAt().now());
    }

    @Test
    @EnableWebSudo
    public void testWebSudoLoginPageSkippedForNormalPages ()
    {
        final ViewProjectsPage viewProjectsPage = jira.visit(ViewProjectsPage.class);
        assertTrue(viewProjectsPage.isAt().now());
    }

    @Test
    @EnableWebSudo
    public void testWebSudoBannerDisappearsAfterDropOnNormalPages ()
    {
        triggerWebSudo();

        jira.visit(ViewProjectsPage.class);

        JiraWebSudoBanner webSudoBanner = pageBinder.bind(JiraWebSudoBanner.class);
        assertTrue(webSudoBanner.isShowing());
        assertTrue(webSudoBanner.hasNormalDropLink());
        assertFalse(webSudoBanner.hasProdectedDropLink());

        final String oldLocation = jira.getTester().getDriver().getCurrentUrl();

        webSudoBanner.dropWebSudo(ViewProjectsPage.class);

        assertEquals("The old location and the current location should match.", oldLocation,
                jira.getTester().getDriver().getCurrentUrl());

        assertFalse(webSudoBanner.isShowing());

        jira.visit(DashboardPage.class);
        webSudoBanner = pageBinder.bind(JiraWebSudoBanner.class);

        assertFalse(webSudoBanner.isShowing());
    }

    @Test
    @EnableWebSudo
    public void testWebSudoCancelRedirect ()
    {
        jira.visit(DashboardPage.class);

        jira.visitDelayed(ViewAttachmentsSettingsPage.class);
        JiraWebSudoPage websudoPage = pageBinder.bind(JiraWebSudoPage.class);

        assertTrue(websudoPage.isAt().now());

        final DashboardPage dashboardPage = websudoPage.cancel(DashboardPage.class);
        assertTrue(dashboardPage.isAt().now());

        jira.logout();
        jira.gotoLoginPage().loginAsSysAdmin(HomePage.class);

        final ViewProjectsPage viewProjectsPage = pageBinder.navigateToAndBind(JiraWebSudoPage.class)
                .cancel(ViewProjectsPage.class);

        assertTrue(viewProjectsPage.isAt().now());
    }

    @Test
    @EnableWebSudo
    public void testWebSudoBannerRedirectsAfterDropOnProtectedPages ()
    {
        triggerWebSudo();

        jira.visit(ViewAttachmentsSettingsPage.class);
        JiraWebSudoBanner webSudoBanner = pageBinder.bind(JiraWebSudoBanner.class);

        assertTrue(webSudoBanner.isShowing());
        assertTrue(webSudoBanner.hasProdectedDropLink());
        assertFalse(webSudoBanner.hasNormalDropLink());

        final DashboardPage dashboard = webSudoBanner.dropWebSudo(DashboardPage.class);

        assertTrue(dashboard.isAt().now());

        webSudoBanner = pageBinder.bind(JiraWebSudoBanner.class);
        assertFalse(webSudoBanner.isShowing());
    }

    @Test
    @EnableWebSudo
    public void testWebSudoDoesNotRedirectExternally()
    {
        jira.visit(JiraWebSudoPage.class, "http://google.com");

        final JiraWebSudoPage jiraWebSudoPage = pageBinder.bind(JiraWebSudoPage.class);
        jiraWebSudoPage.authenticate(ViewProjectsPage.class);

        final ViewProjectsPage viewProjectsPage = pageBinder.bind(ViewProjectsPage.class);
        assertTrue(viewProjectsPage.isAt().now());
    }
}
