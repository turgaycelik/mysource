package com.atlassian.jira.rest.v2.admin.workflowscheme;

import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
* @since v5.2
*/
public class WorkflowMappingBean
{
    @JsonProperty
    private String workflow;

    @JsonProperty
    private List<String> issueTypes;

    @JsonProperty
    private Boolean defaultMapping;

    @JsonProperty
    private Boolean updateDraftIfNeeded;

    WorkflowMappingBean(String workflow, Iterable<String> issueTypes)
    {
        this.workflow = workflow;
        this.issueTypes = Lists.newArrayList(issueTypes);
    }

    WorkflowMappingBean(String workflow, String... issueTypes)
    {
        this.workflow = workflow;
        this.issueTypes = Arrays.asList(issueTypes);
    }

    WorkflowMappingBean(String workflow)
    {
        this.workflow = workflow;
    }

    public WorkflowMappingBean()
    {
    }

    public List<String> getIssueTypes()
    {
        return issueTypes;
    }

    public void setIssueTypes(List<String> issueTypes)
    {
        this.issueTypes = issueTypes;
    }

    public String getWorkflow()
    {
        return workflow;
    }

    public void setWorkflow(String workflow)
    {
        this.workflow = workflow;
    }

    void addIssueType(String issueType)
    {
        if (issueTypes == null)
        {
            issueTypes = Lists.newArrayList();
        }
        issueTypes.add(issueType);
    }

    public Boolean isDefaultMapping()
    {
        return defaultMapping;
    }

    public void setDefaultMapping(Boolean defaultMapping)
    {
        this.defaultMapping = defaultMapping;
    }

    public Boolean isUpdateDraftIfNeeded()
    {
        return updateDraftIfNeeded;
    }

    public void setUpdateDraftIfNeeded(Boolean updateDraftIfNeeded)
    {
        this.updateDraftIfNeeded = updateDraftIfNeeded;
    }
}
