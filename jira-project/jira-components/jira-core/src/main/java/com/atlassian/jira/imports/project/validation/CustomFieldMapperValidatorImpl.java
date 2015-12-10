package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;

import java.util.Collection;
import java.util.Iterator;

/**
 * @since v3.13
 */
public class CustomFieldMapperValidatorImpl implements CustomFieldMapperValidator
{
    private final CustomFieldManager customFieldManager;
    private final ConstantsManager constantsManager;
    private final ProjectManager projectManager;

    public CustomFieldMapperValidatorImpl(final CustomFieldManager customFieldManager, final ConstantsManager constantsManager, final ProjectManager projectManager)
    {
        this.customFieldManager = customFieldManager;
        this.constantsManager = constantsManager;
        this.projectManager = projectManager;
    }

    public MessageSet validateMappings(final I18nHelper i18nHelper, final BackupProject backupProject, final IssueTypeMapper issueTypeMapper, final CustomFieldMapper customFieldMapper)
    {
        final MessageSet messageSet = new MessageSetImpl();

        // Check all required CustomFields
        for (final String oldId : customFieldMapper.getRequiredOldIds())
        {
            final boolean customFieldMapped = customFieldMapper.getMappedId(oldId) != null;
            // We know that the CustomFieldMapperImpl will not map BAD custom fields. Once we know it is not mapped we need to find out why.
            if (customFieldMapped)
            {
                validateMappedCustomField(i18nHelper, backupProject, issueTypeMapper, customFieldMapper, messageSet, oldId);
            }
            else
            {
                validateUnmappedCustomField(i18nHelper, backupProject, issueTypeMapper, customFieldMapper, messageSet, oldId);
            }
        }
        return messageSet;
    }

