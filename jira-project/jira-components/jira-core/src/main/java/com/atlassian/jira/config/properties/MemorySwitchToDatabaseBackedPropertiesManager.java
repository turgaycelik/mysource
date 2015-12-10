package com.atlassian.jira.config.properties;

import java.util.Collections;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.atlassian.util.concurrent.Supplier;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import com.opensymphony.module.propertyset.memory.MemoryPropertySet;

/**
 * A Container for a {@link MemorySwitchToDatabasePropertySet}
 *
 * @since v4.4
 */
public class MemorySwitchToDatabaseBackedPropertiesManager implements BackingPropertySetManager
{
    @ClusterSafe // This only exists before the database is setup, so is never cluster aware.
    private final ResettableLazyReference<MemorySwitchToDatabasePropertySet> propertySetRef;

    /**
     * Create a PropertySet that is really 2 propertysets.  One is backed by memory - the other by a DB
     * The active propertySet is indicated by hte active flag.
     * When initiallly created the memory backed set is active.
     */
    public MemorySwitchToDatabaseBackedPropertiesManager()
    {
        propertySetRef = new ResettableLazyReference<MemorySwitchToDatabasePropertySet>()
        {
            @Override
            protected MemorySwitchToDatabasePropertySet create() throws Exception
            {
                LazyReference<PropertySet> dbBacked = new LazyReference<PropertySet>()
                {
                    @Override
                    protected PropertySet create() throws Exception
                    {
                        return PropertySetUtils.createDatabaseBackedPropertySet(DefaultOfBizConnectionFactory.getInstance());
                    }
                };

                MemoryPropertySet mem = new MemoryPropertySet();
                mem.init(Collections.emptyMap(), Collections.emptyMap());
                return new MemorySwitchToDatabasePropertySet(mem, dbBacked);
            }
        };
    }


    /**
     *  We will switch from memory to the caching version that is backed by db
     *  It is important to copy all the properties from the memory based PropertySet into
     *  the DB backed one.  Currently this switch is one way (htere is no real need for the other way yet)
     */
    private void switchToDbMode()
    {
        final MemorySwitchToDatabasePropertySet switchablePropertySet = propertySetRef.get();
        PropertySetManager.clone(switchablePropertySet.getMemoryPropertySet(), switchablePropertySet.getDatabasePropertySet());
        switchablePropertySet.switchToDatabaseMode();
    }

    /**
     * This clears the cache - in this implementation it also switches to database mode
     */
    @Override
    public void refresh()
    {
        propertySetRef.reset();
        // we don't want to be back at the MemoryPropertySet
        propertySetRef.get().switchToDatabaseMode();
    }

    @Override
    public void switchBackingStore()
    {
        switchToDbMode();
    }

    @Override
    public Supplier<? extends PropertySet> getPropertySetSupplier()
    {
        return propertySetRef;
    }
}
