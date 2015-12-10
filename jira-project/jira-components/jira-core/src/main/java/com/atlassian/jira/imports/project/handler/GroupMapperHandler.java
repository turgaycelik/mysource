package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComment;
import com.atlassian.jira.external.beans.ExternalGroup;
import com.atlassian.jira.external.beans.ExternalWorklog;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.parser.CommentParser;
import com.atlassian.jira.imports.project.parser.CommentParserImpl;
import com.atlassian.jira.imports.project.parser.GroupParser;
import com.atlassian.jira.imports.project.parser.GroupParserImpl;
import com.atlassian.jira.imports.project.parser.WorklogParser;
import com.atlassian.jira.imports.project.parser.WorklogParserImpl;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * This is used to find all the defined groups in the backup file.
 *
 * @since v3.13
 */
public class GroupMapperHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(GroupMapperHandler.class);

    private final BackupProject backupProject;
    private final SimpleProjectImportIdMapper groupMapper;
    private GroupParser groupParser;
    private CommentParser commentParser;
    private WorklogParser worklogParser;

    public GroupMapperHandler(BackupProject backupProject, final SimpleProjectImportIdMapper groupMapper)
    {
        this.backupProject = backupProject;
        this.groupMapper = groupMapper;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        // Check if this is a "Group" entity
        if (GroupParser.GROUP_ENTITY_NAME.equals(entityName))
        {
            final ExternalGroup group = getGroupParser().parse(attributes);
            // No "ID" as such - we just use the groupName.
            groupMapper.registerOldValue(group.getName(), group.getName());
        }
        // Check for Comments with Group Level security
        else if (CommentParser.COMMENT_ENTITY_NAME.equals(entityName))
        {
            final ExternalComment comment = getCommentParser().parse(attributes);
            // The Comment Parser can return null if we get old values where type != "Comment".
            if (comment != null && backupProject.containsIssue(comment.getIssueId()))
            {
                // Get the group that the comment is restricted to
                final String groupName = comment.getGroupLevel();
                // Note that we have seen instances where this is the empty string, rather than null (on JAC - seems to be created by SOAP call?).
                if ((groupName != null) && (groupName.length() > 0))
                {
                    if (log.isDebugEnabled())
                    {
                        // During debug logging we log extra information for the first time a required group appears.
                        if (!groupMapper.getRequiredOldIds().contains(groupName))
                        {
                            // We are the first to flag this group as required.
                            log.debug("The group '" + groupName + "' is required because it is used in the security of comment (Action) with id = '" + comment.getId() + "'.");
                        }
                    }
                    groupMapper.flagValueAsRequired(comment.getGroupLevel());
                }
            }
        }
        // Check for Worklogs with Group Level security
        else if (WorklogParser.WORKLOG_ENTITY_NAME.equals(entityName))
        {
            final ExternalWorklog worklog = getWorklogParser().parse(attributes);
            // Check if this worklog is for our project:
            if (backupProject.containsIssue(worklog.getIssueId()))
            {
                final String groupName = worklog.getGroupLevel();
                if ((groupName != null) && (groupName.length() > 0))
                {
                    if (log.isDebugEnabled())
                    {
                        // During debug logging we log extra information for the first time a required group appears.
                        if (!groupMapper.getRequiredOldIds().contains(groupName))
                        {
                            // We are the first to flag this group as required.
                            log.debug("The group '" + groupName + "' is required because it is used in the security of Worklog with id = '" + worklog.getId() + "'.");
                        }
                    }
                    // No need to check if the worklog actually has Group Level security - the mapper handles null.
                    groupMapper.flagValueAsRequired(worklog.getGroupLevel());
                }
            }
        }
    }

    public void startDocument()
    {
    // no-op
    }

    public void endDocument()
    {
    // no-op
    }

    ///CLOVER:OFF
    GroupParser getGroupParser()
    {
        if (groupParser == null)
        {
            groupParser = new GroupParserImpl();
        }
        return groupParser;
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
    WorklogParser getWorklogParser()
    {
        if (worklogParser == null)
        {
            worklogParser = new WorklogParserImpl();
        }
        return worklogParser;
    }
    ///CLOVER:ON
}
