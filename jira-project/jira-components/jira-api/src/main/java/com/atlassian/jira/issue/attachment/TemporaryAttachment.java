package com.atlassian.jira.issue.attachment;

import com.atlassian.annotations.PublicApi;
import com.atlassian.fugue.Either;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.File;
import java.util.Date;

/**
 * Represents a temporary attachment, that is an attachment that's been uploaded to the server
 * but not yet attached to an issue. The issueId may be null for newly created issues!
 *
 * Temporary Attachments can be sorted by created date,
 *
 * @since v4.2
 */
@PublicApi
public class TemporaryAttachment implements Comparable<TemporaryAttachment>
{
    private static final String ISSUETOKEN_FORMAT = "issuetoken-%d";
    private static final String PROJECTTOKEN_FORMAT = "projecttoken-%d";

    private final Long id;
    private final File tempAttachment;
    private final String filename;
    private final String contentType;
    private final Long issueId;
    private final Date created;
    private final String formToken;

    @Deprecated
    public TemporaryAttachment(final Long id, Long issueId, final File tempAttachment, final String filename, final String contentType)
    {
        this(id, issueId, tempAttachment, filename, contentType, getIssueToken(issueId));
    }

    public TemporaryAttachment(final Long id, final File tempAttachment, final String filename, final String contentType, final String formToken)
    {
        this(id, null, tempAttachment, filename, contentType, formToken);
    }

    private TemporaryAttachment(final Long id, Long issueId, final File tempAttachment, final String filename, final String contentType, final String formToken)
    {
        this.id = id;
        this.tempAttachment = tempAttachment;
        this.filename = filename;
        this.contentType = contentType;
        this.created = new Date();
        this.formToken = formToken;
        this.issueId = issueId;
    }

    public Long getId()
    {
        return id;
    }

    @Deprecated
    public Long getIssueId()
    {
        return issueId;
    }

    public File getFile()
    {
        return tempAttachment;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getFilename()
    {
        return filename;
    }

    public Date getCreated()
    {
        return created;
    }

    public String getFormToken()
    {
        return formToken;
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

        final TemporaryAttachment that = (TemporaryAttachment) o;

        if (!contentType.equals(that.contentType))
        {
            return false;
        }
        if (!filename.equals(that.filename))
        {
            return false;
        }
        if (!id.equals(that.id))
        {
            return false;
        }
        if (!tempAttachment.equals(that.tempAttachment))
        {
            return false;
        }
        if (!created.equals(that.created))
        {
            return false;
        }
        if (!formToken.equals(that.formToken))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id.hashCode();
        result = 31 * result + (tempAttachment != null ? tempAttachment.hashCode() : 0);
        result = 31 * result + (filename != null ? filename.hashCode() : 0);
        result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (formToken != null ? formToken.hashCode() : 0);

        return result;
    }

    public int compareTo(final TemporaryAttachment other)
    {
        return this.created.compareTo(other.getCreated());
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Deprecated
    private final static String getIssueToken(Issue issue)
    {
        if (issue == null)
        {
            return null;
        }

        return getIssueToken(issue.getId());
    }

    @Deprecated
    public final static String getIssueToken(Long issueId)
    {
        if (issueId == null)
        {
            return null;
        }

        return String.format(ISSUETOKEN_FORMAT, issueId);
    }

    @Deprecated
    private static String getProjectToken(final Project project)
    {
        if (project == null)
        {
            return null;
        }

        return getProjectToken(project.getId());
    }

    @Deprecated
    private static String getProjectToken(final Long projectId)
    {
        if (projectId == null)
        {
            return null;
        }

        return String.format(PROJECTTOKEN_FORMAT, projectId);
    }

    @Deprecated
    public final static String getEntityToken(Either<Issue, Project> entity)
    {
        if (entity == null)
        {
            return null;
        }

        if (entity.isLeft())
        {
            return getIssueToken(entity.left().get());
        }
        else if (entity.isRight())
        {
            return getProjectToken(entity.right().get());
        }
        else
        {
            return null;
        }
    }

}

