package com.atlassian.jira.workflow;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang3.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.unmodifiableMap;

/**
 * @since v5.2
 */
public class OfBizAssignableWorkflowSchemeStore implements AssignableWorkflowSchemeStore
{
    private static class SchemeTable
    {
        private static final String ENTITY = "WorkflowScheme";
        private static final String ENTITY_RELATIONSHIP = "ChildWorkflowSchemeEntity";

        private static class Columns
        {
            private static final String ID = "id";
            private static final String NAME = "name";
            private static final String DESCRIPTION = "description";
        }
    }

    private static class EntityTable
    {
        private static final String ENTITY = "WorkflowSchemeEntity";
    }

    private final WorkflowSchemeStoreSupport<AssignableState> support;

    public OfBizAssignableWorkflowSchemeStore(OfBizDelegator delegator)
    {
        this.support = new WorkflowSchemeStoreSupport<AssignableState>(delegator, new SupportDelegate(delegator));
    }

    @Override
    public AssignableState create(AssignableState state)
    {
        return support.create(state);
    }

    @Override
    public AssignableState update(AssignableState state)
    {
        return support.update(state);
    }

    @Override
    public boolean delete(long id)
    {
        return support.delete(id);
    }

    @Override
    public boolean delete(AssignableState state)
    {
        Assertions.notNull("state", state);
        Assertions.notNull("state.id", state.getId());

        return delete(state.getId());
    }

    @Override
    public AssignableState get(long id)
    {
        return support.get(id);
    }

    @Override
    public Iterable<AssignableState> getAll()
    {
        return support.getAll();
    }

    @Override
    public boolean renameWorkflow(String oldName, String newName)
    {
        return support.renameWorkflow(oldName, newName);
    }

    @Override
    public Iterable<AssignableState> getSchemesUsingWorkflow(JiraWorkflow jiraWorkflow)
    {
        return support.getSchemesUsingWorkflow(jiraWorkflow);
    }

    @Override
    public AssignableState.Builder builder()
    {
        return new DefaultWorkflowSchemeStateBuilder();
    }

    private static class DefaultWorkflowSchemeStateBuilder extends WorkflowSchemeStateBuilderTemplate<AssignableState.Builder>
            implements AssignableState.Builder
    {
        private String name;
        private String description;

        DefaultWorkflowSchemeStateBuilder()
        {
        }

        DefaultWorkflowSchemeStateBuilder(AssignableState state)
        {
            super(state);
            this.name = state.getName();
            this.description = state.getDescription();
        }

        @Override
        AssignableState.Builder getThis()
        {
            return this;
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

        @Override
        public AssignableState.Builder setName(String name)
        {
            name = StringUtils.stripToNull(name);
            if (name == null)
            {
                throw new IllegalArgumentException("Scheme name cannot be null or empty.");
            }
            else if (name.length() > 255)
            {
                throw new IllegalArgumentException("Scheme name must be less than 255 characters.");
            }
            this.name = name;
            return this;
        }

        @Override
        public AssignableState.Builder setDescription(String description)
        {
            this.description = StringUtils.stripToNull(description);
            return this;
        }

        @Override
        public AssignableState build()
        {
            return new AssignableWorkflowSchemeStateImpl(getId(), name, description, getMappings());
        }
    }

    private static class AssignableWorkflowSchemeStateImpl implements AssignableState
    {
        private final Long id;
        private final String name;
        private final String description;
        private final Map<String, String> issueTypeToWorkflow;

        private AssignableWorkflowSchemeStateImpl(Long id, String name, String description, Map<String, String> issueTypeToWorkflow)
        {
            this.id = id;
            this.name = name;
            this.description = description;
            this.issueTypeToWorkflow = unmodifiableMap(newHashMap(issueTypeToWorkflow));
        }

        @Override
        public Long getId()
        {
            return id;
        }

        @Override
        public Map<String, String> getMappings()
        {
            return issueTypeToWorkflow;
        }

        @Override
        public String getDefaultWorkflow()
        {
            return issueTypeToWorkflow.get(null);
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

        @Override
        public AssignableState.Builder builder()
        {
            return new DefaultWorkflowSchemeStateBuilder(this);
        }
    }

    private static class SupportDelegate implements WorkflowSchemeStoreSupport.Delegate<AssignableState>
    {
        private final OfBizDelegator delegator;

        private SupportDelegate(OfBizDelegator delegator)
        {
            this.delegator = delegator;
        }

        @Override
        public String schemeTable()
        {
            return SchemeTable.ENTITY;
        }

        @Override
        public String entityTable()
        {
            return EntityTable.ENTITY;
        }

        @Override
        public String schemeToEntityRelationship()
        {
            return SchemeTable.ENTITY_RELATIONSHIP;
        }

        @Override
        public GenericValue create(AssignableState state)
        {
            FieldMap fieldMap = FieldMap.build(SchemeTable.Columns.NAME, state.getName());
            fieldMap.add(SchemeTable.Columns.DESCRIPTION, state.getDescription());

            return delegator.createValue(SchemeTable.ENTITY, fieldMap);
        }

        @Override
        public void update(AssignableState state, GenericValue schemeGv)
        {
            schemeGv.set(SchemeTable.Columns.NAME, state.getName());
            schemeGv.set(SchemeTable.Columns.DESCRIPTION, state.getDescription());
            delegator.store(schemeGv);
        }

        @Override
        public AssignableState get(GenericValue schemeGv, Map<String, String> map)
        {
            final Long id = schemeGv.getLong(SchemeTable.Columns.ID);
            final String name = schemeGv.getString(SchemeTable.Columns.NAME);
            final String description = schemeGv.getString(SchemeTable.Columns.DESCRIPTION);

            return new AssignableWorkflowSchemeStateImpl(id, name, description, map);
        }
    }
}
