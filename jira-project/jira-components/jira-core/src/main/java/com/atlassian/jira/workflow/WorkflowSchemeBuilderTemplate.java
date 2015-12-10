package com.atlassian.jira.workflow;

import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Map;

/**
 * @since v5.2
 */
abstract class WorkflowSchemeBuilderTemplate<T extends WorkflowScheme.Builder<T>> implements WorkflowScheme.Builder<T>
{
    private final Long id;
    private Map<String, String> mappings;

    WorkflowSchemeBuilderTemplate(Long id, Map<String, String> mappings)
    {
        this.id = id;
        this.mappings = Maps.newHashMap(mappings);
    }

    WorkflowSchemeBuilderTemplate()
    {
        this.id = null;
        this.mappings = Maps.newHashMap();
    }

    WorkflowSchemeBuilderTemplate(WorkflowScheme scheme)
    {
        this.id = scheme.getId();
        this.mappings = Maps.newHashMap(scheme.getMappings());
    }

    @Override
    @Nonnull
    public T setDefaultWorkflow(@Nonnull String workflowName)
    {
        mappings.put(null, workflowName);
        return builder();
    }

    @Override
    @Nonnull
    public T setMapping(@Nonnull String issueTypeId, @Nonnull String workflowName)
    {
        mappings.put(issueTypeId, workflowName);
        return builder();
    }

    @Override
    @Nonnull
    public T setMappings(@Nonnull Map<String, String> mappings)
    {
        this.mappings = Maps.newHashMap(mappings);
        return builder();
    }

    @Nonnull
    @Override
    public T removeMapping(@Nonnull String issueTypeId)
    {
        this.mappings.remove(issueTypeId);
        return builder();
    }

    @Nonnull
    @Override
    public T removeDefault()
    {
        mappings.remove(null);
        return builder();
    }

    @Nonnull
    @Override
    public T clearMappings()
    {
        mappings.clear();
        return builder();
    }

    @Nonnull
    @Override
    public T removeWorkflow(@Nonnull String workflowName)
    {
        for (Iterator<String> iterator = mappings.values().iterator(); iterator.hasNext(); )
        {
            String s = iterator.next();
            if (workflowName.equals(s))
            {
                iterator.remove();
            }
        }
        return builder();
    }

    @Override
    public String getDefaultWorkflow()
    {
        return mappings.get(null);
    }

    @Override
    public String getMapping(@Nonnull String issueTypeId)
    {
        return mappings.get(issueTypeId);
    }

    public Long getId()
    {
        return id;
    }

    public Map<String, String> getMappings()
    {
        return mappings;
    }

    abstract T builder();
}
