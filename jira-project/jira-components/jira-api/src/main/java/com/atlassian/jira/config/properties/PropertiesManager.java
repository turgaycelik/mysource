package com.atlassian.jira.config.properties;

import com.atlassian.util.concurrent.Supplier;
import com.opensymphony.module.propertyset.PropertySet;

import javax.annotation.concurrent.GuardedBy;

/**
 * This class has undergone significant change in 4.4 to support the new in line database functionality. The 1st page of
 * setup may have no database - if that is the case you need to store properties in memory When the database is loaded
 * the properties from page 1 (primarily language) are cloned to a PropertySet that is backed by the DB. From that
 * point on any new PropertiesManager instances injected by Pico will be backed by database property sets. using a
 * ComponentAdaptor. The onDataBaseConfigured method is the entry point to indicate the database setup is complete.
 */
public class PropertiesManager
{

    private final BackingPropertySetManager propertySetManager;

    public PropertiesManager(BackingPropertySetManager propertySetManager)
    {
        this.propertySetManager = propertySetManager;
    }

    public PropertySet getPropertySet()
    {
        return propertySetManager.getPropertySetSupplier().get();
    }

    /**
     * Get a reference for when you need to look at the properties for a longer period of time.
     *
     * @return a resettable reference to the global properties
     */
    public Supplier<? extends PropertySet> getPropertySetReference()
    {
        return propertySetManager.getPropertySetSupplier();
    }

    /**
     * Refresh the properties from the database. If called after creation and write operations called on the PropertySet
     * yet before db setup, writes will be lost!
     */
    public void refresh()
    {
        propertySetManager.refresh();
    }

    /**
     * Called to indicate the database has been set up this causes the switch to a db-backed PropertySet
     */
    @GuardedBy ("ReentrantLock setupLock in DatabaseConfigurationManagerImpl")
    public void onDatabaseConfigured()
    {
        propertySetManager.switchBackingStore();
    }
}
