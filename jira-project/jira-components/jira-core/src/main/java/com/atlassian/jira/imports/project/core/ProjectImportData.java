package com.atlassian.jira.imports.project.core;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.util.ProjectImportTemporaryFiles;

/**
 * Stores project specific data that is used to make the import happen. Provides the project import mapper
 * and the paths, on disk, to the partitioned XML files that contain the backup projects data.
 *
 * @since v3.13
 */
public interface ProjectImportData
{
    /**
     * The path to the partitioned XML file for the projects custom field values.
     *
     * @return path to the partitioned XML file for the projects custom field values.
     */
    String getPathToCustomFieldValuesXml();

    /**
     * @return The count of the number of custom field values that are stored in the custom field values XML file.
     */
    int getCustomFieldValuesEntityCount();

    /**
     * The path to the partitioned XML file for the projects issue related entities.
     *
     * @return path to the partitioned XML file for the projects issue related entities.
     */
    String getPathToIssueRelatedEntitiesXml();

    /**
     * @return The count of the number of issue related values that are stored in the issue related XML file.
     */
    int getIssueRelatedEntityCount();

    /**
     * The path to the partitioned XML file for the projects issue entities.
     *
     * @return path to the partitioned XML file for the projects issue entities.
     */
    String getPathToIssuesXml();

    /**
     * @return The count of the number of issues that are stored in the issue XML file.
     */
    int getIssueEntityCount();

    /**
     * The path to the partitioned XML file for the projects issue file attachment entities.
     *
     * @return path to the partitioned XML file for the projects issue file attachment entities. This can be null
     * if the user has chosen not to specify an attachment directory.
     */
    String getPathToFileAttachmentXml();

    /**
     * @return The count of the number of file attachment values that are stored in the attachment XML file.
     */
    int getFileAttachmentEntityCount();

    /**
     * The central object used to map all the data the backup project contains.
     *
     * @return object used to map all the data the backup project contains.
     */
    ProjectImportMapper getProjectImportMapper();

    /**
     * Sets the count of the number of valid attachments the import will try to create.
     * @param validAttachmentCount the count of the number of valid attachments the import will try to create.
     */
    void setValidAttachmentsCount(int validAttachmentCount);

    /**
     * Gets the count of the number of valid attachments the import will try to create.
     * @return the count of the number of valid attachments the import will try to create.
     */
    int getValidAttachmentsCount();

    /**
     * The path to the partitioned XML file for the projects change item entities.
     *
     * @return path to the partitioned XML file for the projects change item entities.
     */
    String getPathToChangeItemXml();

    /**
     * @return The count of the number of change item values that are stored in the change item XML file.
     */
    int getChangeItemEntityCount();

    /**
     * Returns the ProjectImportTemporaryFiles containing the path to the Project Import's temporary directory and all the partitioned XML files.
     *
     * @return the ProjectImportTemporaryFiles containing the path to the Project Import's temporary directory and all the partitioned XML files.
     */
    ProjectImportTemporaryFiles getTemporaryFiles();
}
