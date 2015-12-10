package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValue;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.CustomFieldValueParser;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * This handler is used to give the mapped custom fields an opportunity to validate the custom field values
 * that we are going to ask them to map. The custom fields will be provided with the values context
 *
 * NOTE: The mapper that is passed to this class MUST be a mapper that has either categorized EVERY custom field
 * referenced by the custom field values as mapped or ignored. The class will throw ParseException if it encounters
 * a custom field that is not mapped in one of these two states.

 * @since v3.13
 */
public class CustomFieldValueValidatorHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(CustomFieldValueValidatorHandler.class);

    private final ProjectImportMapper projectImportMapper;
    private final CustomFieldManager customFieldManager;
    private final Map<String, CustomFieldValueParser> parsers;
    private final Long newProjectId;
    final Map customFieldConfigMap;
    final Map fieldMessages;

    public CustomFieldValueValidatorHandler(final BackupProject backupProject, final ProjectImportMapper projectImportMapper, final CustomFieldManager customFieldManager, final Map<String, CustomFieldValueParser> parsers)
    {
        this.projectImportMapper = projectImportMapper;
        this.customFieldManager = customFieldManager;
        this.parsers = parsers;
        fieldMessages = new HashMap();
        customFieldConfigMap = new HashMap();

        // Get the NEW project ID.
        final String newProjectIdStr = projectImportMapper.getProjectMapper().getMappedId(backupProject.getProject().getId());
        newProjectId = (newProjectIdStr == null) ? null : new Long(newProjectIdStr);
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        if(parsers.containsKey(entityName))
        {
            final ExternalCustomFieldValue customFieldValue = parsers.get(entityName).parse(attributes);
            if(customFieldValue != null)
            {
                // Check to see if we should ignore this custom field value
                if (projectImportMapper.getCustomFieldMapper().isIgnoredCustomField(customFieldValue.getCustomFieldId()))
                {
                    return;
                }

                final String mappedId = projectImportMapper.getCustomFieldMapper().getMappedId(customFieldValue.getCustomFieldId());
                final CustomField customField = getCustomField(customFieldValue);

                if (customField == null)
                {
                    log.warn("While validating custom field values we have run into a mapped custom field '" + mappedId + "' which is not in the system and is not marked as ignored.");
                }
                else if (!(customField.getCustomFieldType() instanceof ProjectImportableCustomField))
                {
                    log.warn("While validating custom field values we have run into a mapped custom field '" + mappedId + "' which is not ProjectImportable and is not marked as ignored.");
                }
                else
                {
                    final ProjectCustomFieldImporter projectCustomFieldImporter = ((ProjectImportableCustomField) customField.getCustomFieldType()).getProjectImporter();
                    validateCustomFieldValueWithField(customField, projectCustomFieldImporter, customFieldValue);
                }
            }
        }
    }

    public Map /*<String,MessageSet>*/getValidationResults()
    {
        return fieldMessages;
    }

    void validateCustomFieldValueWithField(final CustomField customField, final ProjectCustomFieldImporter projectCustomFieldImporter, final ExternalCustomFieldValue externalCustomFieldValue)
    {
        // Get the relevant config for this custom field value, based on the issue type and project
        final String oldIssueTypeId = projectImportMapper.getCustomFieldMapper().getIssueTypeForIssue(externalCustomFieldValue.getIssueId());
        final String newIssueTypeId = projectImportMapper.getIssueTypeMapper().getMappedId(oldIssueTypeId);
        if (newIssueTypeId != null)
        {
            // Gets the relevant config for this Custom Field in the context of the current Issue Type.
            final FieldConfig relevantConfig = getCustomFieldConfig(customField, newIssueTypeId);
            if (relevantConfig == null)
            {
                // It is not even valid to store data against this Custom Field in this context - we don't validate this
                // as we will actually ignore the value when it comes time to import.
                log.debug("Skipping validation on value for Custom Field '" + customField.getId() + "', unable to find a relevant configuration for issue type '" + newIssueTypeId + "'.");
                return;
            }

            final I18nHelper i18nBean = getI18nFromCustomField(customField);
            final MessageSet messageSet = projectCustomFieldImporter.canMapImportValue(projectImportMapper, externalCustomFieldValue, relevantConfig,
                i18nBean);
            final String fieldId = externalCustomFieldValue.getCustomFieldId();
            MessageSet existingSet = (MessageSet) fieldMessages.get(fieldId);
            if (existingSet == null)
            {
                // This is the first time we have seen this custom field - build a message set.
                existingSet = new MessageSetImpl();
                fieldMessages.put(fieldId, existingSet);
            }
            existingSet.addMessageSet(messageSet);
        }
    }

    I18nHelper getI18nFromCustomField(final CustomField customField)
    {
        return customField.getCustomFieldType().getDescriptor().getI18nBean();
    }

    CustomField getCustomField(final ExternalCustomFieldValue externalCustomFieldValue) throws ParseException
    {
        // Find the custom field this value is mapped to
        final String oldCustomFieldId = externalCustomFieldValue.getCustomFieldId();
        final String mappedId = projectImportMapper.getCustomFieldMapper().getMappedId(oldCustomFieldId);
        if (mappedId == null)
        {
            log.error("During custom field value validation we have encountered a custom field with id '" + oldCustomFieldId + "' which the mapper does not know about.");
            throw new ParseException(
                "During custom field value validation we have encountered a custom field with id '" + oldCustomFieldId + "' which the mapper does not know about.");
        }
        final Long customFieldId;
        try
        {
            customFieldId = new Long(mappedId);
        }
        catch (final NumberFormatException e)
        {
            log.error("Encountered a custom field value with a custom field id '" + mappedId + "' which is not a valid number.");
            throw new ParseException("Encountered a custom field value with a custom field id '" + mappedId + "' which is not a valid number.");
        }

        return customFieldManager.getCustomFieldObject(customFieldId);
    }

    /**
     * Gets hold of the Field Config for the given CustomField when used with the given Issue Type.
     * This value is cached to minimise DB IO.
     *
     * @param customField The Custom Field.
     * @param newIssueTypeId The Issue Type.
     * @return The Custom Field Config for the given CustomField when used with the given Issue Type.
     */
    FieldConfig getCustomFieldConfig(final CustomField customField, final String newIssueTypeId)
    {
        final CustomFieldConfigMapKey key = new CustomFieldConfigMapKey(customField.getId(), newIssueTypeId);
        FieldConfig fieldConfig = (FieldConfig) customFieldConfigMap.get(key);
        if (fieldConfig == null)
        {
            fieldConfig = customField.getRelevantConfig(new IssueContextImpl(newProjectId, newIssueTypeId));
            customFieldConfigMap.put(key, fieldConfig);
        }
        return fieldConfig;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public void startDocument()
    {
    // No-op
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public void endDocument()
    {
    // No-op
    }

    ///CLOVER:ON

    static class CustomFieldConfigMapKey
    {
        private final String customFieldId;
        private final String issueTypeId;

        CustomFieldConfigMapKey(final String customFieldId, final String issueTypeId)
        {
            this.customFieldId = customFieldId;
            this.issueTypeId = issueTypeId;
        }

        ///CLOVER:OFF
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass()))
            {
                return false;
            }

            final CustomFieldConfigMapKey that = (CustomFieldConfigMapKey) o;

            if (customFieldId != null ? !customFieldId.equals(that.customFieldId) : that.customFieldId != null)
            {
                return false;
            }
            if (issueTypeId != null ? !issueTypeId.equals(that.issueTypeId) : that.issueTypeId != null)
            {
                return false;
            }

            return true;
        }

        ///CLOVER:ON

        ///CLOVER:OFF
        public int hashCode()
        {
            int result;
            result = (customFieldId != null ? customFieldId.hashCode() : 0);
            result = 31 * result + (issueTypeId != null ? issueTypeId.hashCode() : 0);
            return result;
        }
        ///CLOVER:ON
    }

    ///CLOVER:OFF
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final CustomFieldValueValidatorHandler that = (CustomFieldValueValidatorHandler) o;

        if (customFieldConfigMap != null ? !customFieldConfigMap.equals(that.customFieldConfigMap) : that.customFieldConfigMap != null)
        {
            return false;
        }
        if (customFieldManager != null ? !customFieldManager.equals(that.customFieldManager) : that.customFieldManager != null)
        {
            return false;
        }
        if (fieldMessages != null ? !fieldMessages.equals(that.fieldMessages) : that.fieldMessages != null)
        {
            return false;
        }
        if (newProjectId != null ? !newProjectId.equals(that.newProjectId) : that.newProjectId != null)
        {
            return false;
        }
        if (projectImportMapper != null ? !projectImportMapper.equals(that.projectImportMapper) : that.projectImportMapper != null)
        {
            return false;
        }

        return true;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public int hashCode()
    {
        int result;
        result = (projectImportMapper != null ? projectImportMapper.hashCode() : 0);
        result = 31 * result + (customFieldManager != null ? customFieldManager.hashCode() : 0);
        result = 31 * result + (newProjectId != null ? newProjectId.hashCode() : 0);
        result = 31 * result + (customFieldConfigMap != null ? customFieldConfigMap.hashCode() : 0);
        result = 31 * result + (fieldMessages != null ? fieldMessages.hashCode() : 0);
        return result;
    }
    ///CLOVER:OFF
}
