package com.atlassian.jira.config.properties;

import java.util.Properties;

import com.google.common.base.Preconditions;

import static com.atlassian.jira.config.properties.SystemPropertyKeys.*;

/**
 * Handles getting and setting of system properties.
 * <p/>
 * Non-null values are cached. If you need the latest value of the system property, call {@link #refresh()} before
 * you access that property. If you are changing a system property wish the changed value to be seen by this class
 * immediately you need to call {@link #setProperty(String, Object)} or {@link #setProperties(java.util.Properties)}
 * method of this class. Doing this often is a potential performance problem, as {@link Boolean#getBoolean(String a)}
 * is a blocking operation, so you <b>should NOT do this</b> for every SQL statement or Web request etc.
 * </p>
 * For reading properties, this class catches {@link SecurityException}s and returns null or default values provided.
 * However, this class will not catch such exceptions when setting system properties.

 * @since v6.1
 */
public class JiraPropertiesImpl implements JiraProperties
{

    private final PropertiesAccessor accessor;

    public JiraPropertiesImpl(final PropertiesAccessor propertiesAccessor)
    {
        accessor = propertiesAccessor;
    }

    /**
     * Returns a cached system property as a String.
     * <p/>
     * This call will not raise {@link SecurityException}, it will return null instead.
     * @param key the name of the system property.
     * @return the value of the system property, or null if property is not set or {@link SecurityException} occurs.
     */
    @Override
    public String getProperty(final String key)
    {
        return key == null ? null : accessor.getProperty(key);
    }

    /**
     * Returns a cached system property as a String, or a default value if property is not set.
     * <p/>
     * Note that if the property is not set, the accessor will remain unchanged.
     * <p/>
     * This call will not raise {@link SecurityException}, it will return defaultString instead.
     * @param key the name of the system property.
     * @param defaultString default value
     * @return the value of the system property, or defaultString if property is not set or {@link SecurityException} occurs.
     */
    @Override
    public String getProperty(final String key, final String defaultString)
    {
        final String value = getProperty(key);
        return value == null ? defaultString : value;
    }

    /**
     * Sets a system property and invalidates the accessor accordingly, or unsets the property if value is null.
     * The value set is the result of {@link Object#toString()} called on the value.
     * @param key the name of the system property.
     * @param value the value
     */
    @Override
    public <T> void setProperty(final String key, final T value)
    {
        if (value == null)
        {
            unsetProperty(key);
        }
        else
        {
            accessor.setProperty(key, value.toString());
        }
    }

    /**
     * Unsets a system property and invalidates the accessor accordingly.
     * @param key the name of the system property.
     */
    @Override
    public void unsetProperty(final String key)
    {
        accessor.unsetProperty(key);
    }

    /**
     * Returns a cached system property as a Boolean.
     * <p/>
     * This call will not raise {@link SecurityException}, it will return false instead.
     * <p/>
     * Because of the way system properties are read by the Boolean class, this method will never return null,
     * nor it is possible to provide a variant with the default value. See {@link Boolean#getBoolean(String)}.
     * You can however use {@link #getProperty(String)} and convert it to Boolean manually for that desired effect.
     * @param key the name of the system property.
     * @return the value of the system property, or false if property is not set or {@link SecurityException} occurs.
     */
    @Override
    public Boolean getBoolean(final String key)
    {
        return key == null ? null : accessor.getBoolean(key);
    }

    /**
     * Returns a cached system property as an Integer.
     * <p/>
     * This call will not raise {@link SecurityException}, it will return null instead.
     * @param key the name of the system property.
     * @return the value of the system property, or null if property is not set or {@link SecurityException} occurs.
     */
    @Override
    public Integer getInteger(final String key)
    {
        return key == null ? null : accessor.getInteger(key);
    }

