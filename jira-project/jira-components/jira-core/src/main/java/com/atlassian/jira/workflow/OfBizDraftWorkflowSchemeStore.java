package com.atlassian.jira.workflow;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.RealClock;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableMap;

/**
 * @since v5.2
 */
public class OfBizDraftWorkflowSchemeStore implements DraftWorkflowSchemeStore
{
    private static class SchemeTable
    {
        private static final String ENTITY = "DraftWorkflowScheme";
        private static final String ENTITY_RELATIONSHIP = "ChildDraftWorkflowSchemeEntity";

        private static class Columns
        {
            private static final String ID = "id";
            private static final String PARENT = "workflowSchemeId";
            private static final String UPDATED_DATE = "lastModifiedDate";
            private static final String UPDATED_USER = "lastModifiedUser";
        }
    }

    private static class EntityTable
    {
        private static final String ENTITY = "DraftWorkflowSchemeEntity";
    }

    private final OfBizDelegator delegator;
    private final WorkflowSchemeStoreSupport<DraftState> support;

    OfBizDraftWorkflowSchemeStore(OfBizDelegator delegator, Clock clock)
    {
        this.delegator = delegator;
        this.support = new WorkflowSchemeStoreSupport<DraftState>(delegator, new SupportDelegate(delegator, clock));
    }

    public OfBizDraftWorkflowSchemeStore(OfBizDelegator delegator)
    {
        this(delegator, RealClock.getInstance());
    }

    @Override
    public DraftState create(DraftState state)
    {
        return support.create(state);
    }

    @Override
    public DraftState update(DraftState state)
    {
        return support.update(state);
    }

    @Override
    public boolean delete(long id)
    {
        return support.delete(id);
    }

    @Override
    public boolean delete(DraftState state)
    {
        return support.delete(state);
    }

    @Override
    public boolean deleteByParentId(long parentId)
    {
        final GenericValue schemeGvFromParent = findSchemeGvFromParent(parentId);
        return schemeGvFromParent != null && delete(schemeGvFromParent.getLong(SchemeTable.Columns.ID));
    }

    @Override
    public boolean hasDraftForParent(long parentId)
    {
        return findSchemeGvFromParent(parentId) != null;
    }

    @Override
    public DraftState getDraftForParent(long parentId)
    {
        return support.createStateFrom(findSchemeGvFromParent(parentId));
    }

    @Override
    public DraftState get(long id)
    {
        final GenericValue schemeGv = getGenericValue(id);
        return support.createStateFrom(schemeGv);
    }

    private GenericValue getGenericValue(long id)
    {
        return delegator.findByPrimaryKey(SchemeTable.ENTITY, id);
    }

    @Override
    public Iterable<DraftState> getAll()
    {
        return support.getAll();
    }

    @Override
    public DraftState.Builder builder(long parentId)
    {
        return new DefaultWorkflowSchemeStateBuilder(parentId);
    }

    @Override
    public boolean renameWorkflow(String oldName, String newName)
    {
        return support.renameWorkflow(oldName, newName);
    }

    @Override
    public Long getParentId(long id)
    {
        GenericValue draftValue = getGenericValue(id);
        return draftValue != null ? draftValue.getLong(SchemeTable.Columns.PARENT) : null;
    }

    @Override
    public Iterable<DraftState> getSchemesUsingWorkflow(JiraWorkflow workflow)
    {
        return support.getSchemesUsingWorkflow(workflow);
    }

    private GenericValue findSchemeGvFromParent(long id)
    {
        List<GenericValue> byAnd = delegator.findByAnd(SchemeTable.ENTITY, of(SchemeTable.Columns.PARENT, id), singletonList(SchemeTable.Columns.ID));
        return EntityUtil.getFirst(byAnd);
    }

