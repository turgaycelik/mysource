package com.atlassian.jira.webtest.webdriver.tests.visualregression;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.user.UserBrowserPage;
import com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowsPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

import static com.atlassian.jira.functest.framework.NavigationImpl.PROJECT_PLUGIN_PREFIX;

/**
 * Webdriver test for visual regression.
 *
 * @since v5.0
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.VISUAL_REGRESSION })
public class TestVisualRegressionSmoke extends JiraVisualRegressionTest
{

    @BeforeClass
    public static void restoreInstance(){
        //we cannot restore instance via @Restore annotation as it doesn't support restoring license from backup file
        backdoor.dataImport().restoreDataFromResource("xml/TestVisualRegressionSmoke.zip", "");
    }

    @Test
    public void testEmptyDashboard()
    {
        visualComparer.setWaitforJQueryTimeout(0);
        jira.gotoHomePage().gadgets().switchDashboard("Empty");
        assertUIMatches("empty-dashboard");
    }

    @Test
    public void testMediumDashboard()
    {
        visualComparer.setWaitforJQueryTimeout(0);
        jira.gotoHomePage().gadgets().switchDashboard("Medium");
        assertUIMatches("medium-dashboard");
    }

    @Test
    public void testViewIssue()
    {
        jira.goToViewIssue("BULK-5");
        assertUIMatches("long-issue");
    }

    @Test
    public void testWorkflowDesigner()
    {
        jira.goTo(WorkflowsPage.class).openDesigner("Copy of Copy of jira");
        visualComparer.setWaitforJQueryTimeout(0);
        assertUIMatches("workflow-designer");
    }

    @Test
    public void testUserBrowser()
    {
        jira.goTo(UserBrowserPage.class).setUserFilterTo10Users().gotoResultPage(2);

        /*
         * As we just logged in with 'admin', its login info will have been updated to the current time.
         * We should ignore that info in the screenshot.
         */
        addElementsToIgnore(By.xpath("//table[@id=\"user_browser_table\"]/tbody/tr[@data-user=\"admin\"]/td[@data-cell-type=\"login-details\"]"));

        // Take the screenshot and compare.
        assertUIMatches("user-browser");
    }

    @Test
    public void testManageDashboardsFavouriteTab()
    {
        goTo("/secure/ConfigurePortalPages!default.jspa?view=favourites");
        assertUIMatches("manage-favourite-dashboards");
    }

    @Test
    public void cloneDashboardPage()
    {
        goTo("/secure/ConfigurePortalPages!default.jspa?view=favourites");
        clickOnElement("#pp_10032 > .cell-type-actions .cog-dd"); // To open the options dropdown
        clickOnElement("#clone_2"); // To select 'Copy' from the dropdown
        assertUIMatches("clone-medium-dashboard");
    }

    @Test
    public void testManageFiltersFavouriteTab()
    {
        goTo("/secure/ManageFilters.jspa?filterView=favourites");
        assertUIMatches("manage-favourite-filters");
    }

    @Test
    public void testSearchFilters() throws InterruptedException
    {
        goTo("/secure/ManageFilters.jspa?filterView=search");
        clickOnElement("#filterSearchForm input[type=submit]");
        visualComparer.setWaitforJQueryTimeout(5000);
        visualComparer.setRefreshAfterResize(false);
        assertUIMatches("search-filters");
    }

    @Test
    @Ignore("Test can't seem to find the input element when run in bamboo. Don't know why.")
    public void subscribeToFilterMonthly()
    {
        goTo("/secure/ManageFilters.jspa?filterView=my");
        clickOnElement("#subscribe_Bugs", true);
        clickOnElement("input[value='daysOfMonth']"); // to change to a days in a month filter
        assertUIMatches("subscribe-to-bugs-filter-monthly");
    }

    @Test
    public void editFilterPage()
    {
        goTo("/EditFilter!default.jspa?filterId=10016");
        assertUIMatches("edit-bugs-filter");
    }

    @Test
    @Ignore("Large screen, takes too much time to compare in CI builds")
    public void testAdminPermissionScheme() throws InterruptedException
    {
        visualComparer.setWaitforJQueryTimeout(5000);
        goTo("/secure/admin/EditPermissions!default.jspa?schemeId=10001");
        assertUIMatches("admin-permission-scheme");
    }

    @Test
    public void testAdminListWorkflows()
    {
        goTo("/secure/admin/workflows/ListWorkflows.jspa");
        assertUIMatches("admin-list-workflows-page");
    }

    @Test
    public void testAdminViewWorkflow() throws InterruptedException
    {
        goTo("/secure/admin/ViewWorkflowSteps!default.jspa?workflowMode=live&workflowName=jira");
        visualComparer.setWaitforJQueryTimeout(5000);
        assertUIMatches("admin-view-workflow-page");
    }

    @Test
    public void testAdminProjectSummary()
    {
        jira.goTo(ProjectSummaryPageTab.class, "BULK");
        assertUIMatches("project-summary");
    }

    @Test
    public void testAdminProjectPermissions() throws InterruptedException
    {
        goTo("/plugins/servlet/project-config/BULK/permissions");
        visualComparer.setWaitforJQueryTimeout(5000);
        assertUIMatches("admin-project-permissions");
    }

    @Test
    public void testAdminProjectNotifications() throws InterruptedException
    {
        goTo("/plugins/servlet/project-config/BULK/notifications");
        visualComparer.setWaitforJQueryTimeout(5000);
        assertUIMatches("admin-project-notifications");
    }

    @Test
    public void testAdminProjectWorkflows() throws InterruptedException
    {
        goTo("/plugins/servlet/project-config/BULK/workflows");
        visualComparer.setWaitforJQueryTimeout(5000);
        assertUIMatches("admin-project-workflows");
    }

    @Test
    public void testBrowseProjectSummary() throws InterruptedException
    {
        goTo("/browse/BULK");
        // ignore the 30 day summary graph
        addElementsToIgnore(By.cssSelector("#fragcreatedvsresolved img"));
        // Wait for the activity stream to load in...
        visualComparer.setWaitforJQueryTimeout(7000);
        assertUIMatches("browse-project-summary");
    }

    @Test
    public void testBrowseProjectIssues() throws InterruptedException
    {
        gotoBrowseProjectTab("issues-panel-panel");
        assertUIMatches("browse-project-issues");
    }

    @Test
    public void testBrowseProjectVersions() throws InterruptedException
    {
        gotoBrowseProjectTab("versions-panel-panel");
        assertUIMatches("browse-project-versions");
    }

    @Test
    public void testBrowseProjectComponents() throws InterruptedException
    {
        gotoBrowseProjectTab("components-panel-panel");
        assertUIMatches("browse-project-components");
    }

    @Test
    public void testBrowseProjectChangelog() throws InterruptedException
    {
        final String BULK_MOVE_2 = "BLUK";
        gotoBrowseProjectTab(BULK_MOVE_2,"changelog-panel-panel");
        visualComparer.setWaitforJQueryTimeout(15000);
        assertUIMatches("browse-project-changelog");
    }

    @Test
    public void testComponentSummaryViaBrowseProject() throws InterruptedException
    {
        visualComparer.setWaitforJQueryTimeout(5000);
        goTo("/browse/BULK/component/10003");
        assertUIMatches("component-summary-for-project");
    }

    @Test
    public void testVersionSummaryViaBrowseProject() throws InterruptedException
    {
        visualComparer.setWaitforJQueryTimeout(5000);
        goTo("/browse/XSS/fixforversion/10023");
        assertUIMatches("version-summary-for-project");
    }

    @Test
    public void testAdminViewUserProfile() throws InterruptedException
    {
        goTo("/secure/admin/user/ViewUser.jspa?name=admin");

        // Ignore login times.
        addElementsToIgnore(By.id("lastLogin"));
        addElementsToIgnore(By.id("previousLogin"));
        addElementsToIgnore(By.id("lastFailedLogin"));

        //and login count as we do not restore JIRA at each test
        addElementsToIgnore(By.id("loginCount"));

        assertUIMatches("admin-user-profile");
    }

    @Test
    public void testUserRolePermissions() throws InterruptedException
    {
        goTo("/secure/admin/user/ViewUser.jspa?name=admin");
        clickOnElement("#viewprojectroles_link");
        assertUIMatches("admin-user-permissions");
    }

    @Test
    public void testProfilePage() throws InterruptedException
    {
        goTo("/secure/ViewProfile.jspa");
        visualComparer.setWaitforJQueryTimeout(7000);
        assertUIMatches("view-profile");
    }

    @Test
    public void testAuiLayoutPage()
    {
        goTo("aui-examples/default-layout.jsp");
        assertUIMatches("aui-examples-page");
    }

    @Test
    public void test404MessagePage()
    {
        goTo("foo");
        assertUIMatches("404-message-page");
    }

    @Ignore("The value of the test is minimal -- there's a lot of dynamic data on the page, plus it's static anyway and unlikely to change.")
    public void test500MessagePage()
    {
        // Trying to hit the displayError.jsp causes a 500 error
        goToErrorPage("displayError.jsp");
        assertUIMatches("500-message-page");
    }

    @Test
    public void testDisplayErrorMessagePage()
    {
        goTo("display-error");
        assertUIMatches("display-error-message-page");
    }

    @Test
    public void testSignupMessagePage()
    {
        goTo("/secure/Signup!default.jspa");
        assertUIMatches("signup-message-page");
    }

    @Test
    public void testSignupPage()
    {
        jira.logout();
        goTo("/secure/Signup!default.jspa");
        assertUIMatches("signup-page");
    }

    private void gotoBrowseProjectTab(final String tabPanelKey)
    {
        gotoBrowseProjectTab("BULK", tabPanelKey);
    }

    private void gotoBrowseProjectTab(String projectKey, String tabPanelKey)
    {
        goTo("/browse/" + projectKey);
        String href =  jira.getTester().getDriver().findElement(By.id(PROJECT_PLUGIN_PREFIX + tabPanelKey)).getAttribute("href");
        jira.getTester().gotoUrl(href);
    }
    @Test
    public void testSchedulerAdmin()
    {
        goTo("secure/admin/SchedulerAdmin.jspa");
        assertUIMatches("test-for-scheduler-admin");
    }
}

