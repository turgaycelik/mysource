package com.atlassian.jira.workflow;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

import static com.atlassian.jira.workflow.AssignableWorkflowScheme.Builder;

/**
 * @since v5.2
 */
class AssignableWorkflowSchemeBuilder extends WorkflowSchemeBuilderTemplate<Builder>
        implements Builder
{
    private String name;
    private String description;

    AssignableWorkflowSchemeBuilder()
    {
    }

    AssignableWorkflowSchemeBuilder(AssignableWorkflowScheme scheme)
    {
        super(scheme);
        this.name = scheme.getName();
        this.description = scheme.getDescription();
    }

    @Override
    Builder builder()
    {
        return this;
    }

    @Nonnull
    @Override
    public Builder setName(@Nonnull String name)
    {
        this.name = StringUtils.stripToNull(name);
        return this;
    }

    @Override
    @Nonnull
    public Builder setDescription(String description)
    {
        this.description = StringUtils.stripToNull(description);
        return this;
    }

    @Nonnull
    @Override
    public AssignableWorkflowScheme build()
    {
        return new WorkflowSchemeImpl(getId(), name, description, getMappings());
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

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public String getName()
    {
        return name;
    }
}
