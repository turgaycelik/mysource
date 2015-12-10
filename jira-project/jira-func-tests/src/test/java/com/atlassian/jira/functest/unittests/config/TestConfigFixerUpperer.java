package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.ConfigFixerUpperer;
import com.atlassian.jira.functest.config.ConfigurationDefaults;
import com.atlassian.jira.functest.config.ZipHelper;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.TempDirectoryUtil;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.util.NodeComparator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.zip.ZipInputStream;

/**
 * Test {@link com.atlassian.jira.functest.config.ConfigFixerUpperer}. This is really an integration test.
 *
 * @since v4.1
 */
public class TestConfigFixerUpperer extends TestCase implements EnvironmentAware
{
    @Override
    protected void tearDown() throws Exception
    {
    }

    public void testFixBrokenFiles() throws Exception
    {
        final File directory = TempDirectoryUtil.createTempDirectory("testFixBrokenFiles");
        ZipHelper.extractTo(getResource("fixerBroken.zip"), directory);

        ConfigFixerUpperer upper = new ConfigFixerUpperer();
        upper.setRoot(directory).setConfigurationChecks(ConfigurationDefaults.createDefaultConfigurationChecks());
        upper.setCreateBackups(true);
        upper.setExcludes(Collections.<IOFileFilter>singletonList(new NameFileFilter("empty.xml")));
        upper.fix();

        checkResult(directory);

        //We want to leave this directory around on an error so that we can check out the problems.
        FileUtils.deleteDirectory(directory);
    }

    private void checkResult(File file) throws Exception
    {
        IOFileFilter tempFiler = FileFilterUtils.or(FileFilterUtils.suffixFileFilter(".xml"), FileFilterUtils.suffixFileFilter(".zip"));
        tempFiler = FileFilterUtils.or(FileFilterUtils.directoryFileFilter(), FileFilterUtils.makeFileOnly(tempFiler));

        visitFiles(file, tempFiler, new FileVisitor()
        {
            public void visitFile(final File file) throws Exception
            {
                checkFile(file);
            }
        });
    }

    private void visitFiles(final File file, final IOFileFilter tmpFilter, final FileVisitor visitor) throws Exception
    {
        if (file.isDirectory())
        {
            final File[] files = file.listFiles((FileFilter) tmpFilter);
            for (File f : files)
            {
                visitFiles(f, tmpFilter, visitor);
            }
        }
        else
        {
            visitor.visitFile(file);
        }
    }

    private void checkFile(final File file) throws Exception
    {
        File origFile = addExtension(file, "orig");
        File fixFile = addExtension(file, "fix");
        File backupFile = addExtension(file, "bak");

        if (fixFile.exists())
        {
            assertTrue("Did not seem to backup: '" + file + "'.", backupFile.exists());
            assertTrue(String.format("Backup file '%s' does not match '%s'.", backupFile, origFile), FileUtils.contentEquals(backupFile, origFile));

            assertXmlDom(fixFile, file);
        }
        else
        {
            assertFalse("Did backup of '" + file + "' when not expected.", backupFile.exists());
            assertTrue(String.format("File '%s' should not be changed.", file), FileUtils.contentEquals(file, origFile));
        }
    }

    private void assertXmlDom(final File expected, final File actual) throws Exception
    {
        Document expectedDoc = readFile(expected);
        Document actualDoc = readFile(actual);

        NodeComparator comparator = new NodeComparator();
        assertTrue(String.format("Expected file '%s' did not match actual file '%s'.", expected, actual),
                comparator.compare(expectedDoc, actualDoc) == 0);
    }

    private Document readFile(final File file) throws Exception
    {
        final String extension = FilenameUtils.getExtension(StringUtils.removeEnd(file.getName(), ".fix"));
        if ("XML".equalsIgnoreCase(extension))
        {
            return readRaw(file);
        }
        else if ("ZIP".equalsIgnoreCase(extension))
        {
            return readZip(file);
        }
        else
        {
            throw new IOException("Don't know how to process '" + file + "'.");
        }
    }

