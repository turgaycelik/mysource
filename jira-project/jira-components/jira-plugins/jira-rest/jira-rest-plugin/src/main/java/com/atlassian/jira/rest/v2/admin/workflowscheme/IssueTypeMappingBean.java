package com.atlassian.jira.rest.v2.admin.workflowscheme;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @since v5.2
 */
public class IssueTypeMappingBean
{
    @JsonProperty
    private String issueType;

    @JsonProperty
    private String workflow;

    @JsonIgnore
    private boolean workflowSet;

    @JsonProperty
    private Boolean updateDraftIfNeeded;

    public IssueTypeMappingBean()
    {
    }

    public String getIssueType()
    {
        return issueType;
    }

    public void setIssueType(String issueType)
    {
        this.issueType = issueType;
    }

    public String getWorkflow()
    {
        return workflow;
    }

    public void setWorkflow(String workflow)
    {
        this.workflowSet = true;
        this.workflow = workflow;
    }

    public Boolean getUpdateDraftIfNeeded()
    {
        return updateDraftIfNeeded;
    }

    public void setUpdateDraftIfNeeded(Boolean updateDraftIfNeeded)
    {
        this.updateDraftIfNeeded = updateDraftIfNeeded;
    }

    boolean isWorkflowSet()
    {
        return workflowSet;
    }
}
