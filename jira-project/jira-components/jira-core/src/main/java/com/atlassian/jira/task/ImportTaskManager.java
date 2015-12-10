package com.atlassian.jira.task;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;

import javax.annotation.Nonnull;

/**
 * A simplified version of the {@link TaskManager}.  This interface should only be used for data imports.
 * Implementations should *not* rely on any external dependencies since they will get refreshed during a data import
 * which can cause all sorts of issues during an import.  Also depending on any external dependencies will double memory
 * consumption potentially during an import.
 *
 * @since v4.4
 */
public interface ImportTaskManager
{

    /**
     * Returns the {@link TaskDescriptor} of the current import task that's running
     *
     * @return a {@link TaskDescriptor} or null if the manager has not such task. The descriptor returned is a snapshot
     *         of the task state when the method returns will not reflect any future changes. <code>null</code> will be
     *         returned when no matching task can be found.
     */
    <V extends Serializable> TaskDescriptor<V> getTask();

    /**
     * This submits a {@link java.util.concurrent.Callable} task to the manager which can then be started at the
     * managers discretion, but hopefully very soon. The {@link TaskDescriptor} returned is a snapshot of the task's
     * state when the method returns and will not change to reflect the task's future state changes.
     *
     * @param callable the long running task
     * @param taskName An i18nized string describing this task
     * @return a TaskDescriptor for the new long running task. The returned descriptor is a snapshot of the task state
     *         when the method returns and will not reflect any future changes.
     * @throws java.util.concurrent.RejectedExecutionException if the task manager is being shutdown and cannot accept
     * new tasks.
     * @throws AlreadyExecutingException if another import task is already running in the task manager.
     */
    <V extends Serializable> TaskDescriptor<V> submitTask(@Nonnull Callable<V> callable, final String taskName)
            throws RejectedExecutionException;

    /**
     * Attempts to aggressively stop the executing tasks and shuts down the underlying thread pool.
     */
    void shutdownNow();

    /**
     * Prepares a set of cached I18n strings used by import progress page (JRADEV-22513)
     *
     * @param locale
     */
    public void prepareCachedResourceBundleStrings(Locale locale);

    /**
     * Clears a set of cached I18n strings used by import progress page (JRADEV-22513)
     */
    public void clearCachedResourceBundleStrings();

    /**
     * Returns a set of cached I18n strings used by import progress page (JRADEV-22513)
     *
     * @return
     */
    public Map<String, String> getCachedResourceBundleStrings();

}
