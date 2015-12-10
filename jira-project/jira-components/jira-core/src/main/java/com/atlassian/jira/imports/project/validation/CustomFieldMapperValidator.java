package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;

/**
 * Validates the automatic mappings that have been created to see if the mappings are relevant in the current
 * JIRA instances setup.
 * The following checks are reported as errors and will stop the import:
 * <ul>
 * <li>A required Custom Field does not exist in the current instance</li>
 * <li>The mapped CustomField is of a different Custom Field Type to that in the import file.</li>
 * <li>The mapped CustomField is not available for a required Issue Type in this Project.</li>
 * </ul>
 *
 * @since v3.13
 */
public interface CustomFieldMapperValidator
{
    /**
     * Validates the automatic mappings that have been created to see if the mappings are relevant in the current
     * JIRA instances setup.
     * The following checks are reported as errors and will stop the import:
     * <ul>
     * <li>A required Custom Field does not exist in the current instance</li>
     * <li>The mapped CustomField is of a different Custom Field Type to that in the import file.</li>
     * <li>The mapped CustomField is not available for a required Issue Type in this Project.</li>
     * </ul>
     * <p>
     * Note that validation of the actual values in the Custom Fields is done separately by the Custom Field itself.
     * </p>
     *
     * @param i18nHelper helper bean that allows us to get i18n translations
     * @param backupProject is the backup project the data is mapped from
     * @param issueTypeMapper is the populated issueTypeMapper
     * @param customFieldMapper is the populated statusMapper
     * @return a MessageSet that will contain any generated errors (which should stop the import) or warnings
     * (which should be displayed to the user). The error and warning collection's will be empty if all validation
     * passes.
     */
    MessageSet validateMappings(I18nHelper i18nHelper, BackupProject backupProject, IssueTypeMapper issueTypeMapper, CustomFieldMapper customFieldMapper);

    /**
     * Returns true if the new custom field is valid for all the issue types that the old custom field is used from.
     *
     * @param externalCustomFieldConfiguration contains the configuration of the custom field as defined in the backup XML.
     * @param newCustomField is a custom field in the current JIRA instance who's context is being checked.
     * @param oldCustomFieldId the old custom field id from the backup XML which will indicate which issue types the
     * field was used in.
     * @param customFieldMapper is the populated custom field mapper.
     * @param issueTypeMapper is the populated issue type mapper.
     * @param projectKey is the project we are importing into.
     * @return true if the new custom field is valid for all the issue types that the old custom field is used from.
     */
    boolean customFieldIsValidForRequiredContexts(ExternalCustomFieldConfiguration externalCustomFieldConfiguration, CustomField newCustomField, String oldCustomFieldId, CustomFieldMapper customFieldMapper, IssueTypeMapper issueTypeMapper, String projectKey);

    /**
     * Returns true if the given customFieldTypeKey from the Project Import exists in the current system, and also
     * implements the CustomFieldImportable interface.
     * @param customFieldTypeKey Key of the CustomField Type that we are checking.
     * @return true if the given customFieldTypeKey from the Project Import exists in the current system, and also
     * implements the CustomFieldImportable interface.
     */
    boolean customFieldTypeIsImportable(final String customFieldTypeKey);
}
