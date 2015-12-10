package com.atlassian.jira.webtests.ztests.timetracking.modern;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.changehistory.ChangeHistoryList;
import com.atlassian.jira.functest.framework.locator.IdLocator;

import static com.atlassian.jira.permission.ProjectPermissions.WORK_ON_ISSUES;

/**
 * Definition of functional tests for the "log work" system field. The field can be placed in various different contexts
 * which have different business logic. Implementors of this class will define a {@link LogWorkRunner} which will allow
 * the tests to be exercised in a specific context.
 *
 * @since v4.2
 */
public abstract class AbstractTestCreateWorklogAsField extends FuncTestCase
{
    protected static final String CHANGEHISTORY_10001 = "changehistory_10001";
    protected static final String HSP_1 = "HSP-1";
    protected static final String HSP_2 = "HSP-2";

    private static final String WORK_LOG_COMMENT_1 = "This is a comment generated for a first work log.";
    private static final String ROLE_DEVELOPERS = "Developers";
    private static final String GROUP_ADMINISTRATORS = "jira-administrators";

    private static final String WORKLOG = "worklog_";
    private static final String FIELD_NAME_TIME_LOGGED = WORKLOG + "timeLogged";
    private static final String FIELD_NAME_START_DATE = WORKLOG + "startDate";
    private static final String FIELD_NAME_NEW_ESTIMATE = WORKLOG + "newEstimate";
    private static final String FIELD_NAME_ADJUSTMENT_AMOUNT = WORKLOG + "adjustmentAmount";
    private static final String FIELD_NAME_ADJUST_ESTIMATE = WORKLOG + "adjustEstimate";
    private static final String FIELD_NAME_COMMENT = "comment";
    private static final String FIELD_NAME_COMMENT_LEVEL = "commentLevel";

    private TimeTracking.Format currentTimeFormat;

    /**
     * @return the LogWorkRunner implementation to use when running the tests.
     */
    protected abstract LogWorkRunner getLogWorkRunner();

    @Override
    public void setUpTest()
    {
        administration.restoreData("TestLogWorkAsField.xml");
    }

    public void testWorklogNoPermToCreate()
    {
        administration.permissionSchemes().defaultScheme();
        getTester().clickLink("del_perm_" + WORK_ON_ISSUES.permissionKey() + "_jira-developers");
        getTester().submit("Delete");
        getLogWorkRunner().gotoLogWorkScreen1();
        getTester().assertElementNotPresent("worklog_timeLogged");
    }

    public void testWorklogTimeTrackingDisabled()
    {
        administration.timeTracking().disable();
        getLogWorkRunner().gotoLogWorkScreen1();
        tester.assertElementNotPresent("log-work-time-logged");

        administration.timeTracking().enable(TimeTracking.Mode.MODERN);
        getLogWorkRunner().gotoLogWorkScreen1();
        tester.assertElementPresent("log-work-time-logged");
    }

//    public void testLogWorkConversionRateNoteCorrect()
//    {
//        getLogWorkRunner().gotoLogWorkScreen1();
//        text.assertTextPresent("Note: Your current conversion rates are 1w = 7d and 1d = 24h");
//
//        // turn it off so we can re-enable with hours as the default
//        administration.timeTracking().disable();
//
//        // check that by default we'll end up with Minutes to preserve old behaviour
//        getTester().clickLink("admin_link");
//        getTester().clickLink("timetracking");
//        getTester().setFormElement("hoursPerDay", "6");
//        getTester().setFormElement("daysPerWeek", "5");
//        getTester().submit("Activate");
//
//        getLogWorkRunner().gotoLogWorkScreen1();
//        text.assertTextPresent("Note: Your current conversion rates are 1w = 5d and 1d = 6h");
//    }
//
    public void testLogWorkLeaveEstimateNoteCorrect()
    {
        // note: this test won't work the same way on Create, as the estimate will always be "unknown"
        getLogWorkRunner().gotoLogWorkScreen1();
        if (getLogWorkRunner().isLoggingWorkTwiceSupported())
        {
            text.assertTextPresent("Use existing estimate of 15 hours");
        }
        else
        {
            text.assertTextPresent("Leave estimate unset");
        }

        // this will be both regardless of Create or otherwise
        getLogWorkRunner().gotoLogWorkScreenWithOriginalEstimate("");
        text.assertTextPresent("Leave estimate unset");
    }

