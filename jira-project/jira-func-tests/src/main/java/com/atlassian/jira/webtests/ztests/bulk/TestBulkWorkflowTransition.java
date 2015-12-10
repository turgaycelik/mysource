package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.WORKFLOW })
public class TestBulkWorkflowTransition extends BulkChangeIssues
{
    protected static final String NOT_AVAILABLE_BULK_EDIT = "NOTE: This field is not available for bulk update operations.";
    protected static final String WORKFLOW_TRANSITION_CHOOSE_ERROR_TEXT = "Please select a transition to execute";
    protected static final String WORKFLOW_TRANSITION_EDIT_TEXT = "Select and edit the fields available on this transition.";
    protected static final String WORKFLOW_TRANSITION_MULTI_PROJECT_ERROR = "NOTE: This operation can be performed only on issues from ONE project.";
    protected static final String WORKFLOW_TRANSITION_SELECTION_TEXT = "Select the workflow transition to execute on the associated issues";

    private static final String COMMENT_1 = "This issue is resolved now.";
    private static final String COMMENT_2 = "Viewable by developers group.";
    private static final String COMMENT_3 = "Viewable by Developers role.";
    public static final String TABLE_EDITFIELDS_ID = "screen-tab-1-editfields";

    public TestBulkWorkflowTransition(String name)
    {
        super(name);
    }

    @Override
    public void tearDown()
    {
        getBackdoor().darkFeatures().disableForSite("jira.no.frother.reporter.field");
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        removeGlobalPermission(BULK_CHANGE, Groups.USERS);
        super.tearDown();
    }

    public void testBulkTransitionSessionTimeouts()
    {
        log("Bulk Transition - Test that you get redirected to the session timeout page when jumping into the wizard");

        restoreBlankInstance();
        beginAt("secure/views/bulkedit/BulkWorkflowTransitionDetails.jspa");
        verifyAtSessionTimeoutPage();
        beginAt("secure/views/bulkedit/BulkWorkflowTransitionDetailsValidation.jspa");
        verifyAtSessionTimeoutPage();
        beginAt("secure/views/bulkedit/BulkWorkflowTransitionEditValidation.jspa");
        verifyAtSessionTimeoutPage();
    }

    /**
     * Validate non editable fields and project specific fields Time Tracking - not editable Attachments - not editable
     * Summary - not editable Components - project specific Versions - project specific
     */
    public void testNotAvailableFields()
    {
        restoreData("TestBulkWorkflowTransition.xml");
        grantGlobalPermission(BULK_CHANGE, Groups.USERS);
        // get to the issue display page
        _testToOperationDetailsWorkflowTranisition();
        navigation.workflows().chooseWorkflowAction("classic default workflow_2_6");
        assertTextPresent(WORKFLOW_TRANSITION_EDIT_TEXT);

        // make sure that Attachment, Summary and Timetracking are not available for edit.
        assertTableRowsEqual(TABLE_EDITFIELDS_ID, 14, new String[][]
                {
                        {"N/A", "Change Attachment", NOT_AVAILABLE_BULK_EDIT}});
        assertTableRowsEqual(TABLE_EDITFIELDS_ID, 25, new String[][]
                {
                        {"N/A", "Change Summary", NOT_AVAILABLE_BULK_EDIT}});
        assertTableRowsEqual(TABLE_EDITFIELDS_ID, 27, new String[][]
                {
                        {"N/A", "Change Time Tracking", NOT_AVAILABLE_BULK_EDIT}});

        // assert project specific fields
        assertTableRowsEqual(TABLE_EDITFIELDS_ID, 1, new String[][]
                {
                        {"N/A", "Change Fix Version/s", WORKFLOW_TRANSITION_MULTI_PROJECT_ERROR}});

        assertTableRowsEqual(TABLE_EDITFIELDS_ID, 5, new String[][]
                {
                        {"N/A", "Change Patched Version", WORKFLOW_TRANSITION_MULTI_PROJECT_ERROR}});

        assertTableRowsEqual(TABLE_EDITFIELDS_ID, 15, new String[][]
                {
                        {"N/A", "Change Component/s", WORKFLOW_TRANSITION_MULTI_PROJECT_ERROR}});
        removeGlobalPermission(BULK_CHANGE, Groups.USERS);
    }


