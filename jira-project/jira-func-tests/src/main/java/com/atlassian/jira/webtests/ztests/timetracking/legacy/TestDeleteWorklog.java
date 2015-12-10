package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.changehistory.ChangeHistoryList;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.xml.sax.SAXException;

import static com.atlassian.jira.permission.ProjectPermissions.DELETE_ALL_WORKLOGS;

@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING, Category.WORKLOGS })
public class TestDeleteWorklog extends FuncTestCase
{
    private static final String ADMIN_COMMENT = "Admin does some work.";
    private static final String HSP_1 = "HSP-1";

    public TestDeleteWorklog(String name)
    {
        this.setName(name);
    }

    /*
    NB: *** TestUpdateWorklog.xml IS ALSO USED IN TestUpdateWorklog.java ***

    TestUpdateWorklog.xml has all time tracking permissions granted to "Anyone" and
    a single issue (HSP-1) with an original estimate of 28d

    There are three users:

    admin - member of the jira-administrators,-develpopers and -users groups
    fred - member of the jira-develpopers and -users groups
    mel - member of the Specialist role (for project homosapien) and jira-users group

    Each user has one worklog created with no security level.
     */
    public void setUpTest()
    {        
        administration.restoreData("TestUpdateWorklog.xml");
    }

    public void testMandatoryFields()
    {
        navigation.issue().viewIssue(HSP_1);
        clickWorkLogLink();
        getTester().clickLink("delete_worklog_10000");
        getTester().setFormElement("newEstimate", "");
        getTester().checkCheckbox("adjustEstimate", "new");
        getTester().submit();
        text.assertTextPresent("You must supply a valid new estimate.");
    }

    /**
     * Errors should be reported if the "new estimate" field (if "Set estimated time
     * remaining" radio button has been selected) contains an invalid duration string.
     *
     * Valid durations strings include "4d 6h", "30m", etc.
     */
    public void testInvalidFormattedDurationFields()
    {
        navigation.issue().viewIssue(HSP_1);
        clickWorkLogLink();
        getTester().clickLink("delete_worklog_10000");
        getTester().setFormElement("newEstimate", "Six Days, Seven Nights");
        getTester().checkCheckbox("adjustEstimate", "new");
        getTester().submit();
        text.assertTextPresent("Invalid new estimate entered.");
    }

    public void testInsufficientSecurityLevelCantDelete()
    {
        //set security level so mel can't see the worklog
        navigation.issue().viewIssue(HSP_1);
        getTester().clickLinkWithText("Work Log");
        getTester().clickLink("edit_worklog_10000");
        getTester().selectOption("commentLevel", "jira-administrators");
        getTester().submit("Log");

        //assert mel can't delete it
        navigation.logout();
        navigation.login("mel", "mel");
        getTester().gotoPage("/secure/DeleteWorklog!default.jspa?id=10000&worklogId=10000");
        text.assertTextPresent("Access Denied");
        text.assertTextPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");
    }

    public void testNoPermissionCantDelete()
    {
        //remove mel's permission to delete others worklogs
        administration.permissionSchemes().defaultScheme();
        getTester().clickLink("del_perm_" + DELETE_ALL_WORKLOGS.permissionKey() + "_");
        getTester().submit("Delete");

        //assert mel can't delete it
        navigation.logout();
        navigation.login("mel", "mel");
        getTester().gotoPage("/secure/DeleteWorklog!default.jspa?id=10000&worklogId=10000");
        text.assertTextPresent("Access Denied");
        text.assertTextPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");
    }

    public void testDeleteNonExistantWorklog()
    {
        getTester().gotoPage("/secure/DeleteWorklog!default.jspa?id=10000&worklogId=OMGROFLCOPTER");
        text.assertTextPresent("Delete Worklog");
        assertions.getJiraFormAssertions().assertFormErrMsg("Cannot retrieve worklog with id='null' for update.");
    }

    public void testDeleteChangeHistory() throws SAXException
    {
        navigation.issue().viewIssue(HSP_1);
        getTester().clickLinkWithText("Work Log");
        getTester().clickLink("delete_worklog_10000");
        getTester().submit("Delete");

        assertAdminWorklogDeleted();
        getTester().clickLinkWithText("History");

        ChangeHistoryList expectedList = new ChangeHistoryList();
        expectedList.addChangeSet(ADMIN_FULLNAME)
                .add("Time Spent", "6 days [ 518400 ]", "5 days [ 432000 ]")
                .add("Remaining Estimate", "3 weeks, 1 day [ 1900800 ]", "3 weeks, 2 days [ 1987200 ]")
                .add("Worklog Id", "10000 [ 10000 ]","")
                .add("Worklog Time Spent", "1 day [ 86400 ]","")
                ;

        final ChangeHistoryList list = parse.issue().parseChangeHistory();
        list.assertContainsSomeOf(expectedList);
    }

