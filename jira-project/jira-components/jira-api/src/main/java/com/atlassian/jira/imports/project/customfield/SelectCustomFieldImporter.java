package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;

/**
 * Implements ProjectCustomFieldImporter for the "Select" custom fields.
 *
 * @since v3.13
 */
public class SelectCustomFieldImporter implements ProjectCustomFieldImporter
{
    public SelectCustomFieldImporter()
    {
    }

    /**
     * Deprecated Constructor.
     *
     * @param optionsManager no longer used
     *
     * @deprecated Use {@link #SelectCustomFieldImporter()} instead. Since v4.4.
     */
    public SelectCustomFieldImporter(final OptionsManager optionsManager)
    {
    }

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

        return new MappedCustomFieldValue(newOptionId, null);
    }
}
