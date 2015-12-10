package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.SECURITY, Category.SUB_TASKS })
public class TestSubTaskToIssueConversionSecurityLevel extends JIRAWebTest
{
    private static final String SUB_SL_REQ_NO_VAL = "HSP-8"; // sub task where security level required but parent has none
    private static final String PARENT_NO_VAL = "HSP-7";
    private static final String SUB_SL_REQ_HAS_VAL = "HSP-6"; // sub task where security level required and set to parent
    private static final String SUB_SL_HIDDEN = "HSP-9";

    public TestSubTaskToIssueConversionSecurityLevel(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestSubTaskToIssueConversionSecurityLevel.xml");
    }

    /*
     * Tests that when the issue to convert has no security level and the target type has it as optional
     * the user is not prompted and it is not on the confirm screen, and checks the issue has not been updated
     */
    public void testSubTaskToIssueConversionSecurityLevelNoValToOptional()
    {
        // Check original security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_REQ_NO_VAL);
        assertTextPresent(SUB_SL_REQ_NO_VAL);

        // Convert
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(SUB_SL_REQ_NO_VAL);
        assertTextNotPresent("Security Level:");

        clickLink("subtask-to-issue");
        selectOption("issuetype", ISSUE_TYPE_BUG);
        submit("Next >>");
        assertTextPresent("All fields will be updated automatically.");
        submit("Next >>");
        assertTextNotPresent("Security Level");
        submit("Finish");


        assertTextNotPresent("Security Level");

        // Ensure Parent Issue link is removed
        assertTextNotPresent(PARENT_NO_VAL);
        clickLinkWithText(CHANGE_HISTORY);
        assertTextSequence(new String[] { CHANGE_HISTORY, PARENT_NO_VAL });

        gotoIssue(PARENT_NO_VAL);
        assertTableNotPresent(SUB_SL_REQ_NO_VAL);

        // Check new security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_REQ_NO_VAL);
        assertTextPresent(SUB_SL_REQ_NO_VAL);
    }

    /*
     * Tests that when the issue to convert has no security level and the target type has it as hidden
     * the user is not prompted and it is not on the confirm screen, and checks the issue has not been updated
     */
    public void testSubTaskToIssueConversionSecurityLevelNoValToHidden()
    {
        // Check original security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_REQ_NO_VAL);
        assertTextPresent(SUB_SL_REQ_NO_VAL);

        // Convert
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(SUB_SL_REQ_NO_VAL);
        assertTextNotPresent("Security Level:");

        clickLink("subtask-to-issue");
        selectOption("issuetype", ISSUE_TYPE_NEWFEATURE);
        submit("Next >>");
        assertTextPresent("All fields will be updated automatically.");
        submit("Next >>");
        assertTextNotPresent("Security Level");
        submit("Finish");

        assertTextNotPresent("Security Level");

        // Check new security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_REQ_NO_VAL);
        assertTextPresent(SUB_SL_REQ_NO_VAL);
    }

