package com.atlassian.jira.bc.imports.project;

import javax.annotation.Nullable;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.imports.project.core.BackupOverview;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.MappingResult;
import com.atlassian.jira.imports.project.core.ProjectImportData;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.taskprogress.TaskProgressInterval;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.MessageSet;

/**
 * The ProjectImportService contains methods related to performing a project import in JIRA.
 *
 * @since v3.13
 */
public interface ProjectImportService
{
    /**
     * Validates if the user has permission to start a project import and if the provided path's exist.
     *
     * @param jiraServiceContext   containing the user who the permission checks will be run against (can be null,
     *                             indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param projectImportOptions user inputed options that contains the pathToBackupXML, the fully qualified path, on the server, to the
     *                             JIRA XML backup file that will be used to provide the data for a project import, this must not be null
     *                             and must resolve to a valid JIRA XML backup file. This also contains the pathToAttachmentBackup which
     *                             is the fully qualified path, on the server, to the backed-up JIRA attachments
     *                             directory that will be used to import project data. This is an optional parameter, if a backup attachment
     *                             path is not provided this should be null. If non-null then this must resolve to a valid directory that
     */
    void validateGetBackupOverview(JiraServiceContext jiraServiceContext, ProjectImportOptions projectImportOptions);

    /**
     * Returns a BackupOverview object containing the overview of information from the backup file if the backup files
     * build number and edition match those of the running instance of JIRA.
     * <p/>
     * If the optional parameter taskProgressSink is provided, then it is used to send information about the progress of this operation.
     * This is used for the "Long Running Task" progress bar.
     * </p>
     *
     * @param jiraServiceContext   containing the user who the permission checks will be run against (can be null,
     *                             indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param projectImportOptions user inputed options that contains the pathToBackupXML, the fully qualified path, on the server, to the
     *                             JIRA XML backup file that will be used to provide the data for a project import, this must not be null
     *                             and must resolve to a valid JIRA XML backup file. This also contains the pathToAttachmentBackup which
     *                             is the fully qualified path, on the server, to the backed-up JIRA attachments
     *                             directory that will be used to import project data. This is an optional parameter, if a backup attachment
     *                             path is not provided this should be null. If non-null then this must resolve to a valid directory that
     * @param taskProgressSink     Used to provide progress feedback, can be null.
     * @return a BackupOverview object containing the overview of information from the backup file, null if the
     *         backup file is from a different edition or build number than the running instance of JIRA.
     */
    @Nullable
    BackupOverview getBackupOverview(JiraServiceContext jiraServiceContext, ProjectImportOptions projectImportOptions, TaskProgressSink taskProgressSink);

    /**
     * Returns a MessageSet which reports if the provided BackupProject meets the JIRA system requirements
     * to be imported.
     * <p>
     * This method will return errors if:
     * <ul>
     * <li>The custom fields for the selected project exist but are not at the same version as in the backup project</li>
     * <li>The backupProject has a corresponding project that exists in JIRA, BUT, the project contains issues</li>
     * <li>The backupProject has a corresponding project that exists in JIRA, BUT, the project contains versions</li>
     * <li>The backupProject has a corresponding project that exists in JIRA, BUT, the project contains components</li>
     * <li>The backupProject is configured with a default assignee of Unassigned, BUT, the JIRA instance does not allow unassigned issues</li>
     * </ul>
     * This method will return a warning if:
     * <ul>
     * <li>The selected project does not yet exist in the JIRA instance</li>
     * </ul>
     * </p>
     *
     * @param jiraServiceContext      containing the user who the permission checks will be run against (can be null,
     *                                indicating an anonymous user). The error collection will contain the same information
     *                                as the error messages in the returned MessageSet.
     * @param project                 the BackupProject we want validate
     * @param backupSystemInformation system-wide info form the backup file.
     * @return a MessageSet which contains any errors or warnings raised in trying to map required System values for this Project Import.
     */
    MessageSet validateBackupProjectImportableSystemLevel(JiraServiceContext jiraServiceContext, BackupProject project, BackupSystemInformation backupSystemInformation);

    /**
     * Validates if the user has permission to create a project import mapper and partition the input data and if the
     * provided path's and backup project exist.
     *
     * @param jiraServiceContext      containing the user who the permission checks will be run against (can be null,
     *                                indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param projectImportOptions    User options for the project import, including the pathToBackupXML, attachmentPath, and "overwriteProjectDetails" flag.
     * @param backupProject           the backup project we want to create a ProjectImportMapper for and partition the
     * @param backupSystemInformation system-wide info form the backup file.
     */
    void validateDoMapping(JiraServiceContext jiraServiceContext, ProjectImportOptions projectImportOptions, BackupProject backupProject, BackupSystemInformation backupSystemInformation);

