package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.issue.attachment.AttachmentAdapterImpl;
import com.atlassian.jira.issue.attachment.AttachmentStore;
import com.atlassian.jira.issue.attachment.FileAttachments;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Date;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v3.13
 */
public class AttachmentParserImpl implements AttachmentParser
{
    private static final String ID = "id";
    private static final String ISSUE = "issue";
    private static final String FILENAME = "filename";
    private static final String CREATED = "created";
    private static final String AUTHOR = "author";

    private final AttachmentStore attachmentStore;
    protected final String attachmentPath;

    public AttachmentParserImpl(final AttachmentStore attachmentStore, final String attachmentPath) {
        this.attachmentStore = attachmentStore;
        this.attachmentPath = attachmentPath;
    }

    public ExternalAttachment parse(final Map<String, String> attributes) throws ParseException
    {
        notNull("attributes", attributes);
        //<FileAttachment id="10000" issue="10000" mimetype="application/octet-stream" filename="clover.license" created="2008-01-08 12:17:39.544" filesize="7535" author="admin"/>
        final String id = attributes.get(ID);
        final String issueId = attributes.get(ISSUE);
        final String fileName = attributes.get(FILENAME);
        final String created = attributes.get(CREATED);
        final String author = attributes.get(AUTHOR);

        //Validate the values
        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("A file attachment must have an id specified.");
        }
        if (StringUtils.isEmpty(issueId))
        {
            throw new ParseException("A file attachment with id '" + id + "' must have an issue id specified.");
        }
        // JRA-15914 Some backups have an empty file name.
        if (fileName == null)
        {
            throw new ParseException("A file attachment with id '" + id + "' must have a file name specified.");
        }
        if (StringUtils.isEmpty(created))
        {
            throw new ParseException("A file attachment with id '" + id + "' must have a create date specified.");
        }

        final Date createdDate = java.sql.Timestamp.valueOf(created);

        return new ExternalAttachment(id, issueId, fileName, createdDate, author);
    }

    public boolean isUsingOriginalKeyPath(final ExternalProject project)
    {
        return StringUtils.isNotEmpty(project.getOriginalKey()) && !StringUtils.equals(project.getKey(), project.getOriginalKey());
    }

    // TODO - Remove attachment directory access
    public File getAttachmentDirectory(final ExternalProject project, final String issueKey)
    {
        final File attachmentDirectory;
        if (isUsingOriginalKeyPath(project))
        {
            // project key was renamed we need to look it the original place
            attachmentDirectory = attachmentStore.getAttachmentDirectory(attachmentPath,
                    project.getOriginalKey(), FileAttachments.computeIssueKeyForOriginalProjectKey(project.getOriginalKey(), issueKey));
        }
        else
        {
            attachmentDirectory = attachmentStore.getAttachmentDirectory(attachmentPath, project.getKey(), issueKey);
        }
        return attachmentDirectory;
    }

    public File getAttachmentFile(final ExternalAttachment attachment, final ExternalProject project, final String issueKey)
    {
        final AttachmentStore.AttachmentAdapter attachmentAdapter = new AttachmentAdapterImpl(Long.valueOf(attachment.getId()), attachment.getFileName());
        final File attachmentFile = attachmentStore.getAttachmentFile(attachmentAdapter, getAttachmentDirectory(project, issueKey));
        return attachmentFile;
    }
}
