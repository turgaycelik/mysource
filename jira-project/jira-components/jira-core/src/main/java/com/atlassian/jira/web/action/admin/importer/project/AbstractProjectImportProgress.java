package com.atlassian.jira.web.action.admin.importer.project;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.TaskDescriptorBean;

/**
 * Base class for the progress screens. Handles the validation and when the task is finished it will call the
 * handleFinishedTask method with the finished task.
 *
 * @since v3.13
 */
public abstract class AbstractProjectImportProgress<V extends Serializable> extends JiraWebActionSupport
{
    private static final long serialVersionUID = -3876535656531565166L;

    private String redirectOnComplete;
    private TaskDescriptorBean<V> ourTask;
    private final TaskManager taskManager;
    private final TaskDescriptorBean.Factory taskBeanFactory;

    public AbstractProjectImportProgress(final TaskManager taskManager, final TaskDescriptorBean.Factory factory)
    {
        this.taskManager = taskManager;
        this.taskBeanFactory = factory;
    }

    @Override
    public void doValidation()
    {
        // Check that we have what we need for this page, otherwise error out
        final Long taskId = ProjectImportBean.getProjectImportBeanFromSession().getTaskProgressInformation().getTaskId();
        final TaskDescriptor<V> descriptor = taskManager.getTask(taskId);
        if ((taskId == null) || (descriptor == null))
        {
            // There is a possibility that the user clicks refresh at the same time that the browser does an auto refresh.
            if (!taskIsComplete())
            {
                addErrorMessage(getText("admin.project.import.progress.no.task.id"));
            }
        }
        else
        {
            ourTask = taskBeanFactory.create(descriptor);
        }
    }

    @Override
    protected String doExecute() throws Exception
    {
        // This is a special case where the user has clicked refresh but we have already finished processing our task
        // and we therefore need to rely on the progress implementation to look for its required session objects to
        // determine if we should move on to the next screen.
        // NOTE: if ourTask is null then taskIsComplete() will always be true otherwise there will be an error added.
        if ((ourTask == null) && taskIsComplete())
        {
            return getRedirect(getRedirectOnComplete());
        }
        if (ourTask.isFinished())
        {
            return handleFinishedTask(ourTask);
        }
        return INPUT;
    }

    protected abstract boolean taskIsComplete();

    protected abstract String handleFinishedTask(final TaskDescriptorBean<V> ourTask) throws ExecutionException, InterruptedException;

    protected abstract String getSubmitUrl();

    public String getRedirectOnComplete()
    {
        return redirectOnComplete;
    }

    public void setRedirectOnComplete(final String redirectOnComplete)
    {
        this.redirectOnComplete = redirectOnComplete;
    }

    public TaskDescriptorBean<?> getOurTask()
    {
        return ourTask;
    }
}
