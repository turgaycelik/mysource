package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import com.atlassian.jira.imports.project.mapper.StatusMapper;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ListOrderedMessageSetImpl;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @since v3.13
 */
public class StatusMapperValidatorImpl implements StatusMapperValidator
{
    private static final Logger log = Logger.getLogger(StatusMapperValidatorImpl.class);

    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;
    private final WorkflowManager workflowManager;

    public StatusMapperValidatorImpl(final ProjectManager projectManager, final ConstantsManager constantsManager, final WorkflowManager workflowManager)
    {
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
        this.workflowManager = workflowManager;
    }

    public MessageSet validateMappings(final I18nHelper i18nHelper, final BackupProject backupProject, final IssueTypeMapper issueTypeMapper, final StatusMapper statusMapper)
    {
        final String projectKey = backupProject.getProject().getKey();
        final MessageSet messageSet = new ListOrderedMessageSetImpl();

        // Check all required statuses
        for (final String oldId : statusMapper.getRequiredOldIds())
        {
            // Get the mapped id
            final String newId = statusMapper.getMappedId(oldId);
            if (newId == null)
            {
                // It could be that the status exists, but is invalid for the required workflow.
                // Try to do optimistic mapping based on name.
                final String statusName = statusMapper.getKey(oldId);
                if (statusName == null)
                {
                    // Assuming this isn't a bug in our MapperHandler, this would be an "orphan" Status (inconsistent import file).
                    // The status with ID ''{0}'' is required for the import but could not find this configured in the import.
                    messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.status.validation.status.not.registered", oldId));
                    messageSet.addErrorMessageInEnglish("The status with ID '" + oldId + "' is required for the import but could not find this Status configured in the import.");
                }
                else
                {
                    final Status existingStatus = constantsManager.getStatusByName(statusName);
                    if (existingStatus == null)
                    {
                        // Add an error that the value is not mapped
                        messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.status.validation.does.not.exist",
                                statusMapper.getDisplayName(oldId)));
                        messageSet.addErrorMessageInEnglish("The status '" + statusMapper.getDisplayName(oldId) + "' is required for the import but does not exist in the current JIRA instance.");
                    }

                    // We expect that the Status is invalid for the required workflow for at least one Issue Type. Even if the status does not exist we want the messages about
                    // its use in the workflow.
                    if (validateStatus(oldId, existingStatus, statusMapper, issueTypeMapper, projectKey, messageSet, i18nHelper))
                    {
                        // Not really expected - The auto mapper should have mapped this - we must add an error message.
                        // The status ''{0}'' is required for the import but was not mapped.
                        messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.status.validation.required.not.mapped",
                                statusMapper.getDisplayName(oldId)));
                        messageSet.addErrorMessageInEnglish("The status '" + statusMapper.getDisplayName(oldId) + "' is required for the import but was not mapped.");
                    }
                }
            }
            else
            {
                final Status existingStatus = constantsManager.getStatusObject(newId);
                validateStatus(oldId, existingStatus, statusMapper, issueTypeMapper, projectKey, messageSet, i18nHelper);
            }
        }
        return messageSet;
    }

    public boolean isStatusValid(final String oldStatusId, final Status existingStatus, final StatusMapper statusMapper, final IssueTypeMapper issueTypeMapper, final String projectKey)
    {
        return validateStatus(oldStatusId, existingStatus, statusMapper, issueTypeMapper, projectKey, null, null);
    }

    private boolean validateStatus(final String oldStatusId, final Status existingStatus, final StatusMapper statusMapper, final IssueTypeMapper issueTypeMapper, final String projectKey, final MessageSet messageSet, final I18nHelper i18nHelper)
    {
        final Project existingProject = projectManager.getProjectObjByKey(projectKey);
        boolean valid = true;
        // Now this status could be required for several Issue Types. We must check that the status is valid in all the
        // appropriate workflows.
        final Collection requiredStatus = statusMapper.getIssueTypeIdsForRequiredStatus(oldStatusId);
        if (requiredStatus != null)
        {
            for (final Object requiredStatu : requiredStatus)
            {
                final String issueTypeId = (String) requiredStatu;
                final JiraWorkflow workflow = (existingProject == null) ? workflowManager.getDefaultWorkflow() : workflowManager.getWorkflow(
                        existingProject.getId(), issueTypeMapper.getMappedId(issueTypeId));
                if (!workflow.getLinkedStatusObjects().contains(existingStatus))
                {
                    valid = false;
                    // If we have a MessageSet, then add an error that the Status is no good for this Issue Type
                    // The status ''{0}'' needs to be associated with Issue Type ''{1}'', but this status is not valid in the workflow ''{2}'' configured for this Issue Type.
                    if (messageSet != null)
                    {
                        final String statusDisplayName = statusMapper.getDisplayName(oldStatusId);
                        final String issueTypeDisplayName = issueTypeMapper.getDisplayName(issueTypeId);
                        final String workflowName = workflow.getName();

                        final MessageSet.MessageLink link = getMessageLink("setupworkflowissuegroup", i18nHelper);
                        if (existingProject != null)
                        {
                            // The project exists
                            if (isUsingDefaultWorkflow(workflow))
                            {
                                final String errorMessage = i18nHelper.getText(
                                        "admin.errors.project.import.status.validation.enterprise.existing.project.default.workflow.not.in.workflow",
                                        statusDisplayName, issueTypeDisplayName, workflowName);
                                messageSet.addErrorMessage(errorMessage, link);
                                messageSet.addErrorMessageInEnglish("The status '" + statusDisplayName + "' is in use by an issue of type '" + issueTypeDisplayName + "' in the backup file. The default workflow '" + workflowName + "', which is associated with issue type ''{1}'', does not use this status. This workflow is not editable. You must associate a workflow with issue type '" + issueTypeDisplayName + "' that uses the status. To do this you will need to use a workflow scheme.");
                            }
                            else
                            {
                                final String errorMessage = i18nHelper.getText(
                                        "admin.errors.project.import.status.validation.enterprise.existing.project.custom.workflow.not.in.workflow",
                                        statusDisplayName, issueTypeDisplayName, workflowName);
                                messageSet.addErrorMessage(errorMessage, link);
                                messageSet.addErrorMessageInEnglish("The status '" + statusDisplayName + "' is in use by an issue of type '" + issueTypeDisplayName + "' in the backup file. The workflow '" + workflowName + "', which is associated with issue type '" + issueTypeDisplayName + "', does not use this status. You must either edit the workflow to use the status or associate a workflow with issue type '" + issueTypeDisplayName + "' that uses the status.");
                            }
                        }
                        else
                        {
                            // The importer is trying to create the project AND this implies we are using the default workflow
                            final String errorMessage = i18nHelper.getText(
                                    "admin.errors.project.import.status.validation.enterprise.no.existing.project.default.workflow.not.in.workflow",
                                    statusDisplayName, issueTypeDisplayName, workflowName, projectKey);
                            messageSet.addErrorMessage(errorMessage, link);
                            messageSet.addErrorMessageInEnglish("The status '" + statusDisplayName + "' is in use by an issue of type '" + issueTypeDisplayName + "' in the backup file. The default workflow '" + workflowName + "', which is associated with issue type '" + issueTypeDisplayName + "', does not use this status. This workflow is not editable. You must create a project with key '" + projectKey + "', instead of letting the import create it for you, and associate a workflow with issue type '" + issueTypeDisplayName + "' that uses the status. To do this you will need to use a workflow scheme.");
                        }
                    }
                }
            }
        }
        return valid;
    }

    MessageSet.MessageLink getMessageLink(final String helpPathProperty, final I18nHelper i18n)
    {
        final HelpUtil.HelpPath helpPath = HelpUtil.getInstance().getHelpPath(helpPathProperty);
        return new MessageSet.MessageLink(i18n.getText("admin.errors.project.import.status.validation.doc.link.text"), helpPath.getUrl(), true);
    }

    ///CLOVER:OFF
    boolean isUsingDefaultWorkflow(final JiraWorkflow workflow)
    {
        final JiraWorkflow defaultWorkflow = workflowManager.getDefaultWorkflow();
        return defaultWorkflow.equals(workflow);
    }
    ///CLOVER:ON
}
