package com.atlassian.jira.issue.attachment;

import java.io.File;

import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @since v6.3
 */
public class TestFileSystemAttachmentDirectoryAccessor
{
    @Rule
    public InitMockitoMocks mocks = new InitMockitoMocks(this);

    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Mock
    AttachmentPathManager attachmentPathManager;

    @Mock
    ProjectManager projectManager;

    FileSystemAttachmentDirectoryAccessor directoryAccessor;

    @Mock
    JiraHome jiraHome;

    @Before
    public void setUp()
    {
        directoryAccessor = new FileSystemAttachmentDirectoryAccessor(projectManager, attachmentPathManager);
        container.addMock(JiraHome.class, jiraHome);
        when(attachmentPathManager.getAttachmentPath()).thenReturn(testFolder.getRoot().getAbsolutePath());
    }

    @Test
    public void getAttachmentDirectorShouldReturnCurrentKeyByDefault()
    {
        Project project = mock(Project.class);
        Issue issue = mock(Issue.class);
        when(issue.getKey()).thenReturn("MKY-5");
        when(issue.getProjectObject()).thenReturn(project);
        when(project.getKey()).thenReturn("MKY");
        when(projectManager.getProjectObjByKey("MKY")).thenReturn(project);
        when(project.getOriginalKey()).thenReturn("MKY");
        assertThat(directoryAccessor.getAttachmentDirectory(issue, false), equalTo(new File(testFolder.getRoot(), "MKY/MKY-5")));
        assertThat(directoryAccessor.getThumbnailDirectory(issue), equalTo(new File(testFolder.getRoot(), "MKY/MKY-5/thumbs")));
    }

    @Test
    public void getAttachmentDirectorShouldReturnCurrentKeyIfNoPriorDirectoryExists()
    {
        Project project = mock(Project.class);
        Issue issue = mock(Issue.class);
        when(issue.getKey()).thenReturn("MKY-5");
        when(issue.getProjectObject()).thenReturn(project);
        when(project.getKey()).thenReturn("MKY");
        when(project.getOriginalKey()).thenReturn("MKY");
        when(project.getId()).thenReturn(11l);

        when(projectManager.getProjectObjByKey("MKY")).thenReturn(project);
        assertThat(directoryAccessor.getAttachmentDirectory(issue, false), equalTo(new File(testFolder.getRoot(), "MKY/MKY-5")));
        assertThat(directoryAccessor.getThumbnailDirectory(issue), equalTo(new File(testFolder.getRoot(), "MKY/MKY-5/thumbs")));

    }

    @Test
    public void getAttachmentDirectorShouldReturnPreviousDirectoryIfItExists()
    {
        assertTrue(new File(testFolder.getRoot(), "OLD/OLD-5").mkdirs());

        Project project = mock(Project.class);
        Issue issue = mock(Issue.class);
        when(issue.getKey()).thenReturn("MKY-5");
        when(issue.getProjectObject()).thenReturn(project);
        when(project.getKey()).thenReturn("MKY");
        when(project.getOriginalKey()).thenReturn("OLD");
        when(project.getId()).thenReturn(11l);

        when(projectManager.getProjectObjByKey("MKY")).thenReturn(project);
        assertThat(directoryAccessor.getAttachmentDirectory(issue, false), equalTo(new File(testFolder.getRoot(), "OLD/OLD-5")));
        assertThat(directoryAccessor.getThumbnailDirectory(issue), equalTo(new File(testFolder.getRoot(), "OLD/OLD-5/thumbs")));
    }

    /**
     * We want to gather old attachments in one directory, even if it's the old one (with an old project key).
     */
    @Test
    public void getAttachmentDirectorShouldUsePreviousDirectoryIfRootExists()
    {
        assertTrue(new File(testFolder.getRoot(), "OLD").mkdirs());

        Project project = mock(Project.class);
        Issue issue = mock(Issue.class);
        when(issue.getKey()).thenReturn("MKY-5");
        when(issue.getProjectObject()).thenReturn(project);
        when(project.getKey()).thenReturn("MKY");
        when(project.getOriginalKey()).thenReturn("OLD");
        when(project.getId()).thenReturn(11l);

        when(projectManager.getProjectObjByKey("MKY")).thenReturn(project);
        assertThat(directoryAccessor.getAttachmentDirectory(issue, false), equalTo(new File(testFolder.getRoot(), "OLD/OLD-5")));
        assertThat(directoryAccessor.getThumbnailDirectory(issue), equalTo(new File(testFolder.getRoot(), "OLD/OLD-5/thumbs")));
    }


    @Test
    public void isHealthyReturnsHealthyIfRootDirectoryAndTmpDirectoryArePresent() throws Exception
    {
        TemporaryFolder tmp = new TemporaryFolder();
        when(attachmentPathManager.getAttachmentPath()).thenReturn(testFolder.getRoot().getAbsolutePath());
        when(jiraHome.getCachesDirectory()).thenReturn(tmp.newFolder());
        assertFalse(directoryAccessor.errors().isDefined());
    }

    @Test
    public void isHealthyReturnsUnhealthyIfRootDirectoryIsNotPresent() throws Exception
    {
        TemporaryFolder tmp = new TemporaryFolder();
        File invalid = new File("/tmp/jira-das-" + java.util.UUID.randomUUID().toString());
        when(attachmentPathManager.getAttachmentPath()).thenReturn(invalid.getAbsolutePath());
        when(jiraHome.getCachesDirectory()).thenReturn(tmp.newFolder());
        assertTrue(directoryAccessor.errors().isDefined());
    }

    @Test
    public void isHealthyReturnsUnhealthyIfTmpDirectoryIsNotPresent() throws Exception
    {
        TemporaryFolder tmp = new TemporaryFolder();
        File invalid = tmp.newFolder();
        invalid.setWritable(false);
        when(jiraHome.getCachesDirectory()).thenReturn(invalid);
        when(attachmentPathManager.getAttachmentPath()).thenReturn(testFolder.getRoot().getAbsolutePath());
        assertTrue(directoryAccessor.errors().isDefined());
    }
}
