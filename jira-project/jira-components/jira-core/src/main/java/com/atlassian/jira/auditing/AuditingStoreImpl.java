package com.atlassian.jira.auditing;


import com.atlassian.jira.entity.AbstractEntityFactory;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.SelectQuery;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.TransactionSupport;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Visitor;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityExprList;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.collect.Collections2.transform;

/**
 * @since v6.2
 */
public class AuditingStoreImpl implements AuditingStore
{
    public static final long OTHER = 0L;
    public static final long SYSADMIN = 1L;

    public static final int MAX_RESULTS_LIMIT = 10000;

    // we assume here that JIRA Internal Directory will always have id 1
    public static final String JIRA_INTERNAL_DIRECTORY_ID = "1";

    private static String ENTITY_NAME = "AuditLog";
    private static String ITEMS_ENTITY_NAME = "AuditItem";
    private static String CHANGED_VALUES_ENTITY_NAME = "AuditChangedValue";

    private final OfBizDelegator ofBizDelegator;
    private final TransactionSupport transactionSupport;

    public AuditingStoreImpl(final OfBizDelegator ofBizDelegator, final TransactionSupport transactionSupport)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.transactionSupport = transactionSupport;
    }

    @Override
    public void storeRecord(@Nonnull final AuditingCategory category, String categoryName, @Nonnull final String summary,
            @Nonnull final String eventSource, @Nullable final ApplicationUser author, @Nullable final String remoteAddress,
            @Nullable final AssociatedItem objectItem,
            @Nullable final Iterable<ChangedValue> changedValues, @Nullable final Iterable<AssociatedItem> associatedItems,
            final boolean isAuthorSysAdmin)
    {
        final Transaction transaction = transactionSupport.begin();
        try
        {
            final Map<String, Object> values = Maps.newHashMap();
            values.put(AuditRecordImpl.REMOTE_ADDR, remoteAddress);
            values.put(AuditRecordImpl.CREATED, new Timestamp(System.currentTimeMillis()));
            values.put(AuditRecordImpl.AUTHOR_KEY, author != null ? author.getKey() : null);
            values.put(AuditRecordImpl.SUMMARY, summary);
            values.put(AuditRecordImpl.CATEGORY, category.getId());
            values.put(AuditRecordImpl.AUTHOR_TYPE, isAuthorSysAdmin ? SYSADMIN : OTHER);
            values.put(AuditRecordImpl.EVENT_SOURCE, StringUtils.defaultString(eventSource));

            if (objectItem != null)
            {
                values.put(AuditRecordImpl.OBJECT_ID, objectItem.getObjectId());
                values.put(AuditRecordImpl.OBJECT_NAME, objectItem.getObjectName());
                values.put(AuditRecordImpl.OBJECT_PARENT_ID, objectItem.getParentId());
                values.put(AuditRecordImpl.OBJECT_PARENT_NAME, objectItem.getParentName());
                values.put(AuditRecordImpl.OBJECT_TYPE, objectItem.getObjectType() != null ? objectItem.getObjectType().toString() : null);
            }
            values.put(AuditRecordImpl.SEARCH_FIELD, computeSearchField(summary, objectItem, associatedItems, changedValues, remoteAddress, author, categoryName, eventSource));
            final GenericValue gv = ofBizDelegator.createValue(ENTITY_NAME, values);

            if (associatedItems != null)
            {
                storeAssociatedItems(gv, associatedItems);
            }
            if (changedValues != null)
            {
                storeChangedValues(gv, changedValues);
            }

            transaction.commit();
        }
        finally
        {
            transaction.finallyRollbackIfNotCommitted();
        }
    }

    /* visibility relaxed to implement upgrade task */
    public static String computeSearchField(String summary, AssociatedItem objectItem, Iterable<AssociatedItem> associatedItems, Iterable<ChangedValue> changedValues, String remoteAddress, ApplicationUser author, String categoryName, String eventSource)
    {
        SearchTokenizer tokenizer = new SearchTokenizer();
        if (author != null)
        {
            tokenizer.put(author.getName());
            tokenizer.put(author.getDisplayName());
        }
        tokenizer.put(remoteAddress);
        tokenizer.put(summary);
        tokenizer.put(categoryName);
        if (objectItem != null)
        {
            tokenizer.put(objectItem.getObjectName());
            tokenizer.put(objectItem.getParentName());
        }

        if (StringUtils.isNotEmpty(eventSource))
        {
            tokenizer.put(eventSource);
        }

        if (associatedItems != null)
        {
            for (AssociatedItem item : associatedItems)
            {
                tokenizer.put(item.getObjectName());
                tokenizer.put(item.getParentName());
            }
        }
        if (changedValues != null)
        {
            for (ChangedValue changedValue : changedValues)
            {
                tokenizer.put(changedValue.getFrom());
                tokenizer.put(changedValue.getTo());
            }
        }

        return tokenizer.getTokenizedString();
    }

    protected void storeChangedValues(@Nonnull final GenericValue gv, @Nonnull Iterable<ChangedValue> changedValues)
    {
        for (ChangedValue changedValue : changedValues)
        {
            final Map<String, Object> values = Maps.newHashMap();
            values.put("logId", gv.get("id"));
            values.put("name", changedValue.getName());
            values.put("deltaFrom", changedValue.getFrom());
            values.put("deltaTo", changedValue.getTo());

            ofBizDelegator.createValue("AuditChangedValue", values);
        }
    }

    protected void storeAssociatedItems(@Nonnull final GenericValue gv, @Nonnull final Iterable<AssociatedItem> associatedItems)
    {
        for (AssociatedItem item : associatedItems)
        {
            final Map<String, Object> values = Maps.newHashMap();
            values.put("logId", gv.getLong("id"));
            values.put(AssociatedItemImpl.OBJECT_ID, item.getObjectId());
            values.put(AssociatedItemImpl.OBJECT_NAME, item.getObjectName());
            values.put(AssociatedItemImpl.OBJECT_PARENT_ID, item.getParentId());
            values.put(AssociatedItemImpl.OBJECT_PARENT_NAME, item.getParentName());
            values.put(AssociatedItemImpl.OBJECT_TYPE, item.getObjectType().toString());
            ofBizDelegator.createValue(ITEMS_ENTITY_NAME, values);
        }
    }

    @Override
    @Nonnull
    public Records getRecords(@Nullable final Long maxId, @Nullable final Long sinceId, @Nullable final Integer maxResults,
            @Nullable final Integer offset, @Nullable final AuditingFilter filter, final boolean includeSysAdminActions)
    {
        final EntityCondition condition = getCondition(maxId, sinceId, filter, includeSysAdminActions);
        final int limit = (maxResults != null && maxResults < MAX_RESULTS_LIMIT) ? maxResults : MAX_RESULTS_LIMIT;
        final List<GenericValue> records = (condition != null ? Select.from(ENTITY_NAME).whereCondition(condition) : Select.from(ENTITY_NAME))
                .orderBy("id desc")
                .limit(Objects.firstNonNull(offset, 0), limit)
                .runWith(ofBizDelegator).asList();
        final ImmutableList<AuditRecord> auditRecords =  getAuditRecords(records);
        final long count = getCount(maxId, records, sinceId, filter, includeSysAdminActions);

        return new Records()
        {
            @Override
            public Iterable<AuditRecord> getRecords()
            {
                return auditRecords;
            }

            @Override
            public List<AuditRecord> getResults()
            {
                return auditRecords;
            }

            @Override
            public long getCount()
            {
                return count;
            }

            @Override
            public int getMaxResults()
            {
                return MAX_RESULTS_LIMIT;
            }
        };
    }

    private Long getCount(final Long maxId, final List<GenericValue> records, final Long sinceId, final AuditingFilter filter, final boolean includeSysAdminActions)
    {
        final Long maxIdFromPreviousQuery = maxId != null ? maxId : (records.size() > 0 ? (Long) records.get(0).get("id") : null);
        final EntityCondition condition = getCondition(maxIdFromPreviousQuery, sinceId, filter, includeSysAdminActions);
        return (condition != null ? Select.from(ENTITY_NAME).whereCondition(condition).runWith(ofBizDelegator) : Select.from(ENTITY_NAME).runWith(ofBizDelegator)).count();
    }

    private ImmutableList<AuditRecord> getAuditRecords(final List<GenericValue> records)
    {
        if(records.isEmpty())
        {
            return ImmutableList.of();
        }
        else
        {
            final List<Long> logIds = ImmutableList.copyOf(Iterables.transform(records, new Function<GenericValue, Long>()
            {
                @Override
                public Long apply(final GenericValue input)
                {
                    return input.getLong("id");
                }
            }));
            final Multimap<Long, GenericValue> logsToItems = getItems(logIds);
            final Multimap<Long, GenericValue> logsToValues = getChangedValues(logIds);
            return ImmutableList.copyOf(Iterables.transform(records, new Function<GenericValue, AuditRecord>()
            {
                @Override
                public AuditRecord apply(final GenericValue input)
                {
                    final Long id = input.getLong("id");
                    final Iterable<AssociatedItem> items = Iterables.transform(
                            logsToItems.get(id), AssociatedItemImpl.from());

                    final AbstractEntityFactory<ChangedValue> changeValueEntityFactory = getChangeValueEntityFactory();
                    final Iterable<ChangedValue> changedValues = Iterables.transform(
                            logsToValues.get(id), new Function<GenericValue, ChangedValue>()
                            {
                                @Override
                                public ChangedValue apply(@Nullable final GenericValue input)
                                {
                                    return changeValueEntityFactory.build(input);
                                }
                            });
                    return new AuditRecordImpl(input, items, changedValues);
                }
            }));
        }
    }

    private Multimap<Long, GenericValue> getChangedValues(final List<Long> logIds)
    {
        final Multimap<Long, GenericValue> logsToValues = LinkedHashMultimap.create(logIds.size(), 10);
        Select.from(CHANGED_VALUES_ENTITY_NAME).whereCondition(new EntityExpr("logId", EntityOperator.IN, logIds)).orderBy("logId, id asc").runWith(ofBizDelegator).visitWith(new Visitor<GenericValue>()
        {
            @Override
            public void visit(final GenericValue element)
            {
                logsToValues.put(element.getLong("logId"), element);
            }
        });
        return logsToValues;
    }

    private Multimap<Long, GenericValue> getItems(final List<Long> logIds)
    {
        final Multimap<Long, GenericValue> logsToItems = LinkedHashMultimap.create(logIds.size(), 10);
        Select.from(ITEMS_ENTITY_NAME).whereCondition(new EntityExpr("logId", EntityOperator.IN, logIds)).orderBy("logId, id asc").runWith(ofBizDelegator).visitWith(new Visitor<GenericValue>()
        {
            @Override
            public void visit(final GenericValue element)
            {
                logsToItems.put(element.getLong("logId"), element);
            }
        });
        return logsToItems;
    }

    @Override
    public long countRecords(@Nullable final Long maxId, @Nullable final Long sinceId, final boolean includeSysAdminActions)
    {
        final EntityCondition condition = getCondition(maxId, sinceId, null, includeSysAdminActions);
        return (condition != null ? Select.from(ENTITY_NAME).whereCondition(condition).runWith(ofBizDelegator) : Select.from(ENTITY_NAME).runWith(ofBizDelegator)).count();
    }

    @Nullable
    protected EntityCondition getCondition(@Nullable final Long maxId, @Nullable final Long sinceId, @Nullable final AuditingFilter filter, final boolean includeSysAdminActions)
    {
        final List<EntityCondition> conditions = Lists.newArrayListWithCapacity(6);

        if (!includeSysAdminActions)
        {
            conditions.add(new EntityExpr(AuditRecordImpl.AUTHOR_TYPE, EntityOperator.NOT_EQUAL, SYSADMIN));
        }

        if (maxId != null)
        {
            conditions.add(new EntityExpr(AuditRecordImpl.ID, EntityOperator.LESS_THAN_EQUAL_TO, maxId));
        }

        if (sinceId != null)
        {
            conditions.add(new EntityExpr(AuditRecordImpl.ID, EntityOperator.GREATER_THAN_EQUAL_TO, sinceId));
        }

        if (filter != null)
        {
            if (StringUtils.isNotBlank(filter.getFilter()))
            {
                conditions.add(getConditionForFilter(filter.getFilter()));
            }

            if (filter.getFromTimestamp() != null)
            {
                conditions.add(new EntityExpr(AuditRecordImpl.CREATED, EntityOperator.GREATER_THAN_EQUAL_TO, new Timestamp(filter.getFromTimestamp())));
            }

            if (filter.getToTimestamp() != null)
            {
                conditions.add(new EntityExpr(AuditRecordImpl.CREATED, EntityOperator.LESS_THAN_EQUAL_TO, new Timestamp(filter.getToTimestamp())));
            }

            if (filter.isHideExternalDirectories())
            {
                conditions.add(getConditionHidingExternalDirectoryEntriesForCategory(AuditingCategory.USER_MANAGEMENT));
                conditions.add(getConditionHidingExternalDirectoryEntriesForCategory(AuditingCategory.GROUP_MANAGEMENT));
            }
        }

        if (conditions.isEmpty()) {
            return null;
        } else {
            return new EntityConditionList(conditions, EntityOperator.AND);
        }
    }

    private EntityExpr getConditionHidingExternalDirectoryEntriesForCategory(AuditingCategory category) {
        return new EntityExpr(
                new EntityExpr(AuditRecordImpl.CATEGORY, EntityOperator.NOT_EQUAL, category.getId()),
                EntityOperator.OR,
                new EntityExpr(
                        new EntityExpr(AuditRecordImpl.CATEGORY, EntityOperator.EQUALS, category.getId()),
                        EntityOperator.AND,
                        new EntityExpr(AuditRecordImpl.OBJECT_PARENT_ID, EntityOperator.EQUALS, JIRA_INTERNAL_DIRECTORY_ID)));
    }

    private EntityCondition getConditionForFilter(String filter) {
        final Collection<EntityExpr> conditions = transform(SearchTokenizer.tokenize(filter), new Function<String, EntityExpr>() {
            @Override
            public EntityExpr apply(String token)
            {
                return new EntityExpr(AuditRecordImpl.SEARCH_FIELD, EntityOperator.LIKE, "%" + token + "%");
            }
        });
        return new EntityExprList(ImmutableList.copyOf(conditions), EntityOperator.AND);
    }

    @Override
    public long removeRecordsOlderThan(final long timestamp)
    {
        final AtomicInteger deleted = new AtomicInteger();
        runWhileReturnsResults(
                Select.from(ENTITY_NAME)
                        .whereCondition(createdBefore(timestamp))
                        .orderBy("id asc") //we remove oldest entries first
                        .limit(MAX_RESULTS_LIMIT)
                        .runWith(ofBizDelegator)
        ).visitWith(new Visitor<GenericValue>()
        {
            @Override
            public void visit(final GenericValue element)
            {
                final Transaction transaction = transactionSupport.begin();
                try
                {
                    element.removeRelated("ChildAuditItem");
                    element.removeRelated("ChildAuditChangedValue");
                    element.remove();
                    transaction.commit();
                    deleted.incrementAndGet();
                }
                catch (GenericEntityException e)
                {
                    throw new RuntimeException(e);
                }
                finally
                {
                    transaction.finallyRollbackIfNotCommitted();
                }
            }
        });
        return deleted.intValue();
    }

    @Override
    public long countRecordsOlderThan(final long timestamp)
    {
        return Select.from(ENTITY_NAME).whereCondition(createdBefore(timestamp)).runWith(ofBizDelegator).count();
    }

    private interface VisitorContext<E>
    {
        void visitWith(final Visitor<E> visitor);
    }

    private VisitorContext<GenericValue> runWhileReturnsResults(final SelectQuery.ExecutionContext<GenericValue> select)
    {
        return new VisitorContext<GenericValue>()
        {
            @Override
            public void visitWith(final Visitor<GenericValue> visitor)
            {
                while (true)
                {
                    if (runSingleQuery(visitor) == 0)
                    {
                        break;
                    }
                }
            }

            private int runSingleQuery(final Visitor<GenericValue> visitor)
            {
                final AtomicInteger executionCount = new AtomicInteger();
                select.visitWith(new Visitor<GenericValue>()
                {
                    @Override
                    public void visit(GenericValue element)
                    {
                        visitor.visit(element);
                        executionCount.incrementAndGet();
                    }
                });
                return executionCount.intValue();
            }
        };
    }

    private EntityExpr createdBefore(final long timestamp)
    {
        return new EntityExpr(AuditRecordImpl.CREATED, EntityOperator.LESS_THAN_EQUAL_TO, new Timestamp(timestamp));
    }

    private AbstractEntityFactory<ChangedValue> getChangeValueEntityFactory()
    {
        return new AbstractEntityFactory<ChangedValue>()
        {
            @Override
            public Map<String, Object> fieldMapFrom(final ChangedValue value)
            {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public String getEntityName()
            {
                return CHANGED_VALUES_ENTITY_NAME;
            }

            @Override
            public ChangedValueImpl build(final GenericValue genericValue)
            {
                return new ChangedValueImpl(genericValue.getString("name"),
                        genericValue.getString("deltaFrom"), genericValue.getString("deltaTo"));
            }
        };
    }
}
