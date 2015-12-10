package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestShowConstantsHelp extends JIRAWebTest
{
    public TestShowConstantsHelp(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestShowConstantsHelp.xml");
    }

    public void testShowConstantsHelpHasPermission()
    {
        gotoPage("secure/ShowConstantsHelp.jspa");
        assertTextPresent("JIRA can be used to track many different types of issues");
    }

    public void testShowConstantsHelpDoesNotHavePermission()
    {
        restoreBlankInstance();
        logout();
        gotoPage("secure/ShowConstantsHelp.jspa");
        assertTextPresent("You are not logged in");
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testShowConstantsHelpSecurityLevels()
    {
        createIssueStep1();
        gotoPage("secure/ShowConstantsHelp.jspa");
        assertTextPresent("Security Levels");
        assertTextPresentBeforeText("Security Level 1", "Only developers are allowed to view this issue");
        assertTextPresentBeforeText("Security Level 2", "All users are allowed to view this issue");
    }

    public void testShowConstantsHelpSecurityLevelsSecurity()
    {
        logout();
        login(FRED_USERNAME);

        createIssueStep1();
        gotoPage("secure/ShowConstantsHelp.jspa");
        assertTextPresent("Security Levels");
        assertTextPresentBeforeText("Security Level 2", "All users are allowed to view this issue");
        assertTextNotPresent("Security Level 1");
        assertTextNotPresent("Only developers are allowed to view this issue");
    }
}
