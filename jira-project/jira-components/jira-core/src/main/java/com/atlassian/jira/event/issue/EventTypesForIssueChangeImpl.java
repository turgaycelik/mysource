package com.atlassian.jira.event.issue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdateBean;

public class EventTypesForIssueChangeImpl implements EventTypesForIssueChange
{
    /**
     * This method inspects the {@link com.atlassian.jira.issue.util.IssueUpdateBean} to find out events that should be
     * raised. Adding a new event type requires making changes to this method.
     */
    @Nonnull
    @Override
    public List<Long> getEventTypeIdsForIssueUpdate(@Nonnull final IssueUpdateBean issueUpdateBean)
    {
        List<Long> eventTypeIds = new ArrayList<Long>();
        Collection<ChangeItemBean> changesOnIssue = issueUpdateBean.getChangeItems();
        if (changesOnIssue != null && !changesOnIssue.isEmpty())
        {
            Collection<String> fieldsChanged = extractFields(changesOnIssue);

            eventTypeIds.addAll(getIssueAssignRelatedEvents(fieldsChanged));
            eventTypeIds.addAll(getIssueMovedRelatedEvents(fieldsChanged));
            eventTypeIds.addAll(getCommentRelatedEvents(issueUpdateBean));
        }
        eventTypeIds.add(EventType.ISSUE_UPDATED_ID);
        return eventTypeIds;
    }

    @Nonnull
    private Collection<String> extractFields(@Nonnull Collection<ChangeItemBean> changesOnIssue)
    {
        Collection<String> fields = new ArrayList<String>();
        for (ChangeItemBean change : changesOnIssue)
        {
            fields.add(change.getField());
        }
        return fields;
    }

    @Nonnull
    private List<Long> getIssueAssignRelatedEvents(@Nonnull Collection<String> fieldsChanged)
    {
        if (fieldsChanged.contains(IssueFieldConstants.ASSIGNEE))
        {
            return Arrays.asList(EventType.ISSUE_ASSIGNED_ID);
        }
        return Collections.emptyList();
    }

    @Nonnull
    private List<Long> getIssueMovedRelatedEvents(@Nonnull Collection<String> fieldsChanged)
    {
        if (fieldsChanged.contains(IssueFieldConstants.PROJECT))
        {
            return Arrays.asList(EventType.ISSUE_MOVED_ID);
        }
        return Collections.emptyList();
    }

    @Nonnull
    private List<Long> getCommentRelatedEvents(@Nonnull IssueUpdateBean issueUpdateBean)
    {
        if (EventType.ISSUE_COMMENT_DELETED_ID.equals(issueUpdateBean.getEventTypeId()))
        {
            return Arrays.asList(EventType.ISSUE_COMMENT_DELETED_ID);
        }
        return Collections.emptyList();
    }
}
