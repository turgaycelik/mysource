package com.atlassian.jira.rest.v2.index;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.atlassian.jira.config.ForegroundIndexTaskContext;
import com.atlassian.jira.rest.bind.DateTimeAdapter;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskProgressEvent;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;

/**
* @since v6.1.4
*/
@XmlRootElement
class ReindexBean
{
    @XmlElement
    private String progressUrl;

    @XmlElement
    private Long currentProgress;

    @XmlElement
    private Type type;

    @XmlJavaTypeAdapter (DateTimeAdapter.class)
    private Date submittedTime;

    @XmlJavaTypeAdapter (DateTimeAdapter.class)
    private Date startTime;

    @XmlJavaTypeAdapter (DateTimeAdapter.class)
    private Date finishTime;

    @XmlElement
    private boolean success;

    public ReindexBean() {}

    public ReindexBean(String progressUrl, Long currentProgress, Type type, Date submittedTime, Date startTime, Date finishTime, boolean success)
    {
        this.progressUrl = progressUrl;
        this.currentProgress = currentProgress;
        this.type = type;
        this.submittedTime = submittedTime;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.success = success;
    }

    final static ReindexBean DOC_EXAMPLE = new ReindexBean();
    static {
        DOC_EXAMPLE.progressUrl = "http://localhost:8080/jira";
        DOC_EXAMPLE.currentProgress=0L;
        DOC_EXAMPLE.type = Type.FOREGROUND;
        DOC_EXAMPLE.submittedTime = new Date();
        DOC_EXAMPLE.startTime = new Date();
        DOC_EXAMPLE.finishTime = new Date();
        DOC_EXAMPLE.success = true;
    }

    public String getProgressUrl()
    {
        return progressUrl;
    }

    public Long getCurrentProgress()
    {
        return currentProgress;
    }

    public Type getType()
    {
        return type;
    }

    public Date getSubmittedTime()
    {
        return submittedTime;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public Date getFinishTime()
    {
        return finishTime;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public enum Type
    {
        FOREGROUND,
        BACKGROUND,
        BACKGROUND_PREFFERED
    }

    public static Type fromString(String typeValue)
    {
        if (typeValue == null)
        {
            return Type.BACKGROUND_PREFFERED;
        }
        for (Type type : Type.values())
        {
            if (type.equals(Type.valueOf(typeValue.toUpperCase())))
            {
                return type;
            }
        }
        return Type.BACKGROUND_PREFFERED;
    }

    public static ReindexBean fromTaskDescriptor(@Nonnull final TaskDescriptor<IndexCommandResult> task)
            throws ExecutionException, InterruptedException
    {
        final TaskProgressEvent tpe =  task.getTaskProgressIndicator().getLastProgressEvent();
        final long progress = tpe != null ?  tpe.getTaskProgress() : 0;
        final boolean success = task.isFinished() && task.getResult().isSuccessful();
        return new ReindexBean(task.getProgressURL(),
                progress,
                indexType(task),
                task.getSubmittedTimestamp(),
                task.getStartedTimestamp(),
                task.getFinishedTimestamp(),
                success);
    }

    public static ReindexBean.Type indexType(@Nonnull final TaskDescriptor<IndexCommandResult> task)
    {
        if (task.getTaskContext() instanceof ForegroundIndexTaskContext)
        {
            return ReindexBean.Type.FOREGROUND;
        }
        return ReindexBean.Type.BACKGROUND;
    }


}
