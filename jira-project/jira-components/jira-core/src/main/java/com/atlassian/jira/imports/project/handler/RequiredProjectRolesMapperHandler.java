package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComment;
import com.atlassian.jira.external.beans.ExternalWorklog;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.parser.CommentParser;
import com.atlassian.jira.imports.project.parser.CommentParserImpl;
import com.atlassian.jira.imports.project.parser.WorklogParser;
import com.atlassian.jira.imports.project.parser.WorklogParserImpl;
import com.atlassian.jira.issue.worklog.OfBizWorklogStore;

import java.util.Map;

/**
 * This is used to flag required values in the project role mapper. This mapper will inspect all the non-issue entities
 * that are known to store project role data so we can flag all required roles for our selected project.
 * The places that this mapper looks are:
 * <p/>
 * <ul>
 * <li>Comment role level</li>
 * <li>Worklog role level</li>
 * </ul>
 *
 * @since v3.13
 */
public class RequiredProjectRolesMapperHandler implements ImportEntityHandler
{
    private CommentParser commentParser;
    private WorklogParser worklogParser;
    private final BackupProject backupProject;
    private final SimpleProjectImportIdMapper projectRoleMapper;

    public RequiredProjectRolesMapperHandler(final BackupProject backupProject, final SimpleProjectImportIdMapper projectRoleMapper)
    {
        this.backupProject = backupProject;
        this.projectRoleMapper = projectRoleMapper;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        if (CommentParser.COMMENT_ENTITY_NAME.equals(entityName))
        {
            handleComment(attributes);
        }
        else if (OfBizWorklogStore.WORKLOG_ENTITY.equals(entityName))
        {
            handleWorklog(attributes);
        }
    }

    private void handleComment(final Map attributes) throws ParseException
    {
        final ExternalComment externalComment = getCommentParser().parse(attributes);
        if ((externalComment != null) && backupProject.containsIssue(externalComment.getIssueId()))
        {
            final Long roleLevelId = externalComment.getRoleLevelId();
            if (roleLevelId != null)
            {
                projectRoleMapper.flagValueAsRequired(roleLevelId.toString());
            }
        }
    }

    private void handleWorklog(final Map attributes) throws ParseException
    {
        final ExternalWorklog externalWorklog = getWorklogParser().parse(attributes);
        if ((externalWorklog != null) && backupProject.containsIssue(externalWorklog.getIssueId()))
        {
            final Long roleLevelId = externalWorklog.getRoleLevelId();
            if (roleLevelId != null)
            {
                projectRoleMapper.flagValueAsRequired(roleLevelId.toString());
            }
        }
    }

    ///CLOVER:OFF
    public void startDocument()
    {
    // No-op
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public void endDocument()
    {
    // No-op
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    WorklogParser getWorklogParser()
    {
        if (worklogParser == null)
        {
            worklogParser = new WorklogParserImpl();
        }
        return worklogParser;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    CommentParser getCommentParser()
    {
        if (commentParser == null)
        {
            commentParser = new CommentParserImpl();
        }
        return commentParser;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final RequiredProjectRolesMapperHandler that = (RequiredProjectRolesMapperHandler) o;

        if (backupProject != null ? !backupProject.equals(that.backupProject) : that.backupProject != null)
        {
            return false;
        }
        if (commentParser != null ? !commentParser.equals(that.commentParser) : that.commentParser != null)
        {
            return false;
        }
        if (projectRoleMapper != null ? !projectRoleMapper.equals(that.projectRoleMapper) : that.projectRoleMapper != null)
        {
            return false;
        }
        if (worklogParser != null ? !worklogParser.equals(that.worklogParser) : that.worklogParser != null)
        {
            return false;
        }

        return true;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public int hashCode()
    {
        int result;
        result = (commentParser != null ? commentParser.hashCode() : 0);
        result = 31 * result + (worklogParser != null ? worklogParser.hashCode() : 0);
        result = 31 * result + (backupProject != null ? backupProject.hashCode() : 0);
        result = 31 * result + (projectRoleMapper != null ? projectRoleMapper.hashCode() : 0);
        return result;
    }
    ///CLOVER:ON
}
