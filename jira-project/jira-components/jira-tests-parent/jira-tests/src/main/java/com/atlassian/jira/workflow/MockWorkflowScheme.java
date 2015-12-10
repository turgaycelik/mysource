package com.atlassian.jira.workflow;

import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Map;

/**
* @since v5.2
*/
public abstract class MockWorkflowScheme implements WorkflowScheme
{
    private Long id;
    private Map<String, String> workflowMap;

    public MockWorkflowScheme()
    {
        this ((Long)null);
        workflowMap = Maps.newHashMap();
    }

    public MockWorkflowScheme(Long id)
    {
        this.id = id;
        workflowMap = Maps.newHashMap();
    }

    public MockWorkflowScheme(WorkflowScheme workflowScheme)
    {
        this.id = workflowScheme.getId();
        this.workflowMap = Maps.newHashMap(workflowScheme.getMappings());
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Nonnull
    @Override
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
    @Override
    public String getActualDefaultWorkflow()
    {
        String defaultWorkflow = workflowMap.get(null);
        if (defaultWorkflow == null)
        {
            defaultWorkflow = JiraWorkflow.DEFAULT_WORKFLOW_NAME;
        }
        return defaultWorkflow;
    }

    @Nonnull
    @Override
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

    public MockWorkflowScheme setId(Long id)
    {
        this.id = id;
        return this;
    }

    public MockWorkflowScheme setMappings(Map<String, String> workflowMap)
    {
        this.workflowMap = Maps.newHashMap(workflowMap);
        return this;
    }

    public MockWorkflowScheme setMapping(String issueType, String workflow)
    {
        this.workflowMap.put(issueType, workflow);
        return this;
    }

    public MockWorkflowScheme setDefaultWorkflow(String workflow)
    {
        this.workflowMap.put(null, workflow);
        return this;
    }

    public MockWorkflowScheme clearMappings()
    {
        this.workflowMap.clear();
        return this;
    }

    public MockWorkflowScheme removeMapping(String issueTypeId)
    {
        this.workflowMap.remove(issueTypeId);
        return this;
    }

    public MockWorkflowScheme removeDefault()
    {
        this.workflowMap.remove(null);
        return this;
    }

    public MockWorkflowScheme removeWorkflow(String workflowName)
    {
        for (Iterator<String> iterator = workflowMap.values().iterator(); iterator.hasNext(); )
        {
            String s = iterator.next();
            if (workflowName.equals(s))
            {
                iterator.remove();
            }
        }
        return this;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        MockWorkflowScheme that = (MockWorkflowScheme) o;

        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (workflowMap != null ? !workflowMap.equals(that.workflowMap) : that.workflowMap != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (workflowMap != null ? workflowMap.hashCode() : 0);
        return result;
    }
}
