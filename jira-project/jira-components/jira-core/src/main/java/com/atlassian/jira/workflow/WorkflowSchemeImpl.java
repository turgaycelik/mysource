package com.atlassian.jira.workflow;

import javax.annotation.Nonnull;
import java.util.Map;

class WorkflowSchemeImpl extends AbstractWorkflowScheme implements AssignableWorkflowScheme
{
    private final String name;
    private final String description;

    WorkflowSchemeImpl(AssignableWorkflowSchemeStore.AssignableState state)
    {
        super(state.getId(), state.getMappings());
        this.name = state.getName();
        this.description = state.getDescription();
    }

    WorkflowSchemeImpl(Long id, String name, String description, Map<String, String> workflowMap)
    {
        super(id, workflowMap);
        this.name = name;
        this.description = description;
    }

    @Override
    public boolean isDraft()
    {
        return false;
    }

    @Override
    public boolean isDefault()
    {
        return false;
    }

    @Nonnull
    @Override
    public AssignableWorkflowScheme.Builder builder()
    {
        return new AssignableWorkflowSchemeBuilder(this);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }
}
