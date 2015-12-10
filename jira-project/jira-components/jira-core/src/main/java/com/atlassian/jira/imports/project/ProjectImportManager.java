package com.atlassian.jira.imports.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.imports.project.core.BackupOverview;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.MappingResult;
import com.atlassian.jira.imports.project.core.ProjectImportData;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.handler.AbortImportException;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.UserMapper;
import com.atlassian.jira.imports.project.taskprogress.TaskProgressInterval;
import com.atlassian.jira.imports.project.taskprogress.TaskProgressProcessor;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.I18nHelper;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Performs project import tasks. This manager does not do any permission checks and throws exceptions if it is
 * given invalid parameters.
 *
 * @since v3.13
 */
public interface ProjectImportManager
{
    /**
     * Parses through the provided JIRA XML backup file and creates a BackupOverview.
     * <p>
     * If the optional parameter taskProgressSink is provided, then it is used to send information about the progress of this operation.
     * This is used for the "Long Running Task" progress bar.
     * </p>
     *
     * @param pathToBackupXml  must be a valid path, on disk, to a valid JIRA XML backup file.
     * @param taskProgressSink Used to provide progress feedback, can be null.
     * @param i18n             used to internationalize the error messages
     * @return a BackupOverview populated with the information gleaned from the JIRA XML backup.
     * @throws java.io.IOException      if there is a problem reading/finding the file
     * @throws org.xml.sax.SAXException if something goes wrong with processing the XML
     */
    BackupOverview getBackupOverview(String pathToBackupXml, TaskProgressSink taskProgressSink, I18nHelper i18n) throws IOException, SAXException;

    /**
     * Parses through the provided JIRA XML backup file and creates a MappingResult. The result will
     * contain an initialProjectImportMapper and the paths to the partitioned XML files (created for the provided
     * BackupProject).
     *
     * @param projectImportOptions    User options for the project import, including the pathToBackupXML, attachmentPath, and "overwriteProjectDetails" flag.
     * @param backupProject           is the selected project that is used to inform creation of a mapper and a subset of XML data files.
     * @param backupSystemInformation system-wide info from the backup file.
     * @param taskProgressProcessor   Used to provide progress feedback, can be null.
     * @return ProjectImportData which holds the initial projectImportMapper and the partitioned XML file paths for the project XML data that was partitioned from the main XML backup.
     * @throws IOException  if something goes wrong handling the backup or partitioned XML files
     * @throws SAXException if something goes wrong with processing the XML
     */
    ProjectImportData getProjectImportData(ProjectImportOptions projectImportOptions, BackupProject backupProject, BackupSystemInformation backupSystemInformation, TaskProgressProcessor taskProgressProcessor) throws IOException, SAXException;

    /**
     * Parses the partitioned custom field value XML and gets the custom fields to validate that they can handle the values.
     * Sets the map of custom field MessageSets in the MappingResult.
     * This will also use the custom field options validator to validate the values of the custom fields that use the
     * built in options.
     *
     * @param projectImportData which holds the initial projectImportMapper and the partitioned XML file paths for the project XML data that was partitioned from the main XML backup.
     * @param mappingResult    this was the result containing the path to the partitioned XML files and the ProjectImportMapper
     * @param backupProject    the selected project to import
     * @param taskProgressProcessor Used to provide progress feedback, can be null.
     * @param i18nBean         used to internationalize the error messages
     * @throws IOException  if something goes wrong handling the partitioned XML files
     * @throws SAXException if something goes wrong with processing the XML
     */
    void validateCustomFieldValues(ProjectImportData projectImportData, MappingResult mappingResult, BackupProject backupProject, TaskProgressProcessor taskProgressProcessor, I18nHelper i18nBean) throws IOException, SAXException;