    private static class DefaultWorkflowSchemeStateBuilder extends WorkflowSchemeStateBuilderTemplate<DraftState.Builder>
        implements DraftState.Builder
    {
        private final long parentId;
        private String lastModifiedUser;
        private Date lastModifiedDate;

        DefaultWorkflowSchemeStateBuilder(long parentId)
        {
            this.parentId = parentId;
        }

        DefaultWorkflowSchemeStateBuilder(DraftState state)
        {
            super(state);
            this.parentId = state.getParentSchemeId();
            this.lastModifiedDate = state.getLastModifiedDate();
            this.lastModifiedUser = state.getLastModifiedUser();
        }

        @Override
        public long getParentSchemeId()
        {
            return parentId;
        }

        @Override
        DraftState.Builder getThis()
        {
            return this;
        }

        @Override
        public String getLastModifiedUser()
        {
            return lastModifiedUser;
        }

        @Override
        public DraftState.Builder setLastModifiedUser(String user)
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
        public DraftState build()
        {
            return new DraftWorkflowSchemeStateImpl(getId(), parentId, getMappings(), lastModifiedDate, lastModifiedUser);
        }
    }

    private static class DraftWorkflowSchemeStateImpl implements DraftState
    {
        private final Long id;
        private final long parentSchemeId;
        private final Date lastModifiedDate;
        private final String lastModifiedUser;
        private final Map<String, String> issueTypeToWorkflow;

        private DraftWorkflowSchemeStateImpl(Long id, long parentSchemeId,
                Map<String, String> issueTypeToWorkflow, Date lastModifiedDate, String lastModifiedUser)
        {
            this.id = id;
            this.parentSchemeId = parentSchemeId;
            this.lastModifiedDate = lastModifiedDate;
            this.lastModifiedUser = lastModifiedUser;
            this.issueTypeToWorkflow = unmodifiableMap(newHashMap(issueTypeToWorkflow));
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
            return issueTypeToWorkflow;
        }

        @Override
        public String getDefaultWorkflow()
        {
            return issueTypeToWorkflow.get(null);
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
        public DefaultWorkflowSchemeStateBuilder builder()
        {
            return new DefaultWorkflowSchemeStateBuilder(this);
        }
    }

    static class SupportDelegate implements WorkflowSchemeStoreSupport.Delegate<DraftState>
    {
        private final OfBizDelegator delegator;
        private final Clock clock;

        SupportDelegate(OfBizDelegator delegator, Clock clock)
        {
            this.delegator = delegator;
            this.clock = clock;
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
        public GenericValue create(DraftState state)
        {
            FieldMap fieldMap = FieldMap.build(SchemeTable.Columns.PARENT, state.getParentSchemeId());
            fieldMap.add(SchemeTable.Columns.UPDATED_DATE, currentDate());
            fieldMap.add(SchemeTable.Columns.UPDATED_USER, state.getLastModifiedUser());
            return  delegator.createValue(SchemeTable.ENTITY, fieldMap);
        }

        @Override
        public void update(DraftState state, GenericValue schemeGv)
        {
            Long parentId = schemeGv.getLong(SchemeTable.Columns.PARENT);
            if (parentId == null || !parentId.equals(state.getParentSchemeId()))
            {
                throw new DataAccessException("Trying to change the parent of a draft.");
            }

            schemeGv.set(SchemeTable.Columns.UPDATED_DATE, currentDate());
            schemeGv.set(SchemeTable.Columns.UPDATED_USER, state.getLastModifiedUser());
            delegator.store(schemeGv);
        }

        @Override
        public DraftState get(GenericValue schemeGv, Map<String, String> map)
        {
            final Long id = schemeGv.getLong(SchemeTable.Columns.ID);
            final Long parentId = schemeGv.getLong(SchemeTable.Columns.PARENT);
            final String user = schemeGv.getString(SchemeTable.Columns.UPDATED_USER);

            final Date lastModified;
            final Timestamp timestamp = schemeGv.getTimestamp(SchemeTable.Columns.UPDATED_DATE);
            if (timestamp != null)
            {
                lastModified = new Date(timestamp.getTime());
            }
            else
            {
                lastModified = new Date(0);
            }

            return new DraftWorkflowSchemeStateImpl(id, parentId, map, lastModified, user);
        }

        private Timestamp currentDate()
        {
            return new Timestamp(clock.getCurrentDate().getTime());
        }
    }
}
