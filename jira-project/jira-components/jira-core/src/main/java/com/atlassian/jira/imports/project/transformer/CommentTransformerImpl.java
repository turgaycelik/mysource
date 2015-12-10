package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalComment;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import org.apache.log4j.Logger;

/**
 * @since v3.13
 */
public class CommentTransformerImpl implements CommentTransformer
{
    private static final Logger log = Logger.getLogger(CommentTransformerImpl.class);

    public ExternalComment transform(final ProjectImportMapper projectImportMapper, final ExternalComment oldComment)
    {
        // Create a new ExternalComment and add non-mapped values
        final ExternalComment newComment = new ExternalComment();
        newComment.setBody(oldComment.getBody());
        newComment.setGroupLevel(oldComment.getGroupLevel());
        newComment.setTimePerformed(oldComment.getTimePerformed());
        newComment.setUpdated(oldComment.getUpdated());

        // transform values that need to be mapped.
        newComment.setUsername(projectImportMapper.getUserMapper().getMappedUserKey(oldComment.getUsername()));
        newComment.setUpdateAuthor(projectImportMapper.getUserMapper().getMappedUserKey(oldComment.getUpdateAuthor()));
        final String mappedIssueId = projectImportMapper.getIssueMapper().getMappedId(oldComment.getIssueId());
        newComment.setIssueId(mappedIssueId);
        final String oldRoleLevel = oldComment.getRoleLevelId() == null ? null : oldComment.getRoleLevelId().toString();
        final String newRoleLevelIdAsString = projectImportMapper.getProjectRoleMapper().getMappedId(oldRoleLevel);
        if (newRoleLevelIdAsString != null)
        {
            newComment.setRoleLevelId(new Long(newRoleLevelIdAsString));
        }
        else if (oldRoleLevel != null)
        {
            // lets log the fact that we are dropping the comment role level
            log.warn("Dropping the project role visibility level with id '" + oldRoleLevel + "' for a comment against issue with id '" + mappedIssueId + "' as JIRA is unable to resolve the project role in the backup data.");
        }
        return newComment;
    }
}
