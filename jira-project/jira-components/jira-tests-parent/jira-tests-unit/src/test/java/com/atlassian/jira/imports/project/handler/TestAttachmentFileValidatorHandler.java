package com.atlassian.jira.imports.project.handler;

import java.io.File;
import java.util.Date;

import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.core.ProjectImportOptionsImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.parser.AttachmentParser;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.google.common.collect.ImmutableList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static com.atlassian.jira.mock.Strict.strict;
import static com.atlassian.jira.util.MessageSetAssert.assert1WarningNoErrors;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v3.13
 */
public class TestAttachmentFileValidatorHandler
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testHandleNoAttachmentPath() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", null);
        final BackupSystemInformation backupSystemInformation = mock(BackupSystemInformation.class, strict());

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(
                backupProject(project), projectImportOptions, backupSystemInformation, new MockI18nBean(), null);

        attachmentFileValidatorHandler.startDocument();
        attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);

        assertNoMessages(attachmentFileValidatorHandler.getValidationResults());
    }

    @Test
    public void testHandleWrongEntity() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final String attachmentPath = "/some/path";
        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);
        final BackupSystemInformation backupSystemInformation = mock(BackupSystemInformation.class, strict());

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(
                backupProject(project), projectImportOptions, backupSystemInformation, new MockI18nBean(), null);

        attachmentFileValidatorHandler.startDocument();
        attachmentFileValidatorHandler.handleEntity("SOME_ENTITY", null);

        assertNoMessages(attachmentFileValidatorHandler.getValidationResults());
    }

    @Test
    public void testHandleNullAttachment() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final String attachmentPath = "/some/path";
        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);
        final BackupSystemInformation backupSystemInformation = mock(BackupSystemInformation.class, strict());

        final AttachmentParser mockAttachmentParser = mock(AttachmentParser.class);

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(
                backupProject(project), projectImportOptions, backupSystemInformation, new MockI18nBean(), null)
        {
            @Override
            protected AttachmentParser createAttachmentParser()
            {
                return mockAttachmentParser;
            }
        };

        attachmentFileValidatorHandler.startDocument();
        attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);
        assertNoMessages(attachmentFileValidatorHandler.getValidationResults());
    }

    @Test
    public void testHandleAttachmentNotInProject() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final String attachmentPath = "/some/path";
        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);
        final BackupSystemInformation backupSystemInformation = mock(BackupSystemInformation.class, strict());

        ExternalAttachment externalAttachment = new ExternalAttachment("1", "12", "test.txt", new Date(), "admin");
        final AttachmentParser mockAttachmentParser = mock(AttachmentParser.class);
        when(mockAttachmentParser.isUsingOriginalKeyPath(project)).thenReturn(false);
        when(mockAttachmentParser.parse(null)).thenReturn(externalAttachment);

        final BackupProject backupProject = new BackupProjectImpl(
                project,
                ImmutableList.<ExternalVersion>of(),
                ImmutableList.<ExternalComponent>of(),
                ImmutableList.<ExternalCustomFieldConfiguration>of(),
                ImmutableList.of(14L));

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(
                backupProject, projectImportOptions, backupSystemInformation, new MockI18nBean(), null)
        {
            @Override
            protected AttachmentParser createAttachmentParser()
            {
                return mockAttachmentParser;
            }
        };

        attachmentFileValidatorHandler.startDocument();
        attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);
        assertNoMessages(attachmentFileValidatorHandler.getValidationResults());
    }

    @Test
    public void testFileDoesNotExist() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final String attachmentPath = folder.newFolder().getAbsolutePath();

        createTempDir(new File(attachmentPath, project.getKey()));

        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);
        final BackupSystemInformation backupSystemInformation = mock(BackupSystemInformation.class);
        when(backupSystemInformation.getIssueKeyForId("12")).thenReturn("TST-1");

        ExternalAttachment externalAttachment = new ExternalAttachment("1", "12", "test.txt", new Date(), "admin");
        final AttachmentParser mockAttachmentParser = mock(AttachmentParser.class);
        when(mockAttachmentParser.parse(null)).thenReturn(externalAttachment);
        when(mockAttachmentParser.getAttachmentFile(externalAttachment, project, "TST-1")).thenReturn(new File("/a/path/that/will/never/exist"));

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(
                backupProject(project), projectImportOptions, backupSystemInformation, new MockI18nBean(), null)
        {
            @Override
            protected AttachmentParser createAttachmentParser()
            {
                return mockAttachmentParser;
            }
        };

        attachmentFileValidatorHandler.startDocument();
        attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);

        assert1WarningNoErrors(attachmentFileValidatorHandler.getValidationResults(),
                "The attachment 'test.txt' does not exist at '/a/path/that/will/never/exist'. It will not be imported.");
    }

    @Test
    public void testFileDoesNotExistNoMoreThan20Warnings() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final String attachmentPath = folder.newFolder().getAbsolutePath();

        createTempDir(new File(attachmentPath, project.getKey()));

        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);
        final BackupSystemInformation backupSystemInformation = mock(BackupSystemInformation.class);
        when(backupSystemInformation.getIssueKeyForId("12")).thenReturn("TST-1");

        ExternalAttachment externalAttachment = new ExternalAttachment("1", "12", "test.txt", new Date(), "admin");
        final AttachmentParser mockAttachmentParser = mock(AttachmentParser.class);
        when(mockAttachmentParser.isUsingOriginalKeyPath(project)).thenReturn(false);
        when(mockAttachmentParser.parse(null)).thenReturn(externalAttachment);
        when(mockAttachmentParser.getAttachmentFile(externalAttachment, project, "TST-1")).thenReturn(new File("/a/path/that/will/never/exist"));

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(
                backupProject(project), projectImportOptions, backupSystemInformation, new MockI18nBean(), null)
        {
            @Override
            protected AttachmentParser createAttachmentParser()
            {
                return mockAttachmentParser;
            }
        };

        // Add 20 warnings
        for (int i = 0; i < 20; i++)
        {
            attachmentFileValidatorHandler.getValidationResults().addWarningMessage(String.valueOf(i));
        }

        attachmentFileValidatorHandler.startDocument();
        attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);
        attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);

        assert1WarningNoErrors(attachmentFileValidatorHandler.getValidationResults(),
                "There are more than twenty attachment entries that do not exist in the attachment directory. See your logs for full details.");
    }

    @Test
    public void testHandleHappyPath() throws Exception
    {
        File dir = null;
        File tempFile = null;
        try
        {
            final ExternalProject project = new ExternalProject();
            project.setKey("TST");
            dir = new File(System.getProperty("java.io.tmpdir") + File.separator + "TST" + File.separator + "TST-1");
            createTempDir(dir);
            tempFile = File.createTempFile("test", ".txt", dir);
            final String attachmentPath = System.getProperty("java.io.tmpdir");
            ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);
            final BackupSystemInformation backupSystemInformation = mock(BackupSystemInformation.class);
            when(backupSystemInformation.getIssueKeyForId("12")).thenReturn("TST-1");

            ExternalAttachment externalAttachment = new ExternalAttachment("1", "12", "test.txt", new Date(), "admin");
            final AttachmentParser mockAttachmentParser = mock(AttachmentParser.class);
            when(mockAttachmentParser.isUsingOriginalKeyPath(project)).thenReturn(false);
            when(mockAttachmentParser.parse(null)).thenReturn(externalAttachment);
            when(mockAttachmentParser.getAttachmentFile(externalAttachment, project, "TST-1")).thenReturn(tempFile);

            AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(
                    backupProject(project), projectImportOptions, backupSystemInformation, new MockI18nBean(), null)
            {
                @Override
                protected AttachmentParser createAttachmentParser()
                {
                    return mockAttachmentParser;
                }
            };

            attachmentFileValidatorHandler.startDocument();
            attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);

            assertNoMessages(attachmentFileValidatorHandler.getValidationResults());
        }
        finally
        {
            deleteNicely(tempFile);
            deleteNicely(dir);
        }
    }

    @Test
    public void testProjectDirectoryDoesNotExist() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final String attachmentPath = System.getProperty("java.io.tmpdir");

        // Delete the TST directory under /tmp if it exists
        File tstDirectory = new File(attachmentPath + File.separator + "TST");
        delete(tstDirectory);

        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);

        ExternalAttachment externalAttachment = new ExternalAttachment("1", "12", "test.txt", new Date(), "admin");
        final AttachmentParser mockAttachmentParser = mock(AttachmentParser.class);
        when(mockAttachmentParser.parse(null)).thenReturn(externalAttachment);

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(
                backupProject(project), projectImportOptions, null, new MockI18nBean(), null)
        {
            @Override
            protected AttachmentParser createAttachmentParser()
            {
                return mockAttachmentParser;
            }
        };

        attachmentFileValidatorHandler.startDocument();
        attachmentFileValidatorHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, null);

        assert1WarningNoErrors(attachmentFileValidatorHandler.getValidationResults(),
                "The provided attachment path does not contain a sub-directory called 'TST'. If you proceed with the import attachments will not be included.");
    }

    @Test
    public void testProjectDirectoryDoesNotExistNoAttachments() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final String attachmentPath = System.getProperty("java.io.tmpdir");

        // Delete the TST directory under /tmp if it exists
        File tstDirectory = new File(attachmentPath + File.separator + "TST");
        delete(tstDirectory);

        ProjectImportOptions projectImportOptions = new ProjectImportOptionsImpl("/some/file", attachmentPath);

        AttachmentFileValidatorHandler attachmentFileValidatorHandler = new AttachmentFileValidatorHandler(
                backupProject(project), projectImportOptions, null, new MockI18nBean(), null);

        attachmentFileValidatorHandler.startDocument();

        assertNoMessages(attachmentFileValidatorHandler.getValidationResults());
    }


    private static BackupProject backupProject(ExternalProject project)
    {
        return new BackupProjectImpl(
                project,
                ImmutableList.<ExternalVersion>of(),
                ImmutableList.<ExternalComponent>of(),
                ImmutableList.<ExternalCustomFieldConfiguration>of(),
                ImmutableList.of(12L, 14L));
    }

    private static void createTempDir(File dir)
    {
        assertThat("Directory exists or is successfully created: " + dir, dir.exists() || dir.mkdirs(), is(true));
        dir.deleteOnExit();
    }

    private static void delete(File file)
    {
        if (file != null)
        {
            assertThat("File/Directory is missing or successfully deleted: " + file, !file.exists() || file.delete(), is(true));
        }
    }

    private static void deleteNicely(File file)
    {
        if (file != null && file.exists() && !file.delete())
        {
            System.err.println("WARNING: Wasn't able to remove temporary file: " + file);
        }
    }
}
