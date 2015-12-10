package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.external.beans.ExternalProjectRoleActor;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;

/**
 * @since v3.13
 */
public class ProjectRoleActorMapperValidatorImpl implements ProjectRoleActorMapperValidator
{
    private final UserManager userManager;

    public ProjectRoleActorMapperValidatorImpl(final UserManager userManager)
    {
        this.userManager = userManager;
    }

    public MessageSet validateProjectRoleActors(final I18nHelper i18nHelper, final ProjectImportMapper projectImportMapper, final ProjectImportOptions projectImportOptions)
    {
        final MessageSet messageSet = new MessageSetImpl();

        // We only need to do the validation if we are actually going to f**k with the role actors
        if (projectImportOptions.overwriteProjectDetails())
        {

            // Loop through all the role actors
            for (final Object o : projectImportMapper.getProjectRoleActorMapper().getAllProjectRoleActors())
            {
                final ExternalProjectRoleActor projectRoleActor = (ExternalProjectRoleActor) o;
                // Find if this actor is a user or group
                if (projectRoleActor.isUserActor())
                {
                    // Switch validation based on whether we can create new Users
                    if (isExternalUserManagementEnabled())
                    {
                        // Simple because we only care whether the user currently exists.
                        if (!projectImportMapper.getUserMapper().userExists(projectRoleActor.getRoleActor()))
                        {
                            // User is missing.
                            final String projectRoleName = projectImportMapper.getProjectRoleMapper().getDisplayName(projectRoleActor.getRoleId());
                            messageSet.addWarningMessage(i18nHelper.getText("admin.errors.project.import.project.role.actor.validation.user.missing",
                                    projectRoleName, projectRoleActor.getRoleActor()));
                            messageSet.addWarningMessageInEnglish("Project role '" + projectRoleName + "' contains a user '" + projectRoleActor.getRoleActor() + "' that doesn't exist in the current system. This user will not be added to the project role membership.");
                        }
                    }
                    else
                    {
                        // Now we have the situation where some missing users will be created during the import.
                        final String userKey = projectRoleActor.getRoleActor();
                        if (!projectImportMapper.getUserMapper().userExists(userKey))
                        {
                            // User is missing from current JIRA.
                            if (projectImportMapper.getUserMapper().getExternalUser(userKey) == null)
                            {
                                // .. and we don't have the details, so they won't be created.
                                final String projectRoleName = projectImportMapper.getProjectRoleMapper().getDisplayName(projectRoleActor.getRoleId());
                                messageSet.addWarningMessage(i18nHelper.getText(
                                        "admin.errors.project.import.project.role.actor.validation.user.missing", projectRoleName, userKey));
                                messageSet.addWarningMessageInEnglish("Project role '" + projectRoleName + "' contains a user '" + userKey + "' that doesn't exist in the current system. This user will not be added to the project role membership.");
                            }
                        }

                    }
                }
                else if (projectRoleActor.isGroupActor())
                {
                    // Check if the group exists in the current system
                    if (projectImportMapper.getGroupMapper().getMappedId(projectRoleActor.getRoleActor()) == null)
                    {
                        final String projectRoleName = projectImportMapper.getProjectRoleMapper().getDisplayName(projectRoleActor.getRoleId());
                        messageSet.addWarningMessage(i18nHelper.getText("admin.errors.project.import.project.role.actor.validation.group.missing",
                                projectRoleName, projectRoleActor.getRoleActor()));
                        messageSet.addWarningMessageInEnglish("Project role '" + projectRoleName + "' contains a group '" + projectRoleActor.getRoleActor() + "' that doesn't exist in the current system. This group will not be added to the project role membership.");
                    }
                }
                else
                {
                    final String projectRoleName = projectImportMapper.getProjectRoleMapper().getDisplayName(projectRoleActor.getRoleId());
                    messageSet.addWarningMessage(i18nHelper.getText("admin.errors.project.import.project.role.actor.validation.unknown.role.type",
                            projectRoleName, projectRoleActor.getRoleActor(), projectRoleActor.getRoleType()));
                    messageSet.addWarningMessageInEnglish("Project role '" + projectRoleName + "' contains an actor '" + projectRoleActor.getRoleActor() + "' of unknown role type '" + projectRoleActor.getRoleType() + "'. This actor will not be added to the project role.");
                }
            }
        }

        return messageSet;
    }

    boolean isExternalUserManagementEnabled()
    {
        return !userManager.hasWritableDirectory();
    }

}