    private Document readRaw(final File file) throws Exception
    {
        FileInputStream fis = new FileInputStream(file);
        try
        {
            return readFromStream(fis);
        }
        finally
        {
            IOUtils.closeQuietly(fis);
        }
    }

    private Document readZip(final File file) throws Exception
    {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
        try
        {
            zis.getNextEntry();
            return readFromStream(zis);
        }
        finally
        {
            IOUtils.closeQuietly(zis);
        }
    }

    private Document readFromStream(final InputStream stream) throws Exception
    {
        SAXReader reader = new SAXReader();
        reader.setMergeAdjacentText(true);
        reader.setStripWhitespaceText(true);

        return reader.read(stream);
    }


    private File addExtension(final File file, final String extension)
    {
        return new File(file.getParent(), file.getName() + '.' + extension);
    }

    public void setEnvironmentData(final JIRAEnvironmentData environmentData)
    {
    }

    private File normalizeFile(File root)
    {
        try
        {
            root = root.getCanonicalFile();
        }
        catch (IOException e)
        {
            root = root.getAbsoluteFile();
        }
        return root;
    }

    private InputStream getResource(final String child)
    {
        return getClass().getResourceAsStream("/xml/" + child);
    }

    //This method helps create the zipfile needed by the test.

    private void main() throws Exception
    {
        final File directory = TempDirectoryUtil.createTempDirectory("makeZip");

        ZipHelper.extractTo(getResource("fixerBroken.zip"), directory);

        //Remove all the old ".orig" and ".fix" files.
        IOFileFilter tmpFilter = FileFilterUtils.or(FileFilterUtils.suffixFileFilter(".orig"), FileFilterUtils.suffixFileFilter(".fix"));
        tmpFilter = FileFilterUtils.makeFileOnly(tmpFilter);
        tmpFilter = FileFilterUtils.or(FileFilterUtils.directoryFileFilter(), tmpFilter);
        visitFiles(directory, tmpFilter, new FileVisitor()
        {
            public void visitFile(final File file) throws Exception
            {
                if (!file.delete())
                {
                    throw new IOException("Unable to delete '" + file + "'.");
                }
            }
        });

        //Make some orig copies of the files.
        visitFiles(directory, FileFilterUtils.trueFileFilter(), new FileVisitor()
        {
            public void visitFile(final File file) throws Exception
            {
                FileUtils.copyFile(file, addExtension(file, "orig"));
            }
        });

        //Fix the files.
        ConfigFixerUpperer upper = new ConfigFixerUpperer();
        upper.setRoot(directory).setConfigurationChecks(ConfigurationDefaults.createDefaultConfigurationChecks());
        upper.setCreateBackups(true);
        upper.fix();

        //A bak file indicates a change. Move the files into the correct location.
        tmpFilter = FileFilterUtils.makeFileOnly(FileFilterUtils.suffixFileFilter(".bak"));
        tmpFilter = FileFilterUtils.or(FileFilterUtils.directoryFileFilter(), tmpFilter);

        visitFiles(directory, tmpFilter, new FileVisitor()
        {
            public void visitFile(final File backFile) throws Exception
            {
                File sourceFile = new File(backFile.getParent(), FilenameUtils.getBaseName(backFile.getName()));
                File fixFile = addExtension(sourceFile, "fix");

                FileUtils.moveFile(sourceFile, fixFile);
                FileUtils.moveFile(backFile, sourceFile);
            }
        });

        System.out.printf("The directory '%s' has been created with the test content. Check the directory to make sure it is correct.", directory);
    }

    private interface FileVisitor
    {
        void visitFile(File file) throws Exception;
    }

    //You can make this public to create the zip file used by this test.
    public static void main(String[] args) throws Exception
    {
        new TestConfigFixerUpperer().main();
    }
}