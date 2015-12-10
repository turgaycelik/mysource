package com.atlassian.jira.issue.history;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.log4j.Logger;

/**
 * Implementation of DateRangeBuilder - will work for assignee fields only.
 *
 * @since v4.4
 */
public class AssigneeDateRangeBuilder extends AbstractDateRangeBuilder
{
    private static final Logger log = Logger.getLogger(AssigneeDateRangeBuilder.class);
    private static final String EMPTY_VALUE =  DocumentConstants.ISSUE_UNASSIGNED;

    public AssigneeDateRangeBuilder()
    {
           this(IssueFieldConstants.ASSIGNEE, EMPTY_VALUE);
    }

    public AssigneeDateRangeBuilder(String field, String emptyValue)
    {
        super(field, emptyValue);
    }

    @Override
    protected ChangeHistoryItem createInitialChangeItem(Issue issue)
    {
        final User assignee = issue.getAssignee();
        final String assigneeName =  (assignee == null) ? "" : assignee.getName();
        final String assigneeValue =  issue.getAssigneeId() == null ? EMPTY_VALUE : issue.getAssigneeId();
        return changeItemBuilder(issue).to(assigneeName, assigneeValue).build();
    }
}
