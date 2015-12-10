package com.atlassian.jira.mock.ofbiz;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import junit.framework.AssertionFailedError;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericEntityNotFoundException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.Transformation;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelReader;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Provides a mock delegator with an in-memory database.
 * <p/>
 * The constructor accepts two lists. The genericValues list can be used to pre-populate the in-memory database with
 * GenericValue objects required for the test. It represents the initial state of the database.
 * <p/>
 * The expectedGenericValues list represents the final state of the database. It can be used to verify the final
 * contents of the in-memory database upon test completion.
 * <p/>
 * It is possible to verify the final contents of the in-memory database with the expectedGenericValues list through the methods
 * {@link #verify} (verify objects in expectedGenericValues exist in database) and {@link #verifyAll} (verify
 * objects in expectedGenericValues and only those objects exist in database).
 */
public class MockOfBizDelegator implements OfBizDelegator
{
    public static final int STARTING_ID = 1000;

    public static final Comparator<GenericValue> GV_ENTITY_NAME_AND_ID_COMPARATOR = new Comparator<GenericValue>()
    {
        @Override
        public int compare(final GenericValue lhs, final GenericValue rhs)
        {
            final String lhsEntityName = lhs.getEntityName();
            final String rhsEntityName = rhs.getEntityName();
            final int result = lhsEntityName == null || rhsEntityName == null ? (lhsEntityName == null
                    && rhsEntityName == null ? 0 : -1) : lhsEntityName.compareTo(rhsEntityName);
            if (result != 0)
            {
                return result;
            }

            final Long lhsId = lhs.getLong("id");
            final Long rhsId = rhs.getLong("id");
            return lhsId == null || rhsId == null ? (lhsId == null && rhsId == null ? 0 : -1) : lhsId.compareTo(rhsId);
        }
    };

    private ModelReader modelReader = ModelReaderMock.getMock();

    private final List<GenericValue> genericValues;
    private final List<GenericValue> expectedGenericValues;
    private final Map<RelatedKey, List<GenericValue>> relatedMap = newHashMap();
    private DelegatorInterface delegatorInterface;

    private long ids = STARTING_ID;

    public MockOfBizDelegator()
    {
        this(null, null);
    }

    /**
     * Creates new instance of MockOfBizDelegator. The genericValues list can be used to pre-populate the in-memory
     * database with GenericValue objects required for the test. It represents the initial state of the database.
     * <p/>
     * The expectedGenericValues list represents a list objects that should exist in the final state of the database.
     * It can be used to verify the final contents of the in-memory database upon test completion.
     *
     * @param genericValues         a list of GenericValue objects that represents the initial state of the database
     * @param expectedGenericValues a list of GenericValue objects that represents the objects that should exist in the
     *                              final state of the database - it will be a complete list for {@link #verifyAll()}
     *                              verification and a subset for {@link #verify()} method.  It may help readability
     *                              to specify this information directly in the {@link #verify(List)} or
     *                              {@link #verifyAll(List)} instead of providing it here.
     */
    public MockOfBizDelegator(final List<? extends GenericValue> genericValues, final List<? extends GenericValue> expectedGenericValues)
    {
        this.genericValues = (genericValues != null) ? newArrayList(genericValues) : Lists.<GenericValue> newArrayList();
        this.expectedGenericValues = (expectedGenericValues != null ? Collections.unmodifiableList(expectedGenericValues) : Collections.<GenericValue>emptyList());
        
        for (final GenericValue genericValue : this.genericValues) {
            if (genericValue instanceof MockGenericValue) {
                ((MockGenericValue) genericValue).setOfBizDelegator(this);
            }
        }
        
        for (final GenericValue expectedGenericValue : this.expectedGenericValues) {
            if (expectedGenericValue instanceof MockGenericValue) {
                ((MockGenericValue) expectedGenericValue).setOfBizDelegator(this);
            }
        }
    }

    public MockOfBizDelegator(final DelegatorInterface delegatorInterface)
    {
        this(null, null);
        this.delegatorInterface = delegatorInterface;
    }

    @Override
    public List<GenericValue> findByField(final String entityName, final String fieldName, final Object fieldValue)
    {
        return findByAnd(entityName, new FieldMap(fieldName, fieldValue));
    }

