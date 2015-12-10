package com.atlassian.jira.task;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import javax.annotation.Nonnull;

/**
 * The <code>TaskManager</code> contains a collection of tasks that are long running.  It schedules them to run
 * at its discretion, but it does try to run them as soon as possible, assuming there is sufficient resources available.
 * </p>
 * Tasks are described operationally by {@link TaskDescriptor}.
 * <p/>
 * NOTE: All task descriptors returned are a snapshot of the task state when the method returns.  So while the task may finish, the TaskDescriptor will
 * not be updated to reflect that.  You must always re-ask the TaskManager for a new TaskDescriptor to find out if a task has finished or not.
 *
 * @since v3.13
 */
public interface TaskManager
{
    /**
     * Return a collection of {@link TaskDescriptor}s currently within the manager.  This will include
     * tasks that are yet to start, tasks that are running and completed tasks.
     * <p/>
     * The returned collection is sorted by task id, which should be in addition order.
     *
     * @return a collection of {@link com.atlassian.jira.task.TaskDescriptor}s. The descriptors returned are a snapshot
     * of each task state when the method returns will not reflect any future changes. An empty collection
     * is returned when there are no current tasks.
     */
    Collection<TaskDescriptor<?>> getAllTasks();

    /**
     * Return a collection of sumitted or running {@link com.atlassian.jira.task.TaskDescriptor}s in the manager.
     * <p/>
     * The returned collection is sorted by task id, which should be in addition order.
     *
     * @return a collection of running {@link com.atlassian.jira.task.TaskDescriptor}s. The descriptors returned are a snapshot
     * of each task state when the method returns and will not reflect any future changes. An empty collection
     * is returned when there are no descriptors to return.
     */
    Collection<TaskDescriptor<?>> getLiveTasks();

    /**
     * Return the live task associated with the passed context, if one exists.
     *
     * @param taskContext the context to search for.
     * @return currently live task associated with the passed context. The descriptor returned is a snapshot
     * of the task state when the method returns will not reflect any future changes. <code>null</code> will be returned
     * when no matching task can be found.
     */
    <V extends Serializable> TaskDescriptor<V> getLiveTask(@Nonnull TaskContext taskContext);

    /**
     * Returns the {@link TaskDescriptor} of the task that is identified by the passed id.
     *
     * @param taskId the id of the task to retrieve, if null then null will be returned
     * @return a {@link TaskDescriptor} or null if the manager has not such task. The descriptor returned is a snapshot
     * of the task state when the method returns will not reflect any future changes. <code>null</code> will be returned
     * when no matching task can be found.
     */
    <V extends Serializable> TaskDescriptor<V> getTask(Long taskId);

    /**
     * This submits a {@link Callable} task to the manager which can then be started at the managers discretion, but hopefully very soon.
     * The {@link TaskDescriptor} returned is a snapshot of the task's state when the method returns and will not change to reflect
     * the task's future state changes. The task's ID can be found in the returned TaskDescriptor.
     * The task is not cancellable.
     *
     * @param callable        the long running task
     * @param taskDescription the description of the task
     * @param taskContext     some stateful context that the task knows about
     * @return a TaskDescriptor for the new long running task. The returned descriptor is a snapshot
     * of the task state when the method returns and will not reflect any future changes.
     * @throws RejectedExecutionException if the task manager is being shutdown and cannot accept new tasks.
     * @throws AlreadyExecutingException if a task with an equal TaskContext is already running in the task manager.
     */
    <V extends Serializable> TaskDescriptor<V> submitTask(@Nonnull Callable<V> callable, @Nonnull String taskDescription, @Nonnull TaskContext taskContext) throws RejectedExecutionException, AlreadyExecutingException;


    /**
     * This submits a {@link Callable} task to the manager which can then be started at the managers discretion, but hopefully very soon.
     * The {@link TaskDescriptor} returned is a snapshot of the task's state when the method returns and will not change to reflect
     * the task's future state changes. The task's ID can be found in the returned TaskDescriptor.
     *
     * @param callable        the long running task
     * @param taskDescription the description of the task
     * @param taskContext     some stateful context that the task knows about
     * @param cancellable     If set to true indicates that this potentially long running task supports being cancelled.
     * @return a TaskDescriptor for the new long running task. The returned descriptor is a snapshot
     * of the task state when the method returns and will not reflect any future changes.
     * @throws RejectedExecutionException if the task manager is being shutdown and cannot accept new tasks.
     * @throws AlreadyExecutingException if a task with an equal TaskContext is already running in the task manager.
     * @since v5.2
     */
    <V extends Serializable> TaskDescriptor<V> submitTask(@Nonnull Callable<V> callable, @Nonnull String taskDescription, @Nonnull TaskContext taskContext, boolean cancellable) throws RejectedExecutionException, AlreadyExecutingException;

