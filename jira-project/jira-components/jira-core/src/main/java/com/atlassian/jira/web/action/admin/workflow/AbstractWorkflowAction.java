package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowUtil;

import com.google.common.base.Objects;

import org.apache.commons.lang.StringUtils;

public abstract class AbstractWorkflowAction extends JiraWebActionSupport
{
    protected final JiraWorkflow workflow;
    private Long project;
    private String issueType;

    public AbstractWorkflowAction(JiraWorkflow workflow)
    {
        this.workflow = workflow;
    }

    public JiraWorkflow getWorkflow()
    {
        return workflow;
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

    public static String getFullModuleKey(final String pluginKey, final String moduleKey)
    {
        if (pluginKey == null && moduleKey == null)
        {
            return StringUtils.EMPTY;
        }

        return pluginKey + Objects.firstNonNull(moduleKey, StringUtils.EMPTY);
    }


    protected String getBasicWorkflowParameters()
    {
        StringBuilder paramBuilder = new StringBuilder("?workflowName=")
                .append(JiraUrlCodec.encode(getWorkflow().getName()))
                .append("&workflowMode=").append(getWorkflow().getMode());

        if (getProject() != null)
        {
            paramBuilder.append("&project=").append(getProject());
        }

        if (getIssueType() != null)
        {
            paramBuilder.append("&issueType=").append(JiraUrlCodec.encode(getIssueType()));
        }

        return paramBuilder.toString();
    }

    /**
     * Return the display name of the current workflow.
     *
     * @return the display name of the current workflow.
     */
    public String getWorkflowDisplayName()
    {
        return WorkflowUtil.getWorkflowDisplayName(workflow);
    }
}
