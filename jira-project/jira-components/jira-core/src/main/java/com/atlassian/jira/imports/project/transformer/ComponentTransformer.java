package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;

/**
 * Used to transform an ExternalNodeAssociation(IssueComponent) based on the project import mapper that is provided. This should only be
 * used with a fully mapped and validated ProjectImportMapper.
 *
 * @since v3.13
 */
public interface ComponentTransformer
{
    /**
     * Transforms an ExternalNodeAssociation (IssueComponent) based on the project import mapper that is provided. This should only be
     * used with a fully mapped and validated ProjectImportMapper.
     *
     * @param projectImportMapper a fully mapped and validated ProjectImportMapper
     * @param component the external node association that contains all the old values that need to be transformed and other values
     * that should be stored that need no transformation.
     * @return a new ExternalNodeAssociation that contains the transformed values based on the projectImportMapper. Null
     * if the node association type is not a component type or if the issue or component id's have not been mapped.
     */
    ExternalNodeAssociation transform(ProjectImportMapper projectImportMapper, ExternalNodeAssociation component);
}
