package com.atlassian.jira.jelly.tag.issue;

import java.util.Date;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.jelly.UserAwareDynaBeanTagSupport;
import com.atlassian.jira.jelly.tag.util.JellyTagUtils;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.lang.StringUtils;

/**
 * Copyright All Rights Reserved.
 * Created: christo 18/07/2006 09:50:08
 */
public class AddComment extends UserAwareDynaBeanTagSupport
{

    private static final String KEY_ISSUE_KEY = "issue-key";
    private static final String KEY_COMMENTER = "commenter";
    private static final String KEY_COMMENT = "comment";
    private static final String KEY_CREATED_DATE = "created";
    private static final String KEY_UPDATED_DATE = "updated";
    private static final String KEY_UPDATE_AUTHOR = "editedBy";
    private static final String KEY_GROUP_LEVEL = "groupLevel";
    private static final String KEY_ROLE_LEVEL = "roleLevel";
    /**
     * Optional parameter.  If set to true, then the issue's updated date will
     * be updated with the comment's updated date.  If set to false, the issue's updated date
     * will be left as is. Defaults to true.
     * @since 4.0
     */
    private static final String KEY_TWEAK_ISSUE_UPDATE_DATE = "tweakIssueUpdateDate";


    public void doTag(XMLOutput xmlOutput) throws JellyTagException
    {
        // Retrieve properties from Jelly Tag
        String commentBody = getPropertyByKey(KEY_COMMENT);
        String grouplevel = getPropertyByKey(KEY_GROUP_LEVEL);
        String roleLevel = getPropertyByKey(KEY_ROLE_LEVEL);
        String dateCreated = getPropertyByKey(KEY_CREATED_DATE);
        String issueKey = getPropertyByKey(KEY_ISSUE_KEY);
        String author = getPropertyByKey(KEY_COMMENTER);
        String updateAuthor = getPropertyByKey(KEY_UPDATE_AUTHOR);
        String dateUpdated = getPropertyByKey(KEY_UPDATED_DATE);
        String tweakIssueUpdateDateString = getPropertyByKey(KEY_TWEAK_ISSUE_UPDATE_DATE);
        boolean tweakIssueUpdateDate = true;

        // Set the 'author' of the comment based off the existence of a 'commenter'
        if (author == null)
        {
            author = getUsername();
        }
        if (updateAuthor == null)
        {
            updateAuthor = author;
        }

        if(StringUtils.isNotBlank(tweakIssueUpdateDateString))
        {
            tweakIssueUpdateDate = Boolean.valueOf(tweakIssueUpdateDateString);
        }

        // preserving earlier behaviour by using the java.sql.Timestamp parser
        Date created;
        if (StringUtils.isNotBlank(dateCreated))
        {
            created = new Date(JellyTagUtils.parseDate(dateCreated).getTime());
        }
        else
        {
            created = new Date();
        }

        Date updated;
        if (StringUtils.isNotBlank(dateUpdated))
        {
            updated = new Date(JellyTagUtils.parseDate(dateUpdated).getTime());
        }
        else
        {
            updated = created;
        }

        ApplicationUser authorUser = findUser(author);
        ApplicationUser updateAuthorUser = findUser(updateAuthor);

        Issue issue = findIssue(issueKey);

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        // Find the role for the comment if this is set.
        ProjectRoleService projectRoleService = ComponentAccessor.getComponent(ProjectRoleService.class);

        Long roleLevelId = null;
        if (roleLevel != null)
        {
            try
            {
                roleLevelId = new Long(roleLevel);
            }
            catch (NumberFormatException nfe)
            {
                // We will just fall back to resolving it by name
            }

            if (roleLevelId == null)
            {
                ProjectRole projectRole = projectRoleService.getProjectRoleByName(authorUser == null ? null : authorUser.getDirectoryUser(), roleLevel, errorCollection);
                if (projectRole != null)
                {
                    roleLevelId = projectRole.getId();
                }
                else
                {
                    throw new JellyTagException("ProjectRole level not found: " + roleLevel);
                }
            }
        }

        if(updated.getTime() < created.getTime())
        {
            throw new JellyTagException("The updated date is earlier than the comment date, this is not allowed.");
        }

        // Now create the comment if no errors occured with the role work above.
        if (!errorCollection.hasAnyErrors())
        {
            CommentService commentService = ComponentAccessor.getComponent(CommentService.class);
            // Do validation from the service
            commentService.hasPermissionToCreate(authorUser, issue, errorCollection);
            if (errorCollection.hasAnyErrors())
            {
                throw new JellyTagException(errorCollection.toString());
            }
            commentService.isValidAllCommentData(authorUser, issue, commentBody, grouplevel, roleLevelId == null ? null : roleLevelId.toString(), errorCollection);
            if (errorCollection.hasAnyErrors())
            {
                throw new JellyTagException(errorCollection.toString());
            }

            // Create the comment through the manager so that we can force the updateAuthor and the updated date if they
            // have been specified.
            CommentManager commentManager = ComponentAccessor.getComponent(CommentManager.class);
            commentManager.create(issue, authorUser, updateAuthorUser, commentBody, grouplevel, roleLevelId == null ? null : roleLevelId, created, updated, true, tweakIssueUpdateDate);
        }
        else
        {
            throw new JellyTagException(errorCollection.toString());
        }

        invokeBody(xmlOutput);
    }

    private ApplicationUser findUser(String author)
            throws JellyTagException
    {
        ApplicationUser user = ComponentAccessor.getUserManager().getUserByName(author);
        if (user == null)
        {
            throw new JellyTagException("No such user exists: " + author);
        }
        return user;
    }

    private Issue findIssue(String issueKey)
            throws JellyTagException
    {
        IssueManager issueManager = ComponentAccessor.getComponent(IssueManager.class);
        Issue issue;
        try
        {
            issue = issueManager.getIssueObject(issueKey);
        }
        catch (RuntimeException e)
        {
            throw new JellyTagException("Error while trying to retrieve issue " + issueKey, e);
        }

        // Check that the issue is backed by a GV
        if (issue == null || issue.getGenericValue() == null)
        {
            throw new JellyTagException("The issue with key: " + issueKey + " does not exist");
        }

        return issue;
    }

    private String getPropertyByKey(String propertyKey)
    {
        String value = null;
        String valueRaw = (String) getProperties().get(propertyKey);
        if (StringUtils.isNotBlank(valueRaw))
        {
            value = valueRaw;
        }
        return value;
    }
}
