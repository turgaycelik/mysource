package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;

/**
 * Implementation of ProjectCustomFieldImporter for custom fields that store references to JIRA projects.
 *
 * @since v3.13
 */
public class ProjectPickerCustomFieldImporter implements ProjectCustomFieldImporter
{
    public MessageSet canMapImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig, final I18nHelper i18n)
    {
        final MessageSet messageSet = new MessageSetImpl();
        final SimpleProjectImportIdMapper projectMapper = projectImportMapper.getProjectMapper();
        final String valueAsIntString = getNonDecimalValue(customFieldValue.getValue());
        final String mappedId = projectMapper.getMappedId(valueAsIntString);
        if (mappedId == null)
        {
            // Add a warning that the value will be ignored since the project does not map in this system. This is different
            // than most implementations as we don't want a missing value to stop the import.
            final String oldProjectName = projectMapper.getDisplayName(valueAsIntString);
            messageSet.addWarningMessage(i18n.getText("admin.error.project.import.project.picker.no.project", oldProjectName));
            messageSet.addWarningMessageInEnglish("The project '" + oldProjectName + "' does not exist in the current JIRA system. This custom field value will not be imported.");
        }
        return messageSet;
    }

    public MappedCustomFieldValue getMappedImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig)
    {
        final SimpleProjectImportIdMapper projectMapper = projectImportMapper.getProjectMapper();
        final String valueAsIntString = getNonDecimalValue(customFieldValue.getValue());
        final String mappedId = projectMapper.getMappedId(valueAsIntString);
        return new MappedCustomFieldValue(mappedId);
    }

    private String getNonDecimalValue(final String value)
    {
        if (value != null)
        {
            return new Long(new Double(value).intValue()).toString();
        }
        return null;
    }
}
