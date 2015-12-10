/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.event.MockEventPublisher;
import com.atlassian.jira.event.issue.DefaultIssueEventBundle;
import com.atlassian.jira.event.issue.DefaultIssueEventManager;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.issue.IssueEventParamsTransformer;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.issue.JiraIssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.MockComment;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.workflow.MockWorkflowContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;

import com.opensymphony.workflow.loader.FunctionDescriptor;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestFireIssueEventFunction
{
    private MockEventPublisher mockEventPublisher;
    private MockUserManager mockUserManager;

    private FireIssueEventFunction fireIssueEventFunction;

    @Before
    public void setUp()
    {
        mockEventPublisher = new MockEventPublisher();
        mockUserManager = new MockUserManager();
        fireIssueEventFunction = new FireIssueEventFunction();
    }

    @Test
    public void executeFiresIssueEventCorrectly() throws Exception
    {
        bindMocks(
                new DefaultIssueEventManager(mock(IssueEventParamsTransformer.class), mockEventPublisher),
                dummyIssueEventBundleFactory(),
                mockUserManager
        );

        ApplicationUser user = new MockApplicationUser("fred");
        mockUserManager.addUser(user);

        Map<String, Object> transientVars = createTransientVars(
                new MockIssue(12, "ABC-123"),
                new MockComment("fred", "Polly want a cracker"),
                user,
                null,
                null
        );
        Map args = EasyMap.build("eventTypeId", EventType.ISSUE_CREATED_ID);
        fireIssueEventFunction.execute(transientVars, args, null);

        IssueEvent event = (IssueEvent) mockEventPublisher.getEvents().get(0);
        assertEquals(EventType.ISSUE_CREATED_ID, event.getEventTypeId());
        assertEquals("ABC-123", event.getIssue().getKey());
        assertEquals("Polly want a cracker", event.getComment().getBody());
        assertEquals("fred", event.getUser().getName());
        assertTrue(event.isRedundant());
    }

    @Test
    public void executeFiresIssueEventBundleCorrectly()
    {
        IssueEventManager issueEventManager = mock(IssueEventManager.class);
        IssueEventBundleFactory issueEventBundleFactory = mock(IssueEventBundleFactory.class);
        bindMocks(issueEventManager, issueEventBundleFactory, mockUserManager);

        ApplicationUser user = new MockApplicationUser("fred");
        mockUserManager.addUser(user);

        Issue issue = mock(Issue.class);
        Comment comment = mock(Comment.class);
        GenericValue changeGroup = new MockGenericValue("ChangeGroup");
        Long eventTypeId = EventType.ISSUE_CREATED_ID;
        boolean sendMail = true;
        String originalAssigneeId = "assignee";

        IssueEventBundle issueEventBundle = mock(IssueEventBundle.class);
        when(issueEventBundleFactory.createWorkflowEventBundle(eventTypeId, issue, user, comment, changeGroup, expectedEventParams(), sendMail, originalAssigneeId)).thenReturn(issueEventBundle);

        Map<String, Object> transientVars = createTransientVars(issue, comment, user, changeGroup, originalAssigneeId);
        Map args = EasyMap.build("eventTypeId", eventTypeId);
        fireIssueEventFunction.execute(transientVars, args, null);

        verify(issueEventManager).dispatchEvent(issueEventBundle);
    }

    @Test
    public void makeDescriptor()
    {
        final FunctionDescriptor descriptor = FireIssueEventFunction.makeDescriptor(EventType.ISSUE_CREATED_ID);
        assertEquals("com.atlassian.jira.workflow.function.event.FireIssueEventFunction", descriptor.getArgs().get("class.name"));
        assertEquals(EventType.ISSUE_CREATED_ID, descriptor.getArgs().get("eventTypeId"));
    }

    private IssueEventBundleFactory dummyIssueEventBundleFactory()
    {
        IssueEventBundleFactory issueEventBundleFactory = mock(IssueEventBundleFactory.class);
        when(issueEventBundleFactory.createWorkflowEventBundle(
                        anyLong(),
                        any(Issue.class),
                        any(ApplicationUser.class),
                        any(Comment.class),
                        any(GenericValue.class),
                        anyMapOf(String.class, Object.class),
                        anyBoolean(),
                        anyString())
        ).thenReturn(DefaultIssueEventBundle.create(Collections.<JiraIssueEvent>emptyList()));
        return issueEventBundleFactory;
    }

    private void bindMocks(IssueEventManager issueEventManager, IssueEventBundleFactory issueEventBundleFactory, UserManager userManager)
    {
        new MockComponentWorker()
                .init()
                .addMock(IssueEventManager.class, issueEventManager)
                .addMock(IssueEventBundleFactory.class, issueEventBundleFactory)
                .addMock(UserManager.class, userManager);
    }

    private Map<String, Object> createTransientVars(Issue issue, Comment comment, ApplicationUser user, GenericValue changeGroup, String originalAssigneeId)
    {
        final Map<String, Object> transientVars = new HashMap<String, Object>();
        transientVars.put("issue", issue);
        transientVars.put("commentValue", comment);
        transientVars.put("changeGroup", changeGroup);
        transientVars.put("context", new MockWorkflowContext(user.getUsername()));
        transientVars.put("originalAssigneeId", originalAssigneeId);
        return transientVars;
    }

    private Map<String, Object> expectedEventParams()
    {
        Map<String, Object> params = new HashMap<String, Object>(4);
        params.put("eventsource", IssueEventSource.WORKFLOW);
        return params;
    }
}
