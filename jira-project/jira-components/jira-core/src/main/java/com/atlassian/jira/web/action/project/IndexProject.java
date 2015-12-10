package com.atlassian.jira.web.action.project;

import java.util.concurrent.ExecutionException;

import com.atlassian.jira.bc.project.index.ProjectIndexTaskContext;
import com.atlassian.jira.bc.project.index.ProjectReindexService;
import com.atlassian.jira.config.IndexTaskContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;
import com.atlassian.jira.web.bean.TaskDescriptorBean;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * @since v6.1
 */
@WebSudoRequired
public class IndexProject extends ViewProject
{

    private final TaskManager taskManager;
    private final TaskDescriptorBean.Factory taskBeanFactory;
    private final ProjectReindexService projectReindexService;

    public IndexProject(TaskManager taskManager, final TaskDescriptorBean.Factory factory, final ProjectReindexService projectReindexService)
    {
        this.taskManager = taskManager;
        this.taskBeanFactory = factory;
        this.projectReindexService = projectReindexService;
    }

    private Long taskId;
    private TaskDescriptorBean<IndexCommandResult> currentTask;

    private long reindexTime = 0;
    private TaskDescriptor<IndexCommandResult> currentTaskDescriptor;

    @Override
    protected String doExecute()
    {
        final Project projectObject = getProjectObject();
        if (projectObject == null)
        {
            return getRedirect("/plugins/servlet/project-config/" + "UNKNOWN" + "/summary");
        }
        final TaskDescriptor<IndexCommandResult> taskDescriptor = getCurrentTaskDescriptor();
        if (taskDescriptor != null)
        {
            return getRedirect(taskDescriptor.getProgressURL());
        }
        else
        {
            return getRedirect(projectReindexService.reindex(getProjectObject()));
        }
    }

    public String doProgress() throws ExecutionException, InterruptedException
    {
        if (taskId == null)
        {
            addErrorMessage(getText("admin.indexing.no.task.id"));
            return ERROR;
        }
        currentTaskDescriptor = taskManager.getTask(taskId);
        if (currentTaskDescriptor == null)
        {
            addErrorMessage(getText("admin.indexing.no.task.found"));
            return ERROR;
        }
        final TaskContext context = currentTaskDescriptor.getTaskContext();
        if (!(context instanceof ProjectIndexTaskContext))
        {
            addErrorMessage(getText("common.tasks.wrong.task.context", IndexTaskContext.class.getName(), context.getClass().getName()));
            return ERROR;
        }

        currentTask = taskBeanFactory.create(currentTaskDescriptor);
        if (currentTaskDescriptor.isFinished() && !currentTaskDescriptor.isCancelled())
        {
            final IndexCommandResult result = currentTaskDescriptor.getResult();
            if (result.isSuccessful())
            {
                reindexTime = result.getReindexTime();
            }
            else
            {
                addErrorCollection(result.getErrorCollection());
            }
        }
        return "progress";
    }

    public String doCancel() throws ExecutionException, InterruptedException
    {
        if (taskId == null)
        {
            addErrorMessage(getText("admin.indexing.no.task.id"));
            return ERROR;
        }
        currentTaskDescriptor = taskManager.getTask(taskId);
        if (currentTaskDescriptor == null)
        {
            addErrorMessage(getText("admin.indexing.no.task.found"));
            return ERROR;
        }
        final TaskContext context = currentTaskDescriptor.getTaskContext();
        if (!(context instanceof ProjectIndexTaskContext))
        {
            addErrorMessage(getText("common.tasks.wrong.task.context", ProjectIndexTaskContext.class.getName(), context.getClass().getName()));
            return ERROR;
        }

        taskManager.cancelTask(taskId);

        // We need to get the task descriptor again because the one we got before is just a shallow copy of the real thing.
        currentTaskDescriptor = taskManager.getTask(taskId);
        currentTask = taskBeanFactory.create(currentTaskDescriptor);

        return SUCCESS;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(final Long taskId)
    {
        this.taskId = taskId;
    }

    public long getReindexTime()
    {
        return reindexTime;
    }

    public TaskDescriptorBean<IndexCommandResult> getOurTask()
    {
        return currentTask;
    }

    public TaskDescriptorBean<IndexCommandResult> getCurrentTask()
    {
        if (currentTask == null)
        {
            final TaskDescriptor<IndexCommandResult> taskDescriptor = getCurrentTaskDescriptor();
            if (taskDescriptor != null)
            {
                currentTask = taskBeanFactory.create(taskDescriptor);
            }
        }
        return currentTask;
    }

    private TaskDescriptor<IndexCommandResult> getCurrentTaskDescriptor()
    {
        if (currentTaskDescriptor == null)
        {
            currentTaskDescriptor = taskManager.getLiveTask(new ProjectIndexTaskContext(getProjectObject()));
        }

        return currentTaskDescriptor;
    }

    public String getDestinationURL()
    {
        final Project projectObject = getProjectObject();
        if (projectObject != null)
        { return "/plugins/servlet/project-config/" + projectObject.getKey() + "/summary"; }
        return "/plugins/servlet/project-config/" + "UNKNOWN" + "/summary";
    }
}
