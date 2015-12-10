package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.ConfigFile;
import com.atlassian.jira.webtests.util.TempDirectoryUtil;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.util.NodeComparator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Test for {@link com.atlassian.jira.functest.config.ConfigFile}.
 *
 * @since v4.1
 */
public class TestConfigFile extends TestCase
{
    public void testBadBackupLocation() throws Exception
    {
        File badDirectory = null;
        File badFile = null;

        try
        {
            badDirectory = TempDirectoryUtil.createTempDirectory("badBackup");
            badDirectory.deleteOnExit();

            badFile = File.createTempFile("testBadBackup", "xml");
            badFile.deleteOnExit();

            try
            {
                ConfigFile.create(new File("iWouldBeReallyUnluckyIfThisFileExisted"));
                fail("A file that does not exist should not work.");
            }
            catch (ConfigFile.ConfigFileException expected)
            {
                //expected.
            }

            try
            {
                ConfigFile.create(badDirectory);
                fail("A directory should not work.");
            }
            catch (ConfigFile.ConfigFileException expected)
            {
                //expected.
            }

            //Should fail because of bad extension.
            try
            {
                ConfigFile.create(badFile);
                fail("Read only file should not work.");
            }
            catch (ConfigFile.ConfigFileException expected)
            {
                //expected.
            }
        }
        finally
        {
            deleteSafely(badFile);
            deleteSafely(badDirectory);
        }
    }

    public void testXmlReadFileGood() throws Exception
    {
        withTempFile("testXmlFile1", ".xml", new Callback<File>()
        {
            public void call(final File argument) throws Exception
            {
                assertXml(argument);
            }
        });

        withTempFile("testXmlFile2", ".XML", new Callback<File>()
        {
            public void call(final File argument) throws Exception
            {
                assertXml(argument);
            }
        });

        withTempFile("testXmlFile2", ".XmL", new Callback<File>()
        {
            public void call(final File argument) throws Exception
            {
                assertXml(argument);
            }
        });
    }

    public void testXmlReadFileBad() throws Exception
    {
        withTempFile("testZipReadBad", ".XML", new Callback<File>()
        {
            public void call(final File argument) throws Exception
            {
                FileOutputStream fos = new FileOutputStream(argument);
                try
                {
                    fos.write("randomCrap".getBytes("UTF-8"));
                }
                finally
                {
                    IOUtils.closeQuietly(fos);
                }

                try
                {
                    ConfigFile configFile = ConfigFile.create(argument);
                    configFile.readConfig();
                    fail("Should fail with parsing error.");
                }
                catch (ConfigFile.ConfigFileException expected)
                {
                    //expected.
                }
            }
        });
    }

    public void testZipReadGood() throws Exception
    {
        withTempFile("testZipFile1", ".zip", new Callback<File>()
        {
            public void call(final File argument) throws Exception
            {
                assertZip(argument);
            }
        });

        withTempFile("testZipFile2", ".ZIP", new Callback<File>()
        {
            public void call(final File argument) throws Exception
            {
                assertZip(argument);
            }
        });

        withTempFile("testZipFile2", ".ZiP", new Callback<File>()
        {
            public void call(final File argument) throws Exception
            {
                assertZip(argument);
            }
        });
    }

