package com.atlassian.jira.bc.imports.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.imports.project.ProjectImportManager;
import com.atlassian.jira.imports.project.core.BackupOverview;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.MappingResult;
import com.atlassian.jira.imports.project.core.ProjectImportData;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.core.ProjectImportResultsImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.handler.AbortImportException;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.taskprogress.EntityCountTaskProgressProcessor;
import com.atlassian.jira.imports.project.taskprogress.TaskProgressInterval;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.plugin.PluginVersion;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.dbc.Null;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the project import service.
 *
 * @since v3.13
 */
public class DefaultProjectImportService implements ProjectImportService
{
    // The Java package structure of services live under "com.atlassian.jira.bc."
    // In order to manage this Logger with all the other ProjectImport stuff, if we omit the "bc" from the hierarchy.
    private static final Logger log = Logger.getLogger("com.atlassian.jira.imports.project.DefaultProjectImportService");

    private final PermissionManager permissionManager;
    private final ProjectImportManager projectImportManager;
    private final ProjectManager projectManager;
    private final UserManager userManager;
    private final IssueManager issueManager;
    private final VersionManager versionManager;
    private final ProjectComponentManager projectComponentManager;
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;
    private final AttachmentManager attachmentManager;
    private final AttachmentPathManager attachmentPathManager;
    private final BuildUtilsInfo buildUtilsInfo;

    public DefaultProjectImportService(final PermissionManager permissionManager, final ProjectImportManager projectImportManager, final ProjectManager projectManager, final UserManager userManager, final IssueManager issueManager, final VersionManager versionManager, final ProjectComponentManager projectComponentManager, final PluginAccessor pluginAccessor, final ApplicationProperties applicationProperties, final AttachmentManager attachmentManager, final AttachmentPathManager attachmentPathManager, final BuildUtilsInfo buildUtilsInfo)
    {
        this.permissionManager = permissionManager;
        this.projectImportManager = projectImportManager;
        this.projectManager = projectManager;
        this.userManager = userManager;
        this.issueManager = issueManager;
        this.versionManager = versionManager;
        this.projectComponentManager = projectComponentManager;
        this.pluginAccessor = pluginAccessor;
        this.applicationProperties = applicationProperties;
        this.attachmentManager = attachmentManager;
        this.attachmentPathManager = attachmentPathManager;
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
    }

    public void validateGetBackupOverview(final JiraServiceContext jiraServiceContext, final ProjectImportOptions projectImportOptions)
    {
        Null.not("projectImportOptions", projectImportOptions);
        validateJiraServiceContext(jiraServiceContext);

        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        // The user must have the system administrator permission to perform a project import
        if (!userHasSysAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.must.be.admin"));
            // Don't care to check any more validity
            return;
        }

        if (StringUtils.isEmpty(projectImportOptions.getPathToBackupXml()))
        {
            errorCollection.addError("backupXmlPath", getText(i18n, "admin.errors.project.import.provide.backup.path"));
        }
        else if (!pathExists(projectImportOptions.getPathToBackupXml(), true))
        {
            errorCollection.addError("backupXmlPath", getText(i18n, "admin.errors.project.import.invalid.backup.path"));
        }

        // Check if the user has supplied an attachment path
        if (!StringUtils.isEmpty(projectImportOptions.getAttachmentPath()))
        {
            // Check that attachments are enabled for the current JIRA
            if (!attachmentManager.attachmentsEnabled())
            {
                errorCollection.addError("backupAttachmentPath", getText(i18n, "admin.errors.project.import.attachments.not.enabled"));
            }
            // Now check if the path exists
            else if (pathExists(projectImportOptions.getAttachmentPath(), false))
            {
                // Path Exists, but it is not allowed to be the current system's Attachment Path.
                // Get the configured attachment path for this JIRA instance.
                final String attachmentPathString = attachmentPathManager.getAttachmentPath();
                // Create a File object with it.
                final File attachmentPathFile = new File(attachmentPathString);
                // Create a File object with the attachment path
                final File backupAttachmentPathFile = new File(projectImportOptions.getAttachmentPath());
                // Compare the canonical paths to see if the directories are the same.
                try
                {
                    if (attachmentPathFile.getCanonicalPath().equals(backupAttachmentPathFile.getCanonicalPath()))
                    {
                        errorCollection.addError("backupAttachmentPath", getText(i18n,
                            "admin.errors.project.import.attachment.backup.path.same.as.system"));
                    }
                }
                catch (final IOException e)
                {
                    // This would be rather strange, but see the javadoc for getCanonicalFile():
                    // "If an I/O error occurs, which is possible because the construction of the canonical pathname may require filesystem queries"
                    errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.attachment.ioexception", e.getMessage()));
                }
            }
            else
            {
                // Path doesn't exist
                errorCollection.addError("backupAttachmentPath", getText(i18n, "admin.errors.project.import.invalid.attachment.backup.path"));
            }
        }
    }

