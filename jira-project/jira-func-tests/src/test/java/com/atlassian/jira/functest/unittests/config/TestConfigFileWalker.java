package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.ConfigFile;
import com.atlassian.jira.functest.config.ConfigFileWalker;
import com.atlassian.jira.webtests.util.TempDirectoryUtil;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Test for {@link com.atlassian.jira.functest.config.ConfigFileWalker}. This is more an integration test.
 *
 * @since v4.1
 */
public class TestConfigFileWalker extends TestCase
{
    public void testWalk() throws Exception
    {
        File root = normalizeFile(TempDirectoryUtil.createTempDirectory("testWalker"));
        root.deleteOnExit();

        try
        {
            File dir1 = createSubDirectory(root, "dir1");
            File svn = createSubDirectory(root, ".svn");

            File one = createFile(root, "one.xml");
            File two = createFile(dir1, "two.zip");

            createFile(root, "ignoe.doc");
            createFile(svn, "ignoreme.xml");
            File explicit = createFile(dir1, "explicit.xml");

            RecordingConfigWalker visitor = new RecordingConfigWalker();
            ConfigFileWalker walker = new ConfigFileWalker(root, visitor);
            walker.walk();
            assertEquals(asSet(one, two, explicit), visitor.files);
            assertEquals(Collections.<File>emptySet(), visitor.errors);

            walker.addFileNameExclude("explicit.xml");
            visitor.reset();
            walker.walk();
            assertEquals(asSet(one, two), visitor.files);
            assertEquals(Collections.<File>emptySet(), visitor.errors);
        }
        finally
        {
            FileUtils.deleteQuietly(root);
        }
    }

    private File createSubDirectory(File root, String name)
    {
        File subDir = normalizeFile(new File(root, name));
        assertTrue(subDir.mkdirs());
        return subDir;
    }

    private File createFile(File root, String name) throws IOException
    {
        File newFile = normalizeFile(new File(root, name));
        assertTrue(newFile.createNewFile());
        return newFile;
    }

    private File normalizeFile(File file)
    {
        try
        {
            return file.getCanonicalFile();
        }
        catch (IOException e)
        {
            return file.getAbsoluteFile();
        }
    }

    private <T> Set<T> asSet(T...elems)
    {
        return new HashSet<T>(Arrays.asList(elems));
    }

    private static class RecordingConfigWalker implements ConfigFileWalker.ConfigVisitor
    {
        private Set<File> errors = new HashSet<File>();
        private Set<File> files = new HashSet<File>();

        public void visitConfig(final ConfigFile file)
        {
            files.add(file.getFile());
        }

        public void visitConfigError(final File file, final ConfigFile.ConfigFileException e)
        {
            errors.add(file);
        }

        private void reset()
        {
            files.clear();
            errors.clear();
        }
    }
}
