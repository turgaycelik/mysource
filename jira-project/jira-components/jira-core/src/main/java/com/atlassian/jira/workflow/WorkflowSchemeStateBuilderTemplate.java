package com.atlassian.jira.workflow;

import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @since v5.2
 */
abstract class WorkflowSchemeStateBuilderTemplate<B extends WorkflowSchemeStore.State.Builder<B>>
        implements WorkflowSchemeStore.State.Builder<B>
{
    private Long id;
    private Map<String, String> schemeMap;

    WorkflowSchemeStateBuilderTemplate()
    {
        this.id = null;
        this.schemeMap = newHashMap();
    }

    WorkflowSchemeStateBuilderTemplate(WorkflowSchemeStore.State state)
    {
        this.id = state.getId();
        this.schemeMap = newHashMap(state.getMappings());
    }

    @Override
    public String getDefaultWorkflow()
    {
        return schemeMap.get(null);
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public String getDefault()
    {
        return schemeMap.get(null);
    }

    @Override
    public Map<String, String> getMappings()
    {
        return schemeMap;
    }

    @Override
    public B setMappings(Map<String, String> mappings)
    {
        Assertions.notNull("mappings", mappings);

        Map<String, String> schemeMap = Maps.newHashMap();
        for (Map.Entry<String, String> entries : mappings.entrySet())
        {
            String key = entries.getKey();
            if (key != null)
            {
                if (StringUtils.isBlank(key))
                {
                    throw new IllegalArgumentException("issueTypeId cannot be blank.");
                }
                else if (key.length() > 255)
                {
                    throw new IllegalArgumentException("issueTypeId '" + key + "' is too long.");
                }
            }

            String value = entries.getValue();
            if (StringUtils.isBlank(value))
            {
                throw new IllegalArgumentException(String.format("workflowName in mappings[%s] cannot be blank.", key));
            }
            else if (value.length() > 255)
            {
                throw new IllegalArgumentException(String.format("workflowName in mappings[%s] = %s too long.", key, value));
            }

            schemeMap.put(key, value);
        }
        this.schemeMap = schemeMap;
        return getThis();
    }

    abstract B getThis();
}
