package com.atlassian.jira.issue.attachment;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * <p>Compares attachments based on the file's name. </p>
 *
 * <p>The comparison is handled using a {@link java.text.Collator}}, so that the results are locale-aware.</p>
 * @see java.text.Collator
 * @since v4.2
 */
public class AttachmentFileNameComparator implements Comparator<Attachment>
{
    private final Collator collator;

    /**
     * Creates a new instance of this comparator with the provided locale
     * @param userLocale The locale to be used to determine order of the file names.
     */
    public AttachmentFileNameComparator(final Locale userLocale)
    {
        this.collator = Collator.getInstance(userLocale);
    }

    public int compare(final Attachment a1, final Attachment a2)
    {
        final String filename1 = a1.getFilename();
        final String filename2 = a2.getFilename();

        if ((filename1 == null) && (filename2 == null))
        {
            return 0;
        }

        if (filename1 == null)
        {
            return -1; // non null values are greater than null values
        }

        if (filename2 == null)
        {
            return 1; // non null values are greater than null values
        }

        return collator.compare(filename1, filename2);
    }
}