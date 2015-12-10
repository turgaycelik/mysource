package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.issue.attachment.AttachmentConstants;

import java.io.File;
import java.util.Map;

/**
 * Converts issue attachment xml in a JIRA backup to an object representation.
 *
 * @since v3.13
 */
public interface AttachmentParser
{
    public static final String ATTACHMENT_ENTITY_NAME = AttachmentConstants.ATTACHMENT_ENTITY_NAME;

    /**
     * Parses the file attachment data from the backup XML.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an ExternalAttachment. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>id (required)</li>
     * <li>issue (required)</li>
     * <li>filename (required)</li>
     * <li>created (required)</li>
     * </ul>
     * An optional attribute is:
     * <ul>
     * <li>author</li>
     * </ul>
     *
     * @return an ExternalAttachment if the attributes contain the required fields
     * @throws com.atlassian.jira.exception.ParseException if the required fields are not found in the attributes map
     */
    ExternalAttachment parse(Map<String, String> attributes) throws ParseException;

    /**
     * Returns the full path, on disk, to where the attachment file should be stored based on the provided inputs.
     *
     *
     * @param attachment represents the database row for an attachment from the backup.
     * @param project the backup project key, used to determine the path to the attachment file.
     * @param issueKey the issue key that the attachment is associated with, used to determine the path to the attachment file.
     * @return the full path, on disk, to where the attachment file should be stored based on the provided inputs.
     */
    File getAttachmentFile(ExternalAttachment attachment, ExternalProject project, String issueKey);

    File getAttachmentDirectory(ExternalProject project, String issueKey);

    boolean isUsingOriginalKeyPath(ExternalProject project);
}
