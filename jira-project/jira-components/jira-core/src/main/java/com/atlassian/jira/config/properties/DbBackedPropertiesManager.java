package com.atlassian.jira.config.properties;

import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.atlassian.util.concurrent.Supplier;

import com.opensymphony.module.propertyset.PropertySet;

/**
 * A container for managing {@link PropertySet}.
 *
 * @since v4.4
 */
public class DbBackedPropertiesManager implements BackingPropertySetManager
{
    private final ResettableLazyReference<PropertySet> dbPropertySetRef;

    public DbBackedPropertiesManager()
    {
        dbPropertySetRef = new ResettableLazyReference<PropertySet>()
        {
            @Override
            protected PropertySet create() throws Exception
            {
                return PropertySetUtils.createDatabaseBackedPropertySet(DefaultOfBizConnectionFactory.getInstance());
            }
        };

    }

    @Override
    public Supplier<? extends PropertySet> getPropertySetSupplier()
    {
        return dbPropertySetRef;
    }

    @Override
    public void refresh()
    {
        dbPropertySetRef.reset();
    }

    @Override
    public void switchBackingStore()
    {
        // do nothing
    }
}
