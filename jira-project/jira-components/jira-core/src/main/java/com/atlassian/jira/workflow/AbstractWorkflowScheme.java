package com.atlassian.jira.workflow;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.unmodifiableMap;

/**
 * @since v5.2
 */
abstract class AbstractWorkflowScheme implements WorkflowScheme
{
    private final Long id;
    private final Map<String, String> workflowMap;

    AbstractWorkflowScheme(Long id, Map<String, String> workflowMap)
    {
        this.id = id;
        this.workflowMap = unmodifiableMap(newHashMap(workflowMap));
    }

    @Nonnull
    public String getActualWorkflow(String issueTypeId)
    {
        String workflow = workflowMap.get(issueTypeId);
        if (workflow == null)
        {
            workflow = getActualDefaultWorkflow();
        }
        return workflow;
    }

    @Nonnull
    public String getActualDefaultWorkflow()
    {
        String workflow = workflowMap.get(null);
        if (workflow == null)
        {
            workflow = JiraWorkflow.DEFAULT_WORKFLOW_NAME;
        }
        return workflow;
    }

    public Long getId()
    {
        return id;
    }

    @Nonnull
    public Map<String, String> getMappings()
    {
        return workflowMap;
    }

    @Override
    public String getConfiguredDefaultWorkflow()
    {
        return workflowMap.get(null);
    }

    @Override
    public String getConfiguredWorkflow(String issueTypeId)
    {
        return workflowMap.get(issueTypeId);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        AbstractWorkflowScheme that = (AbstractWorkflowScheme) o;

        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }
}
