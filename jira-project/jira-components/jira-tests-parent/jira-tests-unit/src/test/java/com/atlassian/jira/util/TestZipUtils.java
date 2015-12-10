package com.atlassian.jira.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import com.atlassian.jira.JiraTestUtil;

import com.google.common.io.ByteStreams;

import org.junit.Test;

import webwork.util.ClassLoaderUtils;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 *
 * @since v4.4
 */
public class TestZipUtils
{
    private static final URL RESOURCE = ClassLoaderUtils.getResource(JiraTestUtil.TESTS_BASE + "/util/zip-with-foo.zip", TestZipUtils.class);
    private static final URL TEST_DIR = ClassLoaderUtils.getResource(JiraTestUtil.TESTS_BASE + "/util/foo-directory", TestZipUtils.class);

    @Test(expected = IOException.class)
    public void testZipFileNotReadable() throws IOException
    {
        File zipFile = new File("ZXCVZXCVZXCVZXVCZXVZXCVZXV");
        ZipUtils.streamForZipFileEntry(zipFile, "some entry");
    }

    @Test
    public void testEntryDoesNotExist() throws IOException
    {
        String path = RESOURCE.getFile();
        File zipFile = new File(path);
        assertThat(zipFile.exists(), is(true));
        assertThat(ZipUtils.streamForZipFileEntry(zipFile, "bar"), nullValue());
    }

    @Test
    public void testExtantEntryStreamRetrieved() throws IOException
    {
        File zipFile = new File(RESOURCE.getFile());
        InputStream stream = ZipUtils.streamForZipFileEntry(zipFile, "foo");
        try
        {
            String entryContent = new String(ByteStreams.toByteArray(stream), "UTF-8");
            assertThat(entryContent, is("Tue 22 May 2012 12:35:20 EST\n"));
        }
        finally
        {
            stream.close();
        }
    }

    @Test
    public void testZipDirectory() throws IOException, URISyntaxException
    {
        File output = File.createTempFile("TestZipUtils", ".zip");
        File directory = new File(TEST_DIR.toURI());
        ZipUtils.zip(directory, output);

        assertZipEntryContent(output, "file1", "Test file 1");
        assertZipEntryContent(output, "file2", "Test file 2\n");
        assertZipEntryContent(output, "child1/file11", "Test file 1 - 1");
        assertZipEntryContent(output, "child1/child11/file111", "Test file 1 - 1 - 1");
        assertZipEntryContent(output, "child2/file11", "Test file 1 - 1");
        assertZipEntryContent(output, "child2/file21", "Test file 2 - 1\n\n");
    }

    @Test
    public void testZipDirectoryBadSource() throws IOException, URISyntaxException
    {
        File output = File.createTempFile("TestZipUtils", ".zip");
        File directory = new File(TEST_DIR.toString() + "NOOO");
        try {
            ZipUtils.zip(directory, output);
            fail();
        }
        catch (FileNotFoundException ex)
        { }
    }

    @Test
    public void testZipDirectoryBadTarget() throws IOException, URISyntaxException
    {
        File output = File.createTempFile("TestZipUtils", "");
        output.mkdir();

        File directory = new File(TEST_DIR.toString());
        try {
            ZipUtils.zip(directory, output);
            fail();
        }
        catch (IOException ex)
        { }
    }

    @Test
    public void testZipDirectoryBadTarget2() throws IOException, URISyntaxException
    {
        File output = File.createTempFile("TestZipUtils", "");
        output.createNewFile();
        output.setReadOnly();

        File directory = new File(TEST_DIR.toString());
        try {
            ZipUtils.zip(directory, output);
            fail();
        }
        catch (IOException ex)
        { }
    }

    @Test
    public void testUnZipDirectory() throws IOException, URISyntaxException
    {
        File output = File.createTempFile("TestZipUtils", ".zip");
        File directory = new File(TEST_DIR.toURI());
        ZipUtils.zip(directory, output);

        File directory2 = createTempDirectory();
        ZipUtils.unzip(output, directory2);

        assertFileContent(directory2, "file1", "Test file 1");
        assertFileContent(directory2, "file2", "Test file 2\n");
        assertFileContent(directory2, "child1/file11", "Test file 1 - 1");
        assertFileContent(directory2, "child1/child11/file111", "Test file 1 - 1 - 1");
        assertFileContent(directory2, "child2/file11", "Test file 1 - 1");
        assertFileContent(directory2, "child2/file21", "Test file 2 - 1\n\n");

    }

    private void assertFileContent(final File directory, final String fileName, final String s)
            throws IOException
    {
        File f = new File(directory, fileName);
        assertThat(f.exists(), is(true));
        InputStream stream = new FileInputStream(f);
        try
        {
            String entryContent = new String(ByteStreams.toByteArray(stream), "UTF-8");
            assertThat(entryContent, is(s));
        }
        finally
        {
            stream.close();
        }
    }

    private void assertZipEntryContent(final File zipFile, final String fileName, final String s) throws IOException
    {
        InputStream stream = ZipUtils.streamForZipFileEntry(zipFile, fileName);
        try
        {
            String entryContent = new String(ByteStreams.toByteArray(stream), "UTF-8");
            assertThat(entryContent, is(s));
        }
        finally
        {
            stream.close();
        }
    }
    private File createTempDirectory() throws IOException
    {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String name = "Test" + System.currentTimeMillis();
        File dir = new File(baseDir, name);
        dir.mkdir();
        return dir;
    }

}
