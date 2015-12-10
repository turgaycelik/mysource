package com.atlassian.jira.event.web.action.browser;

import com.atlassian.analytics.api.annotations.Analytics;

/**
 * Denotes that a report has been generated and viewed by a user.
 *
 * @since v5.2
 */
@Analytics("report.viewed")
public class ReportViewedEvent
{
    private final String reportKey;

    public ReportViewedEvent(final String reportKey)
    {
        this.reportKey = reportKey;
    }

    public String getReportKey()
    {
        return reportKey;
    }
}