    private void validateMappedCustomField(final I18nHelper i18nHelper, final BackupProject backupProject, final IssueTypeMapper issueTypeMapper, final CustomFieldMapper customFieldMapper, final MessageSet messageSet, final String oldId)
    {
        final ExternalCustomFieldConfiguration oldCustomFieldConfig = backupProject.getCustomFieldConfiguration(oldId);
        final String newId = customFieldMapper.getMappedId(oldId);
        // Check that the mapped value is valid. This is a bit redundant as the mapper will not map invalid values
        final CustomField newCustomField = customFieldManager.getCustomFieldObject(new Long(newId));
        // Check the type
        if (!customFieldTypeIsImportable(oldCustomFieldConfig.getCustomField().getTypeKey()))
        {
            // NOTE: this should not happen since the mapper should not have put us into this state, the reason
            // this is an error and not a warning is that the mess up is ours and we don't want to move forward
            final String customFieldName = oldCustomFieldConfig.getCustomField().getName();
            messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.custom.field.not.importable", customFieldName));
            messageSet.addErrorMessageInEnglish("Unable to import custom field '" + customFieldName + "'. The custom field type does not support project imports.");
        }
        else if (!newCustomField.getCustomFieldType().getKey().equals(oldCustomFieldConfig.getCustomField().getTypeKey()))
        {
            // Wrong Type
            final String customFieldTypeKey = oldCustomFieldConfig.getCustomField().getTypeKey();
            messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.custom.field.wrong.type",
                customFieldMapper.getDisplayName(oldId), customFieldTypeKey));
            messageSet.addErrorMessageInEnglish("The custom field '" + customFieldMapper.getDisplayName(oldId) + "' in the backup project is of type '" + customFieldTypeKey + "' " + "but the field with the same name in the current JIRA instance is of a different type.");
        }
        else
        {
            // Right type - check that it is usable by all required Issue Types
            if (!customFieldIsValidForRequiredContexts(oldCustomFieldConfig, newCustomField, oldId, customFieldMapper, issueTypeMapper,
                backupProject.getProject().getKey()))
            {
                final String issueTypeNames = getIssueTypeDisplayNames(oldCustomFieldConfig,
                    customFieldMapper.getIssueTypeIdsForRequiredCustomField(oldId), issueTypeMapper, i18nHelper);
                messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.custom.field.wrong.context",
                    customFieldMapper.getDisplayName(oldId), issueTypeNames));
                messageSet.addErrorMessageInEnglish("The custom field '" + customFieldMapper.getDisplayName(oldId) + "' in the backup project is used by issue types '" + issueTypeNames + "' but the field with the same name in the current JIRA instance is not available to those issue types in this project.");
            }
        }
    }

    private void validateUnmappedCustomField(final I18nHelper i18nHelper, final BackupProject backupProject, final IssueTypeMapper issueTypeMapper, final CustomFieldMapper customFieldMapper, final MessageSet messageSet, final String oldId)
    {
        final ExternalCustomFieldConfiguration oldCustomFieldConfig = backupProject.getCustomFieldConfiguration(oldId);
        if (oldCustomFieldConfig == null)
        {
            // Orphan data. The Mapper has already logged this. we safely ignore.
            return;
        }
        final String customFieldTypeKey = oldCustomFieldConfig.getCustomField().getTypeKey();
        final CustomFieldType type = customFieldManager.getCustomFieldType(customFieldTypeKey);
        if (type == null)
        {
            // The Custom Field Plugin is missing.
            final String customFieldTypeName = oldCustomFieldConfig.getCustomField().getTypeKey();
            final String customFieldDisplayName = customFieldMapper.getDisplayName(oldId);
            messageSet.addWarningMessage(i18nHelper.getText("admin.errors.project.import.custom.field.plugin.missing", customFieldDisplayName,
                customFieldTypeName));
            messageSet.addWarningMessageInEnglish("The custom field '" + customFieldDisplayName + "' will not be imported because the custom field type '" + customFieldTypeName + "' is not installed.");
            return;
        }
        else if (!(type instanceof ProjectImportableCustomField))
        {
            // Not ProjectImportableCustomField
            final String customFieldDisplayName = oldCustomFieldConfig.getCustomField().getName();
            messageSet.addWarningMessage(i18nHelper.getText("admin.errors.project.import.custom.field.not.importable", customFieldDisplayName));
            messageSet.addWarningMessageInEnglish("Unable to import custom field '" + customFieldDisplayName + "'. The custom field type does not support project imports.");
            return;
        }

        // Now we need to introspect into the system to see why the value was unable to be mapped
        final Collection customFieldsWithName = customFieldManager.getCustomFieldObjectsByName(customFieldMapper.getKey(oldId));
        if ((customFieldsWithName == null) || customFieldsWithName.isEmpty())
        {
            messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.custom.field.does.not.exist",
                customFieldMapper.getDisplayName(oldId), getCustomFieldTypeName(oldCustomFieldConfig)));
            messageSet.addErrorMessageInEnglish("The custom field '" + customFieldMapper.getDisplayName(oldId) + "' of type '" + getCustomFieldTypeName(oldCustomFieldConfig) + "' is required for the import but does not exist in the current JIRA instance.");
        }
        else
        {
            // The custom field exists but is not valid for some reason
            final boolean customFieldIsRightType = customFieldIsRightType(customFieldsWithName, oldCustomFieldConfig.getCustomField().getTypeKey());
            if (!customFieldIsRightType)
            {
                messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.custom.field.wrong.type",
                    customFieldMapper.getDisplayName(oldId), getCustomFieldTypeName(oldCustomFieldConfig)));
                messageSet.addErrorMessageInEnglish("The custom field '" + customFieldMapper.getDisplayName(oldId) + "' in the backup project is of type '" + getCustomFieldTypeName(oldCustomFieldConfig) + "' but the field with the same name in the current JIRA instance is of a different type.");
            }
            else
            {
                final String issueTypeNames = getIssueTypeDisplayNames(oldCustomFieldConfig,
                    customFieldMapper.getIssueTypeIdsForRequiredCustomField(oldId), issueTypeMapper, i18nHelper);
                messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.custom.field.wrong.context",
                    customFieldMapper.getDisplayName(oldId), issueTypeNames));
                messageSet.addErrorMessageInEnglish("The custom field '" + customFieldMapper.getDisplayName(oldId) + "' in the backup project is used by issue types '" + issueTypeNames + "' but the field with the same name in the current JIRA instance is not available to those issue types in this project.");
            }
        }
    }

    private String getCustomFieldTypeName(final ExternalCustomFieldConfiguration oldCustomFieldConfig)
    {
        // We want to include the CustomFieldType in the error message, to help the User fix the problem.
        final CustomFieldType customFieldType = customFieldManager.getCustomFieldType(oldCustomFieldConfig.getCustomField().getTypeKey());
        // Because we are not ignored, the CustomFieldType must actually exist in the system, however lets be defensive anyway.
        return customFieldType == null ? oldCustomFieldConfig.getCustomField().getTypeKey() : customFieldType.getName();
    }

    private boolean customFieldIsRightType(final Collection customFieldsWithName, final String customFieldTypeKey)
    {
        for (final Object aCustomFieldsWithName : customFieldsWithName)
        {
            final CustomField newCustomField = (CustomField) aCustomFieldsWithName;
            // Check if this is the right type
            if (customFieldTypeKey.equals(newCustomField.getCustomFieldType().getKey()))
            {
                // That's nice, now we can assume that it is NOT valid for our Project, and Issue Types.
                return true;
            }
        }
        return false;
    }

    public boolean customFieldIsValidForRequiredContexts(final ExternalCustomFieldConfiguration externalCustomFieldConfiguration, final CustomField newCustomField, final String oldCustomFieldId, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper, final String projectKey)
    {
        // Get the Project by Key as a Generic Value - we use this later to create an IssueContext.
        final Project newProject = projectManager.getProjectObjByKey(projectKey);
        final Long newProjectId = (newProject == null) ? null : newProject.getId();

        final Collection issueTypes = customFieldMapper.getIssueTypeIdsForRequiredCustomField(oldCustomFieldId);
        if (issueTypes == null)
        {
            // This should not actually happen, it is just a null safety check
            // This Custom Field has no values against our project (or the only values are "orphans" and we want to ignore it anyway).
            // But in this case, the CustomFieldMapperHandler would not be able to see the issue as in our project, and
            // so it would never have been set as a required value in the mapper.
            // We will act the same way as an empty collection (also impossible) would.
            return true;
        }
        for (final Object issueType : issueTypes)
        {
            final String oldIssueTypeId = (String) issueType;

            // First check to see that this issue type was one that the old custom field config was constrained by
            if (externalCustomFieldConfiguration.isConstrainedForIssueType(oldIssueTypeId))
            {
                final String newIssueTypeId = issueTypeMapper.getMappedId(oldIssueTypeId);
                // Now check that the current config allows for this Custom Field to be used with our project and Issue Type.
                if (newCustomField.getRelevantConfig(new IssueContextImpl(newProjectId, newIssueTypeId)) == null)
                {
                    // null relevant config means invalid context.
                    return false;
                }
            }
        }
        // The CustomField is relevant for all required Issues.
        return true;
    }

    public boolean customFieldTypeIsImportable(final String customFieldTypeKey)
    {
        final CustomFieldType customFieldType = customFieldManager.getCustomFieldType(customFieldTypeKey);
        return (customFieldType != null) && (customFieldType instanceof ProjectImportableCustomField);
    }

    String getIssueTypeDisplayNames(final ExternalCustomFieldConfiguration oldCustomFieldConfig, final Collection issueTypeIdsForRequiredCustomField, final IssueTypeMapper issueTypeMapper, final I18nHelper i18nHelper)
    {
        final StringBuilder sb = new StringBuilder();
        if (issueTypeIdsForRequiredCustomField == null)
        {
            return i18nHelper.getText("common.words.none");
        }
        int i = 0;
        for (final Iterator iterator = issueTypeIdsForRequiredCustomField.iterator(); iterator.hasNext(); i++)
        {
            final String oldIssueTypeId = (String) iterator.next();
            if (oldCustomFieldConfig.isConstrainedForIssueType(oldIssueTypeId))
            {
                if (i != 0)
                {
                    sb.append(", ");
                }
                final String newIssueTypeId = issueTypeMapper.getMappedId(oldIssueTypeId);
                sb.append(constantsManager.getIssueTypeObject(newIssueTypeId).getNameTranslation(i18nHelper));
            }
        }
        return sb.toString();
    }
}
