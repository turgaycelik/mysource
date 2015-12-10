package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;

/**
 * @since v3.13
 */
public class AttachmentTransformerImpl implements AttachmentTransformer
{
    public ExternalAttachment transform(final ProjectImportMapper projectImportMapper, final ExternalAttachment attachment)
    {
        final ExternalAttachment transformedAttachment = new ExternalAttachment();
        transformedAttachment.setAttachedDate(attachment.getAttachedDate());
        transformedAttachment.setAttachedFile(attachment.getAttachedFile());
        transformedAttachment.setFileName(attachment.getFileName());

        // Set the issue id to be the mapped issue id.
        final String mappedIssueId = projectImportMapper.getIssueMapper().getMappedId(attachment.getIssueId());
        transformedAttachment.setIssueId(mappedIssueId);
        // Map the user key of the attacher
        final String mappedAttacher = projectImportMapper.getUserMapper().getMappedUserKey(attachment.getAttacher());
        transformedAttachment.setAttacher(mappedAttacher);
        return transformedAttachment;
    }
}
