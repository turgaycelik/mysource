package com.atlassian.jira.imports.project.mapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldOption;
import com.atlassian.jira.imports.project.util.IssueTypeImportHelper;
import com.atlassian.jira.imports.project.validation.CustomFieldMapperValidator;
import com.atlassian.jira.imports.project.validation.StatusMapperValidator;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.ProjectContext;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;

/**
 * @since v3.13
 */
public class AutomaticDataMapperImpl implements AutomaticDataMapper
{
    private static final Logger log = Logger.getLogger(AutomaticDataMapperImpl.class);

    private final ConstantsManager constantsManager;
    private final CustomFieldManager customFieldManager;
    private final CustomFieldMapperValidator customFieldMapperValidator;
    private final ProjectManager projectManager;
    private final IssueTypeImportHelper issueTypeImportHelper;
    private final StatusMapperValidator statusMapperValidator;
    private final ProjectRoleManager projectRoleManager;
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final SubTaskManager subTaskManager;
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;

    public AutomaticDataMapperImpl(final ConstantsManager constantsManager,
            final CustomFieldManager customFieldManager, final CustomFieldMapperValidator customFieldMapperValidator,
            final ProjectManager projectManager, final IssueTypeImportHelper issueTypeImportHelper,
            final StatusMapperValidator statusMapperValidator, final ProjectRoleManager projectRoleManager,
            final IssueLinkTypeManager issueLinkTypeManager, final SubTaskManager subTaskManager,
            final IssueSecurityLevelManager issueSecurityLevelManager,
            final IssueSecuritySchemeManager issueSecuritySchemeManager,
            final FieldConfigSchemeManager fieldConfigSchemeManager)
    {
        this.constantsManager = constantsManager;
        this.customFieldManager = customFieldManager;
        this.customFieldMapperValidator = customFieldMapperValidator;
        this.projectManager = projectManager;
        this.issueTypeImportHelper = issueTypeImportHelper;
        this.statusMapperValidator = statusMapperValidator;
        this.projectRoleManager = projectRoleManager;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.subTaskManager = subTaskManager;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
    }

    public void mapIssueTypes(final BackupProject backupProject, final IssueTypeMapper issueTypeMapper)
    {
        // Loop through all the objects found in the import file.
        for (final String oldId : issueTypeMapper.getRegisteredOldIds())
        {
            // Find the new ID for this in our current system.
            final IssueType newIssueType = issueTypeImportHelper.getIssueTypeForName(issueTypeMapper.getKey(oldId));
            // If we have found one we still need to check if it is valid
            if ((newIssueType != null) && issueTypeImportHelper.isMappingValid(newIssueType, backupProject.getProject().getKey(),
                    issueTypeMapper.isSubTask(oldId)))
            {
                issueTypeMapper.mapValue(oldId, newIssueType.getId());
            }
        }
    }

    public void mapIssueLinkTypes(final IssueLinkTypeMapper issueLinkTypeMapper)
    {
        // Loop through all the objects found in the import file.
        for (final String oldId : issueLinkTypeMapper.getRegisteredOldIds())
        {
            final String issueLinkTypeName = issueLinkTypeMapper.getKey(oldId);
            final IssueLinkType newIssueLinkType = getIssueLinkTypeByName(issueLinkTypeName);
            if (newIssueLinkType != null)
            {
                if (checkStyleIsValid(oldId, newIssueLinkType, issueLinkTypeMapper))
                {
                    issueLinkTypeMapper.mapValue(oldId, newIssueLinkType.getId().toString());
                }
            }
        }
    }

    private boolean checkStyleIsValid(final String oldId, final IssueLinkType issueLinkType, final IssueLinkTypeMapper issueLinkTypeMapper)
    {
        final String oldStyle = issueLinkTypeMapper.getStyle(oldId);
        final String newStyle = issueLinkType.getStyle();
        if (oldStyle == null)
        {
            // The new style must be null.
            return newStyle == null;
        }
        else
        {
            // The old style is non-null.
            // This is presumably because the link type is for subtasks. This is the only use of style at time of writing.
            if (oldStyle.equals(SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE))
            {
                // check that subtasks are enabled.
                if (!subTaskManager.isSubTasksEnabled())
                {
                    // Subtasks must be enabled.
                    return false;
                }
            }

            // Also check that old and new style are the same.
            return oldStyle.equals(newStyle);
        }
    }