    /**
     * Parses the partitioned file attachment XML and validates that the referenced file attachment exists in the
     * user provided attachment directory.
     * <p/>
     * If any files do not exist a warning will be added to the file attachment message set in the mapping result.
     * <p/>
     * NOTE: this validation only happens if {@link com.atlassian.jira.imports.project.core.ProjectImportOptions#getAttachmentPath()}
     * is non-null meaning that the user wants to restore attachments.
     *
     * @param projectImportOptions    User options for the project import, including the pathToBackupXML, attachmentPath, and "overwriteProjectDetails" flag.
     * @param projectImportData       which holds the initial projectImportMapper and the partitioned XML file paths for the project XML data that was partitioned from the main XML backup.
     * @param mappingResult           this was the result containing the path to the partitioned XML files and the ProjectImportMapper
     * @param backupProject           the selected project to import
     * @param backupSystemInformation system-wide info from the backup file.
     * @param taskProgressProcessor   Used to provide progress feedback, can be null.
     * @param i18n                    used to internationalize the error messages
     * @throws IOException  if something goes wrong handling the partitioned XML files
     * @throws SAXException if something goes wrong with processing the XML
     */
    void validateFileAttachments(ProjectImportOptions projectImportOptions, ProjectImportData projectImportData, MappingResult mappingResult, BackupProject backupProject, BackupSystemInformation backupSystemInformation, TaskProgressProcessor taskProgressProcessor, I18nHelper i18n) throws IOException, SAXException;

    /**
     * Performs an automatic mapping of the IssueTypes from the backup system to the current systems issue types and
     * validates that all mappings are possible. Any errors or warninigs will be reported through
     * {@link com.atlassian.jira.imports.project.core.MappingResult#getIssueTypeMessageSet()}.
     *
     * @param projectImportData which holds the initial projectImportMapper and the partitioned XML file paths for the project XML data that was partitioned from the main XML backup.
     * @param mappingResult    this was the result containing the path to the partitioned XML files,the ProjectImportMapper, and the MessageSets
     * @param backupProject    the selected project to import
     * @param i18nBean         used to internationalize the error messages
     */
    void autoMapAndValidateIssueTypes(ProjectImportData projectImportData, MappingResult mappingResult, BackupProject backupProject, I18nHelper i18nBean);

    /**
     * Performs an automatic mapping of the custom fields from the backup system to the current systems custom field's and
     * validates that all mappings are possible. Any errors or warnings will be reported through the
     * {@link com.atlassian.jira.imports.project.core.MappingResult#getCustomFieldMessageSet()}.
     *
     * @param projectImportData which holds the initial projectImportMapper and the partitioned XML file paths for the project XML data that was partitioned from the main XML backup.
     * @param mappingResult    this was the result containing the path to the partitioned XML files,the ProjectImportMapper, and the MessageSets
     * @param backupProject    the selected project to import
     * @param i18nBean         used to internationalize the error messages
     */
    void autoMapAndValidateCustomFields(ProjectImportData projectImportData, MappingResult mappingResult, BackupProject backupProject, I18nHelper i18nBean);

    /**
     * Performs an automatic mapping of the custom field options from the backup system to the current systems values.
     * This should only ever happen if the custom fields have been mapped and validated.
     *
     * @param projectImportData which holds the initial projectImportMapper and the partitioned XML file paths for the project XML data that was partitioned from the main XML backup.
     * @param backupProject    the selected project to import
     */
    void autoMapCustomFieldOptions(ProjectImportData projectImportData, BackupProject backupProject);

    /**
     * Performs an automatic mapping of the project roles from the backup system to the current systems values.
     *
     * @param projectImportData which holds the initial projectImportMapper and the partitioned XML file paths for the project XML data that was partitioned from the main XML backup.
     */
    void autoMapProjectRoles(ProjectImportData projectImportData);

    /**
     * Performs an automatic mapping of the system fields from the backup system to the current systems system
     * field values The fields that are mapped are:
     * <ul>
     * <li>Priority</li>
     * <li>Resolution</li>
     * <li>Status</li>
     * <li>IssueLinkTypes</li>
     * <li>Projects</li>
     * <li>IssueSecurityLevels</li>
     * </ul>
     *
     * @param projectImportData which holds the initial projectImportMapper and the partitioned XML file paths for the project XML data that was partitioned from the main XML backup.
     * @param backupProject    the selected project to import
     */
    void autoMapSystemFields(ProjectImportData projectImportData, BackupProject backupProject);

