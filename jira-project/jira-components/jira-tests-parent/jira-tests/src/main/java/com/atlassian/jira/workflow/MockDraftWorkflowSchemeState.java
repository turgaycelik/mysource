package com.atlassian.jira.workflow;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @since v5.2
 */
class MockDraftWorkflowSchemeState implements DraftWorkflowSchemeStore.DraftState
{
    static class SchemeTable
    {
        static final String ENTITY = "DraftWorkflowScheme";
        static final String ENTITY_RELATIONSHIP = "ChildDraftWorkflowSchemeEntity";

        static class Columns
        {
            static final String ID = "id";
            static final String PARENT = "workflowSchemeId";
            static final String UPDATED_DATE = "lastModifiedDate";
            static final String UPDATED_USER = "lastModifiedUser";
        }
    }

    static class EntityTable
    {
        static final String ENTITY = "DraftWorkflowSchemeEntity";

        static class Columns
        {
            static final String WORKFLOW_SCHEME = "scheme";
            static final String WORKFLOW_NAME = "workflow";
            static final String ISSUE_TYPE = "issuetype";
        }
    }

    private Long id;
    private long parentSchemeId;
    private Map<String, String> mappings;
    private Date lastModifiedDate;
    private String lastModifiedUser;

    MockDraftWorkflowSchemeState(DraftWorkflowSchemeStore.DraftState scheme)
    {
        this.id = scheme.getId();
        this.mappings = Maps.newHashMap(scheme.getMappings());
        this.lastModifiedUser = scheme.getLastModifiedUser();
        this.lastModifiedDate = scheme.getLastModifiedDate();
        this.parentSchemeId = scheme.getParentSchemeId();
    }

    MockDraftWorkflowSchemeState(GenericValue scheme, Iterable<GenericValue> entities)
    {
        this();

        setId(scheme.getLong(SchemeTable.Columns.ID));
        setLastModifiedUser(scheme.getString(SchemeTable.Columns.UPDATED_USER));
        final Timestamp date = scheme.getTimestamp(SchemeTable.Columns.UPDATED_DATE);
        if (date != null)
        {
            setLastModifiedDate(new Date(date.getTime()));
        }
        setParentSchemeId(scheme.getLong(SchemeTable.Columns.PARENT));

        for (GenericValue mapping : entities)
        {
            final String issueType = mapping.getString(EntityTable.Columns.ISSUE_TYPE);
            final String workflow = mapping.getString(EntityTable.Columns.WORKFLOW_NAME);

            setMapping(issueType.equals("0") ? null : issueType, workflow);
        }
    }

    MockDraftWorkflowSchemeState()
    {
        this.mappings = newHashMap();
    }

    MockDraftWorkflowSchemeState(Long id, long parentSchemeId,
            Map<String, String> mappings, Date date, String user)
    {
        this.id = id;
        this.parentSchemeId = parentSchemeId;
        this.lastModifiedDate = date;
        this.lastModifiedUser = user;
        this.mappings = newHashMap(mappings);
    }

    MockDraftWorkflowSchemeState setLastModifiedDate(Date date)
    {
        this.lastModifiedDate = date;
        return this;
    }

    MockDraftWorkflowSchemeState setId(Long id)
    {
        this.id = id;
        return this;
    }

    MockDraftWorkflowSchemeState setMappings(Map<String, String> issueTypeToWorkflow)
    {
        this.mappings = newHashMap(issueTypeToWorkflow);
        return this;
    }

    MockDraftWorkflowSchemeState setParentSchemeId(long parentSchemeId)
    {
        this.parentSchemeId = parentSchemeId;
        return this;
    }

    MockDraftWorkflowSchemeState setLastModifiedUser(String user)
    {
        this.lastModifiedUser = user;
        return this;
    }

    MockDraftWorkflowSchemeState setDefaultWorkflow(String workflow)
    {
        return this.setMapping(null, workflow);
    }

    MockDraftWorkflowSchemeState setMapping(String issueType, String workflow)
    {
        this.mappings.put(issueType, workflow);
        return this;
    }