    private IssueLinkType getIssueLinkTypeByName(final String linkTypeName)
    {
        final Collection linkTypes = issueLinkTypeManager.getIssueLinkTypesByName(linkTypeName);
        if (linkTypes.isEmpty())
        {
            return null;
        }
        // Return the first one - there should never be more than one.
        return (IssueLinkType) linkTypes.iterator().next();
    }

    public void mapPriorities(final SimpleProjectImportIdMapper priorityMapper)
    {
        // Create a Map from the name to the ID for the objects in the current System.
        final Map nameToIdMap = new HashMap();
        for (final Priority priority : constantsManager.getPriorityObjects())
        {
            nameToIdMap.put(priority.getName(), priority.getId());
        }
        // Use this name-id map to add automatic mappings to our ProjectImportIdMapper
        autopopulateMapper(priorityMapper, nameToIdMap);
    }

    public void mapResolutions(final SimpleProjectImportIdMapper resolutionMapper)
    {
        // Create a Map from the name to the ID for the objects in the current System.
        final Map nameToIdMap = new HashMap();
        for (final Resolution resolution : constantsManager.getResolutionObjects())
        {
            nameToIdMap.put(resolution.getName(), resolution.getId());
        }
        // Use this name-id map to add automatic mappings to our ProjectImportIdMapper
        autopopulateMapper(resolutionMapper, nameToIdMap);
    }

    public void mapStatuses(final BackupProject backupProject, final StatusMapper statusMapper, final IssueTypeMapper issueTypeMapper)
    {
        // We want to map all registered statuses that are valid for our project and the issue types that are in use
        for (final String oldStatusId : statusMapper.getRegisteredOldIds())
        {
            final Status newStatus = constantsManager.getStatusByName(statusMapper.getKey(oldStatusId));
            if ((newStatus != null) && statusMapperValidator.isStatusValid(oldStatusId, newStatus, statusMapper, issueTypeMapper,
                    backupProject.getProject().getKey()))
            {
                statusMapper.mapValue(oldStatusId, newStatus.getId());
            }
        }
    }

    public void mapProjectRoles(final SimpleProjectImportIdMapper projectRoleMapper)
    {
        // We want to map all registered Project Roles.
        for (final String oldProjectRoleId : projectRoleMapper.getRegisteredOldIds())
        {
            final ProjectRole newProjectRole = projectRoleManager.getProjectRole(projectRoleMapper.getKey(oldProjectRoleId));
            if (newProjectRole != null)
            {
                projectRoleMapper.mapValue(oldProjectRoleId, newProjectRole.getId().toString());
            }
        }
    }

    public void mapIssueSecurityLevels(final String projectKey, final SimpleProjectImportIdMapper securityLevelMapper)
    {
        // Security Levels are an Enterprise-only feature.
        final Long issueSecuritySchemeId = getIssueSecuritySchemeId(projectKey);
        if (issueSecuritySchemeId == null)
        {
            // No scheme therefore no mapping.
            return;
        }

        final List<GenericValue> securityLevelsInScheme = issueSecurityLevelManager.getSchemeIssueSecurityLevels(issueSecuritySchemeId);

        // We want to map all registered Security Levels.
        for (final String oldSecurityLevelId : securityLevelMapper.getRegisteredOldIds())
        {
            final String name = securityLevelMapper.getKey(oldSecurityLevelId);
            if (name != null)
            {
                // Try to find this Security Level in the current system.
                for (final GenericValue genericValue : securityLevelsInScheme)
                {
                    if (name.equals(genericValue.getString("name")))
                    {
                        securityLevelMapper.mapValue(oldSecurityLevelId, genericValue.getLong("id").toString());
                    }
                }
            }
        }
    }

