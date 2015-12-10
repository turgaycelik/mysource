package com.atlassian.jira.functest.framework.navigation.issue;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

/**
 * <p>Represents the image attachments gallery shown on the view issue page for an specific issue.</p>
 *
 * <p>NOTES: All the methods defined in this interface assume the tester is currently on the view issue page.</p>
 *
 * @since v4.2
 */
public interface ImageAttachmentsGallery
{
    List<ImageAttachmentItem> get();

    /**
     * Represents an attachment shown in the image attachments gallery.
     */
    class ImageAttachmentItem
    {
        private String name;

        private String size;

        public String getName()
        {
            return name;
        }

        public String getSize()
        {
            return size;
        }

        public ImageAttachmentItem(final String name, final String size)
        {
            this.name = name;
            this.size = size;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj) { return true; }

            if (!(obj instanceof ImageAttachmentItem)) { return false; }

            ImageAttachmentItem rhs = (ImageAttachmentItem) obj;

            return new EqualsBuilder().
                    append(name, rhs.name).
                    append(size, rhs.size).
                    isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder(17,31).
                    append(name).
                    append(size).
                    toHashCode();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this).
                    append("name", name).
                    append("size", size).
                    toString();
        }
    }
}