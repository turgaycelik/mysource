package com.atlassian.jira.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.event.issue.DelegatingJiraIssueEvent;
import com.atlassian.jira.event.issue.EventTypesForIssueChange;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventBundleFactoryImpl;
import com.atlassian.jira.event.issue.IssueEventParamsTransformer;
import com.atlassian.jira.event.issue.JiraIssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.managers.DefaultIssueDeleteHelper;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestIssueEventBundleFactoryImpl
{
    @Mock
    private IssueEventParamsTransformer paramsTransformer;
    @Mock
    private EventTypesForIssueChange eventsForIssueChange;

    private IssueEventBundleFactory factory;

    @Before
    public void setUp()
    {
        factory = new IssueEventBundleFactoryImpl(paramsTransformer, eventsForIssueChange);
    }

    @Test
    public void createIssueUpdateEventBundleCreatesABundleCorrectlyWhenOnlyOneEventShouldBePublished()
    {
        Issue issue = mock(Issue.class);
        GenericValue changeGroup = new MockGenericValue("ChangeGroup");
        ApplicationUser user = mock(ApplicationUser.class);
        IssueUpdateBean issueUpdateBean = newIssueUpdateBean(issue, user);

        when(eventsForIssueChange.getEventTypeIdsForIssueUpdate(issueUpdateBean)).thenReturn(ImmutableList.of(EventType.ISSUE_UPDATED_ID));

        Map<String, Object> transformedParams = Collections.emptyMap();
        when(paramsTransformer.transformParams(issueUpdateBean.getParams())).thenReturn(transformedParams);

        IssueEventBundle issueUpdateEventBundle = factory.createIssueUpdateEventBundle(issue, changeGroup, issueUpdateBean, user);

        Collection<JiraIssueEvent> events = issueUpdateEventBundle.getEvents();
        assertThat(events.size(), is(1));

        IssueEvent issueEvent = extractIssueEventAtPosition(issueUpdateEventBundle, 0);
        assertThat(issueEvent.getEventTypeId(), is(EventType.ISSUE_UPDATED_ID));
        assertThat(issueEvent.getIssue(), is(issue));
        assertThat(issueEvent.getUser(), is(ApplicationUsers.toDirectoryUser(user)));
        assertThat(issueEvent.getComment(), is(issueUpdateBean.getComment()));
        assertThat(issueEvent.getWorklog(), is(issueUpdateBean.getWorklog()));
        assertThat(issueEvent.getChangeLog(), is(changeGroup));
        assertThat(issueEvent.getParams(), is(transformedParams));
        assertThat(issueEvent.isSendMail(), is(issueUpdateBean.isSendMail()));
        assertThat(issueEvent.isSubtasksUpdated(), is(issueUpdateBean.isSubtasksUpdated()));
    }

    @Test
    public void createIssueUpdateEventBundleCreatesABundleCorrectlyWhenSeveralEventsShouldBePublished()
    {
        List<Long> eventsToBePublished = ImmutableList.of(EventType.ISSUE_UPDATED_ID, EventType.ISSUE_ASSIGNED_ID);
        when(eventsForIssueChange.getEventTypeIdsForIssueUpdate(any(IssueUpdateBean.class))).thenReturn(eventsToBePublished);

        IssueEventBundle issueUpdateEventBundle = factory.createIssueUpdateEventBundle(mock(Issue.class), new MockGenericValue("ChangeGroup"), mock(IssueUpdateBean.class), mock(ApplicationUser.class));

        assertThat(extractEventTypeIds(issueUpdateEventBundle), containsInAnyOrder(EventType.ISSUE_UPDATED_ID, EventType.ISSUE_ASSIGNED_ID));
    }

    @Test
    public void createIssueDeleteEventBundleCreatesABundleCorrectly()
    {
        Issue issue = mock(Issue.class);
        ApplicationUser user = mock(ApplicationUser.class);
        DefaultIssueDeleteHelper.DeletedIssueEventData deletedIssueEventData = mock(DefaultIssueDeleteHelper.DeletedIssueEventData.class);

        Map<String, Object> transformedParams = Collections.emptyMap();
        when(paramsTransformer.transformParams(deletedIssueEventData.paramsMap())).thenReturn(transformedParams);

        IssueEventBundle issueUpdateEventBundle = factory.createIssueDeleteEventBundle(issue, deletedIssueEventData, user);

        Collection<JiraIssueEvent> events = issueUpdateEventBundle.getEvents();
        assertThat(events.size(), is(1));

        IssueEvent issueEvent = extractIssueEventAtPosition(issueUpdateEventBundle, 0);
        assertThat(issueEvent.getEventTypeId(), is(EventType.ISSUE_DELETED_ID));
        assertThat(issueEvent.getIssue(), is(issue));
        assertThat(issueEvent.getUser(), is(ApplicationUsers.toDirectoryUser(user)));
        assertThat(issueEvent.getParams(), is(transformedParams));
        assertThat(issueEvent.isSendMail(), is(deletedIssueEventData.isSendMail()));
    }

    @Test
    public void createCommentAddedBundleCreatesABundleCorrectly()
    {
        Issue issue = mock(Issue.class);
        ApplicationUser user = mock(ApplicationUser.class);
        Comment comment = mock(Comment.class);
        Map<String, Object> params = new HashMap<String, Object>();

        Map<String, Object> transformedParams = Collections.emptyMap();
        when(paramsTransformer.transformParams(params)).thenReturn(transformedParams);

        IssueEventBundle issueUpdateEventBundle = factory.createCommentAddedBundle(issue, user, comment, params);

        Collection<JiraIssueEvent> events = issueUpdateEventBundle.getEvents();
        assertThat(events.size(), is(1));

        IssueEvent issueEvent = extractIssueEventAtPosition(issueUpdateEventBundle, 0);
        assertThat(issueEvent.getEventTypeId(), is(EventType.ISSUE_COMMENTED_ID));
        assertThat(issueEvent.getIssue(), is(issue));
        assertThat(issueEvent.getUser(), is(ApplicationUsers.toDirectoryUser(user)));
        assertThat(issueEvent.getParams(), is(transformedParams));
        assertThat(issueEvent.getComment(), is(comment));
    }

    @Test
    public void createCommentEditedBundleCreatesABundleCorrectly()
    {
        Issue issue = mock(Issue.class);
        ApplicationUser user = mock(ApplicationUser.class);
        Comment comment = mock(Comment.class);
        Map<String, Object> params = new HashMap<String, Object>();

        Map<String, Object> transformedParams = Collections.emptyMap();
        when(paramsTransformer.transformParams(params)).thenReturn(transformedParams);

        IssueEventBundle issueUpdateEventBundle = factory.createCommentEditedBundle(issue, user, comment, params);

        Collection<JiraIssueEvent> events = issueUpdateEventBundle.getEvents();
        assertThat(events.size(), is(1));

        IssueEvent issueEvent = extractIssueEventAtPosition(issueUpdateEventBundle, 0);
        assertThat(issueEvent.getEventTypeId(), is(EventType.ISSUE_COMMENT_EDITED_ID));
        assertThat(issueEvent.getIssue(), is(issue));
        assertThat(issueEvent.getUser(), is(ApplicationUsers.toDirectoryUser(user)));
        assertThat(issueEvent.getParams(), is(transformedParams));
        assertThat(issueEvent.getComment(), is(comment));
    }
    
    @Test
    public void createWorkflowEventBundleCreatesABundleCorrectlyWhenIssueDoesNotHaveAnAssignee()
    {
        Long eventType = EventType.ISSUE_CREATED_ID;
        Issue issue = mock(Issue.class);
        ApplicationUser user = mock(ApplicationUser.class);
        Comment comment = mock(Comment.class);
        GenericValue changeGroup = new MockGenericValue("ChangeGroup");
        Map<String, Object> params = new HashMap<String, Object>();
        boolean sendMail = true;
        String anyAssigneeId = "assignee";

        Map<String, Object> transformedParams = Collections.emptyMap();
        when(paramsTransformer.transformParams(params)).thenReturn(transformedParams);

        IssueEventBundle issueUpdateEventBundle = factory.createWorkflowEventBundle(eventType, issue, user, comment, changeGroup, params, sendMail, anyAssigneeId);

        Collection<JiraIssueEvent> events = issueUpdateEventBundle.getEvents();
        assertThat(events.size(), is(1));

        IssueEvent issueEvent = extractIssueEventAtPosition(issueUpdateEventBundle, 0);
        assertThat(issueEvent.getEventTypeId(), is(eventType));
        assertThat(issueEvent.getIssue(), is(issue));
        assertThat(issueEvent.getUser(), is(ApplicationUsers.toDirectoryUser(user)));
        assertThat(issueEvent.getParams(), is(transformedParams));
        assertThat(issueEvent.getComment(), is(comment));
        assertThat(issueEvent.getChangeLog(), is(changeGroup));
        assertThat(issueEvent.isSendMail(), is(sendMail));
    }
    
    @Test
    public void createWorkflowEventBundleCreatesABundleCorrectlyWhenIssueAssigneeHasNotBeenChanged()
    {
        Long eventType = EventType.ISSUE_CREATED_ID;
        String assigneeId = "assignee";
        Issue issue = issueWithAssigneeId(assigneeId);
        ApplicationUser user = mock(ApplicationUser.class);
        Comment comment = mock(Comment.class);
        GenericValue changeGroup = new MockGenericValue("ChangeGroup");
        Map<String, Object> params = new HashMap<String, Object>();
        boolean sendMail = true;

        Map<String, Object> transformedParams = Collections.emptyMap();
        when(paramsTransformer.transformParams(params)).thenReturn(transformedParams);

        IssueEventBundle issueUpdateEventBundle = factory.createWorkflowEventBundle(eventType, issue, user, comment, changeGroup, params, sendMail, assigneeId);

        Collection<JiraIssueEvent> events = issueUpdateEventBundle.getEvents();
        assertThat(events.size(), is(1));

        IssueEvent issueEvent = extractIssueEventAtPosition(issueUpdateEventBundle, 0);
        assertThat(issueEvent.getEventTypeId(), is(eventType));
    }

    @Test
    public void createWorkflowEventBundleCreatesABundleWithAnIssueAssignedEventWhenIssueAssigneeHasChanged()
    {
        Long eventType = EventType.ISSUE_CREATED_ID;
        String originalAssignee = "original";
        String newAssignee = "new";
        Issue issue = issueWithAssigneeId(originalAssignee);
        ApplicationUser user = mock(ApplicationUser.class);
        Comment comment = mock(Comment.class);
        GenericValue changeGroup = new MockGenericValue("ChangeGroup");
        Map<String, Object> params = new HashMap<String, Object>();
        boolean sendMail = true;

        Map<String, Object> transformedParams = Collections.emptyMap();
        when(paramsTransformer.transformParams(params)).thenReturn(transformedParams);

        IssueEventBundle issueUpdateEventBundle = factory.createWorkflowEventBundle(eventType, issue, user, comment, changeGroup, params, sendMail, newAssignee);

        Collection<JiraIssueEvent> events = issueUpdateEventBundle.getEvents();
        assertThat(events.size(), is(2));

        IssueEvent issueEvent = extractIssueEventAtPosition(issueUpdateEventBundle, 1);
        assertThat(issueEvent.getEventTypeId(), is(EventType.ISSUE_ASSIGNED_ID));
    }

    @Test
    public void wrapInBundle()
    {
        IssueEvent event = issueEvent();

        IssueEventBundle bundle = factory.wrapInBundle(event);

        assertThat(bundle.getEvents().size(), is(1));
        assertThat(extractIssueEventAtPosition(bundle, 0), is(event));
    }

    @Test
    public void createWorklogEventBundleCreatesABundleCorrectly()
    {
        Issue issue = mock(Issue.class);
        GenericValue changeGroup = new MockGenericValue("ChangeGroup");
        ApplicationUser user = mock(ApplicationUser.class);
        IssueUpdateBean issueUpdateBean = newIssueUpdateBean(issue, user, EventType.ISSUE_WORKLOG_UPDATED_ID);

        Map<String, Object> transformedParams = Collections.emptyMap();
        when(paramsTransformer.transformParams(issueUpdateBean.getParams())).thenReturn(transformedParams);

        IssueEventBundle issueUpdateEventBundle = factory.createWorklogEventBundle(issue, changeGroup, issueUpdateBean, user);

        Collection<JiraIssueEvent> events = issueUpdateEventBundle.getEvents();
        assertThat(events.size(), is(1));

        IssueEvent issueEvent = extractIssueEventAtPosition(issueUpdateEventBundle, 0);
        assertThat(issueEvent.getEventTypeId(), is(EventType.ISSUE_WORKLOG_UPDATED_ID));
        assertThat(issueEvent.getIssue(), is(issue));
        assertThat(issueEvent.getUser(), is(ApplicationUsers.toDirectoryUser(user)));
        assertThat(issueEvent.getComment(), is(issueUpdateBean.getComment()));
        assertThat(issueEvent.getWorklog(), is(issueUpdateBean.getWorklog()));
        assertThat(issueEvent.getChangeLog(), is(changeGroup));
        assertThat(issueEvent.getParams(), is(transformedParams));
        assertThat(issueEvent.isSendMail(), is(issueUpdateBean.isSendMail()));
        assertThat(issueEvent.isSubtasksUpdated(), is(issueUpdateBean.isSubtasksUpdated()));
    }

    private IssueEvent issueEvent()
    {
        return new IssueEvent(null, null, null, null);
    }

    private IssueUpdateBean newIssueUpdateBean(Issue issue, ApplicationUser user)
    {
        return newIssueUpdateBean(issue, user, EventType.ISSUE_UPDATED_ID);
    }

    private IssueUpdateBean newIssueUpdateBean(Issue issue, ApplicationUser user, Long eventType)
    {
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(issue, issue, eventType, user);
        issueUpdateBean.setComment(mock(Comment.class));
        issueUpdateBean.setWorklog(mock(Worklog.class));
        issueUpdateBean.setParams(Collections.emptyMap());
        return issueUpdateBean;
    }

    private IssueEvent extractIssueEventAtPosition(IssueEventBundle bundle, int position)
    {
        Collection<JiraIssueEvent> events = bundle.getEvents();
        return ((DelegatingJiraIssueEvent) new ArrayList<JiraIssueEvent>(events).get(position)).asIssueEvent();
    }

    private Set<Long> extractEventTypeIds(IssueEventBundle bundle)
    {
        Set<Long> eventTypeIds = new HashSet<Long>();
        for (JiraIssueEvent events : bundle.getEvents())
        {
            eventTypeIds.add(((DelegatingJiraIssueEvent) events).asIssueEvent().getEventTypeId());
        }
        return eventTypeIds;
    }

    private Issue issueWithAssigneeId(String assigneeId)
    {
        Issue issue = mock(Issue.class);
        when(issue.getAssigneeId()).thenReturn(assigneeId);
        return issue;
    }
}