    public void mapProjects(final SimpleProjectImportIdMapper projectMapper)
    {
        // Create a Map from the name to the ID for the objects in the current System.
        final Map nameToIdMap = new HashMap();
        for (final GenericValue gvProject : projectManager.getProjects())
        {
            nameToIdMap.put(gvProject.getString("key"), gvProject.getLong("id").toString());
        }
        // Use this name-id map to add automatic mappings to our ProjectImportIdMapper
        autopopulateMapper(projectMapper, nameToIdMap);
    }

    public void mapCustomFields(final BackupProject backupProject, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper)
    {
        // Loop though all the Custom Fields we found in the backup that are configured for this project.
        for (final ExternalCustomFieldConfiguration oldCustomFieldConfig : backupProject.getCustomFields())
        {
            final ExternalCustomField oldCustomField = oldCustomFieldConfig.getCustomField();
            // The Custom Field Type - Note that ExternalCustomField does not allow a null TypeKey.
            final String oldCustomFieldTypeKey = oldCustomField.getTypeKey();
            // Check if this type exists in the current system and implements the required "Importable" interface.
            if (!customFieldMapperValidator.customFieldTypeIsImportable(oldCustomFieldTypeKey))
            {
                // Not Importable - we ignore this Custom Field, but the validator best warn the user.
                customFieldMapper.ignoreCustomField(oldCustomField.getId());
            }
            else
            {
                final String oldCustomFieldId = oldCustomField.getId();
                // Find all custom fields with the same name
                final Collection<CustomField> customFieldsWithName = customFieldManager.getCustomFieldObjectsByName(oldCustomField.getName());
                if (customFieldsWithName != null)
                {
                    for (final CustomField newCustomField : customFieldsWithName)
                    {
                        // Check if this is the right type.
                        if (oldCustomFieldTypeKey.equals(newCustomField.getCustomFieldType().getKey()))
                        {
                            // That's nice, but is it valid for our Project, and Issue Types?
                            if (customFieldMapperValidator.customFieldIsValidForRequiredContexts(oldCustomFieldConfig, newCustomField,
                                    oldCustomFieldId, customFieldMapper, issueTypeMapper, backupProject.getProject().getKey()))
                            {
                                customFieldMapper.mapValue(oldCustomField.getId(), newCustomField.getIdAsLong().toString());
                                // Note it is actually possible to have multiple Custom Fields with the same name and type,
                                // but this would be fairly stupid and there is not much we can do to map this correctly.
                                break;
                            }
                        }
                    }
                }
            }
        }

        // We also want to check if there are any orphan custom field values that are registered against unknown fields,
        // in this case we want to ignore these custom fields
        for (final String oldId : customFieldMapper.getRequiredOldIds())
        {
            // If the id has not been registered then this must be an orphan value and we should ignore the custom field
            if (customFieldMapper.getKey(oldId) == null)
            {

                log.warn("The backup data has a custom field value in use for custom field with id: '" + oldId + "' but the backup data contains no reference to this field. The data will be not be imported.");
                customFieldMapper.ignoreCustomField(oldId);
            }
        }
    }

    public void mapCustomFieldOptions(final BackupProject backupProject, final CustomFieldOptionMapper customFieldOptionMapper, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper)
    {
        // Loop through the CustomFields in our BackupProject
        for (final ExternalCustomFieldConfiguration externalCustomFieldConfiguration : backupProject.getCustomFields())
        {
            // see if this custom field has options
            final Collection oldParentOptions = customFieldOptionMapper.getParentOptions(externalCustomFieldConfiguration.getConfigurationSchemeId());
            if (!oldParentOptions.isEmpty())
            {
                final Options options = getNewOptions(backupProject, customFieldMapper, issueTypeMapper, externalCustomFieldConfiguration);

                if (options != null)
                {
                    mapOptions(options, customFieldOptionMapper, oldParentOptions);
                }
            }
        }
    }

