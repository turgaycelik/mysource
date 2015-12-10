package com.atlassian.jira.bulkedit.operation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.workflow.WorkflowManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TestBulkMoveOperation
{
    @Rule public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    // construct operation object:
    @Mock WorkflowManager workflowManager;
    @Mock ProjectManager projectManager;
    @Mock FieldManager fieldManager;
    @Mock IssueFactory issueFactory;
    @Mock IssueManager issueManager;
    @Mock AttachmentPathManager attachmentPathManager;
    @Mock IssueEventManager issueEventManager;
    @Mock AttachmentManager attachmentManager;
    @Mock IssueEventBundleFactory issueEventBundleFactory;

    @Mock BulkEditBean bulkEditBean;
    @Mock ApplicationUser applicationUser;

    @Mock @AvailableInContainer
    PermissionManager permissionManager;

    BulkMoveOperationImpl bulkMoveOperation = null;

    @Before
    public void setUp()
    {
        bulkMoveOperation = new BulkMoveOperationImpl(workflowManager, projectManager, fieldManager, issueFactory, issueManager, issueEventManager, null, attachmentManager, issueEventBundleFactory);
    }

    @Test
    public void moveCanBePerformedWhenPermitted()
    {
        // given:
        final List<Issue> issues = ImmutableList.of(
                new MockIssueBuilder().permission(true).build(),
                new MockIssueBuilder().permission(true).build(),
                new MockIssueBuilder().permission(true)
                        .subTask(new MockIssueBuilder().permission(true).build())
                        .subTask(new MockIssueBuilder().permission(true).build())
                        .build());
        when(bulkEditBean.getSelectedIssues()).thenReturn(issues);

        // then:
        assertThat("All issues are movable.", bulkMoveOperation.canPerform(bulkEditBean, applicationUser), Matchers.is(true));
    }

    @Test
    public void testMoveCannotBePerformedWhenNotPermitted()
    {
        // given:
        final List<Issue> issues = ImmutableList.of(
                new MockIssueBuilder().permission(true).build(),
                new MockIssueBuilder().permission(false).build());
        when(bulkEditBean.getSelectedIssues()).thenReturn(issues);

        // then:
        assertThat("No permission for one of the issues should reject move.",
                bulkMoveOperation.canPerform(bulkEditBean, applicationUser), Matchers.is(false));
    }

    @Test
    public void testMoveCannotBePerformedWhenNotPermittedForSubTasks()
    {
        // given:
        final Issue issue = new MockIssueBuilder().permission(true)
                .subTask(new MockIssueBuilder().permission(true).build())
                .subTask(new MockIssueBuilder().permission(false).build())
                .build();
        when(bulkEditBean.getSelectedIssues()).thenReturn(Collections.singletonList(issue));

        // then:
        assertThat("No permission for sub task should reject move.",
                bulkMoveOperation.canPerform(bulkEditBean, applicationUser), Matchers.is(false));
    }

    @Test
    public void noEventsAreDispatchedIfUpdateLogIsNull()
    {
        Issue issue = mock(Issue.class);
        GenericValue updateLog = null;
        boolean subtasksUpdated = true;
        User user = mock(User.class);
        boolean sendEmail = true;
        boolean issueWasMoved = true;

        bulkMoveOperation.dispatchEvents(
                issue,
                updateLog,
                issueChangeHolderWith(Arrays.asList(mock(ChangeItemBean.class)), subtasksUpdated),
                bulkEditBeanWith(sendEmail),
                applicationUserWith(user),
                issueWasMoved
        );

        verify(issueEventManager, never()).dispatchRedundantEvent(anyLong(), any(Issue.class), any(User.class), any(GenericValue.class), anyBoolean(), anyBoolean());
        verify(issueEventManager, never()).dispatchEvent(any(IssueEventBundle.class));
    }

    @Test
    public void noEventsAreDispatchedIfTheListOfChangedFieldsIsEmpty()
    {
        Issue issue = mock(Issue.class);
        GenericValue updateLog = new MockGenericValue("ChangeGroup");
        boolean subtasksUpdated = true;
        User user = mock(User.class);
        boolean sendEmail = true;
        boolean issueWasMoved = true;

        bulkMoveOperation.dispatchEvents(
                issue,
                updateLog,
                issueChangeHolderWith(Collections.<ChangeItemBean>emptyList(), subtasksUpdated),
                bulkEditBeanWith(sendEmail),
                applicationUserWith(user),
                issueWasMoved
        );

        verify(issueEventManager, never()).dispatchRedundantEvent(anyLong(), any(Issue.class), any(User.class), any(GenericValue.class), anyBoolean(), anyBoolean());
        verify(issueEventManager, never()).dispatchEvent(any(IssueEventBundle.class));
    }

    @Test
    public void issueMovedEventIsDispatchedIfTheIssueHasBeenMoved()
    {
        Issue issue = mock(Issue.class);
        GenericValue updateLog = new MockGenericValue("ChangeGroup");
        boolean subtasksUpdated = true;
        User user = mock(User.class);
        boolean sendEmail = true;
        boolean issueWasMoved = true;

        bulkMoveOperation.dispatchEvents(
                issue,
                updateLog,
                issueChangeHolderWith(Arrays.asList(mock(ChangeItemBean.class)), subtasksUpdated),
                bulkEditBeanWith(sendEmail),
                applicationUserWith(user),
                issueWasMoved
        );

        verify(issueEventManager).dispatchRedundantEvent(EventType.ISSUE_MOVED_ID, issue, user, updateLog, sendEmail, subtasksUpdated);
    }

    @Test
    public void issueUpdatedEventIsDispatchedIfTheIssueHasNotBeenMoved()
    {
        Issue issue = mock(Issue.class);
        GenericValue updateLog = new MockGenericValue("ChangeGroup");
        boolean subtasksUpdated = true;
        User user = mock(User.class);
        boolean sendEmail = true;
        boolean issueWasMoved = false;

        bulkMoveOperation.dispatchEvents(
                issue,
                updateLog,
                issueChangeHolderWith(Arrays.asList(mock(ChangeItemBean.class)), subtasksUpdated),
                bulkEditBeanWith(sendEmail),
                applicationUserWith(user),
                issueWasMoved
        );

        verify(issueEventManager).dispatchRedundantEvent(EventType.ISSUE_UPDATED_ID, issue, user, updateLog, sendEmail, subtasksUpdated);
    }

    @Test
    public void issueEventBundledIsDispatched()
    {
        Issue issue = mock(Issue.class);
        GenericValue updateLog = new MockGenericValue("ChangeGroup");
        boolean subtasksUpdated = true;
        User user = mock(User.class);
        boolean sendEmail = true;
        boolean issueWasMoved = false;

        IssueEventBundle issueEventBundle = mock(IssueEventBundle.class);
        when(issueEventBundleFactory.createIssueUpdateEventBundle(any(Issue.class), any(GenericValue.class), any(IssueUpdateBean.class), any(ApplicationUser.class))).thenReturn(issueEventBundle);

        bulkMoveOperation.dispatchEvents(
                issue,
                updateLog,
                issueChangeHolderWith(Arrays.asList(mock(ChangeItemBean.class)), subtasksUpdated),
                bulkEditBeanWith(sendEmail),
                applicationUserWith(user),
                issueWasMoved
        );

        verify(issueEventManager).dispatchEvent(issueEventBundle);
    }

    private IssueChangeHolder issueChangeHolderWith(List<ChangeItemBean> changes, boolean subtasksUpdated)
    {
        IssueChangeHolder issueChangeHolder = mock(IssueChangeHolder.class);
        when(issueChangeHolder.getChangeItems()).thenReturn(changes);
        when(issueChangeHolder.isSubtasksUpdated()).thenReturn(subtasksUpdated);
        return issueChangeHolder;
    }

    private ApplicationUser applicationUserWith(User user)
    {
        ApplicationUser applicationUser = mock(ApplicationUser.class);
        when(applicationUser.getDirectoryUser()).thenReturn(user);
        return applicationUser;
    }

    private BulkEditBean bulkEditBeanWith(boolean sendEmail)
    {
        BulkEditBean bulkEditBean = mock(BulkEditBean.class);
        when(bulkEditBean.isSendBulkNotification()).thenReturn(sendEmail);
        return bulkEditBean;
    }

    class MockIssueBuilder
    {
        private final Issue issue = mock(Issue.class);
        private final Collection<Issue> subTasks = Lists.newArrayList();

        Issue build() {
            when(issue.getSubTaskObjects()).thenReturn(subTasks);
            return issue;
        }

        MockIssueBuilder permission(final boolean hasPermission) {
            when(permissionManager.hasPermission(Permissions.MOVE_ISSUE, issue, applicationUser)).thenReturn(hasPermission);
            return this;
        }

        MockIssueBuilder subTask(final Issue subIssue) {
            subTasks.add(subIssue);
            return this;
        }
    }

}