    @Override
    public List<GenericValue> findByField(final String entityName, final String fieldName, final Object fieldValue, final String orderBy)
    {
        return findByAnd(entityName, new FieldMap(fieldName, fieldValue), CollectionBuilder.list(orderBy));
    }

    public synchronized void setGenericValues(final List<? extends GenericValue> genericValues)
    {
        this.genericValues.clear();
        this.genericValues.addAll(genericValues);
    }

    @Override
    public List<GenericValue> findByAnd(final String s, final Map<String, ?> map) throws DataAccessException
    {
        return EntityUtil.filterByAnd(findAll(s), map);
    }

    @Override
    public List<GenericValue> findByAnd(final String s, final Map<String, ?> map, final List<String> orderClause)
            throws DataAccessException
    {
        final List<GenericValue> values = findByAnd(s, map);
        if (!orderClause.isEmpty())
        {
            return EntityUtil.orderBy(values, orderClause);
        }
        else
        {
            return values;
        }
    }

    @Override
    public List<GenericValue> findByAnd(final String s, final List<EntityCondition> expressions) throws DataAccessException
    {
        final List<EntityExpr> exprs = newArrayList();
        for (final EntityCondition condition : expressions)
        {
            exprs.add((EntityExpr) condition);
        }
        return EntityUtil.filterByAnd(findAll(s), exprs);
    }

    @Override
    public List<GenericValue> findByOr(final String entityName, final List<? extends EntityCondition> expressions, final List<String> orderBy) throws DataAccessException
    {
        return findByCondition(entityName, new EntityConditionList(expressions, EntityOperator.OR), null, orderBy);
    }

    @Override
    public List<GenericValue> findByLike(final String entityName, final Map<String, ?> map, final List<String> orderBy) throws DataAccessException
    {
        final List<EntityExpr> list = newArrayListWithCapacity(map.size());
        for (final Map.Entry<String, ?> entry : map.entrySet())
        {
            list.add(new EntityExpr(entry.getKey(), EntityOperator.LIKE, entry.getValue()));
        }
        final EntityCondition condition = new EntityConditionList(list, EntityOperator.AND);
        return findByCondition(entityName, condition, orderBy);
    }

    @Override
    public List<GenericValue> findByLike(final String entityName, final Map<String, ?> map) throws DataAccessException
    {
        return findByLike(entityName, map, null);
    }

    @Override
    public synchronized void removeAll(final List<GenericValue> genericValues) throws DataAccessException
    {
        final Set<GenericValue> toRemove = new TreeSet<GenericValue>(GV_ENTITY_NAME_AND_ID_COMPARATOR);
        toRemove.addAll(genericValues);

        final Iterator<GenericValue> localStorageIterator = this.genericValues.iterator();
        while (localStorageIterator.hasNext())
        {
            if (toRemove.contains(localStorageIterator.next()))
            {
                localStorageIterator.remove();
            }
        }
    }

    @Override
    public synchronized int removeByAnd(final String s, final Map<String, ?> map) throws DataAccessException
    {
        final List<GenericValue> matching = findByAnd(s, map);
        removeAll(matching);
        return matching.size();
    }

    @Override
    public int removeByCondition(final String entityName, final EntityCondition condition) throws DataAccessException
    {
        final List<GenericValue> matching = findByCondition(entityName, condition, null);
        removeAll(matching);
        return matching.size();
    }

    @Override
    public int removeById(final String entityName, final Long id)
    {
        return removeByAnd(entityName, FieldMap.build("id", id));
    }

    @Override
    public int removeValue(final GenericValue value) throws DataAccessException
    {
        removeAll(singletonList(value));
        return 1;
    }

    @Override
    public synchronized void storeAll(final List<GenericValue> genericValues) throws DataAccessException
    {
        final List<GenericValue> fixed = new ArrayList<GenericValue>(genericValues.size());
        for (GenericValue newValue : genericValues)
        {
            final Iterator<GenericValue> iterator = this.genericValues.iterator();
            while (iterator.hasNext())
            {
                final GenericValue oldValue = iterator.next();
                if (oldValue.getLong("id").equals(newValue.getLong("id")) && oldValue.getEntityName().equals(newValue.getEntityName()))
                {
                    // The old value may contain fields that were not selected. We should not drop the values that
                    // those fields had, so update the old entry. It gets removed and re-added because that's how this
                    // has always worked...
                    iterator.remove();
                    oldValue.putAll(newValue);
                    newValue = oldValue;
                    break;
                }
            }
            fixed.add(newValue);
        }
        this.genericValues.addAll(fixed);
    }