    public void testDeleteAndAutoAdjustRemainingEstimate()
    {
        navigation.issue().viewIssue(HSP_1);
        assertTextSequence(new String [] {"3 weeks, 1 day", "6 days"});
        clickWorkLogLink();
        getTester().clickLink("delete_worklog_10000");
        getTester().checkCheckbox("adjustEstimate", "auto");
        getTester().submit("Delete");

        // Make sure the new estimates and time spent on the issue are correct
        assertTextSequence(new String [] {"3 weeks", "5 days"});

        // Make sure the worklog has been deleted
        assertAdminWorklogDeleted();
    }

    public void testDeleteAndRetainRemainingEstimate()
    {
        navigation.issue().viewIssue(HSP_1);
        assertTextSequence(new String [] {"3 weeks, 1 day", "6 days"});
        clickWorkLogLink();
        getTester().clickLink("delete_worklog_10000");
        getTester().checkCheckbox("adjustEstimate", "leave");
        getTester().submit("Delete");

        // Make sure the estimate remains the same and the time spent is decreased
        assertTextSequence(new String [] {"3 weeks, 1 day", "5 days"});

        // Make sure the worklog has been deleted
        assertAdminWorklogDeleted();
    }

    public void testDeleteAndSpecifyNewEstimate()
    {
        navigation.issue().viewIssue(HSP_1);
        assertTextSequence(new String [] {"3 weeks, 1 day", "6 days"});
        clickWorkLogLink();
        getTester().clickLink("delete_worklog_10000");
        getTester().checkCheckbox("adjustEstimate", "new");
        getTester().setFormElement("newEstimate", "8w");
        getTester().submit("Delete");

        // Make sure the estimate is updated and the time spent is decreased
        assertTextSequence(new String [] {"8 weeks", "5 days"});

        // Make sure the worklog has been deleted
        assertAdminWorklogDeleted();
    }

    private void assertAdminWorklogDeleted()
    {
        text.assertTextNotPresent(ADMIN_COMMENT);
        text.assertTextNotPresent("19/Jun/07 06:09 PM");
        text.assertTextNotPresent("title=\"Created: 19/Jun/07 06:10 PM\"");
        getTester().assertLinkNotPresent("delete_worklog_10000");
        getTester().assertLinkNotPresent("edit_worklog_10000");
    }


    private void clickWorkLogLink()
    {
        if(page.isLinkPresentWithExactText("Work Log"))
        {
            getTester().clickLinkWithText("Work Log");
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    //  Func Tests for manually increasing the estimate.
    //------------------------------------------------------------------------------------------------------------------

    public void testManuallyIncreaseEstimate()
    {
        navigation.issue().viewIssue(HSP_1);

        // Assert the original state of the Work TimeTable
        text.assertTextSequence(new IdLocator(tester, "tt_single_table_info"), "Estimated:","4w",
                "Remaining:", "3w 1d", "Logged:", "6d");

        // View the work log
        getTester().clickLinkWithText("Work Log");
        // Click Link 'Delete' (id='delete_worklog_10002').
        getTester().clickLink("delete_worklog_10002");
        getTester().checkCheckbox("adjustEstimate", "manual");
        getTester().submit("Delete");
        // We left the adjustment amount blank
        text.assertTextPresent("You must supply a valid amount of time to adjust the estimate by.");

        getTester().setFormElement("adjustmentAmount", "heaps");
        getTester().submit("Delete");
        // This is invalid amount
        text.assertTextPresent("Invalid time entered for adjusting the estimate.");

        getTester().setFormElement("adjustmentAmount", "2d");
        getTester().submit("Delete");
        // Assert the new state of the Work TimeTable
        text.assertTextSequence(new IdLocator(tester, "tt_single_table_info"), "Estimated:","4w",
                "Remaining:", "3w 3d", "Logged:", "3d");
    }

    private void assertTextSequence(final String[] strings)
    {
        oldway_consider_porting.assertTextSequence(strings);
    }


}