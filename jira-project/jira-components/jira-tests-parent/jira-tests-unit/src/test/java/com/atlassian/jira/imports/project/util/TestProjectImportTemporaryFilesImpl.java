package com.atlassian.jira.imports.project.util;

import java.io.File;

import com.atlassian.jira.util.TempDirectoryUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestProjectImportTemporaryFilesImpl
{
    @Test
    public void testCreateTempFiles() throws Exception
    {
        ProjectImportTemporaryFilesImpl temporaryFiles = new ProjectImportTemporaryFilesImpl("TST");
        // Should have created a new folder in our temp directory
        File parentDirectory = temporaryFiles.getParentDirectory();
        assertTrue(parentDirectory.exists());
        assertTrue(parentDirectory.isDirectory());
        assertTrue(parentDirectory.getAbsolutePath().startsWith(TempDirectoryUtil.getSystemTempDir()));
        assertTrue(parentDirectory.getName().startsWith("JiraProjectImportTST"));

        File changeItemsFile = temporaryFiles.getChangeItemEntitiesXmlFile();
        assertTrue(changeItemsFile.exists());
        assertTrue(changeItemsFile.isFile());
        assertEquals(parentDirectory, changeItemsFile.getParentFile());
        assertEquals("ChangeItemEntities.xml", changeItemsFile.getName());

        File customFieldValuesXmlFile = temporaryFiles.getCustomFieldValuesXmlFile();
        assertTrue(customFieldValuesXmlFile.exists());
        assertTrue(customFieldValuesXmlFile.isFile());
        assertEquals(parentDirectory, customFieldValuesXmlFile.getParentFile());
        assertEquals("CustomFieldValues.xml", customFieldValuesXmlFile.getName());

        File fileAttachmentEntitiesXmlFile = temporaryFiles.getFileAttachmentEntitiesXmlFile();
        assertTrue(fileAttachmentEntitiesXmlFile.exists());
        assertTrue(fileAttachmentEntitiesXmlFile.isFile());
        assertEquals(parentDirectory, fileAttachmentEntitiesXmlFile.getParentFile());
        assertEquals("FileAttachmentEntities.xml", fileAttachmentEntitiesXmlFile.getName());

        File issueRelatedEntitiesXmlFile = temporaryFiles.getIssueRelatedEntitiesXmlFile();
        assertTrue(issueRelatedEntitiesXmlFile.exists());
        assertTrue(issueRelatedEntitiesXmlFile.isFile());
        assertEquals(parentDirectory, issueRelatedEntitiesXmlFile.getParentFile());
        assertEquals("IssueRelatedEntities.xml", issueRelatedEntitiesXmlFile.getName());

        File issuesXmlFile = temporaryFiles.getIssuesXmlFile();
        assertTrue(issuesXmlFile.exists());
        assertTrue(issuesXmlFile.isFile());
        assertEquals(parentDirectory, issuesXmlFile.getParentFile());
        assertEquals("Issues.xml", issuesXmlFile.getName());
        
        // Finally, we will test that the delete files works, (and this cleans up after ourselves :)
        temporaryFiles.deleteTempFiles();
        assertFalse(parentDirectory.exists());
        assertFalse(changeItemsFile.exists());
        assertFalse(customFieldValuesXmlFile.exists());
        assertFalse(fileAttachmentEntitiesXmlFile.exists());
        assertFalse(issueRelatedEntitiesXmlFile.exists());
        assertFalse(issuesXmlFile.exists());

        // Should be able to call deleteTempFiles() multiple times without exceptions.
        temporaryFiles.deleteTempFiles();
    }
}