    @Nullable
    public BackupOverview getBackupOverview(final JiraServiceContext jiraServiceContext, final ProjectImportOptions projectImportOptions, final TaskProgressSink taskProgressSink)
    {
        Null.not("projectImportOptions", projectImportOptions);
        validateJiraServiceContext(jiraServiceContext);
        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        // The user must have the system administrator permission to perform a project import
        if (!userHasSysAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.must.be.admin"));
            return null;
        }

        try
        {
            final String backupPath = projectImportOptions.getPathToBackupXml();
            log.info("Project Import: Parsing the backup file '" + backupPath + "' to obtain a Backup Overview.");
            final BackupOverview backupOverview = projectImportManager.getBackupOverview(backupPath, taskProgressSink, i18n);
            log.debug("Project count for backup file = " + backupOverview.getProjects().size());
            log.debug("Entity count for backup file = " + backupOverview.getBackupSystemInformation().getEntityCount());

            // Now do some further validation
            // BuildNumbers must be exactly the same.
            if (!getBuildNumber().equalsIgnoreCase(backupOverview.getBackupSystemInformation().getBuildNumber()))
            {
                final String errorMessage = getText(i18n, "admin.errors.project.import.wrong.build.number", getBuildNumber(),
                    backupOverview.getBackupSystemInformation().getBuildNumber());
                errorCollection.addErrorMessage(errorMessage);
                log.error("This data appears to be from an older version of JIRA. Please upgrade the data and try again. The current version of JIRA is at build number '" + getBuildNumber() + "', but the supplied backup file was for build number '" + backupOverview.getBackupSystemInformation().getBuildNumber() + "'.");
            }

            // Only return the backupOverview if we do not have any errors, otherwise we want to fall through and return null
            if (!errorCollection.hasAnyErrors())
            {
                log.info("Project Import: Backup Overview was successfully extracted from '" + backupPath + "'.");

                return backupOverview;
            }
        }
        catch (final IOException e)
        {
            log.error("There was a problem accessing the file '" + projectImportOptions.getPathToBackupXml() + "' when performing a project import.",
                e);
            errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.problem.reading.backup",
                projectImportOptions.getPathToBackupXml()));
        }
        catch (final SAXException e)
        {
            log.error("There was a problem with the SAX parsing of the file '" + projectImportOptions.getPathToBackupXml() + "' when performing a project import.");
            errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.sax.problem", projectImportOptions.getPathToBackupXml(),
                e.getMessage()));
        }
        catch (Exception e)
        {
            log.error("There was a unexpected problem processing the backup XML file at " + projectImportOptions.getPathToBackupXml(), e);
            errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.unexpected.problem", projectImportOptions.getPathToBackupXml(), e.getMessage()));
        }
        return null;
    }

    public MessageSet validateBackupProjectImportableSystemLevel(final JiraServiceContext jiraServiceContext, final BackupProject backupProject, final BackupSystemInformation backupSystemInformation)
    {
        validateJiraServiceContext(jiraServiceContext);
        // No need to check if backupProject has null members, we will never create backup project like that.
        Null.not("backupSystemInformation", backupSystemInformation);

        final MessageSet messageSet = new MessageSetImpl();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        // Need to provide a backupProject
        if (backupProject == null)
        {
            messageSet.addErrorMessage(getText(i18n, "admin.error.project.import.null.project"));
            jiraServiceContext.getErrorCollection().addErrorMessage(getText(i18n, "admin.error.project.import.null.project"));
            return messageSet;
        }

        // The user must have the system administrator permission to perform a project import
        if (!userHasSysAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            messageSet.addErrorMessage(getText(i18n, "admin.errors.project.import.must.be.admin"));
        }
        else
        {
            // Verify that if the backup projects custom field plugins exist that they are of the right version in this JIRA instance
            // NOTE: warnings, such as the plugin not existing or the custom field being not importable or out of context
            // are not checked here, that is handled by the next phase of the import.
            validateCustomFieldPluginVersions(backupProject, backupSystemInformation.getPluginVersions(), messageSet, i18n);

            final String projectKey = backupProject.getProject().getKey();
            final Project existingProject = projectManager.getProjectObjByKey(projectKey);
            if (existingProject == null)
            {
                // It does not really make sense to warn that we will create a project for them if there are already errors.
                if (!messageSet.hasAnyErrors())
                {
                    messageSet.addWarningMessage(getText(i18n, "admin.warning.project.import.no.existing.project", projectKey));
                }
            }
            else if (!projectKey.equals(existingProject.getKey()))
            {
                messageSet.addErrorMessage(getText(i18n, "admin.error.project.import.historical.key", existingProject.getName(), projectKey));
            }
            else
            {
                // We need to make sure that the project does not contain issues, versions, components, etc...
                validateExistingProjectHasValidStateForImport(backupProject, backupSystemInformation, existingProject, i18n, messageSet);
            }
        }

        // Copy the errors into the service context error collection
        jiraServiceContext.getErrorCollection().addErrorMessages(messageSet.getErrorMessages());
        return messageSet;
    }

    public void validateDoMapping(final JiraServiceContext jiraServiceContext, final ProjectImportOptions projectImportOptions, final BackupProject backupProject, final BackupSystemInformation backupSystemInformation)
    {
        Null.not("projectImportOptions", projectImportOptions);
        Null.not("backupSystemInformation", backupSystemInformation);
        validateJiraServiceContext(jiraServiceContext);
        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        // The user must have the system administrator permission to perform a project import
        if (!userHasSysAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.must.be.admin"));
        }

        // Check the pathToBackupXml is valid
        if (StringUtils.isEmpty(projectImportOptions.getPathToBackupXml()))
        {
            errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.provide.backup.path"));
        }
        else if (!pathExists(projectImportOptions.getPathToBackupXml(), true))
        {
            errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.invalid.backup.path"));
        }

        // Check that we are being provided a backup project
        if (backupProject == null)
        {
            errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.no.backup.project"));
        }
    }

    public MappingResult doMapping(final JiraServiceContext jiraServiceContext, final ProjectImportOptions projectImportOptions, final ProjectImportData projectImportData, final BackupProject backupProject, final BackupSystemInformation backupSystemInformation, final TaskProgressInterval taskProgressInterval)
    {
        Null.not("backupProject", backupProject);
        Null.not("projectImportOptions", projectImportOptions);
        Null.not("backupSystemInformation", backupSystemInformation);
        Null.not("projectImportData", projectImportData);
        validateJiraServiceContext(jiraServiceContext);

        // The user must have the system administrator permission to perform a project import
        if (!userHasSysAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(
                getText(jiraServiceContext.getI18nBean(), "admin.errors.project.import.must.be.admin"));
            return null;
        }

        // Now that we have the initial mapper we need to auto map all the fields and their values and validate that our
        // mappings are correct.
        final MappingResult mappingResult = validateAndAutoMapFields(jiraServiceContext, projectImportOptions, projectImportData, backupProject,
            backupSystemInformation, taskProgressInterval);
        if ((mappingResult != null) && !mappingResult.canImport())
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(
                jiraServiceContext.getI18nBean().getText("admin.errors.project.import.mapping.error"));
        }
        // We have log messages in the mapping Result that we didn't write to the log straight away as you could get ltos of repeat messages. We log these now.

        writeLogMessages(mappingResult);
        return mappingResult;
    }

    private void writeLogMessages(final MappingResult mappingResult)
    {
        if (mappingResult == null)
        {
            return;
        }
        // Log messages from System Fields
        for (final MappingResult.ValidationMessage validationMessage : mappingResult.getSystemFieldsMessageList())
        {
            writeLogMessages(validationMessage.getMessageSet());
        }
        // Log messages from Custom Fields
        for (final MappingResult.ValidationMessage validationMessage : mappingResult.getCustomFieldsMessageList())
        {
            writeLogMessages(validationMessage.getMessageSet());
        }
    }

    private void writeLogMessages(final MessageSet messageSet)
    {
        if (messageSet == null)
        {
            return;
        }
        // Log the errors
        for (final String s1 : messageSet.getErrorMessagesInEnglish())
        {
            log.error(s1);
        }
        // Log the warnings
        for (final String s : messageSet.getWarningMessagesInEnglish())
        {
            log.warn(s);
        }
    }

    public ProjectImportResults doImport(final JiraServiceContext jiraServiceContext, final ProjectImportOptions projectImportOptions, final BackupProject backupProject, final BackupSystemInformation backupSystemInformation, final ProjectImportData projectImportData, final TaskProgressInterval taskProgressInterval)
    {
        Null.not("projectImportOptions", projectImportOptions);
        Null.not("backupSystemInformation", backupSystemInformation);
        Null.not("projectImportData", projectImportData);
        Null.not("backupProject", backupProject);
        validateJiraServiceContext(jiraServiceContext);
        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        // Get the expected number of users that we will create
        final int usersToCreate = (isExternalUserManagementEnabled()) ? 0 : projectImportData.getProjectImportMapper().getUserMapper().getUsersToAutoCreate().size();

        final ProjectImportResults projectImportResults = getInitialImportResults(projectImportData, i18n, usersToCreate);

        // The user must have the system administrator permission to perform a project import
        if (!userHasSysAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.must.be.admin"));
            return projectImportResults;
        }

        try
        {
            log.info("Starting project import for project '" + backupProject.getProject().getKey() + "'.");
            if (isExternalUserManagementEnabled())
            {
                log.info("User directories are all read-only. No users will be imported.");
            }
            else
            {
                log.info("Creating missing users. Attempting to create " + projectImportResults.getExpectedUsersCreatedCount() + " users.");
                // External User Management is OFF - create missing users that we can.
                // This will fill in subtask progress from 0% - 10% of the doImport task
                final TaskProgressInterval subInterval = getSubInterval(taskProgressInterval, 0, 10);
                projectImportManager.createMissingUsers(projectImportData.getProjectImportMapper().getUserMapper(), projectImportResults, subInterval);
                log.info("Finished creating missing users. " + projectImportResults.getUsersCreatedCount() + " users created.");
            }

            // Create/Update the project, its details, components, versions, role membership
            try
            {
                // This will fill in subtask progress from 10% - 20% of the doImport task (Allow for creating lots of Project Role members)
                final TaskProgressInterval subInterval = getSubInterval(taskProgressInterval, 10, 20);
                projectImportManager.importProject(projectImportOptions, projectImportData.getProjectImportMapper(), backupProject,
                    projectImportResults, subInterval);
            }
            catch (final AbortImportException e)
            {
                // Add an error message
                errorCollection.addErrorMessage(i18n.getText("admin.error.project.import.project.update.error"));
                throw e;
            }

            // Import the issues and all their related values and reIndex the project once it is done
            try
            {
                // This will fill in subtask progress from 20% - 100% of the doImport task (Allow for creating lots of Project Role members)
                final TaskProgressInterval subInterval = getSubInterval(taskProgressInterval, 20, 100);
                projectImportManager.doImport(projectImportOptions, projectImportData, backupProject, backupSystemInformation, projectImportResults,
                    subInterval, i18n, jiraServiceContext.getLoggedInUser());
                // Only set the completed flag once everything has finished
                projectImportResults.setImportCompleted(true);
            }
            catch (final IOException e)
            {
                log.error("There was a problem accessing the partitioned XML files when performing a project import.", e);
                errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.problem.reading.partitioned.xml", e.getMessage()));
            }
            catch (final AbortImportException aie)
            {
                // Note that AbortImportException extends SAXException, so we need to catch and handle AbortImportException first.
                log.error("The import was aborted because there were too many errors.");
                errorCollection.addErrorMessage(i18n.getText("admin.errors.project.import.import.error"));
            }
            catch (final SAXException e)
            {
                log.error("There was a problem accessing the partitioned XML files when performing a project import.", e);
                errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.sax.problem.partitioned.xml", e.getMessage()));
            }
            catch (final IndexException e)
            {
                log.error("There was a problem reIndexing the newly imported project.", e);
                errorCollection.addErrorMessage(i18n.getText("admin.errors.project.import.reindex.problem", e.getMessage()));
            }
            log.info("Finished project import for project '" + backupProject.getProject().getKey() + "'.");

        }
        catch (final AbortImportException aie)
        {
            log.error("The import was aborted because there were too many errors.");
            errorCollection.addErrorMessage(i18n.getText("admin.errors.project.import.import.error"));
        }
        catch (final RuntimeException re)
        {
            log.error("An unexpected error occurred while importing the project with key: " + backupProject.getProject().getKey(), re);
            throw re;
        }

        // Clean up the temporary "partitioned" XML files.
        projectImportData.getTemporaryFiles().deleteTempFiles();

        // Always record the end of the import.
        projectImportResults.setEndTime(System.currentTimeMillis());
        logImportResults(projectImportResults);
        return projectImportResults;
    }

    private void logImportResults(final ProjectImportResults projectImportResults)
    {
        log.info("The project import took '" + projectImportResults.getImportDuration() + "' ms to run.");
        final Project project = projectImportResults.getImportedProject();
        if (project != null)
        {
            log.info("The project import created '" + project.getComponents().size() + "' project components.");
            log.info("The project import created '" + project.getVersions().size() + "' project versions.");
        }
        log.info("The project import created '" + projectImportResults.getUsersCreatedCount() + "' out of '" + projectImportResults.getExpectedUsersCreatedCount() + "' users.");
        for (final String projectRoleName : projectImportResults.getRoles())
        {
            log.info("The project import created " + projectImportResults.getUsersCreatedCountForRole(projectRoleName) + " users, " + projectImportResults.getGroupsCreatedCountForRole(projectRoleName) + " groups for project role " + projectRoleName + ".");
        }
        log.info("The project import created '" + projectImportResults.getIssuesCreatedCount() + "' out of '" + projectImportResults.getExpectedIssuesCreatedCount() + "' issues.");
        log.info("The project import created '" + projectImportResults.getAttachmentsCreatedCount() + "' out of " + projectImportResults.getExpectedAttachmentsCreatedCount() + "' attachments.");
    }

    private TaskProgressInterval getSubInterval(final TaskProgressInterval taskProgressInterval, final int subIntervalStart, final int subIntervalEnd)
    {
        if (taskProgressInterval == null)
        {
            return null;
        }
        else
        {
            return taskProgressInterval.getSubInterval(subIntervalStart, subIntervalEnd);
        }
    }

    void createValidationMessageList(final MappingResult mappingResult, final ProjectImportData projectImportData, final I18nHelper i18nHelper)
    {
        // create an ordered list of Validation Messages with translated Context names.
        final List<MappingResult.ValidationMessage> systemFieldsMessageList = new ArrayList<MappingResult.ValidationMessage>();
        // Issue Type
        systemFieldsMessageList.add(new MappingResult.ValidationMessage(i18nHelper.getText("issue.field.issuetype"),
            mappingResult.getIssueTypeMessageSet()));
        // Custom Field
        systemFieldsMessageList.add(new MappingResult.ValidationMessage(i18nHelper.getText("admin.project.import.custom.field.configuration"),
            mappingResult.getCustomFieldMessageSet()));
        // Status
        systemFieldsMessageList.add(new MappingResult.ValidationMessage(i18nHelper.getText("issue.field.status"), mappingResult.getStatusMessageSet()));
        // Priority
        systemFieldsMessageList.add(new MappingResult.ValidationMessage(i18nHelper.getText("issue.field.priority"),
            mappingResult.getPriorityMessageSet()));
        // Resolution
        systemFieldsMessageList.add(new MappingResult.ValidationMessage(i18nHelper.getText("issue.field.resolution"),
            mappingResult.getResolutionMessageSet()));
        // Users
        systemFieldsMessageList.add(new MappingResult.ValidationMessage(i18nHelper.getText("admin.common.words.users"),
            mappingResult.getUserMessageSet()));
        // ProjectRole
        systemFieldsMessageList.add(new MappingResult.ValidationMessage(i18nHelper.getText("admin.common.words.projectrole"),
            mappingResult.getProjectRoleMessageSet()));
        // ProjectRoleActors
        systemFieldsMessageList.add(new MappingResult.ValidationMessage(i18nHelper.getText("admin.common.words.projectrole.membership"),
            mappingResult.getProjectRoleActorMessageSet()));
        // Group
        systemFieldsMessageList.add(new MappingResult.ValidationMessage(i18nHelper.getText("admin.common.words.group"),
            mappingResult.getGroupMessageSet()));
        // IssueLinkType
        systemFieldsMessageList.add(new MappingResult.ValidationMessage(i18nHelper.getText("common.concepts.issuelinktype"),
            mappingResult.getIssueLinkTypeMessageSet()));
        // IssueSecurityLevel
        systemFieldsMessageList.add(new MappingResult.ValidationMessage(i18nHelper.getText("admin.common.words.issue.security.level"),
            mappingResult.getIssueSecurityLevelMessageSet()));
        // File Attachments
        systemFieldsMessageList.add(new MappingResult.ValidationMessage(i18nHelper.getText("common.concepts.attachments.files"),
            mappingResult.getFileAttachmentMessageSet()));

        mappingResult.setSystemFieldsMessageList(systemFieldsMessageList);

        // Now find Custom Field Value messages
        final List<MappingResult.ValidationMessage> customFieldsMessageList = new ArrayList<MappingResult.ValidationMessage>();
        final CustomFieldMapper customFieldMapper = projectImportData.getProjectImportMapper().getCustomFieldMapper();
        for (final String oldCustomFieldId : mappingResult.getCustomFieldValueMessageSets().keySet())
        {
            final MessageSet messageSet = mappingResult.getCustomFieldValueMessageSets().get(oldCustomFieldId);
            customFieldsMessageList.add(new MappingResult.ValidationMessage(customFieldMapper.getDisplayName(oldCustomFieldId), messageSet));
        }
        mappingResult.setCustomFieldsMessageList(customFieldsMessageList);
    }

    private boolean projectHasDefaultAssigneeUnassigned(final BackupProject backupProject, final BackupSystemInformation backupSystemInformation)
    {
        Long assigneeType;
        try
        {
            assigneeType = new Long(backupProject.getProject().getAssigneeType());
        }
        catch (final NumberFormatException e)
        {
            // Act as null - best guess
            assigneeType = null;
        }
        if (assigneeType == null)
        {
            // Then it was dependant on the value of the "Allow unassigned issues" setting.
            return backupSystemInformation.unassignedIssuesAllowed();
        }
        return assigneeType.longValue() == AssigneeTypes.UNASSIGNED;
    }

    public ProjectImportData getProjectImportData(final JiraServiceContext jiraServiceContext, final ProjectImportOptions projectImportOptions, final BackupProject backupProject, final BackupSystemInformation backupSystemInformation, final TaskProgressInterval taskProgressInterval)
    {
        Null.not("backupProject", backupProject);
        Null.not("projectImportOptions", projectImportOptions);
        Null.not("backupSystemInformation", backupSystemInformation);
        validateJiraServiceContext(jiraServiceContext);

        // The user must have the system administrator permission to perform a project import
        if (!userHasSysAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(
                getText(jiraServiceContext.getI18nBean(), "admin.errors.project.import.must.be.admin"));
            return null;
        }

        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();
        final ProjectImportData projectImportData;
        try
        {
            // First step is to go through the import file again, populating our mappers and creating partitioned XML files.
            // Create the Task Progress Processor for this subtask.
            EntityCountTaskProgressProcessor taskProgressProcessor = null;
            if (taskProgressInterval != null)
            {
                taskProgressProcessor = new EntityCountTaskProgressProcessor(taskProgressInterval,
                    i18n.getText("admin.message.project.import.manager.do.mapping.extracting.project.data"),
                    backupSystemInformation.getEntityCount(), i18n);
            }
            projectImportData = projectImportManager.getProjectImportData(projectImportOptions, backupProject, backupSystemInformation,
                taskProgressProcessor);
        }
        catch (final IOException e)
        {
            log.error("There was a problem accessing the file '" + projectImportOptions.getPathToBackupXml() + "' when performing a project import.",
                e);
            errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.problem.reading.backup",
                projectImportOptions.getPathToBackupXml()));
            return null;
        }
        catch (final SAXException e)
        {
            log.error("There was a problem with the SAX parsing of the file '" + projectImportOptions.getPathToBackupXml() + "' when performing a project import.");
            errorCollection.addErrorMessage(getText(i18n, "admin.errors.project.import.sax.problem", projectImportOptions.getPathToBackupXml(),
                e.getMessage()));
            return null;
        }
        return projectImportData;
    }

    private MappingResult validateAndAutoMapFields(final JiraServiceContext jiraServiceContext, final ProjectImportOptions projectImportOptions, final ProjectImportData projectImportData, final BackupProject backupProject, final BackupSystemInformation backupSystemInformation, final TaskProgressInterval taskProgressInterval)
    {
        log.info("Project Import: Mapping the backed up data to data in the current system, and validating the mappings...");
        final MappingResult mappingResult = buildMappingResult();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        // Step 2 Map and validate the Issue Types
        projectImportManager.autoMapAndValidateIssueTypes(projectImportData, mappingResult, backupProject, jiraServiceContext.getI18nBean());

        // If there is a problem processing the issue types then we don't want to do any further mappings or validation
        if ((mappingResult.getIssueTypeMessageSet() != null) && !mappingResult.getIssueTypeMessageSet().hasAnyErrors())
        {
            // Try to map the custom fields
            projectImportManager.autoMapAndValidateCustomFields(projectImportData, mappingResult, backupProject, i18n);

            if (!mappingResult.getCustomFieldMessageSet().hasAnyErrors())
            {
                // Only map the system fields if we can move forward with the custom fields
                projectImportManager.autoMapSystemFields(projectImportData, backupProject);

                // Only map the project roles if we can move forward with the custom fields
                projectImportManager.autoMapProjectRoles(projectImportData);

                // Only map the custom field values once we know that the custom fields are good
                projectImportManager.autoMapCustomFieldOptions(projectImportData, backupProject);

                final boolean importAttachments = !StringUtils.isEmpty(projectImportOptions.getAttachmentPath());
                final int customFieldValuePercentage = (importAttachments) ? 60 : 90;
                // if we can successfully map the custom fields then lets validate the custom field values
                try
                {
                    // Create a TaskProgressProcessor for validateCustomFieldValues
                    final TaskProgressInterval subInterval = getSubInterval(taskProgressInterval, 0, customFieldValuePercentage);
                    EntityCountTaskProgressProcessor taskProgressProcessor = null;
                    if (taskProgressInterval != null)
                    {
                        taskProgressProcessor = new EntityCountTaskProgressProcessor(subInterval,
                            i18n.getText("admin.message.project.import.manager.do.mapping.validate.custom.field.values"),
                            projectImportData.getCustomFieldValuesEntityCount(), i18n);
                    }
                    projectImportManager.validateCustomFieldValues(projectImportData, mappingResult, backupProject, taskProgressProcessor, i18n);
                }
                catch (final IOException e)
                {
                    log.error(
                        "There was a problem accessing the file '" + projectImportData.getPathToCustomFieldValuesXml() + "' when performing a project import.",
                        e);
                    jiraServiceContext.getErrorCollection().addErrorMessage(
                        getText(i18n, "admin.errors.project.import.problem.reading.custom.field.xml",
                            projectImportData.getPathToCustomFieldValuesXml()));
                    return null;
                }
                catch (final SAXException e)
                {
                    log.error(
                        "There was a problem accessing the file '" + projectImportData.getPathToCustomFieldValuesXml() + "' when performing a project import.",
                        e);
                    jiraServiceContext.getErrorCollection().addErrorMessage(
                        getText(i18n, "admin.errors.project.import.custom.field.sax.problem", projectImportData.getPathToCustomFieldValuesXml(),
                            e.getMessage()));
                    return null;
                }

                // Only validate the system field mappings after we have done all the rest
                // Create a sub interval of the taskProgressInterval we were given.
                TaskProgressInterval sysFieldSubInterval = null;
                if (taskProgressInterval != null)
                {
                    sysFieldSubInterval = taskProgressInterval.getSubInterval(customFieldValuePercentage, customFieldValuePercentage + 10);
                }
                projectImportManager.validateSystemFields(projectImportData, mappingResult, projectImportOptions, backupProject, sysFieldSubInterval,
                    i18n);

                // Validate the attachments if we are importing attachments
                if (!importAttachments)
                {
                    final MessageSet messageSet = new MessageSetImpl();
                    messageSet.addWarningMessage(getText(i18n, "admin.warning.project.import.mapping.no.backup.atttachment.path"));
                    log.warn("File attachments will not be imported because you have not provided a backup attachment path.");
                    mappingResult.setFileAttachmentMessageSet(messageSet);
                }
                else
                {
                    try
                    {
                        // Create a TaskProgressProcessor for validateFileAttachments
                        final TaskProgressInterval attachmentSubInterval = getSubInterval(taskProgressInterval, 70, 100);
                        EntityCountTaskProgressProcessor taskProgressProcessor = null;
                        if (taskProgressInterval != null)
                        {
                            taskProgressProcessor = new EntityCountTaskProgressProcessor(attachmentSubInterval,
                                i18n.getText("admin.message.project.import.manager.do.mapping.validate.file.attachment.values"),
                                projectImportData.getFileAttachmentEntityCount(), i18n);
                        }
                        projectImportManager.validateFileAttachments(projectImportOptions, projectImportData, mappingResult, backupProject,
                            backupSystemInformation, taskProgressProcessor, i18n);
                    }
                    catch (final IOException e)
                    {
                        log.error(
                            "There was a problem accessing the file '" + projectImportData.getPathToFileAttachmentXml() + "' when performing a project import.",
                            e);
                        jiraServiceContext.getErrorCollection().addErrorMessage(
                            getText(i18n, "admin.errors.project.import.problem.reading.attachment.xml",
                                projectImportData.getPathToFileAttachmentXml()));
                        return null;
                    }
                    catch (final SAXException e)
                    {
                        log.error(
                            "There was a problem accessing the file '" + projectImportData.getPathToFileAttachmentXml() + "' when performing a project import.",
                            e);
                        jiraServiceContext.getErrorCollection().addErrorMessage(
                            getText(i18n, "admin.errors.project.import.custom.field.sax.problem", projectImportData.getPathToFileAttachmentXml(),
                                e.getMessage()));
                        return null;
                    }
                }
            }
            else
            {
                // When the required custom fields have not passed validation we still want to show that validation has
                // not happened for each of the custom field values.
                addCustomFieldValuesNotCheckedMessageSets(projectImportData, mappingResult);
            }
        }
        else
        {
            // When the required issue types have not passed validation we still want to show that validation has
            // not happened for each of the custom field values.
            addCustomFieldValuesNotCheckedMessageSets(projectImportData, mappingResult);
        }

        // We want to populate the message list of the results. This puts the message results into order and gives
        // them i18n header labels
        createValidationMessageList(mappingResult, projectImportData, i18n);

        if (mappingResult.canImport())
        {
            log.info("Project Import: No validation errors were found and the import can continue.");
        }
        else
        {
            log.info("Project Import: Validation errors were found. The import cannot continue.");
        }
        return mappingResult;
    }

    void addCustomFieldValuesNotCheckedMessageSets(final ProjectImportData projectImportData, final MappingResult mappingResult)
    {
        final Map<String, MessageSet> customFieldMessageSets = new HashMap<String, MessageSet>();
        final CustomFieldMapper customFieldMapper = projectImportData.getProjectImportMapper().getCustomFieldMapper();
        for (final String oldCustomFieldId : customFieldMapper.getRequiredOldIds())
        {
            if (!customFieldMapper.isIgnoredCustomField(oldCustomFieldId))
            {
                customFieldMessageSets.put(oldCustomFieldId, null);
            }
        }
        mappingResult.setCustomFieldValueMessageSets(customFieldMessageSets);
    }

    private void validateExistingProjectHasValidStateForImport(final BackupProject backupProject, final BackupSystemInformation backupSystemInformation, final Project existingProject, final I18nHelper i18n, final MessageSet messageSet)
    {
        final String projectKey = backupProject.getProject().getKey();

        // Verify that the project has no existing issues
        final long issueCount = issueManager.getIssueCountForProject(existingProject.getId());
        if (issueCount != 0)
        {
            messageSet.addErrorMessage(getText(i18n, "admin.error.project.import.project.contains.issues", projectKey, String.valueOf(issueCount)));
        }

        // Verify that the project has no existing versions
        final long versionCount = versionManager.getVersions(existingProject.getId()).size();
        if (versionCount != 0)
        {
            messageSet.addErrorMessage(getText(i18n, "admin.error.project.import.project.contains.versions", projectKey, String.valueOf(versionCount)));
        }

        // Verify that the project has no existing components
        final long componentCount = projectComponentManager.findAllForProject(existingProject.getId()).size();
        if (componentCount != 0)
        {
            messageSet.addErrorMessage(getText(i18n, "admin.error.project.import.project.contains.components", projectKey,
                String.valueOf(componentCount)));
        }

        // Verify that if the project has a default assignee of Unassigned that the current instance of JIRA supports unassigned issues
        if (projectHasDefaultAssigneeUnassigned(backupProject, backupSystemInformation))
        {
            // We want this instance of JIRA to allow unassigned issues.
            final boolean allowUnassigned = applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED);
            if (!allowUnassigned)
            {
                messageSet.addErrorMessage(getText(i18n, "admin.error.project.import.project.default.assignee.not.allowed",
                    backupProject.getProject().getName()));
            }
        }

    }

    void validateCustomFieldPluginVersions(final BackupProject backupProject, final Collection backupPluginVersions, final MessageSet messageSet, final I18nHelper i18n)
    {
        for (final ExternalCustomFieldConfiguration customFieldConfiguration : backupProject.getCustomFields())
        {
            String key = customFieldConfiguration.getCustomField().getTypeKey();
            // The key is the plugin container key, a colon, then the module key (e.g. com.atlassian.jira.plugin.system.customfieldtypes:textarea)
            // We just get the plugin container key, as this is where the version exists.
            final int index = key.indexOf(":");
            // This should never happen unless there was a bad plugin module installed in the backup data, if this is
            // the case then we just want to use the key as specified and let the error propagate to the user screen
            if (index != -1)
            {
                key = key.substring(0, index);
            }
            // Does this plugin exist in the current JIRA instance
            final Plugin plugin = pluginAccessor.getPlugin(key);
            if (plugin != null)
            {
                final String currentPluginVersion = plugin.getPluginInformation().getVersion();
                String backupVersion = null;
                for (final Object backupPluginVersion : backupPluginVersions)
                {
                    final PluginVersion pluginVersion = (PluginVersion) backupPluginVersion;
                    if (pluginVersion.getKey().equals(key))
                    {
                        backupVersion = pluginVersion.getVersion();
                        break;
                    }
                }
                if (!currentPluginVersion.equals(backupVersion))
                {
                    final String customFieldName = customFieldConfiguration.getCustomField().getName();
                    if (backupVersion == null)
                    {
                        messageSet.addErrorMessage(i18n.getText("admin.error.project.import.plugin.wrong.version.null.backup",
                                backupProject.getProject().getName(), customFieldName, customFieldConfiguration.getCustomField().getTypeKey(),
                                currentPluginVersion));
                    }
                    else
                    {
                        messageSet.addErrorMessage(i18n.getText("admin.error.project.import.plugin.wrong.version",
                                backupProject.getProject().getName(), customFieldName, customFieldConfiguration.getCustomField().getTypeKey(),
                                currentPluginVersion, backupVersion));
                    }
                }
            }
        }
    }

    String getText(final I18nHelper i18n, final String key, final String value1, final String value2)
    {
        return i18n.getText(key, value1, value2);
    }

    String getText(final I18nHelper i18n, final String key, final String value1)
    {
        return i18n.getText(key, value1);
    }

    ///CLOVER:OFF
    String getBuildNumber()
    {
        return buildUtilsInfo.getCurrentBuildNumber();
    }

    ///CLOVER:ON

    ///CLOVER:ON

    ///CLOVER:OFF
    String getText(final I18nHelper i18n, final String key)
    {
        return i18n.getText(key);
    }

    ///CLOVER:ON

    void validateJiraServiceContext(final JiraServiceContext jiraServiceContext)
    {
        if (jiraServiceContext == null)
        {
            throw new IllegalArgumentException("The JiraServiceContext must not be null.");
        }
    }

    ///CLOVER:OFF
    MappingResult buildMappingResult()
    {
        return new MappingResult();
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    boolean isExternalUserManagementEnabled()
    {
        return !userManager.hasWritableDirectory();
    }

    ///CLOVER:ON

    boolean userHasSysAdminPermission(final User user)
    {
        return permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }

    ProjectImportResults getInitialImportResults(final ProjectImportData projectImportData, final I18nHelper i18n, final int usersToCreate)
    {
        return new ProjectImportResultsImpl(System.currentTimeMillis(), projectImportData.getIssueEntityCount(), usersToCreate,
            projectImportData.getValidAttachmentsCount(), i18n);
    }

    private boolean pathExists(final String path, final boolean isFile)
    {
        final File file = new File(path);
        final boolean fileExists = file.exists();
        final boolean fileIsCorrectType = (isFile) ? file.isFile() : file.isDirectory();
        return fileExists && fileIsCorrectType;
    }
}
