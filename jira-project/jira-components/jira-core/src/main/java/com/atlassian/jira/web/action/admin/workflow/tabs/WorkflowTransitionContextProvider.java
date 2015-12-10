package com.atlassian.jira.web.action.admin.workflow.tabs;

import java.util.Map;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public abstract class WorkflowTransitionContextProvider implements ContextProvider
{

    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(final Map<String, Object> context)
    {
        return ImmutableMap.<String, Object>builder()
                .putAll(
                        Maps.filterEntries(context, new Predicate<Map.Entry<String, Object>>()
                            {
                                @Override
                                public boolean apply(final Map.Entry<String, Object> input)
                                {
                                    return input.getKey() != null && input.getValue() != null;
                                }
                            }
                        ))
                .put(WorkflowTransitionContext.COUNT_KEY, getCount(context))
                .build();
    }

    protected abstract int getCount(final Map<String,Object> context);
}
