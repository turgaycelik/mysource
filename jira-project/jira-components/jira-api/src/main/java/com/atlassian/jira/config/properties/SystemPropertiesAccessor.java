package com.atlassian.jira.config.properties;

import java.util.Properties;

import javax.annotation.Nonnull;

/**
* Implementation of accessing the system properties.
*
* @since v6.1
*/
public class SystemPropertiesAccessor implements PropertiesAccessor
{
    @Override
    public Boolean getBoolean(@Nonnull final String key)
    {
        return Boolean.getBoolean(key);
    }

    @Override
    public String getProperty(@Nonnull final String key)
    {
        return System.getProperty(key);
    }

    @Override
    public void setProperty(@Nonnull final String key, @Nonnull final String value)
    {
        System.setProperty(key, value);
    }

    @Override
    public void unsetProperty(@Nonnull final String key)
    {
        System.clearProperty(key);
    }

    @Override
    public Properties getProperties()
    {
        final Properties properties = new Properties();
        final Properties systemProperties = System.getProperties();
        // Cluster-safe because system properties are node-specific
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (systemProperties) {
            properties.putAll(System.getProperties());
        }
        return properties;
    }

    @Override
    public void setProperties(@Nonnull final Properties props)
    {
        System.setProperties(props);
    }

    @Override
    public Integer getInteger(@Nonnull final String key)
    {
        return Integer.getInteger(key);
    }

    @Override
    public Long getLong(@Nonnull final String key)
    {
        return Long.getLong(key);
    }

    @Override
    public void refresh()
    {
    }

    @Override
    public void refresh(@Nonnull final String key)
    {
    }
}