    /**
     * Errors should be reported on the "Time Spent" field, "Start Date" field and hideable "new estimate" field if
     * "Set estimated time remaining" radio button has been selected, if they are empty.
     */
    public void testMandatoryFields()
    {
        // make Log Work field required
        administration.fieldConfigurations().defaultFieldConfiguration().requireField("Log Work");
        getLogWorkRunner().gotoLogWorkScreen1();

        // check the validation on newEstimate
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "");
        getTester().setFormElement(FIELD_NAME_START_DATE, "");
        getTester().setFormElement(FIELD_NAME_NEW_ESTIMATE, "");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "new");
        getTester().submit();
        text.assertTextPresent("You must indicate the time spent working.");
        text.assertTextPresent("You must specify a date on which the work occurred.");
        text.assertTextPresent("You must supply a valid new estimate.")  ;

        // check the validation on adjustmentAmount
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "");
        getTester().setFormElement(FIELD_NAME_START_DATE, "");
        getTester().setFormElement(FIELD_NAME_ADJUSTMENT_AMOUNT, "");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "manual");
        getTester().submit();
        text.assertTextPresent("You must indicate the time spent working.");
        text.assertTextPresent("You must specify a date on which the work occurred.");
        text.assertTextPresent("You must supply a valid amount of time to adjust the estimate by.");

        // make Log Work field optional
        administration.fieldConfigurations().defaultFieldConfiguration().optionalField("Log Work");
        getLogWorkRunner().gotoLogWorkScreen1();

        // partial information will make the log work inputs required
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "");
        getTester().setFormElement(FIELD_NAME_START_DATE, "");
        getTester().setFormElement(FIELD_NAME_NEW_ESTIMATE, "1h");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "new");
        getTester().submit();
        text.assertTextPresent("You must indicate the time spent working.");
        text.assertTextPresent("You must specify a date on which the work occurred.");

        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "1h");
        getTester().setFormElement(FIELD_NAME_START_DATE, "");
        getTester().setFormElement(FIELD_NAME_NEW_ESTIMATE, "");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "new");
        getTester().submit();
        text.assertTextPresent("You must specify a date on which the work occurred.");
        text.assertTextPresent("You must supply a valid new estimate.")  ;

        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "");
        getTester().setFormElement(FIELD_NAME_START_DATE, "");
        getTester().setFormElement(FIELD_NAME_ADJUSTMENT_AMOUNT, "1h");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "manual");
        getTester().submit();
        text.assertTextPresent("You must indicate the time spent working.");
        text.assertTextPresent("You must specify a date on which the work occurred.");

        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "1h");
        getTester().setFormElement(FIELD_NAME_START_DATE, "");
        getTester().setFormElement(FIELD_NAME_ADJUSTMENT_AMOUNT, "");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "manual");
        getTester().submit();
        text.assertTextPresent("You must specify a date on which the work occurred.");
        text.assertTextPresent("You must supply a valid amount of time to adjust the estimate by.");
    }

    /**
     * Errors should be reported if the "Time Spent" field or the hideable "new estimate" field (if "Set estimated time
     * remaining" radio button has been selected) contains an invalid duration string.
     *
     * Valid durations strings include "4d 6h", "30m", etc.
     */
    public void testInvalidFormattedDurationFields()
    {
        getLogWorkRunner().gotoLogWorkScreen1();

        // check the validation on newEstimate
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "16 Candles");
        getTester().setFormElement(FIELD_NAME_START_DATE, "18/Jun/07 10:49 AM"); //should be valid
        getTester().setFormElement(FIELD_NAME_NEW_ESTIMATE, "Six Days, Seven Nights");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "new");
        getTester().submit();
        text.assertTextPresent("Invalid time duration entered.");
        text.assertTextPresent("Invalid new estimate entered.");

        // check the validation on adjustmentAmount
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "16 Candles");
        getTester().setFormElement(FIELD_NAME_START_DATE, "18/Jun/07 10:49 AM"); //should be valid
        getTester().setFormElement(FIELD_NAME_ADJUSTMENT_AMOUNT, "Six Days, Seven Nights");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "manual");
        getTester().submit();
        text.assertTextPresent("Invalid time entered for adjusting the estimate.");

        // check negative time
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "-2h");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "auto");
        getTester().submit();
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
            getLogWorkRunner().gotoLogWorkScreen1();
            getTester().setFormElement(FIELD_NAME_TIME_LOGGED, badDuration);
            getTester().submit();
            text.assertTextPresent("Invalid time duration entered.");
        }
    }

    public void testGoodFractionDuration() throws Exception
    {
        getLogWorkRunner().gotoLogWorkScreenWithOriginalEstimate("4d");
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "1.5d 2.5h 18m");
        getTester().submit();
        text.assertTextSequence(new IdLocator(tester, "timetrackingmodule"), "Estimated", "4d", "Remaining", "2d 9h 12m", "Logged", "1d 14h 48m");
    }

    public void testInvalidTimeSpentZero()
    {
        getLogWorkRunner().gotoLogWorkScreen1();
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "0");
        getTester().setFormElement(FIELD_NAME_START_DATE, "18/Jun/07 10:49 AM"); //should be valid
        getTester().submit();
        text.assertTextPresent("Time Spent can not be zero.");
    }

    public void testInvalidStartDateField()
    {
        getLogWorkRunner().gotoLogWorkScreen1();
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "4h");
        getTester().setFormElement(FIELD_NAME_START_DATE, "The Day After Tomorrow"); //should be valid
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "leave");
        getTester().submit();
        text.assertTextPresent("You must specify a date on which the work occurred.");
    }

    public void testAutoAdjustEstimate()
    {
        getLogWorkRunner().gotoLogWorkScreenWithOriginalEstimate("4d");
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "4h 30m");
        getTester().submit();
        text.assertTextSequence(new IdLocator(tester, "timetrackingmodule"), "Estimated", "4d", "Remaining", "3d 19h 30m", "Logged", "4h 30m");
    }

    public void testNewEstimate()
    {
        getLogWorkRunner().gotoLogWorkScreenWithOriginalEstimate("4d");
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "4h 30m");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "new");
        getTester().setFormElement(FIELD_NAME_NEW_ESTIMATE, "1d 5h");
        getTester().submit();
        text.assertTextSequence(new IdLocator(tester, "timetrackingmodule"), "Estimated", "4d", "Remaining", "1d 5h", "Logged", "4h 30m");
    }

    public void testLeaveExistingEstimate()
    {
        getLogWorkRunner().gotoLogWorkScreenWithOriginalEstimate("4d");
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "4h 30m");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "leave");
        getTester().submit();
        text.assertTextSequence(new IdLocator(tester, "timetrackingmodule"), "Estimated", "4d", "Remaining", "4d", "Logged", "4h 30m");
    }

    public void testManuallyAdjustEstimate()
    {
        getLogWorkRunner().gotoLogWorkScreenWithOriginalEstimate("2d");
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "4h 30m");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "manual");
        getTester().setFormElement(FIELD_NAME_ADJUSTMENT_AMOUNT, "1d 6h");
        getTester().submit();
        text.assertTextSequence(new IdLocator(tester, "timetrackingmodule"), "Estimated", "2d", "Remaining", "18h", "Logged", "4h 30m");
    }

    public void testLogWorkVisibleToAll()
    {
        reconfigureTimetracking(TimeTracking.Format.PRETTY);
        execLogWorkVisibleToAll();
    }

    public void testLogWorkVisibleToAllDaysTimeFormat()
    {
        reconfigureTimetracking(TimeTracking.Format.DAYS);
        execLogWorkVisibleToAll();
    }

    public void testLogWorkVisibleToAllHoursTimeFormat()
    {
        reconfigureTimetracking(TimeTracking.Format.HOURS);
        execLogWorkVisibleToAll();
    }

    private void execLogWorkVisibleToAll()
    {
        getLogWorkRunner().gotoLogWorkScreen1();
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "2d");
        getTester().setFormElement(FIELD_NAME_COMMENT, WORK_LOG_COMMENT_1);
        getTester().submit();

        getTester().clickLinkWithText("Work Log");
        text.assertTextPresent(WORK_LOG_COMMENT_1);
        if (TimeTracking.Format.PRETTY == currentTimeFormat)
        {
            text.assertTextSequence(new IdLocator(tester, "worklog-10000"), "Time Spent", "2 days");
        }
        else if (TimeTracking.Format.DAYS == currentTimeFormat)
        {
            text.assertTextSequence(new IdLocator(tester, "worklog-10000"), "Time Spent", "2d");
        }
        else if (TimeTracking.Format.HOURS == currentTimeFormat)
        {
            text.assertTextSequence(new IdLocator(tester, "worklog-10000"), "Time Spent", "48h");
        }
    }

    public void testLogWorkVisibleToRole()
    {
        reconfigureTimetracking(TimeTracking.Format.PRETTY);
        execLogWorkVisibleToRole();
    }

    public void testLogWorkVisibleToRoleDaysTimeFormat()
    {
        reconfigureTimetracking(TimeTracking.Format.DAYS);
        execLogWorkVisibleToRole();
    }

    public void testLogWorkVisibleToRoleHoursTimeFormat()
    {
        reconfigureTimetracking(TimeTracking.Format.HOURS);
        execLogWorkVisibleToRole();
    }

    private void execLogWorkVisibleToRole()
    {
        execLogWorkVisibleToCommentLevel(ROLE_DEVELOPERS);
    }

    public void testLogWorkVisibleToGroup()
    {
        reconfigureTimetracking(TimeTracking.Format.PRETTY);
        execLogWorkVisibleToGroup();
    }

    public void testLogWorkVisibleToGroupDaysTimeFormat()
    {
        reconfigureTimetracking(TimeTracking.Format.DAYS);
        execLogWorkVisibleToGroup();
    }

    public void testLogWorkVisibleToGroupHoursTimeFormat()
    {
        reconfigureTimetracking(TimeTracking.Format.HOURS);
        execLogWorkVisibleToGroup();
    }

    public void execLogWorkVisibleToGroup()
    {
        execLogWorkVisibleToCommentLevel(GROUP_ADMINISTRATORS);
    }

    public void execLogWorkVisibleToCommentLevel(final String commentLevel)
    {
        getLogWorkRunner().gotoLogWorkScreen1();

        // first submit with an error
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "dddddd");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "new");
        getTester().setFormElement(FIELD_NAME_NEW_ESTIMATE, "2w");
        getTester().setFormElement(FIELD_NAME_COMMENT, WORK_LOG_COMMENT_1);
        getTester().selectOption(FIELD_NAME_COMMENT_LEVEL, commentLevel);
        getTester().submit();

        // assert that when the form reloads that the previously selected comment level is still selected
        assertions.getJiraFormAssertions().assertSelectElementHasOptionSelected(FIELD_NAME_COMMENT_LEVEL, commentLevel);

        // fix up error and submit
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "2d");
        getTester().submit();

        // assert we can see work log item above as Admin
        getTester().clickLinkWithText("Work Log");
        text.assertTextPresent(WORK_LOG_COMMENT_1);
        if (TimeTracking.Format.PRETTY == currentTimeFormat)
        {
            text.assertTextSequence(new IdLocator(tester, "worklog-10000"), "Time Spent", "2 days");
        }
        else if (TimeTracking.Format.DAYS == currentTimeFormat)
        {
            text.assertTextSequence(new IdLocator(tester, "worklog-10000"), "Time Spent", "2d");
        }
        else if (TimeTracking.Format.HOURS == currentTimeFormat)
        {
            text.assertTextSequence(new IdLocator(tester, "worklog-10000"), "Time Spent", "48h");
        }

        // assert we cannot see work log item above as Fred
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        getLogWorkRunner().gotoLogWorkResult();

        text.assertTextNotPresent(WORK_LOG_COMMENT_1);
        assertions.assertNodeByIdDoesNotExist("worklog-10000");
        if (TimeTracking.Format.PRETTY == currentTimeFormat)
        {
            text.assertTextSequence(new IdLocator(tester, "timetrackingmodule"), "Remaining", "2w", "Logged", "2d");
        }
        else if (TimeTracking.Format.DAYS == currentTimeFormat)
        {
            text.assertTextSequence(new IdLocator(tester, "timetrackingmodule"), "Remaining", "14d", "Logged", "2d");
        }
        else if (TimeTracking.Format.HOURS == currentTimeFormat)
        {
            text.assertTextSequence(new IdLocator(tester, "timetrackingmodule"), "Remaining", "336h", "Logged", "48h");
        }
    }

    public void testLogWorkCommentsNotCopiedWhenCopyingDisabled()
    {
        administration.timeTracking().disableCopyingOfComments();
        getLogWorkRunner().gotoLogWorkScreen1();
        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "1h");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "new");
        getTester().setFormElement(FIELD_NAME_NEW_ESTIMATE, "2w");
        getTester().setFormElement(FIELD_NAME_COMMENT, WORK_LOG_COMMENT_1);
        getTester().selectOption(FIELD_NAME_COMMENT_LEVEL, GROUP_ADMINISTRATORS);
        getTester().submit();

        if (getLogWorkRunner().isCommentFieldShown())
        {
            // assert that is visible as a comment
            text.assertTextPresent(WORK_LOG_COMMENT_1);

            // assert that there is no worklog with that text
            getTester().clickLinkWithText("Work Log");
            text.assertTextNotPresent(WORK_LOG_COMMENT_1);

            // log in as fred
            navigation.login(FRED_USERNAME);

            getLogWorkRunner().gotoLogWorkResult();

            // already on Work Log panel
            // worklog is visible
            text.assertTextSequence(new IdLocator(tester, "worklog-10000"), "Time Spent", "1 hour");

            // comment is not visible
            getTester().clickLinkWithText("Comments");
            text.assertTextNotPresent(WORK_LOG_COMMENT_1);
        }
        else
        {
            // assert that is not visible as a comment
            text.assertTextNotPresent(WORK_LOG_COMMENT_1);

            // assert that there is a worklog with that text
            getTester().clickLinkWithText("Work Log");
            text.assertTextPresent(WORK_LOG_COMMENT_1);

            // log in as fred
            navigation.login(FRED_USERNAME);

            getLogWorkRunner().gotoLogWorkResult();

            // already on Work Log panel
            // cant see the worklog
            getTester().assertElementNotPresent("worklog-10000");

            // comment is not visible
            getTester().clickLinkWithText("Comments");
            text.assertTextNotPresent(WORK_LOG_COMMENT_1);
        }
    }

    public void testLogWorkCommentsCopiedWhenCopyingEnabled()
    {
        if (getLogWorkRunner().isCommentFieldShown())
        {

            getLogWorkRunner().gotoLogWorkScreen1();
            getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "1h");
            getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "new");
            getTester().setFormElement(FIELD_NAME_NEW_ESTIMATE, "2w");
            getTester().setFormElement(FIELD_NAME_COMMENT, WORK_LOG_COMMENT_1);
            getTester().selectOption(FIELD_NAME_COMMENT_LEVEL, GROUP_ADMINISTRATORS);
            getTester().submit();

            // assert that is visible as a comment
            text.assertTextPresent(WORK_LOG_COMMENT_1);

            // assert we can see work log item above as Admin
            getTester().clickLinkWithText("Work Log");
            text.assertTextPresent(WORK_LOG_COMMENT_1);
            text.assertTextSequence(new IdLocator(tester, "worklog-10000"), "Time Spent", "1 hour");

            // login as fred
            navigation.login(FRED_USERNAME);

            getLogWorkRunner().gotoLogWorkResult();

            // already on Work Log panel
            // cant see the worklog
            getTester().assertElementNotPresent("worklog-10000");

            // cant see the comment either
            getTester().clickLinkWithText("Comments");
            text.assertTextNotPresent(WORK_LOG_COMMENT_1);
        }
        else
        {
            log("Skipping this test as it does not make sense when the comment field is not shown.");
        }
    }

    private void reconfigureTimetracking(TimeTracking.Format format)
    {
        administration.timeTracking().disable();
        administration.timeTracking().enable(format);
        currentTimeFormat = format;
    }

    public void testLogWorkDateIsStartDate()
    {
        if (getLogWorkRunner().isLoggingWorkTwiceSupported())
        {
            // Log work on a specific date and make sure it comes before other work
            getLogWorkRunner().gotoLogWorkScreen1();
            getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "2d");
            getTester().setFormElement(FIELD_NAME_START_DATE, "1/Jun/06 12:00 PM");
            getTester().submit();
            getTester().clickLinkWithText("Work Log");
            text.assertTextPresent(new IdLocator(tester, "worklog-10000"), "01/Jun/06 12:00 PM");

            // Add another that is older and make sure it comes first
            getLogWorkRunner().gotoLogWorkScreen2();
            getTester().checkCheckbox("worklog_activate", "true");
            getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "2d");
            getTester().setFormElement(FIELD_NAME_START_DATE, "1/Jun/05 12:00 PM");
            getTester().submit();
            text.assertTextPresent(new IdLocator(tester, "worklog-10001"), "01/Jun/05 12:00 PM");

            text.assertTextSequence(new IdLocator(tester, "issue_actions_container"), "01/Jun/05 12:00 PM", "01/Jun/06 12:00 PM");
        }
        else
        {
            log("Skipping this test as '" + getLogWorkRunner().getClass() + "' does not support logging work twice on the same issue.");
        }
    }

    public void testChangeHistory() throws Exception
    {
//        boolean isOracle = new EnvironmentUtils(tester, getEnvironmentData(), navigation).isOracle();

        getLogWorkRunner().gotoLogWorkScreenWithOriginalEstimate("4d");

//        if (isOracle)
//        {
            //TODO: Remove this sleep hack once http://jira.atlassian.com/browse/JRA-20274 has been resolved
            Thread.sleep(2000);
//        }

        getTester().setFormElement(FIELD_NAME_TIME_LOGGED, "4h 30m");
        getTester().checkCheckbox(FIELD_NAME_ADJUST_ESTIMATE, "new");
        getTester().setFormElement(FIELD_NAME_NEW_ESTIMATE, "1d 5h");
        getTester().submit();

        getLogWorkRunner().gotoLogWorkResult();
        getTester().clickLink("changehistory-tabpanel");

        ChangeHistoryList expectedList = new ChangeHistoryList();
        expectedList.addChangeSet(ADMIN_FULLNAME)
                .add("Time Spent", "", "4 hours, 30 minutes [ 16200 ]")
                .add("Remaining Estimate", "4 days [ 345600 ]", "1 day, 5 hours [ 104400 ]")
                .add("Worklog Id", "10000 [ 10000 ]", "")
                ;

        final ChangeHistoryList list = parse.issue().parseChangeHistory();
        list.assertContainsSomeOf(expectedList);

    }
}

