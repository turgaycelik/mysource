package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.parser.issue.ViewIssueDetails;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.util.Map;
import java.util.TreeMap;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestEditIssue extends JIRAWebTest
{
    private static final String TEST_BACKUP_XML = "TestEditIssue.xml";

    private static final String COMMENT_1 = "This issue is resolved now.";
    private static final String COMMENT_2 = "Viewable by developers group.";
    private static final String COMMENT_3 = "Viewable by Developers role.";
    private static final String HSP_1 = "HSP-1";
    private static final String MKY_2 = "MKY-2";
    private static final String ADDED_COMPONENT = "oracle component";


    public TestEditIssue(String name)
    {
        super(name);
    }

    @Override
    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
        getBackdoor().darkFeatures().enableForSite("jira.no.frother.reporter.field");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    @Override
    public void tearDown()
    {
        getBackdoor().darkFeatures().disableForSite("jira.no.frother.reporter.field");
        super.tearDown();
    }

    /* fix for JRA-13921 */
    public void testUpdateIssueWithVersionCF() throws Exception
    {
        restoreData(TEST_BACKUP_XML);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        // add Version Picker custom field
        gotoAdmin();
        gotoCustomFields();
        clickLink("add_custom_fields");
        checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:multiversion");
        submit("nextBtn");
        setFormElement("fieldName", "CF-A");
        submit("nextBtn");
        checkCheckbox("associatedScreens", "1");
        checkCheckbox("associatedScreens", "3");
        checkCheckbox("associatedScreens", "2");
        submit("Update");

        // set the value of new custom field to 'New Version 1'
        gotoIssue(HSP_1);
        clickLink("edit-issue");
        selectOption("customfield_10000", "New Version 1");
        submit("Update");

        // verify in search
        issueTableAssertions.assertSearchWithResults("project=homosapien AND CF-A=\"New Version 1\"", "HSP-1");

        // unset the value of new custom field
        gotoIssue(HSP_1);
        clickLink("edit-issue");

        getDialog().setFormParameter("customfield_10000", "-1");
        submit("Update");

        // verify in search
        issueTableAssertions.assertSearchWithResults("project=homosapien AND CF-A=\"New Version 1\"");
    }

    /* fix for JRA-20146 */
    public void testEditIssueUnknownReporter() throws Exception
    {
        restoreData(TEST_BACKUP_XML);
        getBackdoor().darkFeatures().enableForSite("jira.no.frother.reporter.field");

        // set the value of new custom field to 'New Version 1'
        gotoIssue(MKY_2);
        clickLink("edit-issue");
        submit("Update");

        // verify bad user
        assertTextPresent("The reporter specified is not a user.");

        // set to a good user
        setFormElement("assignee", ADMIN_USERNAME);
        setFormElement("reporter", ADMIN_USERNAME);
        submit("Update");
    }

    public void testEditIssue()
    {
        String issueKeyNormal = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test with components", "Minor", new String[]{COMPONENT_NAME_ONE}, new String[]{VERSION_NAME_ONE}, new String[]{VERSION_NAME_ONE}, ADMIN_FULLNAME, "test environment 1", "test description normal issue for editing", null, null, null);
        String issueKeyWithNoComponents = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test without components or versions", "Minor", null, null, null, ADMIN_FULLNAME, "test environment 2", "test description issue with components", null, null, null);
        activateTimeTracking();
        String issueKeyWithTimeTracking = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "New Feature", "test with time tracking", "Critical", null, null, null, ADMIN_FULLNAME, "test environment 3", "test description issue with time tracking", "1w", null, null);
        deactivateTimeTracking();

        editIssueWithTimeTracking(issueKeyNormal, issueKeyWithTimeTracking);
        editIssueWithoutSummary(issueKeyNormal);
        editIssueWithRequiredFields(issueKeyWithNoComponents);
        editIssueWithHiddenFields(issueKeyWithNoComponents);
        editIssueWithInvalidDueDate(issueKeyWithNoComponents);
        editIssueWithEditPermission(issueKeyNormal);
        editIssueWithSchedulePermission(issueKeyNormal);
        editIssueWithAssignPermission(issueKeyNormal);
        editIssueWithModifyReporterPermission(issueKeyNormal);
        editIssueWithAddCommentsPermission(issueKeyNormal);
    }

    private void editIssueWithoutSummary(String issueKey)
    {
        log("Edit Issue: omitting summary");
        //check that the issue is initially indexed
        assertIndexedFieldCorrect("//item", EasyMap.build("summary", "test with components"), null, issueKey);
        gotoIssue(issueKey);

        clickLink("edit-issue");

        // Clear summary
        setFormElement("summary", "");

        submit();
        assertTextPresent("You must specify a summary of the issue.");

        setFormElement("summary", "test if index is updated");
        submit();

        //assert that issue summary index was updated
        assertIndexedFieldCorrect("//item", EasyMap.build("summary", "test if index is updated"), EasyMap.build("summary", "test with components"), issueKey);
    }


    /**
     * Makes the fields Components,Affects Versions and Fixed Versions required.
     * Attempts to create an issue with required fields not filled out and with an invalid assignee
     *
     * @param issueKey issue key
     */
    private void editIssueWithRequiredFields(String issueKey)
    {
        // Set fields to be required
        setRequiredFields();

        log("Edit Issue: Editing issue with required fields.");

        gotoIssue(issueKey);
        clickLink("edit-issue");
        assertTextPresent("Edit Issue");
        submit("Update");

        assertTextPresent("Edit Issue");
        assertTextPresent("Component/s is required");
        assertTextPresent("Affects Version/s is required");
        assertTextPresent("Fix Version/s is required");

        // Reset fields to be optional
        resetFields();
    }

    /**
     * Makes the fields Components,Affects Versions and Fixed Versions hidden.
     *
     * @param issueKey issue key
     */
    private void editIssueWithHiddenFields(String issueKey)
    {
        // Hide fields
        setHiddenFields(COMPONENTS_FIELD_ID);
        setHiddenFields(AFFECTS_VERSIONS_FIELD_ID);
        setHiddenFields(FIX_VERSIONS_FIELD_ID);

        log("Edit Issue: Editing issue with Hidden fields.");

        gotoIssue(issueKey);

        clickLink("edit-issue");

        assertTextPresent("Edit Issue");

        assertLinkNotPresent("components");
        assertLinkNotPresent("versions");
        assertLinkNotPresent("fixVersions");

        // Reset fields to be optional
        resetFields();
    }

    private void editIssueWithInvalidDueDate(String issueKey)
    {
        log("Edit Issue: Editing issue with invalid due date");
        gotoIssue(issueKey);
        clickLink("edit-issue");
        assertTextPresent("Edit Issue");
        setFormElement("duedate", "stuff");
        submit();
        assertTextPresent("Edit Issue");
        assertTextPresent("You did not enter a valid date. Please enter the date in the format &quot;d/MMM/yy&quot;");
    }

    /**
     * Tests if the 'Edit Issue' Link is available with the 'Edit Issue' permission removed.
     *
     * @param issueKey issue key
     */
    private void editIssueWithEditPermission(String issueKey)
    {
        log("Edit Issue: Test availability of 'Edit Issue' link with 'Edit Issue' permission.");
        removeGroupPermission(EDIT_ISSUE, Groups.DEVELOPERS);
        gotoIssue(issueKey);
        assertLinkNotPresent("edit-issue");

        // Grant 'Edit Issue' permission
        grantGroupPermission(EDIT_ISSUE, Groups.DEVELOPERS);
        gotoIssue(issueKey);
        assertLinkPresent("edit-issue");
    }

    /**
     * Tests if the 'Due Date' field is available with the 'Schedule Issue' permission removed
     *
     * @param issueKey issue key
     */
    private void editIssueWithSchedulePermission(String issueKey)
    {
        log("Edit Issue: Test prescence of 'Due Date' field with 'Schedule Issue' permission.");
        removeGroupPermission(SCHEDULE_ISSUE, Groups.DEVELOPERS);
        gotoIssue(issueKey);
        clickLink("edit-issue");
        assertTextNotPresent("Due Date");

        // Grant Schedule Issue Permission
        grantGroupPermission(SCHEDULE_ISSUE, Groups.DEVELOPERS);
        gotoIssue(issueKey);
        clickLink("edit-issue");
        assertTextPresent("Due Date");
    }

    /**
     * Tests if the user is able to specify an assignee with the 'Assign Issue' permission removed.
     *
     * @param issueKey issue key
     */
    private void editIssueWithAssignPermission(String issueKey)
    {
        log("Edit Issue: Test ability to specify assignee with 'Assign Issue' permission.");
        removeGroupPermission(ASSIGN_ISSUE, Groups.DEVELOPERS);
        gotoIssue(issueKey);
        clickLink("edit-issue");
        assertFormElementNotPresent("assignee");

        // Grant Assign Issue Permission
        grantGroupPermission(ASSIGN_ISSUE, Groups.DEVELOPERS);
        gotoIssue(issueKey);
        clickLink("edit-issue");
        assertFormElementPresent("assignee");
    }

    /**
     * Tests if the 'Reporter' field is available with the 'Assign Issue' permission removed.
     *
     * @param issueKey issue key
     */
    private void editIssueWithModifyReporterPermission(String issueKey)
    {
        log("Edit Issue: Test availability of Reporter with 'Modify Reporter' permission.");
        removeGroupPermission(MODIFY_REPORTER, Groups.ADMINISTRATORS);
        gotoIssue(issueKey);
        clickLink("edit-issue");
        assertFormElementNotPresent("reporter");

        // Grant Modify Reporter Permission
        grantGroupPermission(MODIFY_REPORTER, Groups.ADMINISTRATORS);
        gotoIssue(issueKey);
        clickLink("edit-issue");
        assertFormElementPresent("reporter");
    }

    /**
     * Tests if the 'Comment' field is available with the 'Add Comments' permission removed.
     *
     * @param issueKey issue key
     */
    private void editIssueWithAddCommentsPermission(String issueKey)
    {
        log("Edit Issue: Test availability of Comment field with 'Add Comments' permission.");
        removeGroupPermission(COMMENT_ISSUE, Groups.USERS);
        gotoIssue(issueKey);
        assertLinkNotPresent("comment-issue");
        assertLinkNotPresent("footer-comment-button");
        clickLink("edit-issue");

        // Grant Add Comments Permission
        grantGroupPermission(COMMENT_ISSUE, Groups.USERS);
        gotoIssue(issueKey);
        assertLinkPresent("comment-issue");
        assertLinkPresent("footer-comment-button");
        clickLink("edit-issue");
    }

    /**
     * Tests that the appropriate error message is displayed when a user tries to edit a closed issue.
     * JRA-13553
     */
    public void testEditClosedIssue()
    {
        restoreData(TEST_BACKUP_XML);
        grantGroupPermission(EDIT_ISSUE, Groups.USERS);

        gotoIssue(HSP_1);
        assertLinkPresent("edit-issue");
        clickLinkWithText(TRANSIION_NAME_CLOSE);
        setWorkingForm("issue-workflow-transition");
        submit("Transition");
        assertLinkNotPresentWithText("edit-issue");
        gotoPage("/secure/EditIssue!default.jspa?id=10000");
        assertTextPresent("You are not allowed to edit this issue due to its current status in the workflow.");
    }

    /**
     * Tests that the appropriate error message is displayed when a user tries to edit an issue without permission.
     * JRA-13553
     */
    public void testEditIssueWithEditPermissionManualNavigation()
    {
        restoreData(TEST_BACKUP_XML);

        removeGroupPermission(EDIT_ISSUE, Groups.DEVELOPERS);
        gotoPage("/secure/EditIssue!default.jspa?id=10000");
        assertTextPresent("You do not have permission to edit issues in this project.");

        grantGroupPermission(EDIT_ISSUE, Groups.USERS);
        gotoPage("/secure/EditIssue!default.jspa?id=10000");
        assertTextNotPresent("You do not have permission to edit issues in this project.");
    }

    /**
     * Tests that the appropriate error message is displayed when a user tries to edit an issue when logged out.
     * JRA-13553
     */
    public void testEditIssueWhileLoggedOut()
    {
        restoreData(TEST_BACKUP_XML);

        logout();
        gotoPage("/secure/EditIssue!default.jspa?id=10000");
        assertTextPresent("You are not logged in");
    }

    public void testEditIssueWithPermissionWhileLoggedOut()
    {
        restoreData(TEST_BACKUP_XML);
        grantGroupPermission(BROWSE, ANYONE);
        grantGroupPermission(EDIT_ISSUE, ANYONE);
        logout();
        gotoPage("secure/Dashboard.jspa");
        gotoIssue(HSP_1);
        assertLinkPresent("edit-issue");

    }

    public void testEditWithCommentVisibility()
    {
        restoreData(TEST_BACKUP_XML);
        enableCommentGroupVisibility(Boolean.TRUE);

        assertIndexedFieldCorrect("//item/comments", null, EasyMap.build("comment", COMMENT_1), HSP_1);
        // edit issue with comment visible to all users
        gotoIssue(HSP_1);
        clickLink("edit-issue");
        setFormElement("comment", COMMENT_1);
        submit("Update");
        assertIndexedFieldCorrect("//item/comments", EasyMap.build("comment", COMMENT_1), null, HSP_1);

        assertIndexedFieldCorrect("//item/comments", null, EasyMap.build("comment", COMMENT_2), HSP_1);
        // edit issue with comment visible only to jira-developers group
        gotoIssue(HSP_1);
        clickLink("edit-issue");
        setFormElement("comment", COMMENT_2);
        selectOption("commentLevel", Groups.DEVELOPERS);
        submit("Update");
        assertIndexedFieldCorrect("//item/comments", EasyMap.build("comment", COMMENT_2), null, HSP_1);


        assertIndexedFieldCorrect("//item/comments", null, EasyMap.build("comment", COMMENT_3), HSP_1);
        // edit issue with comment visible only to Developers role
        gotoIssue(HSP_1);
        clickLink("edit-issue");
        setFormElement("comment", COMMENT_3);
        selectOption("commentLevel", "Developers");
        submit("Update");
        assertIndexedFieldCorrect("//item/comments", EasyMap.build("comment", COMMENT_3), null, HSP_1);

        // verify that Fred can see general comment but not others as he is not in the visibility groups
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(HSP_1);
        assertTextPresent(COMMENT_1);
        assertTextNotPresent(COMMENT_2);
        assertTextNotPresent(COMMENT_3);

        // verify that Admin can see all comments as he is not in all visibility groups
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(HSP_1);
        assertTextPresent(COMMENT_1);
        assertTextPresent(COMMENT_2);
        assertTextPresent(COMMENT_3);
    }

    /**
     * Tests if the 'original estimate' field is available with time tracking activated and WITHOUT a previous original
     * estimate.
     * Tests if the 'original estimate' field is present and displays the correct information with time tracking
     * activated and WITH a previous original estimate,
     *
     * @param issueKey1 first issue key
     * @param issueKey2 second issue key
     */
    private void editIssueWithTimeTracking(String issueKey1, String issueKey2)
    {
        log("Edit Issue: Test availability of time tracking related fields");

        // No original estimate specified
        activateTimeTracking();
        gotoIssue(issueKey1);
        clickLink("edit-issue");
        assertFormElementPresent("timetracking");
        deactivateTimeTracking();

        // Original estimate specified - with work logged against issue
        activateTimeTracking();
        logWorkOnIssue(issueKey2, "1d");
        gotoIssue(issueKey2);
        clickLink("edit-issue");
        assertTextPresent("Remaining Estimate");
        assertFormElementPresent("timetracking");
        // default is "pretty"
        assertFormElementEquals("timetracking", "6d");

        reconfigureTimetracking("days");
        gotoIssue(issueKey2);
        clickLink("edit-issue");
        assertTextPresent("Remaining Estimate");
        assertFormElementPresent("timetracking");
        assertFormElementEquals("timetracking", "6d");

        reconfigureTimetracking("hours");
        gotoIssue(issueKey2);
        clickLink("edit-issue");
        assertTextPresent("Remaining Estimate");
        assertFormElementPresent("timetracking");
        assertFormElementEquals("timetracking", "144h");

        // now we log work in some crazy ways to make sure that the handling of decimal fractional days/hours is done properly
        // 11 minutes can't be displayed as a decimal fractional without losing information so we need to fallback appropriately
        logWorkOnIssue(issueKey2, "11m");

        reconfigureTimetracking("pretty");
        gotoIssue(issueKey2);
        clickLink("edit-issue");
        assertTextPresent("Remaining Estimate");
        assertFormElementPresent("timetracking");
        assertFormElementEquals("timetracking", "5d 23h 49m");

        reconfigureTimetracking("days");
        gotoIssue(issueKey2);
        clickLink("edit-issue");
        assertTextPresent("Remaining Estimate");
        assertFormElementPresent("timetracking");
        assertFormElementEquals("timetracking", "5d 23h 49m");

        reconfigureTimetracking("hours");
        gotoIssue(issueKey2);
        clickLink("edit-issue");
        assertTextPresent("Remaining Estimate");
        assertFormElementPresent("timetracking");
        assertFormElementEquals("timetracking", "143h 49m");

        // after logging a full 30 minutes of work we have a number than can be safely represented as a decimal number of hours
        logWorkOnIssue(issueKey2, "19m");
        reconfigureTimetracking("pretty");
        gotoIssue(issueKey2);
        clickLink("edit-issue");
        assertTextPresent("Remaining Estimate");
        assertFormElementPresent("timetracking");
        assertFormElementEquals("timetracking", "5d 23h 30m");

        reconfigureTimetracking("days");
        gotoIssue(issueKey2);
        clickLink("edit-issue");
        assertTextPresent("Remaining Estimate");
        assertFormElementPresent("timetracking");
        assertFormElementEquals("timetracking", "5d 23.5h");

        reconfigureTimetracking("hours");
        gotoIssue(issueKey2);
        clickLink("edit-issue");
        assertTextPresent("Remaining Estimate");
        assertFormElementPresent("timetracking");
        assertFormElementEquals("timetracking", "143.5h");

        // now we log work to get the days to display as a fraction
        logWorkOnIssue(issueKey2, "11h 48m");

        reconfigureTimetracking("pretty");
        gotoIssue(issueKey2);
        clickLink("edit-issue");
        assertTextPresent("Remaining Estimate");
        assertFormElementPresent("timetracking");
        assertFormElementEquals("timetracking", "5d 11h 42m");

        reconfigureTimetracking("days");
        gotoIssue(issueKey2);
        clickLink("edit-issue");
        assertTextPresent("Remaining Estimate");
        assertFormElementPresent("timetracking");
        assertFormElementEquals("timetracking", "5d 11.7h");

        reconfigureTimetracking("hours");
        gotoIssue(issueKey2);
        clickLink("edit-issue");
        assertTextPresent("Remaining Estimate");
        assertFormElementPresent("timetracking");
        assertFormElementEquals("timetracking", "131.7h");

        deactivateTimeTracking();
    }

    /**
     * Tests adding a new component, editing the issue with no changes and asserting
     * the issue has not changed. Initiated by JRA-12130
     */
    public void testEditNewComponentAndIssueWithNoChanges()
    {
        restoreData("TestEditIssueWithNoChanges.xml");
        getBackdoor().darkFeatures().enableForSite("jira.no.frother.reporter.field");
        //we must explicitly add a new component so that its cached to check JRA-12130
        addComponent(PROJECT_HOMOSAP, ADDED_COMPONENT);
        //add a new issue with the new component
        String issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ISSUE_TYPE_NEWFEATURE, "edit issue test summary", PRIORITY_BLOCKER, new String[]{ADDED_COMPONENT}, new String[]{VERSION_NAME_TWO}, new String[]{VERSION_NAME_ONE, VERSION_NAME_THREE}, ADMIN_FULLNAME, "edit environment", "edit issue description", "5d", null, "14/Feb/07");

        //assert issues initial values are the same as the created values
        gotoIssue(issueKey);
        assertNewComponentAndIssueViewPage();

        //check that the edit issue page has the correct values
        clickLink("edit-issue");
        setWorkingForm("issue-edit");
        assertFormElementHasValue("issue-edit", "summary", "edit issue test summary");
        assertFormTextAreaHasValue("issue-edit", "environment", "edit environment");
        assertFormTextAreaHasValue("issue-edit", "description", "edit issue description");
        assertOptionSelected("issuetype", ISSUE_TYPE_NEWFEATURE);
        assertOptionSelected("priority", PRIORITY_BLOCKER);
        assertFormElementHasValue("issue-edit", "duedate", "14/Feb/07");
        assertOptionSelected("components", ADDED_COMPONENT);
        assertOptionSelected("fixVersions", VERSION_NAME_ONE);
        assertOptionSelected("fixVersions", VERSION_NAME_THREE);
        assertOptionSelected("versions", VERSION_NAME_TWO);
        assertOptionSelected("assignee", ADMIN_FULLNAME);
        assertFormElementHasValue("issue-edit", "reporter", ADMIN_USERNAME);
        assertFormElementHasValue("issue-edit", "timetracking", "5d");

        //update without making any changes and assert that the values are the same
        //Needs to assert that the cached component was not lost JRA-12130
        submit("Update");
        assertNewComponentAndIssueViewPage();
    }

    private void assertNewComponentAndIssueViewPage()
    {
        assertLinkPresentWithText(PROJECT_HOMOSAP);
        assertTextPresentBeforeText("Type", ISSUE_TYPE_NEWFEATURE);
        assertTextPresent("edit issue test summary");
        assertTextPresentBeforeText(PRIORITY_FIELD_ID, PRIORITY_BLOCKER);
        assertTextPresentBeforeText("Due", "14/Feb/07");
        assertTextPresentBeforeText(COMPONENTS_FIELD_ID, ADDED_COMPONENT);
        assertTextPresentBeforeText(AFFECTS_VERSIONS_FIELD_ID, VERSION_NAME_TWO);
        assertTextPresentBeforeText(FIX_VERSIONS_FIELD_ID, VERSION_NAME_ONE);
        assertTextPresentBeforeText(FIX_VERSIONS_FIELD_ID, VERSION_NAME_THREE);
        assertTextPresentBeforeText("Assignee", ADMIN_FULLNAME);
        assertTextPresentBeforeText(REPORTER_FIELD_ID, ADMIN_FULLNAME);
        assertTextPresentBeforeText("Environment", "edit environment");
        assertTextPresentBeforeText("Description", "edit issue description");
    }

    /**
     * Tests that edit issue does not make any changes if no values was actually changed.
     * ie. goto edit issue -> do nothing -> click update -> check nothing changed.
     */
    public void testEditIssueWithCustomFieldsAndNoChanges()
    {
        restoreData("TestEditIssueWithNoChanges.xml");
        getBackdoor().darkFeatures().enableForSite("jira.no.frother.reporter.field");
        //assert issues initial values
        gotoIssue(HSP_1);
        assertInitialViewIssueFieldValues();

        //check that the edit issue page has the correct values
        clickLink("edit-issue");
        assertInitialEditIssueFieldValues();

        //update without making any changes and assert that the values are the same
        setWorkingForm("issue-edit");
        submit();
        assertInitialViewIssueFieldValues();
    }

    /**
     * Tests that edit issue removes all non-required field values
     * ie. goto edit issue -> remove all values -> click update -> check no values set
     */
    public void testEditIssueWithCustomFieldsAndRemoveValues()
    {
        restoreData("TestEditIssueWithNoChanges.xml");
        getBackdoor().darkFeatures().enableForSite("jira.no.frother.reporter.field");
        //assert issues initial values
        gotoIssue(HSP_1);
        assertInitialViewIssueFieldValues();
        assertNoChangesForIssue(HSP_1);

        //check that the edit issue page has the correct initial values
        clickLink("edit-issue");
        assertInitialEditIssueFieldValues();

        //remove all the non-required values
        selectOption("issuetype", ISSUE_TYPE_TASK);
        selectOption("priority", PRIORITY_CRITICAL);
        selectOption("components", "Unknown");
        selectOption("versions", "Unknown");
        selectOption("fixVersions", "Unknown");
        setFormElement("description", "");
        setFormElement("environment", "");
        setFormElement("reporter", ADMIN_USERNAME);
        setFormElement("timetracking", "");
        selectOption("customfield_10000", "None");
        selectOption("customfield_10000:1", "None");
        setFormElement("customfield_10001", "");
        setFormElement("customfield_10002", "");
        setFormElement("customfield_10003", "");
        setFormElement("customfield_10004", "");
        uncheckCheckbox("customfield_10006", "10008");
        setFormElement("customfield_10007", "");
        selectOption("customfield_10008", "None");
        setFormElement("customfield_10009", "");
        setFormElement("customfield_10010", "");
        selectOption("customfield_10011", "None");
        checkCheckbox("customfield_10012", "-1");
        selectOption("customfield_10014", "None");
        selectOption("customfield_10015", "Unknown");
        setFormElement("customfield_10016", "");
        setFormElement("customfield_10017", "");
        setFormElement("customfield_10018", "");
        selectOption("customfield_10019", "Unknown");
        //update the changes and assert that the values are removed
        setWorkingForm("issue-edit");
        submit("Update");

        assertLastChangeHistoryIs(HSP_1, "Issue Type", ISSUE_TYPE_IMPROVEMENT, ISSUE_TYPE_TASK);
        assertLastChangeHistoryIs(HSP_1, "Text Field", "text field", "");
        assertLastChangeHistoryIs(HSP_1, "Environment", "test environment 1", "");
        assertLastChangeHistoryIs(HSP_1, "Project Picker", PROJECT_MONKEY, "");
        assertLastChangeHistoryIs(HSP_1, "Group Picker", Groups.USERS, "");
        //need to assert the values seperately to avoid dependency on database ordering
        assertLastChangeHistoryIs(HSP_1, "Multi Select", "value 1", "");
        assertLastChangeHistoryIs(HSP_1, "Multi Select", "value 2", "");
        assertLastChangeHistoryIs(HSP_1, "Number Field", "12345", "");
        assertLastChangeHistoryIs(HSP_1, COMPONENTS_FIELD_ID, COMPONENT_NAME_ONE, "");
        assertLastChangeHistoryIs(HSP_1, COMPONENTS_FIELD_ID, COMPONENT_NAME_THREE, "");
        assertLastChangeHistoryIs(HSP_1, FIX_VERSIONS_FIELD_ID, VERSION_NAME_TWO, "");
        assertLastChangeHistoryIs(HSP_1, "Free Text Field", "this is a free text", "");
        assertLastChangeHistoryIs(HSP_1, "Multi User Picker", ADMIN_USERNAME, "");
        assertLastChangeHistoryIs(HSP_1, AFFECTS_VERSIONS_FIELD_ID, VERSION_NAME_ONE, "");
        assertLastChangeHistoryIs(HSP_1, AFFECTS_VERSIONS_FIELD_ID, VERSION_NAME_THREE, "");
        assertLastChangeHistoryIs(HSP_1, "User Picker", ADMIN_USERNAME, "");
        assertLastChangeHistoryIs(HSP_1, PRIORITY_FIELD_ID, PRIORITY_TRIVIAL, PRIORITY_CRITICAL);
        assertLastChangeHistoryIs(HSP_1, "URL Field", "http://www.atlassian.com", "");
        assertLastChangeHistoryIs(HSP_1, "Date Picker", "13/Feb/07", "");
        assertLastChangeHistoryIs(HSP_1, "Radio Buttons", "value 3", "");
        assertLastChangeHistoryIs(HSP_1, "Multi Group Picker", Groups.DEVELOPERS, "");
        assertLastChangeHistoryIs(HSP_1, "Multi Group Picker", Groups.USERS, "");
        assertLastChangeHistoryIs(HSP_1, "Cascading Select", "Parent values: value 1", "");
        assertLastChangeHistoryIs(HSP_1, "Cascading Select", "Level 1 values: value 1.2", "");
        assertLastChangeHistoryIs(HSP_1, "Remaining Estimate", "3 days", "");
        assertLastChangeHistoryIs(HSP_1, "Select List", "value 3", "");
        assertLastChangeHistoryIs(HSP_1, "Description", "test editing issue without any changes", "");
        assertLastChangeHistoryIs(HSP_1, "Date Time", "12/Feb/07 11:26 AM", "");
        assertLastChangeHistoryIs(HSP_1, "Multi Checkboxes", "value 3", "");
    }

    /**
     * Tests that edit issue removes all non-required field values
     * ie. goto edit issue -> remove all values -> click update -> check no values set
     */
    public void testEditIssueWithCustomFieldsAndChangeValues()
    {
        restoreData("TestEditIssueWithNoChanges.xml");
        getBackdoor().darkFeatures().enableForSite("jira.no.frother.reporter.field");
        //assert issues initial values
        gotoIssue(HSP_1);
        assertInitialViewIssueFieldValues();
        assertNoChangesForIssue(HSP_1);

        //check that the edit issue page has the correct values
        clickLink("edit-issue");
        assertInitialEditIssueFieldValues();

        //modify values
        setFormElement("summary", "new summary");
        setFormElement("description", "new description");
        setFormElement("environment", "new environment");
        selectOption("issuetype", ISSUE_TYPE_BUG);
        selectOption("priority", PRIORITY_MINOR);
        selectOption("components", COMPONENT_NAME_TWO);
        selectOption("versions", VERSION_NAME_TWO);
        selectOption("fixVersions", VERSION_NAME_ONE);
        selectOption("fixVersions", VERSION_NAME_ONE);
        setFormElement("timetracking", "1d");
        //cascading select
        selectOption("customfield_10000", "value 2");
        selectOption("customfield_10000:1", "value 2.1");
        //date picker
        setFormElement("customfield_10001", "21/Feb/07");
        //date time
        setFormElement("customfield_10002", "21/Feb/07 11:30 AM");
        //free text
        setFormElement("customfield_10003", "new free text field");
        //group picker
        setFormElement("customfield_10004", Groups.DEVELOPERS);
        //Multi checkboxes
        checkCheckbox("customfield_10006", "10007");
        setFormElement("customfield_10007", Groups.ADMINISTRATORS + ", " + Groups.USERS);
        //Multi select
        selectMultiOption("customfield_10008", "value 3");
        selectMultiOption("customfield_10008", "value 2");
        //Multi user picker
        setFormElement("customfield_10009", FRED_USERNAME);
        //Number
        setFormElement("customfield_10010", "54321");
        //Project picker
        selectOption("customfield_10011", PROJECT_HOMOSAP);
        //Radio
        checkCheckbox("customfield_10012", "10012");
        //Select List
        selectOption("customfield_10014", "value 2");
        //Single version picker
        selectOption("customfield_10015", VERSION_NAME_TWO);
        //text field
        setFormElement("customfield_10016", "text field modified");
        //url
        setFormElement("customfield_10017", "http://www.atlassian.com/software/jira");
        //user picker
        setFormElement("customfield_10018", FRED_USERNAME);
        //version picker
        selectOption("customfield_10019", VERSION_NAME_TWO);

        //update the changes and assert that the values are changed
        submit("Update");
        assertLastChangeHistoryIs(HSP_1, "Issue Type", ISSUE_TYPE_IMPROVEMENT, ISSUE_TYPE_BUG);
        assertLastChangeHistoryIs(HSP_1, "Text Field", "text field", "text field modified");
        assertLastChangeHistoryIs(HSP_1, "Environment", "test environment 1", "new environment");
        assertLastChangeHistoryIs(HSP_1, "Project Picker", PROJECT_MONKEY, PROJECT_HOMOSAP);
        assertLastChangeHistoryIs(HSP_1, "Group Picker", Groups.USERS, Groups.DEVELOPERS);
        assertLastChangeHistoryIs(HSP_1, "Multi Select", "value 1", "value 3");
        assertLastChangeHistoryIs(HSP_1, "Multi Select", "value 2", "value 2");
        assertLastChangeHistoryIs(HSP_1, "Number Field", "12345", "54321");
        assertLastChangeHistoryIs(HSP_1, COMPONENTS_FIELD_ID, COMPONENT_NAME_ONE, "");
        assertLastChangeHistoryIs(HSP_1, COMPONENTS_FIELD_ID, COMPONENT_NAME_THREE, "");
        assertLastChangeHistoryIs(HSP_1, COMPONENTS_FIELD_ID, "", COMPONENT_NAME_TWO);
        assertLastChangeHistoryIs(HSP_1, FIX_VERSIONS_FIELD_ID, VERSION_NAME_TWO, "");
        assertLastChangeHistoryIs(HSP_1, FIX_VERSIONS_FIELD_ID, "", VERSION_NAME_ONE);
        assertLastChangeHistoryIs(HSP_1, "Free Text Field", "this is a free text", "new free text field");
        assertLastChangeHistoryIs(HSP_1, "Multi User Picker", ADMIN_USERNAME, FRED_USERNAME);
        assertLastChangeHistoryIs(HSP_1, AFFECTS_VERSIONS_FIELD_ID, VERSION_NAME_ONE, "");
        assertLastChangeHistoryIs(HSP_1, AFFECTS_VERSIONS_FIELD_ID, VERSION_NAME_THREE, "");
        assertLastChangeHistoryIs(HSP_1, AFFECTS_VERSIONS_FIELD_ID, "", VERSION_NAME_TWO);
        assertLastChangeHistoryIs(HSP_1, "User Picker", ADMIN_USERNAME, FRED_USERNAME);
        assertLastChangeHistoryIs(HSP_1, PRIORITY_FIELD_ID, PRIORITY_TRIVIAL, PRIORITY_MINOR);
        assertLastChangeHistoryIs(HSP_1, "Summary", "test edit issue with no changes", "new summary");
        assertLastChangeHistoryIs(HSP_1, "URL Field", "http://www.atlassian.com", "http://www.atlassian.com/software/jira");
        assertLastChangeHistoryIs(HSP_1, "Date Picker", "13/Feb/07", "21/Feb/07");
        assertLastChangeHistoryIs(HSP_1, "Radio Buttons", "value 3", "value 1");
        assertLastChangeHistoryIs(HSP_1, "Multi Group Picker", Groups.DEVELOPERS, Groups.ADMINISTRATORS);
        assertLastChangeHistoryIs(HSP_1, "Multi Group Picker", Groups.USERS, Groups.USERS);
        assertLastChangeHistoryIs(HSP_1, "Cascading Select", "Parent values: value 1", "Parent values: value 2");
        assertLastChangeHistoryIs(HSP_1, "Cascading Select", "Level 1 values: value 1.2", "Level 1 values: value 2.1");
        assertLastChangeHistoryIs(HSP_1, "Remaining Estimate", "3 days", "1 day");
        assertLastChangeHistoryIs(HSP_1, "Single Version Picker", VERSION_NAME_THREE, VERSION_NAME_TWO);
        assertLastChangeHistoryIs(HSP_1, "Version Picker", VERSION_NAME_ONE, VERSION_NAME_TWO);
        assertLastChangeHistoryIs(HSP_1, "Version Picker", VERSION_NAME_THREE, VERSION_NAME_TWO);
        assertLastChangeHistoryIs(HSP_1, "Select List", "value 3", "value 2");
        assertLastChangeHistoryIs(HSP_1, "Description", "test editing issue without any changes", "new description");
        assertLastChangeHistoryIs(HSP_1, "Date Time", "12/Feb/07 11:26 AM", "21/Feb/07 11:30 AM");
        assertLastChangeHistoryIs(HSP_1, "Multi Checkboxes", "value 3", "value 2");
    }

    /**
     * On the view issue page, assert that the field values are set as in the import xml file TestEditIssueWithNoChanges.xml
     * (Note: need to navigate to the view issue page before calling this method)
     */
    private void assertInitialViewIssueFieldValues()
    {
        assertLinkPresentWithText(PROJECT_HOMOSAP);
        assertTextPresentBeforeText("Type", ISSUE_TYPE_IMPROVEMENT);
        assertTextPresent("test edit issue with no changes");
        assertTextPresentBeforeText(PRIORITY_FIELD_ID, PRIORITY_TRIVIAL);
        assertTextPresentBeforeText("Due", "14/Feb/07");

        assertTextPresentBeforeText(COMPONENTS_FIELD_ID, COMPONENT_NAME_ONE);
        assertTextPresentBeforeText(COMPONENTS_FIELD_ID, COMPONENT_NAME_THREE);
        assertTextPresentBeforeText(AFFECTS_VERSIONS_FIELD_ID, VERSION_NAME_ONE);
        assertTextPresentBeforeText(AFFECTS_VERSIONS_FIELD_ID, VERSION_NAME_THREE);
        assertTextPresentBeforeText(FIX_VERSIONS_FIELD_ID, VERSION_NAME_TWO);

        assertTextPresentBeforeText("Assignee", ADMIN_FULLNAME);
        assertTextPresentBeforeText(REPORTER_FIELD_ID, ADMIN_FULLNAME);
        assertTextPresentBeforeText("Environment", "test environment 1");
        assertTextPresentBeforeText("Description", "test editing issue without any changes");

        Map<String, Object> fieldValues = new TreeMap<String, Object>();
        fieldValues.put("Cascading Select", new String[]{"value 1", "value 1.2"});
        fieldValues.put("Date Picker", "13/Feb/07");
        fieldValues.put("Date Time", "12/Feb/07 11:26 AM");
        fieldValues.put("Free Text Field", "this is a free text");
        fieldValues.put("Group Picker", Groups.USERS);
        fieldValues.put("Multi Checkboxes", "value 3");
        fieldValues.put("Multi Group Picker", new String[]{ Groups.DEVELOPERS, Groups.USERS });
        fieldValues.put("Multi Select", new String[]{"value 1", "value 2"});
        fieldValues.put("Multi User Picker", ADMIN_FULLNAME);
        fieldValues.put("Number Field", "12,345");
        fieldValues.put("Project Picker", PROJECT_MONKEY);
        fieldValues.put("Radio Buttons", "value 3");
        fieldValues.put("Select List", "value 3");
        fieldValues.put("Single Version Picker", VERSION_NAME_THREE);
        fieldValues.put("Text Field", "text field");
        fieldValues.put("URL Field", "http://www.atlassian.com");
        fieldValues.put("User Picker", ADMIN_FULLNAME);
        fieldValues.put("Version Picker", new String[]{VERSION_NAME_ONE, VERSION_NAME_THREE});

        ViewIssueDetails details = parse.issue().parseViewIssuePage();
        for (Map.Entry<String, Object> entry : fieldValues.entrySet())
        {
            final String actualValue = details.getCustomFields().get(entry.getKey());
            if (actualValue == null)
            {
                fail("Unable to find a value for field '" + entry.getKey() + "'.");
            }
            else
            {
                if (entry.getValue() instanceof String[])
                {
                    for (String v : (String[])entry.getValue())
                    {
                        text.assertTextPresent(actualValue, v);
                    }
                }
                else
                {
                    text.assertTextPresent(actualValue, entry.getValue().toString());
                }
            }
        }
    }

    /**
     * On the edit issue page, assert that the expected values of the issue are set in TestEditIssueWithNoChanges.xml
     * import file (Note: need to navigate to the edit issue page before calling this method)
     */
    private void assertInitialEditIssueFieldValues()
    {
        setWorkingForm("issue-edit");
        assertFormElementHasValue("issue-edit", "summary", "test edit issue with no changes");
        assertOptionSelected("issuetype", ISSUE_TYPE_IMPROVEMENT);
        assertOptionSelected("priority", PRIORITY_TRIVIAL);
        assertFormElementHasValue("issue-edit", "duedate", "14/Feb/07");
        assertOptionSelected("components", COMPONENT_NAME_ONE);
        assertOptionSelected("components", COMPONENT_NAME_THREE);
        assertOptionSelected("versions", VERSION_NAME_ONE);
        assertOptionSelected("versions", VERSION_NAME_THREE);
        assertOptionSelected("fixVersions", VERSION_NAME_TWO);
        assertOptionSelected("assignee", ADMIN_FULLNAME);
        assertFormElementHasValue("issue-edit", "reporter", ADMIN_USERNAME);
        assertFormTextAreaHasValue("issue-edit", "environment", "test environment 1");
        assertFormTextAreaHasValue("issue-edit", "description", "test editing issue without any changes");
        assertFormElementHasValue("issue-edit", "timetracking", "3d");
        //cascading select
        assertOptionSelected("customfield_10000", "value 1");
        assertOptionSelected("customfield_10000:1", "value 1.2");
        //date picker
        assertFormElementHasValue("issue-edit", "customfield_10001", "13/Feb/07");
        //date time
        assertFormElementHasValue("issue-edit", "customfield_10002", "12/Feb/07 11:26 AM");
        //free text
        assertFormTextAreaHasValue("issue-edit", "customfield_10003", "this is a free text");
        //group picker
        assertFormElementHasValue("issue-edit", "customfield_10004", Groups.USERS);
        //Multi checkboxes
        checkCheckbox("customfield_10006", "10008");
        //Multi group picker
        assertFormTextAreaHasValue("issue-edit", "customfield_10007", Groups.DEVELOPERS + ", " + Groups.USERS);
        //Multi select
        assertOptionSelected("customfield_10008", "value 1");
        assertOptionSelected("customfield_10008", "value 2");
        //Multi user picker
        assertFormTextAreaHasValue("issue-edit", "customfield_10009", ADMIN_USERNAME);
        //Number
        assertFormElementHasValue("issue-edit", "customfield_10010", "12345");
        //Project picker
        assertOptionSelected("customfield_10011", PROJECT_MONKEY);
        //Radio
        assertOptionSelectedById("customfield_10012", "10014");
        //Select List
        assertOptionSelected("customfield_10014", "value 3");
        //Single version picker
        assertOptionSelected("customfield_10015", VERSION_NAME_THREE);
        //text field
        assertFormElementHasValue("issue-edit", "customfield_10016", "text field");
        //url
        assertFormElementHasValue("issue-edit", "customfield_10017", "http://www.atlassian.com");
        //user picker
        assertFormElementHasValue("issue-edit", "customfield_10018", ADMIN_USERNAME);
        //version picker
        assertOptionSelected("customfield_10019", VERSION_NAME_ONE);
        assertOptionSelected("customfield_10019", VERSION_NAME_THREE);
     }
}
