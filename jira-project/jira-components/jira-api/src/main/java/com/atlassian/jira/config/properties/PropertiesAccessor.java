package com.atlassian.jira.config.properties;

import java.util.Properties;

import javax.annotation.Nonnull;

/**
* Interface for accessing system properties.
*
* @since v6.1
*/
public interface PropertiesAccessor
{
    public String getProperty(@Nonnull final String key);

    public void setProperty(@Nonnull final String key, @Nonnull final String value);

    public void unsetProperty(@Nonnull final String key);

    public Properties getProperties();

    public void setProperties(@Nonnull final Properties props);

    public Boolean getBoolean(@Nonnull final String key);

    public Integer getInteger(@Nonnull final String key);

    public Long getLong(@Nonnull final String key);

    public void refresh();

    public void refresh(@Nonnull final String key);
}
