package com.atlassian.jira.task;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettings;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.ClusterMessageConsumer;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.cluster.ClusterServicesRegistry;
import com.atlassian.jira.cluster.Message;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Functions;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.Transformed;
import com.atlassian.jira.util.concurrent.BlockingCounter;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.thread.JiraThreadLocalUtils;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;

import static com.atlassian.jira.util.collect.CollectionUtil.contains;
import static com.atlassian.jira.util.collect.CollectionUtil.filter;
import static com.atlassian.jira.util.collect.CollectionUtil.findFirstMatch;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation of a {@link com.atlassian.jira.task.TaskManager}. This manager uses an {@link ExecutorService} to
 * run submitted tasks.
 *
 * @since 3.13
 */
public class TaskManagerImpl implements TaskManager, TaskProgressListener
{
    public final static String TASK_CANCEL = "Task Cancel Request";
    private final static Logger log = Logger.getLogger(TaskManagerImpl.class);

    private static final Function<TaskDescriptorImpl<?>, TaskDescriptor<?>> COPY =
            new Function<TaskDescriptorImpl<?>, TaskDescriptor<?>>()
            {
                public TaskDescriptor<?> get(final TaskDescriptorImpl<?> input)
                {
                    return copy(input);
                }
            };

    /**
     * The EHCache settings are really coming from ehcache.xml, but we need to set unflushable() here .
     */
    private static final CacheSettings TASKMAP_CACHE_SETTINGS =
            new CacheSettingsBuilder().unflushable().replicateViaCopy().build();
    private static final CacheSettings FUTUREMAP_CACHE_SETTINGS =
            new CacheSettingsBuilder().local().unflushable().build();

    private final Cache<Long, TaskDescriptorImpl<?>> taskMap;
    private final Cache<Long, Future<?>> futureMap;
    private final JiraAuthenticationContext authenticationContext;
    private final ClusterServicesRegistry clusterServicesRegistry;
    private final DelegatorInterface delegatorInterface;
    private final UserManager userManager;
    private final BlockingCounter activeThreads = new BlockingCounter();
    private final MessageConsumer messageConsumer; // Needed to keep the consumer from being garbage collected.
    private ExecutorService executorService;

    public TaskManagerImpl(final JiraAuthenticationContext authenticationContext,
            final ClusterServicesRegistry clusterServicesRegistry, final CacheManager cacheManager,
            final DelegatorInterface delegatorInterface, final UserManager userManager)
    {
        this.authenticationContext = authenticationContext;
        this.clusterServicesRegistry = clusterServicesRegistry;
        this.delegatorInterface = delegatorInterface;
        this.userManager = userManager;
        taskMap = cacheManager.getCache(TaskManagerImpl.class.getName() + ".taskMap", null, TASKMAP_CACHE_SETTINGS);
        futureMap =
                cacheManager.getCache(TaskManagerImpl.class.getName() + ".futureMap", null, FUTUREMAP_CACHE_SETTINGS);
        messageConsumer = new MessageConsumer(futureMap);
        clusterServicesRegistry.getMessageHandlerService().registerListener(TASK_CANCEL, messageConsumer);
        start();
    }

    @Override
    public <V extends Serializable> TaskDescriptor<V> submitTask(@Nonnull Callable<V> callable,
            @Nonnull String taskDescription,
            @Nonnull TaskContext taskContext)
            throws RejectedExecutionException
    {
        return submitTask(callable, taskDescription, taskContext, false);
    }

