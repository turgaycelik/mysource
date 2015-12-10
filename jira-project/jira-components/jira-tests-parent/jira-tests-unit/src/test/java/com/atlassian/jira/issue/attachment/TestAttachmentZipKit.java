package com.atlassian.jira.issue.attachment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import com.atlassian.jira.util.IOUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Responsible for holding unit tests for the {@link com.atlassian.jira.issue.attachment.AttachmentZipKit} class.
 */
public class TestAttachmentZipKit
{
    private static final File validZip = new File(TestAttachmentZipKit.class.getResource("/com/atlassian/jira/issue/attachment/valid.zip").getFile());
    private static final File invalidZip = new File(TestAttachmentZipKit.class.getResource("/com/atlassian/jira/issue/attachment/invalid.zip").getFile());
    private static final File emptyZip = new File(TestAttachmentZipKit.class.getResource("/com/atlassian/jira/issue/attachment/empty.zip").getFile());
    private static final String USER_LOCALES = "en_AU";

    @Test
    public void testIsZipDetectsInvalidZipFiles()
    {
        AttachmentZipKit kit = new AttachmentZipKit();
        assertFalse(kit.isZip(invalidZip));
    }

    @Test
    public void testIsZipReturnsFalseForANullFile()
    {
        AttachmentZipKit kit = new AttachmentZipKit();
        assertFalse(kit.isZip(null));
    }

    @Test
    public void testIsZipReturnsFalseForAnEmptyZipFile()
    {
        AttachmentZipKit kit = new AttachmentZipKit();
        assertFalse(kit.isZip(emptyZip));
    }

    @Test
    public void testIsZipReturnsTrueForAValidZipFile()
    {
        AttachmentZipKit kit = new AttachmentZipKit();
        assertTrue(kit.isZip(validZip));
    }

    @Test
    public void testListEntriesOnAnInvalidFileThrowsAnIOException()
    {
        AttachmentZipKit kit = new AttachmentZipKit();
        try
        {
            kit.listEntries(invalidZip, 100, AttachmentZipKit.FileCriteria.ALL);
            fail("An: " + IOException.class.getName() + "was expected.");
        }
        catch (IOException expected){}
    }

    @Test
    public void testListZipEntries() throws IOException
    {
        AttachmentZipKit kit = new AttachmentZipKit();
        final AttachmentZipKit.AttachmentZipEntries zipEntries = kit.listEntries(validZip, -23, AttachmentZipKit.FileCriteria.ALL);
        assertNotNull(zipEntries);
        final List<AttachmentZipKit.AttachmentZipEntry> list = zipEntries.getList();
        assertEquals(29, list.size());

        // check the first few entries
        AttachmentZipKit.AttachmentZipEntry entry1 = list.get(0);
        assertEquals(entry1.getName(), "temp.c");
        assertEquals(entry1.isDirectory(), false);
        assertEquals(entry1.getSize(), 1);
        assertEquals(entry1.getDirectoryDepth(), 0);
        assertEquals(entry1.getEntryIndex(), 0);
        assertEquals(entry1.getExtension(), ".c");

        AttachmentZipKit.AttachmentZipEntry entry2 = list.get(1);
        assertEquals(entry2.getName(), ".config/compiz/compizconfig/config");
        assertEquals(entry2.isDirectory(), false);
        assertEquals(entry2.getSize(), 56);
        assertEquals(entry2.getDirectoryDepth(), 3);
        assertEquals(entry2.getEntryIndex(), 1);
        assertEquals(entry2.getExtension(), "");

        AttachmentZipKit.AttachmentZipEntry last = list.get(28);
        assertEquals(last.getName(), ".config/Trolltech.conf");
        assertEquals(last.isDirectory(), false);
        assertEquals(last.getSize(), 7688);
        assertEquals(last.getDirectoryDepth(), 1);
        assertEquals(last.getEntryIndex(), 28);
        assertEquals(last.getExtension(), ".conf");

        assertFalse(zipEntries.isMoreAvailable());
    }

    @Test
    public void testZipEntriesWithAMax() throws IOException
    {
        AttachmentZipKit kit = new AttachmentZipKit();
        AttachmentZipKit.AttachmentZipEntries zipEntries = kit.listEntries(validZip, 2, AttachmentZipKit.FileCriteria.ALL);
        assertNotNull(zipEntries);
        assertEquals(2, zipEntries.getList().size());
        assertTrue(zipEntries.isMoreAvailable());

        zipEntries = kit.listEntries(validZip, 0, AttachmentZipKit.FileCriteria.ALL);
        assertNotNull(zipEntries);
        assertEquals(0, zipEntries.getList().size());
        assertTrue(zipEntries.isMoreAvailable());
    }

    @Test(expected = IOException.class)
    public void testExtractInvalidFile() throws IOException
    {
        AttachmentZipKit kit = new AttachmentZipKit();
        kit.extractFile(invalidZip, 2);
    }

    @Test
    public void testExtractFile() throws IOException
    {
        // temp.c
        assertZipEntry("\n", 0);

        // .config/user-dirs.locale
        assertZipEntry(USER_LOCALES, 26);
    }

    @Test
    public void testInvalidIndexes() throws IOException
    {
        AttachmentZipKit kit = new AttachmentZipKit();
        InputStream in = kit.extractFile(validZip, 99);
        assertNull(in);

        in = kit.extractFile(validZip, -1);
        assertNull(in);
    }

    private void assertZipEntry(final String content, final int entryIndex) throws IOException
    {
        InputStream in = null;
        try
        {
            AttachmentZipKit kit = new AttachmentZipKit();
            in = kit.extractFile(validZip, entryIndex);

            StringWriter sw = new StringWriter();
            IOUtil.copy(in, sw);

            assertEquals(content, sw.toString());
        }
        finally
        {
            IOUtil.shutdownStream(in);
        }
    }
}
