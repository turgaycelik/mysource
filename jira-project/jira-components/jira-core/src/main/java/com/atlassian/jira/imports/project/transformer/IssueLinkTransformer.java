package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalLink;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;

/**
 * Used to transform an ExternalLink based on the project import mapper that is provided.
 * This should only be used with a fully mapped and validated ProjectImportMapper.
 * Implementations would normally also use an IssueManager to map any Issue IDs linked to other projects.
 *
 * @since v3.13
 */
public interface IssueLinkTransformer
{
    /**
     * Used to transform an ExternalLink based on the project import mapper that is provided.
     * <p>At least one of the two issues in the link should belong to the project we are importing.
     * For issues in our project, we use the Issue Mapper to get the new ID.
     * For issues in other projects, we look up the Issue in the current system by its key.
     * If an issue with that key does not exist in the current JIRA, then this method will return <code>null</code> to 
     * indicate that we will ignore this link (do not import).
     * </p>
     * <p>This should only be used with a fully mapped and validated ProjectImportMapper.</p>
     *
     * @param projectImportMapper a fully mapped and validated ProjectImportMapper
     * @param issueLink the ExternalLink that contains all the old values that need to be transformed and other values
     * that should be stored that need no transformation.
     * @return a new ExternalLink that contains the transformed values based on the projectImportMapper, or null if this link cannot be created in the current system.
     */
    ExternalLink transform(ProjectImportMapper projectImportMapper, ExternalLink issueLink);
}