    public <V extends Serializable> TaskDescriptor<V> submitTask(@Nonnull final Callable<V> callable,
            @Nonnull final String taskDescription,
            @Nonnull final TaskContext taskContext,
            final boolean cancellable)
            throws RejectedExecutionException
    {
        notNull("callable", callable);
        notNull("taskContext", taskContext);
        notNull("taskDescription", taskDescription);

        final Long taskId = getNextTaskId();
        log.debug("New task ID acquired: " + taskId);

        TaskProgressAdapter taskProgressAdapter = null;
        if (callable instanceof ProvidesTaskProgress)
        {
            taskProgressAdapter = new TaskProgressAdapter();
            taskProgressAdapter.addListener(this);
        }

        final ApplicationUser user = authenticationContext.getUser();
        final String userName = user == null ? null : user.getName();
        final TaskDescriptorImpl<V> taskDescriptor =
                new TaskDescriptorImpl<V>(taskId, taskDescription, taskContext, userName, taskProgressAdapter,
                        cancellable);
        final FutureTask<V> futureTask = new FutureTask<V>(
                new TaskCallableDecorator<V>(callable, taskDescriptor, authenticationContext, activeThreads));

        // can they provide progress feed back
        if (callable instanceof ProvidesTaskProgress)
        {
            taskProgressAdapter.setTaskDescriptor(taskDescriptor);
            ((ProvidesTaskProgress) callable).setTaskProgressSink(taskProgressAdapter);
        }
        // do they want task descriptor info
        if (callable instanceof RequiresTaskInformation)
        {
            @SuppressWarnings ("unchecked")
            final RequiresTaskInformation<V> requiresTaskInformation = (RequiresTaskInformation<V>) callable;
            requiresTaskInformation.setTaskDescriptor(taskDescriptor);
        }

        // THREAD SAFETY :
        //the only thing we are worried about is making sure that two tasks with the same
        //Context do not start. There are race conditions here (i.e. a task can complete
        //between the hasLiveTaskWithContext() and taskMap.put) but this does not
        //matter because the user can just retry the task.
        synchronized (this)
        {
            //
            // check the TaskContext to see if we have any "live" tasks with that context
            final TaskDescriptor<?> testTaskDescriptor = getLiveTask(taskContext);
            if (testTaskDescriptor != null)
            {
                throw new AlreadyExecutingException(testTaskDescriptor,
                        "A task with this context has already been submitted");
            }
            //
            // add the task to out set of known tasks
            taskMap.put(taskId, taskDescriptor);
            futureMap.put(taskId, futureTask);
        }

        //
        // begin execution of the task (soon)
        submitTaskInternal(futureTask);

        // return an immutable  clone
        return new TaskDescriptorImpl<V>(taskDescriptor);
    }

    private Long getNextTaskId()
    {
        return delegatorInterface.getNextSeqId("TaskIdSequence");
    }

    public void removeTask(@Nonnull final Long taskId)
    {
        taskMap.remove(taskId);
        futureMap.remove(taskId);
    }

    @Override
    public void cancelTask(@Nonnull final Long taskId) throws IllegalStateException
    {
        TaskDescriptorImpl<?> task = taskMap.get(taskId);
        Future<?> future = futureMap.get(taskId);
        if (task == null)
        {
            throw new InvalidParameterException("Task not found for taskId = '" + taskId + "'");
        }
        if (!task.isCancellable())
        {
            throw new IllegalStateException("Task '" + taskId + "' is not cancellable");
        }
        if (future != null)
        {
            future.cancel(false);
        }
        else
        {
            // Propagate the cancel to all nodes. We never know who is actually running the task
            clusterServicesRegistry.getMessageHandlerService()
                    .sendMessage(ClusterManager.ALL_NODES, new Message(TASK_CANCEL, taskId.toString()));
        }
        task.setCancelled(true);
        refreshTaskInTaskCache(task);
    }

    @Override
    public void cancelTaskIfRunningLocally(@Nonnull final Long taskId)
    {
        messageConsumer.cancelTaskIfRunningLocally(taskId);
    }

    @Override
    public boolean isCancellable(@Nonnull final Long taskId)
    {
        TaskDescriptor<?> task = taskMap.get(taskId);
        if (task == null)
        {
            throw new InvalidParameterException("Task not found for taskId = '" + taskId + "'");
        }
        return task.isCancellable();
    }

