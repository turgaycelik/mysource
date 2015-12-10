package com.atlassian.jira.config.properties;

import com.atlassian.annotations.ExperimentalApi;

import java.util.Properties;


/**
 * Provides controlled access to system properties. Available from the container.
 * <p/>
 * Default implementation provides caching of keys to minimize access to synchronized static getters such as
 * {@link System#getProperty(String)} or {@link Boolean#getBoolean(String)}.
 * <p/>
 * You may obtain implementations in the following ways:
 * <ul>
 *     <li>
 *         Injected through this interface,
 *     </li>
 *     <li>
 *         {@link JiraSystemProperties#getInstance()}
 *     </li>
 * </ul>
 *
 * @since v6.1
 */
public interface JiraProperties
{
    /**
     * Retrieves a system property of the given name
     * @param key the name of the property.
     * @return the value of the system property, or null if not defined.
     */
    String getProperty(String key);

    /**
     * Retrieves a system property of the given name, with a default value to be returned if property is not set.
     * @param key the name of the property.
     * @param defaultString the default value.
     * @return the value of the system property, or defaultString, if property not defined.
     */
    String getProperty(String key, String defaultString);

    /**
     * Sets the key-value pair into the system properties. The value will be set as string using the {@link Object#toString()}
     * method
     * @param key the name of the property
     * @param value the value of the property, stored as a String.
     */
    <T> void setProperty(String key, T value);

    /**
     * Removes an entry in system properties under the given key.
     * @param key the name of the property to be removed.
     */
    void unsetProperty(String key);

    /**
     * Retrieves a system property of the given name as a boolean.
     * @param key the name of the property.
     * @return the value of the system property, or false if not defined.
     */
    Boolean getBoolean(String key);

    /**
     * Retrieves a system property of the given name as an integer.
     * @param key the name of the property.
     * @return the value of the system property, or null if not defined.
     */
    Integer getInteger(String key);

    /**
     * Retrieves a system property of the given name as an integer, with a default value to be returned if property is not set.
     * @param key the name of the property.
     * @param defaultInteger the default value.
     * @return the value of the system property, or defaultString, if property not defined.
     */
    Integer getInteger(String key, Integer defaultInteger);

    /**
     * Retrieves a system property of the given name as a long.
     * @param key the name of the property.
     * @return the value of the system property, or null if not defined.
     */
    Long getLong(String key);

    /**
     * Retrieves a system property of the given name as a long, with a default value to be returned if property is not set.
     * @param key the name of the property.
     * @param defaultLong the default value.
     * @return the value of the system property, or defaultString, if property not defined.
     */
    Long getLong(String key, Long defaultLong);

    /**
     * Retrieves all system properties as a properties object. Note, this is not a view of the system properties, rather
     * a snapshot at the time of the call.
     * @return system properties.
     */
    Properties getProperties();

    /**
     * Sets the given properties as system properties. Not that this will unset any properties not mentioned in the
     * input properties object.
     * @param props the target system properties.
     * @see {@link System#setProperties(Properties)}
     */
    void setProperties(Properties props);

    /**
     * Causes any previously cached values to be invalidated. As long as this object is used to set or retrieve system
     * properties it is not necessary to call this method. Call it only if you caused a system property change in another
     * way than {@link #setProperty(String, Object)}, {@link #setProperties(java.util.Properties)} or
     * {@link #unsetProperty(String)}.
     */
    void refresh();

    /*
     below methods are convenience methods which should be used in favor of explicitly testing values of specific keys.
     @see com.atlassian.jira.config.properties.SystemPropertyKeys
     */

    public boolean isDevMode();

    public boolean isXsrfDetectionCheckRequired();

    public boolean isSuperBatchingDisabled();

    public boolean isDecodeMailParameters();

    public boolean isCustomPathPluginsEnabled();

    public String getCustomDirectoryPlugins();

    public boolean isWebSudoDisabled();

    public boolean isI18nReloadBundles();

    public boolean showPerformanceMonitor();

    public boolean isBundledPluginsDisabled();

    public boolean isDarkFeaturesDisabled();

    public boolean isDangerMode();

    public boolean isXsrfDiagnostics();

    /**
     * Name of the product as it was when originally installed. Intended for use during setup.
     */
    @ExperimentalApi
    public String getProductName();

}
