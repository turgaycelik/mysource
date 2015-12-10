package com.atlassian.jira.imports.project.util;

import java.io.File;

import com.atlassian.jira.util.TempDirectoryUtil;

/**
 * Mock implementation of ProjectImportTemporaryFiles, which doesn't actually create
 * @since v3.13
 */
public class MockProjectImportTemporaryFiles implements ProjectImportTemporaryFiles
{
    private String projectKey;

    public MockProjectImportTemporaryFiles(String projectKey)
    {
        this.projectKey = projectKey;
    }

    public File getParentDirectory()
    {
        return new File(TempDirectoryUtil.getSystemTempDir(), "ProjectImport" + projectKey + "0000");
    }

    public File getIssuesXmlFile()
    {
        return new File(getParentDirectory(), "Issues.xml");
    }

    public File getCustomFieldValuesXmlFile()
    {
        return new File(getParentDirectory(), "CustomFields.xml");
    }

    public File getIssueRelatedEntitiesXmlFile()
    {
        return new File(getParentDirectory(), "IssueRelatedEntities.xml");
    }

    public File getChangeItemEntitiesXmlFile()
    {
        return new File(getParentDirectory(), "ChangeItemEntities.xml");
    }

    public File getFileAttachmentEntitiesXmlFile()
    {
        return new File(getParentDirectory(), "FileAttachments.xml");
    }

    public void deleteTempFiles()
    {
        // nothing to do.
    }
}