    @Override
    public long getParentSchemeId()
    {
        return parentSchemeId;
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
    public Date getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    @Override
    public String getLastModifiedUser()
    {
        return lastModifiedUser;
    }

    @Override
    public Builder builder()
    {
        return new Builder(this);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        MockDraftWorkflowSchemeState that = (MockDraftWorkflowSchemeState) o;

        if (parentSchemeId != that.parentSchemeId) { return false; }
        if (lastModifiedDate != null ? !lastModifiedDate.equals(that.lastModifiedDate) : that.lastModifiedDate != null)
        {
            return false;
        }

        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (mappings != null ? !mappings.equals(that.mappings) : that.mappings != null)
        { return false; }
        if (lastModifiedUser != null ? !lastModifiedUser.equals(that.lastModifiedUser) : that.lastModifiedUser != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (parentSchemeId ^ (parentSchemeId >>> 32));
        result = 31 * result + (mappings != null ? mappings.hashCode() : 0);
        result = 31 * result + (lastModifiedDate != null ? lastModifiedDate.hashCode() : 0);
        result = 31 * result + (lastModifiedUser != null ? lastModifiedUser.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    static Set<MockDraftWorkflowSchemeState> readSchemes(OfBizDelegator delegator)
    {
        final Set<MockDraftWorkflowSchemeState> result = Sets.newHashSet();
        final List<GenericValue> all = delegator.findAll(SchemeTable.ENTITY);
        for (GenericValue schemeGv : all)
        {
            final Long schemeId = schemeGv.getLong(SchemeTable.Columns.ID);

            final List<GenericValue> related = delegator.findByAnd(EntityTable.ENTITY,
                    ImmutableMap.of(EntityTable.Columns.WORKFLOW_SCHEME, schemeId));

            result.add(new MockDraftWorkflowSchemeState(schemeGv, related));
        }
        return result;
    }

    static Set<MockDraftWorkflowSchemeState> convert(Iterable<DraftWorkflowSchemeStore.DraftState> states)
    {
        Set<MockDraftWorkflowSchemeState> mockStates = Sets.newHashSet();
        for (DraftWorkflowSchemeStore.DraftState state : states)
        {
            mockStates.add(new MockDraftWorkflowSchemeState(state));
        }
        return mockStates;
    }

    GenericValue saveSchemeOnlyTo(OfBizDelegator delegator)
    {
        return delegator.createValue(SchemeTable.ENTITY, createSchemeFieldMap());
    }

    MockDraftWorkflowSchemeState saveTo(OfBizDelegator delegator)
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
        final Timestamp timestamp = genericValue.getTimestamp(SchemeTable.Columns.UPDATED_DATE);
        if (timestamp != null)
        {
            setLastModifiedDate(new Date(timestamp.getTime()));
        }
        else
        {
            setLastModifiedDate(null);
        }
        return this;
    }

    FieldMap createSchemeFieldMap()
    {
        final FieldMap fieldMap = FieldMap.build(SchemeTable.Columns.ID, id)
                .add(SchemeTable.Columns.PARENT, parentSchemeId)
                .add(SchemeTable.Columns.UPDATED_USER, lastModifiedUser);

        if (lastModifiedDate != null)
        {
            fieldMap.add(SchemeTable.Columns.UPDATED_DATE, new Timestamp(lastModifiedDate.getTime()));
        }
        return fieldMap;
    }

    /**
     * @since v5.2
     */
    static class Builder implements DraftWorkflowSchemeStore.DraftState.Builder
    {
        private Long id;
        private String lastModifiedUser;
        private final long parentId;
        private Date lastModifiedDate;
        private Map<String, String> schemeMap;

        public Builder(long parentId)
        {
            this.parentId = parentId;
            schemeMap = newHashMap();
        }

        public Builder(DraftWorkflowSchemeStore.DraftState state)
        {
            this.id = state.getId();
            this.parentId = state.getParentSchemeId();
            this.schemeMap = newHashMap(state.getMappings());
            this.lastModifiedDate = state.getLastModifiedDate();
            this.lastModifiedUser = state.getLastModifiedUser();
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
        public long getParentSchemeId()
        {
            return parentId;
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
        public String getLastModifiedUser()
        {
            return lastModifiedUser;
        }

        @Override
        public DraftWorkflowSchemeStore.DraftState.Builder setLastModifiedUser(String user)
        {
            this.lastModifiedUser = user;
            return this;
        }

        @Override
        public Date getLastModifiedDate()
        {
            return lastModifiedDate;
        }

        @Override
        public MockDraftWorkflowSchemeState build()
        {
            return new MockDraftWorkflowSchemeState(id, parentId, schemeMap, lastModifiedDate, lastModifiedUser);
        }

        Builder setLastModifiedDate(Date date)
        {
            this.lastModifiedDate = date;
            return this;
        }
    }
}
