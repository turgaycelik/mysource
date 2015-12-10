package com.atlassian.jira.issue.attachment;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;

/**
 * Utility methods for creating AttachmentKeys. Primarily designed to wrap the logic for determining the correct project
 * and issue keys especially if the project key has been renamed.
 *
 * @since v6.3
 */
public final class AttachmentKeys
{
    public static AttachmentKey from(final Project project, final Issue issue, final Attachment attachment)
    {
        return from(project.getOriginalKey(), issue.getKey(), attachment);
    }

    public static AttachmentKey from(final String originalProjectKey, final String issueKey, final Attachment attachment)
    {
        final String compoundKey = FileAttachments.computeIssueKeyForOriginalProjectKey(originalProjectKey, issueKey);
        return new AttachmentKey(originalProjectKey, compoundKey, attachment.getId(), attachment.getFilename());
    }

    /*
     This one goes to the database to retrieve the issue.
     */
    public static AttachmentKey from(final Attachment attachment)
    {
        return from(attachment, attachment.getIssueObject());
    }

    public static AttachmentKey from(final Attachment attachment, final Issue issue)
    {
        Project project = issue.getProjectObject();
        return from(project, issue, attachment);
    }

    private AttachmentKeys() {}
}
