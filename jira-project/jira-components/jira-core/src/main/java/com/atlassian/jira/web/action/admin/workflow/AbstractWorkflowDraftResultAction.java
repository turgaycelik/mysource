package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.JiraWorkflow;
import org.apache.commons.lang.StringUtils;

public abstract class AbstractWorkflowDraftResultAction extends JiraWebActionSupport
{
    private JiraWorkflow jiraWorkflow;
    private Long project;
    private String issueType;

    protected final ProjectService projectService;

    protected AbstractWorkflowDraftResultAction(ProjectService projectService)
    {
        this.projectService = projectService;
    }

    protected AbstractWorkflowDraftResultAction(JiraWorkflow jiraWorkflow, ProjectService projectService)
    {
        this.jiraWorkflow = jiraWorkflow;
        this.projectService = projectService;
    }

    public JiraWorkflow getWorkflow()
    {
        return jiraWorkflow;
    }

    protected void setWorkflow(JiraWorkflow jiraWorkflow)
    {
        this.jiraWorkflow = jiraWorkflow;
    }

    public Long getProject()
    {
        return project;
    }

    public void setProject(Long project)
    {
        this.project = project;
    }

    public String getIssueType()
    {
        return issueType;
    }

    public void setIssueType(final String issueType)
    {
        this.issueType = StringUtils.stripToNull(issueType);
    }

    protected String finish(String message, String workflowName)
    {
        String url = null;

        if (getProject() != null)
        {
            final ProjectService.GetProjectResult result = projectService.getProjectByIdForAction(getLoggedInApplicationUser(), getProject(), ProjectAction.EDIT_PROJECT_CONFIG);
            if (result.isValid())
            {
                final String encodedKey = urlEncode(result.getProject().getKey());
                if (getIssueType() == null)
                {
                    url = String.format("/plugins/servlet/project-config/%s/workflows", encodedKey);
                }
                else
                {
                    url = String.format("/plugins/servlet/project-config/%s/issuetypes/%s/workflow",
                            encodedKey, urlEncode(getIssueType()));
                }
            }
        }

        if (url == null)
        {
            url = new UrlBuilder("/secure/admin/workflows/ViewWorkflowSteps.jspa")
                    .addParameter("workflowMode", "live")
                    .addParameter("workflowName", workflowName)
                    .asUrlString();
        }

        return returnCompleteWithInlineRedirectAndMsg(url,
                getText(message), MessageType.SUCCESS, false, null);
    }
}
