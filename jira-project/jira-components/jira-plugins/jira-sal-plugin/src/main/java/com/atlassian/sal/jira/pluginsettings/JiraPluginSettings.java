package com.atlassian.sal.jira.pluginsettings;

import com.atlassian.util.concurrent.Supplier;
import com.opensymphony.module.propertyset.PropertySet;
import com.atlassian.sal.core.pluginsettings.AbstractStringPluginSettings;

public class JiraPluginSettings extends AbstractStringPluginSettings
{
    private static final int STRING_PROPERTY_MAX_LENGTH = 255;

    private Supplier<? extends PropertySet> propertySetRef;

    public JiraPluginSettings(Supplier<? extends PropertySet> set)
    {
        this.propertySetRef = set;
    }

    protected void removeActual(String key)
    {
        propertySetRef.get().remove(key);
    }

    protected void putActual(String key, String val)
    {
        PropertySet propertySet = propertySetRef.get();
        // remove value first
        if (key != null && propertySet.exists(key))
            propertySet.remove(key);

        if (val.length() > STRING_PROPERTY_MAX_LENGTH)
        {
            propertySet.setText(key, val);
        }
        else
        {
            propertySet.setString(key, val);
        }
    }

    protected String getActual(String key)
    {
        PropertySet propertySet = propertySetRef.get();
        if (!propertySet.exists(key))
        {
            return null;
        }
        switch (propertySet.getType(key))
        {
            case PropertySet.STRING:
                return propertySet.getString(key);
            case PropertySet.TEXT:
                return propertySet.getText(key);
            default:
                return null;
        }
    }

}
