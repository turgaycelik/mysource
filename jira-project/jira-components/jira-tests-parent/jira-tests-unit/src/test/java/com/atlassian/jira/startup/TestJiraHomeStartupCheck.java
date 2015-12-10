package com.atlassian.jira.startup;

import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.help.HelpUrl;
import com.atlassian.jira.help.StaticHelpUrls;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.util.TempDirectoryUtil;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings ( { "ResultOfMethodCallIgnored" })
public class TestJiraHomeStartupCheck
{
    @Rule
    public RuleChain container = MockitoMocksInContainer.forTest(this);

    private JiraHomeStartupCheck startupCheck;

    @After
    public void tearDown()
    {
        if (startupCheck != null)
        {
            startupCheck.stop();
        }
    }

    @Test
    public void testName() throws Exception
    {
        assertEquals("Jira.Home Startup Check", new JiraHomeStartupCheck((new MockJiraHomePathLocator(""))).getName());
    }

    @Test
    public void testIsOk() throws Exception
    {
        final File file = TempDirectoryUtil.createTempDirectory("TestJiraHomeStartupCheck.isOk");
        try
        {

            startupCheck = new JiraHomeStartupCheck(new MockJiraHomePathLocator(file));

            assertTrue(startupCheck.isOk());
            assertNull(startupCheck.getFaultDescription());
            assertNull(startupCheck.getHTMLFaultDescription());
            assertEquals("Jira.Home Startup Check", startupCheck.getName());
            assertEquals(file.getAbsolutePath(), startupCheck.getJiraHomeDirectory().getAbsolutePath());
        }
        finally
        {
            file.delete();
        }
    }

    @Test
    public void testDirectoryDoesntExistAndIsCreatedOk() throws Exception
    {
        final File file = TempDirectoryUtil.createTempDirectory("TestJiraHomeStartupCheck.isOk");
        file.delete();
        try
        {

            startupCheck = new JiraHomeStartupCheck(new MockJiraHomePathLocator(file));

            assertTrue(startupCheck.isOk());
            assertNull(startupCheck.getFaultDescription());
            assertNull(startupCheck.getHTMLFaultDescription());
            assertEquals("Jira.Home Startup Check", startupCheck.getName());
            assertEquals(file.getAbsolutePath(), startupCheck.getJiraHomeDirectory().getAbsolutePath());

            // check that all of the expected directories get created
            for (String subdir : JiraHome.localsubdirs)
            {
                assertTrue(new File(file, subdir).exists());
            }
            for (String subdir : JiraHome.sharedsubdirs)
            {
                assertTrue(new File(file, subdir).exists());
            }
        }
        finally
        {
            file.delete();
        }
    }

    @Test
    public void testIsLocked() throws Exception
    {
        final File file = TempDirectoryUtil.createTempDirectory("TestJiraHomeStartupCheck.isOk");
        try
        {

            final JiraHomeStartupCheck startupCheck = new JiraHomeStartupCheck(new MockJiraHomePathLocator(file));

            assertTrue(startupCheck.isOk());

            final JiraHomeStartupCheck startupCheckShouldFailLocked = new JiraHomeStartupCheck(new MockJiraHomePathLocator(file));
            assertFalse(startupCheckShouldFailLocked.isOk());

            String s = String.format("The jira.home directory '%s' is already locked by another running instance of JIRA.", file.getCanonicalPath());
            assertEquals(s, startupCheckShouldFailLocked.getFaultDescription());

            s = String.format("The jira.home directory &#39;%s&#39; is already locked by another running instance of JIRA.", file.getCanonicalPath());
            assertEquals(s, startupCheckShouldFailLocked.getHTMLFaultDescription());
            assertNull(startupCheckShouldFailLocked.getJiraHomeDirectory());
        }
        finally
        {
            file.delete();
        }
    }

    @Test
    public void testIsReadonly() throws Exception
    {
        // This test relies on filesystem-specific behaviour.
        // Making a directory readonly on Windows does not mean you cannot create files within that directory.
        if (System.getProperty("os.name").contains("Windows"))
        {
            return;
        }

        final File file = TempDirectoryUtil.createTempDirectory("TestJiraHomeStartupCheck.isOk");
        try
        {
            final JiraHomeStartupCheck startupCheck = new JiraHomeStartupCheck(new MockJiraHomePathLocator(file));

            // we have to run this once normally to create all of the standard jirahome subdirectories
            startupCheck.createLocalHomeDirectories(file);
            startupCheck.createSharedHomeDirectories(file);

            // then we can set jirahome read only and see the lock failed error
            file.setReadOnly();
            assertFalse(startupCheck.isOk());
            final String expectedMessage = expectedMessage("Unable to create and acquire lock file for jira.home directory '", file.getCanonicalFile(), "'.");
            assertEquals(expectedMessage, startupCheck.getFaultDescription());
            assertEquals(expectedMessage, startupCheck.getHTMLFaultDescription());
            assertNull(startupCheck.getJiraHomeDirectory());
        }
        finally
        {
            file.delete();
        }
    }

