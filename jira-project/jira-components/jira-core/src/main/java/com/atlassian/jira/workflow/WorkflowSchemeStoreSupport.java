package com.atlassian.jira.workflow;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.ofbiz.core.entity.GenericValue;

/**
 * @since v5.2
 */
class WorkflowSchemeStoreSupport<T extends WorkflowSchemeStore.State>
{
    private static final String ALL_ISSUE_TYPES = "0";
    private static class SchemeTable
    {

        private static class Columns
        {
            private static final String ID = "id";
        }
    }
    private static class EntityTable
    {

        private static class Columns
        {
            private static final String WORKFLOW_SCHEME = "scheme";
            private static final String WORKFLOW_NAME = "workflow";
            private static final String ISSUE_TYPE = "issuetype";
        }
    }

    private final OfBizDelegator delegator;
    private final Delegate<T> delegate;

    WorkflowSchemeStoreSupport(OfBizDelegator delegator, Delegate<T> delegate)
    {
        this.delegator = delegator;
        this.delegate = delegate;
    }

    T create(T state)
    {
        Assertions.notNull("state", state);

        GenericValue schemeGv = delegate.create(state);
        Long newId = schemeGv.getLong(SchemeTable.Columns.ID);

        final List<GenericValue> mappingGvs = Lists.newArrayList();
        for (Map.Entry<String, String> mapping : state.getMappings().entrySet())
        {
            mappingGvs.add(createMapping(newId, mapping.getKey(), mapping.getValue()));
        }

        return createStateFrom(schemeGv, mappingGvs);
    }

    T update(T state)
    {
        Assertions.notNull("state", state);
        Assertions.notNull("state.id", state.getId());

        // Get the scheme
        final GenericValue schemeGv = delegator.findByPrimaryKey(delegate.schemeTable(), state.getId());
        if (schemeGv == null)
        {
            throw new DataAccessException("Trying to update workflow scheme that does not exist.");
        }

        delegate.update(state, schemeGv);

        // Find the mappings for this scheme
        final List<GenericValue> related = delegator.getRelated(delegate.schemeToEntityRelationship(), schemeGv);
        Map<String, GenericValue> schemeMap = Maps.newHashMapWithExpectedSize(related.size());
        for (GenericValue value : related)
        {
            String issueType = value.getString(EntityTable.Columns.ISSUE_TYPE);
            if (issueType == null)
            {
                delegator.removeValue(value);
            }
            else
            {
                if (ALL_ISSUE_TYPES.equals(issueType))
                {
                    issueType = null;
                }
                GenericValue oldValue = schemeMap.put(issueType, value);
                if (oldValue != null) // If there was already a relationship set
                {
                    delegator.removeValue(oldValue); // Remove the old one, it must be an orphaned row
                }
            }
        }

        List<GenericValue> enties = Lists.newArrayList();
        for (Map.Entry<String, String> expectedEntry : state.getMappings().entrySet())
        {
            final String issueType = expectedEntry.getKey();
            final String workflowName = expectedEntry.getValue();
            final GenericValue currentGv = schemeMap.remove(issueType);

            if (currentGv == null) // Not mapped
            {
                enties.add(createMapping(state.getId(), expectedEntry.getKey(), workflowName));
            }
            else
            {
                String currentWorkflow = currentGv.getString(EntityTable.Columns.WORKFLOW_NAME);
                if (!currentWorkflow.equals(workflowName)) // Mapped to the wrong workflow
                {
                    currentGv.setString(EntityTable.Columns.WORKFLOW_NAME, workflowName); // Update to the new mapping
                    delegator.store(currentGv);
                }
                enties.add(currentGv);
            }
        }

        // Clean up invalid mappings
        for (GenericValue genericValue : schemeMap.values())
        {
            delegator.removeValue(genericValue);
        }

        return createStateFrom(schemeGv, enties);
    }

    public boolean delete(T state)
    {
        Assertions.notNull("state", state);
        Assertions.notNull("state.id", state.getId());
        return delete(state.getId());
    }

