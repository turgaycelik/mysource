package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalLabel;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;

/**
 * Used to transform an ExternalLabel based on the project import mapper that is provided. This should only be
 * used with a fully mapped and validated ProjectImportMapper.
 *
 * @since v4.2
 */
public interface LabelTransformer
{
    /**
     * Transforms an ExternalLabel based on the project import mapper that is provided. This should only be
     * used with a fully mapped and validated ProjectImportMapper.
     * <p>Note that the ID is left as null, as the new ID will not be known until the object is created.</p>
     *
     * @param projectImportMapper a fully mapped and validated ProjectImportMapper
     * @param label the external label that contains all the old values that need to be transformed and other values
     * that should be stored that need no transformation.
     * @return a new ExternalLabel that contains the transformed values based on the projectImportMapper.
     */
    ExternalLabel transform(ProjectImportMapper projectImportMapper, ExternalLabel label);
}