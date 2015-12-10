package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl;

import java.util.Map;

/**
 * Converts custom field and custom field configuration xml in a JIRA backup to an object representation.
 *
 * @since v3.13
 */
public interface CustomFieldParser
{
    public static final String CUSTOM_FIELD_CONFIGURATION_ENTITY_NAME = "ConfigurationContext";
    public static final String CUSTOM_FIELD_ENTITY_NAME = "CustomField";
    public static final String CUSTOM_FIELD_SCHEME_ISSUE_TYPE_ENTITY_NAME = "FieldConfigSchemeIssueType";

    /**
     * Parses the custom field data from the backup XML. This handles the custom field name, description, id, etc.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ExternalCustomField. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>id (required)</li>
     * <li>name (required)</li>
     * <li>customfieldtypekey (required)</li>
     * </ul>
     * @return an ExternalCustomField if the attributes contain the required fields
     * @throws ParseException if the required fields are not found in the attributes map
     */
    public ExternalCustomField parseCustomField(final Map attributes) throws ParseException;

    /**
     * Parses the custom field configuration context data from the backup XML. This provides the project constraints
     * for a custom field and correctly identifies the {@link com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl.FieldConfigSchemeIssueType}
     * constraints.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ConfigurationContext. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>id (required)</li>
     * <li>key (required)</li>
     * </ul>
     * Also, if the key does not start with "customfield_" then we will return a null ConfigurationContext meaning this
     * parser can not handle the value.
     * @return a ConfigurationContext that identifies a single context for a custom field, null if the ConfigurationContext
     * is not a custom field configuration context.
     * @throws ParseException if the attributes map does not contain the required fields or the field data is invalid
     */
    public BackupOverviewBuilderImpl.ConfigurationContext parseCustomFieldConfiguration(final Map attributes) throws ParseException;

    /**
     * Parses the custom field issue type configuration context data from the backup XML. This provides the issue type
     * constrains for a {@link com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl.ConfigurationContext},
     * if any exist.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an FieldConfigSchemeIssueType. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>id (required)</li>
     * <li>fieldconfigscheme (required)</li>
     * <li>issuetype (required)</li>
     * </ul>
     * @return a FieldConfigSchemeIssueType that identifies the issue types a custom field context is constrained by
     * @throws ParseException if the attributes do not contain a required field
     */
    public BackupOverviewBuilderImpl.FieldConfigSchemeIssueType parseFieldConfigSchemeIssueType(final Map attributes) throws ParseException;
}
