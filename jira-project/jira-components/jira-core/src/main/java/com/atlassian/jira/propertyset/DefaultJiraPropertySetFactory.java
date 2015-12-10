package com.atlassian.jira.propertyset;

import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.jira.ofbiz.FieldMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opensymphony.module.propertyset.PropertyImplementationException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import com.opensymphony.module.propertyset.memory.MemoryPropertySet;
import com.opensymphony.module.propertyset.ofbiz.OFBizPropertySet;

import static com.atlassian.jira.component.ComponentAccessor.getComponent;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the JiraPropertySetFactory.  It relies heavily on the
 * {@link com.opensymphony.module.propertyset.ofbiz.OFBizPropertySet}
 * {@link com.atlassian.jira.propertyset.CachingOfBizPropertySet}.
 *
 * @since v3.12
 */
public class DefaultJiraPropertySetFactory implements JiraPropertySetFactory
{
    private static final Long DEFAULT_ENTITY_ID = 1L;

    /**
     * An empty property set configuration map.  The implementations that we use don't even look at
     * the config, anyway.
     */
    private static final Map<String,Object> NO_CONFIG = ImmutableMap.of();

    /**
     * Hardcoded mapping of the property set implementations that we use, because PropertySetManager's
     * getInstance method hits a synchronized block that we would prefer to avoid.  Note that this
     * means that changes to {@code propertyset.xml} will <strong>not</strong> be honoured by this
     * factory.  In practice, this is unlikely ever to matter.
     */
    private static final Map<String,Class<? extends PropertySet>> IMPLEMENTATIONS =
            ImmutableMap.<String,Class<? extends PropertySet>>builder()
                    .put("ofbiz", OFBizPropertySet.class)
                    .put("ofbiz-cached", CachingOfBizPropertySet.class)
                    .put("memory", MemoryPropertySet.class)
                    .build();

    private final JiraCachingPropertySetManager jiraCachingPropertySetManager;
    private final ClassLoader classLoader;
    private final OfBizPropertyEntryStore ofBizPropertyEntryStore;

    /**
     * Constructor for use by plugins that wrongly instantiate their own instance, e.g. see JRADEV-23709.
     *
     * @deprecated This is a component.  Get it injected or ask the ComponentAccessor for it instead of constructing it.
     */
    @Deprecated
    public DefaultJiraPropertySetFactory()
    {
        this(getComponent(JiraCachingPropertySetManager.class), getComponent(OfBizPropertyEntryStore.class));
    }

    /**
     * Constructor for use by the DI container.
     *
     * @param jiraCachingPropertySetManager required
     * @since 6.1
     */
    public DefaultJiraPropertySetFactory(final JiraCachingPropertySetManager jiraCachingPropertySetManager,
            final OfBizPropertyEntryStore ofBizPropertyEntryStore)
    {
        this.jiraCachingPropertySetManager = notNull("jiraCachingPropertySetManager", jiraCachingPropertySetManager);
        this.ofBizPropertyEntryStore = notNull("ofBizPropertyEntryStore", ofBizPropertyEntryStore);
        this.classLoader = getClass().getClassLoader();
    }

    @Nonnull
    public PropertySet buildNoncachingPropertySet(final String entityName)
    {
        return buildNoncachingPropertySet(entityName, DEFAULT_ENTITY_ID);
    }

    @Nonnull
    public PropertySet buildNoncachingPropertySet(final String entityName, final Long entityId)
    {
        return createPropertySet("ofbiz", FieldMap.build(
                "delegator.name", "default",
                "entityName", entityName,
                "entityId", entityId));
    }

    @Nonnull
    public PropertySet buildCachingDefaultPropertySet(final String entityName)
    {
        return buildCachingPropertySet(entityName, DEFAULT_ENTITY_ID);
    }

    @Nonnull
    public PropertySet buildCachingDefaultPropertySet(final String entityName, final boolean bulkLoad)
    {
        return buildCachingPropertySet(entityName, DEFAULT_ENTITY_ID);
    }

    @Nonnull
    public PropertySet buildCachingPropertySet(final String entityName, final Long entityId, final boolean bulkLoad)
    {
        return buildCachingPropertySet(entityName, entityId);
    }

    @Nonnull
    public PropertySet buildCachingPropertySet(final String entityName, final Long entityId)
    {
        return new CachingOfBizPropertySet(ofBizPropertyEntryStore, entityName, entityId);
    }

    @Nonnull
    public PropertySet buildCachingPropertySet(final PropertySet propertySet, final boolean bulkLoad)
    {
        notNull("propertySet is a required parameter", propertySet);
        final PropertySet cachingPropertySet = createPropertySet("cached", FieldMap.build(
                "PropertySet", propertySet,
                "bulkload", bulkLoad));
        if (cachingPropertySet instanceof JiraCachingPropertySet)
        {
            //noinspection CastToConcreteClass
            jiraCachingPropertySetManager.register((JiraCachingPropertySet) cachingPropertySet);
        }
        return cachingPropertySet;
    }

    @Nonnull
    public PropertySet buildMemoryPropertySet(final String entityName, final Long entityId)
    {
        final PropertySet dbPropertySet = buildNoncachingPropertySet(entityName, entityId);
        final PropertySet memoryPropertySet = createPropertySet("memory", Maps.<String,Object>newHashMap());

        // Clone the property set.
        PropertySetManager.clone(dbPropertySet, memoryPropertySet);

        return memoryPropertySet;
    }

    @Nonnull
    private PropertySet createPropertySet(final String propertySetDelegator, final Map<String,Object> ofbizArgs)
    {
        final Class<? extends PropertySet> implClass = IMPLEMENTATIONS.get(propertySetDelegator);
        if (implClass != null)
        {
            // Avoid using slower PropertySetManager.getInstance if possible
            return createPropertySet(implClass, ofbizArgs);
        }
        return PropertySetManager.getInstance(propertySetDelegator, ofbizArgs, classLoader);
    }

    @Nonnull
    PropertySet createPropertySet(final Class<? extends PropertySet> propertySetClass, final Map<String,Object> args)
    {
        try
        {
            final PropertySet ps = propertySetClass.newInstance();
            ps.init(NO_CONFIG, args);
            return ps;
        }
        catch (InstantiationException e)
        {
            throw new PropertyImplementationException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new PropertyImplementationException(e);
        }
    }
}
