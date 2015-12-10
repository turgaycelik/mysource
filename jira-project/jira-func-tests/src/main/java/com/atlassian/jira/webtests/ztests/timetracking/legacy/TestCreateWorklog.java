package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.GeneralConfiguration;
import com.atlassian.jira.functest.framework.changehistory.ChangeHistoryList;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;

import static com.atlassian.jira.permission.ProjectPermissions.WORK_ON_ISSUES;

/**
 * Functional tests for log work
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING, Category.WORKLOGS })
public class TestCreateWorklog extends FuncTestCase
{
    private static final String HSP_1 = "HSP-1";

    private static final String WORK_LOG_COMMENT_1 = "This is a comment generated for a first work log.";
    private static final String WORK_LOG_COMMENT_2 = "This is a comment generated for a second work log.";
    private static final String ROLE_DEVELOPERS = "Developers";
    private static final String GROUP_ADMINISTRATORS = "jira-administrators";
    private String timeFormat;

    public TestCreateWorklog(String name)
    {
        this.setName(name);
    }

    @Override
    public void setUpTest()
    {
        administration.restoreData("TestLogWork.xml");
        administration.generalConfiguration().setCommentVisibility(GeneralConfiguration.CommentVisibility.GROUPS_PROJECT_ROLES);        
    }

    public void testWorklogNoPermToCreate()
    {
        administration.permissionSchemes().defaultScheme();        
        tester.clickLink("del_perm_" + WORK_ON_ISSUES.permissionKey() + "_jira-developers");
        tester.submit("Delete");
        tester.gotoPage("/secure/CreateWorklog!default.jspa?id=10000");
        tester.assertTextPresent("It seems that you have tried to perform an operation which you are not permitted to perform");
    }


    public void testLogWorkNoteCorrect()
    {
        tester.gotoPage("/secure/ShowTimeTrackingHelp.jspa?decorator=popup#LogWork");
        text.assertTextPresent("Your current conversion rates are 1w = 7d and 1d = 24h.");

        // turn it off so we can re-enable with hours as the default
        administration.timeTracking().disable();

        // check that by default we'll end up with Minutes to preserve old behaviour
        navigation.gotoAdmin();
        tester.clickLink("timetracking");
        tester.setFormElement("hoursPerDay", "6");
        tester.setFormElement("daysPerWeek", "5");
        tester.submit("Activate");

        tester.gotoPage("/secure/ShowTimeTrackingHelp.jspa?decorator=popup#LogWork");
        text.assertTextPresent("Your current conversion rates are 1w = 5d and 1d = 6h.");
    }

    /**
     * Errors should be reported on the "Time Spent" field, "Start Date" field and hideable "new estimate" field if
     * "Set estimated time remaining" radio button has been selected, if they are empty.
     */
    public void testMandatoryFields()
    {
        navigation.issue().viewIssue(HSP_1);
        tester.clickLink("log-work");
        tester.setFormElement("timeLogged", "");
        tester.setFormElement("startDate", "");
        tester.setFormElement("newEstimate", "");
        tester.checkCheckbox("adjustEstimate", "new");
        tester.submit();
        text.assertTextPresent("You must indicate the time spent working.");
        text.assertTextPresent("You must specify a date on which the work occurred.");
        text.assertTextPresent("You must supply a valid new estimate.")  ;
    }

    /**
     * Errors should be reported if the "Time Spent" field or the hideable "new estimate" field (if "Set estimated time
     * remaining" radio button has been selected) contains an invalid duration string.
     *
     * Valid durations strings include "4d 6h", "30m", etc.
     */
    public void testInvalidFormattedDurationFields()
    {
        navigation.issue().viewIssue(HSP_1);
        getTester().clickLink("log-work");
        getTester().setFormElement("timeLogged", "16 Candles");
        getTester().setFormElement("startDate", "18/Jun/07 10:49 AM"); //should be valid
        getTester().setFormElement("newEstimate", "Six Days, Seven Nights");
        getTester().checkCheckbox("adjustEstimate", "new");
        getTester().submit();
        text.assertTextPresent("Invalid time duration entered.");
        text.assertTextPresent("Invalid new estimate entered.");
    }

    public void testNegativeDuration()
    {
        navigation.issue().logWork(HSP_1,"-2h");
        text.assertTextPresent("Invalid time duration entered.");
    }

    public void testBadFractionDuration()
    {
        final String[] badDurations = new String[] {
//                "2.h", // decimal in wrong place
                "5.3756h", // can't be represented as "minutes" without losing accuracy
//                "0.5m", // can't go below 1 minute
        };
        for (String badDuration : badDurations)
        {
            navigation.issue().logWork(HSP_1, badDuration);
            text.assertTextPresent("Invalid time duration entered.");
        }
    }

    public void testGoodFractionDuration() throws Exception
    {
        navigation.issue().logWork(HSP_1, "2.5h");

        assertTextSequence(new String[] {"Time Spent", "2 hours, 30 minutes"});

        navigation.issue().logWork(HSP_1, "2.5h 30m");
        assertTextSequence(new String[] { "Time Spent", "5 hours, 30 minutes" });

        navigation.issue().logWork(HSP_1, "1.5d");
        assertTextSequence(new String[] { "Time Spent", "1 day, 17 hours, 30 minutes"});
    }


    /**
     * Errors should be reported if the "Time Spent" field if the time spent is zero.
     */
    public void testInvalidTimeSpentZero()
    {
        navigation.issue().viewIssue(HSP_1);
        getTester().clickLink("log-work");
        getTester().setFormElement("timeLogged", "0");
        getTester().setFormElement("startDate", "18/Jun/07 10:49 AM"); //should be valid
        getTester().submit();
        text.assertTextPresent("Time Spent can not be zero.");
    }

    public void testInvalidStartDateField()
    {
        navigation.issue().viewIssue(HSP_1);
        getTester().clickLink("log-work");
        getTester().setFormElement("timeLogged", "4h");
        getTester().setFormElement("startDate", "The Day After Tomorrow"); //should be valid
        getTester().checkCheckbox("adjustEstimate", "leave");
        getTester().submit();
        text.assertTextPresent("You must specify a date on which the work occurred.");
    }

    public void testAutoAdjustEstimate()
    {
        navigation.issue().viewIssue(HSP_1);
        getTester().clickLink("edit-issue");
        getTester().setFormElement("timetracking", "4d");
        getTester().submit("Update");
        getTester().clickLink("log-work");
        getTester().setFormElement("timeLogged", "4h 30m");
        getTester().submit("Log");
        assertTextSequence(new String[]{"Original Estimate", "4 days", "Remaining Estimate", "3 days, 19 hours, 30 minutes", "Time Spent", "4 hours, 30 minutes"});
    }

    public void testNewEstimate()
    {
        navigation.issue().viewIssue(HSP_1);
        getTester().clickLink("edit-issue");
        getTester().setFormElement("timetracking", "4d");
        getTester().submit("Update");
        getTester().clickLink("log-work");
        getTester().setFormElement("timeLogged", "4h 30m");
        getTester().checkCheckbox("adjustEstimate", "new");
        getTester().setFormElement("newEstimate", "1d 5h");
        getTester().submit("Log");
        assertTextSequence(new String[]{"Original Estimate", "4 days", "Remaining Estimate", "1 day, 5 hours", "Time Spent", "4 hours, 30 minutes"});
    }

    public void testLeaveExistingEstimate()
    {
        navigation.issue().viewIssue(HSP_1);
        getTester().clickLink("edit-issue");
        getTester().setFormElement("timetracking", "4d");
        getTester().submit("Update");
        getTester().clickLink("log-work");
        getTester().setFormElement("timeLogged", "4h 30m");
        getTester().checkCheckbox("adjustEstimate", "leave");
        getTester().submit("Log");
        assertTextSequence(new String[]{"Original Estimate", "4 days", "Remaining Estimate", "4 days", "Time Spent", "4 hours, 30 minutes"});
    }

    /* --------- Test Group/Role Visibility --------- */

    public void testLogWorkVisibleToAll()
    {
        reconfigureTimetracking(oldway_consider_porting.FORMAT_PRETTY);
        execLogWorkVisibleToAll();
    }

    public void testLogWorkVisibleToAllDaysTimeFormat()
    {
        reconfigureTimetracking(oldway_consider_porting.FORMAT_DAYS);
        execLogWorkVisibleToAll();
    }

    public void testLogWorkVisibleToAllHoursTimeFormat()
    {
        reconfigureTimetracking(oldway_consider_porting.FORMAT_HOURS);
        execLogWorkVisibleToAll();
    }

    private void execLogWorkVisibleToAll()
    {
        navigation.issue().viewIssue(HSP_1);
        oldway_consider_porting.logWorkOnIssueWithComment(HSP_1, "2d", WORK_LOG_COMMENT_1);
        getTester().clickLinkWithText("Work Log");
        text.assertTextPresent(WORK_LOG_COMMENT_1);
        if (oldway_consider_porting.FORMAT_PRETTY.equals(timeFormat))
        {
            text.assertTextPresent("2 days");
        }
        else if (oldway_consider_porting.FORMAT_DAYS.equals(timeFormat))
        {
            oldway_consider_porting.assertTextPresentAfterText("2d","Time Spent");
        }
        else if (oldway_consider_porting.FORMAT_HOURS.equals(timeFormat))
        {
            oldway_consider_porting.assertTextPresentAfterText("48h", "Time Spent");
        }
    }

    public void testLogWorkVisibleToRole()
    {
        reconfigureTimetracking(oldway_consider_porting.FORMAT_PRETTY);
        execLogWorkVisibleToRole();
    }

    public void testLogWorkVisibleToRoleDaysTimeFormat()
    {
        reconfigureTimetracking(oldway_consider_porting.FORMAT_DAYS);
        execLogWorkVisibleToRole();
    }

    public void testLogWorkVisibleToRoleHoursTimeFormat()
    {
        reconfigureTimetracking(oldway_consider_porting.FORMAT_HOURS);
        execLogWorkVisibleToRole();
    }

    public void execLogWorkVisibleToRole()
    {
        navigation.issue().viewIssue(HSP_1);

        // set the role level
        getTester().clickLink("log-work");
        getTester().setFormElement("timeLogged", "2d");
        getTester().checkCheckbox("adjustEstimate", "new");
        getTester().setFormElement("newEstimate", "2w");
        getTester().setFormElement("comment", WORK_LOG_COMMENT_1);
        getTester().selectOption("commentLevel", ROLE_DEVELOPERS);
        getTester().submit();

        getTester().clickLink("log-work");
        getTester().setFormElement("timeLogged", "3d");
        getTester().checkCheckbox("adjustEstimate", "new");
        getTester().setFormElement("newEstimate", "2w");
        getTester().setFormElement("comment", WORK_LOG_COMMENT_2);
        getTester().selectOption("commentLevel", ROLE_DEVELOPERS);
        getTester().submit();

        // assert we can see work log item above as Admin
        getTester().clickLinkWithText("Work Log");
        text.assertTextPresent(WORK_LOG_COMMENT_1);
        text.assertTextPresent(WORK_LOG_COMMENT_2);
        if (oldway_consider_porting.FORMAT_PRETTY.equals(timeFormat))
        {
            text.assertTextPresent("2 days"); // first worklog
            text.assertTextPresent("3 days"); // second worklog
            text.assertTextPresent("5 days"); // total spent
        }
        else if (oldway_consider_porting.FORMAT_DAYS.equals(timeFormat))
        {
            oldway_consider_porting.assertTextPresentAfterText("2d", "Time Spent"); // first worklog
            oldway_consider_porting.assertTextPresentAfterText("3d", "Time Spent"); // second worklog
            oldway_consider_porting.assertTextPresentAfterText("5d", "Time Spent"); // total spent
        }
        else if (oldway_consider_porting.FORMAT_HOURS.equals(timeFormat))
        {
            oldway_consider_porting.assertTextPresentAfterText("48h", "Time Spent"); // first worklog
            oldway_consider_porting.assertTextPresentAfterText("72h", "Time Spent"); // second worklog
            oldway_consider_porting.assertTextPresentAfterText("120h", "Time Spent"); // total spent
        }

        // assert we cannot see work log item above as Fred
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        navigation.issue().viewIssue(HSP_1);
        text.assertTextNotPresent(WORK_LOG_COMMENT_1);
        text.assertTextNotPresent(WORK_LOG_COMMENT_2);
        if (oldway_consider_porting.FORMAT_PRETTY.equals(timeFormat))
        {
            text.assertTextNotPresent("2 days"); // first worklog
            text.assertTextNotPresent("3 days"); // second worklog
            text.assertTextPresent("5 days"); // total worklog
        }
        else if (oldway_consider_porting.FORMAT_DAYS.equals(timeFormat))
        {
            text.assertTextNotPresent(" 2d "); // first worklog
            text.assertTextNotPresent(" 3d "); // second worklog
            oldway_consider_porting.assertTextPresentAfterText("5d", "Time Spent"); // total worklog
        }
        else if (oldway_consider_porting.FORMAT_HOURS.equals(timeFormat))
        {
            text.assertTextNotPresent(" 48h "); // first worklog
            text.assertTextNotPresent(" 72h "); // second worklog
            oldway_consider_porting.assertTextPresentAfterText("120h", "Time Spent"); // total worklog
        }
    }

    public void testLogWorkVisibleToGroup()
    {
        reconfigureTimetracking(oldway_consider_porting.FORMAT_PRETTY);
        execLogWorkVisibleToGroup();
    }

    public void testLogWorkVisibleToGroupDaysTimeFormat()
    {
        reconfigureTimetracking(oldway_consider_porting.FORMAT_DAYS);
        execLogWorkVisibleToGroup();
    }

    public void testLogWorkVisibleToGroupHoursTimeFormat()
    {
        reconfigureTimetracking(oldway_consider_porting.FORMAT_HOURS);
        execLogWorkVisibleToGroup();
    }

    public void testLogWorkDateIsStartDate()
    {
        // Log work on a specific date and make sure it comes before other work
        navigation.issue().viewIssue(HSP_1);
        getTester().clickLink("log-work");
        getTester().setFormElement("timeLogged", "2d");
        getTester().setFormElement("startDate", "1/Jun/06 12:00 PM");
        getTester().submit();
        getTester().clickLinkWithText("Work Log");
        text.assertTextPresent("01/Jun/06 12:00 PM");

        // Add another that is older and make sure it comes first
        getTester().clickLink("log-work");
        getTester().setFormElement("timeLogged", "2d");
        getTester().setFormElement("startDate", "1/Jun/05 12:00 PM");
        getTester().submit();
        oldway_consider_porting.assertTextPresentBeforeText("1/Jun/05 12:00 PM", "01/Jun/06 12:00 PM");
    }

    public void execLogWorkVisibleToGroup()
    {
        navigation.issue().viewIssue(HSP_1);

        // set the role level
        getTester().clickLink("log-work");
        getTester().setFormElement("timeLogged", "2d");
        getTester().checkCheckbox("adjustEstimate", "new");
        getTester().setFormElement("newEstimate", "2w");
        getTester().setFormElement("comment", WORK_LOG_COMMENT_1);
        getTester().selectOption("commentLevel", GROUP_ADMINISTRATORS);
        getTester().submit();

        getTester().clickLink("log-work");
        getTester().setFormElement("timeLogged", "3d");
        getTester().checkCheckbox("adjustEstimate", "new");
        getTester().setFormElement("newEstimate", "2w");
        getTester().setFormElement("comment", WORK_LOG_COMMENT_2);
        getTester().selectOption("commentLevel", GROUP_ADMINISTRATORS);
        getTester().submit();

        // assert we can see work log item above as Admin
        getTester().clickLinkWithText("Work Log");
        getTester().assertTextPresent(WORK_LOG_COMMENT_1);
        getTester().assertTextPresent(WORK_LOG_COMMENT_2);
        if (oldway_consider_porting.FORMAT_PRETTY.equals(timeFormat))
        {
            text.assertTextPresent("2 days"); // first worklog
            text.assertTextPresent("3 days"); // second worklog
            text.assertTextPresent("5 days"); // total spent
        }
        else if (oldway_consider_porting.FORMAT_DAYS.equals(timeFormat))
        {
            oldway_consider_porting.assertTextPresentAfterText("2d", "Time Spent"); // first worklog
            oldway_consider_porting.assertTextPresentAfterText("3d", "Time Spent"); // second worklog
            oldway_consider_porting.assertTextPresentAfterText("5d", "Time Spent"); // total spent
        }
        else if (oldway_consider_porting.FORMAT_HOURS.equals(timeFormat))
        {
            oldway_consider_porting.assertTextPresentAfterText("48h", "Time Spent"); // first worklog
            oldway_consider_porting.assertTextPresentAfterText("72h", "Time Spent"); // second worklog
            oldway_consider_porting.assertTextPresentAfterText("120h", "Time Spent"); // total spent
        }

        // assert we cannot see work log item above as Fred
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        navigation.issue().viewIssue(HSP_1);
        text.assertTextNotPresent(WORK_LOG_COMMENT_1);
        text.assertTextNotPresent(WORK_LOG_COMMENT_2);
        if (oldway_consider_porting.FORMAT_PRETTY.equals(timeFormat))
        {
            text.assertTextNotPresent("2 days"); // first worklog
            text.assertTextNotPresent("3 days"); // second worklog
            text.assertTextPresent("5 days"); // total worklog
        }
        else if (oldway_consider_porting.FORMAT_DAYS.equals(timeFormat))
        {
            text.assertTextNotPresent(" 2d "); // first worklog
            text.assertTextNotPresent(" 3d "); // second worklog
            oldway_consider_porting.assertTextPresentAfterText("5d", "Time Spent"); // total worklog
        }
        else if (oldway_consider_porting.FORMAT_HOURS.equals(timeFormat))
        {
            text.assertTextNotPresent(" 48h "); // first worklog
            text.assertTextNotPresent(" 72h "); // second worklog
            oldway_consider_porting.assertTextPresentAfterText("120h", "Time Spent"); // total worklog
        }
    }

    public void reconfigureTimetracking(String format)
    {
        oldway_consider_porting.reconfigureTimetracking(format);
        timeFormat = format;
    }

    /* --------- End Test Group/Role Visibility --------- */

    public void testChangeHistory() throws Exception
    {
        boolean isOracle = new EnvironmentUtils(tester, getEnvironmentData(), navigation).isOracle();

        navigation.issue().viewIssue(HSP_1);
        getTester().clickLink("edit-issue");
        getTester().setFormElement("timetracking", "4d");
        getTester().submit("Update");

        if(isOracle)
        {
            //TODO: Remove this sleep hack once http://jira.atlassian.com/browse/JRA-20274 has been resolved
            Thread.sleep(2000);
        }

        getTester().clickLink("log-work");
        getTester().setFormElement("timeLogged", "4h 30m");
        getTester().checkCheckbox("adjustEstimate", "new");
        getTester().setFormElement("newEstimate", "1d 5h");
        getTester().submit("Log");
        getTester().clickLinkWithText("History");

       ChangeHistoryList expectedList = new ChangeHistoryList();
       expectedList.addChangeSet(ADMIN_FULLNAME)
           .add("Original Estimate", "4 days [ 345600 ]")
           .add("Remaining Estimate", "4 days [ 345600 ]");
       expectedList.addChangeSet(ADMIN_FULLNAME)
           .add("Time Spent", "4 hours, 30 minutes [ 16200 ]")
           .add("Remaining Estimate", "1 day, 5 hours [ 104400 ]");

        final ChangeHistoryList list = parse.issue().parseChangeHistory();
        list.assertContainsSomeOf(expectedList);
    }


    //------------------------------------------------------------------------------------------------------------------
    //  Func Tests for manually reducing the estimate.
    //------------------------------------------------------------------------------------------------------------------

    public void testManuallyReduceEstimate()
    {
        navigation.issue().viewIssue(HSP_1);
        // Set Estimated Remaining to 2d
        tester.clickLink("edit-issue");
        tester.setFormElement("timetracking", "2d");
        tester.submit("Update");

        // Click Link 'Log work' (id='log-work').
        tester.clickLink("log-work");
        tester.setFormElement("timeLogged", "12h");
        tester.checkCheckbox("adjustEstimate", "manual");
        tester.setWorkingForm("log-work");
        tester.submit();
        // We set manual adjustment, but left the texfield blank - we should get an error.
        // Click Link 'Log work' (id='log-work').
        text.assertTextPresent("You must supply a valid amount of time to adjust the estimate by.");

        // Now try an invalid value
        tester.setFormElement("adjustmentAmount", "1cow");
        tester.setWorkingForm("log-work");
        tester.submit();
        text.assertTextPresent("Invalid time entered for adjusting the estimate.");

        // Finally give a valid value
        tester.setFormElement("adjustmentAmount", "6h");
        tester.setWorkingForm("log-work");
        tester.submit();
        // Assert the table 'tt_single_table_info'
        text.assertTextSequence(new IdLocator(tester, "tt_single_table_info"), "Estimated:","2d",
                "Remaining:", "1d 18h", "Logged:", "12h");
    }

    private void assertTextSequence(final String[] strings)
    {
        oldway_consider_porting.assertTextSequence(strings);
    }

    
}