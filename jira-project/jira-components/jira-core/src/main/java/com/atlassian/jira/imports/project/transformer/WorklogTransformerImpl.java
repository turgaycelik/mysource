package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalWorklog;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import org.apache.log4j.Logger;

/**
 * @since v3.13
 */
public class WorklogTransformerImpl implements WorklogTransformer
{
    private static final Logger log = Logger.getLogger(WorklogTransformerImpl.class);

    public ExternalWorklog transform(final ProjectImportMapper projectImportMapper, final ExternalWorklog oldWorklog)
    {
        // Create a new ExternalWorklog and add non-mapped values
        final ExternalWorklog newWorklog = new ExternalWorklog();
        newWorklog.setComment(oldWorklog.getComment());
        newWorklog.setGroupLevel(oldWorklog.getGroupLevel());
        newWorklog.setCreated(oldWorklog.getCreated());
        newWorklog.setUpdated(oldWorklog.getUpdated());
        newWorklog.setStartDate(oldWorklog.getStartDate());
        newWorklog.setTimeSpent(oldWorklog.getTimeSpent());

        // transform values that need to be mapped.
        final String newUpdateAuthor = projectImportMapper.getUserMapper().getMappedUserKey(oldWorklog.getUpdateAuthor());
        final String newAuthor = projectImportMapper.getUserMapper().getMappedUserKey(oldWorklog.getAuthor());
        newWorklog.setUpdateAuthor(newUpdateAuthor);
        newWorklog.setAuthor(newAuthor);
        final String mappedIssueId = projectImportMapper.getIssueMapper().getMappedId(oldWorklog.getIssueId());
        newWorklog.setIssueId(mappedIssueId);
        final String oldRoleLevel = oldWorklog.getRoleLevelId() == null ? null : oldWorklog.getRoleLevelId().toString();
        final String newRoleLevelIdAsString = projectImportMapper.getProjectRoleMapper().getMappedId(oldRoleLevel);
        if (newRoleLevelIdAsString != null)
        {
            newWorklog.setRoleLevelId(new Long(newRoleLevelIdAsString));
        }
        else if (oldRoleLevel != null)
        {
            // lets log the fact that we are dropping the comment role level
            log.warn("Dropping the project role visibility level with id '" + oldRoleLevel + "' for a worklog against issue with id '" + mappedIssueId + "' as JIRA is unable to resolve the project role in the backup data.");
        }
        return newWorklog;
    }
}
