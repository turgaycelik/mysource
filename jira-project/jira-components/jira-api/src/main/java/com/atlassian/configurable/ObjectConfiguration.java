package com.atlassian.configurable;

import java.util.Map;

/**
 * This interface represents a holder for all the configuration information.
 * E.g. Its fields and description
 *
 * @author Owen Fellows
 */
public interface ObjectConfiguration
{
    /**
     * Initialises the object with some parameters
     *
     * @param params Map of initialisation params
     */
    void init(Map params);

    /**
     * Retrieves the name of a property with the specified key
     *
     * @param key Key of the property
     * @return Name of the specified property
     * @throws ObjectConfigurationException
     */
    String getFieldName(String key) throws ObjectConfigurationException;

    /**
     * Retrieves the description of a property with the specified key
     *
     * @param key Key of the property
     * @return Description of the specified property
     * @throws ObjectConfigurationException
     */
    String getFieldDescription(String key) throws ObjectConfigurationException;

    /**
     * Retrieves the type of the property with the specified key
     *
     * @param key Key of the property
     * @return Type of the specified property
     * @throws ObjectConfigurationException
     */
    int getFieldType(String key) throws ObjectConfigurationException;

    /**
     * Retrieves the default value for property with specified key
     *
     * @param key Key of the property
     * @return Default value of the specified property
     * @throws ObjectConfigurationException
     */
    String getFieldDefault(String key) throws ObjectConfigurationException;

    /**
     * Retrieves a map of available values for property with the specified key. e.g select list values
     *
     * @param key Key of the property
     * @return List valid name/value pairs for the specified property
     * @throws ObjectConfigurationException
     */
    Map getFieldValues(String key) throws ObjectConfigurationException;

    /**
     * Retrieves a map of available values for property with the specified key. e.g select list values.
     * However, keys and values are html encoded in the returned map.
     *
     * @param key
     * @return List valid name/value pairs for the specified property - html encoded
     * @throws ObjectConfigurationException
     */
    Map getFieldValuesHtmlEncoded(String key) throws ObjectConfigurationException;

    /**
     * All the property keys for this configuration.
     *
     * @return Property keys
     */
    String[] getFieldKeys();

    /**
     * The enabled property keys for this configuration.
     *
     * @return Property keys
     *
     * @since 28 Aug 2007 for JIRA v3.11
     */
    String[] getEnabledFieldKeys();

    /**
     * Is a particular property enabled at the moment - may be a runtime check.
     *
     * @param key the property's key.
     * @return true if the property is enabled in the current context
     *
     * @since 28 Aug 2007 for JIRA v3.11
     */
    boolean isEnabled(String key);

    /**
     * The Description of this instance of an Object Configuration
     *
     * @param params Params used to derive
     * @return Description
     */
    String getDescription(Map params);

    /**
     * Determines if there are any non-hidden fields.
     *
     * @return true if there are no fields or all fields are hidden
     */
    boolean allFieldsHidden();

    boolean isI18NValues(String key);
}
