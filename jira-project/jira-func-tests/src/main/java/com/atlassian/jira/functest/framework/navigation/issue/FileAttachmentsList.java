package com.atlassian.jira.functest.framework.navigation.issue;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collections;
import java.util.List;

/**
 * <p>Represents the file attachments list on the view issue page.</p>
 *
 * <p>NOTES:  All methods described in this interface assume that the current page is the view issue page. Therefore,
 * you should make sure that the current page is the view issue page <em>before</em> calling any of these methods.</p>
 *
 * @since v4.2
 */
public interface FileAttachmentsList
{

    /**
     * Retrieves the list of all the attachments displayed on the file attachments list. Each attachment in the list is
     * represented by a {@link com.atlassian.jira.functest.framework.navigation.issue.FileAttachmentsList.FileAttachmentItem}
     * object.
     *
     * @return
     *
     * <p>A list of all the {@link com.atlassian.jira.functest.framework.navigation.issue.FileAttachmentsList.FileAttachmentItem}
     * on the file attachments list of the view issue page.</p>
     *
     * <p>It returns an empty collection if there are no attachments on the page.</p>
     */
    public List<FileAttachmentItem> get();


    /**
     * Holds a collection of factory methods to create the items in a {@link FileAttachmentsList}
     */
    class Items
    {
        public static final Ordering<ZipFileAttachmentEntry> ZIP_ENTRY_DEFUALT_ORDERING = Ordering.from(String.CASE_INSENSITIVE_ORDER)
                .onResultOf(new Function<ZipFileAttachmentEntry, String>()
                {
                    @Override
                    public String apply(ZipFileAttachmentEntry input)
                    {
                        return input.getName();
                    }
                });

        public static ZipFileAttachmentEntry zipEntry(final String name, final String size)
        {
            return new ZipFileAttachmentEntry(name, size);
        }

        public static FileAttachmentItem zip(final String name, final String size, final String author,
                final String date, final List<ZipFileAttachmentEntry> zipFileEntries)
        {
            return zip(0, name, size, author, date, zipFileEntries);
        }

        public static FileAttachmentItem zip(final long id, final String name, final String size, final String author,
                final String date, final List<ZipFileAttachmentEntry> zipFileEntries)
        {
            return new FileAttachmentItem(id, name, size, author, date, zipFileEntries);
        }

        public static FileAttachmentItem file(final String name, final String size, final String author, final String date)
        {
            return file(0, name, size, author, date);
        }

        public static FileAttachmentItem file(final long id, final String name, final String size, final String author, final String date)
        {
            return new FileAttachmentItem(id, name, size, author, date, Collections.<ZipFileAttachmentEntry>emptyList());
        }
    }

    /**
     * Represents a file attachment in the file attachments list on the view issue page.
     */
    class FileAttachmentItem
    {
        private long id;

        private String name;

        private String size;

        private String author;

        private String date;

        private List<ZipFileAttachmentEntry> zipEntries;

        FileAttachmentItem(final long id, final String name, final String size, final String author, final String date, final List<ZipFileAttachmentEntry> zipEntries)
        {
            this.id = id;
            this.name = name;
            this.size = size;
            this.author = author;
            this.date = date;
            this.zipEntries = zipEntries;
        }

        public long getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public String getSize()
        {
            return size;
        }

        public String getAuthor()
        {
            return author;
        }

        public String getDate()
        {
            return date;
        }

        public List<ZipFileAttachmentEntry> getZipEntries()
        {
            return Collections.unmodifiableList(zipEntries);
        }

        /**
         * In some tests we don't care about the order of the entries. This method provides a way
         * to predictively sort zip entries in this attachment item, so that we can make predictive
         * comparisons regardless of the initial order of the entries.
         *
         * @param order ordering of the entries
         * @return this attachment item with changed state d'oh!
         */
        public FileAttachmentItem sortZipEntries(Ordering<ZipFileAttachmentEntry> order)
        {
            if (zipEntries != null)
            {
                this.zipEntries = order.immutableSortedCopy(zipEntries);
            }
            return this;
        }

        /**
         * Determines whether this file attachment is a zip file.
         * @return true if this file attachment is a zip file; otherwise, false.
         */
        public boolean isZip()
        {
            return !zipEntries.isEmpty();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj) { return true; }

            if (!(obj instanceof FileAttachmentItem)) { return false; }

            FileAttachmentItem rhs = (FileAttachmentItem) obj;

            return new EqualsBuilder().
                    append(date, rhs.date).
                    append(name, rhs.name).
                    append(size, rhs.size).
                    append(author, rhs.author).
                    append(zipEntries, rhs.zipEntries).
                    isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder(17, 31).
                    append(date).
                    append(name).
                    append(size).
                    append(author).
                    append(zipEntries).
                    toHashCode();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this).
                    append("name", name).
                    append("size", size).
                    append("author", author).
                    append("date", date).
                    append("zipEntries", zipEntries).
                    toString();
        }
    }

    /**
     * Represents an entry in a zip file attachment in the file attachments list on the view issue page..
     */
    class ZipFileAttachmentEntry
    {
        private String name;

        private String size;

        private ZipFileAttachmentEntry(final String name, final String size)
        {
            this.name = name;
            this.size = size;
        }

        public String getName()
        {
            return name;
        }

        public String getSize()
        {
            return size;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj) { return true; }

            if (!(obj instanceof ZipFileAttachmentEntry)) { return false; }

            ZipFileAttachmentEntry rhs = (ZipFileAttachmentEntry) obj;

            return new EqualsBuilder().
                    append(getName(), rhs.getName()).
                    append(getSize(), rhs.getSize()).
                    isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder(17, 31).
                    append(name).
                    append(size).
                    toHashCode();
        }

        @Override
        public String toString()
        {
            return "[name=" +  name + ",size=" + size + "]";
        }
    }
}