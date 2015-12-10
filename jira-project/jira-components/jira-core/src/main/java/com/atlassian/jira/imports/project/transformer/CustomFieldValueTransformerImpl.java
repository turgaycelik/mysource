package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValue;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValueImpl;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import org.apache.log4j.Logger;

/**
 * @since v3.13
 */
public class CustomFieldValueTransformerImpl implements CustomFieldValueTransformer
{
    private static final Logger log = Logger.getLogger(CustomFieldValueTransformerImpl.class);
    private final CustomFieldManager customFieldManager;

    public CustomFieldValueTransformerImpl(final CustomFieldManager customFieldManager)
    {
        this.customFieldManager = customFieldManager;
    }

    public ExternalCustomFieldValue transform(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue externalCustomFieldValue, final Long newProjectId)
    {
        // We only want to transform the value if we are not ignoring the custom field that this is associated with
        if (projectImportMapper.getCustomFieldMapper().isIgnoredCustomField(externalCustomFieldValue.getCustomFieldId()))
        {
            return null;
        }

        // Get the custom field based on the mapped custom field id
        final String mappedCustomFieldId = projectImportMapper.getCustomFieldMapper().getMappedId(externalCustomFieldValue.getCustomFieldId());
        final CustomField customField = customFieldManager.getCustomFieldObject(new Long(mappedCustomFieldId));

        // Get the mapped issue type for this issue so that we can find the relevantConfig
        final String oldIssueTypeId = projectImportMapper.getCustomFieldMapper().getIssueTypeForIssue(externalCustomFieldValue.getIssueId());
        final String newIssueTypeId = projectImportMapper.getIssueTypeMapper().getMappedId(oldIssueTypeId);

        // Get the mapped issue id
        final String mappedIssueId = projectImportMapper.getIssueMapper().getMappedId(externalCustomFieldValue.getIssueId());
        // Be defensive here and don't proceed if for some reason the issue has not been mapped
        if (mappedIssueId == null)
        {
            log.debug("Found a custom field value with old issue id '" + externalCustomFieldValue.getIssueId() + "' but there is no mapped issue id.");
            return null;
        }

        // Create a new custom field value that will be the transformed value
        final ExternalCustomFieldValueImpl transformedExternalCustomFieldValue = new ExternalCustomFieldValueImpl(null, mappedCustomFieldId,
            mappedIssueId);

        // Get the relevantConfig so that we can pass it to the field
        final FieldConfig relevantConfig = customField.getRelevantConfig(new IssueContextImpl(newProjectId, newIssueTypeId));

        // Get the field and ask it for the transformed value
        final ProjectCustomFieldImporter.MappedCustomFieldValue importValue = ((ProjectImportableCustomField) customField.getCustomFieldType()).getProjectImporter().getMappedImportValue(
            projectImportMapper, externalCustomFieldValue, relevantConfig);
        if (importValue.getValue() == null)
        {
            log.debug("Ignoring custom field value with old id '" + externalCustomFieldValue.getId() + "' because the custom field transformer returned a null transformed value.");
            // Do not create a row for this value.
            return null;
        }

        // We need to set the value into the right type so that when we save the value we persist the right data type
        if (externalCustomFieldValue.getStringValue() != null)
        {
            transformedExternalCustomFieldValue.setStringValue(importValue.getValue());
        }
        else if (externalCustomFieldValue.getDateValue() != null)
        {
            transformedExternalCustomFieldValue.setDateValue(importValue.getValue());
        }
        else if (externalCustomFieldValue.getNumberValue() != null)
        {
            transformedExternalCustomFieldValue.setNumberValue(importValue.getValue());
        }
        else if (externalCustomFieldValue.getTextValue() != null)
        {
            transformedExternalCustomFieldValue.setTextValue(importValue.getValue());
        }

        // Set the parent key in case this custom field value has one
        transformedExternalCustomFieldValue.setParentKey(importValue.getParentKey());

        return transformedExternalCustomFieldValue;
    }
}