    /**
     * Remove a task from the manager. The task will still continue to execute if it has
     * not already completed.
     *
     * @param taskId the task identifier.
     */
    void removeTask(@Nonnull Long taskId);

    /**
     * Cancel a running task. If the task is not running this message is a no-op.
     *
     * @param taskId the task identifier.
     * @throws IllegalStateException if the task is not cancellable
     */
    void cancelTask(@Nonnull Long taskId) throws IllegalStateException;

    /**
     * Cancel a running task if it is running on this cluster node.
     *
     * @param taskId the task identifier.
     */
    void cancelTaskIfRunningLocally(@Nonnull Long taskId);

    /**
     * Return true if this task has been cancelled.
     * @param taskId the task identifier.
     * @return true if this task has been cancelled
     */
    boolean isCancelled(Long taskId);

    /**
     * Returns true if the task can be cancelled.
     * This does not take into account if the task is actually running or not.
     *
     * @param taskId the task identifier.
     * @return true if the task can be cancelled.
     */
    boolean isCancellable(@Nonnull Long taskId);

    /**
     * Start the task manager.  Normally the task manager starts on construction, but if it is shutdown, you may need to restart it.
     * If the task manager is not shutdown, does nothing.
     */
    void start();

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted.  It will wait for at most waitSeconds and then
     * return true if the underlying execution threads are terminated completely.
     *
     * @param waitSeconds the numder of seconds to wait for graceful shutdown.
     * @return true if the underlying execution threads have terminated completely.
     */
    boolean shutdownAndWait(long waitSeconds);

    /** Attempts to aggressively stop all actively executing tasks, halts the processing of waiting tasks. */
    void shutdownNow();

    /**
     * Wait for a specific task to complete.
     * @param taskId Id of task to wait for
     */
    void waitUntilTaskCompletes(Long taskId) throws ExecutionException, InterruptedException;

    /**
     * Waits on the TaskManager until all current active tasks are completed. Calling this method does not stop tasks
     * being added.
     *
     * @param seconds the number of seconds to wait.
     * @return true if all the active tasks are completed before the timeout, false otherwise.
     */
    boolean awaitUntilActiveTasksComplete(long seconds);

    /**
     * Returns true if the there are any tasks (submitted, running or finished) that have a task context
     * that is EQUAL to the passed in taskContext.
     *
     * @param taskContext the specific task context (which implements equals() propertly)
     * @return true if there are any tasks with an equal task context
     */
    boolean hasTaskWithContext(@Nonnull TaskContext taskContext);

    /**
     * Returns true if the there are live tasks (running or submitted) that have a task context
     * that is EQUAL to the passed in taskContext.
     *
     * @param taskContext the specific task context (which implements equals() propertly)
     * @return true if there are any tasks with an equal task context
     */
    boolean hasLiveTaskWithContext(@Nonnull TaskContext taskContext);

    /**
     * Find the first task that "matches" according to the passed matcher.
     *
     * @param matcher the condition used to find the task.
     * @return the first task that "matches" according to the passed matcher. The descriptor returned is a snapshot
     * of the task state when the method returns will not reflect any future changes. <code>null</code> will be returned
     * when no matching task can be found.
     */
    TaskDescriptor<?> findFirstTask(@Nonnull TaskMatcher matcher);

    /**
     * Find all the tasks that "match" according to the passed matcher.
     *
     * @param matcher the condition used to find the task.
     * @return the tasks that "match" according to the passed matcher. The descriptors returned are a snapshot
     * of the task state when the method returns will not reflect any future changes. An empty collection
     * is returned when no tasks are matched.
     */
    Collection<TaskDescriptor<?>> findTasks(@Nonnull TaskMatcher matcher);
}
