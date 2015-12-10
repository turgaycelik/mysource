package com.atlassian.jira.issue.comments;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

/**
 * Compares {@link Comment}s based on date created.
 */
public class CommentComparator implements Comparator<Comment>, Serializable
{
    static final long serialVersionUID = -9195692612948860152L;

    /**
     * Comparator for sorting comments based on creation date.
     */
    public static final Comparator<Comment> COMPARATOR = new CommentComparator();

    /**
     * Don't use this.
     */
    private CommentComparator()
    {
    // intentionally blank
    }

    public int compare(final Comment o1, final Comment o2)
    {
        if ((o1 == null) && (o2 == null))
        {
            return 0;
        }
        if (o1 == null)
        {
            return 1;
        }
        if (o2 == null)
        {
            return -1;
        }
        final Comment c1 = o1;
        final Comment c2 = o2;
        final Date date1 = c1.getCreated();
        final Date date2 = c2.getCreated();

        if ((date1 == null) && (date2 == null))
        {
            return 0;
        }
        if (date1 == null)
        {
            return 1;
        }
        if (date2 == null)
        {
            return -1;
        }
        return date1.compareTo(date2);
    }
}
