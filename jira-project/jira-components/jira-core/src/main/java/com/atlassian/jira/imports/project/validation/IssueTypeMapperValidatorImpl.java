package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import com.atlassian.jira.imports.project.util.IssueTypeImportHelper;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;

/**
 * @since v3.13
 */
public class IssueTypeMapperValidatorImpl implements IssueTypeMapperValidator
{
    private final ConstantsManager constantsManager;
    private final IssueTypeImportHelper issueTypeImportHelper;
    private final SubTaskManager subTaskManager;

    public IssueTypeMapperValidatorImpl(final ConstantsManager constantsManager, final IssueTypeImportHelper issueTypeImportHelper, final SubTaskManager subTaskManager)
    {
        this.constantsManager = constantsManager;
        this.issueTypeImportHelper = issueTypeImportHelper;
        this.subTaskManager = subTaskManager;
    }

    public MessageSet validateMappings(final I18nHelper i18nHelper, final BackupProject backupProject, final IssueTypeMapper issueTypeMapper)
    {
        final MessageSet messageSet = new MessageSetImpl();

        // Get the required objects
        for (final String s : issueTypeMapper.getRequiredOldIds())
        {
            boolean errorFoundForThisIssue = false;
            final String oldId = s;
            // Get the mapped id
            final String newId = issueTypeMapper.getMappedId(oldId);
            final IssueType newIssueType;

            // We want to either validate the issue type for the mapped id OR try to figure out why we could not
            // correctly map the issue type, this means trying to get the most suitable issue type and seeing what
            // is wrong with it
            if (newId == null)
            {
                // Get the "naive" mapping and expect that we can find a reason why it is invalid
                newIssueType = issueTypeImportHelper.getIssueTypeForName(issueTypeMapper.getKey(oldId));
            }
            else
            {
                newIssueType = constantsManager.getIssueTypeObject(newId);
            }

            if (newIssueType == null)
            {
                // Add an error that the value is not mapped
                if (issueTypeMapper.isSubTask(oldId))
                {
                    if (subTaskManager.isSubTasksEnabled())
                    {
                        messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.issue.type.validation.subtask.does.not.exist",
                                issueTypeMapper.getDisplayName(oldId)));
                        messageSet.addErrorMessageInEnglish("Sub-tasks are currently disabled in JIRA, please enable sub-tasks. The sub-task issue type '" + issueTypeMapper.getDisplayName(oldId) + "' is required for the import but does not exist in the current JIRA instance.");
                    }
                    else
                    {
                        messageSet.addErrorMessage(i18nHelper.getText(
                                "admin.errors.project.import.issue.type.validation.subtask.does.not.exist.not.enabled",
                                issueTypeMapper.getDisplayName(oldId)));
                        messageSet.addErrorMessageInEnglish("The issue type '" + issueTypeMapper.getDisplayName(oldId) + "' is required for the import but does not exist in the current JIRA instance.");
                    }
                }
                else
                {
                    messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.issue.type.validation.does.not.exist",
                            issueTypeMapper.getDisplayName(oldId)));
                    messageSet.addErrorMessageInEnglish("The issue type '" + issueTypeMapper.getDisplayName(oldId) + "' is required for the import but does not exist in the current JIRA instance.");
                }
            }
            else
            {
                // Check if it is invalid
                if (!issueTypeImportHelper.isIssueTypeValidForProject(backupProject.getProject().getKey(), newIssueType.getId()))
                {
                    final String issueTypeDisplayName = issueTypeMapper.getDisplayName(oldId);
                    messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.issue.type.validation.not.in.scheme",
                            issueTypeDisplayName));
                    messageSet.addErrorMessageInEnglish("The issue type '" + issueTypeDisplayName + "' exists in the system but is not valid for the projects issue type scheme.");
                    errorFoundForThisIssue = true;
                }
                // We don't want to allow a subtask issue type to be mapped to a normal issue type and vice-versa
                if (issueTypeMapper.isSubTask(oldId) && !newIssueType.isSubTask())
                {
                    final String issueTypeDisplayName = issueTypeMapper.getDisplayName(oldId);
                    messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.issue.type.validation.subtask.normal",
                            issueTypeDisplayName));
                    messageSet.addErrorMessageInEnglish("The issue type '" + issueTypeDisplayName + "' is defined as a sub-task in the backup project, but it is a normal issue type in the current JIRA instance.");
                    errorFoundForThisIssue = true;
                }
                else if (!issueTypeMapper.isSubTask(oldId) && newIssueType.isSubTask())
                {
                    final String issueTypeDisplayName = issueTypeMapper.getDisplayName(oldId);
                    messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.issue.type.validation.normal.subtask",
                            issueTypeDisplayName));
                    messageSet.addErrorMessageInEnglish("The issue type '" + issueTypeDisplayName + "' is defined as a normal issue type in the backup project, but it is a sub-task issue type in the current JIRA instance.");
                    errorFoundForThisIssue = true;
                }
                // If this required value is NOT mapped, then ensure we have some error
                if ((newId == null) && !errorFoundForThisIssue)
                {
                    // Note that this would not happen at time of writing when the auto-mapper uses the sam logic as the validator.
                    // However, if we introduce manual mapping, or change the aoutmap logic, this is an important safety check.
                    // Add an error that the value is not mapped
                    final String issueTypeDisplayName = issueTypeMapper.getDisplayName(oldId);
                    messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.issue.type.validation.required.not.mapped",
                            issueTypeDisplayName));
                    messageSet.addErrorMessageInEnglish("The issue type '" + issueTypeDisplayName + "' is required for the import but it is not mapped.");
                }
            }
        }
        return messageSet;
    }

}
