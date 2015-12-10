package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalChangeGroup;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;

/**
 * @since v3.13
 */
public class ChangeGroupTransformerImpl implements ChangeGroupTransformer
{
    public ExternalChangeGroup transform(final ProjectImportMapper projectImportMapper, final ExternalChangeGroup changeGroup)
    {
        final String newIssueId = projectImportMapper.getIssueMapper().getMappedId(changeGroup.getIssueId());
        final String mappedUserKey = projectImportMapper.getUserMapper().getMappedUserKey(changeGroup.getAuthor());

        final ExternalChangeGroup transformedChangeGroup = new ExternalChangeGroup();
        transformedChangeGroup.setAuthor(mappedUserKey);
        transformedChangeGroup.setCreated(changeGroup.getCreated());
        transformedChangeGroup.setIssueId(newIssueId);

        return transformedChangeGroup;
    }
}
