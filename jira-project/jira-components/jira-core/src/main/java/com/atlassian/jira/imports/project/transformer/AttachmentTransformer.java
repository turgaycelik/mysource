package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;

/**
 * Used to transform an ExternalAttachment based on the project import mapper that is provided. This should only be
 * used with a fully mapped and validated ProjectImportMapper.
 *
 * @since v3.13
 */
public interface AttachmentTransformer
{
    /**
     * Transforms an ExternalAttachment based on the project import mapper that is provided. This should only be
     * used with a fully mapped and validated ProjectImportMapper.
     *
     * @param projectImportMapper a fully mapped and validated ProjectImportMapper
     * @param attachment the external attachment that contains all the old values that need to be transformed and other values
     * that should be stored that need no transformation.
     * @return a new ExternalAttachment that contains the transformed values based on the projectImportMapper.
     */
    ExternalAttachment transform(ProjectImportMapper projectImportMapper, ExternalAttachment attachment);

}
