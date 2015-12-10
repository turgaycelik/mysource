package com.atlassian.jira.issue.comments;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.user.util.Users.isAnonymous;

/**
 * Used to check user commenting permissions
 */
public class DefaultCommentPermissionManager implements CommentPermissionManager
{
    private final ProjectRoleManager projectRoleManager;
    private final PermissionManager permissionManager;
    private final GroupManager groupManager;

    public DefaultCommentPermissionManager(ProjectRoleManager projectRoleManager, PermissionManager permissionManager, GroupManager groupManager)
    {
        this.projectRoleManager = projectRoleManager;
        this.permissionManager = permissionManager;
        this.groupManager = groupManager;
    }

    /**
     * Determines whether the user can see given comment, or is prevented by restrictions on the visibility of the
     * comment (either group- or Project Role-based)
     * <p/>
     * The User can always see the comment if the comment does not have restricted visibility,
     * otherwise only if the user is in either comments group or project role
     * visibility level.
     *
     * @param user    application user
     * @param comment comment
     * @return true if user can see the comment, false otherwise
     */
    public boolean hasBrowsePermission(ApplicationUser user, Comment comment)
    {
        // Retrieve both the group level and role level
        String groupLevel = comment.getGroupLevel();
        Long roleLevel = comment.getRoleLevelId();

        boolean roleProvided = (roleLevel != null);
        boolean groupProvided = StringUtils.isNotBlank(groupLevel);

        boolean userInRole = roleProvided && isUserInRole(roleLevel, user, comment.getIssue());
        boolean userInGroup = groupProvided && isUserInGroup(user, groupLevel);
        boolean noLevelsProvided = !groupProvided && !roleProvided;

        return (noLevelsProvided || userInRole || userInGroup);
    }

    @Override
    public boolean hasBrowsePermission(User user, Comment comment)
    {
        return hasBrowsePermission(ApplicationUsers.from(user), comment);
    }

    /**
     * Determines whether the user can edit given comment.
     * <p/>
     * The User can edit the given comment if he or she can edit all comments
     * for the issue, or is the author and has "edit own comments" permission.
     *
     * @param user    user
     * @param comment comment to edit
     * @return true if user can edit the given comment, false otherwise
     */
    public boolean hasEditPermission(ApplicationUser user, Comment comment)
    {
        return hasEditAllPermission(user, comment.getIssue()) ||
               (hasEditOwnPermission(user, comment.getIssue()) &&  isUserCommentAuthor(user, comment));
    }

    @Override
    public boolean hasEditPermission(User user, Comment comment)
    {
        return hasEditPermission(ApplicationUsers.from(user), comment);
    }

    public boolean isUserCommentAuthor(ApplicationUser user, Comment comment)
    {
        ApplicationUser commentAuthor = comment.getAuthorApplicationUser();

        // if the author was anonymous, then no-one is the author
        if (commentAuthor == null)
        {
            return false;
        }

        // if the user is anonymous, they aren't the author
        if (isAnonymous(user))
        {
            return false;
        }

        // if the attachment author is the user, return true
        return commentAuthor.equals(user);
    }

    @Override
    public boolean isUserCommentAuthor(User user, Comment comment)
    {
        return isUserCommentAuthor(ApplicationUsers.from(user), comment);
    }

    public boolean hasEditAllPermission(final ApplicationUser user, final Issue issue)
    {
        return permissionManager.hasPermission(Permissions.COMMENT_EDIT_ALL, issue, user);
    }

    @Override
    public boolean hasEditAllPermission(User user, Issue issue)
    {
        return hasEditAllPermission(ApplicationUsers.from(user), issue);
    }

    public boolean hasEditOwnPermission(final ApplicationUser user, final Issue issue)
    {
        return permissionManager.hasPermission(Permissions.COMMENT_EDIT_OWN, issue, user);
    }

    @Override
    public boolean hasEditOwnPermission(User user, Issue issue)
    {
        return hasEditOwnPermission(ApplicationUsers.from(user), issue);
    }

    public boolean hasDeleteAllPermission(final ApplicationUser user, final Issue issue)
    {
        return permissionManager.hasPermission(Permissions.COMMENT_DELETE_ALL, issue, user);
    }

    @Override
    public boolean hasDeleteAllPermission(User user, Issue issue)
    {
        return hasDeleteAllPermission(ApplicationUsers.from(user), issue);
    }

    public boolean hasDeleteOwnPermission(final ApplicationUser user, final Issue issue)
    {
        return permissionManager.hasPermission(Permissions.COMMENT_DELETE_OWN, issue, user);
    }

    @Override
    public boolean hasDeleteOwnPermission(User user, Issue issue)
    {
        return hasDeleteOwnPermission(ApplicationUsers.from(user), issue);
    }

    private boolean isUserInGroup(ApplicationUser user, String groupname)
    {
        if (user == null)
        {
            return false;
        }
        Group group = groupManager.getGroup(groupname);
        return group != null && groupManager.isUserInGroup(user == null ? null : user.getDirectoryUser(), group);
    }

    private boolean isUserInRole(Long roleLevel, ApplicationUser user, Issue issue)
    {
        boolean isUserInRole = false;
        ProjectRole projectRole = projectRoleManager.getProjectRole(roleLevel);
        if (projectRole != null)
        {
            isUserInRole = projectRoleManager.isUserInProjectRole(user, projectRole, issue.getProjectObject());
        }
        return isUserInRole;
    }

}
