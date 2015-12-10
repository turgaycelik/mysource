package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;

/**
 * Validates that a IssueSecurityLevel has all required security levels mapped.
 *
 * @since v3.13
 */
public class IssueSecurityLevelValidator
{
    private final ProjectManager projectManager;

    public IssueSecurityLevelValidator(final ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public MessageSet validateMappings(final SimpleProjectImportIdMapper issueSecurityLevelMapper, final BackupProject backupProject, final I18nHelper i18nHelper)
    {
        final MessageSet messageSet = new MessageSetImpl();
        for (final String oldId : issueSecurityLevelMapper.getRequiredOldIds())
        {
            // Get the mapped id
            final String newId = issueSecurityLevelMapper.getMappedId(oldId);
            if (newId == null)
            {
                // Check for orphan data
                if (issueSecurityLevelMapper.getKey(oldId) == null)
                {
                    messageSet.addWarningMessage(i18nHelper.getText("admin.warning.project.import.issue.security.level.validation.orphan", oldId));
                    messageSet.addWarningMessageInEnglish("The issue security level '" + oldId + "' can not be resolved into an actual security level in the backup file. Any issues that were protected by this security level will no longer have an issue security level.");
                }
                else
                {
                    // Add an error that the value is not mapped
                    if (projectExists(backupProject))
                    {
                        messageSet.addErrorMessage(i18nHelper.getText(
                                "admin.errors.project.import.issue.security.level.validation.does.not.exist.project.exists",
                                issueSecurityLevelMapper.getDisplayName(oldId)));
                        messageSet.addErrorMessageInEnglish("The Issue Security Level '" + issueSecurityLevelMapper.getDisplayName(oldId) + "' is required for the import but does not exist in the current JIRA instance.");
                    }
                    else
                    {
                        messageSet.addErrorMessage(i18nHelper.getText(
                                "admin.errors.project.import.issue.security.level.validation.does.not.exist.project.doesnt.exist",
                                issueSecurityLevelMapper.getDisplayName(oldId), backupProject.getProject().getKey()));
                        messageSet.addErrorMessageInEnglish("The Issue Security Level '" + issueSecurityLevelMapper.getDisplayName(oldId) + "' is required for the import but does not exist in the current JIRA instance.");
                    }
                }
            }
        }
        return messageSet;
    }

    boolean projectExists(final BackupProject backupProject)
    {
        return projectManager.getProjectObjByKey(backupProject.getProject().getKey()) != null;
    }
}
