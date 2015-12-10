package com.atlassian.jira.event.web.action.browser;

import com.atlassian.analytics.api.annotations.Analytics;

/**
 * Denotes that the user has opened the configure report page.
 * <p/>
 * We log this event separately to see whether people open this page without actually running a report.
 *
 * @since v5.2
 */
@Analytics ("report.configured")
public class ReportConfiguredEvent
{
    private final String reportKey;

    public ReportConfiguredEvent(final String reportKey)
    {
        this.reportKey = reportKey;
    }

    public String getReportKey()
    {
        return reportKey;
    }
}