    /**
     * Makes a pass through the provided JIRA XML backup data and creates a ProjectImportMapper and partitions the XML
     * data for the project.
     *
     * @param jiraServiceContext      containing the user who the permission checks will be run against (can be null,
     *                                indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param projectImportOptions    User options for the project import, including the pathToBackupXML, attachmentPath, and "overwriteProjectDetails" flag.
     * @param backupProject           the backup project we want to create a ProjectImportMapper for and partition the
     *                                JIRA XML data.
     * @param backupSystemInformation system-wide info form the backup file.
     * @param taskProgressInterval    Used to provide progress feedback, can be null.
     * @return a MappingResult that will contain the initial mapper and paths to the partitioned XML
     *                                files, null if there is an error processing the data.
     */
    ProjectImportData getProjectImportData(JiraServiceContext jiraServiceContext, ProjectImportOptions projectImportOptions, BackupProject backupProject, BackupSystemInformation backupSystemInformation, TaskProgressInterval taskProgressInterval);

    /**
     * Will use the initial data in the ProjectImportData to perform automappings based on the current state of JIRA
     * and then will validate those mappings. Any warnings or errors that may have been generated will be communicated
     * through the mapping result.
     *
     * NOTE: The import should not be allowed to proceed if {@link com.atlassian.jira.imports.project.core.MappingResult#canImport()}
     * is false.
     *
     * @param jiraServiceContext      containing the user who the permission checks will be run against (can be null,
     *                                indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param projectImportOptions    User options for the project import, including the pathToBackupXML, attachmentPath, and "overwriteProjectDetails" flag.
     * @param projectImportData       which holds the initial projectImportMapper and the partitioned XML file paths for the project XML data that was partitioned from the main XML backup.
     * @param backupProject           the backup project we want to create a ProjectImportMapper for and partition the
     *                                JIRA XML data.
     * @param backupSystemInformation system-wide info form the backup file.
     * @param taskProgressInterval    Used to provide progress feedback, can be null.
     * @return a MappingResult that will contain the initial mapper and paths to the partitioned XML
     *                                files, null if there is an error processing the data.
     */
    MappingResult doMapping(JiraServiceContext jiraServiceContext, ProjectImportOptions projectImportOptions, final ProjectImportData projectImportData, BackupProject backupProject, BackupSystemInformation backupSystemInformation, TaskProgressInterval taskProgressInterval);

    /**
     * Imports the passed in project using the provided, populated and validated, project import mapper.
     * <p/>
     * This method will create/update the project, versions, components, role membership, as needed and will
     * then import the issues and all their related values. This will also cause the project that is being
     * imported to be reIndexed.
     * <p/>
     * NOTE: this method does NO validation of the project import mapper. This method must only be called with a project
     * import mapper that has been returned from
     * {@link #doMapping(com.atlassian.jira.bc.JiraServiceContext, com.atlassian.jira.imports.project.core.ProjectImportOptions, com.atlassian.jira.imports.project.core.ProjectImportData, com.atlassian.jira.imports.project.core.BackupProject, com.atlassian.jira.imports.project.core.BackupSystemInformation, com.atlassian.jira.imports.project.taskprogress.TaskProgressInterval)}
     * without any errors being generated.
     *
     * @param jiraServiceContext            containing the user who the permission checks will be run against (can be null,
     *                                      indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param projectImportOptions          User options for the project import, including the pathToBackupXML, attachmentPath, and "overwriteProjectDetails" flag.
     * @param backupProject                 the backup project we want to create a ProjectImportMapper for and partition the
     *                                      JIRA XML data.
     * @param backupSystemInformation       system-wide info form the backup file.
     * @param projectImportData             contains the projectImportMapper, that has been through the automatic mapping and validation process, and the
     *                                      path, on disk, to the partitioned xml files.
     * @param taskProgressInterval          Used to provide progress feedback, can be null.
     * @return projectImportResults         contains the statistics of what was created during the import, if it was a success, and any errors that may have occurred.
     */
    ProjectImportResults doImport(JiraServiceContext jiraServiceContext, ProjectImportOptions projectImportOptions, BackupProject backupProject, BackupSystemInformation backupSystemInformation, ProjectImportData projectImportData, TaskProgressInterval taskProgressInterval);
}