    /**
     * Returns a cached system property as an Integer, or a default value if property is not set.
     * <p/>
     * Note that if the property is not set, the accessor will remain unchanged.
     * <p/>
     * This call will not raise {@link SecurityException}, it will return defaultInteger instead.
     * @param key the name of the system property.
     * @param defaultInteger default value
     * @return the value of the system property, or defaultInteger if property is not set or {@link SecurityException} occurs.
     */
    @Override
    public Integer getInteger(final String key, final Integer defaultInteger)
    {
        final Integer value = getInteger(key);
        return value == null ? defaultInteger : value;
    }

    /**
     * Returns a cached system property as a Long.
     * <p/>
     * This call will not raise {@link SecurityException}, it will return null instead.
     * @param key the name of the system property.
     * @return the value of the system property, or null if property is not set or {@link SecurityException} occurs.
     */
    @Override
    public Long getLong(final String key)
    {
        return key == null ? null : accessor.getLong(key);
    }

    /**
     * Returns a cached system property as a Long, or a default value if property is not set.
     * <p/>
     * Note that if the property is not set, the accessor will remain unchanged.
     * <p/>
     * This call will not raise {@link SecurityException}, it will return defaultLong instead.
     * @param key the name of the system property.
     * @param defaultLong default value
     * @return the value of the system property, or defaultLong if property is not set or {@link SecurityException} occurs.
     */
    @Override
    public Long getLong(final String key, final Long defaultLong)
    {
        final Long value = getLong(key);
        return value == null ? defaultLong : value;
    }

    /**
     * Returns a snapshot of all the set system properties. This is not a view.
     * <p/>
     * This call will not raise {@link SecurityException}, it will return an empty set of properties instead.
     * @return system properties.
     */
    @Override
    public Properties getProperties()
    {
        return accessor.getProperties();
    }

    /**
     * Sets the system properties and invalidates the accessor accordingly.
     * @param props the properties to set.
     */
    @Override
    public void setProperties(final Properties props)
    {
        Preconditions.checkNotNull(props);
        accessor.setProperties(props);
    }

    @Override
    public void refresh()
    {
        accessor.refresh();
    }

    /**
     * @return true if jira is running in dev mode (meaning jira.home lock files will be ignored)
     */
    public boolean isDevMode()
    {
        return getBoolean(JIRA_DEV_MODE) || getBoolean(ATLASSIAN_DEV_MODE);
    }

    public boolean isXsrfDetectionCheckRequired()
    {
        return getBoolean(XSRF_DETECTION_CHECK);
    }

    public boolean isSuperBatchingDisabled()
    {
        return getBoolean(SUPER_BATCH_DISABLED);
    }

    public boolean isDecodeMailParameters()
    {
        return getBoolean(MAIL_DECODE_PARAMETERS);
    }

    public boolean isCustomPathPluginsEnabled()
    {
        return getProperty(CUSTOM_PLUGIN_PATH) != null;
    }

    public String getCustomDirectoryPlugins()
    {
        return getProperty(CUSTOM_PLUGIN_PATH);
    }

    public boolean isWebSudoDisabled()
    {
        return getBoolean(WEBSUDO_IS_DISABLED);
    }

    public boolean isI18nReloadBundles()
    {
        return getBoolean(JIRA_I18N_RELOADBUNDLES);
    }

    public boolean showPerformanceMonitor()
    {
        return getBoolean(SHOW_PERF_MONITOR);
    }

    public boolean isBundledPluginsDisabled()
    {
        return getBoolean(DISABLE_BUNDLED_PLUGINS);
    }

    @Override
    public boolean isDarkFeaturesDisabled()
    {
        return getBoolean(DARK_FEATURES_DISABLED);
    }

    @Override
    public boolean isDangerMode()
    {
        return getBoolean(DANGER_MODE);
    }

    @Override
    public boolean isXsrfDiagnostics()
    {
        return getBoolean(XSRF_DIAGNOSTICS);
    }

    @Override
    public String getProductName()
    {
        return getProperty(SystemPropertyKeys.PRODUCT_NAME, "JIRA");
    }

}