    /**
     * Validates that all mappings are possible. Any errors or warnings will be reported through the field's MessageSet
     * on the MappingResult. The fields that are validated are:
     * <ul>
     * <li>Priority</li>
     * <li>Resolution</li>
     * <li>Status</li>
     * <li>Project Roles</li>
     * <li>Project Roles Membership</li>
     * <li>Groups</li>
     * <li>IssueLinkTypes</li>
     * <li>Users</li>
     * </ul>
     *
     * @param projectImportData which holds the initial projectImportMapper and the partitioned XML file paths for the project XML data that was partitioned from the main XML backup.
     * @param mappingResult    this was the result containing the path to the partitioned XML files,the ProjectImportMapper, and the MessageSets
     * @param projectImportOptions Contains user inputted options related to the project import.
     * @param backupProject    the selected project to import
     * @param taskProgressInterval Used to provide progress feedback, can be null.
     * @param i18nBean         used to internationalize the error messages
     */
    void validateSystemFields(ProjectImportData projectImportData, MappingResult mappingResult, ProjectImportOptions projectImportOptions, BackupProject backupProject, final TaskProgressInterval taskProgressInterval, I18nHelper i18nBean);

    /**
     * This will create or update a project and all its versions, components, and project role membership.
     * <p/>
     * If the project does not exist then it will be created with default schemes and the details from the ExternalProject from
     * the backup file.
     * <p/>
     * If the project exists then the details will be updated to match that of the ExternalProject.
     * <p/>
     * This will also have the side-effect of populating the {@link com.atlassian.jira.imports.project.mapper.ProjectImportMapper#getProjectMapper()}
     * and the {@link com.atlassian.jira.imports.project.mapper.ProjectImportMapper#getVersionMapper()} and
     * {@link com.atlassian.jira.imports.project.mapper.ProjectImportMapper#getComponentMapper()}.
     *
     * @param projectImportOptions Contains user inputted options related to the project import.
     * @param projectImportMapper  contains the project, version, and component mappers. If the external projects id is
     *                             not mapped then this method will try to create the project.
     * @param backupProject        contains the project details used to clobber an existing project or as the details for the new project
     * @param projectImportResults used to collect the statistics of what was created during the import, if it was a success, and any errors that may have occurred.
     * @param taskProgressInterval used to show task progress to the user while this long running task is happening.
     * @throws AbortImportException if the import is aborted due to errors.
     */
    void importProject(ProjectImportOptions projectImportOptions, ProjectImportMapper projectImportMapper, BackupProject backupProject, ProjectImportResults projectImportResults, TaskProgressInterval taskProgressInterval) throws AbortImportException;

    /**
     * This will perform the project import based on the mappings provided. Calling this method will create a projects
     * issues and all issue related data.
     *
     * @param projectImportOptions    User options for the project import, including the pathToBackupXML, attachmentPath, and "overwriteProjectDetails" flag.
     * @param projectImportData       contains a projectImportMapper that has been through the automatic mapping and validation process and
     *                                the paths, on disk, to the partitioned xml files.
     * @param backupProject           contains the import project details.
     * @param backupSystemInformation system-wide info from the backup file.
     * @param projectImportResults    used to collect the statistics of what was created during the import, if it was a success, and any errors that may have occurred.
     * @param taskProgressInterval    used to show task progress to the user while this long running task is happening.
     * @param i18n                    used to internationalize the error messages
     * @param importAuthor            the user who is performing the project import, this is used as the author of the change item marker for created issues @throws IOException  if something goes wrong handling the partitioned XML files @throws SAXException if something goes wrong with processing the XML @throws IOException  if something goes wrong handling the backup or partitioned XML files
     * @throws SAXException if something goes wrong with processing the XML
     * @throws IndexException if there is a problem re-indexing JIRA.
     * @throws java.io.IOException IOException
     */
    void doImport(ProjectImportOptions projectImportOptions, ProjectImportData projectImportData, BackupProject backupProject, BackupSystemInformation backupSystemInformation, ProjectImportResults projectImportResults, TaskProgressInterval taskProgressInterval, I18nHelper i18n, User importAuthor) throws IOException, SAXException, IndexException;

    /**
     * Creates all the Missing users that we have details for.
     * This
     * @param userMapper The UserMapper which is used to determine missing users.
     * @param projectImportResults used to collect the statistics of what was created during the import, if it was a success, and any errors that may have occurred.
     * @param taskProgressInterval used to show task progress to the user while this long running task is happening.
     * @throws AbortImportException if the import is aborted due to too many errors.
     */
    void createMissingUsers(UserMapper userMapper, final ProjectImportResults projectImportResults, TaskProgressInterval taskProgressInterval) throws AbortImportException;
}
