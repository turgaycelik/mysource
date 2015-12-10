package com.atlassian.jira.web.action.admin.task;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Simply cleans up a task and then redirects to a given url
 *
 * @since v3.13
 */
@WebSudoRequired
public class AcknowledgeTask extends JiraWebActionSupport
{
    private Long taskId;
    private String destinationURL;
    private final TaskManager taskManager;
    private final JiraAuthenticationContext authenticationContext;

    public AcknowledgeTask(final TaskManager taskManager, final JiraAuthenticationContext authenticationContext)
    {
        this.taskManager = taskManager;
        this.authenticationContext = authenticationContext;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (taskId != null)
        {
            // is the same user that started the task
            final TaskDescriptor descriptor = taskManager.getTask(taskId);
            if ((descriptor != null) && descriptor.isFinished())
            {
                final User currentUser = authenticationContext.getLoggedInUser();
                if (currentUser != null)
                {
                    if (!currentUser.getName().equals(descriptor.getUserName()))
                    {
                        addErrorMessage(getText("common.tasks.cant.acknowledge.task.you.didnt.start", descriptor.getUserName()));
                        return ERROR;
                    }
                }
                taskManager.removeTask(taskId);
            }
        }
        if (destinationURL != null)
        {
            return getRedirect(destinationURL);
        }
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

    public String getDestinationURL()
    {
        return destinationURL;
    }

    public void setDestinationURL(final String destinationURL)
    {
        this.destinationURL = getRedirectSanitiser().makeSafeRedirectUrl(destinationURL);
    }
}
