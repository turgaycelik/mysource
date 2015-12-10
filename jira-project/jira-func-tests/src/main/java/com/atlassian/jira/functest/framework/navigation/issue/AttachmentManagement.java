package com.atlassian.jira.functest.framework.navigation.issue;

import java.io.InputStream;

/**
 * Represents the View Issue --> Manage Attachments page.
 *
 * NOTES: The methods defined here assume that the current page is the manage attachments page.
 *
 * @since v4.1
 */
public interface AttachmentManagement
{
    /**
     * Deletes all the attachments shown in the manage attachments page for an specific issue.
     */
    void delete();

    /**
     * Downloads the attachment with the given id and title.  Only call if you know the attachment is a plain text
     * attachment, otherwise you may get encoding exceptions
     */
    String downloadAttachmentAsString(long id, String title);
}
