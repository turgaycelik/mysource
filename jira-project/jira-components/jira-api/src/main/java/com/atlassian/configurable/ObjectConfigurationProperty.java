package com.atlassian.configurable;

import java.util.Map;

/**
 * This interface is a property of an Object Configuration and contains a name, description, default value and the type
 * (ObjectConfigurationTypes) of this property. It can also contain a list of values if the type has values specified
 * for it e.g. select list.
 */
public interface ObjectConfigurationProperty extends Map
{
    /**
     * Initialises the object so it can retrieve values dependant on the parameters in the Map
     *
     * @param params
     */
    void init(Map params);

    /**
     * Retrieves the name of this Property e.g. City
     *
     * @return Name of this Property
     */
    String getName();

    /**
     * Retrieves the description of this Property e.g. Please enter you current location
     *
     * @return Description of the Property
     */
    String getDescription();

    /**
     * Retreives the default value of this Property e.g. Sydney
     *
     * @return Default value of Property
     */
    String getDefault();

    /**
     * Retrieves the type of the Property e.g. Text
     *
     * @return Type of property from {@link ObjectConfigurationTypes}
     */
    int getType();

    boolean isI18nValues();

    void setI18nValues(boolean i18nValues);

    String getCascadeFrom();

    void setCascadeFrom(String cascadeFrom);

    /**
     * Whether the property is enabled in the current context. It is up to the implementation to divine the context.
     * @return true only if the property is enabled.
     *
     * @since 28 Aug 2007 for JIRA v3.11
     */
    boolean isEnabled();
}