package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.permission.ProjectPermissions;
import com.meterware.httpunit.WebLink;
import org.xml.sax.SAXException;

import java.util.List;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static com.atlassian.jira.permission.ProjectPermissions.ADD_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.BROWSE_PROJECTS;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_ALL_COMMENTS;

/**
 * Func test to ensure that all pluginised profile page links in JIRA are being displayed correctly.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestUserFormat extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testAnonymous()
    {
        //test anonymous comment.
        String issueKey = navigation.issue().createIssue(PROJECT_HOMOSAP, null, "First test bug");
        navigation.issue().viewIssue(issueKey);
        //create a comment
        tester.clickLink("footer-comment-button");
        tester.setFormElement("comment", "My first test comment");
        tester.submit();

        //comments
        assertions.assertProfileLinkPresent("commentauthor_10000_verbose", ADMIN_FULLNAME);

        //grant anyone browse issue and comment permissions.
        navigation.gotoAdminSection("permission_schemes");
        tester.clickLink("0_edit");
        tester.clickLink("add_perm_" + BROWSE_PROJECTS.permissionKey());
        tester.checkCheckbox("type", "group");
        tester.selectOption("group", "Anyone");
        tester.submit(" Add ");

        tester.clickLink("add_perm_" + ADD_COMMENTS.permissionKey());
        tester.checkCheckbox("type", "group");
        tester.selectOption("group", "Anyone");
        tester.submit(" Add ");

        navigation.logout();
        navigation.issue().viewIssue(issueKey);
        tester.clickLink("footer-comment-button");
        tester.setFormElement("comment", "My second anoymous test comment");
        tester.submit();

        tester.assertLinkNotPresent("Anonymous");
        text.assertTextSequence(new WebPageLocator(tester), "Anonymous", "added a comment");
    }

    public void testCommentPanel()
    {
        //need to grant anyone edit comment permissions.
        tester.gotoPage("/secure/admin/AddPermission!default.jspa?permissions=" + EDIT_ALL_COMMENTS.permissionKey() + "&schemeId=0");
        tester.checkCheckbox("type", "group");
        tester.submit(" Add ");

        String issueKey = navigation.issue().createIssue(PROJECT_HOMOSAP, null, "First test bug");
        navigation.issue().viewIssue(issueKey);
        //create a comment
        tester.clickLink("footer-comment-button");
        tester.setFormElement("comment", "My first test comment");
        tester.submit();

        //check the edit comment view
        tester.clickLink("edit_comment_10000");
        tester.setWorkingForm("comment-edit");
        assertions.assertProfileLinkPresent("comment_summary_admin", ADMIN_FULLNAME);
        tester.setFormElement("comment", "My first test comment edited...");
        //This is here because of MySQL. MySQL does not store the milliseconds in a datetime field.
        //If this test edits the comment within 1 second, then the jiraaction table will store the same
        //creation and edit time and thus the second "comment_summary_updated_admin" link will not be displayed.

        try
        {
            Thread.sleep(1001L);
        }
        catch (InterruptedException ignored)
        {

        }
        tester.submit("Save");

        //now we should have an updated author as well
        tester.clickLink("edit_comment_10000");
        tester.setWorkingForm("comment-edit");
        assertions.assertProfileLinkPresent("comment_summary_admin", ADMIN_FULLNAME);
        assertions.assertProfileLinkPresent("comment_summary_updated_admin", ADMIN_FULLNAME);

        //now the delete comment view
        navigation.issue().viewIssue(issueKey);
        tester.clickLink("delete_comment_10000");
        assertions.assertProfileLinkPresent("comment_summary_admin", ADMIN_FULLNAME);
        assertions.assertProfileLinkPresent("comment_summary_updated_admin", ADMIN_FULLNAME);
    }

    //projectstable.jsp
    public void testBrowseProjectsTable()
    {
        tester.gotoPage("/secure/BrowseProjects.jspa");
        tester.assertTextPresent("Browse Projects");
        assertions.assertProfileLinkPresent("project_HSP_table_admin", ADMIN_FULLNAME);
    }

    //viewprojects.jsp
    public void testViewProjects()
    {
        navigation.gotoAdminSection("view_projects");

        assertions.assertProfileLinkPresent("view_HSP_projects_admin", ADMIN_FULLNAME);
    }

    //browsecomponent.jsp & components-panel.vm
    public void testBrowseComponent()
    {
        administration.project().editComponent("HSP", "New Component 1", "New Component 1", null, FRED_USERNAME);

        tester.gotoPage("/browse/HSP?selectedTab=com.atlassian.jira.jira-projects-plugin:components-panel");
        assertions.assertProfileLinkPresent("component_lead_fred", FRED_FULLNAME);

        tester.gotoPage("/browse/HSP/component/10000");
        assertions.assertProfileLinkPresent("component_summary_fred", FRED_FULLNAME);
    }

    //browseproject.jsp
    public void testBrowseProject()
    {
        tester.gotoPage("/browse/HSP");
        assertions.assertProfileLinkPresent("project_summary_admin", ADMIN_FULLNAME);
    }

    //viewvoters.jsp
    public void testViewVoters()
    {
        String issueKey = navigation.issue().createIssue(PROJECT_HOMOSAP, null, "First test bug");

        navigation.logout();
        navigation.login(FRED_USERNAME);
        navigation.issue().viewIssue(issueKey);
        tester.clickLink("toggle-vote-issue");

        navigation.logout();
        navigation.login(ADMIN_USERNAME);
        navigation.issue().viewIssue(issueKey);
        tester.clickLink("view-voters");

        assertions.assertProfileLinkPresent("voter_link_fred", FRED_FULLNAME);
    }

    //tests anything under /views/user
    public void testFullProfileIsSafeFromXSS()
    {
        tester.gotoPage("/secure/EditProfile!default.jspa?username=admin");
        tester.setFormElement("fullName", ADMIN_USERNAME + " \"<script>alert('owned')</script>\"");
        tester.setFormElement("email", "\"<script>alert('owned')</script>\"@localhost");
        tester.setFormElement("password", "admin");
        tester.submit();

        tester.gotoPage("/secure/ViewProfile.jspa");
        assertTrue(tester.getDialog().getResponseText().indexOf("User Profile: " + ADMIN_USERNAME + " &quot;&lt;script&gt;alert(&#39;owned&#39;)&lt;/script&gt;&quot;") != -1);
        assertTrue(tester.getDialog().getResponseText().indexOf("User Profile: " + ADMIN_USERNAME + " \"<script>alert(&#39;owned&#39;)</script>\"") == -1);
        assertTrue(tester.getDialog().getResponseText().indexOf("mailto:&quot;&lt;script&gt;alert(&#39;owned&#39;)&lt;/script&gt;&quot;") != -1);
        assertTrue(tester.getDialog().getResponseText().indexOf("mailto:\"<script>alert('owned')</script>\"") == -1);
    }

    //IssueSummaryWebComponent
    public void testIssueSummary()
    {
        //allow all user's to be assigned issues
        tester.gotoPage("/secure/admin/AddPermission!default.jspa?permissions=" + ProjectPermissions.ASSIGNABLE_USER.permissionKey() + "&schemeId=0");
        tester.checkCheckbox("type", "group");
        tester.submit(" Add ");

        String issueKey = navigation.issue().createIssue(PROJECT_HOMOSAP, null, "First test bug");
        navigation.issue().viewIssue(issueKey);
        try
        {
            backdoor.darkFeatures().enableForSite("no.frother.assignee.field");
            //assign the issue to fred
            tester.clickLink("assign-issue");
            tester.selectOption("assignee", FRED_FULLNAME);
            tester.submit("Assign");
        }
        finally
        {
            backdoor.darkFeatures().disableForSite("no.frother.assignee.field");
        }

        assertions.assertNodeByIdHasText("issue_summary_assignee_fred", FRED_FULLNAME);
        assertions.assertNodeByIdHasText("issue_summary_reporter_admin", ADMIN_FULLNAME);
    }

    //assignee-columnview.vm & reporter-columnview.vm
    public void testColumnView()
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        navigation.issue().createIssue(PROJECT_HOMOSAP, null, "First test bug");

        navigation.issueNavigator().displayAllIssues();
        assertions.assertProfileLinkPresent("assignee_admin", ADMIN_FULLNAME);
        assertions.assertProfileLinkPresent("reporter_admin", ADMIN_FULLNAME);
    }

    //assignee-columnview.vm & reporter-columnview.vm - JRA-15578
    public void testColumnViewUnAssigned() throws SAXException
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        navigation.gotoAdminSection("general_configuration");
        tester.clickLink("edit-app-properties");
        tester.checkCheckbox("allowUnassigned", "true");
        tester.submit("Update");

        navigation.issue().goToCreateIssueForm(null,null);
        tester.setFormElement("summary", "Unassigned bug");
        tester.selectOption("assignee", "Unassigned");
        tester.submit("Create");

        navigation.issueNavigator().displayAllIssues();
        final WebLink link = tester.getDialog().getResponse().getLinkWithID("assignee_admin");
        assertNull(link);
        text.assertTextPresent(new TableCellLocator(tester,"issuetable", 1, 3), "Unassigned");

        assertions.assertProfileLinkPresent("reporter_admin", ADMIN_FULLNAME);
    }

    //pickertable.vm
    public void testIssueWatchers()
    {
        navigation.userProfile().changeAutowatch(false);
        final String issueKey = navigation.issue().createIssue(PROJECT_HOMOSAP, null, "First test bug");
        navigation.issue().viewIssue(issueKey);

        //start watching
        tester.clickLink("toggle-watch-issue");

        tester.clickLink("manage-watchers");
        assertions.assertProfileLinkPresent("watcher_link_admin", ADMIN_FULLNAME);
    }

    //view-multiuser.vm & view-user.vm
    public void testCustomFields()
    {
        //add custom fields
        navigation.gotoAdminSection("view_custom_fields");
        //add a single user
        tester.clickLink("add_custom_fields");
        tester.checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:userpicker");
        tester.submit("nextBtn");
        tester.setFormElement("fieldName", "Single User");
        tester.submit("nextBtn");
        tester.checkCheckbox("associatedScreens", "1");
        tester.submit("Update");

        //add a multi-user
        tester.clickLink("add_custom_fields");
        tester.checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker");
        tester.submit("nextBtn");
        tester.setFormElement("fieldName", "Multi User");
        tester.submit("nextBtn");
        tester.checkCheckbox("associatedScreens", "1");
        tester.submit("Update");

        final String issueKey = navigation.issue().createIssue(PROJECT_HOMOSAP, null, "First test bug");
        navigation.issue().viewIssue(issueKey);

        //give the custom fields some values
        tester.clickLink("edit-issue");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.setFormElement("customfield_10001", "admin, fred");
        tester.submit("Update");

        assertions.assertNodeByIdHasText("user_cf_fred", FRED_FULLNAME);
        assertions.assertNodeByIdHasText("multiuser_cf_fred", FRED_FULLNAME);
        assertions.assertNodeByIdHasText("multiuser_cf_admin", ADMIN_FULLNAME);
    }

    //macros.vm, changehistory.vm, worklog.vm
    public void testActionHeaders()
    {
        //enable time tracking
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);

        String issueKey = navigation.issue().createIssue(PROJECT_HOMOSAP, null, "First test bug");
        navigation.issue().viewIssue(issueKey);
        //create a comment
        tester.clickLink("footer-comment-button");
        tester.setFormElement("comment", "My first test comment");
        tester.submit();
        //log some work
        tester.clickLink("log-work");
        tester.setFormElement("timeLogged", "1h");
        tester.clickButton("log-work-submit");
        //change something
        tester.clickLink("edit-issue");
        tester.setFormElement("summary", "First test bug really");
        tester.submit("Update");

        tester.gotoPage("/browse/HSP-1?page=com.atlassian.jira.plugin.system.issuetabpanels%3Aall-tabpanel");

        //comments
        assertions.assertProfileLinkPresent("commentauthor_10000_verbose", ADMIN_FULLNAME);

        //changehistory
        assertions.assertProfileLinkPresent("changehistoryauthor_10000", ADMIN_FULLNAME);
        assertions.assertProfileLinkPresent("changehistoryauthor_10001", ADMIN_FULLNAME);

        //worklog
        assertions.assertProfileLinkPresent("worklogauthor_10000", ADMIN_FULLNAME);
    }

    //developer-workload-report.vm
    public void testDeveloperWorkloadReport()
    {
        //enable time tracking
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);

        String issueKey = navigation.issue().createIssue(PROJECT_HOMOSAP, null, "First test bug");
        navigation.issue().viewIssue(issueKey);
        //log some work
        tester.clickLink("log-work");
        tester.setFormElement("timeLogged", "1h");
        tester.clickButton("log-work-submit");

        //go to the developer workload report
        navigation.runReport((long) 10000, "com.atlassian.jira.plugin.system.reports:developer-workload");
        tester.setFormElement("developer", ADMIN_USERNAME);
        tester.submit("Next");

        assertions.assertProfileLinkPresent("dev_wl_report_admin", ADMIN_FULLNAME);
    }

    // JRA-15748
    public void testDifferentUsersInIterator()
    {
        administration.restoreData("TestFormatUserDifferentUsersInIterator.xml");
        navigation.gotoAdminSection("view_projects");

        assertions.assertProfileLinkPresent("view_HSP_projects_admin", ADMIN_FULLNAME);
        assertions.assertProfileLinkPresent("view_MKY_projects_fred", FRED_FULLNAME);

        tester.gotoPage("/secure/BrowseProjects.jspa");
        assertions.assertProfileLinkPresent("project_HSP_table_admin", ADMIN_FULLNAME);
        assertions.assertProfileLinkPresent("project_MKY_table_fred", FRED_FULLNAME);

        tester.gotoPage("/secure/ViewVoters!default.jspa?id=10001");
        assertions.assertProfileLinkPresent("voter_link_admin", ADMIN_FULLNAME);
        assertions.assertProfileLinkPresent("voter_link_fred", FRED_FULLNAME);

    }

    private void assertFullProfilePresent(String username, String fullName, String email, List groups)
    {
        final Locator tableLocator = new IdLocator(tester, "full_profile");
        text.assertTextSequence(tableLocator, new String[] { "Username", username, "Full Name", fullName, "Email", email, "Groups" });
        
        //assert groups
        for (final Object group : groups)
        {
            String groupName = (String) group;
            text.assertTextPresent(new XPathLocator(tester, "//ul[@id='full_profile']/li[4]"), groupName);
        }

        text.assertTextSequence(new IdLocator(tester, "full_profile_ops"), new String[] {"View OAuth Access Tokens" });
    }
}
