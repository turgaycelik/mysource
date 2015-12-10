package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.SUB_TASKS })
public class TestSubTaskToIssueConversionStep1 extends JIRAWebTest
{
    private static final String BUG_KEY = "HSP-1";
    private static final String BUG_ID = "10000";
    private static final String SUBTASK_KEY = "HSP-3";
    private static final String SUBTASK_ID = "10002";
    private static final String SUBTASK_KEY_NO_FEATURES_IN_SCHEME = "MKY-2";
    private static final String SUBTASK_ID_NO_FEATURES_IN_SCHEME = "10021";
    private static final String FEATURE_KEY = "HSP-4";
    private static final String FEATURE_ID = "10010";
    private static final String TASK_KEY = "HSP-5";
    private static final String IMPROVEMENT_KEY = "HSP-6";
    private static final String INVALID_ISSUE_ID = "9999";


    public TestSubTaskToIssueConversionStep1(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestSubTaskToIssueConversion.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    /*
     * Tests that convert subtask to issue is only available to users with Permissions.EDIT_ISSUE
     */
    public void testSubTaskToIssueConversionEditPermission()
    {
        gotoIssue(SUBTASK_KEY);
        assertLinkPresent("subtask-to-issue");
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUBTASK_KEY);
        assertLinkNotPresent("subtask-to-issue");

        gotoConvertSubTask(SUBTASK_ID);
        assertTextPresent("Access Denied");
        assertTextNotPresent("Step 1 of 4");

        logout();
        gotoConvertSubTask(SUBTASK_ID);
        assertTextPresent("You must log in to access this page.");
    }

    /*
     * Tests that only subtasks can be converted to issues
     */
    public void testSubTaskToIssueConversionCheckIssueType()
    {
        gotoIssue(SUBTASK_KEY);
        assertLinkPresent("subtask-to-issue");
        gotoIssue(BUG_KEY);
        assertLinkNotPresent("subtask-to-issue");
        gotoIssue(FEATURE_KEY);
        assertLinkNotPresent("subtask-to-issue");
        gotoIssue(TASK_KEY);
        assertLinkNotPresent("subtask-to-issue");
        gotoIssue(IMPROVEMENT_KEY);
        assertLinkNotPresent("subtask-to-issue");

        gotoConvertSubTask(FEATURE_ID);
        assertTextPresent("Issue HSP-4 is not a sub-task");
        assertTextNotPresent("Step 1 of 4");

        // Check Wizard pane shows correct link
        assertTextPresentBeforeText("Return to", FEATURE_KEY);
        assertLinkPresentWithText(FEATURE_KEY);

        gotoConvertSubTask(SUBTASK_ID);
        assertTextPresent("Step 1 of 4");



    }

    /*
     * Tests that an invalid issue id returns error
     */
    public void testIssueToSubTaskConversionInvalidIssue()
    {
        gotoConvertSubTask(SUBTASK_ID);
        assertTextPresent("Step 1 of 4");

        gotoConvertSubTask(INVALID_ISSUE_ID);
        assertTextPresent("Errors");
        assertTextPresent("Issue not found");
        assertTextNotPresent("Step 1 of 4");

        // Check Wizard pane shows correct link
        assertTextPresentBeforeText("Return to", "Dashboard");
        assertLinkPresentWithText("Dashboard");
    }

    /*
    * Tests that the new issue type list only contains valid issue types
    */
    public void testIssueToSubTaskConversionIssueType()
    {

        gotoConvertSubTask(SUBTASK_ID);
        assertOptionsEqual("issuetype", new String[]{ISSUE_TYPE_BUG, ISSUE_TYPE_NEWFEATURE, ISSUE_TYPE_TASK, ISSUE_TYPE_IMPROVEMENT});
        assertOptionValueNotPresent("issuetype", ISSUE_TYPE_SUB_TASK);

        // Project where SUBTASK_TYPE_2 doesn't exist
        gotoConvertSubTask(SUBTASK_ID_NO_FEATURES_IN_SCHEME);
        assertOptionsEqual("issuetype", new String[]{ISSUE_TYPE_BUG, ISSUE_TYPE_IMPROVEMENT, ISSUE_TYPE_TASK});
        assertOptionValueNotPresent("issuetype", ISSUE_TYPE_NEWFEATURE);

        // Test issue type not in project
        gotoConvertSubTaskStep2(SUBTASK_ID_NO_FEATURES_IN_SCHEME, "2");
        assertTextPresent("Step 1 of 4");
        assertTextPresent("Issue type New Feature not applicable for this project");

        //Test invalid issue type
        gotoConvertSubTaskStep2(SUBTASK_ID_NO_FEATURES_IN_SCHEME, "9999");
        assertTextPresent("Step 1 of 4");
        assertTextPresent("Selected issue type not found.");

        //Test no issue type passed
        gotoConvertSubTaskStep2(SUBTASK_ID_NO_FEATURES_IN_SCHEME, "");
        assertTextPresent("Step 1 of 4");
        assertTextPresent("Issue type not specified");

        //Test subtask issue type
        gotoConvertSubTaskStep2(SUBTASK_ID_NO_FEATURES_IN_SCHEME, "5");
        assertTextPresent("Step 1 of 4");
        assertTextPresent("Issue type Sub-task is a sub-task");


    }

}