    @Override
    public synchronized List<GenericValue> findAll(final String s)
    {
        final List<GenericValue> matchingValues = newArrayListWithCapacity(genericValues.size());
        for (final GenericValue value : genericValues)
        {
            if (s.equals(value.getEntityName()))
            {
                matchingValues.add((GenericValue) value.clone());
            }
        }
        return matchingValues;
    }

    @Override
    public List<GenericValue> findAll(final String s, final List<String> sortOrder) throws DataAccessException
    {
        return filterByOrderBy(findAll(s), sortOrder);
    }

    @Override
    public void store(final GenericValue gv) throws DataAccessException
    {
        // TODO: Should store GVs in a Map, not a list to avoid duplicates
        final Object id = gv.get("id");
        final GenericValue currentValue = findByPrimaryKey(gv.getEntityName(), FieldMap.build("id", id));
        if (currentValue == null)
        {
            throw new DataAccessException(new GenericEntityNotFoundException("Tried to update an entity that does not exist"));
        }
        else
        {
            // current value - is clone, which need to be removed from original store
            genericValues.remove(currentValue);
            // allow for partial updates
            for (final Map.Entry<String, Object> entry : gv.entrySet())
            {
                currentValue.set(entry.getKey(), entry.getValue());
            }
            // returns new updated value to the store
            genericValues.add(currentValue);
        }
    }

    public void createValue(final GenericValue entity)
    {
        genericValues.add(entity);
    }


    @Override
    public synchronized GenericValue createValue(final String entity, final Map<String, Object> params) throws DataAccessException
    {
        final Map<String, Object> fields = new HashMap<String, Object>(params);
        if (fields.get("id") == null)
        {
            fields.put("id", ids++);
        }

        final GenericValue gv = makeValue(entity, fields);
        genericValues.add(gv);
        return gv;
    }

    @Override
    public void createValueWithoutId(final String entityName, final Map<String, Object> fields)
    {
        final GenericValue gv = makeValue(entityName, fields);
        genericValues.add(gv);
    }

    @Override
    public GenericValue makeValue(final String entity)
    {
        return makeValue(entity, null);
    }

