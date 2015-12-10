package com.atlassian.jira.webtest.webdriver.tests.home;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.pageobjects.navigator.AgnosticIssueNavigator;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.MinimalContentJiraPage;
import com.atlassian.jira.pageobjects.xsrf.XsrfPage;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * Moved from ODAT.
 *
 * @since v6.2
 */
@WebTest ({ Category.WEBDRIVER_TEST })
public class TestMyJiraHome extends BaseJiraWebTest
{
    @Inject PageElementFinder elementFinder;

    static String JIRA_HOME_URL = "/secure/MyJiraHome.jspa";

    public static final String TEST_USER = "myjirahometest";
    public static final String TEST_USER_PASS = "k1isr34ffrts";

    private static boolean testUserCreated = false;

    @BeforeClass
    public static void createTestUser() throws Exception
    {
        jira.backdoor().usersAndGroups().addUser(TEST_USER, TEST_USER_PASS, "Test User " + TEST_USER, TEST_USER + "@testusers.com");
        testUserCreated = true;
        jira.quickLogin(TEST_USER, TEST_USER_PASS);
        jira.backdoor().getTestkit().whatsNew().disableForCurrentlyLoggedInUser();

    }

    @AfterClass
    public static void cleanupTestUser() throws Exception
    {
        if (testUserCreated)
        {
            jira.backdoor().usersAndGroups().deleteUser(TEST_USER);
        }
    }


    @Test
    @LoginAs (user = TEST_USER, password = TEST_USER_PASS, targetPage = MinimalContentJiraPage.class)
    public void testRedirectToIssueNavigator() throws Exception
    {
        dropDownUserPreferences();
        setJiraHomeToIssueNav();
        pageBinder.bind(AgnosticIssueNavigator.class); // should immediately go there
        pageBinder.navigateToAndBind(JiraHomeRedirectedToIssueNav.class);
    }

    @Test
    @LoginAs (user = TEST_USER, password = TEST_USER_PASS, targetPage = MinimalContentJiraPage.class)
    public void testRedirectToIssueNavigatorAfterLogin()
    {
        dropDownUserPreferences();
        setJiraHomeToIssueNav();
        pageBinder.bind(AgnosticIssueNavigator.class); // should immediately go there

        jira.logout().gotoLoginPage().performLoginSteps(TEST_USER, TEST_USER_PASS, false);
        pageBinder.bind(AgnosticIssueNavigator.class); // should be directed to issue nav
    }


    @Test
    @LoginAs (user = TEST_USER, password = TEST_USER_PASS, targetPage = MinimalContentJiraPage.class)
    public void testRedirectToDashboard() throws Exception
    {
        dropDownUserPreferences();
        setJiraHomeToDashboard();
        pageBinder.bind(DashboardPage.class); // should immediately go there
        pageBinder.navigateToAndBind(JiraHomeRedirectedToDashboard.class);
    }

    @Test
    @LoginAs (user = TEST_USER, password = TEST_USER_PASS, targetPage = MinimalContentJiraPage.class)
    public void testRedirectToDashboardAfterLogin()
    {
        dropDownUserPreferences();
        setJiraHomeToDashboard();
        pageBinder.bind(DashboardPage.class); // should immediately go there

        jira.logout().gotoLoginPage().performLoginSteps(TEST_USER, TEST_USER_PASS, false);
        pageBinder.bind(DashboardPage.class); // should be directed dashboard
    }

    @Test
    @LoginAs (user = TEST_USER, password = TEST_USER_PASS, targetPage = MinimalContentJiraPage.class)
    public void testXsrfProtection() throws Exception
    {
        dropDownUserPreferences();
        final PageElement updateToIssueNav = getSetHomeToIssueNavLink();
        final String targetLink = updateToIssueNav.getAttribute("href");

        assertThat("My JIRA Home update link has no href attribute", targetLink, is(notNullValue()));
        assertThat("My JIRA Home update link is missing the XSRF token", targetLink, containsString("atl_token"));

        final String targetLinkWithoutXsrfToken = makeRelativeLink(targetLink.replace("atl_token", "data"));

        jira.goTo(XsrfPage.class, targetLinkWithoutXsrfToken);
    }

    private void setJiraHomeToDashboard()
    {
        elementFinder.find(By.id("set_my_jira_home_default")).click();
    }

    private void setJiraHomeToIssueNav()
    {
        getSetHomeToIssueNavLink().click();
    }

    private PageElement getSetHomeToIssueNavLink()
    {
        return elementFinder.find(By.id("set_my_jira_home_issuenav"));
    }

    private void dropDownUserPreferences()
    {
        elementFinder.find(By.id("header-details-user-fullname")).click();
        waitUntilTrue(elementFinder.find(By.id("user-options-content")).timed().isVisible());
    }

    public static class JiraHomeRedirectedToIssueNav extends AgnosticIssueNavigator
    {
        @Override
        public String getUrl()
        {
            return TestMyJiraHome.JIRA_HOME_URL;
        }
    }

    public static class JiraHomeRedirectedToDashboard extends DashboardPage
    {
        @Override
        public String getUrl()
        {
            return TestMyJiraHome.JIRA_HOME_URL;
        }
    }

    private String makeRelativeLink(final String fullLink)
    {
        final JIRAEnvironmentData jiraEnvironmentData = jira.environmentData();

        return fullLink.substring(fullLink.indexOf('/', fullLink.indexOf(jiraEnvironmentData.getBaseUrl().getHost())))
                .substring(jiraEnvironmentData.getContext().length());
    }

}


