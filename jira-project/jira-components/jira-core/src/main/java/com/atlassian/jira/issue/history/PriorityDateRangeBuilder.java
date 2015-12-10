package com.atlassian.jira.issue.history;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.priority.Priority;
import org.apache.log4j.Logger;

/**
 * Implementation  implementation of DateRangeBuilder - will work for status fields only.
 *
 * @since v4.4
 */
public class PriorityDateRangeBuilder extends AbstractDateRangeBuilder
{
    private static final Logger log = Logger.getLogger(PriorityDateRangeBuilder.class);
    private static final String EMPTY_VALUE = "-1";

    public PriorityDateRangeBuilder()
    {
        this(IssueFieldConstants.PRIORITY, EMPTY_VALUE);
    }

    public PriorityDateRangeBuilder(String field, final String emptyValue)
    {
        super(field, emptyValue);
    }

    @Override
    protected ChangeHistoryItem createInitialChangeItem(Issue issue)
    {
        final Priority priority = issue.getPriorityObject();
        final String priorityName =  priority == null ? EMPTY_VALUE : priority.getName();
        final String priorityValue = priority == null ? EMPTY_VALUE : priority.getId();
        return changeItemBuilder(issue).to(priorityName, priorityValue).build();
    }
}
