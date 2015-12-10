package com.atlassian.jira.entity;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Has some utility methods for working with Entity Engine.
 * <p>
 * This originally lived in atlassian-core, but got moved to JIRA in v4.3 and moved to the com.atlassian.jira package in v5.1
 *
 * @since v5.1
 */
public class EntityUtils
{

    /**
     * Create a new entity.
     * <p>
     * If there is no "id" in the parameter list, one is created using the entity sequence.
     *
     * @param entityName the entity name.
     * @param fields field values
     * @return The new GenericValue.
     *
     * @throws com.atlassian.jira.exception.DataAccessException if an error occurs in the Database layer
     *
     * @see com.atlassian.jira.ofbiz.OfBizDelegator#createValue(String, java.util.Map)
     */
    public static GenericValue createValue(final String entityName, final Map<String, Object> fields)
    {
        return ComponentAccessor.getOfBizDelegator().createValue(entityName, fields);
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
     * @param v1 the first GenericValue
     * @param v2 the second GenericValue
     *
     * @return true if the issues are the same, false if they are different
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
                if (v2.get(key) != null)
                {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Calculate a new entity ID (by basically taking one more than the max integer that exists).
     * If there are string IDs here, they will not be affected.
     *
     * @param entityName The entity name
     * @return the next ID for a String ID column.
     */
    public static String getNextStringId(final String entityName)
    {
        long maxID = 1;

        for (final GenericValue entity : ComponentAccessor.getOfBizDelegator().findAll(entityName))
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


    /**
     * Forcibly interns the contents of a String field in a GenericValue.
     * This is a horrible, hackish thing to do and should be avoided if at all
     * possible.  However, if you are required to support an API that still
     * uses <code>GenericValue</code>s and need to cache them, it may make
     * sense to use this method to avoid keeping excessive duplicate copies
     * of their values.
     * <p>
     * Caveats:
     * <ul>
     * <li>This should be considered both a smell and a last resort.</li>
     * <li>GenericValue is mutable and not thread-safe.  Caching them is
     *      less than ideal.  Consider doing something else if you can.</li>
     * </ul>
     * </p>
     */
    public static void internStringFieldValue(GenericValue value, String fieldName)
    {
        if (value != null)
        {
            final String fieldValue = value.getString(fieldName);
            if (fieldValue != null)
            {
                // Note: The "dangerous" part here is that this doesn't synchronize, check the
                // data type, mark the object as modified, or notify observers.  That is
                // exactly what we want, here.
                value.dangerousSetNoCheckButFast(value.getModelEntity().getField(fieldName), fieldValue.intern());
            }
        }
    }

    public static <E> List<GenericValue> convertToGenericValues(EntityFactory<E> entityFactory, List<E> entityList)
    {
        OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();
        List<GenericValue> gvList = new ArrayList<GenericValue>(entityList.size());
        for (E e : entityList)
        {
            gvList.add(ofBizDelegator.makeValue(entityFactory.getEntityName(), entityFactory.fieldMapFrom(e)));
        }
        return gvList;
    }
}
