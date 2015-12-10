package com.atlassian.jira.config.properties;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * This can be used to lookup JIRA application properties. This uses a two stage strategy for finding property values.
 * First the database is checked to see if a value exists. If it doesnt exist, it falls back to the
 * file for a value.
 *
 * Once a key is placed in the database (via an upgrade task or UI interaction) then it will always be loaded from
 * the database.
 *
 * NOTE : Be very careful with boolean property values. Because of the way OSPropertySets work, its impossible to
 * distinguish between properties that have a false value and properties that have NO value. Therefore it is usually
 * better to have a "String" property set to the value "true" or "false" and then use Boolean.valueOf()
 * in it. This way it's possible to distinguish the absence of a property value from it being set to false.
 */
@PublicApi
public interface ApplicationProperties
{
    String getText(String name);

    /**
     * Get the property from the application properties, but if not found, try to get from the default properties file.
     */
    String getDefaultBackedText(String name);

    void setText(String name, String value);

    String getString(String name);

    /**
     * Get all the keys from the default properties
     */
    Collection<String> getDefaultKeys();

    /**
     * Get the property from the application properties, but if not found, try to get from the default properties file.
     */
    String getDefaultBackedString(String name);

    /**
     * Get the default property (if the property is not set)
     *
     * @param name the name of the property.
     */
    String getDefaultString(String name);

    void setString(String name, String value);

    /**
     * Get the option from the application properties, but if not found, try to get from the default properties file.
     */
    boolean getOption(String key);

    Collection<String> getKeys();

    void setOption(String key, boolean value);

    /**
     * Convenience method to get the content type for an application
     */
    String getEncoding();

    /**
     * Convenience method to get the email encoding
     */
    public String getMailEncoding();

    String getContentType();

    /**
     * Refresh application properties object by refreshing the PropertiesManager
     */
    void refresh();

    /**
     * Returns the default {@link java.util.Locale} set up on the JIRA instance.
     * @return the default locale.
     */
    Locale getDefaultLocale();

    Collection<String> getStringsWithPrefix(String prefix);

    /**
     * This will return all application and typed values.
     * For example if the property is a boolean then a {@code Boolean} object will be returned.
     * If an application property has a null value, then the key will still be in the {@link java.util.Map#keySet()}
     * <p/>
     * <strong>WARNING</strong>: This method is somewhat expensive.  Do not use it unless you really
     * are doing something with all of the application properties, such as for the system information page.
     * If you are just retrieving a single property, then use one of the {@code get} methods instead.  For
     * most properties, {@link #getDefaultBackedString(String)} or {@link #getOption(String)} will be the best
     * choice.
     *
     * @return a map of key to actual value object
     */
    @Internal
    Map<String, Object> asMap();
}