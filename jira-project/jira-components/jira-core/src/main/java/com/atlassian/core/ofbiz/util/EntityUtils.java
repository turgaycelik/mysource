/*
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 28/02/2002
 * Time: 15:15:42
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.atlassian.core.ofbiz.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.ObjectUtils;
import com.atlassian.jira.component.ComponentAccessor;

import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 *
 * @deprecated Use {@link com.atlassian.jira.entity.EntityUtils} instead. Since v5.1.
 */
public class EntityUtils
{
    private static final EntityOperator[] entityOperators = { EntityOperator.EQUALS, EntityOperator.NOT_EQUAL,
            EntityOperator.LESS_THAN, EntityOperator.GREATER_THAN, EntityOperator.LESS_THAN_EQUAL_TO,
            EntityOperator.GREATER_THAN_EQUAL_TO, EntityOperator.IN, EntityOperator.BETWEEN, EntityOperator.NOT,
            EntityOperator.AND, EntityOperator.OR };

    private static final String NEXT_STRING_ID_LOCK_NAME = EntityUtils.class.getName() + ".nextStringId";

    /**
     * Small utility method to get an entity operator from it's code
     *
     * @deprecated Not used in JIRA code - please replicate this if you want to use it. Since v5.1.
     */
    public static EntityOperator getOperator(final String code)
    {
        for (final EntityOperator operator : entityOperators)
        {
            if (operator.toString().trim().equals(code.trim()))
            {
                return operator;
            }
        }
        return null;
    }

    /**
     * Create a new entity.
     *
     * If there is no "id" in the parameter list, one is created using the entity sequence
     *
     * @deprecated Use {@link com.atlassian.jira.entity.EntityUtils#createValue(String, java.util.Map)} instead. Since v5.1.
     */
    public static GenericValue createValue(final String entity, final Map<String, ?> paramMap) throws GenericEntityException
    {
        final Map<String, Object> params = (paramMap == null) ? new HashMap<String, Object>() : new HashMap<String, Object>(paramMap);

        if (params.get("id") == null)
        {
            final Long id = CoreFactory.getGenericDelegator().getNextSeqId(entity);
            params.put("id", id);
        }

        final GenericValue v = CoreFactory.getGenericDelegator().makeValue(entity, params);
        v.create();
        return v;
    }

