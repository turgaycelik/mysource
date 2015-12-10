package com.atlassian.jira.imports.project.util;

import java.io.File;

/**
 * Single interface to hold the paths to the Temporary XML partition files, and their temporary parent directory.
 *
 * @since v3.13
 */
public interface ProjectImportTemporaryFiles
{
    /**
     * Returns the temporary directory which is the parent of all the temporary partition files.
     * @return the temporary directory which is the parent of all the temporary partition files.
     */
    File getParentDirectory();

    /**
     * Returns the temporary XML partition file for Issues.
     * @return the temporary XML partition file for Issues.
     */
    File getIssuesXmlFile();

    /**
     * Returns the temporary XML partition file for CustomFields.
     * @return the temporary XML partition file for CustomFields.
     */
    File getCustomFieldValuesXmlFile();

    /**
     * Returns the temporary XML partition file for Issue related entities.
     * @return the temporary XML partition file for Issue related entities.
     */
    File getIssueRelatedEntitiesXmlFile();

    /**
     * Returns the temporary XML partition file for ChangeItems.
     * @return the temporary XML partition file for ChangeItems.
     */
    File getChangeItemEntitiesXmlFile();

    /**
     * Returns the temporary XML partition file for FileAttachments.
     * @return the temporary XML partition file for FileAttachments.
     */
    File getFileAttachmentEntitiesXmlFile();

    /**
     * Deletes the temporary files held in this object.
     * It is safe to call this method twice as it will check if the files actually exist first.
     */
    void deleteTempFiles();
}
