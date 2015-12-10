package com.atlassian.jira.imports.project.populator;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl;
import com.atlassian.jira.imports.project.parser.CustomFieldParser;
import com.atlassian.jira.imports.project.parser.CustomFieldParserImpl;

import java.util.Map;

/**
 * Populates {@link com.atlassian.jira.external.beans.ExternalCustomField}, {@link com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl.ConfigurationContext},
 * and {@link com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl.FieldConfigSchemeIssueType} objects
 * from the custom field and custom field configuration data in a backup XML file.
 * <br/>
 * This information is used to eventually generate
 * {@link com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration}'s from the
 * {@link com.atlassian.jira.imports.project.core.BackupOverviewBuilder}.
 *
 * @since v3.13
 */
public class CustomFieldPopulator implements BackupOverviewPopulator
{
    private CustomFieldParser customFieldParser;

    public void populate(final BackupOverviewBuilder backupOverviewBuilder, final String elementName, final Map attributes) throws ParseException
    {
        if (CustomFieldParser.CUSTOM_FIELD_ENTITY_NAME.equals(elementName))
        {
            final ExternalCustomField externalCustomField = getCustomFieldParser().parseCustomField(attributes);
            backupOverviewBuilder.addExternalCustomField(externalCustomField);
        }
        if (CustomFieldParser.CUSTOM_FIELD_CONFIGURATION_ENTITY_NAME.equals(elementName))
        {
            final BackupOverviewBuilderImpl.ConfigurationContext configuration = getCustomFieldParser().parseCustomFieldConfiguration(attributes);
            // Only store the configurations that are related to custom fields
            if (configuration != null)
            {
                backupOverviewBuilder.addConfigurationContext(configuration);
            }
        }
        if (CustomFieldParser.CUSTOM_FIELD_SCHEME_ISSUE_TYPE_ENTITY_NAME.equals(elementName))
        {
            final BackupOverviewBuilderImpl.FieldConfigSchemeIssueType fieldConfigSchemeIssueType = getCustomFieldParser().parseFieldConfigSchemeIssueType(
                attributes);
            backupOverviewBuilder.addFieldConfigSchemeIssueType(fieldConfigSchemeIssueType);
        }
    }

    CustomFieldParser getCustomFieldParser()
    {
        if (customFieldParser == null)
        {
            customFieldParser = new CustomFieldParserImpl();
        }
        return customFieldParser;
    }

}