    /**
     * Bulk edit all fields (system and custom fields) - check that all fields are edited correctly - check correct
     * fields available with permission schemes
     */

    public void testWorkflowTransitionCompleteWalkthrough()
    {
        restoreData("TestBulkWorkflowTransition.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        getBackdoor().darkFeatures().enableForSite("jira.no.frother.reporter.field");
        // Enable attachments
        administration.attachments().enable();
        setShownFields(ATTACHMENT_FIELD_ID);

        grantGlobalPermission(BULK_CHANGE, Groups.USERS);
        _testToOperationDetailsWorkflowTranisition();
        navigation.workflows().chooseWorkflowAction("classic default workflow_5_5");
        assertTextPresent(WORKFLOW_TRANSITION_EDIT_TEXT);
        assertAllFieldsPresent();
        assertWikiRendererCommentField();
        editAllFields();
        // bulkWorkflowTransitionOperationDetails(true);
        navigation.clickOnNext();
        assertTextPresent("Please confirm the details of this operation");

        // assert all edit fields are to be changed
        assertAllEditFieldsInUpdatedFieldTable();

        navigation.clickOnNext();
        waitAndReloadBulkOperationProgressPage();

        assertTextInTable("issuetable", "Resolved");
        assertTextInTable("issuetable", "Fixed");

        // Go to one issue and validate all changed
        gotoIssue("TSTWO-6");

        // assert modified custom fields
        text.assertTextPresent(new IdLocator(tester, "customfield_10000-val"), "29/Nov/05 4:27 PM");
        text.assertTextPresent(new IdLocator(tester, "customfield_10002-val"),  "Linux");
        text.assertTextPresent(new IdLocator(tester, "customfield_10003-val"),  "1");
        text.assertTextPresent(new IdLocator(tester, "customfield_10004-val"),  "http://www.atlassian.com");
        text.assertTextPresent(new IdLocator(tester, "customfield_10007-val"),  "Functional test - management comment");
        text.assertTextPresent(new IdLocator(tester, "customfield_10008-val"),  "Development");
        text.assertTextPresent(new IdLocator(tester, "customfield_10009-val"), ADMIN_FULLNAME);
        text.assertTextPresent(new IdLocator(tester, "customfield_10010-val"),  "Test Project 1");
        text.assertTextPresent(new IdLocator(tester, "customfield_10013-val"), ADMIN_FULLNAME);
        text.assertTextPresent(new IdLocator(tester, "customfield_10014-val"),  "Func Test - Limited text Field");
        text.assertTextPresent(new IdLocator(tester, "customfield_10006-val"), "21/Dec/05");

        IdLocator locator = new IdLocator(tester, "type-val");
        text.assertTextPresent(locator, "New Feature" );

        locator = new IdLocator(tester, "status-val");
        text.assertTextPresent(locator, "Resolved" );

        locator = new IdLocator(tester, "resolution-val");
        text.assertTextPresent(locator, "Fixed" );

        locator = new IdLocator(tester, "priority-val");
        text.assertTextPresent(locator, "Critical" );


        // Assert that a comment was added
        assertTextPresent("Bulk Edit Comment");
        removeGlobalPermission(BULK_CHANGE, Groups.USERS);

        //assert that the index was correctly updated
        assertIndexedFieldCorrect("//item", EasyMap.build("status", "Resolved", "resolution", "Fixed"), null, "TSTWO-6");

        //assert bulk edit comment was indexed
        assertIndexedFieldCorrect("//comments", EasyMap.build("comment", "&lt;p&gt;Bulk Edit Comment&lt;/p&gt;"), null, "TSTWO-6");

        //assert customfield was indexed correctly
        assertIndexedFieldCorrect("//customfield/customfieldvalues", EasyMap.build("customfieldvalue", "Linux"), null, "TSTWO-6");

    }

    public void testWorkflowTransitionConcurrentIssueUpdate()
    {
        restoreData("TestBulkTransition.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        displayAllIssues();
        bulkChangeIncludeAllPages();
        checkCheckbox("bulkedit_10011", "on");
        checkCheckbox("bulkedit_10010", "on");
        checkCheckbox("bulkedit_10000", "on");
        submit("Next");
        checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        submit("Next");
        checkCheckbox("wftransition", "classic default workflow_2_6");
        submit("Next");
        selectOption("resolution", "Won't Fix");
        submit("Next");

        // now we change one of the issues concurrently...
        IssueTransitioner issueTransitioner = new IssueTransitioner();
        issueTransitioner.setEnvironmentData(getEnvironmentData());
        issueTransitioner.init();
        issueTransitioner.closeIssue("HSP-2");

        // and then fire off our bulk transition
        submit("Next");
        waitAndReloadBulkOperationProgressPage();

        assertTextNotInTable("issueTable", new String[]{"Open", "Reopen"});
    }

    /**
     * Check errors generated correctly (e.g. incorrect date input, etc.)
     */
    public void testWorkflowTransitionEditFieldErrors()
    {
        restoreData("TestBulkWorkflowTransition.xml");
        grantGlobalPermission(BULK_CHANGE, Groups.USERS);
        _testToOperationDetailsWorkflowTranisition();
        navigation.workflows().chooseWorkflowAction("classic default workflow_5_5");
        assertTextPresent(WORKFLOW_TRANSITION_EDIT_TEXT);
        generateMultipleInputErrors();
        removeGlobalPermission(BULK_CHANGE, Groups.USERS);
    }

    public void testWorkflowTransitionErrorOnWorkflowSelection()
    {
        restoreData("TestBulkWorkflowTransition.xml");
        grantGlobalPermission(BULK_CHANGE, Groups.USERS);
        _testToOperationDetailsWorkflowTranisition();
        navigation.clickOnNext();
        assertTextPresent(WORKFLOW_TRANSITION_CHOOSE_ERROR_TEXT);
        removeGlobalPermission(BULK_CHANGE, Groups.USERS);
    }


    public void testBulkTransitionWithCommentVisibility()
    {
        restoreData("TestBulkWorkflowTransition.xml");
        enableCommentGroupVisibility(Boolean.TRUE);

        ////////////////////// ALL User Comments /////////////////////////////
        // Do first two issues with comments viewable to all.
        displayAllIssues();
        bulkChangeIncludeAllPages();
        checkCheckbox("bulkedit_10009", "on"); // TSTWO-5
        checkCheckbox("bulkedit_10008", "on"); // TSTWO-4
        submit("Next");

        // choose to transition issues
        assertTextPresent("Choose the operation you wish to perform");
        chooseOperationExecuteWorfklowTransition();
        navigation.workflows().assertStepOperationDetails();
        assertTextPresent(WORKFLOW_TRANSITION_SELECTION_TEXT);

        // Choose the workflow to transition
        navigation.workflows().chooseWorkflowAction("classic default workflow_5_5");

        // edit fields
        selectOption("resolution", "Fixed");
        checkCheckbox("commentaction", "comment");
        setFormElement("comment", COMMENT_1);
        // Submit to confirmation page
        navigation.clickOnNext();
        assertTextPresent("Please confirm the details of this operation");
        // Final submit
        navigation.clickOnNext();
        waitAndReloadBulkOperationProgressPage();

        ////////////////////// Group Comments /////////////////////////////
        // Now transition next two issues with group comment
        displayAllIssues();
        bulkChangeIncludeAllPages();
        checkCheckbox("bulkedit_10009", "on"); // TSTWO-5
        checkCheckbox("bulkedit_10008", "on"); // TSTWO-4
        submit("Next");

        // choose to transition issues
        assertTextPresent("Choose the operation you wish to perform");
        chooseOperationExecuteWorfklowTransition();
        navigation.workflows().assertStepOperationDetails();
        assertTextPresent(WORKFLOW_TRANSITION_SELECTION_TEXT);

        // Choose the workflow to transition
        navigation.workflows().chooseWorkflowAction("classic default workflow_3_4");

        // edit the fields
        checkCheckbox("commentaction", "comment");
        selectOption("commentLevel", "jira-developers");
        setFormElement("comment", COMMENT_2);
        // Submit to confirmation page
        navigation.clickOnNext();
        assertTextPresent("Please confirm the details of this operation");
        // Final submit
        navigation.clickOnNext();
        waitAndReloadBulkOperationProgressPage();

        ////////////////////// ROLE Comments /////////////////////////////
        // Now transition next two issues with role comment
        displayAllIssues();
        bulkChangeIncludeAllPages();
        checkCheckbox("bulkedit_10009", "on"); // TSTWO-5
        checkCheckbox("bulkedit_10008", "on"); // TSTWO-4
        submit("Next");

        // choose to transition issues
        assertTextPresent("Choose the operation you wish to perform");
        chooseOperationExecuteWorfklowTransition();
        navigation.workflows().assertStepOperationDetails();
        assertTextPresent(WORKFLOW_TRANSITION_SELECTION_TEXT);

        // Choose the workflow to transition
        navigation.workflows().chooseWorkflowAction("classic default workflow_5_5");

        // edit the fields
        selectOption("resolution", "Fixed");
        checkCheckbox("commentaction", "comment");
        selectOption("commentLevel", "Developers");
        setFormElement("comment", COMMENT_3);
        // Submit to confirmation page
        navigation.clickOnNext();
        assertTextPresent("Please confirm the details of this operation");
        // Final submit
        navigation.clickOnNext();
        waitAndReloadBulkOperationProgressPage();

        // verify that Fred can see general comment but not others as he is not in the visibility groups
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue("TSTWO-4");
        assertTextPresent(COMMENT_1);
        assertTextNotPresent(COMMENT_2);
        assertTextNotPresent(COMMENT_3);
        gotoIssue("TSTWO-5");
        assertTextPresent(COMMENT_1);
        assertTextNotPresent(COMMENT_2);
        assertTextNotPresent(COMMENT_3);
        gotoIssue("TSTWO-6");
        assertTextNotPresent(COMMENT_1);
        assertTextNotPresent(COMMENT_2);
        assertTextNotPresent(COMMENT_3);

        //assert that the bulk editted workflow has been updated in the index and that Fred's permissions are applied to the index
        assertIndexedFieldCorrect("//comments", EasyMap.build("comment", "&lt;p&gt;" + COMMENT_1 + "&lt;/p&gt;"), EasyMap.build("comment", "&lt;p&gt;" + COMMENT_2 + "&lt;/p&gt;", "comment", "&lt;p&gt;" + COMMENT_3 + "&lt;/p&gt;"), "TSTWO-4");
        assertIndexedFieldCorrect("//comments", EasyMap.build("comment", "&lt;p&gt;" + COMMENT_1 + "&lt;/p&gt;"), EasyMap.build("comment", "&lt;p&gt;" + COMMENT_2 + "&lt;/p&gt;", "comment", "&lt;p&gt;" + COMMENT_3 + "&lt;/p&gt;"), "TSTWO-5");
        assertIndexedFieldCorrect("//comments", null, EasyMap.build("comment", "&lt;p&gt;" + COMMENT_1 + "&lt;/p&gt;", "comment", "&lt;p&gt;" + COMMENT_2 + "&lt;/p&gt;", "comment", "&lt;p&gt;" + COMMENT_3 + "&lt;/p&gt;"), "TSTWO-6");

        // verify that Admin can see all comments as he is not in all visibility groups
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue("TSTWO-4");
        assertTextPresent(COMMENT_1);
        assertTextPresent(COMMENT_2);
        assertTextPresent(COMMENT_3);
        gotoIssue("TSTWO-5");
        assertTextPresent(COMMENT_1);
        assertTextPresent(COMMENT_2);
        assertTextPresent(COMMENT_3);
        gotoIssue("TSTWO-6");
        assertTextNotPresent(COMMENT_1);
        assertTextNotPresent(COMMENT_2);
        assertTextNotPresent(COMMENT_3);

        assertIndexedFieldCorrect("//comments", EasyMap.build("comment", "&lt;p&gt;" + COMMENT_1 + "&lt;/p&gt;", "comment", "&lt;p&gt;" + COMMENT_2 + "&lt;/p&gt;", "comment", "&lt;p&gt;" + COMMENT_3 + "&lt;/p&gt;"), null, "TSTWO-4");
        assertIndexedFieldCorrect("//comments", EasyMap.build("comment", "&lt;p&gt;" + COMMENT_1 + "&lt;/p&gt;", "comment", "&lt;p&gt;" + COMMENT_2 + "&lt;/p&gt;", "comment", "&lt;p&gt;" + COMMENT_3 + "&lt;/p&gt;"), null, "TSTWO-5");
        assertIndexedFieldCorrect("//comments", null, EasyMap.build("comment", "&lt;p&gt;" + COMMENT_1 + "&lt;/p&gt;", "comment", "&lt;p&gt;" + COMMENT_2 + "&lt;/p&gt;", "comment", "&lt;p&gt;" + COMMENT_3 + "&lt;/p&gt;"), "TSTWO-6");

    }


    private void assertAllEditFieldsInUpdatedFieldTable()
    {
        assertTableRowsEqual("updatedfields", 1, new String[][]
                {
                        { "Assignee", ADMIN_FULLNAME }});
        assertTableRowsEqual("updatedfields", 2, new String[][]
                {
                        {"Test Run Date", "29/Nov/05 4:27 PM"}});
        assertTableRowsEqual("updatedfields", 3, new String[][]
                {
                        {"Test Run Number", "1"}});
        assertTableRowsEqual("updatedfields", 4, new String[][]
                {
                        {"Release Date", "21/Dec/05"}});
        assertTableRowsEqual("updatedfields", 5, new String[][]
                {
                        {"Management Comments", "Functional test - management comment"}});
        assertTableRowsEqual("updatedfields", 6, new String[][]
                {
                        {"Affected Business Units", "Development"}});
        assertTableRowsEqual("updatedfields", 7, new String[][]
                {
                        { "Affected Users", ADMIN_FULLNAME }});
        assertTableRowsEqual("updatedfields", 8, new String[][]
                {
                        {"Related Projects", "Test Project 1"}});
        assertTableRowsEqual("updatedfields", 9, new String[][]
                {
                        { "End User", ADMIN_FULLNAME }});
        assertTableRowsEqual("updatedfields", 10, new String[][]
                {
                        {"LImtied Text Field", "Func Test - Limited text Field"}});
        assertTableRowsEqual("updatedfields", 11, new String[][]
                {
                        {"Description", "Func Test - Description"}});
        assertTableRowsEqual("updatedfields", 12, new String[][]
                {
                        {"Due Date", "29/Dec/05"}});
        assertTableRowsEqual("updatedfields", 13, new String[][]
                {
                        {"Environment", "Func test environment"}});
        assertTableRowsEqual("updatedfields", 14, new String[][]
                {
                        {"Issue Type", "New Feature"}});
        assertTableRowsEqual("updatedfields", 15, new String[][]
                {
                        {"Operating System", "Linux"}});
        assertTableRowsEqual("updatedfields", 16, new String[][]
                {
                        {"Priority", "Critical"}});
        assertTableRowsEqual("updatedfields", 17, new String[][]
                {
                        { "Reporter", ADMIN_FULLNAME }});
        assertTableRowsEqual("updatedfields", 18, new String[][]
                {
                        {"Web Address", "http://www.atlassian.com"}});
        assertTableRowsEqual("updatedfields", 19, new String[][]
                {
                        {"Resolution", "Fixed"}});
        assertTableRowsEqual("updatedfields", 20, new String[][]
                {
                        {"Comment", "Bulk Edit Comment"}});
    }

    private void assertAllFieldsPresent()
    {
        assertTextPresent("Change Resolution");
        assertTextPresent("Change Fix Version/s");
        assertTextPresent("Change Assignee");
        assertTextPresent("Change Test Run Date");
        assertTextPresent("Change Test Run Number");
        assertTextPresent("Change Patched Version");
        assertTextPresent("Change Release Date");
        assertTextPresent("Change Management Comments");
        assertTextPresent("Change Affected Business Units");
        assertTextPresent("Change Affected Users");
        assertTextPresent("Change Related Projects");
        assertTextPresent("Change End User");
        assertTextPresent("Change LImtied Text Field");
        assertTextPresent("Change Affects Version/s");
        assertTextPresent("Change Component/s");
        assertTextPresent("Change Description");
        assertTextPresent("Change Due Date");
        assertTextPresent("Change Environment");
        assertTextPresent("Change Import Id");
        assertTextPresent("Change Issue Type");
        assertTextPresent("Change Operating System");
        assertTextPresent("Change Priority");
        assertTextPresent("Change Read Only Field");
        assertTextPresent("Change Reporter");
        assertTextPresent("Change Summary");
        assertTextPresent("Change Web Address");
        assertTextPresent("Change Time Tracking");
        assertTextPresent("Change Comment");
    }

    private void assertWikiRendererCommentField()
    {
        assertTextPresent("comment-preview_link");
        assertLinkPresent("viewHelp");
    }

    private void editAllFields()
    {
        checkCheckbox("actions", "resolution");

        selectOption("resolution", "Fixed");

        checkCheckbox("actions", "assignee");
        selectOption("assignee", ADMIN_FULLNAME);

        checkCheckbox("actions", "customfield_10000");
        setFormElement("customfield_10000", "29/Nov/2005 04:27 PM");

        checkCheckbox("actions", "customfield_10003");
        setFormElement("customfield_10003", "1");

        checkCheckbox("actions", "customfield_10006");
        setFormElement("customfield_10006", "21/Dec/2005");

        checkCheckbox("actions", "customfield_10007");
        setFormElement("customfield_10007", "Functional test - management comment");

        checkCheckbox("actions", "customfield_10008");
        checkCheckbox("customfield_10008", "10006");

        checkCheckbox("actions", "customfield_10009");
        setFormElement("customfield_10009", ADMIN_USERNAME);

        checkCheckbox("actions", "customfield_10010");
        selectOption("customfield_10010", "Test Project 1");

        checkCheckbox("actions", "customfield_10013");
        setFormElement("customfield_10013", ADMIN_USERNAME);

        checkCheckbox("actions", "customfield_10014");
        setFormElement("customfield_10014", "Func Test - Limited text Field");

        checkCheckbox("actions", "description");
        setFormElement("description", "Func Test - Description");

        checkCheckbox("actions", "duedate");
        setFormElement("duedate", "29/Dec/2005");

        checkCheckbox("actions", "environment");
        setFormElement("environment", "Func test environment");

        checkCheckbox("actions", "issuetype");
        selectOption("issuetype", "New Feature");

        checkCheckbox("actions", "customfield_10002");
        selectOption("customfield_10002", "Linux");

        checkCheckbox("actions", "priority");
        selectOption("priority", "Critical");

        checkCheckbox("actions", "reporter");
        setFormElement("reporter", ADMIN_USERNAME);

        checkCheckbox("actions", "customfield_10004");
        setFormElement("customfield_10004", "http://www.atlassian.com");

        checkCheckbox("commentaction", "comment");
        setFormElement("comment", "Bulk Edit Comment");
    }

    private void generateMultipleInputErrors()
    {
        // Set invalid data
        checkCheckbox("actions", "resolution");
        selectOption("resolution", "Fixed");
        checkCheckbox("actions", "duedate");
        setFormElement("duedate", "functest");
        checkCheckbox("actions", "customfield_10000");
        setFormElement("customfield_10000", "functest");
        checkCheckbox("actions", "customfield_10004");
        setFormElement("customfield_10004", "functest");
        checkCheckbox("actions", "customfield_10006");
        setFormElement("customfield_10006", "functest");
        checkCheckbox("actions", "customfield_10009");
        setFormElement("customfield_10009", "functest");

        navigation.clickOnNext();

        // Assert error messages:
        assertTextPresent("You did not enter a valid date. Please enter the date in the format");
        assertTextPresent("Not a valid URL");
        assertTextPresent("Invalid date format. Please enter the date in the format &quot;d/MMM/yy&quot;.");
    }

    private void verifyAtSessionTimeoutPage()
    {
        assertTextPresent("Your session timed out while performing bulk operation on issues.");
    }

    protected void _testToOperationDetailsWorkflowTranisition()
    {
        displayAllIssues();
        bulkChangeIncludeAllPages();
        assertTextPresent("Step 1 of 4: Choose Issues");
        bulkChangeChooseIssuesAll();
        assertTextPresent("Choose the operation you wish to perform");
        chooseOperationExecuteWorfklowTransition();
        navigation.workflows().assertStepOperationDetails();
        assertTextPresent(WORKFLOW_TRANSITION_SELECTION_TEXT);
    }

    protected void restoreWithGlobalWorkflowPermission(String restoreXml)
    {
        restoreData(restoreXml);
        grantGlobalPermission(BULK_CHANGE, Groups.USERS);
    }

    /**
     * Multiple workflows - Make sure multiple present - only select one transition
     */
    public void testBulkWorkFlowTransitionMultipleWorkflowsExists()
    {
        restoreWithGlobalWorkflowPermission("TestBulkWorkflowTransitionEnterprise.xml");
        _testToOperationDetailsWorkflowTranisition();
        navigation.clickOnNext();
        navigation.workflows().assertStepOperationDetails();
        // Assert that two tables exist - thus two workflows (Enterprise feature)
        assertTablePresent("workflow_0");
        assertTablePresent("workflow_1");
    }

    public void testFunkyWorkflowName()
    {
        restoreWithGlobalWorkflowPermission("TestBulkWorkflowTransitionEnterprise4.xml");
        _testToOperationDetailsWorkflowTranisition();
        checkCheckbox("wftransition", "Second_Workflow_4_3");
        submit("Next");
        assertTextPresent(WORKFLOW_TRANSITION_EDIT_TEXT);

    }

    public void testCustomFieldContextAndIssueTypeBehaviour()
    {
        restoreData("TestBulkWorkflowTranistionForCustomFieldContextAndIssueType.xml");

        // Display all the
        displayAllIssues();
        bulkChangeIncludeAllPages();

        // Select the two 'Monkey' Issues
        checkCheckbox("bulkedit_10002", "on");
        checkCheckbox("bulkedit_10001", "on");
        submit("Next");

        // Select the transition workflow operation
        checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        submit("Next");

        // Select to resolve these two issues
        navigation.workflows().chooseWorkflowAction("classic default workflow_5_5");

        // Make sure we are on the edit screen for the bulk workflow transition operation.
        assertTextPresent(WORKFLOW_TRANSITION_EDIT_TEXT);

        // Now check which fields should be displayed/not displayed
        // Select Resolution
        checkCheckbox("actions", "resolution");
        selectOption("resolution", "Fixed");

        // We can edit the Monkey Custom field (context = Project Monkey)
        checkCheckbox("actions", "customfield_10000");
        setFormElement("customfield_10000", "Setting Monkey Custom Field");

        // We can't edit Man Custom Field (context = Project Homosapien, Issue Type = Bug)
        assertTableRowsEqual(TABLE_EDITFIELDS_ID, 4, new String[][] {
                { "N/A", "Change Man Custom Field", "NOTE: The field is not available for all issues with the same configuration." } });

        // We can edit the Man and Monkey custom field (context is for both projects - Not global though!)
        checkCheckbox("actions", "customfield_10010");
        checkCheckbox("customfield_10010", "10001");

        // We can edit the Global Field since it is global
        checkCheckbox("actions", "customfield_10011");
        setFormElement("customfield_10011", "Setting the global field");

        // We can edit the 'Bug Only Field' since it is available for Issues of type Bug in a global context.
        checkCheckbox("actions", "customfield_10012");
        setFormElement("customfield_10012", "8/Sep/06");

        navigation.clickOnNext();

        // Assert that all the fields that are going to be updated are present.
        assertTextInTable("updatedfields", "Fixed");
        assertTextInTable("updatedfields", "Setting Monkey Custom Field");
        assertTextInTable("updatedfields", "Monkey");
        assertTextInTable("updatedfields", "Setting the global field");
        assertTextInTable("updatedfields", "08/Sep/06");

        // Submit the form
        navigation.clickOnNext();

        waitAndReloadBulkOperationProgressPage();

        // Go to one of the issues and make sure it has been correctly transitioned and updated
        gotoIssue("MKY-2");

        text.assertTextPresent(new IdLocator(tester, "rowForcustomfield_10000"), "Setting Monkey Custom Field");
        text.assertTextPresent(new IdLocator(tester, "rowForcustomfield_10010"), "Monkey");
        text.assertTextPresent(new IdLocator(tester, "rowForcustomfield_10011"), "Setting the global field");
        text.assertTextPresent(new IdLocator(tester, "customfield_10012-val"), "08/Sep/06");


        //assert that that workflow transitions have been applied and index is updated
        assertIndexedFieldCorrect("//customfields/customfield/customfieldvalues", EasyMap.build("customfieldvalue", "Setting Monkey Custom Field", "customfieldvalue", "Monkey", "customfieldvalue", "Setting the global field"), null, "MKY-2");
        assertIndexedFieldCorrect("//item", EasyMap.build("resolution", "Fixed", "status", "Resolved"), null, "MKY-2");
    }

    private static final class IssueTransitioner extends JIRAWebTest
    {
        public IssueTransitioner()
        {
            super("IssueTransitioner");
        }

        void closeIssue(String issueKey)
        {
            gotoIssue(issueKey);
            clickLink("action_id_2");
            setWorkingForm("issue-workflow-transition");
            submit("Transition");
        }
    }
}