    /*
     * Tests that when the issue to convert has no security level and the target type has it as required
     * the user is prompted and it is on the confirm screen, and checks the issue has been updated
     */
    public void testSubTaskToIssueConversionSecurityLevelNoValToRequired()
    {
        // Check original security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_REQ_NO_VAL);
        assertTextPresent(SUB_SL_REQ_NO_VAL);

        // Convert
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(SUB_SL_REQ_NO_VAL);
        assertTextNotPresent("Security Level:");

        clickLink("subtask-to-issue");
        selectOption("issuetype", ISSUE_TYPE_IMPROVEMENT);
        submit("Next >>");
        assertTextPresent("Security Level");
        assertTextSequence(new String[] { "Security Level", "Developers" });
        selectOption("security", "Developers");
        submit("Next >>");
        assertTextSequence(new String[] { "Security Level", "None", "Developers" });

        submit("Finish");

        assertTextSequence(new String[] { "Security Level", "Developers" });


        // Check new security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_REQ_NO_VAL);
        assertTextPresent("Permission Violation");
    }
    /*
     * Tests that when the issue to convert has security level and the target type has it as optional/required
     * the user is not prompted and it is not on the confirm screen, and checks the issue has not been updated
     */
    public void testSubTaskToIssueConversionSecurityLevelValToOptional()
    {
        // Check original security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_REQ_HAS_VAL);
        assertTextPresent("Permission Violation");

        // Convert
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(SUB_SL_REQ_HAS_VAL);
        assertTextSequence(new String[] { "Security Level", "Developers" });

        clickLink("subtask-to-issue");
        selectOption("issuetype", ISSUE_TYPE_IMPROVEMENT);
        submit("Next >>");
        assertTextNotPresent("Security Level");
        assertTextPresent("All fields will be updated automatically.");
        submit("Next >>");
        assertTextNotPresent("Security Level");
        submit("Finish");


        assertTextSequence(new String[] { "Security Level", "Developers" });

        // Check new security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_REQ_HAS_VAL);
        assertTextPresent("Permission Violation");
    }
    /*
     * Tests that when the issue to convert has security level and the target type has it as hidden
     * the user is not prompted and it is on the confirm screen, and checks the issue has been updated
     */
    public void testSubTaskToIssueConversionSecurityLevelValToHidden()
    {
        // Check original security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_REQ_HAS_VAL);
        assertTextPresent("Permission Violation");

        // Convert
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(SUB_SL_REQ_HAS_VAL);
        assertTextSequence(new String[] { "Security Level", "Developers" });

        clickLink("subtask-to-issue");
        selectOption("issuetype", ISSUE_TYPE_NEWFEATURE);
        submit("Next >>");
        assertTextNotPresent("Security Level");
        assertTextPresent("All fields will be updated automatically.");
        submit("Next >>");
        assertTextSequence(new String[] { "Security Level", "Developers" });
        submit("Finish");


        assertTextNotPresent("Security Level");

        // Check new security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_REQ_HAS_VAL);
        assertTextPresent(SUB_SL_REQ_HAS_VAL);
    }

    /*
     * Tests that when the issue to convert has security level hidden and the target type has it as optional
     * the user is not prompted and it is not on the confirm screen, and checks the issue has not been updated
     */
    public void testSubTaskToIssueConversionSecurityLevelHiddenToOptional()
    {
        // Check original security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_HIDDEN);
        assertTextPresent(SUB_SL_HIDDEN);

        // Convert
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(SUB_SL_HIDDEN);
        assertTextNotPresent("Security Level");

        clickLink("subtask-to-issue");
        selectOption("issuetype", ISSUE_TYPE_BUG);
        submit("Next >>");
        assertTextNotPresent("Security Level");
        assertTextPresent("All fields will be updated automatically.");
        submit("Next >>");
        assertTextNotPresent("Security Level");
        submit("Finish");

        assertTextNotPresent("Security Level");

        // Check new security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_HIDDEN);
        assertTextPresent(SUB_SL_HIDDEN);
    }
    /*
     * Tests that when the issue to convert has security level hidden and the target type has it as hidden
     * the user is not prompted and it is not on the confirm screen, and checks the issue has not been updated
     */
    public void testSubTaskToIssueConversionSecurityLevelHiddenToHidden()
    {
        // Check original security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_HIDDEN);
        assertTextPresent(SUB_SL_HIDDEN);

        // Convert
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(SUB_SL_HIDDEN);
        assertTextNotPresent("Security Level");

        clickLink("subtask-to-issue");
        selectOption("issuetype", ISSUE_TYPE_NEWFEATURE);
        submit("Next >>");
        assertTextNotPresent("Security Level");
        assertTextPresent("All fields will be updated automatically.");
        submit("Next >>");
        assertTextNotPresent("Security Level");
        submit("Finish");

        assertTextNotPresent("Security Level");

        // Check new security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_HIDDEN);
        assertTextPresent(SUB_SL_HIDDEN);
    }
    /*
     * Tests that when the issue to convert has security level hidden and the target type has it as required
     * the user is prompted and it is on the confirm screen, and checks the issue has been updated
     */
    public void testSubTaskToIssueConversionSecurityLevelHiddenToRequired()
    {
        // Check original security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_HIDDEN);
        assertTextPresent(SUB_SL_HIDDEN);

        // Convert
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(SUB_SL_HIDDEN);
        assertTextNotPresent("Security Level");

        clickLink("subtask-to-issue");
        selectOption("issuetype", ISSUE_TYPE_IMPROVEMENT);
        submit("Next >>");
        assertTextPresent("Security Level");
        assertTextSequence(new String[] { "Security Level", "Developers" });
        selectOption("security", "Developers");
        submit("Next >>");
        assertTextSequence(new String[] { "Security Level", "None", "Developers" });

        submit("Finish");

        assertTextSequence(new String[] { "Security Level", "Developers" });


        // Check new security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(SUB_SL_HIDDEN);
        assertTextPresent("Permission Violation");
    }

}