    /**
     * Compare two GenericValues based on their content.
     *
     * This method will check the keys and values of both GenericValues.
     *
     * There is only a single difference between this method and GenericValue.equals(gv2),
     * that is that if one GV has no key of a certain type, and the other has a null value
     * for that key, they are still deemed to be identical (as GenericValue.get(key)
     * always returns null if the key exists or not).
     *
     * @return true if the issues are the same, false if they are different
     *
     * @deprecated Use {@link com.atlassian.jira.entity.EntityUtils#identical(org.ofbiz.core.entity.GenericValue, org.ofbiz.core.entity.GenericValue)} instead. Since v5.1.
     */
    public static boolean identical(final GenericValue v1, final GenericValue v2)
    {
        if ((v1 == null) && (v2 == null))
        {
            return true;
        }
        if ((v1 == null) || (v2 == null))
        {
            return false;
        }

        if (!v1.getEntityName().equals(v2.getEntityName()))
        {
            return false;
        }

        // get the keys of v1, make sure they are equal in v2
        for (final String key : v1.getAllKeys())
        {
            if ((v1.get(key) == null) && (v2.get(key) == null))
            {
                continue;
            }
            if ((v1.get(key) == null) && (v2.get(key) != null))
            {
                return false;
            }
            else
            {
                // handle timestamps specially due to precision
                if ((v1.get(key) instanceof Timestamp) && (v2.get(key) instanceof Timestamp))
                {
                    final Timestamp t1 = (Timestamp) v1.get(key);
                    final Timestamp t2 = (Timestamp) v2.get(key);
                    if (!DateUtils.equalTimestamps(t1, t2))
                    {
                        return false;
                    }
                }
                else if (!v1.get(key).equals(v2.get(key)))
                {
                    return false;
                }
            }
        }

        // if they keys aren't the same, loop through v2
        final Collection<String> uncheckedKeys = new ArrayList<String>(v2.getAllKeys());
        uncheckedKeys.removeAll(v1.getAllKeys());

        // for the unchecked keys in v2, if they have values in v2, then the GVs are not identical
        if (uncheckedKeys.size() > 0)
        {
            for (final String key : uncheckedKeys)
            {
                if (v2.get(key) == null)
                {
                    continue;
                }
                else
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if a collection of entities contains an identical entity
     * to the one provided.
     *
     * @param entities The list of entities to search
     * @param entity The entity to search for and compare to
     * @return The matching entity, or null if no matching entity is found
     *
     * @deprecated Not used in JIRA code - please replicate this if you want to use it. Since v5.1.
     */
    public static boolean contains(final Collection<GenericValue> entities, final GenericValue entity)
    {
        for (final GenericValue gv : entities)
        {
            if (identical(gv, entity))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is analogous to the OFBiz EntityUtil.filterByAnd method
     * except that theirs does not filter on GT, GTE, LT, LTE yet.
     *
     * @param values A collection of GenericValues
     * @param exprs The expressions that must validate to true
     * @return Collection of GenericValues that match the expression list
     *
     * @deprecated Use {@link EntityUtil#filterByAnd(java.util.List, java.util.List)} instead or write your own filter. Since v5.1.
     */
    public static List<GenericValue> filterByAnd(final List<GenericValue> values, final List<? extends EntityExpr> exprs)
    {
        if (values == null)
        {
            return null;
        }
        if ((exprs == null) || (exprs.size() == 0))
        {
            return values;
        }

        final List<GenericValue> result = new ArrayList<GenericValue>();

        for (final GenericValue value : values)
        {
            boolean include = true;

            for (final EntityExpr expr : exprs)
            {
                final Object lhs = value.get((String) expr.getLhs());
                final Object rhs = expr.getRhs();

                if (EntityOperator.EQUALS.equals(expr.getOperator()))
                {
                    //if the field named by lhs is not equal to rhs value, constraint fails
                    include = ObjectUtils.isIdentical(lhs, rhs);

                    if (!include)
                    {
                        break;
                    }
                }
                else if (EntityOperator.NOT_EQUAL.equals(expr.getOperator()))
                {
                    include = ObjectUtils.isDifferent(lhs, rhs);

                    if (!include)
                    {
                        break;
                    }
                }
                else if (EntityOperator.GREATER_THAN.equals(expr.getOperator()) || EntityOperator.GREATER_THAN_EQUAL_TO.equals(expr.getOperator()) || EntityOperator.LESS_THAN.equals(expr.getOperator()) || EntityOperator.LESS_THAN_EQUAL_TO.equals(expr.getOperator()))
                {
                    if ((rhs != null) && (lhs != null) && (rhs instanceof Comparable))
                    {
                        final Comparable rhsComp = (Comparable) rhs;
                        final Comparable lhsComp = (Comparable) lhs;

                        final int comparison = lhsComp.compareTo(rhsComp);

                        if ((comparison <= 0) && EntityOperator.LESS_THAN_EQUAL_TO.equals(expr.getOperator()))
                        {
                            include = true;
                        }
                        else if ((comparison < 0) && EntityOperator.LESS_THAN.equals(expr.getOperator()))
                        {
                            include = true;
                        }
                        else if ((comparison >= 0) && EntityOperator.GREATER_THAN_EQUAL_TO.equals(expr.getOperator()))
                        {
                            include = true;
                        }
                        else if ((comparison > 0) && EntityOperator.GREATER_THAN.equals(expr.getOperator()))
                        {
                            include = true;
                        }
                        else
                        {
                            include = false;
                            break;
                        }
                    }
                    else
                    {
                        throw new IllegalArgumentException(
                                "Operation " + expr.getOperator().getCode() + " is not yet supported by filterByAnd with objects that do not implement Comparable");
                    }
                }
                else
                {
                    throw new IllegalArgumentException("Operation " + expr.getOperator().getCode() + " is not yet supported by filterByAnd");
                }
            }

            if (include)
            {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Calculate a new entity ID (by basically taking one more than the max integer that exists).
     * If there are string IDs here, they will not be affected.
     *
     * @deprecated Use {@link com.atlassian.jira.entity.EntityUtils#getNextStringId(String)} instead. Since v5.1.
     */
    public static String getNextStringId(final String entityName) throws GenericEntityException
    {
        final ClusterLockService clusterLockService = ComponentAccessor.getComponent(ClusterLockService.class);
        final Lock lock = clusterLockService.getLockForName(NEXT_STRING_ID_LOCK_NAME);
        lock.lock();
        try
        {
            long maxID = 1;
            for (final GenericValue entity : CoreFactory.getGenericDelegator().findAll(entityName))
            {
                try
                {
                    final long entityId = Long.parseLong(entity.getString("id"));
                    if (entityId >= maxID)
                    {
                        maxID = entityId + 1;
                    }
                }
                catch (final NumberFormatException nfe)
                {
                    // ignore - we don't care about String constant IDs
                }
            }
            return Long.toString(maxID);
        }
        finally
        {
            lock.unlock();
        }
    }
}
