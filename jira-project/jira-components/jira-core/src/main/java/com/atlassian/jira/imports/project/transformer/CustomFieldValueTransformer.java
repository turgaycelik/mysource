package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValue;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;

/**
 * Used to transform an ExternalCustomFieldValue based on the project import mapper that is provided. This should only be
 * used with a fully mapped and validated ProjectImportMapper.
 *
 * @since v3.13
 */
public interface CustomFieldValueTransformer
{
    /**
     * Transforms a custom field value by letting the custom field tell us what the value (and optionally parent value)
     * should be transformed into. The resulting ExternalCustomFieldValue will have a null id.
     *
     * @param projectImportMapper a fully populated and validated project import mapper
     * @param externalCustomFieldValue the value representing a single custom field value from the backup XML
     * @param newProjectId the id of the project, in this JIRA instance, that the data is being imported into
     *
     * @return an ExternalCustomFieldValue that contains the transformed values and a null id, null if the custom
     * field has been flagged as ignored by the CustomFieldMapper, or if the custom field thinks the value should
     * be transformed to null, or if for some reason the issue id has not been mapped.
     */
    ExternalCustomFieldValue transform(ProjectImportMapper projectImportMapper, ExternalCustomFieldValue externalCustomFieldValue, Long newProjectId);
}