    boolean delete(long id)
    {
        final GenericValue schemeGv = delegator.findByPrimaryKey(delegate.schemeTable(), id);
        if (schemeGv != null)
        {
            delegator.removeRelated(delegate.schemeToEntityRelationship(), schemeGv);
            delegator.removeValue(schemeGv);
            return true;
        }
        else
        {
            return false;
        }
    }

    T get(long id)
    {
        final GenericValue schemeGv = getGenericValue(id);
        return createStateFrom(schemeGv);
    }

    GenericValue getGenericValue(long id)
    {
        return delegator.findByPrimaryKey(delegate.schemeTable(), id);
    }

    Iterable<T> getAll()
    {
        final List<GenericValue> allGvs = delegator.findAll(delegate.schemeTable());
        return toDraftWorkflowSchemeStates(allGvs);
    }

    List<T> toDraftWorkflowSchemeStates(List<GenericValue> gvs)
    {
        final List<T> states = Lists.newArrayListWithExpectedSize(gvs.size());
        for (GenericValue schemeGv : gvs)
        {
            states.add(createStateFrom(schemeGv));
        }
        return states;
    }

    boolean renameWorkflow(String oldName, String newName)
    {
        return delegator.bulkUpdateByAnd(delegate.entityTable(),
                ImmutableMap.of(EntityTable.Columns.WORKFLOW_NAME, newName),
                ImmutableMap.of(EntityTable.Columns.WORKFLOW_NAME, oldName)) > 0;
    }

    Iterable<T> getSchemesUsingWorkflow(JiraWorkflow workflow)
    {
        if (workflow.isSystemWorkflow())
        {
            throw new IllegalArgumentException("Can't get schemes for system workflow");
        }

        Collection<T> states = new LinkedList<T>();
        Set<Long> schemeIds = new HashSet<Long>();

        List<GenericValue> schemeEntities = delegator.findByAnd(delegate.entityTable(),
                ImmutableMap.of(EntityTable.Columns.WORKFLOW_NAME, workflow.getName()));

        for (GenericValue schemeEntity : schemeEntities)
        {
            Long schemeId = schemeEntity.getLong(EntityTable.Columns.WORKFLOW_SCHEME);
            if (!schemeIds.contains(schemeId))
            {
                states.add(get(schemeId));
                schemeIds.add(schemeId);
            }
        }

        return states;
    }

    private GenericValue createMapping(Long schemeId, String issueType, String workflow)
    {
        FieldMap entityMap = FieldMap.build(EntityTable.Columns.WORKFLOW_SCHEME, schemeId);
        entityMap.add(EntityTable.Columns.ISSUE_TYPE, issueType == null ? ALL_ISSUE_TYPES : issueType);
        entityMap.add(EntityTable.Columns.WORKFLOW_NAME, workflow);

        return delegator.createValue(delegate.entityTable(), entityMap);
    }

    T createStateFrom(GenericValue schemeGv)
    {
        if (schemeGv == null)
        {
            return null;
        }

        return createStateFrom(schemeGv, delegator.getRelated(delegate.schemeToEntityRelationship(), schemeGv));
    }

    private T createStateFrom(GenericValue schemeGv, Iterable<GenericValue> entities)
    {
        Map<String, String> mappings = Maps.newHashMap();
        //Get the mappings.
        for (GenericValue value : entities)
        {
            String issueType = value.getString(EntityTable.Columns.ISSUE_TYPE);
            String workflowName = value.getString(EntityTable.Columns.WORKFLOW_NAME);

            if (issueType != null)
            {
                if (ALL_ISSUE_TYPES.equals(issueType))
                {
                    issueType = null;
                }

                mappings.put(issueType, workflowName);
            }
        }

        return delegate.get(schemeGv, mappings);
    }

    interface Delegate<T extends WorkflowSchemeStore.State>
    {
        String schemeTable();
        String entityTable();
        String schemeToEntityRelationship();

        GenericValue create(T state);
        void update(T state, GenericValue schemeGv);

        T get(GenericValue schemeGv, Map<String, String> map);
    }
}