    public void testZipReadBad() throws Exception
    {
        withTempFile("testZipReadbad", ".zip", new Callback<File>()
        {
            public void call(final File argument) throws Exception
            {
                withZip(argument, new Callback<OutputStream>()
                {
                    public void call(final OutputStream argument) throws Exception
                    {
                        argument.write("randomBytes".getBytes("UTF-8"));
                    }
                });

                try
                {
                    ConfigFile configFile = ConfigFile.create(argument);
                    configFile.readConfig();
                    fail("Should fail with parsing error.");
                }
                catch (ConfigFile.ConfigFileException expected)
                {
                    //expected.
                }
            }
        });

        withTempFile("testZipReadBad2", ".zip", new Callback<File>()
        {
            public void call(final File argument) throws Exception
            {
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(argument));
                try
                {
                    Document document = createDummyDocument();

                    String badName = FilenameUtils.getBaseName(argument.getName()) + ".doc";
                    zos.putNextEntry(new ZipEntry(badName));
                    writeXmlToFile(argument, document);

                    zos.close();

                    try
                    {
                        ConfigFile configFile = ConfigFile.create(argument);
                        configFile.readConfig();
                        fail("Should fail with parsing error.");
                    }
                    catch (ConfigFile.ConfigFileException expected)
                    {
                        //expected.
                    }
                }
                finally
                {
                    IOUtils.closeQuietly(zos);
                }
            }
        });
    }

    public void testWriteXml() throws Exception
    {
        withTempFile("testWrite", ".xml", new Callback<File>()
        {
            public void call(final File argument) throws Exception
            {
                final Document document = createDummyDocument();
                ConfigFile file = ConfigFile.create(argument);
                file.writeFile(document);

                assertDocumentsEqual(document, readXmlFromFile(argument));
            }
        });

        withTempFile("testWriteComment", ".xml", new Callback<File>()
        {
            public void call(final File argument) throws Exception
            {
                DocumentFactory factory = DocumentFactory.getInstance();
                Document document = factory.createDocument();
                document.addComment("This is a comment");
                document.addElement("someElement");

                ConfigFile file = ConfigFile.create(argument);
                file.writeFile(document);

                assertFileContents(argument, "<!--This is a comment-->\n<someElement/>");
            }
        });
    }

    public void testWriteZip() throws Exception
    {
        withTempFile("testWriteZip", ".zip", new Callback<File>()
        {
            public void call(final File argument) throws Exception
            {
                final Document document = createDummyDocument();
                ConfigFile file = ConfigFile.create(argument);
                file.writeFile(document);

                ZipInputStream zis = new ZipInputStream(new FileInputStream(argument));
                try
                {
                    ZipEntry zipEntry = zis.getNextEntry();
                    assertNotNull(zipEntry);
                    String expectedName = FilenameUtils.getBaseName(argument.getName()) + ".xml";
                    assertEquals(expectedName, zipEntry.getName());

                    assertDocumentsEqual(document, readXmlFromStream(zis));
                }
                finally
                {
                    IOUtils.closeQuietly(zis);
                }

            }
        });
    }

    private void withTempFile(String prefix, String suffix, Callback<File> callback) throws Exception
    {
        File badFile = File.createTempFile(prefix, suffix);
        badFile.deleteOnExit();
        try
        {
            callback.call(badFile);
        }
        finally
        {
            deleteSafely(badFile);
        }
    }

    private void withZip(File file, Callback<OutputStream> callback) throws Exception
    {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
        try
        {
            String entryName = FilenameUtils.getBaseName(file.getName()) + ".xml";
            zos.setLevel(9);
            zos.putNextEntry(new ZipEntry(entryName));

            callback.call(zos);
        }
        finally
        {
            IOUtils.closeQuietly(zos);
        }
    }

    private void assertZip(final File zipFile) throws Exception
    {
        final Document document = createDummyDocument();

        withZip(zipFile, new Callback<OutputStream>()
        {
            public void call(final OutputStream argument) throws Exception
            {
                writeXmlToStream(argument, document);
            }
        });

        ConfigFile configFile = ConfigFile.create(zipFile);
        Document readDocument = configFile.readConfig();

        assertDocumentsEqual(document, readDocument);
    }

    private void assertDocumentsEqual(final Document expected, final Document actual)
    {
        NodeComparator comparator = new NodeComparator();
        assertTrue(comparator.compare(actual, expected) == 0);
    }

    private void assertFileContents(File file, String expected) throws Exception
    {
        String actual = FileUtils.readFileToString(file, "UTF-8");
        assertTrue(actual.contains(expected));
    }

    private void assertXml(final File testFile) throws IOException
    {
        Document document = createDummyDocument();

        writeXmlToFile(testFile, document);

        ConfigFile configFile = ConfigFile.create(testFile);
        Document readDocument = configFile.readConfig();

        assertDocumentsEqual(document, readDocument);
    }

    private void writeXmlToFile(File file, Document document) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(file);
        try
        {
            writeXmlToStream(fos, document);
        }
        finally
        {
            IOUtils.closeQuietly(fos);
        }
    }

    private void writeXmlToStream(OutputStream os, Document document) throws IOException
    {
        Writer writer = new OutputStreamWriter(new DontCloseOutputStream(os), "UTF-8");
        try
        {
            writeToWriter(writer, document);
        }
        finally
        {
            IOUtils.closeQuietly(writer);
        }
    }

    private void writeToWriter(Writer writer, Document document) throws IOException
    {
        XMLWriter xmlWriter = new XMLWriter(writer, OutputFormat.createPrettyPrint());
        xmlWriter.write(document);
        xmlWriter.flush();
    }

    private Document readXmlFromFile(File file) throws Exception
    {
        FileInputStream fis = new FileInputStream(file);
        try
        {
            return readXmlFromStream(fis);
        }
        finally
        {
            IOUtils.closeQuietly(fis);
        }
    }

    private Document readXmlFromStream(InputStream stream) throws Exception
    {
        Reader reader = new InputStreamReader(new DontCloseInputStream(stream), "UTF-8");
        try
        {
            SAXReader docReader = new SAXReader();
            return docReader.read(reader);
        }
        finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    private void deleteSafely(File file)
    {
        if (file != null)
        {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    private Document createDummyDocument()
    {
        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element element = document.addElement("stuff");
        element.addAttribute("test", "true");

        return document;
    }

    private static class DontCloseOutputStream extends OutputStream
    {
        private final OutputStream delegate;

        private DontCloseOutputStream(final OutputStream delegate)
        {
            this.delegate = delegate;
        }

        public void write(final int b) throws IOException
        {
            delegate.write(b);
        }

        public void write(final byte[] b) throws IOException
        {
            delegate.write(b);
        }

        public void write(final byte[] b, final int off, final int len) throws IOException
        {
            delegate.write(b, off, len);
        }

        public void flush() throws IOException
        {
            delegate.flush();
        }

        public void close()
        {
        }
    }

    private static class DontCloseInputStream extends InputStream
    {
        private final InputStream delegate;

        private DontCloseInputStream(final InputStream delegate)
        {
            this.delegate = delegate;
        }

        public int read() throws IOException
        {
            return delegate.read();
        }

        public int read(final byte[] b) throws IOException
        {
            return delegate.read(b);
        }

        public int read(final byte[] b, final int off, final int len) throws IOException
        {
            return delegate.read(b, off, len);
        }

        public long skip(final long n) throws IOException
        {
            return delegate.skip(n);
        }

        public int available() throws IOException
        {
            return delegate.available();
        }

        public void close() throws IOException { }

        public void mark(final int readlimit)
        {
            delegate.mark(readlimit);
        }

        public void reset() throws IOException
        {
            delegate.reset();
        }

        public boolean markSupported()
        {
            return delegate.markSupported();
        }
    }

    private interface Callback<T>
    {
        void call(T argument) throws Exception;
    }
}