package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.Permissions;

/**
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING, Category.WORKLOGS })
public class TestWorkLogTabPanel extends JIRAWebTest
{
    private static final String ADMIN_TIME_WORKED = "4h 30m";
    private static final String ADMIN_TIME_PERFORMED = "18/Jun/07 05:39 PM";
    private static final String ADMIN_COMMENT = "Admin's worklog comment.";
    private static final String ADMIN = ADMIN_USERNAME;
    private static final String FRED_TIME_WORKED = "3d";
    private static final String FRED_TIME_PERFORMED = "20/Jun/07 05:39 PM";
    private static final String FRED_COMMENT = "Fred's worklog comment.";
    private static final String FRED = FRED_USERNAME;

    public TestWorkLogTabPanel(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestLogWork.xml");
        grantPermission(FRED, Permissions.WORK_ISSUE);
        addTestWorklogs();
    }

    public void testViewIssuePermissionOnly()
    {
        gotoHSP1WorklogTab();
        assertWorklogsPresent();

        //no edit/delete links should be present
        assertFalse(isAdminWorklogEditLinkPresent());
        assertFalse(isAdminWorklogDeleteLinkPresent());
        assertFalse(isFredsWorklogEditLinkPresent());
        assertFalse(isFredsWorklogDeleteLinkPresent());
    }

    public void testEditOwnWorklogsPermission()
    {
        grantPermission(ADMIN, Permissions.WORKLOG_EDIT_OWN);

        gotoHSP1WorklogTab();
        assertWorklogsPresent();

        //can edit own
        assertTrue(isAdminWorklogEditLinkPresent());
        assertFalse(isAdminWorklogDeleteLinkPresent());
        assertFalse(isFredsWorklogEditLinkPresent());
        assertFalse(isFredsWorklogDeleteLinkPresent());
    }

    public void testEditAllWorklogsPermission()
    {
        grantPermission(ADMIN, Permissions.WORKLOG_EDIT_ALL);

        gotoHSP1WorklogTab();
        assertWorklogsPresent();

        //can edit all
        assertTrue(isAdminWorklogEditLinkPresent());
        assertFalse(isAdminWorklogDeleteLinkPresent());
        assertTrue(isFredsWorklogEditLinkPresent());
        assertFalse(isFredsWorklogDeleteLinkPresent());
    }

    public void testDeleteOwnWorklogsPermission()
    {
        grantPermission(ADMIN, Permissions.WORKLOG_DELETE_OWN);

        gotoHSP1WorklogTab();
        assertWorklogsPresent();

        //can delete own
        assertFalse(isAdminWorklogEditLinkPresent());
        assertTrue(isAdminWorklogDeleteLinkPresent());
        assertFalse(isFredsWorklogEditLinkPresent());
        assertFalse(isFredsWorklogDeleteLinkPresent());
    }

    public void testDeleteAllWorklogsPermission()
    {
        grantPermission(ADMIN, Permissions.WORKLOG_DELETE_ALL);

        gotoHSP1WorklogTab();
        assertWorklogsPresent();

        //can delete all
        assertFalse(isAdminWorklogEditLinkPresent());
        assertTrue(isAdminWorklogDeleteLinkPresent());
        assertFalse(isFredsWorklogEditLinkPresent());
        assertTrue(isFredsWorklogDeleteLinkPresent());
    }

    public void testFullWorklogPermissions()
    {
        grantPermission(ADMIN, Permissions.WORKLOG_EDIT_ALL);        
        grantPermission(ADMIN, Permissions.WORKLOG_DELETE_ALL);

        gotoHSP1WorklogTab();
        assertWorklogsPresent();

        //can edit/delete all
        assertTrue(isAdminWorklogEditLinkPresent());
        assertTrue(isAdminWorklogDeleteLinkPresent());
        assertTrue(isFredsWorklogEditLinkPresent());
        assertTrue(isFredsWorklogDeleteLinkPresent());
    }

    private void assertWorklogsPresent()
    {
        assertTextSequence(new String[] {
                ADMIN_FULLNAME, "Time Spent:", "4 hours, 30 minutes", "Admin&#39;s worklog comment.",
                FRED_FULLNAME, "Time Spent:", "3 days", "Fred&#39;s worklog comment."
        });
    }

    private void gotoHSP1WorklogTab()
    {
        gotoIssue("HSP-1");
        clickLinkWithText("Work Log");
    }

    private boolean isAdminWorklogEditLinkPresent()
    {
        log("checking if the edit link for Admin's worklog is present");
        return getDialog().isTextInResponse("/secure/UpdateWorklog!default.jspa?id=10000&worklogId=10000");
    }

    private boolean isAdminWorklogDeleteLinkPresent()
    {
        log("checking if the delete link for Admin's worklog is present");
        return getDialog().isTextInResponse("/secure/DeleteWorklog!default.jspa?id=10000&worklogId=10000");
    }

    private boolean isFredsWorklogEditLinkPresent()
    {
        log("checking if the edit link for Fred's worklog is present");
        return getDialog().isTextInResponse("/secure/UpdateWorklog!default.jspa?id=10000&worklogId=10001");
    }

    private boolean isFredsWorklogDeleteLinkPresent()
    {
        log("checking if the edit link for Fred's worklog is present");
        return getDialog().isTextInResponse("/secure/DeleteWorklog!default.jspa?id=10000&worklogId=10001");
    }

    private void addTestWorklogs()
    {
        //assumed preconditions: currently logged in as admin, fred has "log work" permission
        log("Adding worklog as admin");

        gotoIssue("HSP-1");
        clickLink("log-work");
        setFormElement("timeLogged", ADMIN_TIME_WORKED);
        setFormElement("startDate", ADMIN_TIME_PERFORMED);
        getDialog().setFormParameter("comment", ADMIN_COMMENT);
        submit("Log");
        logout();

        log("Adding worklog as fred");
        login(FRED, FRED);
        gotoIssue("HSP-1");
        clickLink("log-work");
        setFormElement("timeLogged", FRED_TIME_WORKED);
        setFormElement("startDate", FRED_TIME_PERFORMED);
        getDialog().setFormParameter("comment", FRED_COMMENT);
        submit("Log");
        logout();

        log("Logging back in as admin");
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }


    private void grantPermission(String username, int permission)
    {
        getNavigation().gotoAdmin();
        grantPermissionToUserInEnterprise(permission, username);
    }
}
