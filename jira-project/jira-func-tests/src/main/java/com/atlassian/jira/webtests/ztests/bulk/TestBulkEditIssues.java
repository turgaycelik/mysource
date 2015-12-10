package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.ISSUES })
public class TestBulkEditIssues extends BulkChangeIssues
{
    private static final String HSP_5 = "HSP-5";
    private static final String TST_1 = "TST-1";

    private static final String COMMENT_1 = "This issue is resolved now.";
    private static final String COMMENT_2 = "Viewable by developers group.";
    private static final String COMMENT_3 = "Viewable by Developers role.";
    private static final String UNAVAILABLE_BULK_EDIT_ACTIONS_TABLE_ID = "unavailableActionsTable";
    private static final String SESSION_TIMEOUT_MESSAGE_CONTAINER_LOCATOR = ".aui-message.warning";

    public TestBulkEditIssues(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        bulkChangeSetup();
        administration.addGlobalPermission(BULK_CHANGE, Groups.USERS);
    }

    public void tearDown()
    {
        administration.removeGlobalPermission(BULK_CHANGE, Groups.USERS);
        super.tearDown();
    }

    public void testBulkEditIssues()
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        _testBulkEditOneIssueInCurrentPage();
        _testBulkEditOneIssueInAllPages();
        _testBulkEditAllIssuesInAllPages();
        // At the moment this must go last since as a sideeffect it sets HttpUnitOptions.setScriptingEnabled(false);
        _testBulkEditAllIssuesInCurrentPage();
    }

    /**
     * tests to see if editing one issue in the current page works.
     */
    public void _testBulkEditOneIssueInCurrentPage()
    {
        log("Bulk Change - Edit Operation: ONE issue from CURRENT page");
        String summary = "EditOneIssueInCurrentPage";
        addCurrentPageLink();
        String key = addIssue(summary);
        assertIndexedFieldCorrect("//item", EasyMap.build("key", key), EasyMap.build("fixVersion", VERSION_NAME_ONE, "version", VERSION_NAME_TWO), key);
        createSessionSearchForAll();
        bulkChangeIncludeCurrentPage();
        bulkChangeSelectIssue(key);

        bulkChangeChooseOperationEdit();
        Map fields = new HashMap();
        fields.put(FIELD_FIX_VERSIONS, OPTION_VERSION_ONE);
        fields.put(FIELD_VERSIONS, OPTION_VERSION_TWO);
        fields.put(FIELD_ASSIGNEE, ADMIN_FULLNAME);
        fields.put(FIELD_PRIORITY, OPTION_PRIORITY_THREE);
        bulkEditOperationDetailsSetAs(fields);
        fields = new HashMap();
        fields.put(FIELD_FIX_VERSIONS, VERSION_NAME_ONE);
        fields.put(FIELD_VERSIONS, VERSION_NAME_TWO);
        fields.put(FIELD_ASSIGNEE, ADMIN_FULLNAME);
        fields.put(FIELD_PRIORITY, TYPE_PRIORITY_THREE);
        bulkEditConfirmEdit(fields);
        bulkChangeConfirm();
        waitAndReloadBulkOperationProgressPage();

        //make sure we were redirected the right page
        assertions.getURLAssertions().assertCurrentURLEndsWith("/issues/?jql");

        //make sure that the individual issue was updated during the bulk edit
        //assertIndexedFieldCorrect("//item", EasyMap.build("fixVersion", VERSION_NAME_ONE, "version", VERSION_NAME_TWO, FIELD_ASSIGNEE, ADMIN_FULLNAME, FIELD_PRIORITY, TYPE_PRIORITY_THREE), null, key);
    }

    /**
     * tests to see if editing one issue in all the pages works.
     */
    public void _testBulkEditOneIssueInAllPages()
    {
        log("Bulk Change - Edit Operation: ONE issue from ALL pages");
        String summary = "EditOneIssueInAllPages";
        addCurrentPageLink();
        String key = addIssue(summary);
        displayAllIssues();
        bulkChangeIncludeAllPages();

        bulkChangeSelectIssue(key);

        bulkChangeChooseOperationEdit();

        Map fields = new HashMap();
        fields.put(FIELD_FIX_VERSIONS, OPTION_VERSION_ONE);
        fields.put(FIELD_VERSIONS, OPTION_VERSION_ONE);
        fields.put(FIELD_COMPONENTS, OPTION_COMPONENT_ONE);
        fields.put(FIELD_ASSIGNEE, ADMIN_FULLNAME);
        fields.put(FIELD_PRIORITY, OPTION_PRIORITY_ONE);
        bulkEditOperationDetailsSetAs(fields);
        fields = new HashMap();
        fields.put(FIELD_FIX_VERSIONS, VERSION_NAME_ONE);
        fields.put(FIELD_VERSIONS, VERSION_NAME_ONE);
        fields.put(FIELD_COMPONENTS, COMPONENT_NAME_ONE);
        fields.put(FIELD_ASSIGNEE, ADMIN_FULLNAME);
        fields.put(FIELD_PRIORITY, TYPE_PRIORITY_ONE);
        bulkEditConfirmEdit(fields);
        bulkChangeConfirm();
        waitAndReloadBulkOperationProgressPage();

        //make sure we were redirected the right page
        assertions.getURLAssertions().assertCurrentURLEndsWith("/issues/?jql");
    }

    /**
     * TODO: clean this up AND DEAL WITH JAVASCRIPT ISSUE
     * tests to see if editing all issue in the current page works.
     */
    public void _testBulkEditAllIssuesInCurrentPage()
    {
        log("Bulk Change - Edit Operation: ALL issue from CURRENT pages");
        setUnassignedIssuesOption(true);
        addCurrentPageLink();
        displayAllIssues();
        bulkChangeIncludeCurrentPage();

        bulkChangeChooseIssuesAll();

        bulkChangeChooseOperationEdit();

        Map fields = new HashMap();
        fields.put(FIELD_FIX_VERSIONS, OPTION_VERSION_TWO);
        fields.put(FIELD_VERSIONS, OPTION_VERSION_TWO);
        fields.put(FIELD_COMPONENTS, OPTION_COMPONENT_ONE);
        fields.put(FIELD_ASSIGNEE, OPTION_UNASSIGNED);
        bulkEditOperationDetailsSetAs(fields);
        fields = new HashMap();
        fields.put(FIELD_FIX_VERSIONS, VERSION_NAME_TWO);
        fields.put(FIELD_VERSIONS, VERSION_NAME_TWO);
        fields.put(FIELD_COMPONENTS, COMPONENT_NAME_ONE);
        fields.put(FIELD_ASSIGNEE, OPTION_UNASSIGNED);
        bulkEditConfirmEdit(fields);
        bulkChangeConfirm();
        waitAndReloadBulkOperationProgressPage();

        //make sure we were redirected the right page
        assertions.getURLAssertions().assertCurrentURLEndsWith("/issues/?jql");

        deleteAllIssuesInAllPages();
        setUnassignedIssuesOption(false);
    }

    /**
     * tests to see if editing all issues in all the pages works.
     */
    public void _testBulkEditAllIssuesInAllPages()
    {
        log("Bulk Change - Edit Operation: ALL issue from ALL pages");
        addCurrentPageLink();

        displayAllIssues();
        bulkChangeIncludeAllPages();

        bulkChangeChooseIssuesAll();

        bulkChangeChooseOperationEdit();

        Map<String,String> fields = new HashMap<String,String>();
        fields.put(FIELD_VERSIONS, OPTION_VERSION_TWO);
        fields.put(FIELD_ASSIGNEE, ADMIN_FULLNAME);
        fields.put(FIELD_PRIORITY, OPTION_PRIORITY_ONE);
        bulkEditOperationDetailsSetAs(fields);
        fields = new HashMap<String,String>();
        fields.put(FIELD_VERSIONS, VERSION_NAME_TWO);
        fields.put(FIELD_ASSIGNEE, ADMIN_FULLNAME);
        fields.put(FIELD_PRIORITY, TYPE_PRIORITY_ONE);
        bulkEditConfirmEdit(fields);
        bulkChangeConfirm();
        waitAndReloadBulkOperationProgressPage();

        //make sure we were redirected the right page
        assertions.getURLAssertions().assertCurrentURLEndsWith("/issues/?jql");

        //assert that the index has been updated for *some* issues

        assertIndexedFieldCorrect("//item", EasyMap.build("version", VERSION_NAME_TWO, FIELD_ASSIGNEE, ADMIN_FULLNAME, FIELD_PRIORITY, TYPE_PRIORITY_ONE), null, "HSP-1");
        assertIndexedFieldCorrect("//item", EasyMap.build("version", VERSION_NAME_TWO, FIELD_ASSIGNEE, ADMIN_FULLNAME, FIELD_PRIORITY, TYPE_PRIORITY_ONE), null, "HSP-5");
        assertIndexedFieldCorrect("//item", EasyMap.build("version", VERSION_NAME_TWO, FIELD_ASSIGNEE, ADMIN_FULLNAME, FIELD_PRIORITY, TYPE_PRIORITY_ONE), null, "HSP-9");
    }

    // This test is to cover JRA-10167
    public void testBulkEditIssuesIssueTypesWithDiffWorkflows()
    {
        administration.restoreData("TestBulkEditIssues.xml");
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeSelectIssue(HSP_5);
        bulkChangeChooseOperationEdit();
        // Make certain that we only have the three options we expect
        tester.assertOptionsEqual("issuetype", new String[] { "Test Bug", "Test Improvment", "Test New Feature" });
    }

    // This test will have
    // This test is to cover JRA-10167
    public void testBulkEditIssuesIssueTypesWithDiffWorkflowsMultipleIssues()
    {
        administration.restoreData("TestBulkEditIssues.xml");
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeSelectIssues(Arrays.asList("HSP-2", TST_1));
        bulkChangeChooseOperationEdit();
        tester.assertOptionsEqual("issuetype", new String[] { "Task" });
    }

    // This test will have no issuetype options because the issues start from
    // different workflows and have no issue types in their issue type schemes
    // that have that workflow in common. This test is to cover JRA-10167.
    public void testBulkEditIssuesIssueTypesWithDifferentWorkflowsNoOptions()
    {
        administration.restoreData("TestBulkEditIssues.xml");
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeSelectIssues(Arrays.asList(HSP_5, TST_1));
        bulkChangeChooseOperationEdit();
        text.assertTextPresent(locator.id(UNAVAILABLE_BULK_EDIT_ACTIONS_TABLE_ID),
                "There are no issue types available for selected issues.");
    }

    // This test expects there to be only one issuetype option because
    // the BUG and TASK will be dropped out by the fact that it has a
    // different workflow configured for those types and the New Feature
    // option has a different field configuration scheme associated with
    // it.
    public void testBulkEditIssuesIssueTypesWithDifferentFieldConfigurationSchemes()
    {
        administration.restoreData("TestBulkEditIssues.xml");
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeSelectIssue("TST-3");
        bulkChangeChooseOperationEdit();
        tester.assertOptionsEqual("issuetype", new String[] { "Improvement" });
    }


    // This tests that a sub-task issue type will not be shown if it has a different workflow associated
    // with it.
    public void testBulkEditIssuesIssueTypesWithDifferentWorkflowsSubtasks()
    {
        administration.restoreData("TestBulkEditIssues.xml");
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeSelectIssue("HSP-6");
        bulkChangeChooseOperationEdit();
        tester.assertOptionsEqual("issuetype", new String[] { "Super Sub-taks" });
    }

    // This tests that a sub-task issue type will not be shown if it has a different field configuration scheme
    // associated with it.
    public void testBulkEditIssuesIssueTypesWithDiffFieldConfSchemesSubtasks()
    {
        administration.restoreData("TestBulkEditIssues.xml");
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeSelectIssue("TST-4");
        bulkChangeChooseOperationEdit();
        tester.assertOptionsEqual("issuetype", new String[] { ISSUE_TYPE_SUB_TASK });
    }

    public void testBulkEditSessionTimeouts()
    {
        log("Bulk Edit - Test that you get redirected to the session timeout page when jumping into the wizard");

        administration.restoreBlankInstance();
        tester.beginAt("secure/views/bulkedit/BulkEditDetails.jspa");
        verifyAtSessionTimeoutPage();
        final String xsrfToken = page.getXsrfToken();
        tester.beginAt("secure/views/bulkedit/BulkEditDetailsValidation.jspa?atl_token=" + xsrfToken);
        verifyAtSessionTimeoutPage();
    }

    private void verifyAtSessionTimeoutPage()
    {
        text.assertTextPresent
                (
                        locator.css(SESSION_TIMEOUT_MESSAGE_CONTAINER_LOCATOR),
                        "Your session timed out while performing bulk operation on issues."
                );
    }

    public void testBulkEditWithCommentVisibility()
    {
        administration.restoreData("TestBulkEditIssues.xml");
        enableCommentGroupVisibility(Boolean.TRUE);

        // comment visible to all users
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeSelectIssues(Arrays.asList(HSP_5, TST_1));
        bulkChangeChooseOperationEdit();
        tester.checkCheckbox("actions", "priority");
        tester.selectOption("priority", "Minor");
        tester.checkCheckbox("actions", "comment");
        tester.setFormElement("comment", COMMENT_1);
        tester.submit("Next");
        bulkChangeConfirm();
        waitAndReloadBulkOperationProgressPage();

         // comment visible to jira-developers group only
        addComment(COMMENT_2, "jira-developers");
        // comment visible to Developers role only
        addComment(COMMENT_3, "Developers");
        // comment visible to Users role
        addComment("This comment should be visible to users role.", "Users");
        // comment visible to jira users
        addComment("This comment should be visible to jira-users group", "jira-users");
        // comment visible to jira admins
        addComment("this comment should be visible to jira admins", "jira-administrators");
        // comment visible to administrators role
        addComment("this comment should be visible to Administrators role", "Administrators");

        List userComments = EasyList.build("This comment should be visible to users role.", "This comment should be visible to jira-users group", COMMENT_1);
        List developerComments = EasyList.build(COMMENT_2, COMMENT_3);
        List adminComments = EasyList.build("this comment should be visible to jira admins","this comment should be visible to Administrators role");

        // verify that Fred can see general comment but not others as he is not in the visibility groups
        checkCommentVisibility(FRED_USERNAME, "HSP-5", userComments, EasyList.mergeLists(developerComments, adminComments, null));
        checkCommentVisibility(FRED_USERNAME, "TST-1", userComments, EasyList.mergeLists(developerComments, adminComments, null));

        //admin only guy should not see developer comments
        checkCommentVisibility("adminman", "HSP-5", EasyList.mergeLists(adminComments, userComments, null), developerComments);
        checkCommentVisibility("adminman", "TST-1", EasyList.mergeLists(adminComments, userComments, null), developerComments);

        //developer guy should not see admin comments
        checkCommentVisibility("devman", "HSP-5", EasyList.mergeLists(userComments, developerComments, null), adminComments);
        checkCommentVisibility("devman", "TST-1", EasyList.mergeLists(userComments, developerComments, null), adminComments);

        // verify that Admin can see all comments as he is in all visibility groups
        checkCommentVisibility(ADMIN_USERNAME, "HSP-5", EasyList.mergeLists(developerComments, adminComments, userComments), null);
        checkCommentVisibility(ADMIN_USERNAME, "TST-1", EasyList.mergeLists(developerComments, adminComments, userComments), null);
    }

    private void addComment(String comment, String commentLevel)
    {
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeSelectIssues(Arrays.asList(HSP_5, TST_1));
        bulkChangeChooseOperationEdit();
        tester.checkCheckbox("actions", "priority");
        tester.selectOption("priority", "Minor");
        tester.checkCheckbox("actions", "comment");
        tester.setFormElement("comment", comment);
        tester.selectOption("commentLevel", commentLevel);
        tester.submit("Next");
        bulkChangeConfirm();
        waitAndReloadBulkOperationProgressPage();
    }
}