    /**
     * Returns the new Options for the new (mapped) Custom Field.
     * it will return null if we do not have a mapped Custom Field.
     *
     * @param backupProject     BackupProject
     * @param customFieldMapper CustomFieldMapper
     * @param issueTypeMapper   IssueTypeMapper
     * @param externalCustomFieldConfiguration
     *                          ExternalCustomFieldConfiguration
     * @return the new Options for the new (mapped) Custom Field.
     */
    Options getNewOptions(final BackupProject backupProject, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper, final ExternalCustomFieldConfiguration externalCustomFieldConfiguration)
    {
        // Get the mapped custom field id
        final String newCustomFieldId = customFieldMapper.getMappedId(externalCustomFieldConfiguration.getCustomField().getId());
        if (newCustomFieldId == null)
        {
            return null;
        }
        final CustomField newCustomField = customFieldManager.getCustomFieldObject(new Long(newCustomFieldId));

        final Project newProject = projectManager.getProjectObjByKey(backupProject.getProject().getKey());
        final Long newProjectId = (newProject != null) ? newProject.getId() : null;

        final JiraContextNode projectContext = getProjectContext(newProjectId);
        final FieldConfigScheme scheme = fieldConfigSchemeManager.getRelevantConfigScheme(projectContext, newCustomField);
        final FieldConfig relevantConfig = scheme == null ? null : scheme.getOneAndOnlyConfig();

        // Finally get the options
        return newCustomField.getOptions(null, relevantConfig, projectContext);
    }

    ///CLOVER:OFF
    JiraContextNode getProjectContext(final Long newProjectId)
    {
        return new ProjectContext(newProjectId);
    }

    ///CLOVER:ON

    void mapOptions(final Options options, final CustomFieldOptionMapper customFieldOptionMapper, final Collection parentOptions)
    {
        for (final Object parentOption : parentOptions)
        {
            final ExternalCustomFieldOption oldParentOption = (ExternalCustomFieldOption) parentOption;
            final Option newParentOption = options.getOptionForValue(oldParentOption.getValue(), null);
            if (newParentOption != null)
            {
                customFieldOptionMapper.mapValue(oldParentOption.getId(), newParentOption.getOptionId().toString());
                // Now lets map the children values
                final Collection childOptions = customFieldOptionMapper.getChildOptions(oldParentOption.getId());
                for (final Object childOption : childOptions)
                {
                    final ExternalCustomFieldOption oldChildOption = (ExternalCustomFieldOption) childOption;
                    final Option newChildOption = options.getOptionForValue(oldChildOption.getValue(), newParentOption.getOptionId());
                    if (newChildOption != null)
                    {
                        customFieldOptionMapper.mapValue(oldChildOption.getId(), newChildOption.getOptionId().toString());
                    }
                }
            }
        }
    }

    ///CLOVER:ON
    Long getIssueSecuritySchemeId(final String projectKey)
    {
        final GenericValue projectGV = projectManager.getProjectByKey(projectKey);
        if (projectGV == null)
        {
            // Project doesn't exist yet and there is no default scheme.
            return null;
        }
        else
        {
            try
            {
                final List schemes = issueSecuritySchemeManager.getSchemes(projectGV);
                if (schemes.isEmpty())
                {
                    return null;
                }
                else
                {
                    // There is actually only allowed to be one Scheme.
                    final GenericValue schemeGV = (GenericValue) schemes.iterator().next();
                    return schemeGV.getLong("id");
                }
            }
            catch (final GenericEntityException e)
            {
                throw new DataAccessException(e);
            }
        }
    }

    private void autopopulateMapper(final ProjectImportIdMapper projectImportIdMapper, final Map nameToIdMap)
    {
        // Loop through all the objects found in the import file.
        for (final String oldId : projectImportIdMapper.getRegisteredOldIds())
        {
            // Find the new ID for this in our current system.
            final String newId = (String) nameToIdMap.get(projectImportIdMapper.getKey(oldId));
            // Now we can add the mapping from old ID to new ID
            if (newId != null)
            {
                projectImportIdMapper.mapValue(oldId, newId);
            }
        }
    }

}
