package com.atlassian.jira.bean.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.dataimport.ExportService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.task.TaskProgressSink;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAutoExportImpl
{
    private static final String FIXTURE_NAME = TestAutoExportImpl.class.getName();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public RuleChain initMockitoMocks = MockitoMocksInContainer.forTest(this);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private FilenameGenerator fixedFilenameGenerator;

    @Mock
    @AvailableInContainer
    private IndexPathManager indexPathManager;

    @Mock
    @AvailableInContainer
    private JiraHome jiraHome;

    @Mock
    @AvailableInContainer
    private ApplicationProperties applicationProperties;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    @AvailableInContainer
    private ExportService exportService;

    private AutoExportImpl autoExport;

    private File homeDir;


    @Before
    public void setUp() throws Exception
    {
        homeDir = temporaryFolder.newFolder(FIXTURE_NAME);

        when(jiraHome.getHome()).thenReturn(homeDir);

        when(fixedFilenameGenerator.generate(anyString())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                return new File((String) invocationOnMock.getArguments()[0], AutoExport.BASE_FILE_NAME + "fixed-filename" + ".zip");
            }
        });

        autoExport = new AutoExportImpl(jiraHome.getHome().getAbsolutePath(), fixedFilenameGenerator);
    }

    @Test
    public void nullDirectoryShoulBeInvalid()
    {
        assertFalse(autoExport.isValidDirectory(null));
    }

    @Test
    public void emptyDirectoryNameShouldBeInvalid()
    {
        assertFalse(autoExport.isValidDirectory(""));
    }

    @Test
    public void nonExistingDirectoryShouldBeInvalid()
    {
        assertFalse(autoExport.isValidDirectory(new File(jiraHome.getHome(), "doesNotExists123876").getAbsolutePath()));
    }

    @Test
    public void fileNamesOfExistingFilesShouldBeInvalid() throws IOException
    {
        final File f = File.createTempFile("jira_autoexport_testfile", "", jiraHome.getHome());

        assertTrue(f.exists());
        assertFalse(autoExport.isValidDirectory(f.getAbsolutePath()));
    }

    @Test
    public void testIsValidDirectory() throws IOException
    {
        assertTrue(autoExport.isValidDirectory(jiraHome.getHome().getAbsolutePath()));
    }

    @Test
    public void testGetExportFilePathNoDirectory() throws Exception
    {
        expectedException.expect(FileNotFoundException.class);
        expectedException.expectMessage("Could not find suitable directory for export");

        autoExport = new AutoExportImpl("/default/directory/which/doesnt/exist");
        when(jiraHome.getExportDirectory()).thenReturn(new File("/also/non/existent"));
        autoExport.getExportFilePath();

    }

    @Test
    public void testGetExportFilePathFileExists() throws Exception
    {
        final File file = new File(fixedFilenameGenerator.generate(jiraHome.getHome().getAbsolutePath()).getAbsolutePath());
        assertTrue(file.createNewFile());

        expectedException.expect(FileExistsException.class);
        expectedException.expectMessage("File with file name '" + file.getAbsolutePath() + "' already exists");

        when(jiraHome.getExportDirectory()).thenReturn(homeDir);

        autoExport.getExportFilePath();
    }

    @Test
    public void testGetExportFileTempDir() throws IOException, FileExistsException
    {
        when(jiraHome.getExportDirectory()).thenReturn(homeDir);

        final String expectedPath = fixedFilenameGenerator.generate(jiraHome.getHome().getAbsolutePath()).getAbsolutePath();
        final String path = autoExport.getExportFilePath();
        Assert.assertEquals(expectedPath, path);
    }

    @Test
    public void testGetExportFileIndexDir() throws IOException, FileExistsException
    {
        when(jiraHome.getExportDirectory()).thenReturn(new File("/non/existent/dir"));
        when(indexPathManager.getIndexRootPath()).thenReturn(homeDir.getAbsolutePath());

        final String expectedPath = fixedFilenameGenerator.generate(homeDir.getAbsolutePath()).getAbsolutePath();
        final String path = autoExport.getExportFilePath();
        Assert.assertEquals(expectedPath, path);
    }

    @Test
    public void testGetExportFileBackupDir() throws IOException, FileExistsException
    {
        when(jiraHome.getExportDirectory()).thenReturn(new File("/non/existent/dir"));
        when(indexPathManager.getIndexRootPath()).thenReturn("/again/not/there");
        when(applicationProperties.getString(APKeys.JIRA_PATH_BACKUP)).thenReturn(homeDir.getAbsolutePath());

        final String expectedPath = fixedFilenameGenerator.generate(homeDir.getAbsolutePath()).getAbsolutePath();
        final String path = autoExport.getExportFilePath();
        Assert.assertEquals(expectedPath, path);
    }

    @Test
    public void testExportDataReturnsFileNameOfExportedData() throws Exception
    {
        final String expectedFilePath = fixedFilenameGenerator.generate(homeDir.getAbsolutePath()).getAbsolutePath();

        final User user = mock(User.class);
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(user);

        final ServiceOutcome<Void> serviceOutcome = mock(ServiceOutcome.class);
        when(serviceOutcome.isValid()).thenReturn(true);
        when(exportService.export(Mockito.eq(user), Mockito.eq(expectedFilePath), Mockito.any(TaskProgressSink.class))).thenReturn(serviceOutcome);

        when(jiraHome.getExportDirectory()).thenReturn(homeDir);

        final String filePath = autoExport.exportData();
        Assert.assertEquals(expectedFilePath, filePath);
    }
}


