package com.atlassian.jira.task;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.ClusterServicesRegistry;
import com.atlassian.jira.cluster.Message;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.cluster.MessageHandlerService;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.DelegatorInterface;

import static com.atlassian.jira.task.TaskManagerImpl.TASK_CANCEL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @since v3.13 */

public class TestTaskManagerImpl
{
    private static final String PROGRESS_URL = "PROGRESS_URL";

    private static final String TASK_RESULT1 = "TaskResult1";
    private static final String TASK_RESULT2 = "TaskResult2";
    private static final String TEST_STARTED = "TestStarted";
    private static final String TEST_STARTED_MSG = "Your test has started.";

    private static final int PCNT_50 = 50;
    private static final int PCNT_100 = 100;
    private static final String TEST_FINISHED = "TestFinished";
    private static final String TEST_FINISHED_MSG = "Your test has finished.";

    private User testUser;
    private TaskManagerImpl taskManager;

    @AvailableInContainer @Mock private MessageHandlerService messageHandlerService;
    @AvailableInContainer @Mock private ClusterServicesRegistry clusterServicesRegistry;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);
    private MemoryCacheManager cacheManager;
    @Mock private DelegatorInterface delegatorInterface;
    @Mock private UserManager userManager;

    @Before
    public void setUp() throws Exception
    {
        testUser = new MockUser("TestTaskManagerImplUser");
        cacheManager = new MemoryCacheManager();
        when(clusterServicesRegistry.getMessageHandlerService()).thenReturn(messageHandlerService);
        taskManager = new TaskManagerImpl(getAuthContext(), clusterServicesRegistry, cacheManager, delegatorInterface, userManager);
        when(delegatorInterface.getNextSeqId("TaskIdSequence")).thenAnswer(new Answer<Long>()
        {
            long seq = 0;
            @Override
            public Long answer(final InvocationOnMock invocation) throws Throwable
            {
                return seq++;
            }
        });
    }

    @After
    public void tearDown() throws Exception
    {
        taskManager.shutdownAndWait(0);
        taskManager.shutdownNow();
    }

    @Test
    public void testInitialState()
    {
        Collection<TaskDescriptor<?>> tasks;

        tasks = taskManager.getAllTasks();
        assertNotNull(tasks);
        assertEquals(0, tasks.size());

        tasks = taskManager.getLiveTasks();
        assertNotNull(tasks);
        assertEquals(0, tasks.size());

        assertTrue(taskManager.awaitUntilActiveTasksComplete(10));
    }

    @Test
    public void testTaskSubmission()
    {
        final Callable<? extends Serializable> ourTask = getNoopCallable();

        final AtomicBoolean called = new AtomicBoolean(false);
        final TaskManager noopTaskManager = getNoExecutionTaskManager(called);
        final String taskDescription = "OurTask";
        final TaskContext ourContext = getTaskContext();

        assertNotNull(noopTaskManager.getAllTasks());
        assertTrue(noopTaskManager.getAllTasks().isEmpty());
        assertNotNull(noopTaskManager.getLiveTasks());
        assertTrue(noopTaskManager.getLiveTasks().isEmpty());
        assertTrue(taskManager.awaitUntilActiveTasksComplete(10));

        final TaskDescriptor<? extends Serializable> taskDescriptor = noopTaskManager.submitTask(ourTask, taskDescription, ourContext);

        assertEquals(true, called.get());

        assertNotNull(taskDescriptor);
        assertNotNull(taskDescriptor.getTaskId());
        assertSame(ourContext, taskDescriptor.getTaskContext());
        assertEquals(taskDescription, taskDescriptor.getDescription());
        assertEquals(testUser.getName(), taskDescriptor.getUserName());
        assertTrue(taskManager.awaitUntilActiveTasksComplete(10));

        // this should be null because our tasks doesnt implement ProvidesTaskInfo
        assertNull(taskDescriptor.getTaskProgressIndicator());

        final TaskDescriptor<?> lookupDescriptor = noopTaskManager.getTask(taskDescriptor.getTaskId());
        assertNotNull(lookupDescriptor);
        assertTaskIdsEquals(taskDescriptor, lookupDescriptor);

        // must be a clone
        assertFalse(taskDescriptor == lookupDescriptor);

        Collection<TaskDescriptor<?>> tasks;

        tasks = noopTaskManager.getAllTasks();
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertTaskInCollection(taskDescriptor, tasks);
    }

    @Test
    public void testTaskSubmissionSinkAndProgressIndicatorSet()
    {
        class ProgressCallable implements Callable<Serializable>, ProvidesTaskProgress
        {
            private TaskProgressSink sink = null;

            public Serializable call() throws Exception
            {
                return null;
            }

            public void setTaskProgressSink(final TaskProgressSink taskProgressSink)
            {
                sink = taskProgressSink;
            }
        }

        final AtomicBoolean called = new AtomicBoolean(false);
        final TaskManager noopTaskManager = getNoExecutionTaskManager(called);
        final Callable<? extends Serializable> ourTask = new ProgressCallable();
        final TaskContext ourTaskContext = getTaskContext();

        final String taskDescription = "OurTaskWithProgress";
        final TaskDescriptor<?> taskDescriptor = noopTaskManager.submitTask(ourTask, taskDescription, ourTaskContext);

        assertEquals(true, called.get());

        assertNotNull(taskDescriptor);
        assertNotNull(taskDescriptor.getTaskId());
        assertSame(ourTaskContext, taskDescriptor.getTaskContext());
        assertEquals(taskDescription, taskDescriptor.getDescription());
        assertEquals(testUser.getName(), taskDescriptor.getUserName());
        assertEquals(PROGRESS_URL, taskDescriptor.getProgressURL());
        assertTrue(taskManager.awaitUntilActiveTasksComplete(10));

        assertNotNull(taskDescriptor.getTaskProgressIndicator());
        assertNotNull(((ProgressCallable) ourTask).sink);
    }

    @Test
    public void testTaskDescriptorState() throws InterruptedException, ExecutionException
    {
        final Date then = new Date();

        final LatchedCallable latchedCallable = new LatchedCallable();

        final TaskDescriptor<String> taskDescriptorOrig = taskManager.submitTask(latchedCallable, "Testing Task State", getTaskContext());

        assertNotNull(latchedCallable.getTaskDescriptor());

        latchedCallable.waitForStart();
        assertFalse(taskManager.awaitUntilActiveTasksComplete(0));
        TaskDescriptor<String> taskDescriptor = taskManager.getTask(taskDescriptorOrig.getTaskId());

        assertFalse(taskDescriptor == taskDescriptorOrig);
        assertTrue(taskDescriptor.isStarted());
        assertNotNull(taskDescriptor.getStartedTimestamp());
        assertNotNull(taskDescriptor.getSubmittedTimestamp());
        assertTrue(taskDescriptor.getSubmittedTimestamp().compareTo(then) >= 0);
        assertTrue(taskDescriptor.getStartedTimestamp().compareTo(taskDescriptor.getSubmittedTimestamp()) >= 0);
        assertNotNull(latchedCallable.getTaskDescriptor());

        final TaskProgressIndicator indicator = taskDescriptor.getTaskProgressIndicator();
        assertNotNull(indicator);
        assertNotNull(indicator.getLastProgressEvent());

        TaskProgressEvent event = indicator.getLastProgressEvent();
        assertNotNull(event);
        assertEquals(TEST_STARTED, event.getCurrentSubTask());
        assertEquals(TEST_STARTED_MSG, event.getMessage());
        assertEquals(PCNT_50, event.getTaskProgress());

        assertNotNull(taskManager.getLiveTasks());
        assertEquals(1, taskManager.getLiveTasks().size());

        assertTaskInCollection(taskDescriptor, taskManager.getLiveTasks());

        assertNotNull(taskManager.getAllTasks());
        assertEquals(1, taskManager.getAllTasks().size());

        assertTaskInCollection(taskDescriptor, taskManager.getLiveTasks());

        assertFalse(taskDescriptor.isFinished());
        assertNull(taskDescriptor.getFinishedTimestamp());

        // release latch to go to next state
        latchedCallable.unblock();

        // this will block until the callable returns
        assertTrue(taskManager.awaitUntilActiveTasksComplete(10));
        taskDescriptor = taskManager.getTask(taskDescriptorOrig.getTaskId());
        final String result = taskDescriptor.getResult();
        assertEquals(TASK_RESULT1, result);

        final TaskDescriptor<String> taskDescriptorFinished = taskManager.getTask(taskDescriptor.getTaskId());

        assertFalse(taskDescriptorFinished == taskDescriptorOrig);
        assertFalse(taskDescriptorFinished == taskDescriptor);

        assertTrue(taskDescriptorFinished.isStarted());
        assertNotNull(taskDescriptorFinished.getStartedTimestamp());
        assertTrue(taskDescriptorFinished.getStartedTimestamp().compareTo(then) >= 0);

        assertTrue(taskDescriptorFinished.isFinished());
        assertNotNull(taskDescriptorFinished.getFinishedTimestamp());
        assertTrue(taskDescriptorFinished.getFinishedTimestamp().compareTo(taskDescriptorFinished.getStartedTimestamp()) >= 0);

        // whats left over in the taskManager
        assertNotNull(taskManager.getLiveTasks());
        assertEquals(0, taskManager.getLiveTasks().size());

        assertNotNull(taskManager.getAllTasks());
        assertEquals(1, taskManager.getAllTasks().size());
        assertTaskInCollection(taskDescriptorFinished, taskManager.getAllTasks());

        assertTaskIdsEquals(taskManager.getTask(taskDescriptorFinished.getTaskId()), taskDescriptor);

        event = indicator.getLastProgressEvent();
        assertNotNull(event);
        assertEquals(TEST_FINISHED, event.getCurrentSubTask());
        assertEquals(TEST_FINISHED_MSG, event.getMessage());
        assertEquals(PCNT_100, event.getTaskProgress());
    }

    @Test
    public void testTaskMultipleTasks() throws InterruptedException, ExecutionException, BrokenBarrierException
    {
        final LatchedCallable callableFirst = new LatchedCallable();
        final LatchedCallable callableSecond = new LatchedCallable(TASK_RESULT2);

        TaskDescriptor<String> firstDescriptor = taskManager.submitTask(callableFirst, "All The Way", getTaskContext());
        TaskDescriptor<String> secondDescriptor = taskManager.submitTask(callableSecond, "Dont run", getTaskContext());

        assertNotNull(callableFirst.getTaskDescriptor());
        assertNotNull(callableSecond.getTaskDescriptor());

        callableFirst.waitForStart();
        assertFalse(taskManager.awaitUntilActiveTasksComplete(0));
        callableSecond.waitForStart();

        assertEquals(2, taskManager.getLiveTasks().size());

        callableFirst.unblock(); // allow first thread to complete.
        assertFalse(taskManager.awaitUntilActiveTasksComplete(0));
        taskManager.waitUntilTaskCompletes(firstDescriptor.getTaskId());
        firstDescriptor = taskManager.getTask(firstDescriptor.getTaskId());
        assertEquals(TASK_RESULT1, firstDescriptor.getResult()); // wait for this one to finish

        assertEquals(1, taskManager.getLiveTasks().size());

        assertTaskInCollection(secondDescriptor, taskManager.getLiveTasks());

        callableSecond.unblock(); // allow second thread to complete.
        assertTrue(taskManager.awaitUntilActiveTasksComplete(10));
        taskManager.waitUntilTaskCompletes(secondDescriptor.getTaskId());
        secondDescriptor = taskManager.getTask(secondDescriptor.getTaskId());
        assertEquals(TASK_RESULT2, secondDescriptor.getResult()); // wait for it to finish
        assertEquals(0, taskManager.getLiveTasks().size());
    }

    @Test
    public void testShutdownAndWait() throws InterruptedException, ExecutionException
    {
        final LatchedCallable latchedCallable = new LatchedCallable();

        final TaskDescriptor<String> taskDescriptor = taskManager.submitTask(latchedCallable, "Blah", getTaskContext());

        assertNotNull(latchedCallable.getTaskDescriptor());

        latchedCallable.waitForStart();

        long start = System.currentTimeMillis();
        //it should not aggresively kill tasks that are already running.
        assertFalse(taskManager.shutdownAndWait(1));
        assertTrue(System.currentTimeMillis() - start >= 1000);
        
        try
        {
            taskManager.submitTask(getNoopCallable(), "Balh", getTaskContext());
            fail("Task manager should fail to submit new tasks on shutdown.");
        }
        catch (final RejectedExecutionException e)
        {
            //we expect this to happen on shutdown.
        }

        latchedCallable.unblock();
        taskDescriptor.getResult();
        assertTrue(taskManager.shutdownAndWait(Integer.MAX_VALUE));
        assertTrue(taskManager.awaitUntilActiveTasksComplete(10));

        try
        {
            taskManager.shutdownAndWait(-1);
            fail("Illegal argument exception.");
        }
        catch (final IllegalArgumentException e)
        {
            //this is expected.
        }
    }

    @Test
    public void testShutdownNow() throws InterruptedException, ExecutionException
    {
        final LatchedCallable latchedCallable = new LatchedCallable();

        final TaskDescriptor<String> taskDescriptor = taskManager.submitTask(latchedCallable, "Blah", getTaskContext());

        assertNotNull(latchedCallable.getTaskDescriptor());

        latchedCallable.waitForStart();
        //it should not aggresively kill tasks that are already running.
        assertFalse(taskManager.shutdownAndWait(0));
        assertFalse(taskManager.awaitUntilActiveTasksComplete(0));
        //this should aggressively shutdown the tasks.
        taskManager.shutdownNow();

        assertTrue(taskManager.awaitUntilActiveTasksComplete(10));
    }

    @Test
    public void testRemoveTask() throws ExecutionException, InterruptedException
    {
        final Callable<? extends Serializable> testCallable = getNoopCallable();

        final TaskDescriptor<?> taskDescriptor = taskManager.submitTask(testCallable, "Description", getTaskContext());
        taskManager.waitUntilTaskCompletes(taskDescriptor.getTaskId());
        taskManager.removeTask(taskDescriptor.getTaskId());
        assertNull(taskManager.getTask(taskDescriptor.getTaskId()));
        assertEquals(0, taskManager.getLiveTasks().size());
        assertEquals(0, taskManager.getAllTasks().size());
    }

    @Test
    public void testTaskContext() throws InterruptedException, ExecutionException
    {
        final Callable<? extends Serializable> noopCallable = getNoopCallable();

        final SimpleTaskContext context1 = new SimpleTaskContext(1);
        final SimpleTaskContext context1Same = new SimpleTaskContext(1);
        final SimpleTaskContext context2 = new SimpleTaskContext(2);

        final TaskManager noopTaskManager = getNoExecutionTaskManager(new AtomicBoolean(false));

        assertFalse(noopTaskManager.hasTaskWithContext(context1));
        assertFalse(noopTaskManager.hasLiveTaskWithContext(context1));

        final TaskDescriptor<?> td1 = noopTaskManager.submitTask(noopCallable, "Desc1", context1);

        assertTrue(noopTaskManager.hasTaskWithContext(context1));
        assertTrue(noopTaskManager.hasLiveTaskWithContext(context1));

        try
        {
            noopTaskManager.submitTask(noopCallable, "Desc1", context1);
            fail("it shouldnt allow multiple task contexts that are equal to be submitted");
        }
        catch (final AlreadyExecutingException e)
        {
            //expected
            assertTaskIdsEquals(e.getTaskDescriptor(), td1);
        }

        try
        {
            noopTaskManager.submitTask(noopCallable, "Desc1", context1Same);
            fail("it shouldnt allow multiple task contexts that are equal to be submitted");
        }
        catch (final AlreadyExecutingException e)
        {
            //expected.
            assertTaskIdsEquals(e.getTaskDescriptor(), td1);
        }

        final LatchedCallable callable1 = new LatchedCallable();
        final LatchedCallable callable2 = new LatchedCallable();
        final LatchedCallable callable3 = new LatchedCallable();

        final TaskDescriptor<String> descriptor1 = taskManager.submitTask(callable1, "Description", context1);

        assertNotNull(callable1.getTaskDescriptor());

        callable1.waitForStart();

        try
        {
            taskManager.submitTask(callable2, "Description", context1Same);
            fail("Should not be able to accept two tasks with the same context.");
        }
        catch (final AlreadyExecutingException e)
        {
            //expected.
            assertTaskIdsEquals(e.getTaskDescriptor(), descriptor1);
        }

        callable1.unblock();
        taskManager.waitUntilTaskCompletes(descriptor1.getTaskId());

        final TaskDescriptor<String> descriptor2 = taskManager.submitTask(callable2, "Description", context1Same);
        final TaskDescriptor<String> descriptor3 = taskManager.submitTask(callable3, "Description", context2);

        assertNotNull(callable2.getTaskDescriptor());
        assertNotNull(callable3.getTaskDescriptor());

        assertTrue(taskManager.hasTaskWithContext(context1));
        assertTrue(taskManager.hasTaskWithContext(context2));
        assertTrue(taskManager.hasLiveTaskWithContext(context1));
        assertTrue(taskManager.hasLiveTaskWithContext(context2));

        callable3.unblock();
        taskManager.waitUntilTaskCompletes(descriptor3.getTaskId());

        assertTrue(taskManager.hasTaskWithContext(context1));
        assertTrue(taskManager.hasTaskWithContext(context2));
        assertTrue(taskManager.hasLiveTaskWithContext(context1));
        assertFalse(taskManager.hasLiveTaskWithContext(context2));

        callable2.unblock();
        taskManager.waitUntilTaskCompletes(descriptor2.getTaskId());

        assertTrue(taskManager.hasTaskWithContext(context1));
        assertTrue(taskManager.hasTaskWithContext(context2));
        assertFalse(taskManager.hasLiveTaskWithContext(context1));
        assertFalse(taskManager.hasLiveTaskWithContext(context2));

        //make sure a NPE or IllegalArgumentException is thrown.
        try
        {
            taskManager.submitTask(getNoopCallable(), "Description", null);
            fail("This should throw an exception.");
        }
        catch (final RuntimeException e)
        {
            //expected.
        }

    }

    @Test
    public void testTaskMatchers() throws InterruptedException, ExecutionException
    {
        final LatchedCallable callable1 = new LatchedCallable();
        final LatchedCallable callable2 = new LatchedCallable();

        final TaskDescriptor<String> descriptor1 = taskManager.submitTask(callable1, "Description", new SimpleTaskContext(1));
        final TaskDescriptor<String> descriptor2 = taskManager.submitTask(callable2, "Description", new SimpleTaskContext(2));

        assertNotNull(callable1.getTaskDescriptor());
        assertNotNull(callable2.getTaskDescriptor());

        //make the task finish.
        callable1.unblock();
        taskManager.waitUntilTaskCompletes(descriptor1.getTaskId());

        //find the first finished task.
        TaskDescriptor<?> descriptor = taskManager.findFirstTask(new TaskMatcher()
        {
            public boolean match(final TaskDescriptor<?> descriptor)
            {
                return descriptor.isFinished();
            }
        });

        assertEquals(descriptor1.getTaskId(), descriptor.getTaskId());

        Collection<TaskDescriptor<?>> collection = taskManager.findTasks(new TaskMatcher()
        {
            public boolean match(final TaskDescriptor<?> descriptor)
            {
                return descriptor.isFinished();
            }
        });

        assertEquals(1, collection.size());
        assertTaskInCollection(descriptor1, collection);

        //look for all current tasks.
        collection = taskManager.findTasks(new TaskMatcher()
        {
            public boolean match(final TaskDescriptor<?> descriptor)
            {
                return true;
            }
        });

        assertTaskInCollection(descriptor1, collection);
        assertTaskInCollection(descriptor2, collection);

        callable2.unblock();
        taskManager.waitUntilTaskCompletes(descriptor2.getTaskId());

        //looks for not finished tasks (there should be none).
        collection = taskManager.findTasks(new TaskMatcher()
        {
            public boolean match(final TaskDescriptor<?> descriptor)
            {
                return !descriptor.isFinished();
            }
        });

        assertTrue(collection.isEmpty());

        descriptor = taskManager.findFirstTask(new TaskMatcher()
        {
            public boolean match(final TaskDescriptor<?> descriptor)
            {
                return !descriptor.isFinished();
            }
        });

        assertNull(descriptor);

        try
        {
            taskManager.findTasks(null);
            fail("This should throw an exception.");
        }
        catch (final RuntimeException e)
        {
            //expected.
        }

        try
        {
            taskManager.findFirstTask(null);
            fail("This should throw an exception.");
        }
        catch (final RuntimeException e)
        {
            //expected.
        }
    }

    @Test
    public void testCancelTaskLocal() throws InterruptedException, ExecutionException
    {
        final LatchedCallable latchedCallable = new LatchedCallable();
        final TaskDescriptor<String> taskDescriptorOrig = taskManager.submitTask(latchedCallable, "Testing Task Cancel", getTaskContext(), true);
        assertNotNull(latchedCallable.getTaskDescriptor());
        taskManager.cancelTask(taskDescriptorOrig.getTaskId());

        verify(messageHandlerService, never()).sendMessage(ClusterManager.ALL_NODES, new Message(TASK_CANCEL, taskDescriptorOrig.getTaskId().toString()));
    }

    @Test
    public void testCancelTaskRemote() throws InterruptedException, ExecutionException
    {
        // Add a remote entry to the task manager cache
        final TaskDescriptorImpl<String> taskDescriptor = new TaskDescriptorImpl<String>(101L, "remote task", getTaskContext(), "T", null, true);
        Cache<Long, TaskDescriptorImpl<String>> taskMapCache = cacheManager.getCache(TaskManagerImpl.class.getName() + ".taskMap", null, new CacheSettingsBuilder().unflushable().replicateViaCopy().build());
        taskMapCache.put(taskDescriptor.getTaskId(), taskDescriptor);

        taskManager.cancelTask(taskDescriptor.getTaskId());

        verify(messageHandlerService).sendMessage(ClusterManager.ALL_NODES, new Message(TASK_CANCEL, taskDescriptor.getTaskId().toString()));
    }

    @Test(expected=IllegalStateException.class)
    public void testCancelUncancellable() throws InterruptedException, ExecutionException
    {
        final LatchedCallable latchedCallable = new LatchedCallable();
        final TaskDescriptor<String> taskDescriptorOrig = taskManager.submitTask(latchedCallable, "Testing Task Cancel", getTaskContext(), false);
        assertNotNull(latchedCallable.getTaskDescriptor());
        taskManager.cancelTask(taskDescriptorOrig.getTaskId());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCancelBadTask() throws InterruptedException, ExecutionException
    {
        final LatchedCallable latchedCallable = new LatchedCallable();
        final TaskDescriptor<String> taskDescriptorOrig = taskManager.submitTask(latchedCallable, "Testing Task Cancel", getTaskContext(), false);
        assertNotNull(latchedCallable.getTaskDescriptor());
        taskManager.cancelTask(-73L);
    }

    private void assertTaskIdsEquals(final TaskDescriptor<?> one, final TaskDescriptor<?> two)
    {
        assertNotNull(one);
        assertNotNull(two);
        assertEquals(one.getTaskId(), two.getTaskId());
    }

    private void assertTaskInCollection(final TaskDescriptor<?> test, final Collection<TaskDescriptor<?>> collection)
    {
        if (!CollectionUtil.contains(collection, new Predicate<TaskDescriptor<?>>()
        {
            public boolean evaluate(final TaskDescriptor<?> taskDescriptor)
            {
                return taskDescriptor.getTaskId().equals(test.getTaskId());
            }
        }))
        {
            fail("Could not find task descritor in collection.");
        }
    }

    private Callable<Serializable> getNoopCallable()
    {
        return new Callable<Serializable>()
        {
            public Serializable call() throws Exception
            {
                return null;
            }
        };
    }

    private TaskContext getTaskContext()
    {
        return new TaskContext()
        {
            public String buildProgressURL(final Long taskId)
            {
                return PROGRESS_URL;
            }
        };
    }

    private TaskManager getNoExecutionTaskManager(final AtomicBoolean called)
    {
        return new TaskManagerImpl(getAuthContext(), clusterServicesRegistry, new MemoryCacheManager(), delegatorInterface, userManager)
        {
            @Override
            void submitTaskInternal(final FutureTask<?> futureTask)
            {
                called.set(true);
            }
        };
    }

    private JiraAuthenticationContext getAuthContext()
    {
        return new MockSimpleAuthenticationContext(testUser);
    }

    private class SimpleTaskContext implements TaskContext
    {
        private final long id;

        public SimpleTaskContext(final long id)
        {
            this.id = id;
        }

        public String buildProgressURL(final Long taskId)
        {
            return "id=" + id;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass()))
            {
                return false;
            }

            final SimpleTaskContext that = (SimpleTaskContext) o;

            return id == that.id;

        }

        @Override
        public int hashCode()
        {
            return (int) (id ^ (id >>> 32));
        }
    }

    private static class LatchedCallable implements Callable<String>, ProvidesTaskProgress, RequiresTaskInformation<String>
    {
        private volatile TaskProgressSink sink = null;
        private final CountDownLatch startedLatched = new CountDownLatch(1);
        private final CountDownLatch blockingLatch = new CountDownLatch(1);
        private final String taskResult;
        private volatile TaskDescriptor<String> taskDescriptor = null;

        public LatchedCallable()
        {
            this(TASK_RESULT1);
        }

        public LatchedCallable(final String taskResult)
        {
            this.taskResult = taskResult;
        }

        public String call() throws Exception
        {
            // started state
            sink.makeProgress(PCNT_50, TEST_STARTED, TEST_STARTED_MSG);
            startedLatched.countDown();
            blockingLatch.await();

            sink.makeProgress(PCNT_100, TEST_FINISHED, TEST_FINISHED_MSG);
            return taskResult;
        }

        public void setTaskProgressSink(final TaskProgressSink taskProgressSink)
        {
            sink = taskProgressSink;
        }

        public void waitForStart() throws InterruptedException
        {
            startedLatched.await();
        }

        public void unblock()
        {
            blockingLatch.countDown();
        }

        public void setTaskDescriptor(final TaskDescriptor<String> taskDescriptor)
        {
            this.taskDescriptor = taskDescriptor;
        }

        public TaskDescriptor<String> getTaskDescriptor()
        {
            return taskDescriptor;
        }
    }
}
