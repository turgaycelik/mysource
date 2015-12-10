package com.atlassian.jira.util.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.atlassian.jira.bc.dataimport.DefaultExportService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class TestJiraFileInputStream
{
    private static final byte[] DATA = {0x00, 0x11, 0x22, 0x33, 0x44, 0x55};

    @Test
    public void testSkipByteOrderMarks() throws IOException
    {
        for (byte[] bombyte : JiraFileInputStream.BOMBYTES)
        {
            assertByteOrderMarkSkiped(bombyte);
        }
    }

    @Test
    public void testStreamSizeCorrect() throws Exception
    {
        int currentSize = 10000;
        RepeatByteStream stream = new RepeatByteStream(currentSize, (byte) 0x67);
        assertSize(currentSize, createXmlBackup(stream));

        currentSize = 10684;
        stream.setCount(currentSize);
        assertSize(currentSize, createNewZipWithData(stream));

        currentSize = 20000;
        stream.setCount(currentSize);
        assertSize(currentSize, createOldZipWithData(stream));
    }

    private void assertSize(long size, File file) throws IOException
    {
        final JiraFileInputStream stream = new JiraFileInputStream(file);
        try
        {
            assertEquals(size, stream.getSize());
        }
        finally
        {
            IOUtils.closeQuietly(stream);
            FileUtils.deleteQuietly(file);
        }
    }

    private void assertByteOrderMarkSkiped(byte[] bom) throws IOException
    {
        byte[] data = new byte[bom.length + DATA.length];
        System.arraycopy(bom, 0, data, 0, bom.length);
        System.arraycopy(DATA, 0, data, bom.length, DATA.length);
        ByteArrayInputStream bis = new ByteArrayInputStream(data);

        bis.mark(data.length);
        assertByteOrderMarkSkiped(createXmlBackup(bis));
        bis.reset();
        assertByteOrderMarkSkiped(createOldZipWithData(bis));
        bis.reset();
        assertByteOrderMarkSkiped(createNewZipWithData(bis));
    }

    private void assertByteOrderMarkSkiped(File file) throws IOException
    {
        final byte[] actualData = new byte[DATA.length];
        final JiraFileInputStream stream = new JiraFileInputStream(file);
        try
        {
            assertEquals(actualData.length, stream.read(actualData));
            assertArrayEquals(DATA, actualData);
            assertTrue(stream.read() == -1);
        }
        finally
        {
            IOUtils.closeQuietly(stream);
            FileUtils.deleteQuietly(file);
        }
    }

    private File createXmlBackup(InputStream stream) throws IOException
    {
        File file = createTemp("data");
        FileOutputStream fos = new FileOutputStream(file);
        try
        {
            IOUtils.copy(stream, fos);
        }
        finally
        {
            fos.close();
        }

        return file;
    }

    private File createOldZipWithData(InputStream stream) throws IOException
    {
        File file = createTemp("zip");
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));

        try
        {
            ZipEntry entryName = new ZipEntry(getBaseName(file.getName()) + ".xml");
            zos.putNextEntry(entryName);
            IOUtils.copy(stream, zos);
        }
        finally
        {
            zos.close();
        }

        return file;
    }

    private File createNewZipWithData(InputStream stream) throws IOException
    {
        File file = createTemp("zip");
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));

        try
        {
            ZipEntry entryName = new ZipEntry(DefaultExportService.ENTITIES_XML);
            zos.putNextEntry(entryName);
            IOUtils.copy(stream, zos);

            zos.putNextEntry(new ZipEntry(DefaultExportService.ACTIVEOBJECTS_XML));
            zos.write("something".getBytes());
        }
        finally
        {
            zos.close();
        }

        return file;
    }

    private File createTemp(final String extension) throws IOException
    {
        File file = File.createTempFile("TestJiraFileInputStream", "." + extension);
        assertEquals(extension, getExtension(file.getName()));
        file.deleteOnExit();
        return file;
    }

    private static class RepeatByteStream extends InputStream
    {
        private final byte data;
        private int count;

        private RepeatByteStream(int count, byte data)
        {
            this.count = count;
            this.data = data;
        }

        @Override
        public int read() throws IOException
        {
            if (count > 0)
            {
                count = count - 1;
                return data;
            }
            else
            {
                return -1;
            }
        }

        public void setCount(int count)
        {
            this.count = count;
        }
    }
}
