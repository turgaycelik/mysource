package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.external.beans.ExternalChangeGroup;
import com.atlassian.jira.external.beans.ExternalComment;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalProjectRoleActor;
import com.atlassian.jira.external.beans.ExternalVoter;
import com.atlassian.jira.external.beans.ExternalWatcher;
import com.atlassian.jira.external.beans.ExternalWorklog;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.mapper.UserMapper;
import com.atlassian.jira.imports.project.parser.AttachmentParser;
import com.atlassian.jira.imports.project.parser.AttachmentParserImpl;
import com.atlassian.jira.imports.project.parser.ChangeGroupParser;
import com.atlassian.jira.imports.project.parser.ChangeGroupParserImpl;
import com.atlassian.jira.imports.project.parser.CommentParser;
import com.atlassian.jira.imports.project.parser.CommentParserImpl;
import com.atlassian.jira.imports.project.parser.ProjectRoleActorParser;
import com.atlassian.jira.imports.project.parser.ProjectRoleActorParserImpl;
import com.atlassian.jira.imports.project.parser.UserAssociationParser;
import com.atlassian.jira.imports.project.parser.UserAssociationParserImpl;
import com.atlassian.jira.imports.project.parser.WorklogParser;
import com.atlassian.jira.imports.project.parser.WorklogParserImpl;
import com.atlassian.jira.issue.attachment.AttachmentStore;
import com.atlassian.jira.issue.worklog.OfBizWorklogStore;

import java.util.Map;

/**
 * This is used to flag required values in the user mapper. This mapper will inspect all the non-issue entities
 * that are known to store user data so we can flag all required users for our selected project.
 * The places that this mapper looks are:
 * <p/>
 * <ul>
 * <li>Voters</li>
 * <li>Watchers</li>
 * <li>Comment authors and editors</li>
 * <li>Worklog authors and editors</li>
 * <li>ChangeGroup authors</li>
 * </ul>
 *
 * @since v3.13
 */
public class UserMapperHandler implements ImportEntityHandler
{
    private final BackupProject backupProject;
    private final UserMapper userMapper;
    private final AttachmentStore attachmentStore;
    private CommentParser commentParser;
    private WorklogParser worklogParser;
    private ChangeGroupParser changeGroupParser;
    private ProjectRoleActorParser projectRoleActorParser;
    private UserAssociationParser userAssociationParser;
    private AttachmentParser attachmentParser;
    private final ProjectImportOptions projectImportOptions;

    public UserMapperHandler(ProjectImportOptions projectImportOptions, BackupProject backupProject, UserMapper userMapper,
            final AttachmentStore attachmentStore)
    {
        this.projectImportOptions = projectImportOptions;
        this.backupProject = backupProject;
        this.userMapper = userMapper;
        this.attachmentStore = attachmentStore;
    }

    public void handleEntity(String entityName, Map<String, String> attributes) throws ParseException
    {

        if (UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME.equals(entityName))
        {
            handleUserAssociation(attributes);
        }
        else if (CommentParser.COMMENT_ENTITY_NAME.equals(entityName))
        {
            handleAction(attributes);
        }
        else if (OfBizWorklogStore.WORKLOG_ENTITY.equals(entityName))
        {
            handleWorklog(attributes);
        }
        else if (ChangeGroupParser.CHANGE_GROUP_ENTITY_NAME.equals(entityName))
        {
            handleChangeGroup(attributes);
        }
        else if (AttachmentParser.ATTACHMENT_ENTITY_NAME.equals(entityName))
        {
            handleAttachment(attributes);
        }
        else if (ProjectRoleActorParser.PROJECT_ROLE_ACTOR_ENTITY_NAME.equals(entityName))
        {
            handleProjectRoleActor(attributes);
        }
    }

    private void handleProjectRoleActor(final Map attributes) throws ParseException
    {
        final ExternalProjectRoleActor externalProjectRoleActor = getProjectRoleActorParser().parse(attributes);
        if (backupProject.getProject().getId().equals(externalProjectRoleActor.getProjectId()) && externalProjectRoleActor.isUserActor())
        {
            this.userMapper.flagUserAsInUse(externalProjectRoleActor.getRoleActor());
        }
    }

    private void handleChangeGroup(final Map attributes) throws ParseException
    {
        ExternalChangeGroup externalChangeGroup = getChangeGroupParser().parse(attributes);
        if (externalChangeGroup != null && backupProject.containsIssue(externalChangeGroup.getIssueId()))
        {
            this.userMapper.flagUserAsInUse(externalChangeGroup.getAuthor());
        }
    }

    private void handleWorklog(final Map attributes) throws ParseException
    {
        ExternalWorklog externalWorklog = getWorklogParser().parse(attributes);
        if (externalWorklog != null && backupProject.containsIssue(externalWorklog.getIssueId()))
        {
            this.userMapper.flagUserAsInUse(externalWorklog.getAuthor());
            this.userMapper.flagUserAsInUse(externalWorklog.getUpdateAuthor());
        }
    }

