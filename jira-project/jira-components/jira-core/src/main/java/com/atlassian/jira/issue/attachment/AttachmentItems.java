package com.atlassian.jira.issue.attachment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents an ordered sequence of AttachmentItem's.
 *
 * @since v5.0
 */
public class AttachmentItems implements Iterable<AttachmentItem>
{
    @Nonnull
    private final ImmutableList<AttachmentItem> items;

    public AttachmentItems(Iterable<AttachmentItem> items)
    {
        this.items = ImmutableList.copyOf(items);
    }

    /**
     * @return a List of Attachment
     */
    public List<Attachment> attachments()
    {
        return Lists.transform(items, new AttachmentsCategoriser.AttachmentGetter());
    }

    /**
     * @return an iterator for iterating over this AttachmentItem's items
     */
    @Override
    public Iterator<AttachmentItem> iterator()
    {
        return items.iterator();
    }

    /**
     * @return true if this AttachmentItems does not contain any items
     */
    public boolean isEmpty()
    {
        return items.isEmpty();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        AttachmentItems that = (AttachmentItems) o;
        return items.equals(that.items);
    }

    @Override
    public int hashCode()
    {
        return items.hashCode();
    }

    @Override
    public String toString()
    {
        return "AttachmentItems{items=" + items + '}';
    }
}
