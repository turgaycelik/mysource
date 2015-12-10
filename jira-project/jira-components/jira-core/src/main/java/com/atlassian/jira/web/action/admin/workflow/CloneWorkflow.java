/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.names.WorkflowCopyNameFactory;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

@WebSudoRequired
public class CloneWorkflow extends JiraWebActionSupport
{
    private final JiraWorkflow workflow;
    private final WorkflowService workflowService;
    private final WorkflowCopyNameFactory workflowCopyNameFactory;

    private String description;
    private String newWorkflowName;

    public CloneWorkflow(JiraWorkflow workflow, WorkflowService workflowService, WorkflowCopyNameFactory workflowCopyNameFactory)
    {
        this.workflow = workflow;
        this.workflowService = workflowService;
        this.workflowCopyNameFactory = workflowCopyNameFactory;
    }

    public String doDefault() throws Exception
    {
        newWorkflowName = workflowCopyNameFactory.createFrom(getWorkflow().getName(), getLocale());
        if (TextUtils.stringSet(getWorkflow().getDescription()))
        {
            setDescription(getWorkflow().getDescription());
        }

        return super.doDefault();
    }

    protected void doValidation()
    {
        workflowService.validateCopyWorkflow(getJiraServiceContext(), StringUtils.trim(newWorkflowName));

        super.doValidation();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final JiraWorkflow jiraWorkflow = workflowService.copyWorkflow(getJiraServiceContext(), StringUtils.trim(newWorkflowName), getDescription(), getWorkflow());
        if (jiraWorkflow == null || hasAnyErrors())
        {
            return ERROR;
        }
        else
        {
            UrlBuilder urlBuilder = new UrlBuilder("EditWorkflowDispatcher.jspa")
                    .addParameter("wfName", jiraWorkflow.getName())
                    .addParameter("atl_token", getXsrfToken());
            return returnCompleteWithInlineRedirect(urlBuilder.asUrlString());
        }
    }

    public String getNewWorkflowName()
    {
        return newWorkflowName;
    }

    public void setNewWorkflowName(String newWorkflowName)
    {
        this.newWorkflowName = newWorkflowName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public JiraWorkflow getWorkflow()
    {
        return workflow;
    }
}
