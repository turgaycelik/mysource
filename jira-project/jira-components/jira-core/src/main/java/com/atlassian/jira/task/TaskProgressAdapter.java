package com.atlassian.jira.task;

import java.io.Serializable;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Acts as an adapter between a {@link TaskProgressSink} and a {@link TaskProgressIndicator}
 *
 * @since v3.13
 */
class TaskProgressAdapter implements TaskProgressIndicator, TaskProgressSink, Serializable
{
    private static final long serialVersionUID = 7455317280573447044L;

    private final AtomicReference<TaskProgressEvent> lastTaskProgressEvent = new AtomicReference<TaskProgressEvent>(null);
    private transient volatile TaskDescriptor<?> taskDescriptor = null;
    private transient final List<EventListener> listenerList = new CopyOnWriteArrayList<EventListener>();

    TaskProgressAdapter()
    {}

    public void addListener(final TaskProgressListener listener)
    {
        listenerList.add(listener);
    }

    public void removeListener(final TaskProgressListener listener)
    {
        listenerList.remove(listener);
    }

    public TaskProgressEvent getLastProgressEvent()
    {
        return lastTaskProgressEvent.get();
    }

    public void makeProgress(final long taskProgress, final String currentSubTask, final String message)
    {
        final long elapsedRunTime = taskDescriptor.getElapsedRunTime();
        final TaskProgressEvent tpe = new TaskProgressEvent(taskDescriptor.getTaskId(), elapsedRunTime, taskProgress, currentSubTask, message);
        // record just one last progress event.  Maybe we can use more in the future.
        lastTaskProgressEvent.set(tpe);

        for (final Object element : listenerList)
        {
            final TaskProgressListener taskProgressListener = (TaskProgressListener) element;
            taskProgressListener.onProgressMade(tpe);
        }
    }

    /**
     * Sneaky back door way to change the TaskDescriptor.
     *
     * @param taskDescriptor the thing to set
     */
    void setTaskDescriptor(final TaskDescriptor taskDescriptor)
    {
        this.taskDescriptor = taskDescriptor;
    }
}
