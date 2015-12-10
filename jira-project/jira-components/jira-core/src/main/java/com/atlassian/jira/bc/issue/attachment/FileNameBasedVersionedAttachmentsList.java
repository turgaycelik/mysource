package com.atlassian.jira.bc.issue.attachment;

import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentCreationDateComparator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This implementation wraps a list of attachments and adds the concept of attachment 'versions' by grouping
 * attachments that have the same filename as versions of the same file.
 *
 * It stores this 'version' information when constructed by building up a {@link java.util.Map} with keys for every
 * distinct file name in the underlying {@link java.util.List} of attachments. Then, each key's value is a
 * {@link java.util.TreeSet} of attachments which have that filename.
 * 
 * @since v4.2
 */
public class FileNameBasedVersionedAttachmentsList implements VersionedAttachmentsList
{
    private final List<Attachment> attachments;

    private final Map<String, TreeSet<Attachment>> fileNameGroupingMap = new HashMap<String, TreeSet<Attachment>>();

    public FileNameBasedVersionedAttachmentsList(final List<Attachment> attachments)
    {
        this.attachments = attachments;
        groupAttachmentsByFileName();
    }

    private void groupAttachmentsByFileName()
    {
        for (final Attachment attachment : attachments)
        {
            TreeSet<Attachment> namedAttachments = fileNameGroupingMap.get(attachment.getFilename());
            if (namedAttachments == null)
            {
                namedAttachments = new TreeSet<Attachment>(new AttachmentCreationDateComparator());
                fileNameGroupingMap.put(attachment.getFilename(), namedAttachments);
            }
            namedAttachments.add(attachment);
        }
    }

    public List<Attachment> asList()
    {
        return Collections.unmodifiableList(attachments);
    }

    /**
     * Determines whether the specified attachment is the latest file uploaded amongst the group of files
     * with the same name in the underlying list.
     * @param attachment The attachment in play. Should not be null.
     * @return true if this is the latest (or only) file with this name; otherwise false.
     */
    public boolean isLatestVersion(Attachment attachment)
    {
        notNull("attachment" , attachment);

        final TreeSet<Attachment> namedAttachments = fileNameGroupingMap.get(attachment.getFilename());

        if (namedAttachments != null)
        {
            return (namedAttachments.size() == 1) || (namedAttachments.last().equals(attachment));
        }
        return false;
    }
}