    private void handleAction(final Map attributes) throws ParseException
    {
        ExternalComment externalComment = getCommentParser().parse(attributes);
        if (externalComment != null && backupProject.containsIssue(externalComment.getIssueId()))
        {
            this.userMapper.flagUserAsInUse(externalComment.getUsername());
            this.userMapper.flagUserAsInUse(externalComment.getUpdateAuthor());
        }
    }

    private void handleAttachment(final Map<String, String> attributes) throws ParseException
    {
        // Only include this if we are importing attachments in the import
        if (projectImportOptions != null && projectImportOptions.getAttachmentPath() != null)
        {
            ExternalAttachment externalAttachment = getAttachmentParser().parse(attributes);
            if (externalAttachment != null && backupProject.containsIssue(externalAttachment.getIssueId()))
            {
                this.userMapper.flagUserAsInUse(externalAttachment.getAttacher());
            }
        }
    }

    private void handleUserAssociation(final Map attributes) throws ParseException
    {
        // See if this UserAssociation can be resolved into a voter
        ExternalVoter externalVoter = getUserAssociationParser().parseVoter(attributes);
        if (externalVoter != null && backupProject.containsIssue(externalVoter.getIssueId()))
        {
            userMapper.flagUserAsInUse(externalVoter.getVoter());
        }

        // See if this UserAssociation can be resolved into a watcher
        ExternalWatcher externalWatcher = getUserAssociationParser().parseWatcher(attributes);
        if (externalWatcher != null && backupProject.containsIssue(externalWatcher.getIssueId()))
        {
            userMapper.flagUserAsInUse(externalWatcher.getWatcher());
        }
    }

    public void endDocument()
    {
        // If we are using the old projects details to overwrite the current projects details then we want to include
        // the project lead as a required user.
        if (projectImportOptions != null && projectImportOptions.overwriteProjectDetails())
        {
            userMapper.flagUserAsMandatory(backupProject.getProject().getLead());
        }
        // Also flag the component leads as mandatory
        // Unlike project lead, we always add the Components from the import, so we don't care about the overwriteProjectDetails flag.
        for (final ExternalComponent externalComponent : backupProject.getProjectComponents())
        {
            if (externalComponent.getLead() != null)
            {
                userMapper.flagUserAsMandatory(externalComponent.getLead());
            }
        }
    }

    ///CLOVER:OFF
    ChangeGroupParser getChangeGroupParser()
    {
        if (this.changeGroupParser == null)
        {
            this.changeGroupParser = new ChangeGroupParserImpl();
        }
        return this.changeGroupParser;
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    ProjectRoleActorParser getProjectRoleActorParser()
    {
        if (this.projectRoleActorParser == null)
        {
            this.projectRoleActorParser = new ProjectRoleActorParserImpl();
        }
        return this.projectRoleActorParser;
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    UserAssociationParser getUserAssociationParser()
    {
        if (this.userAssociationParser == null)
        {
            this.userAssociationParser = new UserAssociationParserImpl();
        }
        return this.userAssociationParser;
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    WorklogParser getWorklogParser()
    {
        if (this.worklogParser == null)
        {
            this.worklogParser = new WorklogParserImpl();
        }
        return this.worklogParser;
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    CommentParser getCommentParser()
    {
        if (this.commentParser == null)
        {
            this.commentParser = new CommentParserImpl();
        }
        return this.commentParser;
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    AttachmentParser getAttachmentParser()
    {
        if (this.attachmentParser == null)
        {
            this.attachmentParser = new AttachmentParserImpl(attachmentStore, projectImportOptions.getAttachmentPath());
        }
        return this.attachmentParser;
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    public void startDocument()
    {
        // No-op
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final UserMapperHandler that = (UserMapperHandler) o;

        if (attachmentParser != null ? !attachmentParser.equals(that.attachmentParser) : that.attachmentParser != null)
        {
            return false;
        }
        if (backupProject != null ? !backupProject.equals(that.backupProject) : that.backupProject != null)
        {
            return false;
        }
        if (changeGroupParser != null ? !changeGroupParser.equals(that.changeGroupParser) : that.changeGroupParser != null)
        {
            return false;
        }
        if (commentParser != null ? !commentParser.equals(that.commentParser) : that.commentParser != null)
        {
            return false;
        }
        if (userAssociationParser != null ? !userAssociationParser.equals(that.userAssociationParser) : that.userAssociationParser != null)
        {
            return false;
        }
        if (userMapper != null ? !userMapper.equals(that.userMapper) : that.userMapper != null)
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
        result = (backupProject != null ? backupProject.hashCode() : 0);
        result = 31 * result + (userMapper != null ? userMapper.hashCode() : 0);
        result = 31 * result + (commentParser != null ? commentParser.hashCode() : 0);
        result = 31 * result + (worklogParser != null ? worklogParser.hashCode() : 0);
        result = 31 * result + (changeGroupParser != null ? changeGroupParser.hashCode() : 0);
        result = 31 * result + (userAssociationParser != null ? userAssociationParser.hashCode() : 0);
        result = 31 * result + (attachmentParser != null ? attachmentParser.hashCode() : 0);
        return result;
    }
    ///CLOVER:ON
}
