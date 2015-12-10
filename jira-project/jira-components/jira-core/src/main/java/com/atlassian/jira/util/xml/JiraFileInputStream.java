package com.atlassian.jira.util.xml;

import com.atlassian.core.util.DataUtils;
import com.atlassian.jira.bc.dataimport.DefaultExportService;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.apache.commons.lang.StringUtils.stripToEmpty;

/**
 * An input streams that handles Unicode Byte-Order Mark (BOM) marker within a normal file as well as a ZIP file.
 * Distilled and adapted from http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6206835
 */
public class JiraFileInputStream extends InputStream
{
    // ------------------------------------------------------------------------------------------------------- Constants
    public final static byte[] UTF32BEBOMBYTES = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF,};
    public final static byte[] UTF32LEBOMBYTES = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00,};
    public final static byte[] UTF16BEBOMBYTES = new byte[]{(byte) 0xFE, (byte) 0xFF,};
    public final static byte[] UTF16LEBOMBYTES = new byte[]{(byte) 0xFF, (byte) 0xFE,};
    public final static byte[] UTF8BOMBYTES = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF,};
    public final static byte[][] BOMBYTES = new byte[][]{
            UTF32BEBOMBYTES,
            UTF32LEBOMBYTES,
            UTF16BEBOMBYTES,
            UTF16LEBOMBYTES,
            UTF8BOMBYTES,
    };
    public final static int NONE = -1;

    /**
     * No bom sequence is longer than 4 bytes
     */
    public final static int MAXBOMBYTES = 4;

    private final InputStream daStream;
    private final long size;

    public JiraFileInputStream(File file) throws IOException
    {
        notNull("file", file);

        int BOMType = getBOMType(file);
        int skipBytes = getSkipBytes(BOMType);
        Pair pair = getFileInputStream(file);
        InputStream fIn = pair.stream;
        if (skipBytes > 0)
        {
            long skipped = fIn.skip(skipBytes);
            if (skipped < skipBytes)
            {
                // TODO: Not all bytes were skipped, possible EOS. Should we do something about it?
            }
        }
        daStream = fIn;
        size = pair.size;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public int read() throws IOException
    {
        return daStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        return daStream.read(b, off, len);
    }

    public long getSize()
    {
        return size;
    }

    @Override
    public void close() throws IOException
    {
        daStream.close();
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods
    private static boolean zipFileHasExactlyOneEntry(ZipFile file)
    {
        Enumeration<ZipArchiveEntry> entries = file.getEntries();
        if (entries.hasMoreElements())
        {
            entries.nextElement();
            return !entries.hasMoreElements();
        }
        else
        {
            return false;
        }
    }

    private static Pair getFileInputStream(File file) throws IOException
    {
        final InputStream is;
        final long size;
        final FileInputStream fileInputStream = new FileInputStream(file);
        if (stripToEmpty(file.getName()).endsWith(DataUtils.SUFFIX_ZIP))
        {
            final ZipFile zipFile = new ZipFile(file);
            // If there is only one entry inside the zip we use it no matter what it is called.
            if (zipFileHasExactlyOneEntry(zipFile))
            {
                final ZipArchiveEntry entry = zipFile.getEntries().nextElement();
                is = getInputStreamForEntry(zipFile, file.getPath(), entry);
                size = entry.getSize();
            }
            else
            {
                // Otherwise we expect them to be using the "new" (ca 2011) naming convention.
                final ZipArchiveEntry entry = zipFile.getEntry(DefaultExportService.ENTITIES_XML);
                if (entry == null)
                {
                    try
                    {
                        zipFile.close();
                    }
                    catch (IOException ignored)
                    {
                        Logger.getLogger(JiraFileInputStream.class).warn("Unable to cleanly close '" + file.getPath() + "'.", ignored);
                    }
                    throw new IOException(String.format("Unable to find JIRA backup (%s) inside of zip file: %s", DefaultExportService.ENTITIES_XML, file));
                }
                is = getInputStreamForEntry(zipFile, file.getPath(), entry);
                size = entry.getSize();
            }
        }
        else
        {
            is = new BufferedInputStream(fileInputStream);
            size = file.length();
        }
        return new Pair(size, is);
    }

    private static InputStream getInputStreamForEntry(final ZipFile zipFile, final String path, ZipArchiveEntry entry) throws IOException
    {
        return new FilterInputStream(zipFile.getInputStream(entry))
        {
            @Override
            public void close() throws IOException
            {
                try
                {
                    in.close();
                }
                finally
                {
                    try
                    {
                        zipFile.close();
                    }
                    catch (IOException e)
                    {
                        Logger.getLogger(JiraFileInputStream.class).warn("Unable to cleanly close '" + path + "'.", e);
                    }
                }
            }
        };
    }

    private int getBOMType(File _f) throws IOException
    {
        InputStream fileInputStream = getFileInputStream(_f).stream;
        try
        {
            byte[] buff = new byte[MAXBOMBYTES];
            int read = fileInputStream.read(buff);
            fileInputStream.close();
            fileInputStream = null;
            
            return getBOMType(buff, read);
        }
        finally
        {
            IOUtils.closeQuietly(fileInputStream);
        }
    }

    private int getSkipBytes(int bomType)
    {
        if (bomType < 0 || bomType >= BOMBYTES.length) return 0;
        return BOMBYTES[bomType].length;
    }

    private int getBOMType(byte[] _bomBytes, int _length)
    {
        for (int i = 0; i < BOMBYTES.length; i++)
        {
            for (int j = 0; j < _length && j < BOMBYTES[i].length; j++)
            {
                if (_bomBytes[j] != BOMBYTES[i][j]) break;
                if (_bomBytes[j] == BOMBYTES[i][j] && j == BOMBYTES[i].length - 1) return i;
            }
        }
        return NONE;
    }

    private static class Pair
    {
        private final long size;
        private final InputStream stream;

        private Pair(long size, InputStream stream)
        {
            this.size = size;
            this.stream = stream;
        }
    }
}