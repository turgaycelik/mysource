package com.atlassian.jira.imports.project.parser;

import java.util.Map;

/**
 * Contains some useful methods and constants for Parsers that read OS Properties.
 *
 * @since v3.13
 */
public abstract class OSPropertyParser
{
    public static final String OSPROPERTY_STRING = "OSPropertyString";
    public static final String OSPROPERTY_NUMBER = "OSPropertyNumber";
    public static final String OSPROPERTY_ENTRY = "OSPropertyEntry";
    /**
     * When OSProperty stores booleans in OSPropertyNumber, 1 represnets true.
     * This is the String value of this as stored in an XML import file.
     */
    private static final String NUMERIC_TRUE_STRING_VALUE = "1";

    /**
     * Returns the "id" property from a Map of attributes for an OSProperty entry in a JIRA import file.
     *
     * @param attributes The Map of attributes for this entry.
     * @return the "id" property from a Map of attributes for an OSProperty entry in a JIRA import file.
     */
    public String getID(final Map attributes)
    {
        return (String) attributes.get("id");
    }

    /**
     * Returns the "entityName" property from a Map of attributes for an OSProperty entry in a JIRA import file.
     *
     * @param attributes The Map of attributes for this entry.
     * @return the "entityName" property from a Map of attributes for an OSProperty entry in a JIRA import file.
     */
    public String getEntityName(final Map attributes)
    {
        return (String) attributes.get("entityName");
    }

    /**
     * Returns the "propertyKey" property from a Map of attributes for an OSProperty entry in a JIRA import file.
     *
     * @param attributes The Map of attributes for this entry.
     * @return the "propertyKey" property from a Map of attributes for an OSProperty entry in a JIRA import file.
     */
    public String getPropertyKey(final Map attributes)
    {
        return (String) attributes.get("propertyKey");
    }

    /**
     * Returns the "value" property from a Map of attributes for an OSProperty entry in a JIRA import file.
     *
     * @param attributes The Map of attributes for this entry.
     * @return the "value" property from a Map of attributes for an OSProperty entry in a JIRA import file.
     */
    public String getValue(final Map attributes)
    {
        return (String) attributes.get("value");
    }

    /**
     * Returns <code>true<code> if the given element name is the "OSPropertyEntry" element.
     * @param elementName The name of the XML element.
     * @return <code>true<code> if the given element name is the "OSPropertyEntry" element.
     */
    public boolean isOSPropertyEntry(final String elementName)
    {
        return OSPROPERTY_ENTRY.equals(elementName);
    }

    /**
     * Returns <code>true<code> if the given element name is the "OSPropertyString" element.
     * @param elementName The name of the XML element.
     * @return <code>true<code> if the given element name is the "OSPropertyString" element.
     */
    public boolean isOSPropertyString(final String elementName)
    {
        return OSPROPERTY_STRING.equals(elementName);
    }

    public boolean parseNumberAsBoolean(final String numericValue)
    {
        return NUMERIC_TRUE_STRING_VALUE.equals(numericValue);
    }

}
