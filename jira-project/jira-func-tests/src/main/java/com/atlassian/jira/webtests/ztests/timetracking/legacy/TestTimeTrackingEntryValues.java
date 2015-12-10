package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for holding tests that verify that the time tracking fields validate and process data entry correctly.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING })
public class TestTimeTrackingEntryValues extends FuncTestCase
{
    public void testBasicValidation() throws Exception
    {
        administration.restoreBlankInstance();
        administration.timeTracking().enable("24", "7", "pretty", "hour", TimeTracking.Mode.LEGACY);

        // create an issue
        final String key = navigation.issue().createIssue("homosapien", "Bug", "time tracking entry");

        // set the Original Estimate to an invalid amount
        navigation.issue().setOriginalEstimate(key, "-5h");
        assertions.getJiraFormAssertions().assertAuiFieldErrMsg("The original estimate specified is not valid.");

        // set the Original Estimate to a valid amount
        navigation.issue().setOriginalEstimate(key, "5h");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("5h", "5h", "Not Specified");

        // now log some work
        navigation.issue().logWork(key, "2h");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("5h", "3h", "2h");

        // now set the Remaining Estimate to an invalid amount
        navigation.issue().setRemainingEstimate(key, "xxx");
        assertions.getJiraFormAssertions().assertAuiFieldErrMsg("The remaining estimate specified is not valid.");

        // set the Remaining Estimate to a valid amount
        navigation.issue().setRemainingEstimate(key, "1h");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("5h", "1h", "2h");
    }

    /**
     * Verifies that the time tracking field processes fractional values correctly.
     */
    public void testDayFractions()
    {
        administration.restoreBlankInstance();
        administration.timeTracking().enable("24", "7", "pretty", "hour", TimeTracking.Mode.LEGACY);

        final String key = navigation.issue().createIssue("homosapien", "Bug", "time tracking entry");

        // set the original estimate to 12.15h (fractions!)
        setOriginalEstimate(key, "12.15h");
        text.assertTextSequence(new IdLocator(getTester(), "tt_single_table_info"), "Estimated:", "12h 9m",
                "Remaining:", "12h 9m");

        // switch our printing format over to hours to make sure we display the fraction there
        administration.timeTracking().disable();
        administration.timeTracking().enable("24", "7", "hours", "hour", TimeTracking.Mode.LEGACY);

        navigation.issue().viewIssue(key);
        text.assertTextSequence(new IdLocator(getTester(), "tt_single_table_info"), "Estimated:", "12.15h",
                "Remaining:", "12.15h");

        // enter something that falls below our unit of precision (we are at hours, enter something less than one hour)
        setOriginalEstimate(key, "12m");
        text.assertTextSequence(new IdLocator(getTester(), "tt_single_table_info"), "Estimated:", "0.2h",
                "Remaining:", "0.2h");

        // switch back to days and make sure that we don't have a leading "0 days"
        administration.timeTracking().disable();
        administration.timeTracking().enable("24", "7", "days", "hour", TimeTracking.Mode.LEGACY);
        navigation.issue().viewIssue(key);
        text.assertTextNotPresent(new IdLocator(getTester(), "tt_single_values_orig"), "0d 0.2h");
        text.assertTextNotPresent(new IdLocator(getTester(), "tt_single_values_remain"), "0d 0.2h");

        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_remain"), "0.2h");

        // 12 minutes can be represented as 0.2 without losing precision. but 20 minutes cannot (1/3) make sure
        // that we fall back and display this as minutes
        setOriginalEstimate(key, "20m");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_orig"), "20m");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_remain"), "20m");

        // now go back to 12.15h and change the hours-per-day setting to see how we respond. 0.15h = 9m
        setOriginalEstimate(key, "12.15h");
        administration.timeTracking().disable();
        administration.timeTracking().enable("4.5", "7", "days", "hour", TimeTracking.Mode.LEGACY);
        navigation.issue().viewIssue(key);
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_orig"), "2.7d");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_remain"), "2.7d");

        administration.timeTracking().disable();
        administration.timeTracking().enable("9.5", "7", "days", "hour", TimeTracking.Mode.LEGACY);
        navigation.issue().viewIssue(key);
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_orig"), "1d 2.65h");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_remain"), "1d 2.65h");

        // let's see how the values change when we log a single minute. also, this verifies that logging work works
        // when we have a fractional hours-per-day
        navigation.issue().logWork(key, "1m");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_orig"), "1d 2.65h");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_remain"), "1d 2h 38m");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_spent"), "1m");

        // switch to hours and make sure we look okay there, too
        administration.timeTracking().disable();
        administration.timeTracking().enable("9.5", "7", "hours", "hour", TimeTracking.Mode.LEGACY);
        navigation.issue().viewIssue(key);
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_orig"), "12.15h");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_remain"), "12h 8m");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_spent"), "1m");

        // change from 9.5 to 9.4 hours per day and make sure that we display the changes properly (.1h = 6m)
        administration.timeTracking().disable();
        administration.timeTracking().enable("9.4", "7", "days", "hour", TimeTracking.Mode.LEGACY);
        navigation.issue().viewIssue(key);
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_orig"), "1d 2.75h");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_remain"), "1d 2h 44m");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_spent"), "1m");

        // log 1d = 9.4h = 9h 24m
        navigation.issue().logWork(key, "1d");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_orig"), "1d 2.75h");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_remain"), "2h 44m");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_spent"), "1d 1m");

        // switch back to 9.5 hours per day to make sure we update it properly
        administration.timeTracking().disable();
        administration.timeTracking().enable("9.5", "7", "days", "hour", TimeTracking.Mode.LEGACY);
        navigation.issue().viewIssue(key);
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_orig"), "1d 2.65h");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_remain"), "2h 44m");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_spent"), "9h 25m"); // was 1m, added 9h 24m
    }

    private void setOriginalEstimate(final String issueKey, final String originalEstimate)
    {
        navigation.issue().viewIssue(issueKey);
        getTester().clickLink("edit-issue");
        getTester().assertFormElementPresent("timetracking");
        getTester().setFormElement("timetracking", originalEstimate);
        getTester().submit();
    }
}
