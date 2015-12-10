package com.atlassian.jira.functest.config.ps;

/**
 * Loads a property set from an XML configuration file.
 *
 * @since v4.1
 */
public interface ConfigPropertySetManager
{
    ConfigPropertySet loadPropertySet(String entityName, long id);
    boolean savePropertySet(ConfigPropertySet propertySet);
    void deletePropertySet(ConfigPropertySet propertySet);
    void deletePropertySet(String entityName, Long id);
}
