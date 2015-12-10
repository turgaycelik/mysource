package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;

/**
 * Used to transform an ExternalIssue based on the project import mapper that is provided. This should only be
 * used with a fully mapped and validated ProjectImportMapper.
 *
 * @since v3.13
 */
public interface IssueTransformer
{
    /**
     * Transforms an ExternalIssue based on the project import mapper that is provided. This should only be
     * used with a fully mapped and validated ProjectImportMapper.
     * <p>Note that the ID is left as null, as the new ID will not be known until the object is created.</p>
     *
     * @param projectImportMapper a fully mapped and validated ProjectImportMapper
     * @param issue the external issue that contains all the old values that need to be transformed and other values
     * that should be stored that need no transformation.
     * @return a new ExternalIssue that contains the transformed values based on the projectImportMapper.
     */
    ExternalIssue transform(ProjectImportMapper projectImportMapper, ExternalIssue issue);
}
