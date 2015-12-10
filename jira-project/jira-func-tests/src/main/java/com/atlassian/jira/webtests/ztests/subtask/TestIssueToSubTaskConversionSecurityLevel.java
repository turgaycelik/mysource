package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.SECURITY, Category.SUB_TASKS })
public class TestIssueToSubTaskConversionSecurityLevel extends JIRAWebTest
{
    private static final String NO_SECURITY_ISSUE = "HSP-1";
    private static final String DEV_SECURITY_ISSUE = "HSP-2";
    private static final String ADMIN_SECURITY_ISSUE = "HSP-3";
    private static final String DEV_SECURITY_ISSUE_2 = "HSP-4";

    public TestIssueToSubTaskConversionSecurityLevel(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestIssueToSubTaskConversionSecurityLevel.xml");
    }

    /*
     * Tests that when the issue to convert has no security level and the parent issue does
     * the subtask takes the parent security level
     */
    public void testIssueToSubTaskConversionSecurityLevelIntroduced()
    {
        // Check original security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(NO_SECURITY_ISSUE);
        assertTextPresent(NO_SECURITY_ISSUE);

        // Convert to subtask (and change security level)
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(NO_SECURITY_ISSUE);
        assertTextNotPresent("Security Level:");

        clickLink("issue-to-subtask");
        setFormElement("parentIssueKey", DEV_SECURITY_ISSUE);
        selectOption("issuetype", "Sub-task");
        submit("Next >>");
        assertTextPresent("All fields will be updated automatically.");
        submit("Next >>");
        assertTextPresentBeforeText("Security", "Developers");
        submit("Finish");

        assertTextPresentBeforeText("Security Level:", "Developers");

        // Check new security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(NO_SECURITY_ISSUE);
        assertTextPresent("Permission Violation");
    }

    /*
     * Tests that when the issue to convert has a security level and the parent issue doesn't
     * the subtask takes the parent security level ie. none
     */
    public void testIssueToSubTaskConversionSecurityLevelRemoved()
    {
        // Check original security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(DEV_SECURITY_ISSUE);
        assertTextPresent("Permission Violation");

        // Convert to subtask (and change security level)
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(DEV_SECURITY_ISSUE);
        assertTextPresentBeforeText("Security Level:", "Developers");

        clickLink("issue-to-subtask");
        setFormElement("parentIssueKey", NO_SECURITY_ISSUE);
        selectOption("issuetype", "Sub-task");
        submit("Next >>");
        assertTextPresent("All fields will be updated automatically.");
        submit("Next >>");
        assertTextPresentBeforeText("Security", "Developers");
        submit("Finish");

        assertTextNotPresent("Security Level:");

        // Check new security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(DEV_SECURITY_ISSUE);
        assertTextPresent(DEV_SECURITY_ISSUE);
    }

    /*
     * Tests that when the issue to convert has a security level and the parent issue has
     * a different security level the subtask takes the parent security level
     */
    public void testIssueToSubTaskConversionSecurityLevelChanged()
    {
        // Check original security level
        logout();
        login("jane", "jane");
        gotoIssue(DEV_SECURITY_ISSUE);
        assertTextPresent(DEV_SECURITY_ISSUE);

        // Convert to subtask (and change security level)
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(DEV_SECURITY_ISSUE);
        assertTextPresentBeforeText("Security Level:", "Developers");

        clickLink("issue-to-subtask");
        setFormElement("parentIssueKey", ADMIN_SECURITY_ISSUE);
        selectOption("issuetype", "Sub-task");
        submit("Next >>");
        assertTextPresent("All fields will be updated automatically.");
        submit("Next >>");
        assertTextPresentBeforeText("Security", "Developers");
        assertTextPresentBeforeText("Developers", "Administrators");
        submit("Finish");

        assertTextPresentBeforeText("Security Level:", "Administrators");

        // Check new security level
        logout();
        login("jane", "jane");
        gotoIssue(DEV_SECURITY_ISSUE);
        assertTextPresent("Permission Violation");
    }

    /*
     * Tests that when the issue to convert has a security level and the parent issue has
     * the same security level there is no change and no confirmation
     */
    public void testIssueToSubTaskConversionSecurityLevelSame()
    {
        // Check original security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(DEV_SECURITY_ISSUE);
        assertTextPresent("Permission Violation");

        // Convert to subtask (and change security level)
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(DEV_SECURITY_ISSUE);
        assertTextPresentBeforeText("Security Level:", "Developers");

        clickLink("issue-to-subtask");
        setFormElement("parentIssueKey", DEV_SECURITY_ISSUE_2);
        selectOption("issuetype", "Sub-task");
        submit("Next >>");
        assertTextPresent("All fields will be updated automatically.");
        submit("Next >>");
        assertTextNotPresent("Developers");
        submit("Finish");

        assertTextPresentBeforeText("Security Level:", "Developers");

        // Check new security level
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(DEV_SECURITY_ISSUE);
        assertTextPresent("Permission Violation");
    }
}
