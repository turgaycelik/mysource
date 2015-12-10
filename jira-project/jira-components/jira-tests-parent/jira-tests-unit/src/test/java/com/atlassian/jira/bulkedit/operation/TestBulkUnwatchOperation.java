package com.atlassian.jira.bulkedit.operation;

import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.web.bean.BulkEditBean;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestBulkUnwatchOperation
{
    private WatcherService watcherService;

    @Before
    public void setUp() throws Exception
    {
        watcherService = mock(WatcherService.class);
    }

    @Test
    public void shouldBeAbleToPerformABulkUnwatchOperationWhenTheUserIsPermittedToUnwatchAllTheSelectedIssues()
    {
        final ApplicationUser user = new MockApplicationUser("test-user");
        final ImmutableList<Issue> currentlySelectedIssues = ImmutableList.of(mock(Issue.class));

        final BulkEditBean bulkEditBeanForTheSelectedIssues = mock(BulkEditBean.class);
        when(bulkEditBeanForTheSelectedIssues.getSelectedIssues()).thenReturn(currentlySelectedIssues);

        final BulkUnwatchOperation bulkUnwatchOperation = new BulkUnwatchOperation(watcherService);

        when(watcherService.canUnwatchAll(currentlySelectedIssues, user)).thenReturn(true);

        assertTrue
                (
                        "It should be possible to perform a bulk unwatch operation when the specified user can be "
                                + "removed as a watcher on all the selected issues",
                        bulkUnwatchOperation.canPerform(bulkEditBeanForTheSelectedIssues, user)
                );
    }

    @Test
    public void shouldNotBeAbleToPerformABulkUnwatchOperationWhenTheUserIsNotPermittedToUnwatchAllTheSelectedIssues()
    {
        final ApplicationUser user = new MockApplicationUser("test-user");
        final ImmutableList<Issue> currentlySelectedIssues = ImmutableList.of(mock(Issue.class));

        final BulkEditBean bulkEditBeanForTheSelectedIssues = mock(BulkEditBean.class);
        when(bulkEditBeanForTheSelectedIssues.getSelectedIssues()).thenReturn(currentlySelectedIssues);

        final BulkUnwatchOperation bulkUnwatchOperation = new BulkUnwatchOperation(watcherService);

        when(watcherService.canUnwatchAll(currentlySelectedIssues, user)).thenReturn(false);

        assertFalse
                (
                        "It should not be possible to perform a bulk unwatch operation when the specified user can not "
                                + "be removed as a watcher on all the selected issues",
                        bulkUnwatchOperation.canPerform(bulkEditBeanForTheSelectedIssues, user)
                );
    }

    @Test
    public void performingABulkUnwatchOperationShouldRemoveTheUserAsAWatcherOnAllTheSelectedIssues() throws Exception
    {
        final ApplicationUser user = new MockApplicationUser("test-user");
        final ImmutableList<Issue> currentlySelectedIssues = ImmutableList.of(mock(Issue.class), mock(Issue.class));

        final BulkEditBean bulkEditBeanForTheSelectedIssues = mock(BulkEditBean.class);
        when(bulkEditBeanForTheSelectedIssues.getSelectedIssues()).thenReturn(currentlySelectedIssues);

        final BulkUnwatchOperation bulkUnwatchOperation = new BulkUnwatchOperation(watcherService);

        bulkUnwatchOperation.perform(bulkEditBeanForTheSelectedIssues, user, Contexts.nullContext());

        verify(watcherService).removeWatcherFromAll(currentlySelectedIssues, user, user, Contexts.nullContext());
    }
}
