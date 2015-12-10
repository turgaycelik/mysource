package com.atlassian.jira.workflow;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @since v5.2
 */
public class MockAssignableWorkflowSchemeStore implements AssignableWorkflowSchemeStore
{
    private long lastId = 0;
    private Map<Long, AssignableState> workflowState = Maps.newHashMap();

    @Override
    public AssignableState create(AssignableState state)
    {
        final MockAssignableWorkflowSchemeState.Builder builder = new MockAssignableWorkflowSchemeState.Builder(state);
        builder.setId(++lastId);

        return addState(builder.build());
    }

    public AssignableState addState(AssignableState state)
    {
        state = new MockAssignableWorkflowSchemeState.Builder(state).build();

        workflowState.put(state.getId(), state);
        return state;
    }

    public AssignableState addStateForScheme(AssignableWorkflowScheme scheme)
    {
        return addState(new MockAssignableWorkflowSchemeState.Builder()
                .setId(scheme.getId())
                .setName(scheme.getName())
                .setDescription(scheme.getDescription())
                .setMappings(scheme.getMappings())
                .build());
    }

    @Override
    public AssignableState update(AssignableState state)
    {
        return addState(state);
    }

    @Override
    public boolean delete(long id)
    {
        return workflowState.remove(id) != null;
    }

    @Override
    public boolean delete(AssignableState state)
    {
        return delete(state.getId());
    }

    @Override
    public AssignableState get(long id)
    {
        return workflowState.get(id);
    }

    @Override
    public Iterable<AssignableState> getAll()
    {
        return workflowState.values();
    }

    @Override
    public boolean renameWorkflow(String oldName, String newName)
    {
        boolean changed = false;
        for (AssignableState state : Lists.newArrayList(getAll()))
        {
            Map<String, String> newMap = Maps.newHashMap();
            boolean newMapChanged = false;
            for (Map.Entry<String, String> entry : state.getMappings().entrySet())
            {
                if (oldName.equals(entry.getValue()))
                {
                    newMapChanged = true;
                    newMap.put(entry.getKey(), newName);
                }
                else
                {
                    newMap.put(entry.getKey(), entry.getValue());
                }
            }
            if (newMapChanged)
            {
                addState(state.builder().setMappings(newMap).build());
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public Iterable<AssignableState> getSchemesUsingWorkflow(JiraWorkflow jiraWorkflow)
    {
        List<AssignableState> states = new ArrayList<AssignableState>();

        for (Map.Entry<Long, AssignableState> entry : workflowState.entrySet())
        {
            AssignableState state = entry.getValue();
            if (state.getMappings().containsValue(jiraWorkflow.getName()))
            {
                states.add(state);
            }
        }

        return states;
    }

    public long getLastId()
    {
        return lastId;
    }

    @Override
    public AssignableState.Builder builder()
    {
        return new MockAssignableWorkflowSchemeState.Builder();
    }
}
