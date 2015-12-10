package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebForm;

/**
 * Test Cross-site scripting vulnerabilities when script entered in fullname
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestXSSInFullName extends JIRAWebTest
{
    private static final String FULLNAME = "Alan <script>alert('I am a script')</script> Sapinsly";
    private static final String ESCAPED_FULLNAME = "Alan &lt;script&gt;alert(&#39;I am a script&#39;)&lt;/script&gt; Sapinsly";
    private static final String TEST_PROJECT = "Test";
    private static final String NEW_PROJECT = "New Project";

    // Username and password for user with script within fullname
    private static final String ALANS_USERNAME = "alans";
    public static final String ALANS_PASSWORD = "alans";


    public TestXSSInFullName(String string)
    {
        super(string);
    }

    public void setUp()
    {
        super.setUp();

        // Restore data with user's (alans) fullname set to include script
        // The user is also the only assignable user for the project 'NP'
        // One issue TST-1 exists - created by admin user
        restoreData("TestXSSData.xml");
        enableCommentGroupVisibility(Boolean.TRUE);
    }

    public void tearDown()
    {
        super.tearDown();
    }

    public void testFullNameWithScript()
    {
        _testFullUsernameInComment();
        _testEditAssigneeField();
        _testAssigneeNavigatorView();
        _testAssigneeFieldinMoveOperation();
        _testChangeHistoryTab();
        _testDeveloperWorkloadReport();
        _testWorklogTab();
        _testUserPickerCustomField();
        _testReporterInNavigatorView();
        _testWatchersTable();
        _testDashboardPortlets();
        _testReporterInBulkEdit();
        _testMultiUserCustomFieldView();
        _testTimeTrackingExcel();
    }

    /**
     * Ensure that a user's fullname is html escaped correctly in the comments and in the bodytop where the fullname of the
     * logged in user is displayed.
     */
    public void _testFullUsernameInComment()
    {
        // log out and log in as alans (with malicious fullname)
        logout();
        login(ALANS_USERNAME, ALANS_PASSWORD);

        gotoIssue("TST-1");
        clickLink("footer-comment-button");
        setWorkingForm("comment-add");
        setFormElement("comment", "Testing scripting error");
        submit("Add");

        // Assert that the unescaped fullname is not present on the page - in the comment or the top right section where
        // the logged in user fullname is displayed.
        checkName();
        assertTextPresent("Testing scripting error");
    }

    public void _testEditAssigneeField()
    {
        // start creation of one issue
        navigation.issue().goToCreateIssueForm("New Project",null);

        // assert that the assignee name is correctly escaped
        checkName();
    }

    public void _testAssigneeNavigatorView()
    {
        createIssue(NEW_PROJECT);

        // Goto issue navigator and display issues (assignee column included)
        clickLink("find_link");

        // assert that the assignee name is correctly escaped
        checkName();
    }

    public void _testAssigneeFieldinMoveOperation()
    {
        createIssue(TEST_PROJECT);

        // Move issue to "New Project"
        // Forced to select assignee during move operation as 'admin' is not assignable in "New Project"
        clickLink("move-issue");
        selectOption("pid", "New Project");
        submit("Next >>");

        // assert that the assignee name is correctly escaped on field update screen
        checkName();

        // Set assignee to user with scripted fullname
        selectOption("assignee", FULLNAME);
        submit("Next >>");

        // assert that the assignee name is correctly escaped on move issue confirm screen
        checkName();
    }

    public void _testChangeHistoryTab()
    {
        createIssue(TEST_PROJECT);

        logout();
        login(ALANS_USERNAME, ALANS_PASSWORD);

        // Resolve an issue
        gotoIssue("TST-1");
        clickLink("action_id_5");
        setWorkingForm("issue-workflow-transition");
        submit("Transition");

        // Assert that name is correctly escaped in "Change History" tab
        clickLinkWithText(ISSUE_TAB_CHANGE_HISTORY);
        checkName();
    }

    public void _testDeveloperWorkloadReport()
    {
        // Create issue assigned to 'alans' with an estimate set
        navigation.issue().goToCreateIssueForm("New Project",null);
        setFormElement("summary", "Test");
        setFormElement("timetracking", "1h");
        submit("Create");

        // Create the report
        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:developer-workload");
        setFormElement("developer", "alans");
        submit("Next");

        // Assert that name is correctly escaped in report
        checkName();

    }

    public void _testWorklogTab()
    {
        // Create issue assigned to 'alans' with an estimate set
        navigation.issue().goToCreateIssueForm("New Project",null);
        setFormElement("summary", "Test");
        setFormElement("timetracking", "1h");
        submit("Create");

        // log out and log in as alans (with malicious fullname)
        logout();
        login(ALANS_USERNAME, ALANS_PASSWORD);

        // goto issue and log work
        gotoIssue("NP-1");
        clickLink("log-work");
        setFormElement("timeLogged", "1h");
        submit();
        clickLinkWithText("Work Log");

        // Assert that name is correctly escaped in report
        checkName();

    }

    public void _testUserPickerCustomField()
    {
        navigation.issue().goToCreateIssueForm(null,null);
        setFormElement("summary", "Test");
        // Set 'alans' as the cf user
        setFormElement("customfield_10000", "alans");
        submit("Create");

        // Assert that name is correctly escaped in report
        checkName();

    }

    public void _testReporterInNavigatorView()
    {
        // log out and log in as alans (with malicious fullname)
        logout();
        login(ALANS_USERNAME, ALANS_PASSWORD);
        createIssue(TEST_PROJECT);

        // Goto issue navigator
        clickLink("find_link");

        // Assert that name is correctly escaped in report
        checkName();
    }

    public void _testWatchersTable()
    {
        // log out and log in as alans (with malicious fullname)
        logout();
        login(ALANS_USERNAME, ALANS_PASSWORD);

        // goto issue and watch it
        gotoIssue("TST-1");
        clickLink("toggle-watch-issue");

        // goto watchers table
        clickLink("view-watcher-list");
        // Assert that name is correctly escaped in table
        checkName();
    }

    public void _testDashboardPortlets()
    {
        // log out and log in as alans (with malicious fullname)
        logout();
        login(ALANS_USERNAME, ALANS_PASSWORD);

        // create an issue with User picker CF value set to alans
        navigation.issue().goToCreateIssueForm(null,null);
        setFormElement("summary", "Test");
        // Set 'alans' as the cf user
        setFormElement("customfield_10000", "alans");
        submit("Create");

        // Navigate to the dashboard
        clickLink("home_link");

        // The dashboard has a number of portlets - including the 2-D portlet that will display
        // the malicious fullname

        // Assert that name is correctly escaped on dashboard
        checkName();

    }

    public void _testReporterInBulkEdit()
    {
        logout();
        login(ALANS_USERNAME, ALANS_PASSWORD);

        // Bulk edit the existing issue to have 'alans' as reporter.
        clickLink("find_link");
        navigation.issueNavigator().bulkEditAllIssues();

        // Check all checkboxes to select all issues
        tester.setWorkingForm("bulkedit");
        WebForm form = tester.getDialog().getForm();
        String[] parameterNames = form.getParameterNames();
        for (String name : parameterNames)
        {
            if (name.startsWith("bulkedit_"))
            {
                checkCheckbox(name);
            }
        }
        submit("Next");

        checkCheckbox("operation", "bulk.edit.operation.name");
        submit("Next");
        checkCheckbox("actions", "reporter");
        setFormElement("reporter", "alans");
        submit("Next");

        // Assert that name is correctly escaped in details table
        checkName();
    }

    public void _testMultiUserCustomFieldView()
    {
        // create one issue
        navigation.issue().goToCreateIssueForm(TEST_PROJECT,null);
        setFormElement("summary", "Bug 1");
        setFormElement("customfield_10010", "alans");
        submit("Create");

        // Assert that name is correctly escaped in multi-user custom field
        checkName();
    }

    public void _testTimeTrackingExcel()
    {
        // log out and log in as alans (with malicious fullname)
        logout();
        login(ALANS_USERNAME, ALANS_PASSWORD);

        // Create issue assigned to 'alans' with an estimate set
        navigation.issue().goToCreateIssueForm("New Project",null);
        setFormElement("summary", "Test");
        setFormElement("timetracking", "1h");
        submit("Create");

        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:time-tracking");
        submit("Next");
        clickLinkWithText("Excel View");

        // Assert that name is correctly escaped in excel view name
        checkName();
    }

    // Create one issue in project specified
    // alans is default assignee for project NP
    // admin is default assignee for project TST
    private void createIssue(String projectKey)
    {
        // create one issue
        navigation.issue().goToCreateIssueForm(projectKey,null);
        setFormElement("summary", "Bug 1");
        submit("Create");
    }

    // Verify that the name is correctly escaped
    private void checkName()
    {
        assertTextNotPresent(FULLNAME);
        assertTextPresent(ESCAPED_FULLNAME);
    }
}
