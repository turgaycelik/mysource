package com.atlassian.jira.issue.attachment;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * This class will help when working with zip files
 *
 * @since 4.1
 */
public class AttachmentZipKit
{
    private static final Logger log = Logger.getLogger(AttachmentZipKit.class);

    public interface AttachmentZipEntries
    {
        /**
         * @return true if there are more entries available in the zip than asked for.
         */
        public boolean isMoreAvailable();

        /**
         * @return total number of entries available (can be larger that what was asked for)
         */
        public int getTotalNumberOfEntriesAvailable();

        /**
         * @return the list of {@link com.atlassian.jira.issue.attachment.AttachmentZipKit.AttachmentZipEntry}
         */
        public List<AttachmentZipEntry> getList();
    }

    /**
     * These are the data returned about entries in a zip file
     */
    public static interface AttachmentZipEntry
    {
        /**
         * @return the entryIndex within the zip file
         */
        long getEntryIndex();

        /**
         * @return the file name of the zip entry
         */
        String getName();

        /**
         * @return the abbreviated file name
         */
        String getAbbreviatedName();

        /**
         * @return the extension of the file name of the zip entry
         */
        String getExtension();

        /**
         * @return the size of the file uncompressed
         */
        long getSize();

        /**
         * @return true if the entry is a directory instead of a file
         */
        boolean isDirectory();

        /**
         * @return the number of directories deep this entry is
         */
        int getDirectoryDepth();

        /**
         * @return The date the entry was last modified
         */
        Date getModifiedDate();

        /**
         * @return The mimetype of the entry
         */
        String getMimetype();
    }

    public static class ZipEntryInputStream extends InputStream
    {
        private final InputStream inputStream;
        private final ZipArchiveEntry zipEntry;

        private ZipEntryInputStream(InputStream inputStream, ZipArchiveEntry zipEntry)
        {
            this.inputStream = inputStream;
            this.zipEntry = zipEntry;
        }

        public ZipArchiveEntry getZipEntry()
        {
            return zipEntry;
        }


        public int read() throws IOException
        {
            return inputStream.read();
        }

        public int read(final byte[] b) throws IOException
        {
            return inputStream.read(b);
        }

        public int read(final byte[] b, final int off, final int len) throws IOException
        {
            return inputStream.read(b, off, len);
        }

        public long skip(final long n) throws IOException
        {
            return inputStream.skip(n);
        }

        public int available() throws IOException
        {
            return inputStream.available();
        }

        public void close() throws IOException
        {
            inputStream.close();
        }

        public void mark(final int readlimit)
        {
            inputStream.mark(readlimit);
        }

        public void reset() throws IOException
        {
            inputStream.reset();
        }

        public boolean markSupported()
        {
            return inputStream.markSupported();
        }

    }

