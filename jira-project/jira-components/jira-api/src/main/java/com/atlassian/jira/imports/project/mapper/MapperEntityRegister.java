package com.atlassian.jira.imports.project.mapper;

/**
 * Interface that defines the simple way to register values in the Project Import file.
 * Some objects, eg Issue Type, may need a more complicated method.
 *
 * @since v3.13
 */
public interface MapperEntityRegister
{
    /**
     * This method registers a value from the backup system as a value that can then be mapped in this Mapper.
     * If the value is used by the backup data then the appropriate flagValueAsRequired() method should be called.
     *
     * @param oldId the string representation of the id of the backup value
     * @param oldKey the string representation of a descriptive value (key) representing the id (e.g. HSP-1 for an issue, or Bug for an issue type).
     */
    void registerOldValue(String oldId, String oldKey);
}
