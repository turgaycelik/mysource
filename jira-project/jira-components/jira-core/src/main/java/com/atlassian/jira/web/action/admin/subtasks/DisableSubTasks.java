package com.atlassian.jira.web.action.admin.subtasks;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collection;

@WebSudoRequired
public class DisableSubTasks extends JiraWebActionSupport
{
    private final SubTaskManager subTaskManager;

    private int subTaskCount;

    public DisableSubTasks(SubTaskManager subTaskManager)
    {
        this.subTaskManager = subTaskManager;
    }

    @RequiresXsrfCheck
    public String doDefault() throws Exception
    {
        // Find all sub-tasks in the system
        final Collection subTaskIssueIds = subTaskManager.getAllSubTaskIssueIds();
        if (subTaskIssueIds.size() > 0)
        {
            subTaskCount = subTaskIssueIds.size();
            // We have sub-takss in the system - somthing needs to be done about them
            // Ask user for input
            return super.doDefault();
        }
        else
        {
            // No sub-tasks exist. We can safely disable them
            subTaskManager.disableSubTasks();
            return getRedirect();
        }
    }

    private String getRedirect() throws Exception
    {
        return getRedirect("ManageSubTasks.jspa");
    }

    protected String doExecute() throws Exception
    {
        return getRedirect();
    }

    public int getSubTaskCount()
    {
        return subTaskCount;
    }
}
