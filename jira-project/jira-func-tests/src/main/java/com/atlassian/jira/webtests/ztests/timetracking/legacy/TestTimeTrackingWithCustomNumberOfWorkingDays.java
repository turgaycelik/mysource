package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * <p>Responsible for holding tests which verify that a custom value can be set for the number of working days per week,
 * and the number of working hours per day.</p>
 * <p>Additionally, the tests verify that existing data is not corrupted and that basic functionality works after the
 * default unit has changed.</p>
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING })
public class TestTimeTrackingWithCustomNumberOfWorkingDays extends FuncTestCase
{
    /**
     * <p>Verifies that the number of working days per week can be set to a fractional value (e.g 5.5)</p>
     * <p>After doing this, it executes a basic sanity check of time tracking operations (e.g. creating an issue with
     * estimates) and asserts that they continue to work as expected.</p>
     */
    public void testNumberOfWorkingDaysPerWeekCanBeFractional()
    {
        administration.restoreBlankInstance();
        administration.timeTracking().enable("24", "5.5", "pretty", "hour", TimeTracking.Mode.LEGACY);
        text.assertTextPresent("The number of working days per week is <b>5.5</b>");

        // Sanity Check - Estimates can be entered on a new issue
        final String issueKey = navigation.issue().createIssue("homosapien", "Bug", "time tracking entry");
        setOriginalEstimate(issueKey, "11d");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_orig"), "2w");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_remain"), "2w");

        administration.timeTracking().disable();
        administration.timeTracking().enable("24", "9.5", "pretty", "hour", TimeTracking.Mode.LEGACY);

        // Sanity Check - estimates for existing data are displayed according to the new number of working days
        navigation.issue().viewIssue(issueKey);
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_orig"), "1w 1d 12h");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_remain"), "1w 1d 12h");
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