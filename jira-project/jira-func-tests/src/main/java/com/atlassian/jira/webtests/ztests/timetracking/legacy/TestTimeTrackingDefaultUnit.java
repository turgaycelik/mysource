package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * <p>Responsible for executing tests that verify that the default unit of time can be altered.</p>
 * <p>Additionally, the tests verify that existing data is not corrupted and that basic functionality works after the
 * default unit has changed.</p>
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING })
public class TestTimeTrackingDefaultUnit extends FuncTestCase
{
    /**
     * Verifies that the default time tracking unit is minutes.
     */
    public void testDefaultUnitIsMinutes()
    {
        administration.restoreBlankInstance();
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);

        getTester().assertTextPresent("The current default unit for time tracking is <b>minute</b>.");
    }

    /**
     * <p>Verifies that the default unit for time tracking can be set to a value different from the default (minutes).</p>
     * <p>After activating time tracking, it performs basic time tracking operations (creating an issue with estimates,
     * logging work) and asserts that they continue to work as expected.</p>
     */
    public void testDefaultUnitCanBeSet()
    {
        administration.restoreBlankInstance();
        administration.timeTracking().enable("24", "7", "pretty", "hour", TimeTracking.Mode.LEGACY);
        text.assertTextPresent("The current default unit for time tracking is <b>hour</b>.");

        final String issueKey = createNewIssueWithTimeTrackingEstimate("5");
        text.assertTextPresent("Time Tracking");
        assertTimeTrackingFields("5h", "5h", "Not Specified");

        navigation.issue().logWork(issueKey, "3");
        assertTimeTrackingFields("5h", "2h", "3h");
    }

    /**
     * <p>Verifies that the default unit of time can be switched from one unit to another, while you change the
     * length of the week and of the working day.</p>
     * <p>After activating time tracking, it performs basic time tracking operations (creating an issue with estimates,
     * logging work) and asserts that they continue to work as expected, and that existing that the existing data is in
     * a consistent state.</p>
     */
    public void testSwitchDefaultUnitWhileChangingTheLengthOfWeeksAndWorkingDays()
    {
        administration.restoreBlankInstance();
        administration.timeTracking().enable("8", "5", "pretty", "day", TimeTracking.Mode.LEGACY);
        text.assertTextPresent("The current default unit for time tracking is <b>day</b>.");

        final String issueKey = createNewIssueWithTimeTrackingEstimate("11");
        text.assertTextPresent("Time Tracking");
        assertTimeTrackingFields("2w 1d", "2w 1d", "Not Specified");

        navigation.issue().logWork(issueKey, "1w");
        assertTimeTrackingFields("2w 1d", "1w 1d", "1w");

        // Turn it off so we can change the definition of "hoursPerDay" and "daysPerWeek"
        administration.timeTracking().disable();
        administration.timeTracking().enable("24", "2", "pretty", "week", TimeTracking.Mode.LEGACY);
        text.assertTextPresent("The current default unit for time tracking is <b>week</b>.");

        navigation.issue().viewIssue(issueKey);
        assertTimeTrackingFields("1w 1d 16h", "1w", "1d 16h");

        navigation.issue().logWork(issueKey, "1");
        assertTimeTrackingFields("1w 1d 16h", "0m", "1w 1d 16h");
    }

    /**
     * <p>Verifies that the default unit of time can be switched from one unit to another.</p>
     * <p>After activating time tracking, it performs basic time tracking operations (creating an issue with estimates,
     * logging work) and asserts that they continue to work as expected, and that existing that the existing data is in
     * a consistent state.</p>
     */
    public void testSwitchDefaultUnitOnly()
    {
        administration.restoreBlankInstance();
        administration.timeTracking().enable("6", "5", "pretty", "minute", TimeTracking.Mode.LEGACY);
        text.assertTextPresent("The current default unit for time tracking is <b>minute</b>.");

        final String issueKey = createNewIssueWithTimeTrackingEstimate("3w");
        text.assertTextPresent("Time Tracking");
        assertTimeTrackingFields("3w", "3w", "Not Specified");

        navigation.issue().logWork(issueKey, "120");
        assertTimeTrackingFields("3w", "2w 4d 4h", "2h");

        // Turn it off so we can re-enable with hours as the default
        administration.timeTracking().disable();
        administration.timeTracking().enable("6","5","pretty","hour", TimeTracking.Mode.LEGACY);
        getTester().assertTextPresent("The current default unit for time tracking is <b>hour</b>.");

        navigation.issue().viewIssue(issueKey);
        navigation.issue().logWork(issueKey, "5");
        assertTimeTrackingFields("3w", "2w 3d 5h", "1d 1h");
    }

    private void assertTimeTrackingFields(final String originalEstimate, final String remainingEstimate, final String timeSpent)
    {
        text.assertTextSequence(new IdLocator(getTester(), "tt_single_table_info"), "Estimated:", originalEstimate,
                "Remaining:", remainingEstimate, "Logged:", timeSpent);
    }

    private String createNewIssueWithTimeTrackingEstimate(final String originalEstimate)
    {
        final String issueSummary = "Morbi pretium mattis nulla";

        navigation.issue().goToCreateIssueForm(null,null);

        getTester().setFormElement("summary", issueSummary);
        getTester().setFormElement("timetracking", originalEstimate);
        getTester().submit("Create");

        text.assertTextPresent(issueSummary);
        return new IdLocator(getTester(), "key-val").getText();
    }
}