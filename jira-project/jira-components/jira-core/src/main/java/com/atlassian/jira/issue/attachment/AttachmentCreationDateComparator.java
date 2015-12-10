package com.atlassian.jira.issue.attachment;

import java.sql.Timestamp;
import java.util.Comparator;

/**
 * Attachment Comparator based on creation date in ascending order
 * 
 * @since v4.2
 */
public class AttachmentCreationDateComparator implements Comparator<Attachment>
{
    public int compare(final Attachment attachment1, final Attachment attachment2)
    {
        final Timestamp created1 = attachment1.getCreated();
        final Timestamp created2 = attachment2.getCreated();

        if ((created1 == null) && (created2 == null))
        {
            return 0;
        }
        if (created1 == null)
        {
            return 1;
        }
        if (created2 == null)
        {
            return -1;
        }
        return created1.compareTo(created2);
    }
}