    @Override
    public boolean isCancelled(final Long taskId)
    {
        TaskDescriptor<?> task = taskMap.get(taskId);
        if (task == null)
        {
            throw new InvalidParameterException("Task not found for taskId = '" + taskId + "'");
        }
        return task.isCancelled();
    }

    /**
     * Called to submit the task to an ExecutorService.  Made into a package level method to allow for better unit
     * testing
     *
     * @param futureTask the callable future that wraps the task's callable.
     */

    void submitTaskInternal(final FutureTask<?> futureTask)
    {
        executorService.submit(futureTask);
    }

    @Override
    public void start()
    {
        if (executorService == null || executorService.isShutdown())
        {
            executorService = new ForkedThreadExecutor(5, new TaskManagerThreadFactory());
        }
    }

    public boolean shutdownAndWait(final long waitSeconds)
    {
        if (waitSeconds < 0)
        {
            throw new IllegalArgumentException("waitSeconds must be >= 0");
        }

        executorService.shutdown();
        boolean val;
        try
        {
            val = executorService.awaitTermination(waitSeconds, TimeUnit.SECONDS);
        }
        catch (final InterruptedException e)
        {
            val = executorService.isTerminated();
        }

        logRunningTasksOnShutdown();
        return val;
    }

    public void shutdownNow()
    {
        executorService.shutdownNow();
    }

