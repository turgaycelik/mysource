package com.atlassian.jira.dev.reference.plugin.fields;

/**
 * A component that will programmatically create managed custom fields.
 *
 * @since v5.2
 */
public interface CustomFieldCreator
{
    public void registerManagedFields();
}
