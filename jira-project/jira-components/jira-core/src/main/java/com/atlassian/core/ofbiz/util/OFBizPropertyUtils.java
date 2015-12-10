package com.atlassian.core.ofbiz.util;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.ofbiz.FieldMap;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public class OFBizPropertyUtils
{
    /**
     * Gets a non-caching property set that is associated with the specified entity.
     * The {@link #getCachingPropertySet(String,Long)} method should <em>almost always</em> be preferred over
     * this one because that implementation will be more efficient for most cases.  It is also <em>safer</em>
     * to use the caching property set, because it will notify any other caching property sets if their information
     * has become stale.  The non-caching property set returned by this method does not do this, so any caching
     * property sets created by other code will not be notified of changes.
     *
     * @param entityName the entity model name of the property set's owning entity
     * @param entityId the entity ID of the property set's owning entity
     * @return the non-caching property set
     * @since 6.2
     */
    public static PropertySet getPropertySet(String entityName, Long entityId)
    {
        return getPropertySet("ofbiz", null, entityName, entityId);
    }

    /**
     * Gets a non-caching property set that is associated with the specified generic value.
     * The {@link #getCachingPropertySet(GenericValue)} method should <em>almost always</em> be preferred over
     * this one because that implementation will be more efficient for most cases.  It is also <em>safer</em>
     * to use the caching property set, because it will notify any other caching property sets if their information
     * has become stale.  The non-caching property set returned by this method does not do this, so any caching
     * property sets created by other code will not be notified of changes.
     *
     * @param gv the generic value which owns the property set
     * @return the non-caching property set
     */
    public static PropertySet getPropertySet(GenericValue gv)
    {
        return getPropertySet("ofbiz", gv.delegatorName, gv.getEntityName(), gv.getLong("id"));
    }


    /**
     * Gets a caching property set that is associated with the specified entity.
     * This method should <em>almost always</em> be preferred over {@link #getPropertySet(String,Long)},
     * because this implementation will be more efficient for most cases.  It is also <em>safer</em>
     * to use the caching property set, because it will notify any other caching property sets if their information
     * has become stale.  The non-caching property set returned by the other method does not do this, so any caching
     * property sets created by other code will not be notified of changes.
     *
     * @param entityName the entity model name of the property set's owning entity
     * @param entityId the entity ID of the property set's owning entity
     * @return the caching property set
     * @since 6.2
     */
    public static PropertySet getCachingPropertySet(String entityName, Long entityId)
    {
        return getPropertySet("ofbiz-cached", null, entityName, entityId);
    }

    /**
     * Gets a caching property set that is associated with the specified generic value.
     * This method should <em>almost always</em> be preferred over {@link #getPropertySet(GenericValue)},
     * because this implementation will be more efficient for most cases.  It is also <em>safer</em>
     * to use the caching property set, because it will notify any other caching property sets if their information
     * has become stale.  The non-caching property set returned by the other method does not do this, so any caching
     * property sets created by other code will not be notified of changes.
     *
     * @param gv the generic value which owns the property set
     * @return the caching property set
     */
    public static PropertySet getCachingPropertySet(GenericValue gv)
    {
        return getPropertySet("ofbiz-cached", gv.delegatorName, gv.getEntityName(), gv.getLong("id"));
    }

    private static PropertySet getPropertySet(@Nonnull String propertySetType, @Nullable String delegatorName,
            @Nonnull String entityName, @Nonnull Long entityId)
    {
        final Map<String, Object> ofbizArgs = FieldMap.build(
                "delegator.name", delegatorName,
                "entityName", notNull("entityName", entityName),
                "entityId", notNull("entityId", entityId));
        return PropertySetManager.getInstance(propertySetType, ofbizArgs, OFBizPropertyUtils.class.getClassLoader());
    }


    /**
     * Implementation note: This method assumes that the property set has been accessed in
     * a cached manner and flushes any associated entries.  If it is known in advance that
     * there are no caching consumers of the property set, then this can be avoided by
     * calling {@code remove} directly on the non-cached implementation:
     * <code><pre>
     *     {@link #getPropertySet(String,Long) getPropertySet(entityName,entityId)}.{@link PropertySet#remove() remove()}
     * </pre></code>
     *
     * @param entityName the entity model name of the property set's owning entity
     * @param entityId the entity ID of the property set's owning entity
     * @since 6.2
     */
    public static void removePropertySet(String entityName, Long entityId)
    {
        getCachingPropertySet(entityName, entityId).remove();
    }

    /**
     * Implementation note: This method assumes that the property set has been accessed in
     * a cached manner and flushes any associated entries.  If it is known in advance that
     * there are no caching consumers of the property set, then this can be avoided by
     * calling {@code remove} directly on the non-cached implementation:
     * <code><pre>
     *     {@link #getPropertySet(GenericValue) getPropertySet(gv)}.{@link PropertySet#remove() remove()}
     * </pre></code>
     *
     * @param gv the generic value which owns the property set
     */
    public static void removePropertySet(GenericValue gv)
    {
        getCachingPropertySet(gv).remove();
    }
}
