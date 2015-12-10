package com.atlassian.configurable;

import com.atlassian.annotations.PublicSpi;
import com.opensymphony.module.propertyset.PropertySet;

/**
 * This interface should be implemented by any classes that are configured using
 * an Object Configuration.
 *
 * @author Owen Fellows
 */
@PublicSpi
public interface ObjectConfigurable
{
    /**
     * Retrieves an object configuration object with properties that can be set
     *
     * @return ObjectConfiguration object
     * @throws ObjectConfigurationException
     */
    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException;

    /**
     * Checks if this object has a particular property
     *
     * @param propertyKey to look for
     * @return true If there is a value
     * @throws ObjectConfigurationException
     */
    public boolean hasProperty(String propertyKey) throws ObjectConfigurationException;

    /**
     * Returns a property of this object with the specified key
     *
     * @param propertyKey String key used to retrieve the property value
     * @return Property value
     * @throws ObjectConfigurationException
     */
    public String getProperty(String propertyKey) throws ObjectConfigurationException;

    /**
     * Returns a property of this object with the specified key, the property is of type text
     *
     * @param propertyKey String key used to retrieve the property value
     * @return Property value
     * @throws ObjectConfigurationException
     */
    public String getTextProperty(String propertyKey) throws ObjectConfigurationException;

    /**
     * Returns a property of this object with the specified key as a long
     *
     * @param propertyKey String key used to retrieve the property value
     * @return Property value
     * @throws ObjectConfigurationException
     */
    public Long getLongProperty(String propertyKey) throws ObjectConfigurationException;

    /**
     * Returns the default property value for a key
     *
     * @param propertyKey String key used to retrieve the properties default value
     * @return Default property key
     * @throws ObjectConfigurationException
     */
    public String getDefaultProperty(String propertyKey) throws ObjectConfigurationException;

    /**
     * Retrieve all the specified Properties for this object
     *
     * @return Set of properties for this object
     * @throws ObjectConfigurationException
     */
    public PropertySet getProperties() throws ObjectConfigurationException;

    /**
     * Return the key of this object
     *
     * @return Key of object
     */
    public String getKey();
}
