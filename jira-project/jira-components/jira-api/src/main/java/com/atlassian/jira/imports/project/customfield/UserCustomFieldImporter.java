package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;

/**
 * Implementation of ProjectCustomFieldImporter for custom fields that store user keys.
 *
 * @since v3.13
 */
public class UserCustomFieldImporter implements ProjectCustomFieldImporter
{
    public MessageSet canMapImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig, final I18nHelper i18n)
    {
        final String userKey = customFieldValue.getValue();
        // ignore empty userKey including null and empty String.
        if ((userKey != null) && (userKey.length() > 0))
        {
            // Flag the userKey as required
            projectImportMapper.getUserMapper().flagUserAsInUse(userKey);
            // We don't check the Mapper directly if the userKey can be mapped, because Users can sometimes be automatically imported
            // during the Project Import.
        }
        return null;
    }

    public MappedCustomFieldValue getMappedImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig)
    {
        // Map the user key
        return new MappedCustomFieldValue(projectImportMapper.getUserMapper().getMappedUserKey(customFieldValue.getValue()));
    }
}
