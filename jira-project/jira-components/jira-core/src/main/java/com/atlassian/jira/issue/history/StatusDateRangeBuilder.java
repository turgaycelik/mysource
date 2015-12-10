package com.atlassian.jira.issue.history;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.status.Status;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Implementation  implementation of DateRangeBuilder - will work for status fields only.
 *
 * @since v4.4
 */
public class StatusDateRangeBuilder extends AbstractDateRangeBuilder
{
    private static final Logger log = Logger.getLogger(StatusDateRangeBuilder.class);
    private static final String EMPTY_VALUE = "-1";

    public StatusDateRangeBuilder()
    {
        this(IssueFieldConstants.STATUS);
    }

    public StatusDateRangeBuilder(String field)
    {
        super(field, EMPTY_VALUE);
    }

    @Override
    protected ChangeHistoryItem createInitialChangeItem(Issue issue)
    {
        final Status status = issue.getStatusObject();
        final String statusName =  status == null ? null : status.getName();
        final String statusValue = status == null ? EMPTY_VALUE : status.getId();
        return changeItemBuilder(issue).to(statusName, statusValue).build();
    }
}
