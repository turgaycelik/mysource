package com.atlassian.jira.bc.workflow;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;

import com.google.common.collect.Lists;
import com.opensymphony.workflow.loader.DescriptorFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(ListeningMockitoRunner.class)
public class TestDefaultWorkflowServiceConcurrency
{
    private static void runInSeparateThreadsUntilComplete(final List<Callable<Void>> tasks) throws Exception
    {
        final ExecutorService pool = newFixedThreadPool(tasks.size());
        try
        {
            runToCompletion(tasks, pool);
        }
        finally
        {
            pool.shutdown();
        }
    }

    private static void runToCompletion(final Collection<Callable<Void>> tasks, final ExecutorService executor)
            throws InterruptedException, ExecutionException
    {
        final List<Future<Void>> futures;
        try
        {
            futures = executor.invokeAll(tasks);
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeException(e);
        }

        // wait for all tasks to finish
        for (final Future future : futures)
        {
            future.get();
        }
    }

    // Fixture
    private I18nHelper mockI18nHelper;

    @Mock private WorkflowManager mockWorkflowManager;
    @Mock private PermissionManager mockPermissionManager;

    @Mock private JiraWorkflow mockJiraWorkflow;
    @Mock private JiraWorkflow mockDraftWorkflow;


    @Before
    public void setUp() throws Exception
    {
        mockI18nHelper = new MockI18nHelper();
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void testConcurrentEditAndOverwrite() throws Exception
    {
        // Set up
        final AtomicLong updateWorkflowTime = new AtomicLong();
        final AtomicLong overwriteWorkflowTime = new AtomicLong();
        final Object workflowManagerDelegate = new Object()
        {
            @SuppressWarnings("unused")
            public void overwriteActiveWorkflow(final ApplicationUser user, final String workflowName)
            {
                assertEquals("testuser", user.getKey());
                assertEquals("jiraworkflow", workflowName);
                overwriteWorkflowTime.set(System.currentTimeMillis());
            }

            @SuppressWarnings("unused")
            public void updateWorkflow(final ApplicationUser user, final JiraWorkflow workflow)
            {
                updateWorkflowTime.set(System.currentTimeMillis());
            }
        };

        final WorkflowManager mockWorkflowManager =
                (WorkflowManager) DuckTypeProxy.getProxy(WorkflowManager.class, workflowManagerDelegate);
        final CountDownLatch validateOverwriteLatch = new CountDownLatch(1);

        final DefaultWorkflowService defaultWorkflowService =
                new DefaultWorkflowService(mockWorkflowManager, null, null, null, new SimpleClusterLockService())
        {
            I18nHelper getI18nBean()
            {
                return mockI18nHelper;
            }

            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            public void validateOverwriteWorkflow(final JiraServiceContext jiraServiceContext, final String workflowName)
            {
                // count down the latch, to get the update going
                validateOverwriteLatch.countDown();
                try
                {
                    // give the update thread some time to try to run
                    Thread.sleep(200);
                }
                catch (final InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
        defaultWorkflowService.start();

        final User testUser = new MockUser("testuser");
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        // TODO change to ApplicationUser once test passes
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        final List<Callable<Void>> tasks = Lists.newArrayList();

        when(mockJiraWorkflow.getDescriptor()).thenReturn(new DescriptorFactory().createWorkflowDescriptor());
        when(mockJiraWorkflow.isEditable()).thenReturn(true);
        tasks.add(new Callable<Void>()
        {
            public Void call() throws Exception
            {
                // wait until validate is called
                validateOverwriteLatch.await();
                defaultWorkflowService.updateWorkflow(jiraServiceContext, mockJiraWorkflow);
                return null;
            }
        });
        tasks.add(new Callable<Void>()
        {
            public Void call() throws Exception
            {
                defaultWorkflowService.overwriteActiveWorkflow(jiraServiceContext, "jiraworkflow");
                return null;
            }
        });

        // Invoke
        runInSeparateThreadsUntilComplete(tasks);

        // Check
        assertTrue("The update should always occur after the overwrite",
                updateWorkflowTime.get() >= overwriteWorkflowTime.get());
    }
}