    /**
     * This will return true if the file is in fact a valid zip file.
     *
     * @param file the file in play.
     * @return true if its actually a readable zip file.
     */
    public boolean isZip(File file)
    {
        if (file == null)
        {
            return false;
        }
        try
        {
            final ZipFile zipFile = new ZipFile(file);
            boolean hasAtLeastOneEntry = zipFile.getEntries().hasMoreElements();
            zipFile.close();
            return hasAtLeastOneEntry;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    /**
     * This will extract an {@link java.io.OutputStream} of the contents of the file at entryIndex within the zip file.
     * If no file can be found be at that entryIndex or the entry is a directory then null will be returned.
     *
     * @param zipFile the zip file in play
     * @param entryIndex the entryIndex of the entry to return
     * @return an {@link com.atlassian.jira.issue.attachment.AttachmentZipKit.ZipEntryInputStream} that can be read from
     *         for data and has extra {@link org.apache.commons.compress.archivers.zip.ZipArchiveEntry} information.
     * @throws IOException if things go wrong during reading
     */
    public ZipEntryInputStream extractFile(File zipFile, long entryIndex) throws IOException
    {
        if (!isZip(zipFile))
        {
            throw new IOException("This is not a zipFile" + zipFile);
        }
        int i = 0;
        ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(new FileInputStream(zipFile));
        ZipArchiveEntry entry = zipInputStream.getNextZipEntry();
        while (entry != null && i < entryIndex)
        {
            entry = zipInputStream.getNextZipEntry();
            i++;
        }
        if (entry != null && i == entryIndex)
        {
            return new ZipEntryInputStream(zipInputStream, entry);
        }
        return null;
    }

    public enum FileCriteria
    {
        ONLY_FILES
                {
                    boolean matches(final ZipArchiveEntry zipEntry)
                    {
                        return !zipEntry.isDirectory();
                    }
                },
        ONLY_DIRECTORIES
                {
                    boolean matches(final ZipArchiveEntry zipEntry)
                    {
                        return zipEntry.isDirectory();
                    }
                },
        ALL
                {
                    boolean matches(final ZipArchiveEntry zipEntry)
                    {
                        return true;
                    }
                };

        abstract boolean matches(final ZipArchiveEntry zipEntry);
    }

    /**
     * This will return a list of {@link AttachmentZipKit.AttachmentZipEntry}s for a given zip file
     *
     * @param zipFile the ZIP file in play
     * @param criteria the criteria of which entries to return
     * @param maxEntries the maximum number of entries to put in th list.  If this is -negative then not maximum is
     * used.
     * @return a list of entries
     * @throws IOException if the file cant be read or is not a zip.
     * file.
     */
    public AttachmentZipEntries listEntries(File zipFile, int maxEntries, final FileCriteria criteria)
            throws IOException
    {
        ZipFile zf = new ZipFile(zipFile);

        try
        {
            List<AttachmentZipEntry> list = new ArrayList<AttachmentZipEntry>();
            int currentEntry = 0;

            for (Enumeration<ZipArchiveEntry> enumeration = zf.getEntries(); enumeration.hasMoreElements();)
            {
                final ZipArchiveEntry zipEntry = enumeration.nextElement();
                if (criteria.matches(zipEntry))
                {
                    list.add(new AttachmentZipEntryImpl(currentEntry, zipEntry));
                }
                currentEntry++;
            }

            final int totalNumberOfEntriesAvailable = list.size();
            boolean hasMore = false;

            if (maxEntries >= 0 && list.size() > maxEntries)
            {
                hasMore = true;
                list = list.subList(0, maxEntries);
            }
            return new AttachmentZipEntriesImpl(list, totalNumberOfEntriesAvailable, hasMore);
        }
        finally
        {
            try
            {
                zf.close();
            }
            catch (IOException e)
            {
                log.error(String.format("JIRA was not able to close the zip file: '%s' while / after listing its "
                        + "entries. An IOException was thrown.", zipFile.getPath()));

            }
        }
    }

    private static class AttachmentZipEntriesImpl implements AttachmentZipEntries
    {
        private final boolean moreAvailable;
        private final int totalNumberOfEntriesAvailable;
        private final List<AttachmentZipEntry> list;

        private AttachmentZipEntriesImpl(final List<AttachmentZipEntry> list, final int totalNumberOfEntriesAvailable, final boolean moreAvailable)
        {
            this.moreAvailable = moreAvailable;
            this.totalNumberOfEntriesAvailable = totalNumberOfEntriesAvailable;
            this.list = list;
        }

        public boolean isMoreAvailable()
        {
            return moreAvailable;
        }

        public int getTotalNumberOfEntriesAvailable()
        {
            return totalNumberOfEntriesAvailable;
        }

        public List<AttachmentZipEntry> getList()
        {
            return Collections.unmodifiableList(list);
        }
    }

    private static class AttachmentZipEntryImpl implements AttachmentZipEntry
    {
        private final int entryIndex;
        private final int directoryDepth;
        private final ZipArchiveEntry zipEntry;

        public AttachmentZipEntryImpl(final int entryIndex, final ZipArchiveEntry zipEntry)
        {
            this.entryIndex = entryIndex;
            this.zipEntry = zipEntry;
            this.directoryDepth = calcDepth(zipEntry.getName());
        }

        private int calcDepth(final String name)
        {
            File f = new File(name);
            int i = 0;
            while ((f = f.getParentFile()) != null)
            {
                i++;
            }
            return i;
        }

        @Override
        public long getEntryIndex()
        {
            return entryIndex;
        }

        @Override
        public String getName()
        {
            return zipEntry.getName();
        }

        @Override
        public String getAbbreviatedName()
        {
            return new Path(getName()).abbreviate(40).getPath();
        }

        @Override
        public String getExtension()
        {
            String name = new File(getName()).getName();
            int index = name.lastIndexOf(".");
            if (index > 0)
            {
                return name.substring(index);
            }
            return "";
        }

        @Override
        public long getSize()
        {
            return zipEntry.getSize();
        }

        @Override
        public boolean isDirectory()
        {
            return zipEntry.isDirectory();
        }

        @Override
        public int getDirectoryDepth()
        {
            return directoryDepth;
        }

        @Override
        public Date getModifiedDate()
        {
            return new Date(zipEntry.getTime());
        }

        @Override
        public String getMimetype()
        {
            return MimetypesFileTypeMap.getContentType(getName());
        }

        @Override
        public String toString()
        {
            return new StringBuilder(super.toString())
                    .append("-idx:").append(getEntryIndex())
                    .append("-name:").append(getName())
                    .append("-dir:").append(isDirectory())
                    .append("-size:").append(getSize())
                    .toString();
        }
    }
}
