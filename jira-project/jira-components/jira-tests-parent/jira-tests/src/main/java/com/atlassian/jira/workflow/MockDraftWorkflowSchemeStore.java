package com.atlassian.jira.workflow;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @since v5.2
 */
public class MockDraftWorkflowSchemeStore implements DraftWorkflowSchemeStore
{
    private long lastId = 0;
    private Map<Long, DraftState> workflowState = Maps.newHashMap();
    private Date lastDate;

    @Override
    public DraftState create(DraftState state)
    {
        final MockDraftWorkflowSchemeState.Builder builder = new MockDraftWorkflowSchemeState.Builder(state);
        builder.setId(++lastId);
        builder.setLastModifiedDate(nextDate());

        return addState(builder.build());
    }

    public DraftState addState(DraftState state)
    {
        state = new MockDraftWorkflowSchemeState.Builder(state)
                .setLastModifiedDate(nextDate()).build();

        workflowState.put(state.getId(), state);
        return state;
    }

    public DraftState addStateForScheme(DraftWorkflowScheme scheme)
    {
        return addState(new MockDraftWorkflowSchemeState.Builder(scheme.getParentScheme().getId())
                .setId(scheme.getId())
                .setMappings(scheme.getMappings())
                .setLastModifiedDate(scheme.getLastModifiedDate()).build());
    }

    @Override
    public DraftState update(DraftState state)
    {
        return addState(state);
    }

    @Override
    public boolean delete(long id)
    {
        return workflowState.remove(id) != null;
    }

    @Override
    public boolean delete(DraftState state)
    {
        return delete(state.getId());
    }

    @Override
    public boolean deleteByParentId(long parentId)
    {
        boolean changed = false;
        for (Iterator<DraftState> iterator = workflowState.values().iterator(); iterator.hasNext(); )
        {
            DraftState next = iterator.next();
            if (next.getParentSchemeId() == parentId)
            {
                iterator.remove();
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean hasDraftForParent(long parentId)
    {
        return getDraftForParent(parentId) != null;
    }

    @Override
    public DraftState getDraftForParent(long parentId)
    {
        for (DraftState state : getAll())
        {
            if (parentId == state.getParentSchemeId())
            {
                return state;
            }
        }
        return null;
    }

    @Override
    public DraftState get(long id)
    {
        return workflowState.get(id);
    }

    @Override
    public Iterable<DraftState> getAll()
    {
        return workflowState.values();
    }

    @Override
    public DraftState.Builder builder(long parentId)
    {
        return new MockDraftWorkflowSchemeState.Builder(parentId);
    }

    @Override
    public boolean renameWorkflow(String oldName, String newName)
    {
        boolean changed = false;
        for (DraftState state : Lists.newArrayList(getAll()))
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
    public Long getParentId(long id)
    {
        DraftState state = get(id);
        return state != null ? state.getParentSchemeId() : null;
    }

    @Override
    public Iterable<DraftState> getSchemesUsingWorkflow(JiraWorkflow jiraWorkflow)
    {
        List<DraftState> states = new ArrayList<DraftState>();

        for (Map.Entry<Long, DraftState> entry : workflowState.entrySet())
        {
            DraftState state = entry.getValue();
            if (state.getMappings().containsValue(jiraWorkflow.getName()))
            {
                states.add(state);
            }
        }

        return states;
    }

    private Date nextDate()
    {
        return lastDate = new Date();
    }

    public Date getLastDate()
    {
        return lastDate;
    }

    public long getLastId()
    {
        return lastId;
    }
}
