package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.google.common.collect.ImmutableMap;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestCreateIssue extends JIRAWebTest
{
    private static final String PROJECT_MONKEY_ID = "10001";
    private static final String PROJECT_HOMOSAPIEN_ID = "10000";
    private static final String LOGIN = "log in";

    public TestCreateIssue(String name)
    {
        super(name);
    }

    @Override
    public void setUp()
    {
        super.setUp();
        administration.restoreBlankInstance();
        getBackdoor().darkFeatures().enableForSite("jira.no.frother.reporter.field");
    }

    @Override
    public void tearDown()
    {
        getBackdoor().darkFeatures().disableForSite("jira.no.frother.reporter.field");
        super.tearDown();
    }

    public void testCreateIssueInJiraWithSingleProjectAndSingleIssueType()
    {
        tester.clickLink("create_link");
        assertTextPresent("Create Issue"); // step 1 present as there are two projects
        assertTextNotPresent("CreateIssueDetails.jspa");
        assertFormElementPresent("pid");
        assertFormElementPresent("issuetype");
        assertFormElementNotPresent("summary");

        // TestOneProjectWithOneIssueType.xml is set up with a single project that uses "Bugs Only" issue type scheme,
        // which has only a single "Bug" issue type.
        administration.restoreData("TestOneProjectWithOneIssueType.xml");

        tester.clickLink("create_link");
        assertTextPresent("CreateIssueDetails.jspa");
        assertTextSequence(new String[] { "Project", "homosapien", "Issue Type", "Bug" });
        assertFormElementPresent("summary");

        Long projectId = backdoor.project().getProjectId(PROJECT_HOMOSAP_KEY);
        // Additionally, we have a "Bugs & Sub-tasks" issue type scheme
        tester.gotoPage("/secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + projectId);

        tester.checkCheckbox("createType", "chooseScheme");
        // Select 'Bugs & Sub-tasks' from select box 'schemeId'.
        tester.selectOption("schemeId", "Bugs & Sub-tasks");
        tester.submit();

        //make sure we're no longer in the admin section (where the create issue link is no longer displayed).
        if (tester.getDialog().isLinkPresent("leave_admin"))
        {
            tester.clickLink("leave_admin");
        }
        tester.clickLink("create_link");
        assertTextPresent("CreateIssueDetails.jspa");
        assertTextSequence(new String[] { "Project", "homosapien", "Issue Type", "Bug" });
        assertFormElementPresent("summary");
    }

    public void testCreateIssue()
    {
        resetFields();

        String issueKey1 = createIssue();
        String issueKey2 = createIssueWithoutComponents();
        String issueKey3 = createIssueWithTimeTrackingDetails();
        String issueKey4 = createIssueWithoutAssignee();

        createIssueWithNoSummary();
        createIssueWithRequiredFields();
        createIssueWithHiddenFields();
        createIssueWithInvalidDueDate();
        createIssueWithCreatePermission();
        createIssueWithSchedulePermission();
        createIssueWithAssignPermission();
        createIssueWithModifyReporterPermission();
        createIssueWithTimeTracking();
        createIssueWithUnassignableUser();

        deleteCreatedIssue(issueKey1);
        deleteCreatedIssue(issueKey2);
        deleteCreatedIssue(issueKey3);
        deleteCreatedIssue(issueKey4);

        //createIssueWithNoBrowsePermission deletes all issues since it does not know wat issues it added...
        createIssueWithNoBrowsePermission();
    }

    /**
     * Checks that step 1 (choose project and issue type) of create issue is skipped if there is nothing for the user to choose on the form.
     * If there is only one project in which the user has create issue permission or the project id is preselected
     * and there is only one issue type available, then expect to be redirected to the create issue details page.
     */
    public void testCreateIssueSkipStep1OnlyOneProjectAndOneIssueType()
    {
        String[] data = new String[]
                {
                        "TestCreateIssueOneProjectOneIssueType.xml",           // one project
                        "TestCreateIssueOneIssueCreateProjectOneIssueType.xml" // two projects but only one has create issue perm
                };
        for (String dataBackup : data)
        {
            navigation.gotoDashboard();
            administration.restoreData(dataBackup);
            assertRedirectAndFollow("/secure/CreateIssue!default.jspa", ".*CreateIssue\\.jspa\\?pid=" + PROJECT_MONKEY_ID + "&issuetype=3$");
            assertTextSequence(new String[] { "Create Issue", "CreateIssueDetails.jspa", "Project", "monkey", "Issue Type", "Task", "Summary" });
        }
    }

    public void testCreateIssueSkipStep1IssueTypeSchemeInfersOneProjectAndIssueType()
    {
        // check the preselection of the project in the form to "current project"
        administration.restoreData("TestCreateMonkeyHasOneIssueType.xml");
        navigation.browseProject(PROJECT_MONKEY_KEY);
        tester.clickLink("create_link");
        assertRadioOptionSelected("pid", PROJECT_MONKEY_ID);
        assertTextSequence(new String[] { "Create Issue" });

        // check we redirect to step 2 if we pass a pid url param for a project which has one issue type
        assertRedirectAndFollow("/secure/CreateIssue!default.jspa?pid=" + PROJECT_MONKEY_ID, ".*CreateIssue\\.jspa\\?pid=" + PROJECT_MONKEY_ID + "&issuetype=1$");
        assertTextSequence(new String[] { "Create Issue", "CreateIssueDetails.jspa", "Project", "monkey", "Issue Type", "Bug", "Summary" });

        // check that we do not redirect for homosapien since there are multiple issue types to choose from
        tester.gotoPage("/secure/CreateIssue!default.jspa?pid=" + PROJECT_HOMOSAPIEN_ID);
        assertRadioOptionSelected("pid", PROJECT_HOMOSAPIEN_ID);
        assertTextSequence(new String[] { "Create Issue" });

        // logout and then rerequest the url
        navigation.logout();
        tester.gotoPage("/secure/CreateIssue!default.jspa?pid=" + PROJECT_MONKEY_ID);
        assertTextPresent("You are not logged in, and do not have the permissions required to create an issue in this project as a guest.");
        tester.clickLinkWithText(LOGIN);
        tester.setFormElement("os_username", ADMIN_USERNAME);
        tester.setFormElement("os_password", ADMIN_USERNAME);
        tester.setWorkingForm("login-form");
        tester.submit();
        assertTextSequence(new String[] { "Create Issue", "CreateIssueDetails.jspa", "Project", "monkey", "Issue Type", "Bug", "Summary" });
    }

    public void testCreateIssueUserHasNoCreateIssuePermission()
    {
        // check the preselection of the project in the form to "current project"
        administration.restoreData("TestCreateIssueOneProjectOneIssueType.xml");
        navigation.login(FRED_USERNAME);
        assertLinkNotPresent("create_link");
        navigation.browseProject(PROJECT_MONKEY_KEY);
        assertLinkNotPresentWithText("Create a new issue in project monkey");

        // now go direct to these URLs
        // if they choose the project on the URL we should forbid them in project permission terms
        tester.gotoPage("/secure/CreateIssue!default.jspa?pid=" + PROJECT_MONKEY_ID);
        assertTextPresent("Error");
        assertTextPresent("You do not have permission to create issues in this project.");

        tester.gotoPage("/secure/CreateIssue!default.jspa");
        tester.assertTitleEquals("Error - jWebTest JIRA installation");
        text.assertTextPresent(new CssLocator(tester, "#content .error"), "You have not selected a valid project to create an issue in.");
    }

    public void testCreateIssueWithNoPidInUrlOneProjectAvailableIssueTypeInUrl()
    {
        administration.restoreData("TestCreateIssueOneProjectThreeIssueTypes.xml");
        tester.clickLink("create_link");
        assertTextSequence(new String[] { "Create Issue" });

        final String monkeyTask = ".*CreateIssue\\.jspa\\?pid=" + PROJECT_MONKEY_ID + "&issuetype=3$";
        assertRedirectAndFollow("/secure/CreateIssue!default.jspa?issuetype=3", monkeyTask);
        assertTextSequence(new String[] { "Create Issue", "CreateIssueDetails.jspa", "Project", "monkey", "Issue Type", "Task", "Summary" });
    }

    private void deleteCreatedIssue(String issueKey)
    {
        navigation.issue().deleteIssue(issueKey);
        assertPageDoesNotExist("The issue has not been removed from the index.", "/si/jira.issueviews:issue-xml/" + issueKey + "/" + issueKey + ".xml?jira.issue.searchlocation=index");
    }

    /**
     * Test 1: Creating issue with summary, priority, components, fix versions, affects versions, environment and description
     */
    public String createIssue()
    {
        String issueKey;
        // Creating issue with summary, priority, components, fix versions, affects versions, environment and description
        issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test 1", "Minor", new String[] { COMPONENT_NAME_ONE }, new String[] { VERSION_NAME_ONE }, new String[] { VERSION_NAME_ONE }, ADMIN_FULLNAME, "test environment 1", "test description 1 for test create issue", null, null, null);
        assertTextPresent("test 1");
        assertTextPresent("Bug");
        assertTextPresent("Minor");

        assertIndexedFieldCorrect("//item", ImmutableMap.of("summary", "test 1"), null, issueKey);
        assertIndexedFieldCorrect("//item", ImmutableMap.of("key", issueKey), null, issueKey);

        return issueKey;
    }

    /**
     * Test 2: Creating issue WITHOUT components, fix versions and affects versions
     */
    public String createIssueWithoutComponents()
    {
        String issueKey;
        // Creating issue WITHOUT components, fix versions and affects versions
        issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Improvement", "test 2", "Major", null, null, null, ADMIN_FULLNAME, "test environment 2", "test description 2 for test create issue", null, null, null);
        assertTextPresent("test 2");
        assertTextPresent("Improvement");
        assertTextPresent("Major");

        assertIndexedFieldCorrect("//item", ImmutableMap.of("type", "Improvement"), null, issueKey);
        assertIndexedFieldCorrect("//item", ImmutableMap.of("summary", "test 2"), null, issueKey);

        return issueKey;
    }

    /**
     * Test 3: Creating issue WITH time tracking details and WITHOUT components, fix versions and affects versions
     */
    public String createIssueWithTimeTrackingDetails()
    {
        String issueKey;
        // Creating issue WITH time tracking details
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);
        issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "New Feature", "test 3", "Critical", null, null, null, ADMIN_FULLNAME, "test environment 3", "test description 3 for test create issue", "1w", null, null);
        assertTextPresent("test 3");
        assertTextPresent("New Feature");
        assertTextPresent("Critical");
        assertTextPresent("Original Estimate");
        assertTextPresent("1 week");
        assertIndexedFieldCorrect("//item", ImmutableMap.of("priority", "Critical"), null, issueKey);
        assertIndexedFieldCorrect("//item", ImmutableMap.of("type", "New Feature"), null, issueKey);
        administration.timeTracking().disable();
        return issueKey;
    }

    /**
     * Test 4: Creating an issue WITHOUT an assignee and WITHOUT fix versions and affects version
     */
    public String createIssueWithoutAssignee()
    {
        // Creating an issue without an assignee
        administration.generalConfiguration().setAllowUnassignedIssues(true);
        final String issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Task", "test 4", "Blocker", new String[] { COMPONENT_NAME_ONE }, null, null, "Unassigned", "test environment 4", "test description 4 for test create issue", null, null, null);
        assertTextPresent("test 4");
        assertTextPresent("Task");
        assertTextPresent("Blocker");
        assertTextPresentBeforeText("Assignee:", "Unassigned");
        assertIndexedFieldCorrect("//item", ImmutableMap.of("priority", "Blocker"), null, issueKey);
        assertIndexedFieldCorrect("//item", ImmutableMap.of("environment", "test environment 4"), null, issueKey);
        navigation.issue().assignIssue(issueKey, "Assigning issue to ADMIN", ADMIN_FULLNAME);
        administration.generalConfiguration().setAllowUnassignedIssues(false);
        return issueKey;
    }

    public void createIssueWithNoSummary()
    {
        log("Create Issue: Adding issue without summary");
        createIssueStep1();

        // Not setting summary

        // Need to set priority as this is automatically set
        tester.selectOption("priority", "Minor");

        tester.submit();
        assertTextPresent("CreateIssueDetails.jspa");
        assertTextPresent("You must specify a summary of the issue.");
    }

    /**
     * Makes the fields Components,Affects Versions and Fixed Versions required.
     * Attempts to create an issue with required fields not filled out and with an invalid assignee
     */
    public void createIssueWithRequiredFields()
    {
        // Set fields to be required
        setRequiredFields();

        log("Create Issue: Test the creation of an issue using required fields");
        createIssueStep1();

        tester.setFormElement("summary", "This is a new summary");
        tester.setFormElement("reporter", "");

        tester.submit("Create");

        assertTextPresent("CreateIssueDetails.jspa");
        assertTextPresent("Component/s is required");
        assertTextPresent("Affects Version/s is required");
        assertTextPresent("Fix Version/s is required");

        // Reset fields to be optional
        resetFields();
    }

    /**
     * Makes the fields Components,Affects Versions and Fixed Versions hidden.
     */
    public void createIssueWithHiddenFields()
    {
        // Hide fields
        setHiddenFields(COMPONENTS_FIELD_ID);
        setHiddenFields(AFFECTS_VERSIONS_FIELD_ID);
        setHiddenFields(FIX_VERSIONS_FIELD_ID);

        log("Create Issue: Test the creation of am issue using hidden fields");
        createIssueStep1();

        assertFormElementNotPresent("components");
        assertFormElementNotPresent("versions");
        assertFormElementNotPresent("fixVersions");

        // Reset fields to be optional
        resetFields();
    }

    public void createIssueWithInvalidDueDate()
    {
        log("Create Issue: Adding issue with invalid due date");
        createIssueStep1();

        assertTextPresent("CreateIssueDetails.jspa");

        tester.setFormElement("summary", "stuff");
        tester.setFormElement("duedate", "stuff");

        tester.submit("Create");

        assertTextPresent("CreateIssueDetails.jspa");

        assertTextPresent("You did not enter a valid date. Please enter the date in the format &quot;d/MMM/yy&quot;");
    }

    /**
     * Tests if the 'Create New Issue' Link is available with the 'Create Issue' permission removed
     */
    public void createIssueWithCreatePermission()
    {
        log("Create Issue: Test availability of 'Create Issue' link with 'Create Issue' permission.");
        administration.permissionSchemes().defaultScheme().removePermission(CREATE_ISSUE, Groups.USERS);
        getNavigation().gotoDashboard();
        assertLinkNotPresent("create_link");

        // Grant 'Create Issue' permission
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(CREATE_ISSUE, Groups.USERS);
        getNavigation().gotoDashboard();
        assertLinkPresent("create_link");
    }

    /**
     * Tests if the Due Date' field is available with the 'Schedule Issue' permission removed
     */
    public void createIssueWithSchedulePermission()
    {
        log("Create Issue: Test prescence of 'Due Date' field with 'Schedule Issue' permission.");
        administration.permissionSchemes().defaultScheme().removePermission(SCHEDULE_ISSUE, Groups.DEVELOPERS);
        createIssueStep1();
        assertTextNotPresent("Due Date");

        // Grant Schedule Issue Permission
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(SCHEDULE_ISSUE, Groups.DEVELOPERS);
        createIssueStep1();
        assertTextPresent("Due Date");
    }

    /**
     * Tests if the user is able to assign an issue with the 'Assign Issue' permission removed
     */
    public void createIssueWithAssignPermission()
    {
        log("Create Issue: Test ability to specify assignee with 'Assign Issue' permission.");
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGN_ISSUE, Groups.DEVELOPERS);
        createIssueStep1();
        assertFormElementNotPresent("assignee");

        // Grant Assign Issue Permission
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(ASSIGN_ISSUE, Groups.DEVELOPERS);
        createIssueStep1();
        assertFormElementPresent("assignee");
    }

    /**
     * Tests if the 'Reporter' Option is available with the 'Modify Reporter' permission removed
     */
    public void createIssueWithModifyReporterPermission()
    {
        log("Create Issue: Test availability of Reporter with 'Modify Reporter' permission.");
        administration.permissionSchemes().defaultScheme().removePermission(MODIFY_REPORTER, Groups.ADMINISTRATORS);
        createIssueStep1();
        assertFormElementNotPresent("reporter");

        // Grant Modify Reporter Permission
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(MODIFY_REPORTER, Groups.ADMINISTRATORS);
        createIssueStep1();
        assertFormElementPresent("reporter");
    }

    /**
     * Tests if the 'Orignial Estimate' Link is available with Time Tracking activated
     */
    public void createIssueWithTimeTracking()
    {
        log("Create Issue: Test availability of time tracking ...");
        administration.timeTracking().disable();
        createIssueStep1();
        assertFormElementNotPresent("timetracking");

        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);
        createIssueStep1();
        assertFormElementPresent("timetracking");
        administration.timeTracking().disable();
    }

    /**
     * Tests if the user is able to assign an issue with the 'Assignable User' permission removed
     */
    public void createIssueWithUnassignableUser()
    {
        log("Create Issue: Attempt to set the assignee to be an unassignable user ...");

        // Remove assignable permission
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGNABLE_USER, Groups.DEVELOPERS);
        createIssueStep1();
        tester.submit("Create");
        tester.setFormElement("summary", "Test summary");

        assertTextPresent(DEFAULT_ASSIGNEE_ERROR_MESSAGE);

        //Restore permission
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(ASSIGNABLE_USER, Groups.DEVELOPERS);
    }

    /**
     * Test that user is redirected if user does not have the permission to view the issue they created. JRA-7684
     */
    public void createIssueWithNoBrowsePermission()
    {
        log("Create Issue: Adding issue with no browse permission for user");

        //setup
        administration.restoreData("TestCantViewCreatedIssue.xml");

        //Start of tests
        //create issue - not logged in, no browse permission
        navigation.logout();
        navigation.gotoDashboard();
        addIssueOnly("noBrowseProject", "NBP", "Bug", "test1", "Minor", null, null, null, null, null, "description", null, null, null);
        assertIssueCreatedButCannotView(false);
        //log in as user with no browse permission
        tester.clickLinkWithText(LOGIN);
        tester.setFormElement("os_username", "nobrowseuser");
        tester.setFormElement("os_password", "nobrowseuser");
        tester.setWorkingForm("login-form");
        tester.submit();
        assertIssueCreatedButCannotView(true);

        //create issue - logged in, no browse permission
        addIssueOnly("noBrowseProject", "NBP", "Bug", "test1", "Minor", null, null, null, null, null, "description", null, null, null);
        assertIssueCreatedButCannotView(true);

        //create issue - not logged in, no browse permission
        navigation.logout();
        navigation.gotoDashboard();
        addIssueOnly("noBrowseProject", "NBP", "Bug", "test1", "Minor", null, null, null, null, null, "description", null, null, null);
        assertIssueCreatedButCannotView(false);
        //log in as admin with browse permission
        tester.clickLinkWithText(LOGIN);
        tester.setFormElement("os_username", ADMIN_USERNAME);
        tester.setFormElement("os_password", ADMIN_USERNAME);
        tester.setWorkingForm("login-form");
        tester.submit();
        assertTextNotPresent("Issue Created Successfully");
        assertLinkNotPresentWithText(LOGIN);
        assertTextPresent("Details");

        //tear down
        administration.restoreBlankInstance();
    }

    public void testEscapeProjectNameOnFirstScreenOfCreateIssue()
    {
        navigation.gotoAdmin();
        tester.clickLink("add_project");
        tester.setFormElement("name", "Xss Project &trade;");
        tester.setFormElement("key", "XP");
        tester.setFormElement("lead", ADMIN_USERNAME);
        tester.submit("Add");

        //make sure we're no longer in the admin section (where the create issue link is no longer displayed).
        if (tester.getDialog().isLinkPresent("leave_admin"))
        {
            tester.clickLink("leave_admin");
        }
        tester.clickLink("create_link");
        assertTextNotPresent("&trade;");
        assertTextPresent("&amp;trade;");
    }


    // JRA-16369
    public void testXssInDueDate()
    {
        final String value = "\"><script>";
        final String valueEncoded = "&quot;&gt;&lt;script&gt;";
        final String expectedFormElement = "<input class=\"text medium-field\" id=\"duedate\" name=\"duedate\" type=\"text\" value=\"";
        final String notExpected = expectedFormElement + value + "\"";
        final String expected = expectedFormElement + valueEncoded + "\"";

        tester.clickLink("create_link");
        tester.submit("Next");
        tester.setFormElement("duedate", value);
        tester.submit("Create");
        text.assertTextPresent(locator.page().getHTML(), expected);
        text.assertTextNotPresent(locator.page().getHTML(), notExpected);
    }

    private void assertIssueCreatedButCannotView(boolean loggedIn)
    {
        assertTextPresent("Issue Created Successfully");
        assertTextPresentBeforeText("You have successfully created the issue (", "), however you do not have the permission to view the created issue.");
        if (!loggedIn)
        {
            assertLinkPresentWithText(LOGIN);
        }
    }

    public void testCreateButtonEncoding()
    {
        navigation.userProfile().changeUserLanguage("fran\u00e7ais (France)");
        navigation.issue().goToCreateIssueForm("homosapien", "Bogue");
        assertions.assertNodeHasText(xpath("//input[@id='issue-create-submit']/@value"), "Cr\u00E9er"); // UNICODE e-acute=00E9
        navigation.userProfile().changeUserLanguageToJiraDefault();
    }
}
