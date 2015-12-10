package com.atlassian.jira.imports.project.util;

import com.atlassian.jira.util.TempDirectoryUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Single class to hold the paths to the Tempory XML partition files, and their temporary parent directory.
 *
 * @since v3.13
 */
public class ProjectImportTemporaryFilesImpl implements ProjectImportTemporaryFiles
{
    private static final Logger log = Logger.getLogger(ProjectImportTemporaryFilesImpl.class);

    private final File parentDirectory;
    private final File issuesXmlFile;
    private final File customFieldValuesXmlFile;
    private final File issueRelatedEntitiesXmlFile;
    private final File changeItemEntitiesXmlFile;
    private final File fileAttachmentEntitiesXmlFile;

    public ProjectImportTemporaryFilesImpl(final String projectKey) throws IOException
    {
        // Create the parent directory
        parentDirectory = TempDirectoryUtil.createTempDirectory("JiraProjectImport" + projectKey);
        // Create the Issues partition file.
        issuesXmlFile = new File(parentDirectory, "Issues.xml");
        issuesXmlFile.createNewFile();
        // Create the CustomFieldValues partition file.
        customFieldValuesXmlFile = new File(parentDirectory, "CustomFieldValues.xml");
        customFieldValuesXmlFile.createNewFile();
        // Create the IssueRelatedEntities partition file.
        issueRelatedEntitiesXmlFile = new File(parentDirectory, "IssueRelatedEntities.xml");
        issueRelatedEntitiesXmlFile.createNewFile();
        // Create the ChangeItemEntities partition file.
        changeItemEntitiesXmlFile = new File(parentDirectory, "ChangeItemEntities.xml");
        changeItemEntitiesXmlFile.createNewFile();
        // Create the FileAttachmentEntities partition file.
        fileAttachmentEntitiesXmlFile = new File(parentDirectory, "FileAttachmentEntities.xml");
        fileAttachmentEntitiesXmlFile.createNewFile();
    }

    public File getParentDirectory()
    {
        return parentDirectory;
    }

    public File getIssuesXmlFile()
    {
        return issuesXmlFile;
    }

    public File getCustomFieldValuesXmlFile()
    {
        return customFieldValuesXmlFile;
    }

    public File getIssueRelatedEntitiesXmlFile()
    {
        return issueRelatedEntitiesXmlFile;
    }

    public File getChangeItemEntitiesXmlFile()
    {
        return changeItemEntitiesXmlFile;
    }

    public File getFileAttachmentEntitiesXmlFile()
    {
        return fileAttachmentEntitiesXmlFile;
    }

    public void deleteTempFiles()
    {
        // First delete all the partition XML files
        deleteTemporaryFile(getChangeItemEntitiesXmlFile());
        deleteTemporaryFile(getCustomFieldValuesXmlFile());
        deleteTemporaryFile(getFileAttachmentEntitiesXmlFile());
        deleteTemporaryFile(getIssueRelatedEntitiesXmlFile());
        deleteTemporaryFile(getIssuesXmlFile());
        // finally delete the temp directory they all live in.
        deleteTemporaryFile(getParentDirectory());
    }

    private void deleteTemporaryFile(final File file)
    {
        if (!file.exists())
        {
            return;
        }
        final boolean deletedSuccessfully = file.delete();
        if (!deletedSuccessfully)
        {
            log.warn("Unable to delete temporary file '" + file.getAbsolutePath() + "'.");
        }
    }
}