/**
 * Defines the required behaviours of a context for logging work by using the system field. This allows us to run the
 * same tests in different contexts easily.
 */
interface LogWorkRunner
{
    /**
     * Navigate to the screen where logging work will take place.
     */
    void gotoLogWorkScreen1();

    /**
     * Defines whether the comment field is shown on the log work screen. A value of false implies that the Work Description
     * field is shown instead.
     *
     * @return true if it is; false otherwise.
     */
    boolean isCommentFieldShown();

    /**
     * Defines whether logging work twice for the same issue is applicable in this context. If false, some tests can be
     * skipped.
     *
     * @return true if it is supported; false otherwise.
     */
    boolean isLoggingWorkTwiceSupported();

    /**
     * Navigate to the screen where logging work will take place for a second time on the same issue.
     *
     * @throws UnsupportedOperationException if {@link #isLoggingWorkTwiceSupported()} is <code>false</code>.
     */
    void gotoLogWorkScreen2() throws UnsupportedOperationException;

    /**
     * Set the original estimate for the issue about to be worked on and then navigate to the screen where logging work
     * will take place.
     *
     * @param originalEstimate the value to set for the original estimate of the issue.
     */
    void gotoLogWorkScreenWithOriginalEstimate(final String originalEstimate);

    /**
     * Navigate to the screen which will display the issue which had work logged on it.
     */
    void gotoLogWorkResult();
}
