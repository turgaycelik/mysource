package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Responsible for holding tests that verify that the tooltips shown in the View Issue Page for the Time Tracking fields
 * are correct according to the selected format for time tracking.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING })
public class TestTimeTrackingToolTips extends FuncTestCase
{
    public void testTimeTrackingToolTips() throws Exception
    {
        administration.restoreBlankInstance();

        _testToolTips("pretty", new PrettyDurationFormatter());
        _testToolTips("days", new ShortDurationFormatter());
        _testToolTips("hours", new ShortDurationFormatter());
    }

    private void _testToolTips(String format, DurationFormatter formatter)
    {
        administration.timeTracking().disable();
        administration.timeTracking().enable("24", "7", format, "hour", TimeTracking.Mode.LEGACY);

        String key = navigation.issue().createIssue("homosapien", "Bug", "time tracking entry " + format);

        setOriginalEstimate(key, "12.15h");

        String origEstimate = "Original Estimate - " + formatter.formatDuration(12L, 9L);
        String remainingEstimate = "Remaining Estimate - " + formatter.formatDuration(12L, 9L);
        String timeSpent = "Time Spent - Not Specified";

        assertTimeTrackingToolTips("orig", origEstimate);
        assertTimeTrackingToolTips("remain", remainingEstimate);
        assertTimeTrackingToolTips("spent", timeSpent);

        navigation.issue().logWork(key, "5h 9m");

        timeSpent = "Time Spent - " + formatter.formatDuration(5L, 9L);
        remainingEstimate = "Remaining Estimate - " + formatter.formatDuration(7L, null);

        assertTimeTrackingToolTips("orig", origEstimate);
        assertTimeTrackingToolTips("remain", remainingEstimate, timeSpent, remainingEstimate);
        assertTimeTrackingToolTips("spent", timeSpent, timeSpent, remainingEstimate);

        navigation.issue().logWork(key, "6.5h", "0h");

        remainingEstimate = "Remaining Estimate - " + formatter.formatDuration(null, 0L);
        timeSpent = "Time Spent - " + formatter.formatDuration(11L, 39L);

        assertTimeTrackingToolTips("orig", origEstimate);
        assertTimeTrackingToolTips("remain", remainingEstimate);
        assertTimeTrackingToolTips("spent", timeSpent, timeSpent, "Time Not Required");

        navigation.issue().logWork(key, "30m", "0m");
        remainingEstimate = "Remaining Estimate - " + formatter.formatDuration(null, 0L);
        timeSpent = "Time Spent - " + formatter.formatDuration(12L, 9L);

        assertTimeTrackingToolTips("orig", origEstimate);
        assertTimeTrackingToolTips("remain", remainingEstimate);
        assertTimeTrackingToolTips("spent", timeSpent);

        navigation.issue().logWork(key, "30m", "0m");
        timeSpent = "Time Spent - " + formatter.formatDuration(12L, 39L);
        assertTimeTrackingToolTips("orig", origEstimate);
        assertTimeTrackingToolTips("remain", remainingEstimate);
        assertTimeTrackingToolTips("spent", timeSpent);
    }

    private void assertTimeTrackingToolTips(String timeTrackingField, String expectedToolTip, String... expectedGraphToolTip)
    {
        final XPathLocator fieldLabelToolTipLocator = new XPathLocator(getTester(), String.format("//*[@id='tt_single_text_%s']/@title", timeTrackingField));
        text.assertTextPresent(fieldLabelToolTipLocator, expectedToolTip);

        final XPathLocator fieldValueToolTipLocator = new XPathLocator(getTester(), String.format("//*[@id='tt_single_values_%s']/@title", timeTrackingField));
        text.assertTextPresent(fieldValueToolTipLocator, expectedToolTip);

        final XPathLocator fieldGraphToolTipLocator = new XPathLocator(getTester(), String.format("//*[@id='tt_single_graph_%s']//img/@title", timeTrackingField));

        if (expectedGraphToolTip.length > 0)
        {
            text.assertTextSequence(fieldGraphToolTipLocator, expectedGraphToolTip);
        }
        else
        {
            text.assertTextSequence(fieldGraphToolTipLocator, expectedToolTip);
        }
    }

    private interface DurationFormatter
    {
        String formatDuration(Long hours, Long minutes);
    }

    private static class PrettyDurationFormatter implements DurationFormatter
    {
        private final String minuteUnit;
        private final String hourUnit;
        private final String separator;

        private PrettyDurationFormatter()
        {
            this(" minutes", " hours", ", ");
        }

        private PrettyDurationFormatter(String minuteUnit, String hourUnit, final String separator)
        {
            this.minuteUnit = minuteUnit;
            this.hourUnit = hourUnit;
            this.separator = separator;
        }

        public String formatDuration(Long hours, Long minutes)
        {
            StringBuilder builder = new StringBuilder();
            if (hours != null)
            {
                builder.append(hours).append(hourUnit);
            }

            if (minutes != null)
            {
                if (builder.length() > 0)
                {
                    builder.append(separator);
                }

                builder.append(minutes).append(minuteUnit);
            }
            return builder.toString();
        }
    }

    private static class ShortDurationFormatter implements DurationFormatter
    {
        private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

        public String formatDuration(Long hours, Long minutes)
        {
            double total = 0;
            if (hours != null)
            {
                total = hours;
            }

            if (minutes != null)
            {
                total += (double) minutes / 60;
            }

            NumberFormat format = DECIMAL_FORMAT;
            return format.format(total);
        }
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