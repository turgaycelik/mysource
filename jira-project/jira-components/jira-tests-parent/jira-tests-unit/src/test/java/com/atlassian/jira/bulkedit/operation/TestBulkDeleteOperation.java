package com.atlassian.jira.bulkedit.operation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanImpl;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;


public class TestBulkDeleteOperation
{
    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    @Mock
    private IssueManager issueManager;

    @AvailableInContainer
    @Mock
    private PermissionManager permissionManager;

    @Mock
    private ApplicationUser applicationUser;

    @Mock
    private User directoryUser;

    private BulkEditBean bulkEditBean;

    private final BulkDeleteOperation bulkDeleteOperation = new BulkDeleteOperation();

    private final MockIssue issue1 = new MockIssue(1);
    private final MockIssue issue2 = new MockIssue(2);

    @Before
    public void setUp() throws Exception
    {
        when(applicationUser.getDirectoryUser()).thenReturn(directoryUser);

        bulkEditBean = new BulkEditBeanImpl(issueManager);
        bulkEditBean.initSelectedIssues(ImmutableList.<Issue>of(issue1, issue2));
    }

    private void setupExistingIssues(final MutableIssue... issues)
    {
        for (final MutableIssue issue : issues)
        {
            when(issueManager.getIssueObject(issue.getId())).thenReturn(issue);
        }
    }

    @Test
    public void shouldNotPerformWithNoPermission() throws Exception
    {
        // given:
        setupExistingIssues(issue1, issue2);
        when(permissionManager.hasPermission(Permissions.DELETE_ISSUE, issue1, applicationUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.DELETE_ISSUE, issue2, applicationUser)).thenReturn(false);

        // when:
        final boolean result = bulkDeleteOperation.canPerform(bulkEditBean, applicationUser);

        // then:
        assertFalse("Operation should not be permitted as user has no permissions to delete one of the issues.", result);
    }

    @Test
    public void testCanPerform() throws Exception
    {
        // given:
        setupExistingIssues(issue1, issue2);
        when(permissionManager.hasPermission(Permissions.DELETE_ISSUE, issue1, applicationUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.DELETE_ISSUE, issue2, applicationUser)).thenReturn(true);

        // when:
        final boolean result = bulkDeleteOperation.canPerform(bulkEditBean, applicationUser);

        // then:
        assertTrue("Operation should be permitted, since user has all necessary permissions.", result);
    }

    @Test
    public void shouldNotAttemptToDeleteIssueIfNotExistingAtTimeOfDelete() throws Exception
    {
        // Test that bulk delete does not delete an issue that does not exist in the database
        // This is required as if a user is bulk deleteing issues and its sub-tasks the sub-tasks will be deleted when
        // the issue is deleted, by the time the bulk-delete gets around to deleting the sub-task, they have already been removed

        // given:
        bulkEditBean = new BulkEditBeanImpl(issueManager);
        bulkEditBean.initSelectedIssues(ImmutableList.<Issue>of(issue1));
        when(issueManager.getIssueObject(issue1.getId())).thenReturn(issue1).thenReturn(null);

        // when:
        bulkDeleteOperation.perform(bulkEditBean, applicationUser, Contexts.nullContext());

        // then:
        Mockito.verify(issueManager, never()).deleteIssue(directoryUser, (Issue) issue1, EventDispatchOption.ISSUE_DELETED, false);
    }

    @Test
    public void testPerform() throws Exception
    {
        // given:
        setupExistingIssues(issue1, issue2);
        when(permissionManager.hasPermission(Permissions.DELETE_ISSUE, issue1, applicationUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.DELETE_ISSUE, issue2, applicationUser)).thenReturn(true);


        // when:
        bulkDeleteOperation.perform(bulkEditBean, applicationUser, Contexts.nullContext());

        // then:
        Mockito.verify(issueManager).deleteIssue(directoryUser, (Issue) issue1, EventDispatchOption.ISSUE_DELETED, false);
        Mockito.verify(issueManager).deleteIssue(directoryUser, (Issue) issue2, EventDispatchOption.ISSUE_DELETED, false);
    }
}
