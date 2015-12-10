package com.atlassian.jira.imports.project.core;

/**
 * Contains user inputed options related to the project import.
 *
 * @since v3.13
 */
public interface ProjectImportOptions
{
    /**
     * Specifies the path, on disk on the server, to the backup XML data file that will contain the project data
     * that will be restored.
     *
     * @return the file path on disk to the backup XML file.
     */
    String getPathToBackupXml();

    /**
     * Specifies the path, on disk on the server, to the directory that will contain the attachments for the project data
     * that will be restored.
     *
     * @return the file path on disk to the attachments directory.
     */
    String getAttachmentPath();

    /**
     * A boolean that indicates if the user wants the existing JIRA project, which is being imported into, to have
     * its project details (name, lead, etc) over-written by the backup projects data.
     *
     * NOTE: If the project does not yet exist the importer will work as if this value is set to true, no matter what
     * the value really is.
     *
     * @return true if we want to overwrite the project details, false otherwise
     */
    boolean overwriteProjectDetails();

    /**
     * The project key that the user has chosen to import.
     *
     * @return project key that the user has chosen to import.
     */
    String getSelectedProjectKey();
}
