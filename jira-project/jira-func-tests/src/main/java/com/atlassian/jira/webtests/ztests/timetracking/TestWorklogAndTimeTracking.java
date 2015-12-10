package com.atlassian.jira.webtests.ztests.timetracking;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Testing the interactions between the Worklog System Field and the Time Tracking System Field.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING })
public class TestWorklogAndTimeTracking extends FuncTestCase
{
    private static final String ISSUE_WITH_WORK = "HSP-1";
    private static final String NO_WORK_ISSUE = "HSP-2";

    private static final String TIMETRACKING = "timetracking";
    private static final String TIMETRACKING_ORIGINALESTIMATE = "timetracking_originalestimate";
    private static final String TIMETRACKING_REMAININGESTIMATE = "timetracking_remainingestimate";

    private static final String WORKLOG_ACTIVATE = "worklog_activate";
    private static final String WORKLOG_TIME_LOGGED = "worklog_timeLogged";
    private static final String WORKLOG_TIMETRACKINGCONTAINER = "worklog-timetrackingcontainer";
    private static final String WORKLOG_LOGWORKCONTAINER = "worklog-logworkcontainer";
    private static final String SUMMARY = "summary";

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestWorklogAndTimeTracking.xml");
        administration.fieldConfigurations().defaultFieldConfiguration().requireField("Time Tracking");
    }

    public void testValidationOnCreateInModernMode() throws Exception
    {
        enableModernMode();

        ///// Time Tracking is required hence entering the Remaining Estimate only is not sufficient when also Logging Work

        // goto create issue
        navigation.issue().goToCreateIssueForm("homosapien", "Bug");

        // leave original estimate blank
        // set remaining estimate
        // activate log work
        // set a time spent
        tester.setFormElement(SUMMARY, "The summary of this new issue");
        tester.setFormElement(TIMETRACKING_REMAININGESTIMATE, "5h");
        tester.setFormElement(WORKLOG_ACTIVATE, "true");
        tester.setFormElement(WORKLOG_TIME_LOGGED, "2h");
        tester.submit();

        // validation does not pass since we did not enter Original Estimate
        assertions.getJiraFormAssertions().assertAuiFieldErrMsg("Original Estimate is required.");

        ///// Time Tracking is required and Original Estimate is supplied while Logging Work

        // goto create issue
        navigation.issue().goToCreateIssueForm("homosapien", "Bug");

        // leave original estimate blank
        // set remaining estimate
        // activate log work
        // set a time spent
        tester.setFormElement(SUMMARY, "The summary of this new issue");
        tester.setFormElement(TIMETRACKING_ORIGINALESTIMATE, "4h");
        tester.setFormElement(TIMETRACKING_REMAININGESTIMATE, "5h");
        tester.setFormElement(WORKLOG_ACTIVATE, "true");
        tester.setFormElement(WORKLOG_TIME_LOGGED, "2h");
        tester.submit();

        // original estimate is now 4h
        // remaining estimate is now 2h
        // time logged is 2h
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("4h", "2h", "2h");

        ///// Time Tracking is not required, but we are Logging Work and so the Remaining Estimate is ignored

        // Time Tracking is not required
        administration.fieldConfigurations().defaultFieldConfiguration().optionalField("Time Tracking");

        // goto create issue
        navigation.issue().goToCreateIssueForm("homosapien", "Bug");

        // leave original estimate blank
        // set remaining estimate
        // activate log work
        // set a time spent
        tester.setFormElement(SUMMARY, "The summary of this new issue");
        tester.setFormElement(TIMETRACKING_REMAININGESTIMATE, "5h");
        tester.setFormElement(WORKLOG_ACTIVATE, "true");
        tester.setFormElement(WORKLOG_TIME_LOGGED, "2h");
        tester.submit();

        // original estimate is undefined
        // remaining estimate is 0m
        // time logged is 2h
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("Not Specified", "0m", "2h");
    }

    public void testValidationOfInputsTogetherLegacyModeWorkStarted() throws Exception
    {
        ///// Both inputs invalid

        // Enter an invalid input for Remaining Estimate
        // Activate Log Work
        // Time Spent invalid
        editTimeAndLogWork(ISSUE_WITH_WORK, "xxxx", true, "yyyy");

        // Assert that error returned for Log Work but not Remaining Estimate
        // Assert that the Log Work checkbox is checked
        // Assert that the input for Remaining Estimate was reset to what was on the issue
        assertFormState(
                true, "4h", "yyyy", "Invalid time duration entered.",
                "The original estimate specified is not valid."
        );


        // Leave Remaining Estimate blank
        // Dont activate log work
        // Enter an invalid input for Time Spent
        editTimeAndLogWork(ISSUE_WITH_WORK, "", false, "yyyy");

        // Assert that error returned for Remaining Estimate but not Log Work
        // Assert that the Log Work checkbox is not checked
        // Assert that the input for Time Spent was reset to nothing
        assertFormState(
                false, "", "", "Time Tracking is required.",
                "Invalid time duration entered."
        );

        ///// One input valid

        // Remaining Estimate invalid
        // Log Work valid
        // Log Work activated
        editTimeAndLogWork(ISSUE_WITH_WORK, "xxxx", true, "30m");

        // Remaining Estimate error message is not present
        text.assertTextNotPresent("The remaining estimate specified is not valid.");
        
        // 30m was logged - assert new time tracking info
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("6h", "3h 30m", "2h 30m");

        // Remaining Estimate valid
        // Log Work invalid
        // Log Work not activated
        editTimeAndLogWork(ISSUE_WITH_WORK, "4h 15m", false, "yyyy");

        // Remaining Estimate error message is not present
        text.assertTextNotPresent("Invalid time duration entered.");
        
        // Remaining Estimate was reset to 4h 15m - assert new time tracking info
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("6h", "4h 15m", "2h 30m");

        ///// Both inputs valid

        // Log Work activated
        editTimeAndLogWork(ISSUE_WITH_WORK, "7m", true, "8m");

        // 8m was logged - assert new time tracking info
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("6h", "4h 7m", "2h 38m");

        // Log Work not activated
        editTimeAndLogWork(ISSUE_WITH_WORK, "5h", false, "3m");

        // Remaining Estimate was reset to 5h - assert new time tracking info
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("6h", "5h", "2h 38m");
    }

    public void testValidationOfInputsTogetherLegacyModeWorkNotStarted() throws Exception
    {
        administration.fieldConfigurations().defaultFieldConfiguration().requireField("Log Work");

        ///// Both inputs invalid

        // Enter an invalid input for Original Estimate
        // Leave Time Spent blank
        editTimeAndLogWork(NO_WORK_ISSUE, "xxxx", "");

        // Assert that error returned for Log Work and Original Estimate
        // Assert that the inputs are as submitted
        assertFormState(
                "xxxx", "",
                "You must indicate the time spent working",
                "The original estimate specified is not valid.");


        ///// One input valid

        // Original Estimate valid
        // Log Work invalid
        editTimeAndLogWork(NO_WORK_ISSUE, "4h", "xxxx");

        assertFormState("4h", "xxxx", "Invalid time duration entered.");

        ///// Both inputs valid

        // Log Work activated
        editTimeAndLogWork(NO_WORK_ISSUE, "8h", "1h");

        // Original Estimate was set to 8h and 1h was logged - assert new time tracking info
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("8h", "7h", "1h");
    }

    public void testValidationOfInputsTogetherModernModeWorkStarted() throws Exception
    {
        enableModernMode();

        ///// Three inputs invalid

        // Enter an invalid input for Original Estimate
        // Enter an invalid input for Remaining Estimate
        // Activate Log Work
        // Enter an invalid Time spent
        editTimeAndLogWork(ISSUE_WITH_WORK, "zzzz", "xxxx", true, "yyyy");

        // Assert that error returned for Log Work and Original Estimate but not Remaining Estimate
        // Assert that the Log Work checkbox is checked
        // Assert that the input for Remaining Estimate was reset to what was on the issue
        assertFormStateModernMode(
                true, "zzzz", "4h", "yyyy",
                new String[] {"Invalid time duration entered.", "The original estimate specified is not valid."},
                new String[] {"The remaining estimate specified is not valid."}
        );

        // Original Estimate left blank
        // Enter an invalid input for Remaining Estimate
        // Activate Log Work
        // Enter an invalid Time Spent value
        editTimeAndLogWork(ISSUE_WITH_WORK, "", "xxxx", false, "ffff");

        // Assert that error returned for Remaining Estimate but not Original Estimate or Log Work
        // Assert that the Log Work checkbox is not checked
        // Assert that the input for Time Spent was reset to blank
        assertFormStateModernMode(
                false, "", "xxxx", "",
                new String[] {"The remaining estimate specified is not valid."},
                new String[] {"Original Estimate is required.", "Invalid time duration entered."}
        );

        ///// One input invalid

        // Remaining Estimate is valid
        // Original Estimate is blank
        // Log Work valid
        // Log Work activated
        editTimeAndLogWork(ISSUE_WITH_WORK, "", "1h", true, "30m");

        // Assert that Remaining Estimate was reset to what was on the issue.
        assertFormStateModernMode(
                true, "", "4h", "30m",
                new String[] {"Original Estimate is required."},
                new String[] {}
        );

        // Remaining Estimate invalid
        // Original Estimate valid
        // Log Work valid
        // Log Work activated
        editTimeAndLogWork(ISSUE_WITH_WORK, "8h", "xxxx", true, "30m");

        // Remaining Estimate error message is not present
        text.assertTextNotPresent("The remaining estimate specified is not valid.");

        // 30m was logged and the Original Estimate was set to 8h - assert new time tracking info
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("8h", "3h 30m", "2h 30m");

        // Remaining Estimate valid
        // Original Estimate valid
        // Log Work invalid
        // Log Work not activated
        editTimeAndLogWork(ISSUE_WITH_WORK, "9h", "4h", false, "xxxx");

        // Time Spent error message is not present
        text.assertTextNotPresent("Invalid time duration entered.");

        // Original Estimate was reset to 9h, Remaining Estimate was reset to 4h - assert new time tracking info
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("9h", "4h", "2h 30m");

        ///// All inputs valid

        // Log Work activated
        editTimeAndLogWork(ISSUE_WITH_WORK, "10h", "9h", true, "15m");

        // Original Estimate was reset to 10h, 15m was logged - assert new time tracking info
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("10h", "3h 45m", "2h 45m");

        // Log Work not activated
        editTimeAndLogWork(ISSUE_WITH_WORK, "11h", "7h", false, "6m");

        // Original Estimate was reset to 11h, Remaining Estimate was reset to 7h - assert new time tracking info
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("11h", "7h", "2h 45m");
    }

    public void testInitialRenderingOfTimeTrackingAndLogWorkFields() throws Exception
    {
        _testLegacyModeWorkStartedLogWorkPresent();
        _testLegacyModeWorkStartedLogWorkRequired();
        _testLegacyModeWorkStartedLogWorkNotPresent();
        _testLegacyModeWorkNotStartedLogWorkPresent();
        _testLegacyModeWorkNotStartedLogWorkNotPresent();

        enableModernMode();

        _testModernModeWorkNotStartedLogWorkPresent();
        _testModernModeWorkNotStartedLogWorkRequired();
        _testModernModeWorkNotStartedLogWorkNotPresent();
        _testModernModeWorkStartedLogWorkPresent();
        _testModernModeWorkStartedLogWorkNotPresent();
    }

    private void _testLegacyModeWorkStartedLogWorkPresent()
    {
        navigation.issue().gotoEditIssue(ISSUE_WITH_WORK);

        // remaining estimate should be shown
        tester.assertElementPresent(TIMETRACKING);
        assertLabelText(TIMETRACKING, "Remaining Estimate");
        assertContainerVisible(WORKLOG_TIMETRACKINGCONTAINER);

        // Log Work checkbox should be unselected
        tester.assertCheckboxNotSelected(WORKLOG_ACTIVATE);

        // Log Work container should be hidden
        assertContainerHidden(WORKLOG_LOGWORKCONTAINER);
    }

    private void _testLegacyModeWorkStartedLogWorkRequired()
    {
        // make Log Work required
        administration.fieldConfigurations().defaultFieldConfiguration().requireField("Log Work");

        navigation.issue().gotoEditIssue(ISSUE_WITH_WORK);

        // remaining estimate should not be present
        tester.assertElementNotPresent(TIMETRACKING);
        assertContainerNotPresent(WORKLOG_TIMETRACKINGCONTAINER);

        // Log Work checkbox should not be present
        assertCheckboxNotPresent(WORKLOG_ACTIVATE);

        // Log Work container should be shown
        assertContainerVisible(WORKLOG_LOGWORKCONTAINER);

        // make Log Work optional again
        administration.fieldConfigurations().defaultFieldConfiguration().optionalField("Log Work");
    }

    private void _testLegacyModeWorkStartedLogWorkNotPresent()
    {
        administration.fieldConfigurations().defaultFieldConfiguration().getScreens("Log Work").removeFieldFromScreen("Default Screen");
        navigation.issue().gotoEditIssue(ISSUE_WITH_WORK);

        // remaining estimate should be shown
        tester.assertElementPresent(TIMETRACKING);
        assertLabelText(TIMETRACKING, "Remaining Estimate");
        assertContainerNotPresent(WORKLOG_TIMETRACKINGCONTAINER);

        // Log Work checkbox should not be present
        assertCheckboxNotPresent(WORKLOG_ACTIVATE);

        // Log Work container should not be present
        assertContainerNotPresent(WORKLOG_LOGWORKCONTAINER);
    }

    private void _testLegacyModeWorkNotStartedLogWorkPresent()
    {
        administration.fieldConfigurations().defaultFieldConfiguration().getScreens("Log Work").addFieldToScreen("Default Screen");
        navigation.issue().gotoEditIssue(NO_WORK_ISSUE);

        // original estimate should be shown
        tester.assertElementPresent(TIMETRACKING);
        assertLabelText(TIMETRACKING, "Original Estimate");
        assertContainerNotPresent(WORKLOG_TIMETRACKINGCONTAINER);

        // Log Work checkbox should not be present
        assertCheckboxNotPresent(WORKLOG_ACTIVATE);

        // Log Work container should be shown
        assertContainerVisible(WORKLOG_LOGWORKCONTAINER);
    }

    private void _testLegacyModeWorkNotStartedLogWorkNotPresent()
    {
        administration.fieldConfigurations().defaultFieldConfiguration().getScreens("Log Work").removeFieldFromScreen("Default Screen");
        navigation.issue().gotoEditIssue(NO_WORK_ISSUE);

        // original estimate should be shown
        tester.assertElementPresent(TIMETRACKING);
        assertLabelText(TIMETRACKING, "Original Estimate");
        assertContainerNotPresent(WORKLOG_TIMETRACKINGCONTAINER);

        // Log Work checkbox should not be present
        assertCheckboxNotPresent(WORKLOG_ACTIVATE);

        // Log Work container should not be present
        assertContainerNotPresent(WORKLOG_LOGWORKCONTAINER);
    }

    private void _testModernModeWorkNotStartedLogWorkPresent()
    {
        administration.fieldConfigurations().defaultFieldConfiguration().getScreens("Log Work").addFieldToScreen("Default Screen");
        navigation.issue().gotoEditIssue(NO_WORK_ISSUE);

        // original estimate should be shown
        tester.assertElementPresent(TIMETRACKING_ORIGINALESTIMATE);
        assertLabelText(TIMETRACKING_ORIGINALESTIMATE, "Original Estimate");

        // remaining estimate should be shown
        tester.assertElementPresent(TIMETRACKING_REMAININGESTIMATE);
        assertLabelText(TIMETRACKING_REMAININGESTIMATE, "Remaining Estimate");
        assertContainerVisible(WORKLOG_TIMETRACKINGCONTAINER);

        // Log Work checkbox should be unselected
        tester.assertCheckboxNotSelected(WORKLOG_ACTIVATE);

        // Log Work container should be hidden
        assertContainerHidden(WORKLOG_LOGWORKCONTAINER);
    }

    private void _testModernModeWorkNotStartedLogWorkRequired()
    {
        // make Log Work required
        administration.fieldConfigurations().defaultFieldConfiguration().requireField("Log Work");
        navigation.issue().gotoEditIssue(NO_WORK_ISSUE);

        // original estimate should be shown
        tester.assertElementPresent(TIMETRACKING_ORIGINALESTIMATE);
        assertLabelText(TIMETRACKING_ORIGINALESTIMATE, "Original Estimate");

        // remaining estimate should not be present
        tester.assertElementNotPresent(TIMETRACKING_REMAININGESTIMATE);
        assertContainerNotPresent(WORKLOG_TIMETRACKINGCONTAINER);

        // Log Work checkbox should not be present
        assertCheckboxNotPresent(WORKLOG_ACTIVATE);

        // Log Work container should be shown
        assertContainerVisible(WORKLOG_LOGWORKCONTAINER);

        // make Log Work optional again
        administration.fieldConfigurations().defaultFieldConfiguration().optionalField("Log Work");
    }

    private void _testModernModeWorkNotStartedLogWorkNotPresent()
    {
        administration.fieldConfigurations().defaultFieldConfiguration().getScreens("Log Work").removeFieldFromScreen("Default Screen");
        navigation.issue().gotoEditIssue(NO_WORK_ISSUE);

        // original estimate should be shown
        tester.assertElementPresent(TIMETRACKING_ORIGINALESTIMATE);
        assertLabelText(TIMETRACKING_ORIGINALESTIMATE, "Original Estimate");

        // remaining estimate should be shown
        tester.assertElementPresent(TIMETRACKING_REMAININGESTIMATE);
        assertLabelText(TIMETRACKING_REMAININGESTIMATE, "Remaining Estimate");
        assertContainerNotPresent(WORKLOG_TIMETRACKINGCONTAINER);

        // Log Work checkbox should not be present
        assertCheckboxNotPresent(WORKLOG_ACTIVATE);

        // Log Work container should not be present
        assertContainerNotPresent(WORKLOG_LOGWORKCONTAINER);
    }

    private void _testModernModeWorkStartedLogWorkPresent()
    {
        administration.fieldConfigurations().defaultFieldConfiguration().getScreens("Log Work").addFieldToScreen("Default Screen");
        navigation.issue().gotoEditIssue(ISSUE_WITH_WORK);

        // original estimate should be shown
        tester.assertElementPresent(TIMETRACKING_ORIGINALESTIMATE);
        assertLabelText(TIMETRACKING_ORIGINALESTIMATE, "Original Estimate");

        // remaining estimate should be shown
        tester.assertElementPresent(TIMETRACKING_REMAININGESTIMATE);
        assertLabelText(TIMETRACKING_REMAININGESTIMATE, "Remaining Estimate");
        assertContainerVisible(WORKLOG_TIMETRACKINGCONTAINER);

        // Log Work checkbox should be unselected
        tester.assertCheckboxNotSelected(WORKLOG_ACTIVATE);

        // Log Work container should be hidden
        assertContainerHidden(WORKLOG_LOGWORKCONTAINER);
    }

    private void _testModernModeWorkStartedLogWorkNotPresent()
    {
        administration.fieldConfigurations().defaultFieldConfiguration().getScreens("Log Work").removeFieldFromScreen("Default Screen");
        navigation.issue().gotoEditIssue(ISSUE_WITH_WORK);

        // original estimate should be shown
        tester.assertElementPresent(TIMETRACKING_ORIGINALESTIMATE);
        assertLabelText(TIMETRACKING_ORIGINALESTIMATE, "Original Estimate");

        // remaining estimate should be shown
        tester.assertElementPresent(TIMETRACKING_REMAININGESTIMATE);
        assertLabelText(TIMETRACKING_REMAININGESTIMATE, "Remaining Estimate");
        assertContainerNotPresent(WORKLOG_TIMETRACKINGCONTAINER);

        // Log Work checkbox should not be present
        assertCheckboxNotPresent(WORKLOG_ACTIVATE);

        // Log Work container should not be present
        assertContainerNotPresent(WORKLOG_LOGWORKCONTAINER);
    }

    /**
     * Asserts the state of the edit form after an unsuccessful edit in Modern Mode.
     *
     * @param shouldLogWorkBeChecked should the checkbox be checked after submission
     * @param expectedOriginalEstimate expected value in the original estimate input
     * @param expectedRemainingEstimate expected value in the remaining estimate input
     * @param expectedTimeLogged expected value in the time logged input
     * @param presentErrors errors which should be present in the form
     * @param absentErrors errors which should be absent in the form
     */
    private void assertFormStateModernMode(final boolean shouldLogWorkBeChecked, final String expectedOriginalEstimate,
            final String expectedRemainingEstimate, final String expectedTimeLogged, final String[] presentErrors,
            final String[] absentErrors)
    {
        for (String error : presentErrors)
        {
            assertions.getJiraFormAssertions().assertAuiFieldErrMsg(error);
        }
        for (String error : absentErrors)
        {
            text.assertTextNotPresent(error);
        }

        if (shouldLogWorkBeChecked)
        {
            assertCheckboxChecked(WORKLOG_ACTIVATE);
        }
        else
        {
            tester.assertCheckboxNotSelected(WORKLOG_ACTIVATE);
        }

        tester.assertFormElementEquals(TIMETRACKING_ORIGINALESTIMATE, expectedOriginalEstimate);
        tester.assertFormElementEquals(TIMETRACKING_REMAININGESTIMATE, expectedRemainingEstimate);
        tester.assertFormElementEquals(WORKLOG_TIME_LOGGED, expectedTimeLogged);
    }

    /**
     * Asserts the state of the edit form after an unsuccessful edit in Legacy Mode for an issue which has started work.
     *
     * @param shouldLogWorkBeChecked should the checkbox be checked after submission
     * @param expectedRemainingEstimate expected value in the remaining estimate input
     * @param expectedTimeLogged expected value in the time logged input
     * @param presentError errors which should be present in the form
     * @param absentError errors which should be absent in the form
     */
    private void assertFormState(final boolean shouldLogWorkBeChecked, final String expectedRemainingEstimate,
            final String expectedTimeLogged, final String presentError, final String absentError)
    {
        assertions.getJiraFormAssertions().assertAuiFieldErrMsg(presentError);
        text.assertTextNotPresent(absentError);

        if (shouldLogWorkBeChecked)
        {
            assertCheckboxChecked(WORKLOG_ACTIVATE);
        }
        else
        {
            tester.assertCheckboxNotSelected(WORKLOG_ACTIVATE);
        }

        tester.assertFormElementEquals(TIMETRACKING, expectedRemainingEstimate);
        tester.assertFormElementEquals(WORKLOG_TIME_LOGGED, expectedTimeLogged);
    }

    /**
     * Asserts the state of the edit form after an unsuccessful edit in Legacy Mode for an issue which has not yet started work.
     *
     * @param expectedOriginalEstimate expected value in the original estimate input
     * @param expectedTimeLogged expected value in the time logged input
     * @param presentErrors errors which should be present in the form
     */
    private void assertFormState(final String expectedOriginalEstimate, final String expectedTimeLogged, final String... presentErrors)
    {
        tester.assertFormElementEquals(TIMETRACKING, expectedOriginalEstimate);
        tester.assertFormElementEquals(WORKLOG_TIME_LOGGED, expectedTimeLogged);

        for (String error : presentErrors)
        {
            assertions.getJiraFormAssertions().assertAuiFieldErrMsg(error);
        }
    }

    /**
     * Edit an issue, set the remaining estimate, potentially activate the Log Work checkbox and set the time logged field.
     *
     * @param issueKey issue key
     * @param remainingEstimate remaining estimate
     * @param activateLogWork true if we are activating the checkbox
     * @param timeLogged the time spent
     */
    private void editTimeAndLogWork(final String issueKey, final String remainingEstimate, final boolean activateLogWork, final String timeLogged)
    {
        navigation.issue().gotoEditIssue(issueKey);
        tester.setFormElement(TIMETRACKING, remainingEstimate);
        if (activateLogWork)
        {
            tester.setFormElement(WORKLOG_ACTIVATE, "true");
        }
        tester.setFormElement(WORKLOG_TIME_LOGGED, timeLogged);
        tester.submit();
    }

    /**
     * Edit an issue, set the remaining estimate, potentially activate the Log Work checkbox and set the time logged field.
     *
     * @param issueKey issue key
     * @param originalEstimate original estimate
     * @param remainingEstimate remaining estimate
     * @param activateLogWork true if we are activating the checkbox
     * @param timeLogged the time spent
     */
    private void editTimeAndLogWork(final String issueKey, final String originalEstimate, final String remainingEstimate, final boolean activateLogWork, final String timeLogged)
    {
        navigation.issue().gotoEditIssue(issueKey);
        tester.setFormElement(TIMETRACKING_ORIGINALESTIMATE, originalEstimate);
        tester.setFormElement(TIMETRACKING_REMAININGESTIMATE, remainingEstimate);
        if (activateLogWork)
        {
            tester.setFormElement(WORKLOG_ACTIVATE, "true");
        }
        tester.setFormElement(WORKLOG_TIME_LOGGED, timeLogged);
        tester.submit();
    }

    /**
     * Edit an issue, set the original estimate and the time logged field.
     *
     * @param issueKey issue key
     * @param originalEstimate the original estimate
     * @param timeLogged the time spent
     */
    private void editTimeAndLogWork(final String issueKey, final String originalEstimate, final String timeLogged)
    {
        navigation.issue().gotoEditIssue(issueKey);
        tester.setFormElement(TIMETRACKING, originalEstimate);
        tester.setFormElement(WORKLOG_TIME_LOGGED, timeLogged);
        tester.submit();
    }

    private void assertLabelText(final String labelField, final String labelText)
    {
        text.assertTextPresent(new XPathLocator(tester, String.format("//label[@for='%s']", labelField)), labelText);
    }

    private void assertCheckboxNotPresent(final String checkboxName)
    {
        assertions.assertNodeDoesNotExist(String.format("//input[@type='checkbox' and @name='%s']", checkboxName));
    }

    private void assertContainerNotPresent(final String containerId)
    {
        assertions.assertNodeDoesNotExist(String.format("//div[@id='%s']", containerId));
    }

    private void assertCheckboxChecked(final String checkboxName)
    {
        assertions.assertNodeExists(String.format("//input[@type='checkbox' and @name='%s' and @checked='checked']", checkboxName));
    }

    private void assertContainerVisible(final String containerId)
    {
        assertions.assertNodeExists(String.format("//div[@id='%s' and not(@class)]", containerId));
    }

    private void assertContainerHidden(final String containerId)
    {
        assertions.assertNodeExists(String.format("//div[@id='%s' and @class='hidden']", containerId));
    }

    private void enableModernMode()
    {
        administration.timeTracking().disable();
        administration.timeTracking().enable(TimeTracking.Mode.MODERN);
    }
}
