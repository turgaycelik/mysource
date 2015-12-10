package com.atlassian.jira.issue.managers;

import com.atlassian.jira.issue.Issue;
import com.atlassian.util.concurrent.Promise;

/**
 * Manager for deleting attachments for a given issue including any thumbnails for those
 * attachments.
 *
 * @since v6.3
 */
public interface IssueAttachmentDeleteHelper
{
    /**
     * Deletes attachments for the specified issue, including any thumbnails.
     * @param issue The issue for which to delete attachments.
     * @return A promise that contains a RemoveException if there is an error deleting attachments.
     */
    Promise<Void> deleteAttachmentsForIssue(Issue issue);
}
