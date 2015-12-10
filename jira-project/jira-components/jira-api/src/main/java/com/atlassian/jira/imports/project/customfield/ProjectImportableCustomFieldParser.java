package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.parser.CustomFieldValueParser;

/**
 * If a custom field chooses to persist it's data in a table separate to the custom field values table, then it's custom
 * field type will need to implement this interface in order to make it importable via project import.
 * <p/>
 * The CustomField Type will have to return the entity name of the database table that its values are stored in.
 * Additionally it will have to implement the methods from the {@link com.atlassian.jira.imports.project.parser.CustomFieldValueParser}
 * interface.  This means that the storage of the customfield is very much constrained by it fitting into a {@link
 * com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValue} domain object.
 *
 * @since v4.2
 */
public interface ProjectImportableCustomFieldParser extends CustomFieldValueParser
{
    /**
     * The table name of the ofbiz entity (read database table) in which the custom field values for this custom field
     * type are persisted
     *
     * @return The ofbiz table name for this custom field type
     */
    String getEntityName();
}