    @Override
    public GenericValue makeValue(final String entityName, final Map<String, Object> fields)
    {
        ModelEntity modelEntity;
        try
        {
            modelEntity = getModelReader().getModelEntity(entityName);
        }
        catch (final GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
        final MockGenericValue genericValue;
        if (modelEntity != null)
        {
            genericValue = new MockGenericValue(entityName, modelEntity, fields);
        }
        else
        {
            genericValue = new MockGenericValue(entityName, fields);
        }
        genericValue.setOfBizDelegator(this);
        return genericValue;
    }

    @Override
    public GenericValue findById(final String entityName, final Long id) throws DataAccessException
    {
        return findByPrimaryKey(entityName, id);
    }

    @Override
    public GenericValue findByPrimaryKey(final String entityName, final Long id)
    {
        return findByPrimaryKey(entityName, FieldMap.build("id", id));
    }

    @Override
    public GenericValue findByPrimaryKey(final String s, final Map<String, ?> map)
    {
        final GenericValue result = EntityUtil.getOnly(findByAnd(s, map));
        return result != null ? (GenericValue) result.clone() : null;
    }

    @Override
    public List<GenericValue> getRelated(final String relationName, final GenericValue gv)
    {
        final RelatedKey key = new RelatedKey(gv, relationName);

        if (relatedMap.containsKey(key))
        {
            return relatedMap.get(key);
        }
        else
        {
            return newArrayList();
        }
    }

    @Override
    public List<GenericValue> getRelated(final String relationName, final GenericValue gv, final List<String> orderBy)
    {
        // we can't really order these - make sure that you add them in the right order and you'll be fine
        return getRelated(relationName, gv);
    }

    @Override
    public long getCount(final String entityName)
    {
        return findAll(entityName).size();
    }

    @Override
    public long getCountByAnd(final String entityName, final Map<String, ?> fields)
    {
        return findByAnd(entityName, fields).size();
    }

    @Override
    public OfBizListIterator findListIteratorByCondition(final String entityType, final EntityCondition condition)
    {
        List<GenericValue> results = findAll(entityType);
        results = filterByEntityCondition(results, condition);
        return new MockOfBizListIterator(results);
    }

    @Override
    public OfBizListIterator findListIteratorByCondition(final String entityName, final EntityCondition whereEntityCondition,
            final EntityCondition havingEntityCondition, final Collection<String> fieldsToSelect, final List<String> orderBy,
            final EntityFindOptions entityFindOptions)
    {
        List<GenericValue> results = findAll(entityName);
        results = filterByEntityCondition(results, whereEntityCondition);
        results = filterByOrderBy(results, orderBy);
        results = filterByFieldsToSelect(results, fieldsToSelect);
        results = filterByFindOptions(results, entityFindOptions);
        return new MockOfBizListIterator(results);
    }

    private List<GenericValue> filterByFieldsToSelect(final List<GenericValue> values, final Collection<String> fieldsToSelect)
    {
        if (fieldsToSelect == null || fieldsToSelect.isEmpty())
        {
            return values;
        }
        final List<GenericValue> result = new ArrayList<GenericValue>(values.size());
        for (final GenericValue value : values)
        {
            final MockGenericValue copy = new MockGenericValue(value);
            copy.setOfBizDelegator(this);
            copy.keySet().retainAll(fieldsToSelect);
            result.add(copy);
        }
        return result;
    }

    private List<GenericValue> filterByOrderBy(final List<GenericValue> allValues, final List<String> orderBy)
    {
        return (orderBy == null || orderBy.isEmpty()) ? allValues : EntityUtil.orderBy(allValues, orderBy);
    }

    private List<GenericValue> filterByFindOptions(List<GenericValue> values, final EntityFindOptions options)
    {
        if (options != null)
        {
            if (options.getDistinct())
            {
                values = filterDistinct(values);
            }
            if (options.getMaxResults() > 0)
            {
                values = filterRange(values, options.getOffset(), options.getMaxResults());
            }
        }
        return values;
    }

    private List<GenericValue> filterDistinct(final List<GenericValue> values)
    {
        final List<GenericValue> result = new ArrayList<GenericValue>(values.size());
        final Set<GenericValue> seen = new HashSet<GenericValue>();
        for (final GenericValue value : values)
        {
            if (seen.add(value))
            {
                result.add(value);
            }
        }
        return result;
    }

    private List<GenericValue> filterRange(final List<GenericValue> values, final int offset, final int maxResults)
    {
        if (offset >= values.size())
        {
            return newArrayList();
        }
        return newArrayList(values.subList(offset, Math.min(values.size(), offset + maxResults)));
    }

    private List<GenericValue> filterByEntityCondition(final List<GenericValue> allValues, final EntityCondition entityCondition)
    {
        if (entityCondition == null)
        {
            return allValues;
        }
        return newArrayList(filter(allValues, getPredicateFor(entityCondition)));
    }

    private Predicate<GenericValue> getPredicateFor(final EntityCondition entityCondition)
    {
        if (entityCondition instanceof EntityFieldMap)
        {
            return getPredicateForEntityFieldMap((EntityFieldMap) entityCondition);
        }
        if (entityCondition instanceof EntityExpr)
        {
            return getPredicateForEntityExpr((EntityExpr) entityCondition);
        }
        if (entityCondition instanceof EntityConditionList)
        {
            return getPredicateForEntityConditionList((EntityConditionList) entityCondition);
        }
        throw new UnsupportedOperationException("Mock cannot yet handle EntityCondition of type " + entityCondition.getClass());
    }

    private Predicate<GenericValue> getPredicateForEntityConditionList(final EntityConditionList conditionList)
    {
        final List<Predicate<GenericValue>> conditions = newArrayListWithCapacity(conditionList.getConditionListSize());
        final Iterator<? extends EntityCondition> iter = conditionList.getConditionIterator();
        while (iter.hasNext())
        {
            conditions.add(getPredicateFor(iter.next()));
        }

        if (conditionList.getOperator().equals(EntityOperator.AND))
        {
            return Predicates.and(conditions);
        }

        if (conditionList.getOperator().equals(EntityOperator.OR))
        {
            return Predicates.or(conditions);
        }

        throw new UnsupportedOperationException("Mock cannot yet handle EntityConditionList with operator " + conditionList.getOperator());
    }

    private Predicate<GenericValue> getPredicateForEntityFieldMap(final EntityFieldMap entityFieldMap)
    {
        if (entityFieldMap.getOperator().getId() == EntityOperator.ID_AND)
        {
            return new Predicate<GenericValue>()
            {
                @Override
                public boolean apply(final GenericValue input)
                {
                    final Iterator<String> iter = entityFieldMap.getFieldKeyIterator();
                    while (iter.hasNext())
                    {
                        final String fieldName = iter.next();
                        if (!entityFieldMap.getField(fieldName).equals(input.get(fieldName)))
                            return false;
                    }
                    return true;
                }
            };
        }
        if (entityFieldMap.getOperator().getId() == EntityOperator.ID_OR)
        {
            return new Predicate<GenericValue>()
            {
                @Override
                public boolean apply(final GenericValue input)
                {
                    final Iterator<String> iter = entityFieldMap.getFieldKeyIterator();
                    while (iter.hasNext())
                    {
                        final String fieldName = iter.next();
                        if (entityFieldMap.getField(fieldName).equals(input.get(fieldName)))
                            return true;
                    }
                    return false;
                }
            };
        }
        throw new IllegalStateException("I don't like the operator " + entityFieldMap.getOperator());
    }

    private Predicate<GenericValue> getPredicateForEntityExpr(final EntityExpr expr)
    {
        switch (expr.getOperator().getId())
        {
            case EntityOperator.ID_EQUALS:
            {
                return new Predicate<GenericValue>()
                {
                    private final Object rhs = rhs(expr);
                    @Override
                    public boolean apply(final GenericValue input)
                    {
                        return Objects.equal(lhs(input, expr), rhs);
                    }
                };
            }
            case EntityOperator.ID_NOT_EQUAL:
            {
                return new Predicate<GenericValue>()
                {
                    private final Object rhs = rhs(expr);
                    @Override
                    public boolean apply(final GenericValue input)
                    {
                        return !Objects.equal(lhs(input, expr), rhs);
                    }
                };
            }
            case EntityOperator.ID_LESS_THAN_EQUAL_TO:
            {
                return new Predicate<GenericValue>()
                {
                    private final Number rhs = (Number) rhs(expr);
                    @Override
                    public boolean apply(final GenericValue input)
                    {
                        return ((Number)lhs(input, expr)).longValue() <= rhs.longValue();
                    }
                };
            }
            case EntityOperator.ID_GREATER_THAN:
            {
                return new Predicate<GenericValue>()
                {
                    @Override
                    public boolean apply(final GenericValue input)
                    {
                        final Object lhs = lhs(input, expr);
                        if (lhs == null)
                        {
                            return false;
                        }
                        final Object rhs = rhs(expr);
                        if (rhs instanceof Date)
                        {
                            return ((Date) lhs).getTime() > ((Date) rhs).getTime();
                        }
                        if (rhs instanceof Number)
                        {
                            return ((Number) lhs).longValue() > ((Number) rhs).longValue();
                        }
                        throw new UnsupportedOperationException("Cannot use > on a " + rhs.getClass().getName());
                    }
                };
            }
            case EntityOperator.ID_LIKE:
            {
                return new Predicate<GenericValue>()
                {
                    private final Pattern rhs = compileLike((String) rhs(expr));
                    @Override
                    public boolean apply(final GenericValue input)
                    {
                        return rhs.matcher((String)lhs(input, expr)).matches();
                    }
                };
            }
            case EntityOperator.ID_IN:
            {
                return new Predicate<GenericValue>()
                {
                    private final Collection<?> rhs = (Collection<?>)rhs(expr);
                    @Override
                    public boolean apply(final GenericValue input)
                    {
                        return rhs.contains(lhs(input, expr));
                    }
                };
            }
            case EntityOperator.ID_AND:
            {
                return new Predicate<GenericValue>()
                {
                    Predicate<GenericValue> predicate1 = getPredicateFor((EntityCondition) expr.getLhs());
                    Predicate<GenericValue> predicate2 = getPredicateFor((EntityCondition) expr.getRhs());

                    @Override
                    public boolean apply(final GenericValue input)
                    {
                        return predicate1.apply(input) && predicate2.apply(input);
                    }
                };
            }
            default:
            {
                throw new UnsupportedOperationException("Can't deal with " + expr.getOperator() + " yet.");
            }
        }
    }

    static Pattern compileLike(final String regex)
    {
        final StringBuilder sb = new StringBuilder(regex.length() + 64).append('^');
        for (int i = 0; i < regex.length(); ++i)
        {
            final char c = regex.charAt(i);
            switch (c)
            {
            case '_':
                sb.append('.');
                break;

            case '%':
                sb.append(".*");
                break;

            // Various regex metachars:
            case '.':
            case '\\':
            case '[':
            case ']':
            case '{':
            case '}':
            case '*':
            case '+':
            case '?':
            case '|':
            case '(':
            case ')':
                sb.append('\\').append(c);
                break;

            default:
                sb.append(c);

            }
        }
        return Pattern.compile(sb.append('$').toString());
    }

    static Object lhs(final GenericValue input, final EntityExpr expr)
    {
        return maybeUpper(input.get((String) expr.getLhs()), expr.isLUpper());
    }

    static Object rhs(final EntityExpr expr)
    {
        return maybeUpper(expr.getRhs(), expr.isLUpper());
    }


    static Object maybeUpper(final Object in, final boolean flag)
    {
        if (flag && in != null)
        {
            if (in instanceof String)
            {
                return ((String) in).toUpperCase();
            }
            throw new IllegalArgumentException("Why did we get an UPPER(xxx) request around class " + in.getClass().getName() + "?!");
        }
        return in;
    }

    private List<GenericValue> filterByAnd(List<GenericValue> values, final EntityConditionList conditions)
    {
        final Iterator<? extends EntityCondition> iter = conditions.getConditionIterator();
        while (iter.hasNext())
        {
            values = filterByEntityCondition(values, iter.next());
        }
        return values;
    }

    @Override
    public int bulkUpdateByPrimaryKey(final String entityName, final Map<String, ?> updateValues, final List<Long> keys)
    {
        int sum = 0;
        for (final Object key : keys)
        {
            sum += bulkUpdateByAnd(entityName, updateValues, ImmutableMap.of("id", key));
        }
        return sum;
    }

    @Override
    public int bulkUpdateByAnd(final String entityName, final Map<String, ?> updateValues, final Map<String, ?> criteria)
    {
        final List<GenericValue> results = findByAnd(entityName, criteria);

        for (final GenericValue gv : results)
        {
            genericValues.remove(gv);
            for (final Map.Entry<String, ?> updateEntry : updateValues.entrySet())
            {
                gv.set(updateEntry.getKey(), updateEntry.getValue());
            }
            genericValues.add(gv);
        }
        return results.size();
    }

    /**
     * Look through all the fields that are expected, and checks that they have the correct values.
     *
     * @throws AssertionFailedError if does not match
     */
    public void verifyAll() throws AssertionFailedError
    {
        verifyAll(expectedGenericValues);
    }

    public void verifyAll(final GenericValue... expectedGenericValues)
    {
        verifyAll(asList(expectedGenericValues));
    }

    /**
     * Look through all the fields that are expected, and checks that they have the correct values.
     *
     * @throws AssertionFailedError if does not match
     */
    public void verifyAll(final List<GenericValue> expectedGenericValues) throws AssertionFailedError
    {
        final GenericValue[] expected = expectedGenericValues.toArray(new GenericValue[expectedGenericValues.size()]);
        assertThat(genericValues, containsInAnyOrder(expected));
    }

    /**
     * Look through all the fields that are expected, and checks that they have the correct values.  Ignores
     * values that are not in the expected GenericValue.
     *
     * @throws AssertionFailedError if does not match
     */
    public void verify() throws AssertionFailedError
    {
        verify(expectedGenericValues);
    }

    public void verify(final GenericValue... expectedGenericValues)
    {
        verify(asList(expectedGenericValues));
    }


    /**
     * Look through all the fields that are expected, and checks that they have the correct values.  Ignores
     * values that are not in the expected GenericValue.
     *
     * @param expectedGenericValues overrides anything set during construction
     * @throws AssertionFailedError if does not match
     */
    public void verify(final List<GenericValue> expectedGenericValues)
    {
        if (expectedGenericValues.size() > genericValues.size())
        {
            throw new AssertionFailedError(
                "Expected: " + expectedGenericValues.size() + " genericValues, but there are " + genericValues.size() + ". Expected: " + expectedGenericValues + ", received " + genericValues);
        }

        for (final GenericValue expectedValue : expectedGenericValues)
        {
            List<GenericValue> matchingValues;

            // if we have specified an id (primary key), search by that, else search on all fields
            if (expectedValue.getLong("id") != null)
            {
                matchingValues = findByAnd(expectedValue.getEntityName(), ImmutableMap.of("id", expectedValue.getLong("id")));
            }
            else
            {
                matchingValues = findByAnd(expectedValue.getEntityName(), expectedValue.getFields(expectedValue.getAllKeys()));
            }

            if ((matchingValues == null) || matchingValues.isEmpty())
            {
                throw new AssertionFailedError(
                    "Expected GenericValue " + expectedValue + " not found.  Found entities " + findAll(expectedValue.getEntityName()));
            }
            else if (matchingValues.size() > 1)
            {
                throw new AssertionFailedError(
                    "Multiple matches for GenericValue " + expectedValue.getEntityName() + " with id " + expectedValue.getLong("id"));
            }

            final GenericValue receivedValue = EntityUtil.getOnly(matchingValues);
            assertFieldsMatch(expectedValue, receivedValue);

        }
    }

    private void assertFieldsMatch(final GenericValue expectedValue, final GenericValue receivedValue)
    {
        for (final String fieldName : expectedValue.getAllKeys())
        {
            final Object receivedField = receivedValue.get(fieldName);
            final Object expectedField = expectedValue.get(fieldName);

            if ((receivedField == null) && (expectedField == null))
            {
                continue;
            }

            if ((receivedField == null) || !receivedField.equals(expectedField))
            {
                throw new AssertionFailedError(
                        "Expected '" + expectedField + "' for field '" + fieldName + "', but received '" + receivedField + "'." + "Expected GV: " + expectedValue + ". Received " + receivedValue);
            }

        }

    }

    public void addRelatedMap(final String relationName, final GenericValue gv, final List<GenericValue> listToReturn)
    {
        final List<GenericValue> newRelations = newArrayList();
        final RelatedKey key = new RelatedKey(gv, relationName);
        final List<GenericValue> existingRelations = relatedMap.get(key);
        if (existingRelations != null)
        {
            newRelations.addAll(existingRelations);
        }
        newRelations.addAll(listToReturn);
        relatedMap.put(key, newRelations);
    }

    public void addRelatedMap(final String relationName, final GenericValue gv, final GenericValue... gvsToReturns)
    {
        addRelatedMap(relationName, gv, asList(gvsToReturns));
    }

    @Override
    public int removeByOr(final String entityName, final String entityId, final List<Long> ids)
    {
        final List<EntityExpr> expressions = new ArrayList<EntityExpr>();
        for (final Long id : ids)
        {
            expressions.add(new EntityExpr(entityId, EntityOperator.EQUALS, id));
        }
        final List<GenericValue> removees = findByOr(entityName, expressions, null);
        removeAll(removees);
        return removees.size();
    }

    @Override
    public List<GenericValue> findByCondition(final String entityName, final EntityCondition entityCondition, final Collection<String> fieldsToSelect, final List<String> orderBy) throws DataAccessException
    {
        List<GenericValue> results = findAll(entityName);
        results = filterByEntityCondition(results, entityCondition);
        results = filterByOrderBy(results, orderBy);
        results = filterByFieldsToSelect(results, fieldsToSelect);
        return results;
    }

    @Override
    public List<GenericValue> findByCondition(final String entityName, final EntityCondition entityCondition, final Collection<String> fieldsToSelect)
    {
        return findByCondition(entityName, entityCondition, fieldsToSelect, null);
    }

    @Override
    public ModelReader getModelReader()
    {
        return modelReader;
    }
    
    public void setModelReader(final ModelReader modelReader)
    {
        this.modelReader = modelReader;
    }

    @Override
    public void refreshSequencer()
    {
        throw new UnsupportedOperationException("Not mocked yet.");
    }

    @Override
    public DelegatorInterface getDelegatorInterface()
    {
        if (delegatorInterface == null)
        {
            delegatorInterface = mockDelegatorInterface();
        }
        return delegatorInterface;
    }

    @Override
    public boolean removeRelated(final String relationName, final GenericValue schemeGv)
    {
        final List<GenericValue> remove = relatedMap.remove(new RelatedKey(schemeGv, relationName));
        if (remove != null)
        {
            removeAll(remove);
        }
        return remove != null;
    }

    @Override
    public int bulkCopyColumnValuesByAnd(final String entityName, final Map updateColumns, final Map criteria)
    {
        throw new UnsupportedOperationException("Not mocked yet.");
    }

    // Synchronized to give us the concurrency that in production we get from a db transaction
    @Override
    public synchronized List<GenericValue> transform(final String entityName, final EntityCondition entityCondition,
            final List<String> orderBy, final String lockField, final Transformation transformation)
    {
        final List<GenericValue> entities = findByCondition(entityName, entityCondition, null, orderBy);
        for (final GenericValue entity : entities)
        {
            transformation.transform(entity);
            store(entity);
        }
        return entities;
    }

    // Synchronized to give us the concurrency that in production we get from a db transaction
    @Override
    public synchronized GenericValue transformOne(final String entityName, final EntityCondition entityCondition,
            final String lockField, final Transformation transformation)
        {
        final List<GenericValue> transformedEntities =
                transform(entityName, entityCondition, null, lockField, transformation);
        if (transformedEntities.size() != 1) {
            throw new IllegalStateException("Expected one entity but found these: " + transformedEntities);
        }
        return transformedEntities.get(0);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(genericValues.size() * 256).append("MockOfBizDelegator[");
        if (!genericValues.isEmpty())
        {
            for (final GenericValue genericValue : genericValues)
            {
                sb.append("\n\t").append(genericValue).append(',');
            }
            sb.setCharAt(sb.length() - 1, '\n');
        }
        return sb.append(']').toString();
    }

    static class RelatedKey
    {
        private final GenericValue gv;
        private final String relationName;

        private RelatedKey(final GenericValue gv, final String relationName)
        {
            this.gv = gv;
            this.relationName = relationName;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final RelatedKey that = (RelatedKey) o;

            if (gv != null ? !gv.equals(that.gv) : that.gv != null) { return false; }
            if (relationName != null ? !relationName.equals(that.relationName) : that.relationName != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = gv != null ? gv.hashCode() : 0;
            result = 31 * result + (relationName != null ? relationName.hashCode() : 0);
            return result;
        }
    }

    private DelegatorInterface mockDelegatorInterface()
    {
        try
        {
            return mockDelegatorInterfaceImpl();
        }
        catch (GenericEntityException gee)
        {
            throw new DataAccessException(gee);
        }
    }
    private DelegatorInterface mockDelegatorInterfaceImpl() throws GenericEntityException
    {
        final DelegatorInterface delegator = Mockito.mock(DelegatorInterface.class);
        Mockito.when(delegator.getNextSeqId(anyString())).thenAnswer(new Answer<Long>()
        {
            private final AtomicLong counter = new AtomicLong(10000L);

            @Override
            public Long answer(final InvocationOnMock invocation) throws Throwable
            {
                return counter.getAndIncrement();
            }
        });

        Mockito.when(delegator.countByCondition(anyString(), anyString(), any(EntityCondition.class), any(EntityFindOptions.class)))
                .thenAnswer(new Answer<Integer>()
                {
                    @Override
                    public Integer answer(final InvocationOnMock invocation) throws Throwable
                    {
                        Object[] args = invocation.getArguments();
                        assertNull("Counting by field not implemented", args[1]);
                        return findByCondition((String)args[0], (EntityCondition)args[2], null, null).size();
                    }
                });
        return delegator;
    }

}