    @Test
    public void testIsNull() throws Exception
    {
        final JiraHomeStartupCheck startupCheck = new JiraHomeStartupCheck(new MockJiraHomePathLocator((File) null));

        assertFalse(startupCheck.isOk());
        assertTrue(startupCheck.getFaultDescription().startsWith("No jira.home is configured."));
        assertTrue(startupCheck.getHTMLFaultDescription().startsWith("No jira.home is configured."));
        assertNull(startupCheck.getJiraHomeDirectory());
    }

    @Test
    public void testIsBlank() throws Exception
    {
        final JiraHomeStartupCheck startupCheck = new JiraHomeStartupCheck(new MockJiraHomePathLocator(""));

        assertFalse(startupCheck.isOk());
        assertTrue(startupCheck.getFaultDescription().startsWith("No jira.home is configured."));
        assertTrue(startupCheck.getHTMLFaultDescription().startsWith("No jira.home is configured."));
        assertNull(startupCheck.getJiraHomeDirectory());
    }

    @Test
    public void testFileIsNotDirectory() throws Exception
    {
        final File file = File.createTempFile(this.getClass().getSimpleName(), "testFileIsNotDirectory");
        try
        {
            startupCheck = new JiraHomeStartupCheck(new MockJiraHomePathLocator(file));

            assertFalse(startupCheck.isOk());
            final String expectedMessage = expectedMessage("Configured jira.home '", file, "' is not a directory.");
            assertEquals(expectedMessage, startupCheck.getFaultDescription());
            assertEquals(expectedMessage, startupCheck.getHTMLFaultDescription());
            assertNull(startupCheck.getJiraHomeDirectory());
        }
        finally
        {
            file.delete();
        }
    }

    @Test
    public void testCannotCreateDirectory() throws Exception
    {
        // This test relies on filesystem-specific behaviour.
        // Making a directory readonly on Windows does not mean you cannot create files within that directory.
        if (System.getProperty("os.name").contains("Windows"))
        {
            return;
        }

        final File file = TempDirectoryUtil.createTempDirectory("TestJiraHomeStartupCheck.testCannotCreateDirectory");
        file.setReadOnly();
        try
        {
            final String path = file.getAbsolutePath() + java.io.File.separator + "readonly";
            startupCheck = new JiraHomeStartupCheck(new MockJiraHomePathLocator(path));

            assertFalse(startupCheck.isOk());
            final HelpUrl jiraHomeHelp = StaticHelpUrls.getInstance().getUrl("jirahome");
            final String expectedMessage = "Could not create jira.home directory '" + path + "'. Please see "
                    + jiraHomeHelp.getUrl() + " for more information on how to set up your JIRA home directory.";
            assertEquals(expectedMessage, startupCheck.getFaultDescription());
            final String expectedHtmlMessage = String.format("Could not create jira.home directory &#39;%s&#39;. "
                    + "Please see <a href=\"%s\">%s</a> for more information on how to set up your JIRA home directory.",
                    path, jiraHomeHelp.getUrl(), jiraHomeHelp.getTitle());
            assertEquals(expectedHtmlMessage, startupCheck.getHTMLFaultDescription());
            assertNull(startupCheck.getJiraHomeDirectory());
        }
        finally
        {
            file.delete();
        }
    }

    private String expectedMessage(final String prefix, final File jiraHomeDirectory, final String suffix)
    {
        return prefix + jiraHomeDirectory.getAbsolutePath() + suffix;
    }

    private static class MockJiraHomePathLocator implements JiraHomePathLocator
    {
        private final String path;

        private MockJiraHomePathLocator(String filePath)
        {
            path = filePath;
        }

        private MockJiraHomePathLocator(File filePath)
        {
            path = filePath == null ? null : filePath.getAbsolutePath();
        }


        public String getJiraHome()
        {
            return path;
        }

        public String getDisplayName()
        {
            return "";
        }
    }
}
