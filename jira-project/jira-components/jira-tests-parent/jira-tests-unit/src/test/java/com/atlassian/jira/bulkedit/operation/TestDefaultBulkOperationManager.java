package com.atlassian.jira.bulkedit.operation;

import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.bulkedit.DefaultBulkOperationManager;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collection;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;


public class TestDefaultBulkOperationManager
{
    @Rule
    public InitMockitoMocks initMockitoMocks = new InitMockitoMocks(this);

    @Mock
    private WatcherService watcherService;

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private BulkEditOperation bulkEditOperation;

    @Mock
    private BulkMigrateOperation bulkMigrateOperation;

    @Mock
    private BulkWorkflowTransitionOperation bulkWorkflowTransitionOperation;

    @Mock
    private BulkDeleteOperation bulkDeleteOperation;

    @Mock
    private BulkWatchOperation bulkWatchOperation;

    @Mock
    private BulkUnwatchOperation bulkUnwatchOperation;

    @Before
    public void setUp()
    {
        when(bulkEditOperation.getNameKey()).thenReturn(BulkEditOperation.NAME_KEY);
        when(bulkMigrateOperation.getNameKey()).thenReturn(BulkMigrateOperation.NAME_KEY);
        when(bulkWorkflowTransitionOperation.getNameKey()).thenReturn(BulkWorkflowTransitionOperation.NAME_KEY);
        when(bulkDeleteOperation.getNameKey()).thenReturn(BulkDeleteOperation.NAME_KEY);
        when(bulkWatchOperation.getNameKey()).thenReturn(BulkWatchOperation.NAME_KEY);
        when(bulkUnwatchOperation.getNameKey()).thenReturn(BulkUnwatchOperation.NAME_KEY);
    }

    @Test
    public void testGetBulkOperationsWatchingEnabled()
    {
        performTest(true, true, bulkEditOperation, bulkMigrateOperation, bulkWorkflowTransitionOperation,
                bulkDeleteOperation, bulkWatchOperation, bulkUnwatchOperation);
    }

    @Test
    public void testGetBulkOperationsWatchingEnabledAndAnonymous()
    {
        performTest(true, false, bulkEditOperation, bulkMigrateOperation, bulkWorkflowTransitionOperation,
                bulkDeleteOperation);
    }

    @Test
    public void testGetBulkOperationsWatchingDisabled()
    {
        performTest(false, true, bulkEditOperation, bulkMigrateOperation, bulkWorkflowTransitionOperation,
                bulkDeleteOperation);
    }

    @Test
    public void testGetBulkOperationsWatchingDisabledAndAnonymous()
    {
        performTest(false, false, bulkEditOperation, bulkMigrateOperation, bulkWorkflowTransitionOperation,
                bulkDeleteOperation);
    }

    private void performTest(final boolean watchingEnabled, final boolean loggedInUser,
                             final ProgressAwareBulkOperation... results)
    {
        when(watcherService.isWatchingEnabled()).thenReturn(watchingEnabled);
        when(jiraAuthenticationContext.isLoggedInUser()).thenReturn(loggedInUser);
        final BulkOperationManager bulkOperationManager = new DefaultBulkOperationManager(jiraAuthenticationContext,
                watcherService, bulkEditOperation, bulkMigrateOperation, bulkWorkflowTransitionOperation,
                bulkDeleteOperation,
                bulkWatchOperation, bulkUnwatchOperation);

        final Collection<ProgressAwareBulkOperation> bulkOperations =
                bulkOperationManager.getProgressAwareBulkOperations();

        assertNotNull(bulkOperations);

        assertThat("Wrong operations were provided.", bulkOperations, contains(results));
    }
}
