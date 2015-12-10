package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import org.apache.log4j.Logger;

/**
 * Implements ProjectCustomFieldImporter for the "Version" custom fields.
 *
 * @since v3.13
 */
public class VersionCustomFieldImporter implements ProjectCustomFieldImporter
{
    private static final Logger log = Logger.getLogger(VersionCustomFieldImporter.class);

    public MessageSet canMapImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig, final I18nHelper i18n)
    {
        // We can always map the versions since the importer will create them for us
        return null;
    }

    public MappedCustomFieldValue getMappedImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig)
    {
        // Since the value is stored as a number custom field we need to get the int, string value of it so we can get
        // the correctly mapped value
        final String oldVersionId = getNonDecimalValue(customFieldValue.getValue());
        final String mappedVersionId = projectImportMapper.getVersionMapper().getMappedId(oldVersionId);
        if (mappedVersionId == null)
        {
            log.warn("The version custom field '" + fieldConfig.getCustomField().getName() + "' references a version with id '" + oldVersionId + "' that is an orphan value. The value will not be imported.");
        }
        return new MappedCustomFieldValue(mappedVersionId);
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
