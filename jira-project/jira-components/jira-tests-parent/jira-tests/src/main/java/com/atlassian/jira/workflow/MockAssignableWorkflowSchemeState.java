package com.atlassian.jira.workflow;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.workflow.AssignableWorkflowSchemeStore.AssignableState;
import static com.google.common.collect.Maps.newHashMap;

/**
* @since v5.2
*/
class MockAssignableWorkflowSchemeState implements AssignableState
{
    static class SchemeTable
    {
        static final String ENTITY = "WorkflowScheme";
        static final String ENTITY_RELATIONSHIP = "ChildWorkflowSchemeEntity";
        static class Columns
        {
            static final String ID = "id";
            static final String NAME = "name";
            static final String DESCRIPTION = "description";
        }
    }

    static class EntityTable
    {
        static final String ENTITY = "WorkflowSchemeEntity";
        static class Columns
        {
            static final String WORKFLOW_SCHEME = "scheme";
            static final String WORKFLOW_NAME = "workflow";
            static final String ISSUE_TYPE = "issuetype";
        }
    }

    private Long id;
    private Map<String, String> mappings;
    private String name, description;

    MockAssignableWorkflowSchemeState(AssignableState scheme)
    {
        this.id = scheme.getId();
        this.mappings = Maps.newHashMap(scheme.getMappings());
        this.name = scheme.getName();
        this.description = scheme.getDescription();
    }

    MockAssignableWorkflowSchemeState(AssignableWorkflowScheme scheme)
    {
        this.id = scheme.getId();
        this.mappings = Maps.newHashMap(scheme.getMappings());
        this.name = scheme.getName();
        this.description = scheme.getDescription();
    }

    MockAssignableWorkflowSchemeState(GenericValue scheme, Iterable<GenericValue> entities)
    {
        this();

        setId(scheme.getLong(SchemeTable.Columns.ID));
        setDescription(scheme.getString(SchemeTable.Columns.DESCRIPTION));
        setName(scheme.getString(SchemeTable.Columns.NAME));

        for (GenericValue mapping : entities)
        {
            final String issueType = mapping.getString(EntityTable.Columns.ISSUE_TYPE);
            final String workflow = mapping.getString(EntityTable.Columns.WORKFLOW_NAME);

            setMapping(issueType.equals("0") ? null : issueType, workflow);
        }
    }

    MockAssignableWorkflowSchemeState()
    {
        this.mappings = newHashMap();
    }

    MockAssignableWorkflowSchemeState(Long id, String name, String description,
            Map<String, String> mappings)
    {
        this.id = id;
        this.mappings = newHashMap(mappings);
        this.name = name;
        this.description = description;
    }

    MockAssignableWorkflowSchemeState setId(Long id)
    {
        this.id = id;
        return this;
    }

    MockAssignableWorkflowSchemeState setMappings(Map<String, String> issueTypeToWorkflow)
    {
        this.mappings = newHashMap(issueTypeToWorkflow);
        return this;
    }

    MockAssignableWorkflowSchemeState setDefaultWorkflow(String workflow)
    {
        return this.setMapping(null,workflow);
    }

    MockAssignableWorkflowSchemeState setMapping(String issueType, String workflow)
    {
        this.mappings.put(issueType, workflow);
        return this;
    }

    MockAssignableWorkflowSchemeState setName(String name)
    {
        this.name = name;
        return this;
    }

    MockAssignableWorkflowSchemeState setDescription(String description)
    {
        this.description = description;
        return this;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public Map<String, String> getMappings()
    {
        return mappings;
    }

    @Override
    public String getDefaultWorkflow()
    {
        return mappings.get(null);
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
    public Builder builder()
    {
        return new Builder(this);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        MockAssignableWorkflowSchemeState that = (MockAssignableWorkflowSchemeState) o;

        if (description != null ? !description.equals(that.description) : that.description != null) { return false; }
        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (mappings != null ? !mappings.equals(that.mappings) : that.mappings != null) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (mappings != null ? mappings.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    static Set<MockAssignableWorkflowSchemeState> readSchemes(OfBizDelegator delegator)
    {
        final Set<MockAssignableWorkflowSchemeState> result = Sets.newHashSet();
        final List<GenericValue> all = delegator.findAll(SchemeTable.ENTITY);
        for (GenericValue schemeGv : all)
        {
            final Long schemeId = schemeGv.getLong(SchemeTable.Columns.ID);

            final List<GenericValue> related = delegator.findByAnd(EntityTable.ENTITY,
                    ImmutableMap.of(EntityTable.Columns.WORKFLOW_SCHEME, schemeId));

            result.add(new MockAssignableWorkflowSchemeState(schemeGv, related));
        }
        return result;
    }

    static Set<MockAssignableWorkflowSchemeState> convert(Iterable<AssignableWorkflowSchemeStore.AssignableState> states)
    {
        Set<MockAssignableWorkflowSchemeState> mockStates = Sets.newHashSet();
        for (AssignableState state : states)
        {
            mockStates.add(new MockAssignableWorkflowSchemeState(state));
        }
        return mockStates;
    }

    GenericValue saveSchemeOnlyTo(OfBizDelegator delegator)
    {
        return delegator.createValue(SchemeTable.ENTITY, createSchemeFieldMap());
    }

    MockAssignableWorkflowSchemeState saveTo(OfBizDelegator delegator)
    {
        final GenericValue genericValue = saveSchemeOnlyTo(delegator);

        for (Map.Entry<String, String> mapping : mappings.entrySet())
        {
            String issueType = mapping.getKey();
            String workflow = mapping.getValue();

            final FieldMap fieldMap = FieldMap.build(EntityTable.Columns.WORKFLOW_SCHEME, id)
                    .add(EntityTable.Columns.ISSUE_TYPE, issueType == null ? "0" : issueType)
                    .add(EntityTable.Columns.WORKFLOW_NAME, workflow);

            delegator.createValue(EntityTable.ENTITY, fieldMap);
        }
        return this;
    }

    FieldMap createSchemeFieldMap()
    {
        return FieldMap.build(SchemeTable.Columns.ID, id)
                .add(SchemeTable.Columns.NAME, name)
                .add(SchemeTable.Columns.DESCRIPTION, description);
    }

    /**
     * @since v5.2
     */
    static class Builder implements AssignableState.Builder
    {
        private Long id;
        private Map<String, String> schemeMap;
        private String description;
        private String name;

        public Builder()
        {
            schemeMap = newHashMap();
        }

        public Builder(AssignableState state)
        {
            this.id = state.getId();
            this.schemeMap = newHashMap(state.getMappings());
            this.name = state.getName();
            this.description = state.getDescription();
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

        Builder setId(Long id)
        {
            this.id = id;
            return this;
        }

        @Override
        public Map<String, String> getMappings()
        {
            return schemeMap;
        }

        @Override
        public Builder setMappings(Map<String, String> mappings)
        {
            schemeMap = newHashMap(mappings);
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
            this.name = name;
            return this;
        }

        @Override
        public AssignableState.Builder setDescription(String description)
        {
            this.description = description;
            return this;
        }

        @Override
        public MockAssignableWorkflowSchemeState build()
        {
            return new MockAssignableWorkflowSchemeState(id, name, description, schemeMap);
        }
    }
}
