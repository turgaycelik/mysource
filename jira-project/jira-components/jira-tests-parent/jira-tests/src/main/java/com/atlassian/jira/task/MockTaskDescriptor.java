package com.atlassian.jira.task;

import java.io.Serializable;
import java.util.Date;

/**
* @since v5.1
*/
public class MockTaskDescriptor<V extends Serializable> implements TaskDescriptor<V>
{
    private V result;
    private Date submittedTime;
    private Date startedTime;
    private Date finishedTime;
    private TaskProgressIndicator taskProgressIndicator;
    private String description;
    private Long taskId;
    private long elapsedRunTime;
    private TaskContext taskContext;
    private String userName;
    private boolean cancelled;

    public MockTaskDescriptor()
    {
        clear();
    }

    public void clear()
    {
        elapsedRunTime = 0;
        taskId = null;
        result = null;
        submittedTime = null;
        startedTime = null;
        finishedTime = null;
        taskProgressIndicator = null;
        description = null;
        taskContext = null;
        userName = null;
    }

    public V getResult()
    {
        return result;
    }

    public boolean isStarted()
    {
        return startedTime != null;
    }

    public boolean isFinished()
    {
        return finishedTime != null;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public Date getStartedTimestamp()
    {
        return startedTime;
    }

    public Date getFinishedTimestamp()
    {
        return finishedTime;
    }

    public Date getSubmittedTimestamp()
    {
        return submittedTime;
    }

    public long getElapsedRunTime()
    {
        return elapsedRunTime;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getDescription()
    {
        return description;
    }

    public TaskContext getTaskContext()
    {
        return taskContext;
    }

    public String getProgressURL()
    {
        return "/userUrl?user=";
    }

    public TaskProgressIndicator getTaskProgressIndicator()
    {
        return taskProgressIndicator;
    }

    public void setResult(final V result)
    {
        this.result = result;
    }

    public void setSubmittedTime(final Date submittedTime)
    {
        this.submittedTime = submittedTime;
    }

    public void setStartedTime(final Date startedTime)
    {
        this.startedTime = startedTime;
    }

    public void setFinishedTime(final Date finishedTime)
    {
        this.finishedTime = finishedTime;
    }

    public void setTaskProgressIndicator(final TaskProgressIndicator taskProgressIndicator)
    {
        this.taskProgressIndicator = taskProgressIndicator;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public void setTaskId(final Long taskId)
    {
        this.taskId = taskId;
    }

    public void setElapsedRunTime(final long elapsedRunTime)
    {
        this.elapsedRunTime = elapsedRunTime;
    }

    public void setTaskContext(final TaskContext taskContext)
    {
        this.taskContext = taskContext;
    }

    public void setUserName(final String userName)
    {
        this.userName = userName;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancellable()
    {
        return false;
    }
}
