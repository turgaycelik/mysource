package com.atlassian.jira.issue.attachment;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

/**
 * Wraps important information required to retrieve an attachment from an attachment store. Unfortunately the
 * AttachmentStore.AttachmentAdapter doesn't contain information about the project or issue.
 *
 * @since v6.3
 */
public final class AttachmentKey
{
    private final Long attachmentId;
    private final String attachmentFilename;
    private final String issueKey;
    private final String projectKey;

    AttachmentKey(@Nonnull final String projectKey, @Nonnull final String issueKey, @Nonnull final Long attachmentId, @Nonnull final String attachmentFilename)
    {
        this.attachmentId = Preconditions.checkNotNull(attachmentId);
        this.issueKey = Preconditions.checkNotNull(issueKey);
        this.projectKey = Preconditions.checkNotNull(projectKey);
        this.attachmentFilename = Preconditions.checkNotNull(attachmentFilename);
    }

    public Long getAttachmentId()
    {
        return attachmentId;
    }

    public String getAttachmentFilename()
    {
        return attachmentFilename;
    }

    public String getIssueKey()
    {
        return issueKey;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final AttachmentKey that = (AttachmentKey) o;

        return attachmentFilename.equals(that.attachmentFilename) &&
                attachmentId.equals(that.attachmentId) &&
                issueKey.equals(that.issueKey) &&
                projectKey.equals(that.projectKey);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(super.hashCode(), attachmentId, attachmentFilename, issueKey, projectKey);
    }

    @Override
    public String toString()
    {
        return "AttachmentKey{" +
                "attachmentId=" + attachmentId +
                ", attachmentFilename='" + attachmentFilename + '\'' +
                ", issueKey='" + issueKey + '\'' +
                ", projectKey='" + projectKey + '\'' +
                '}';
    }
}
