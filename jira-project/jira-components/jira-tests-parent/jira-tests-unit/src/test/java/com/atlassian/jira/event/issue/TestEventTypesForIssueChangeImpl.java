package com.atlassian.jira.event.issue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.user.ApplicationUser;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestEventTypesForIssueChangeImpl
{
    private EventTypesForIssueChange factory;

    @Before
    public void setUp()
    {
        factory = new EventTypesForIssueChangeImpl();
    }
    
    @Test
    public void issueUpdatedEventIsTriggeredWhenTheListOfChangesOnTheIssueIsNull()
    {
        List<Long> events = factory.getEventTypeIdsForIssueUpdate(issueUpdateWithNullAsListOfChanges());

        assertEventsArePresent(events, EventType.ISSUE_UPDATED_ID);
    }
    
    @Test
    public void issueUpdatedEventIsTriggeredWhenTheListOfChangesOnTheIssueIsEmpty()
    {
        List<Long> events = factory.getEventTypeIdsForIssueUpdate(issueUpdateWithEmptyListAsListOfChanges());

        assertEventsArePresent(events, EventType.ISSUE_UPDATED_ID);
    }

    @Test
    public void changingAssigneeShouldTriggerIssueUpdatedAndAssigned()
    {
        IssueUpdateBean issueUpdateBean = issueChangesOn(IssueFieldConstants.ASSIGNEE);

        List<Long> events = factory.getEventTypeIdsForIssueUpdate(issueUpdateBean);

        assertEventsArePresent(events,EventType.ISSUE_ASSIGNED_ID, EventType.ISSUE_UPDATED_ID);
    }
    
    @Test
    public void changingProjectShouldTriggerIssueMovedEvent()
    {
        IssueUpdateBean issueUpdateBean = issueChangesOn(IssueFieldConstants.PROJECT);

        List<Long> events = factory.getEventTypeIdsForIssueUpdate(issueUpdateBean);

        assertEventsArePresent(events,EventType.ISSUE_MOVED_ID, EventType.ISSUE_UPDATED_ID);
    }
    
    @Test
    public void issueUpdatedShouldBeTriggeredWhenTheChangedFieldDoesNotHaveASpecificEvent()
    {
        IssueUpdateBean issueUpdateBean = issueChangesOn(IssueFieldConstants.COMPONENTS);

        List<Long> events = factory.getEventTypeIdsForIssueUpdate(issueUpdateBean);

        assertEventsArePresent(events, EventType.ISSUE_UPDATED_ID);
    }

    @Test
    public void issueUpdatedEventMustBeTheLastOne()
    {
        IssueUpdateBean issueUpdateBean = issueChangesOn(IssueFieldConstants.ASSIGNEE);

        List<Long> events = factory.getEventTypeIdsForIssueUpdate(issueUpdateBean);

        assertThat(
                "The issue updated event must be the last one. We want the most specific events to be triggered first",
                events,
                contains(EventType.ISSUE_ASSIGNED_ID, EventType.ISSUE_UPDATED_ID)
        );
    }
    
    @Test
    public void changingWorklogIdDoesNotTriggerAnySpecificWorklogEventIfTheIssueUpdateBeanContainsNoWorklog()
    {
        IssueUpdateBean issueUpdateBean = issueChangesOn(IssueFieldConstants.WORKLOG_ID, null);

        List<Long> events = factory.getEventTypeIdsForIssueUpdate(issueUpdateBean);

        assertEventsArePresent(events, EventType.ISSUE_UPDATED_ID);
    }
    
    @Test
    public void changingWorklogIdDoesNotTriggerAnySpecificWorklogEventIfTheWorklogHasNoDates()
    {
        Worklog worklog = worklogWithDates(null, null);
        IssueUpdateBean issueUpdateBean = issueChangesOn(IssueFieldConstants.WORKLOG_ID, worklog);

        List<Long> events = factory.getEventTypeIdsForIssueUpdate(issueUpdateBean);

        assertEventsArePresent(events, EventType.ISSUE_UPDATED_ID);
    }

    @Test
    public void deletingACommentShouldTriggerCommentDeletedEvent()
    {
        IssueUpdateBean issueUpdateBean = issueUpdateBeanWithDeletedComment();

        List<Long> events = factory.getEventTypeIdsForIssueUpdate(issueUpdateBean);

        assertEventsArePresent(events, EventType.ISSUE_COMMENT_DELETED_ID, EventType.ISSUE_UPDATED_ID);
    }

    private IssueUpdateBean issueUpdateWithNullAsListOfChanges()
    {
        IssueUpdateBean issueUpdateBean = anyIssueUpdateBean();
        issueUpdateBean.setChangeItems(null);
        return issueUpdateBean;
    }
    
    private IssueUpdateBean issueUpdateWithEmptyListAsListOfChanges()
    {
        IssueUpdateBean issueUpdateBean = anyIssueUpdateBean();
        issueUpdateBean.setChangeItems(Collections.<ChangeItemBean>emptyList());
        return issueUpdateBean;
    }

    private IssueUpdateBean issueChangesOn(String... fields)
    {
        IssueUpdateBean issueUpdateBean = anyIssueUpdateBean();
        issueUpdateBean.setChangeItems(buildChanges(fields));
        return issueUpdateBean;
    }

    private List<ChangeItemBean> buildChanges(String... fields)
    {
        List<ChangeItemBean> changeItemBeans = new ArrayList<ChangeItemBean>();
        for (String field : fields) {
            ChangeItemBean change = new ChangeItemBean();
            change.setField(field);
            changeItemBeans.add(change);
        }
        return changeItemBeans;
    }

    private IssueUpdateBean issueChangesOn(final String field, final Worklog worklog)
    {
        IssueUpdateBean issueUpdateBean = issueChangesOn(field);
        issueUpdateBean.setWorklog(worklog);
        return issueUpdateBean;
    }

    private IssueUpdateBean issueUpdateBeanWithDeletedComment()
    {
        IssueUpdateBean issueUpdateBean = issueUpdateBeanWithEventType(EventType.ISSUE_COMMENT_DELETED_ID);
        issueUpdateBean.setChangeItems(buildChanges("Comment"));
        return issueUpdateBean;
    }

    private IssueUpdateBean anyIssueUpdateBean()
    {
        return issueUpdateBeanWithEventType(1L);
    }

    private IssueUpdateBean issueUpdateBeanWithEventType(long eventType)
    {
        return new IssueUpdateBean(mock(Issue.class), mock(Issue.class), eventType, mock(ApplicationUser.class));
    }

    private Worklog worklogWithDates(Date createdDate, Date updatedDate)
    {
        Worklog worklog = mock(Worklog.class);
        when(worklog.getCreated()).thenReturn(createdDate);
        when(worklog.getUpdated()).thenReturn(updatedDate);
        return worklog;
    }

    private void assertEventsArePresent(List<Long> eventIds, Long... expectedEventIds)
    {
        assertThat(eventIds.size(), is(expectedEventIds.length));
        assertThat(eventIds, hasItems(expectedEventIds));
    }
}
