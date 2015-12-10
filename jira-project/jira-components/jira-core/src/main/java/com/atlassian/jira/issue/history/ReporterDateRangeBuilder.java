package com.atlassian.jira.issue.history;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.log4j.Logger;

/**
 * Implementation of DateRangeBuilder - will work for reporter fields only.
 *
 * @since v4.4
 */
public class ReporterDateRangeBuilder extends AbstractDateRangeBuilder
{
    private static final Logger log = Logger.getLogger(ReporterDateRangeBuilder.class);
    private static final String EMPTY_VALUE = DocumentConstants.ISSUE_NO_AUTHOR;

    public ReporterDateRangeBuilder()
    {
        this(IssueFieldConstants.REPORTER, EMPTY_VALUE);
    }

    public ReporterDateRangeBuilder(String field, String emptyValue)
    {
        super(field, emptyValue);
    }

    @Override
    protected ChangeHistoryItem createInitialChangeItem(Issue issue)
    {
        final User reporter = issue.getReporter();
        final String reporterName = reporter == null ? "" : issue.getReporter().getDisplayName();
        final String reporterValue = issue.getReporterId() == null ? EMPTY_VALUE : issue.getReporterId();
        return changeItemBuilder(issue).to(reporterName, reporterValue).build();
    }
}
