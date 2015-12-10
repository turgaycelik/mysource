package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.changehistory.ChangeHistoryList;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.xml.sax.SAXException;

import static com.atlassian.jira.permission.ProjectPermissions.EDIT_ALL_WORKLOGS;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_OWN_WORKLOGS;

/**
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING, Category.WORKLOGS })
public class TestUpdateWorklog extends FuncTestCase
{
    private static final String ADMIN_COMMENT = "Admin does some work.";
    private static final String HSP_1 = "HSP-1";
    private static final String HSP_2 = "HSP-2";

    public TestUpdateWorklog(String name)
    {
        this.setName(name);
    }

    /*
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

    public void testWorklogDoesNotExist()
    {
        getTester().gotoPage("/secure/UpdateWorklog!default.jspa?worklogId=99999&id=10000");
        assertions.assertNodeHasText("//div[@class='aui-message error']", "Cannot retrieve worklog with id='99999' for update.");
//                getJiraFormAssertions().assertFormErrMsg("Cannot retrieve worklog with id='99999' for update.");

    }

    public void testWorklogNoPermToUpdate()
    {
        administration.permissionSchemes().defaultScheme();
        getTester().clickLink("del_perm_" + EDIT_OWN_WORKLOGS.permissionKey() + "_");
        getTester().submit("Delete");
        getTester().clickLink("del_perm_" + EDIT_ALL_WORKLOGS.permissionKey() + "_");
        getTester().submit("Delete");
        getTester().gotoPage("/secure/UpdateWorklog!default.jspa?worklogId=10000&id=10000");
        text.assertTextPresent("Access Denied");
    }

    public void testMandatoryFields()
    {
        navigation.issue().viewIssue(HSP_1);
        clickWorkLogLink();
        getTester().clickLink("edit_worklog_10000");
        getTester().setFormElement("timeLogged", "");
        getTester().setFormElement("startDate", "");
        getTester().setFormElement("newEstimate", "");
        getTester().checkCheckbox("adjustEstimate", "new");
        getTester().submit();
        text.assertTextPresent("You must indicate the time spent working.");
        text.assertTextPresent("You must specify a date on which the work occurred.");
        text.assertTextPresent("You must supply a valid new estimate.");
    }

    /**
     * Errors should be reported if the "Time Spent" field or the hideable "new estimate" field (if "Set estimated time
     * remaining" radio button has been selected) contains an invalid duration string.
     * <p/>
     * Valid durations strings include "4d 6h", "30m", etc.
     */
    public void testInvalidFormattedDurationFields()
    {
        navigation.issue().viewIssue(HSP_1);
        clickWorkLogLink();
        getTester().clickLink("edit_worklog_10000");
        getTester().setFormElement("timeLogged", "16 Candles");
        getTester().setFormElement("startDate", "18/Jun/07 10:49 AM"); //should be valid
        getTester().setFormElement("newEstimate", "Six Days, Seven Nights");
        getTester().checkCheckbox("adjustEstimate", "new");
        getTester().submit();
        text.assertTextPresent("Invalid time duration entered.");
        text.assertTextPresent("Invalid new estimate entered.");
    }

    /**
     * Errors should be reported if the "Time Spent" field if the time spent is zero.
     */
    public void testInvalidTimeSpentZero()
    {
        navigation.issue().viewIssue(HSP_1);
        clickWorkLogLink();
        getTester().clickLink("edit_worklog_10000");
        getTester().setFormElement("timeLogged", "0");
        getTester().setFormElement("startDate", "18/Jun/07 10:49 AM"); //should be valid
        getTester().submit();
        text.assertTextPresent("Time Spent can not be zero.");
    }

    public void testInvalidStartDateField()
    {
        navigation.issue().viewIssue(HSP_1);
        clickWorkLogLink();
        getTester().clickLink("edit_worklog_10000");
        getTester().setFormElement("timeLogged", "4h");
        getTester().setFormElement("startDate", "The Day After Tomorrow"); //should be valid
        getTester().checkCheckbox("adjustEstimate", "leave");
        getTester().submit();
        text.assertTextPresent("You must specify a date on which the work occurred.");
    }

    public void testUpdateTimeSpentDecreasingAutoAdjust()
    {
        navigation.issue().viewIssue(HSP_1);
        assertTextSequence(new String[] { "3 weeks, 1 day", "6 days" });
        clickWorkLogLink();
        getTester().clickLink("edit_worklog_10002");
        getTester().setFormElement("timeLogged", "1d");
        getTester().submit("Log");
        // Make sure the new estimates and time spent on the issue are correct
        assertTextSequence(new String[] { "3 weeks, 3 days", "4 days" });

        // Make sure the worklog has been updated correctly

        text.assertTextPresent(new IdLocator(tester, "wl-10002-d"), "1 day");
    }

    public void testUpdateTimeSpentIncreasingAutoAdjust()
    {
        navigation.issue().viewIssue(HSP_1);
        assertTextSequence(new String[] { "3 weeks, 1 day", "6 days" });
        clickWorkLogLink();
        getTester().clickLink("edit_worklog_10000");
        getTester().setFormElement("timeLogged", "8d");
        getTester().submit("Log");
        // Make sure the new estimates and time spent on the issue are correct
        assertTextSequence(new String[] { "2 weeks, 1 day", "1 week, 6 days" });

        // Make sure the worklog has been updated correctly
        text.assertTextPresent(new IdLocator(tester, "wl-10000-d"), "1 week, 1 day");
    }

    // Need to test that if we increase the amount of time spent on a worklog that the remaining estimate does not
    // auto adjust to a negative number
    public void testUpdateTimeSpentStopsAtZeroAutoAdjust()
    {
        navigation.issue().viewIssue(HSP_2);
        assertTextSequence(new String[] { "3 days", "2 weeks" });
        clickWorkLogLink();
        getTester().clickLink("edit_worklog_10010");
        getTester().setFormElement("timeLogged", "3w");
        getTester().submit("Log");
        // Make sure the new estimates and time spent on the issue are correct
        assertTextSequence(new String[] { "0 minutes", "3 weeks" });

        // Make sure the worklog has been updated correctly
        text.assertTextPresent(new IdLocator(tester, "wl-10010-d"), "3 weeks");
    }

    public void testUpdateStartDateOrderChanges()
    {
        navigation.issue().viewIssue(HSP_1);
        clickWorkLogLink();
        // Make sure the worklogs are initially in the correct order
        assertTextSequence(new String[] { "worklog_details_10000", "worklog_details_10001", "worklog_details_10002" });
        getTester().clickLink("edit_worklog_10002");
        getTester().setFormElement("startDate", "1/Jun/06 12:00 PM");
        getTester().submit("Log");

        // Make sure the worklogs have been reordered
        assertTextSequence(new String[] { "worklog_details_10002", "worklog_details_10000", "worklog_details_10001" });

        // Make sure the worklog has been updated correctly
        text.assertTextPresent("1/Jun/06 12:00 PM");
    }

    public void testUpdateTimeSpentIncreasingNoRemainingEstimateChange()
    {
        navigation.issue().viewIssue(HSP_1);
        assertTextSequence(new String[] { "3 weeks, 1 day", "6 days" });
        clickWorkLogLink();
        getTester().clickLink("edit_worklog_10000");
        getTester().setFormElement("timeLogged", "8d");
        getTester().checkCheckbox("adjustEstimate", "leave");
        getTester().submit("Log");

        // Make sure the new estimates and time spent on the issue are correct
        assertTextSequence(new String[] { "3 weeks, 1 day", "1 week, 6 days" });

        // Make sure the worklog has been updated correctly
        text.assertTextPresent(new IdLocator(tester, "wl-10000-d"), "1 week, 1 day");
    }

    public void testUpdateTimeSpentIncreasingClobberRemainingEstimate()
    {
        navigation.issue().viewIssue(HSP_1);
        assertTextSequence(new String[] { "3 weeks, 1 day", "6 days" });
        clickWorkLogLink();
        getTester().clickLink("edit_worklog_10000");
        getTester().setFormElement("timeLogged", "8d");
        getTester().checkCheckbox("adjustEstimate", "new");
        getTester().setFormElement("newEstimate", "8w");
        getTester().submit("Log");

        // Make sure the new estimates and time spent on the issue are correct
        assertTextSequence(new String[] { "8 weeks", "1 week, 6 days" });

        // Make sure the worklog has been updated correctly
        text.assertTextPresent(new IdLocator(tester, "wl-10000-d"), "1 week, 1 day");
    }

    public void testUpdateComment()
    {
        navigation.issue().viewIssue(HSP_1);
        assertTextSequence(new String[] { "3 weeks, 1 day", "6 days" });
        clickWorkLogLink();
        getTester().clickLink("edit_worklog_10000");
        getTester().setFormElement("comment", "This is an updated worklog comment");
        getTester().submit("Log");

        // Make sure the worklog has been updated correctly
        text.assertTextPresent(new IdLocator(tester, "wl-10000-c"), "This is an updated worklog comment");
    }

    public void testUpdateTimeSpentAutoAdjustCreatesChangeHistory() throws SAXException
    {
        navigation.issue().viewIssue(HSP_1);
        assertTextSequence(new String[] { "3 weeks, 1 day", "6 days" });
        clickWorkLogLink();
        getTester().clickLink("edit_worklog_10000");
        getTester().setFormElement("timeLogged", "8d");
        getTester().submit("Log");
        // Make sure the new estimates and time spent on the issue are correct
        assertTextSequence(new String[] { "2 weeks, 1 day", "1 week, 6 days" });
        getTester().clickLinkWithText("History");

        ChangeHistoryList expectedList = new ChangeHistoryList();
        expectedList.addChangeSet(ADMIN_FULLNAME)
                .add("Time Spent", "6 days [ 518400 ]", "1 week, 6 days [ 1123200 ]")
                .add("Remaining Estimate", "3 weeks, 1 day [ 1900800 ]", "2 weeks, 1 day [ 1296000 ]")
                .add("Worklog Id", "10000 [ 10000 ]", "")
                ;

        final ChangeHistoryList list = parse.issue().parseChangeHistory();
        list.assertContainsSomeOf(expectedList);
    }

    public void testUpdateWorklogSecurityLevels()
    {
        //check mel and fred can see admin's worklog
        assertTrue(canUserSeeComment("mel", ADMIN_COMMENT));
        assertTrue(canUserSeeComment(FRED_USERNAME, ADMIN_COMMENT));

        //set worklog visible to jira-developers - check fred can see and mel can't
        navigation.issue().viewIssue(HSP_1);
        clickWorkLogLink();
        getTester().clickLink("edit_worklog_10000");
        getTester().setFormElement("timeLogged", "1d");
        getTester().selectOption("commentLevel", "jira-developers");
        getTester().submit("Log");

        assertTrue(canUserSeeComment(FRED_USERNAME, ADMIN_COMMENT));
        assertFalse(canUserSeeComment("mel", ADMIN_COMMENT));

        navigation.issue().viewIssue(HSP_1);
        //set worklog visible to Specialist role - check mel can see and fred can't
        getTester().clickLink("edit_worklog_10000");
        getTester().setFormElement("timeLogged", "1d");
        getTester().selectOption("commentLevel", "Specialist");
        getTester().submit("Log");

        assertFalse(canUserSeeComment(FRED_USERNAME, ADMIN_COMMENT));
        assertTrue(canUserSeeComment("mel", ADMIN_COMMENT));

        navigation.issue().viewIssue(HSP_1);
        //set worklog visible to jira-user - check everyone can see again
        getTester().clickLink("edit_worklog_10000");
        getTester().setFormElement("timeLogged", "1d");
        getTester().selectOption("commentLevel", "jira-users");
        getTester().submit("Log");

        assertTrue(canUserSeeComment("mel", ADMIN_COMMENT));
        assertTrue(canUserSeeComment(FRED_USERNAME, ADMIN_COMMENT));
    }

    /**
     * It is not possible to "manually adjust" the estimated time remaining during an update operation. This only makes
     * sense for create and delete. This test ensures that this radio option is not shown to the user.
     */
    public void testManuallyAdjustEstimateOptionNotAvailable()
    {
        navigation.issue().viewIssue(HSP_1);
        getTester().clickLinkWithText("Work Log");
        // Edit a worklog
        getTester().clickLink("edit_worklog_10001");
        // Assert that the radio buttons are set up as we expect.
        text.assertTextPresent("id=\"log-work-adjust-estimate-auto\"");
        // Assert that the "manual adjust" option is missing.
        text.assertTextNotPresent("id=\"log-work-adjust-estimate-manual\"");
    }

    private boolean canUserSeeComment(String user, String comment)
    {
        try
        {
            navigation.logout();
            navigation.login(user, user);
            log("Checking if " + user + " can see comment '" + comment + "'");
            navigation.issue().viewIssue(HSP_1);
            clickWorkLogLink();
            return getTester().getDialog().isTextInResponse(comment);
        }
        finally
        {
            navigation.login(ADMIN_USERNAME);
        }
    }

    private void clickWorkLogLink()
    {
        if (page.isLinkPresentWithExactText("Work Log"))
        {
            getTester().clickLinkWithText("Work Log");
        }
    }

    private void assertTextSequence(final String[] strings)
    {
        oldway_consider_porting.assertTextSequence(strings);
    }


}