    public boolean awaitUntilActiveTasksComplete(long seconds)
    {
        try
        {
            return activeThreads.await(seconds, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            return activeThreads.getCount() == 0;
        }
    }

    @Override
    public void waitUntilTaskCompletes(final Long taskId) throws ExecutionException, InterruptedException
    {
        Future<?> future = futureMap.get(taskId);
        if (future != null)
        {
            try
            {
                future.get();
            }
            catch (CancellationException e)
            {
                log.debug("Task - '" + taskId + "' was cancelled");
            }
        }
    }

    public <V extends Serializable> TaskDescriptor<V> getLiveTask(@Nonnull final TaskContext taskContext)
    {
        notNull("taskContext", taskContext);

        @SuppressWarnings ("unchecked")
        final TaskDescriptor<V> result = (TaskDescriptor<V>) findFirstTask(new ActiveMatcher(taskContext));
        return result;
    }

    public <V extends Serializable> TaskDescriptor<V> getTask(final Long taskId)
    {
        if (taskId == null)
        {
            return null;
        }
        @SuppressWarnings ("unchecked")
        final TaskDescriptorImpl<V> input = (TaskDescriptorImpl<V>) taskMap.get(taskId);
        return copy(input);
    }

    public boolean hasLiveTaskWithContext(@Nonnull final TaskContext taskContext)
    {
        return hasTask(new ActiveMatcher(taskContext));
    }

    public boolean hasTaskWithContext(@Nonnull final TaskContext taskContext)
    {
        notNull("taskContext", taskContext);
        return hasTask(new TaskMatcher()
        {
            public boolean match(final TaskDescriptor<?> descriptor)
            {
                return taskContext.equals(descriptor.getTaskContext());
            }
        });
    }

    public TaskDescriptor<?> findFirstTask(@Nonnull final TaskMatcher matcher)
    {
        return findFirstMatch(getTasks(taskMap), new TaskMatcherPredicate(matcher));
    }

    @ClusterSafe (
            "We walk the keys of this map with the knowledge this is a canonical pinned cache of all existing tasks.")
    private Collection<? extends TaskDescriptorImpl<?>> getTasks(final Cache<Long, TaskDescriptorImpl<?>> taskMap)
    {
        List<TaskDescriptorImpl<?>> tasks = new ArrayList<TaskDescriptorImpl<?>>();
        for (Long taskId : taskMap.getKeys())
        {
            final TaskDescriptorImpl<?> taskDescriptor = taskMap.get(taskId);
            if (taskDescriptor != null)
            {
                tasks.add(taskDescriptor);
            }
        }
        return tasks;
    }

    public Collection<TaskDescriptor<?>> findTasks(@Nonnull final TaskMatcher matcher)
    {
        return findTasksInternal(matcher);
    }

    public Collection<TaskDescriptor<?>> getAllTasks()
    {
        return sortIntoIdOrder(Transformed.collection(getTasks(taskMap), COPY));
    }

    public Collection<TaskDescriptor<?>> getLiveTasks()
    {
        return sortIntoIdOrder(findTasksInternal(new TaskMatcher()
        {
            public boolean match(final TaskDescriptor<?> descriptor)
            {
                return !descriptor.isFinished();
            }
        }));
    }

    private Collection<TaskDescriptor<?>> findTasksInternal(final TaskMatcher matcher)
    {
        notNull("matcher", matcher);
        return Transformed.collection(filter(getTasks(taskMap), new TaskMatcherPredicate(matcher)),
                Functions.<TaskDescriptor<?>, TaskDescriptor<?>>coerceToSuper());
    }

    private boolean hasTask(final TaskMatcher matcher)
    {
        return contains(getTasks(taskMap), new TaskMatcherPredicate(matcher));
    }

    private static <V extends Serializable> TaskDescriptor<V> copy(final TaskDescriptorImpl<V> input)
    {
        if (input == null)
        {
            return null;
        }
        return new TaskDescriptorImpl<V>(input);
    }

    /**
     * Sorts a list of TaskDescriptor objects in taskId order.
     *
     * @param input the list of TaskDescriptor objects
     * @return the list sorted.
     */

    private List<TaskDescriptor<?>> sortIntoIdOrder(final Collection<TaskDescriptor<?>> input)
    {
        final List<TaskDescriptor<?>> result = new ArrayList<TaskDescriptor<?>>(input);
        Collections.sort(result, new Comparator<TaskDescriptor<?>>()
        {
            public int compare(final TaskDescriptor<?> o1, final TaskDescriptor<?> o2)
            {
                return o1.getTaskId().compareTo(o2.getTaskId());
            }
        });
        return result;
    }

    private void logRunningTasksOnShutdown()
    {
        final Collection<TaskDescriptor<?>> liveTasks = getLiveTasks();
        if (!liveTasks.isEmpty())
        {
            log.warn("Shutting down task manager while the following tasks are still executing:");

            for (final TaskDescriptor<?> taskDescriptor : liveTasks)
            {
                final StringBuilder sb = new StringBuilder();
                sb.append("Task Id ");
                sb.append(taskDescriptor.getTaskId());
                final TaskProgressEvent event = taskDescriptor.getTaskProgressIndicator() == null ? null :
                        taskDescriptor.getTaskProgressIndicator().getLastProgressEvent();
                if (event != null)
                {
                    sb.append(" - ");
                    sb.append(event.getTaskProgress());
                    sb.append("% complete");
                }
                sb.append(" - ");
                sb.append(taskDescriptor.getDescription());
                log.warn(sb.toString());
            }
        }
    }

    @Override
    public void onProgressMade(final TaskProgressEvent event)
    {
        TaskDescriptorImpl<?> taskDescriptor = taskMap.get(event.getTaskId());
        refreshTaskInTaskCache(taskDescriptor);
    }

    /**
     * THREAD SAFETY :
     * <p/>
     * This wraps the task Callable and ensures that the TaskDescriptor is updated in regards to start and finish times.
     *  It also clears the reference to the original Callable to help with memory cleanup when the Callable is finished
     * and has returned a result.
     */
    private class TaskCallableDecorator<V extends Serializable> implements Callable<V>
    {
        private final AtomicReference<Callable<V>> actualCallableRef;
        private final TaskDescriptorImpl<V> taskDescriptor;
        private final JiraAuthenticationContext context;
        private final BlockingCounter counter;

        private TaskCallableDecorator(final Callable<V> callable, final TaskDescriptorImpl<V> taskDescriptor,
                final JiraAuthenticationContext context, final BlockingCounter counter)
        {
            this.counter = counter;
            Assertions.notNull("callable", callable);
            Assertions.notNull("taskDescriptor", taskDescriptor);
            Assertions.notNull("context", context);

            actualCallableRef = new AtomicReference<Callable<V>>(callable);
            this.taskDescriptor = taskDescriptor;
            this.context = context;
        }

        public V call() throws Exception
        {
            preCallSetup();

            taskDescriptor.setStartedTimestamp();
            refreshTaskInTaskCache(taskDescriptor);
            counter.up();
            try
            {
                //We want the executor to forget about the callable so that it can
                //be garbage collected as we are only interested in the results. This also
                //creates a happens-before edge between the thread that created the task
                //and the thread that will execute it. This will make assignments on the creating
                //thead visible to the executing thread.
                final Callable<V> actualCallable = actualCallableRef.getAndSet(null);
                if (actualCallable != null)
                {
                    final V result = actualCallable.call();
                    taskDescriptor.setResult(result);
                    return result;
                }
                // really really unlikely in fact we reckon improssible
                throw new IllegalStateException("Callable executed twice.");
            }
            finally
            {
                postCallTearDown();
            }
        }

        private void preCallSetup()
        {
            JiraThreadLocalUtils.preCall();
            ApplicationUser user = userManager.getUserByName(taskDescriptor.getUserName());
            context.setLoggedInUser(user);
        }

        private void postCallTearDown()
        {
            taskDescriptor.setFinishedTimestamp();
            refreshTaskInTaskCache(taskDescriptor);
            counter.down();

            JiraThreadLocalUtils.postCall(log, new JiraThreadLocalUtils.ProblemDeterminationCallback()
            {
                public void onOpenTransaction()
                {
                    log.error("The task '" + taskDescriptor.getDescription() +
                            "' has left an open database transaction in play.");
                }
            });
        }
    }

    private void refreshTaskInTaskCache(final TaskDescriptorImpl<?> taskDescriptor)
    {
        taskMap.put(taskDescriptor.getTaskId(), taskDescriptor);
    }

    /**
     * Internal matcher that looks for active projects.
     */

    private static class ActiveMatcher implements TaskMatcher
    {
        private final TaskContext taskContext;

        public ActiveMatcher(final TaskContext taskContext)
        {
            this.taskContext = taskContext;
        }

        public boolean match(final TaskDescriptor<?> descriptor)
        {
            return !descriptor.isFinished() && taskContext.equals(descriptor.getTaskContext());
        }
    }

    /**
     * Internal thread factory class for threads created when executing tasks.
     */

    private static class TaskManagerThreadFactory implements ThreadFactory
    {
        private final AtomicLong threadId = new AtomicLong(0);

        @Nonnull
        public Thread newThread(@Nonnull final Runnable runnable)
        {
            final Thread t = new Thread(runnable, "JiraTaskExectionThread-" + threadId.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    }

    /**
     * Predicate matcher, always synchronises on the TaskDescriptor while matching.
     */
    private final class TaskMatcherPredicate implements Predicate<TaskDescriptor<?>>
    {
        final TaskMatcher matcher;

        TaskMatcherPredicate(@Nonnull final TaskMatcher matcher)
        {
            this.matcher = notNull("matcher", matcher);
        }

        public boolean evaluate(final TaskDescriptor<?> input)
        {
            synchronized (input)
            {
                return matcher.match(input);
            }
        }
    }

    private static class MessageConsumer implements ClusterMessageConsumer
    {

        private final Cache<Long, Future<?>> futureMap;

        public MessageConsumer(final Cache<Long, Future<?>> futureMap)
        {
            this.futureMap = futureMap;
        }

        @Override
        public void receive(final String channel, final String message, final String senderId)
        {
            if (channel.equals(TASK_CANCEL))
            {
                final long taskId = Long.valueOf(message);
                cancelTaskIfRunningLocally(taskId);
            }
        }

        public void cancelTaskIfRunningLocally(@Nonnull final Long taskId)
        {
            Future<?> future = futureMap.get(taskId);
            if (future != null)
            {
                future.cancel(false);
            }
        }

    }
}
