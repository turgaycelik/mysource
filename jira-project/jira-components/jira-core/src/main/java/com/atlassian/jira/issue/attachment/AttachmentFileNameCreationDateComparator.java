package com.atlassian.jira.issue.attachment;

import java.util.Comparator;
import java.util.Locale;

/**
 * <p>Compares attachments by file name first, and then creation date if the file names are the same.</p>
 *
 * <p>File name comparison is handled using a {@link java.text.Collator}}, so that the results are locale-aware.</p>
 * @see java.text.Collator
 *
 * @since v4.2
 */
public class AttachmentFileNameCreationDateComparator implements Comparator<Attachment>
{
    private final Comparator<Attachment> creationDateComparator;
    private final Comparator<Attachment> fileNameComparator;

    /**
     * Creates a new instance of this attachment comparator.
     * @param userLocale The locale to be used to determine order of the file names.
     */
    public AttachmentFileNameCreationDateComparator(final Locale userLocale)
    {
        this.fileNameComparator = new AttachmentFileNameComparator(userLocale);
        this.creationDateComparator = new AttachmentCreationDateComparator();
    }

    public int compare(final Attachment a1, final Attachment a2)
    {
        final int fileNameComparison = fileNameComparator.compare(a1, a2);
        //If the file names are the same, let's compare using the creation date
        if(fileNameComparison == 0)
        {
            final int creationDateComparison = creationDateComparator.compare(a1, a2);

            // creationDateComparator returns in ascending order (from earliest to latest),
            // we want the results from latest to earliest. Therefore, we reverse the results.
            if(creationDateComparison > 0)
            {
                return -1;
            }
            else if(creationDateComparison < 0)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        return fileNameComparison;
    }
}