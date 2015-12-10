/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock.config.properties;

import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.util.concurrent.Supplier;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

public class MockPropertiesManager extends PropertiesManager
{
    PropertySet ps;

    public MockPropertiesManager()
    {
        super(null);
        this.ps = PropertySetManager.getInstance("memory", null);
    }

    public MockPropertiesManager(PropertySet ps)
    {
        super(null);
        this.ps = ps;
    }

    public PropertySet getPropertySet()
    {
        return ps;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof MockPropertiesManager))
            return false;

        final MockPropertiesManager mockPropertiesManager = (MockPropertiesManager) o;

        if (ps != null ? !ps.equals(mockPropertiesManager.ps) : mockPropertiesManager.ps != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        return (ps != null ? ps.hashCode() : 0);
    }

    @Override
    public Supplier<? extends PropertySet> getPropertySetReference()
    {
        return null;
    }

    @Override
    public void refresh()
    {
    }


    @Override
    public void onDatabaseConfigured()
    {
    }
}
