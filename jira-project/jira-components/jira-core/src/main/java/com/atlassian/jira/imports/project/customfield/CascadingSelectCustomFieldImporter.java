package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;

/**
 * Implements ProjectCustomFieldImporter for the "Cascading Select" custom field.
 *
 * @since v3.13
 */
public class CascadingSelectCustomFieldImporter implements ProjectCustomFieldImporter
{
    public MessageSet canMapImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig, final I18nHelper i18n)
    {
        final String optionId = customFieldValue.getValue();
        // We wouldn't expect null - the value should just be missing for a blank choice, but handle it just in case
        if (optionId != null)
        {
            // tell the Mapper that this is a required field - Note that the generic validator will validate that required fields are available.
            projectImportMapper.getCustomFieldOptionMapper().flagValueAsRequired(optionId);
        }
        return null;
    }

    public MappedCustomFieldValue getMappedImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig)
    {
        final String oldOptionId = customFieldValue.getValue();
        final String newOptionId = projectImportMapper.getCustomFieldOptionMapper().getMappedId(oldOptionId);
        final String oldParentOptionId = customFieldValue.getParentKey();
        final String newParentOptionId = projectImportMapper.getCustomFieldOptionMapper().getMappedId(oldParentOptionId);

        return new MappedCustomFieldValue(newOptionId, newParentOptionId);
    }
}
