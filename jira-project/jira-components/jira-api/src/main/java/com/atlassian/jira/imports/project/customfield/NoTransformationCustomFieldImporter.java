package com.atlassian.jira.imports.project.customfield;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;

/**
 * This class provides an implementation of ProjectCustomFieldImporter for Custom Fields which don't need to transform
 * the custom field value upon import into a new JIRA instance.
 * This would be used for any Custom Fields that store a "literal value" - eg text, date, etc.
 * <p>
 * The {@link #getMappedImportValue} method will just return the original value, and the {@link ProjectCustomFieldImporter#canMapImportValue} method
 * will never return errors or warnings.
 * </p>
 *
 * @since v3.13
 */
@PublicApi
public class NoTransformationCustomFieldImporter implements ProjectCustomFieldImporter
{
    public MessageSet canMapImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig, final I18nHelper i18n)
    {
        // We will always be able to give a new value.
        return null;
    }

    public MappedCustomFieldValue getMappedImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig)
    {
        // No mapping required, just return the original String.
        return new MappedCustomFieldValue(customFieldValue.getValue());
    }
}
