package com.atlassian.jira.rest.v2.admin.workflowscheme;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @since v5.2
 */
public class DefaultBean
{
    @JsonProperty
    private String workflow;

    @JsonIgnore
    private boolean workflowSet = false;

    @JsonProperty
    private Boolean updateDraftIfNeeded;

    public DefaultBean() {}

    public DefaultBean(String workflow)
    {
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

    public String getWorkflow()
    {
        return workflow;
    }

    public void setWorkflow(String workflow)
    {
        this.workflowSet = true;
        this.workflow = workflow;
    }

    boolean isWorkflowSet()
    {
        return workflowSet;
    }
}
