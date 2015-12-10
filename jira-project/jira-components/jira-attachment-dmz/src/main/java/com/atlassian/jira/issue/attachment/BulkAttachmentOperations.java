package com.atlassian.jira.issue.attachment;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.collect.EnclosedIterable;

/**
 * Methods for handling attachments in bulk.
 *
 * @since v6.3
 */
public interface BulkAttachmentOperations
{
    /**
     * Retrieve all the attachments of an issue.
     * @param issue The issue.
     * return a class to traverse all the returned attachments.
     */
    EnclosedIterable<Attachment> getAttachmentOfIssue(@Nonnull Issue issue);

    /**
     * Retrieve all the attachments of the system.
     * return a class to traverse all the returned attachments.
     */
    EnclosedIterable<Attachment> getAllAttachments();
}
