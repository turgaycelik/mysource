package com.atlassian.jira.bc.group;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidMembershipException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.AddException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.sharing.SharePermissionDeleteUtils;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.GlobalPermissionGroupAssociationUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Default implementation of a {@link com.atlassian.jira.bc.group.GroupService}.
 *
 * @since v3.12
 */
public class DefaultGroupService implements GroupService
{
    private static final Logger log = Logger.getLogger(DefaultGroupService.class);

    private final GlobalPermissionManager globalPermissionManager;
    private final GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil;
    private final CommentManager commentManager;
    private final WorklogManager worklogManager;
    private final NotificationSchemeManager notificationSchemeManager;
    private final PermissionManager permissionManager;
    private final ProjectRoleService projectRoleService;
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final UserUtil userUtil;
    private final SharePermissionDeleteUtils sharePermissionDeleteUtils;
    private final SubscriptionManager subscriptionManager;
    private final CrowdService crowdService;
    private final GroupManager groupManager;
    private final JiraContactHelper jiraContactHelper;
    private static final int MAX_REPORTABLE_ERRORS = 5;

    public DefaultGroupService(final GlobalPermissionManager globalPermissionManager,
            final GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil,
            final CommentManager commentManager, final WorklogManager worklogManager,
            final NotificationSchemeManager notificationSchemeManager, final PermissionManager permissionManager,
            final ProjectRoleService projectRoleService, final IssueSecurityLevelManager issueSecurityLevelManager,
            final UserUtil userUtil, final SharePermissionDeleteUtils sharePermissionDeleteUtils,
            final SubscriptionManager subscriptionManager, final CrowdService crowdService,
            final JiraContactHelper jiraContactHelper, final GroupManager groupManager)
    {
        this.globalPermissionManager = globalPermissionManager;
        this.globalPermissionGroupAssociationUtil = globalPermissionGroupAssociationUtil;
        this.commentManager = commentManager;
        this.worklogManager = worklogManager;
        this.notificationSchemeManager = notificationSchemeManager;
        this.permissionManager = permissionManager;
        this.projectRoleService = projectRoleService;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.userUtil = userUtil;
        this.sharePermissionDeleteUtils = sharePermissionDeleteUtils;
        this.subscriptionManager = subscriptionManager;
        this.crowdService = crowdService;
        this.jiraContactHelper = jiraContactHelper;
        this.groupManager = groupManager;
    }

    public boolean validateDelete(final JiraServiceContext jiraServiceContext, final String groupName, final String swapGroup)
    {
        validateJiraServiceContext(jiraServiceContext);

        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        if (!userHasAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.groups.must.be.admin"));
            return false;
        }

        // Check that we are allowing JIRA to manage groups
        if (isExternalUserManagementEnabled())
        {
            String link = getContactAdminLink(i18n);
            errorCollection.addErrorMessage(i18n.getText("admin.errors.groups.error.external.managment", link));
            return false;
        }

        // Check that the group to delete is a valid group
        if (isGroupNull(groupName))
        {
            errorCollection.addErrorMessage(i18n.getText("admin.errors.groups.group.does.not.exist", groupName));
            return false;
        }

        // Check that we are not deleting the group giving us our admin permission
        if (areOnlyGroupsGrantingUserAdminPermissions(jiraServiceContext, EasyList.build(groupName)))
        {
            return false;
        }

        // Check that we do not have an admin deleting a sys admin group
        if (isAdminDeletingSysAdminGroup(jiraServiceContext, groupName))
        {
            return false;
        }

        // Check if there are any comments or worklogs that are guarded by this group
        if ((getCommentsAndWorklogsGuardedByGroupCount(groupName) > 0) && (swapGroup == null))
        {
            errorCollection.addErrorMessage(i18n.getText("admin.errors.groups.must.specify.group.to.move.comments"));
            return false;
        }

        // Check that we have specified a valid swapGroup
        if (swapGroup != null)
        {
            if (swapGroup.equalsIgnoreCase(groupName))
            {
                errorCollection.addErrorMessage(i18n.getText("admin.errors.groups.cannot.swap.to.group.deleting"));
                return false;
            }

            // Check that the swap group is a valid group
            if (isGroupNull(swapGroup))
            {
                errorCollection.addErrorMessage(i18n.getText("admin.errors.groups.invalid.swap.group"));
                return false;
            }
        }

        return true;
    }

    public boolean delete(final JiraServiceContext jiraServiceContext, final String groupName, final String swapGroup)
    {
        validateJiraServiceContext(jiraServiceContext);

        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        if (!userHasAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.groups.must.be.admin"));
            return false;
        }

        final ErrorCollection errorCollection = new SimpleErrorCollection();

        try
        {
            // The Validation happens in the request to perform the action
            // We need to call the deprecated method (i.e. we need to specify a User) because the rpc plugin allows
            // for actions by users other than the logged in user of the local authentication context. The rpc plugin
            // goes away in JIRA 7, and so can this method.
            projectRoleService.removeAllRoleActorsByNameAndType(jiraServiceContext.getLoggedInUser(), groupName, GroupRoleActorFactory.TYPE, errorCollection);

            if (!errorCollection.hasAnyErrors())
            {

                // We only need to update the comments and worklogs visibility if a swapGroup has been provided
                if (swapGroup != null)
                {
                    updateCommentsAndWorklogs(jiraServiceContext.getLoggedInUser(), groupName, swapGroup);
                }

                // Remove any permission using this group
                permissionManager.removeGroupPermissions(groupName);

                // Remove any notifications using this group
                notificationSchemeManager.removeEntities(GroupDropdown.DESC, groupName);

                sharePermissionDeleteUtils.deleteGroupPermissions(groupName);

                final Group group = getGroup(groupName);
                subscriptionManager.deleteSubscriptionsForGroup(group);

                // Remove the group itself
                removeGroup(group);

                //flush the issue security levels
                clearIssueSecurityLevelCache();

                return true;
            }
        }
        catch (final PermissionException e)
        {
            errorCollection.addErrorMessage(i18n.getText("admin.errors.groups.permission.removing.group"));
            log.error("Exception trying to remove group: " + e, e);
        }
        catch (final Exception e)
        {
            errorCollection.addErrorMessage(i18n.getText("admin.errors.groups.error.occurred.removing.group", e.getMessage()));
            log.error("Exception trying to remove group: " + e, e);
        }

        jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
        return false;
    }

    public boolean validateAddUserToGroup(final JiraServiceContext jiraServiceContext, final Collection /*<String>*/groupsToJoin, final String userName)
    {
        final BulkEditGroupValidationResult result = validateAddUsersToGroup(jiraServiceContext, groupsToJoin, EasyList.build(userName));
        return result.isSuccess();
    }

    public BulkEditGroupValidationResult validateAddUsersToGroup(final JiraServiceContext jiraServiceContext, final Collection groupsToJoin, final Collection userNames)
    {
        validateJiraServiceContext(jiraServiceContext);

        if (groupsToJoin == null)
        {
            throw new IllegalArgumentException("You must specify non null groupsToJoin.");
        }

        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        // Do a permission check to see that the currentUser is at least an admin
        if (!userHasAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            errorCollection.addErrorMessage(i18n.getText("admin.errors.groups.must.be.admin"));
            return new BulkEditGroupValidationResult(false);
        }

        // Validate that all the provided groupNames resolve to valid groups
        if (!validateGroupNamesExist(groupsToJoin, errorCollection, i18n))
        {
            return new BulkEditGroupValidationResult(false);
        }

        // Validate that external user management is not enabled
        if (isExternalUserManagementEnabled())
        {
            String link = getContactAdminLink(i18n);
            errorCollection.addErrorMessage(i18n.getText("admin.bulkeditgroups.error.cannot.edit.user.groups.external.managment", link));
            return new BulkEditGroupValidationResult(false);
        }

        boolean success = true;
        final List<String> invalidUsers = new ArrayList<String>();

        // Perform a set of validations that are specific to the user passed into the userNames collection
        for (final Object userName1 : userNames)
        {
            final String userName = (String) userName1;
            final User user = getUser(userName);

            // Validate that the provided user to add to the group is not null
            if (isUserNull(user))
            {
                errorCollection.addErrorMessage(i18n.getText("admin.bulkeditgroups.error.adding.invalid.user", userName));
                invalidUsers.add(userName);
                success = false;
            }
            // Need to validate that they are not trying to join groups they do not have permission to join
            else if (!getGroupNamesUserCanSee(jiraServiceContext.getLoggedInUser()).containsAll(groupsToJoin))
            {
                errorCollection.addErrorMessage(i18n.getText("admin.errors.cannot.join.user.groups.not.visible"));
                success = false;
            }
            // Check that the user is not already a member of the groups to join
            else if (!validateUserIsNotInSelectedGroups(jiraServiceContext, groupsToJoin, user))
            {
                invalidUsers.add(userName);
                success = false;
            }
        }

        // JRA-10393: if we're successful here, also perform a check on the current number of active users
        if (success && groupsHaveGlobalUsePermissions(groupsToJoin) && !userUtil.canActivateUsers(userNames))
        {
            errorCollection.addErrorMessage(i18n.getText("admin.errors.edit.group.membership.exceeded.license.limit"));
            success = false;

            // Note that for the BulkEditUserGroups action, we want all the users to be considered "invalid" if we can't
            // activate all of them.
            invalidUsers.addAll(userNames);
        }

        return new BulkEditGroupValidationResult(invalidUsers, success);
    }

    public BulkEditGroupValidationResult validateAddGroupsToGroup(final JiraServiceContext jiraServiceContext, final Collection<String> groupsToJoin, final Collection<String> groupNames)
    {
        validateJiraServiceContext(jiraServiceContext);

        if (groupsToJoin == null)
        {
            throw new IllegalArgumentException("You must specify non null groupsToJoin.");
        }

        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        // Do a permission check to see that the currentUser is at least an admin
        if (!userHasAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            errorCollection.addErrorMessage(i18n.getText("admin.errors.groups.must.be.admin"));
            return new BulkEditGroupValidationResult(false);
        }

        // Validate that all the provided groupNames resolve to valid groups
        if (!validateGroupNamesExist(groupsToJoin, errorCollection, i18n))
        {
            return new BulkEditGroupValidationResult(false);
        }

        // Validate that all the provided groupsToJoin are not admin
        if (!validateGroupNamesNotAdmin(groupsToJoin, errorCollection, i18n))
        {
            return new BulkEditGroupValidationResult(false);
        }

        // Validate that external user management is not enabled
        if (isExternalUserManagementEnabled())
        {
            String link = getContactAdminLink(i18n);
            errorCollection.addErrorMessage(i18n.getText("admin.editnestedgroups.error.cannot.edit.group.groups.external.managment", link));
            return new BulkEditGroupValidationResult(false);
        }

        boolean success = true;
        final List<String> invalidGroups = Lists.newArrayList();

        // Perform a set of validations that are specific to the group passed into the groupNames collection
        for (final String groupName : groupNames)
        {
            final Group group = getGroup(groupName);

            // Validate that the provided user to add to the group is not null
            if (group == null)
            {
                errorCollection.addErrorMessage(i18n.getText("admin.errors.groups.group.does.not.exist", groupName));
                invalidGroups.add(groupName);
                success = false;
            }
            // Check that the group is not already a member of the groups to join
            else if (!validateGroupIsNotInSelectedGroups(jiraServiceContext, groupsToJoin, group))
            {
                invalidGroups.add(groupName);
                success = false;
            }
        }

        return new BulkEditGroupValidationResult(invalidGroups, success);
    }

    public boolean addUsersToGroups(final JiraServiceContext jiraServiceContext, final Collection<String> groupsToJoin, final Collection<String> userNames)
    {
        if (groupsToJoin == null)
        {
            throw new IllegalArgumentException("You must specify non null groupsToJoin.");
        }

        if (userNames == null)
        {
            throw new IllegalArgumentException("You must specify non null userNames.");
        }

        validateJiraServiceContext(jiraServiceContext);

        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        if (!userHasAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.groups.must.be.admin"));
            return false;
        }

        // We only want to do the permission check validation, all other validation is assumed to have already occurred
        if (!getGroupNamesUserCanSee(jiraServiceContext.getLoggedInUser()).containsAll(groupsToJoin))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.cannot.join.user.groups.not.visible"));
            return false;
        }

        int errorCount = 0;
        boolean success = true;
        for (final String userName : userNames)
        {
            Collection<Group> groups = convertGroupNamesToGroups(groupsToJoin);
            for (Group group : groups)
            {
                try
                {
                    userUtil.addUserToGroup(group, getUser(userName));
                }
                catch (PermissionException e)
                {
                    if (errorCount < MAX_REPORTABLE_ERRORS)
                    {
                        jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.cannot.join.user.groups.no.permission", userName, group.getName()));
                        success = false;
                    }
                    errorCount++;
                }
                catch (AddException e)
                {
                    if (errorCount < MAX_REPORTABLE_ERRORS)
                    {
                        jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.cannot.join.user.groups.failed", userName, group.getName(), e.getMessage()));
                        success = false;
                    }
                    errorCount++;
                }
            }
        }
        if (errorCount >= MAX_REPORTABLE_ERRORS)
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.more.unreported.errors", String.valueOf(errorCount - MAX_REPORTABLE_ERRORS + 1)));
        }
        return success;
    }

    public boolean addGroupsToGroups(final JiraServiceContext jiraServiceContext, final Collection<String> groupsToJoin, final Collection<String> childNames)
    {
        if (groupsToJoin == null)
        {
            throw new IllegalArgumentException("You must specify non null groupsToJoin.");
        }

        if (childNames == null)
        {
            throw new IllegalArgumentException("You must specify non null childNames.");
        }

        validateJiraServiceContext(jiraServiceContext);

        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        if (!userHasAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.groups.must.be.admin"));
            return false;
        }

        // We only want to do the permission check validation, all other validation is assumed to have already occurred
        if (!getGroupNamesUserCanSee(jiraServiceContext.getLoggedInUser()).containsAll(groupsToJoin))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.cannot.join.group.groups.not.visible"));
            return false;
        }

        int errorCount = 0;
        boolean success = true;
        for (final String childName : childNames)
        {
            try
            {
                Group child = getGroup(childName);
                for (String parentName : groupsToJoin)
                {
                    Group parent = getGroup(parentName);
                    crowdService.addGroupToGroup(child, parent);
                }
            }
            catch (OperationNotPermittedException e)
            {
                if (errorCount < MAX_REPORTABLE_ERRORS)
                {
                    jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.cannot.join.group.groups.no.permission"));
                    success = false;
                }
                errorCount++;
            }
            catch (InvalidMembershipException e)
            {
                if (errorCount < MAX_REPORTABLE_ERRORS)
                {
                    jiraServiceContext.getErrorCollection().addErrorMessage(e.getMessage());
                    success = false;
                }
                errorCount++;
            }
        }
        if (errorCount >= MAX_REPORTABLE_ERRORS)
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.more.unreported.errors", String.valueOf(errorCount - MAX_REPORTABLE_ERRORS + 1)));
        }
        return success;
    }

    public boolean validateRemoveUserFromGroups(final JiraServiceContext jiraServiceContext, final List groupsToLeave, final String userName)
    {
        if (groupsToLeave == null)
        {
            throw new IllegalArgumentException("You must specify a non null groupsToLeave.");
        }

        final GroupRemoveChildMapper groupRemoveChildMapper = new GroupRemoveChildMapper(groupsToLeave);
        groupRemoveChildMapper.register(userName, groupsToLeave);

        return validateRemoveUsersFromGroups(jiraServiceContext, groupRemoveChildMapper);
    }

    public boolean validateRemoveUsersFromGroups(final JiraServiceContext jiraServiceContext, final GroupRemoveChildMapper mapper)
    {
        validateJiraServiceContext(jiraServiceContext);

        if (mapper == null)
        {
            throw new IllegalArgumentException("You must specify a non null mapper.");
        }

        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        if (!userHasAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.groups.must.be.admin"));
            return false;
        }

        // Validate that external user management is not enabled
        if (isExternalUserManagementEnabled())
        {
            String link = getContactAdminLink(i18n);
            errorCollection.addErrorMessage(i18n.getText("admin.bulkeditgroups.error.cannot.edit.user.groups.external.managment", link));
            return false;
        }

        boolean success = true;

        for (final Iterator iterator = mapper.childIterator(); iterator.hasNext();)
        {
            final String userName = (String) iterator.next();
            final User user = getUser(userName);

            //check that all the users to remove are valid users with valid groups
            if (isUserNull(user))
            {
                errorCollection.addErrorMessage(i18n.getText("admin.bulkeditgroups.error.removing.invalid.user", userName));
                success = false;
            }
            else
            {
                // If the user is mapped to remove all selected groups we need to check all groups at once
                // so we can add the appropriate error messages.
                if (mapper.isRemoveFromAllSelected(userName))
                {
                    success = validateCanRemoveUserFromGroups(jiraServiceContext, user, mapper.getDefaultGroupNames(), mapper.getDefaultGroupNames(),
                            true);
                }
                // Otherwise we need to run through and validate the groups one at a time so the correct error
                // messages are added
                else
                {
                    for (final Iterator groupIter = mapper.getGroupsIterator(userName); groupIter.hasNext();)
                    {
                        final String groupName = (String) groupIter.next();
                        success &= validateCanRemoveUserFromGroups(jiraServiceContext, user, mapper.getDefaultGroupNames(),
                                EasyList.build(groupName), false);
                    }
                }
            }
        }

        return success;
    }

    public boolean validateRemoveGroupsFromGroups(final JiraServiceContext jiraServiceContext, final GroupRemoveChildMapper mapper)
    {
        validateJiraServiceContext(jiraServiceContext);

        if (mapper == null)
        {
            throw new IllegalArgumentException("You must specify a non null mapper.");
        }

        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        if (!userHasAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.groups.must.be.admin"));
            return false;
        }

        // Validate that external user management is not enabled
        if (isExternalUserManagementEnabled())
        {
            String link = getContactAdminLink(i18n);
            errorCollection.addErrorMessage(i18n.getText("admin.editnestedgroups.error.cannot.edit.group.groups.external.managment", link));
            return false;
        }

        boolean success = true;

        for (final Iterator iterator = mapper.childIterator(); iterator.hasNext();)
        {
            final String childName = (String) iterator.next();
            final Group child = getGroup(childName);

            //check that all the users to remove are valid users with valid groups
            if (child == null)
            {
                errorCollection.addErrorMessage(i18n.getText("admin.errors.groups.group.does.not.exist", childName));
                success = false;
            }
            else
            {
                // If the user is mapped to remove all selected groups we need to check all groups at once
                // so we can add the appropriate error messages.
                if (mapper.isRemoveFromAllSelected(childName))
                {
                    success = validateCanRemoveGroupFromGroups(jiraServiceContext, child, mapper.getDefaultGroupNames(), mapper.getDefaultGroupNames(),
                        true);
                }
                // Otherwise we need to run through and validate the groups one at a time so the correct error
                // messages are added
                else
                {
                    for (final Iterator groupIter = mapper.getGroupsIterator(childName); groupIter.hasNext();)
                    {
                        final String groupName = (String) groupIter.next();
                        success &= validateCanRemoveGroupFromGroups(jiraServiceContext, child, mapper.getDefaultGroupNames(),
                            EasyList.build(groupName), false);
                    }
                }
            }
        }

        return success;
    }

    private String getContactAdminLink(I18nHelper i18n)
    {
        return jiraContactHelper.getAdministratorContactMessage(i18n);
    }

    public boolean removeUsersFromGroups(final JiraServiceContext jiraServiceContext, final GroupRemoveChildMapper mapper)
    {
        if (mapper == null)
        {
            throw new IllegalArgumentException("You must specify non null mapper.");
        }

        validateJiraServiceContext(jiraServiceContext);

        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        if (!userHasAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.groups.must.be.admin"));
            return false;
        }

        int errorCount = 0;
        boolean success = true;
        for (final Iterator iterator = mapper.childIterator(); iterator.hasNext();)
        {
            final String userName = (String) iterator.next();
            final Collection groupsToLeave = mapper.getGroups(userName);

            // Need to validate that they are not trying to leave groups they do not have permission to leave
            if (!getGroupNamesUserCanSee(jiraServiceContext.getLoggedInUser()).containsAll(groupsToLeave))
            {
                jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.cannot.leave.user.groups.not.visible"));
                success = false;
            }
            else
            {
                Collection<com.atlassian.crowd.embedded.api.Group> groups = convertGroupNamesToGroups(groupsToLeave);
                for (com.atlassian.crowd.embedded.api.Group group : groups)
                {
                    try
                    {
                        userUtil.removeUserFromGroup(group, getUser(userName));
                    }
                    catch (PermissionException e)
                    {
                        if (errorCount < MAX_REPORTABLE_ERRORS)
                        {
                            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.cannot.leave.user.groups.no.permission", userName, group.getName()));
                            success = false;
                        }
                        errorCount++;
                    }
                    catch (RemoveException e)
                    {
                        if (errorCount < MAX_REPORTABLE_ERRORS)
                        {
                            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.cannot.leave.user.groups.failed", userName, group.getName(), e.getMessage()));
                            success = false;
                        }
                        errorCount++;
                    }
                }
            }
        }
        if (errorCount >= MAX_REPORTABLE_ERRORS)
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.more.unreported.errors", String.valueOf(errorCount - MAX_REPORTABLE_ERRORS + 1)));
        }
        return success;
    }

    public boolean removeGroupsFromGroups(final JiraServiceContext jiraServiceContext, final GroupRemoveChildMapper mapper)
    {
        if (mapper == null)
        {
            throw new IllegalArgumentException("You must specify non null mapper.");
        }

        validateJiraServiceContext(jiraServiceContext);

        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        if (!userHasAdminPermission(jiraServiceContext.getLoggedInUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.groups.must.be.admin"));
            return false;
        }

        boolean success = true;
        for (final Iterator iterator = mapper.childIterator(); iterator.hasNext();)
        {
            final String childName = (String) iterator.next();
            final Collection<String> groupsToLeave = mapper.getGroups(childName);

            try
            {
                Group child = getGroup(childName);
                for (String parentName : groupsToLeave)
                {
                    Group parent = getGroup(parentName);
                    crowdService.removeGroupFromGroup(child, parent);
                }
            }
            catch (OperationNotPermittedException e)
            {
                jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.cannot.leave.group.groups.no.permission"));
                success = false;
            }
        }
        return success;
    }

    /**
     * This method is used to do the "meat" of the validation to see if a user can be removed from either
     * a group or a list of groups.
     *
     * @param jiraServiceContext the context containing user and an error collection
     * @param userToRemove the user to remove from the group(s)
     * @param allSelectedGroups the selected list of groups, this only really makes sense when doing a
     * bulk delete.
     * @param groupsToLeave are the groups to remove the user from.
     * @param isAll indicates if the user should be removed from the allSelectedGroups, also indicates
     * what type of error messages to show.
     * @return true if all the validation succeeds, false otherwise.
     */
    boolean validateCanRemoveUserFromGroups(final JiraServiceContext jiraServiceContext, final User userToRemove, final List allSelectedGroups, final List groupsToLeave, final boolean isAll)
    {
        final User currentUser = jiraServiceContext.getLoggedInUser();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();
        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        // First validate that all the groups exist
        if (!validateGroupNamesExist(groupsToLeave, errorCollection, i18n))
        {
            return false;
        }

        // We need validate that the current group is part of the selected groups if not removing from All selected groups
        if (!isAll && (allSelectedGroups != null) && !allSelectedGroups.containsAll(groupsToLeave))
        {
            errorCollection.addErrorMessage(i18n.getText("admin.bulkeditgroups.error.user.group.not.selected", userToRemove.getName(),
                (String) groupsToLeave.get(0)));
            return false;
        }

        // If we are removing the current user do validation to ensure we are not removing our administration rights
        if ((currentUser != null) && currentUser.equals(userToRemove) && areOnlyGroupsGrantingUserAdminPermissionsBulk(jiraServiceContext,
            groupsToLeave))
        {
            return false;
        }

        // Need to validate that they are not trying to leave groups they do not have permission to leave
        if (!getGroupNamesUserCanSee(currentUser).containsAll(groupsToLeave))
        {
            errorCollection.addErrorMessage(i18n.getText("admin.errors.cannot.leave.user.groups.not.visible"));
            return false;
        }

        // case where the user is not in the group(s)
        if (!isUserInGroups(userToRemove, new HashSet(groupsToLeave)))
        {
            // We need to know about the all flag so we can possibly give the right error message
            if (isAll)
            {
                if (groupsToLeave.size() == 1)
                {
                    errorCollection.addErrorMessage(i18n.getText("admin.bulkeditgroups.error.user.not.member.of.group", userToRemove.getName(),
                        (String) groupsToLeave.get(0)));
                }
                else
                {
                    errorCollection.addErrorMessage(i18n.getText("admin.bulkeditgroups.error.user.not.member.of.all", userToRemove.getName()));
                }
            }
            else
            {
                errorCollection.addErrorMessage(i18n.getText("admin.bulkeditgroups.error.user.not.member.of.group", userToRemove.getName(),
                    (String) groupsToLeave.get(0)));
            }
            return false;
        }

        return true;
    }


    /**
     * This method is used to do the "meat" of the validation to see if a child can be removed from either
     * a group or a list of groups.
     *
     * @param jiraServiceContext the context containing user and an error collection
     * @param childToRemove the group to remove from the group(s)
     * @param allSelectedGroups the selected list of groups, this only really makes sense when doing a
     * bulk delete.
     * @param groupsToLeave are the groups to remove the child from.
     * @param isAll indicates if the child should be removed from the allSelectedGroups, also indicates
     * what type of error messages to show.
     * @return true if all the validation succeeds, false otherwise.
     */
    boolean validateCanRemoveGroupFromGroups(final JiraServiceContext jiraServiceContext, final Group childToRemove, final List allSelectedGroups, final List groupsToLeave, final boolean isAll)
    {
        final User currentUser = jiraServiceContext.getLoggedInUser();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();
        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        // First validate that all the groups exist
        if (!validateGroupNamesExist(groupsToLeave, errorCollection, i18n))
        {
            return false;
        }

        // We need validate that the current group is part of the selected groups if not removing from All selected groups
        if (!isAll && (allSelectedGroups != null) && !allSelectedGroups.containsAll(groupsToLeave))
        {
            errorCollection.addErrorMessage(i18n.getText("admin.editnestedgroups.error.group.group.not.selected", childToRemove.getName(),
                (String) groupsToLeave.get(0)));
            return false;
        }

        // case where the user is not in the group(s)
        if (!isGroupInGroups(childToRemove, new HashSet(groupsToLeave)))
        {
            // We need to know about the all flag so we can possibly give the right error message
            if (isAll)
            {
                if (groupsToLeave.size() == 1)
                {
                    errorCollection.addErrorMessage(i18n.getText("admin.editnestedgroups.error.group.not.member.of.group", childToRemove.getName(),
                        (String) groupsToLeave.get(0)));
                }
                else
                {
                    errorCollection.addErrorMessage(i18n.getText("admin.editnestedgroups.error.group.not.member.of.all", childToRemove.getName()));
                }
            }
            else
            {
                errorCollection.addErrorMessage(i18n.getText("admin.editnestedgroups.error.group.not.member.of.group", childToRemove.getName(),
                    (String) groupsToLeave.get(0)));
            }
            return false;
        }
        // Validate that all the provided groupNames are not admin
        if (!validateGroupNamesNotAdmin(groupsToLeave, errorCollection, i18n))
        {
            return false;
        }

        return true;
    }

    public long getCommentsAndWorklogsGuardedByGroupCount(final String groupName)
    {
        if (groupName == null)
        {
            throw new IllegalArgumentException("The provided group name must not be null.");
        }

        return worklogManager.getCountForWorklogsRestrictedByGroup(groupName) + commentManager.getCountForCommentsRestrictedByGroup(groupName);
    }

    public boolean areOnlyGroupsGrantingUserAdminPermissions(final JiraServiceContext jiraServiceContext, final Collection groupNames)
    {
        return areOnlyGroupsGrantingUserAdminPermissions(jiraServiceContext, groupNames,
            "admin.errors.groups.cannot.delete.users.last.sys.admin.group", null, "admin.errors.groups.cannot.delete.users.last.admin.group", null);
    }

    boolean areOnlyGroupsGrantingUserAdminPermissionsBulk(final JiraServiceContext jiraServiceContext, final Collection groupNames)
    {
        final ApplicationUser currentUser = jiraServiceContext.getLoggedInApplicationUser();
        final Collection sysAdminMemberGroups = globalPermissionGroupAssociationUtil.getSysAdminMemberGroups(currentUser);
        final Collection adminMemberGroups = globalPermissionGroupAssociationUtil.getAdminMemberGroups(currentUser);
        final String sysAdminErrorMsg = "admin.errors.users.cannot.leave.sys.admin.groups";
        final String adminErrorMsg = "admin.errors.users.cannot.leave.admin.groups";
        return areOnlyGroupsGrantingUserAdminPermissions(jiraServiceContext, groupNames, sysAdminErrorMsg, sysAdminMemberGroups, adminErrorMsg,
            adminMemberGroups);
    }

    boolean areOnlyGroupsGrantingUserAdminPermissions(final JiraServiceContext jiraServiceContext, final Collection<String> groupNames, final String sysAdminErrorMessage, final Object sysAdminErrorParameters, final String adminErrorMessage, final Object adminErrorParameters)
    {
        validateJiraServiceContext(jiraServiceContext);

        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        if (globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, jiraServiceContext.getLoggedInApplicationUser()))
        {
            if (globalPermissionGroupAssociationUtil.isRemovingAllMySysAdminGroups(groupNames, jiraServiceContext.getLoggedInApplicationUser()))
            {
                if (sysAdminErrorParameters == null)
                {
                    jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText(sysAdminErrorMessage));
                }
                else
                {
                    jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText(sysAdminErrorMessage, sysAdminErrorParameters));
                }
                return true;
            }
        }
        else if (globalPermissionGroupAssociationUtil.isRemovingAllMyAdminGroups(groupNames, jiraServiceContext.getLoggedInApplicationUser()))
        {
            if (adminErrorParameters == null)
            {
                jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText(adminErrorMessage));
            }
            else
            {
                jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText(adminErrorMessage, adminErrorParameters));
            }
            return true;
        }
        return false;
    }

    public boolean isAdminDeletingSysAdminGroup(final JiraServiceContext jiraServiceContext, final String groupName)
    {
        validateJiraServiceContext(jiraServiceContext);

        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        // We only need to do this check if the user is not a SYS_ADMIN
        if (!globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, jiraServiceContext.getLoggedInUser()))
        {
            if (globalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN).contains(groupName))
            {
                jiraServiceContext.getErrorCollection().addErrorMessage(i18n.getText("admin.errors.groups.error.no.permission.to.remove.group"));
                return true;
            }
        }
        return false;
    }

    boolean userHasAdminPermission(final User user)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, user);
    }

    boolean validateUserIsNotInSelectedGroups(final JiraServiceContext jiraServiceContext, final Collection<String> selectedGroupsNames, final User user)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        // get the selected groups that the user is already in
        final Collection<String> groupsUserIsIn = CollectionUtils.intersection(getUserGroups(user), selectedGroupsNames);

        if ((selectedGroupsNames.size() == 1) && (groupsUserIsIn.size() == 1))
        {
            final String groupName = groupsUserIsIn.iterator().next();
            errorCollection.addErrorMessage(i18n.getText("admin.bulkeditgroups.error.user.already.member.of.group", user.getName(), groupName));
        }
        else if (selectedGroupsNames.size() == groupsUserIsIn.size())
        {
            errorCollection.addErrorMessage(i18n.getText("admin.bulkeditgroups.error.user.already.member.of.all", user.getName()));
        }

        if (errorCollection.hasAnyErrors())
        {
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }
        return true;
    }

    boolean validateGroupIsNotInSelectedGroups(final JiraServiceContext jiraServiceContext, final Collection<String> selectedGroupsNames, final Group group)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        // get the selected groups that the user is already in
        final Collection groupsUserIsIn = CollectionUtils.intersection(getParentGroupNames(group), selectedGroupsNames);

        if ((selectedGroupsNames.size() == 1) && (groupsUserIsIn.size() == 1))
        {
            final String groupName = (String) groupsUserIsIn.iterator().next();
            errorCollection.addErrorMessage(i18n.getText("admin.editnestedgroups.error.group.already.member.of.group", group.getName(), groupName));
        }
        else if (selectedGroupsNames.size() == groupsUserIsIn.size())
        {
            errorCollection.addErrorMessage(i18n.getText("admin.editnestedgroups.error.group.already.member.of.all", group.getName()));
        }

        if (errorCollection.hasAnyErrors())
        {
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }
        return true;
    }

    /**
     * Determines whether the given user is in all the groups with the given names or not.
     *
     * @param user       the user.
     * @param groupNames the names of the groups to check.
     * @return true only if the user is a member of all the groups.
     */
    boolean isUserInGroups(final User user, final Set<String> groupNames)
    {
        final Set<String> usersGroupNames = new HashSet<String>(getUserGroups(user));
        // if the subset of the given groupnames that are also groups the user is in is the same set as the
        // given groups, then the user is in all the given groups.
        return CollectionUtils.intersection(usersGroupNames, groupNames).size() == groupNames.size();
    }

    /**
     * Determines whether the given chilg group is in all the groups with the given names or not.
     *
     * @param child      the child group.
     * @param groupNames the names of the groups to check.
     * @return true only if the group is a member of all the groups.
     */
    boolean isGroupInGroups(final Group child, final Set<String> groupNames)
    {
        final Collection usersGroupNames = getParentGroupNames(child);
        // if the subset of the given groupnames that are also groups the user is in is the same set as the
        // given groups, then the user is in all the given groups.
        return CollectionUtils.intersection(usersGroupNames, groupNames).size() == groupNames.size();
    }

    boolean isExternalUserManagementEnabled()
    {
        // TODO: Would be good to give more thought to groups which are a bit of a special case because they can span multiple directories.
        return !ComponentAccessor.getUserManager().hasGroupWritableDirectory();
    }

    boolean validateGroupNamesExist(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
    {
        final ErrorCollection myErrorCollection = new SimpleErrorCollection();

        // Run through all the groups to make sure they exist
        for (final Object groupName1 : groupNames)
        {
            final String groupName = (String) groupName1;
            if (isGroupNull(groupName))
            {
                myErrorCollection.addErrorMessage(i18n.getText("admin.errors.groups.group.does.not.exist", groupName));
            }
        }

        if (myErrorCollection.hasAnyErrors())
        {
            errorCollection.addErrorCollection(myErrorCollection);
            return false;
        }
        return true;
    }

    boolean validateGroupNamesNotAdmin(final Collection groupNames, final ErrorCollection errorCollection, final I18nHelper i18n)
    {
        final ErrorCollection myErrorCollection = new SimpleErrorCollection();

        Set<String> dissallowedGroups = new HashSet<String>();
        dissallowedGroups.addAll(globalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN));
        dissallowedGroups.addAll(globalPermissionManager.getGroupNames(Permissions.ADMINISTER));
        // Run through all the groups to make sure they exist
        for (final Object groupName1 : groupNames)
        {
            final String groupName = (String) groupName1;
            if (dissallowedGroups.contains(groupName))
            {
                myErrorCollection.addErrorMessage(i18n.getText("admin.editnestedgroups.error.admin.group.not.supported", groupName));
            }
        }

        if (myErrorCollection.hasAnyErrors())
        {
            errorCollection.addErrorCollection(myErrorCollection);
            return false;
        }
        return true;
    }

    void updateCommentsAndWorklogs(final User user, final String groupName, final String swapGroup)
    {
        // Update all the comments
        commentManager.swapCommentGroupRestriction(groupName, swapGroup);

        // Update all the worklogs
        worklogManager.swapWorklogGroupRestriction(groupName, swapGroup);
    }

    void clearIssueSecurityLevelCache()
    {
        try
        {
            issueSecurityLevelManager.clearUsersLevels();
        }
        catch (final UnsupportedOperationException uoe)
        {
            log.debug("Unsupported operation was thrown when trying to clear the issue security level manager cache", uoe);
        }
    }

    /**
     * This method returns all the groups that the user has permission to see.
     * If the current user is not a {@link Permissions#SYSTEM_ADMIN} then this method will also filter out all
     * groups that are assigned to the SYSTEM_ADMIN global permission.
     *
     * @param currentUser the user executing the method.
     * @return a List of visible groupNames.
     */
    List getGroupNamesUserCanSee(final com.atlassian.crowd.embedded.api.User currentUser)
    {
        final List allGroupNames = getAllGroupNames();
        return globalPermissionGroupAssociationUtil.getGroupNamesModifiableByCurrentUser(currentUser, allGroupNames);
    }

    void validateJiraServiceContext(final JiraServiceContext jiraServiceContext)
    {
        if (jiraServiceContext == null)
        {
            throw new IllegalArgumentException("The JiraServiceContext must not be null.");
        }
        if (jiraServiceContext.getErrorCollection() == null)
        {
            throw new IllegalArgumentException("The error collection must not be null.");
        }
    }

    Collection<com.atlassian.crowd.embedded.api.Group> convertGroupNamesToGroups(final Collection<String> groupNames)
    {
        final Collection<com.atlassian.crowd.embedded.api.Group> groups = Lists.newArrayListWithCapacity(groupNames.size());
        for (final String groupName : groupNames)
        {
            groups.add(getGroup(groupName));
        }
        return groups;
    }

    /**
     * Tests to see if at least one of the groups specified has the global permissions to login a user,
     * i.e. USE, ADMINISTER or SYSTEM_ADMIN.
     *
     * @param groupNames the list of groups to check. The names are assumed to be valid group names.
     * @return True if one of the groups has any of these permissions. False otherwise.
     */
    boolean groupsHaveGlobalUsePermissions(final Collection /* <String> */groupNames)
    {

        final Set<String> groupNamesWithUsePermission = new HashSet<String>();
        for (final Integer permission : Permissions.getUsePermissions())
        {
            groupNamesWithUsePermission.addAll(globalPermissionManager.getGroupNames(permission.intValue()));
        }

        for (final Object groupName : groupNames)
        {
            final String group = (String) groupName;
            if (groupNamesWithUsePermission.contains(group))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public Collection<String> getChildGroupNames(com.atlassian.crowd.embedded.api.Group group)
    {
        final com.atlassian.crowd.search.query.membership.MembershipQuery<com.atlassian.crowd.embedded.api.Group> membershipQuery =
                QueryBuilder.queryFor(com.atlassian.crowd.embedded.api.Group.class, EntityDescriptor.group()).childrenOf(EntityDescriptor.group()).withName(group.getName()).returningAtMost(EntityQuery.ALL_RESULTS);

        final SortedSet<String> setOfGroups = new TreeSet<String>();

        Iterable<com.atlassian.crowd.embedded.api.Group> children = crowdService.search(membershipQuery);
        for (com.atlassian.crowd.embedded.api.Group child : children)
        {
            if (crowdService.isGroupDirectGroupMember(child, group))
            {
                setOfGroups.add(child.getName());
            }
        }
        return Collections.unmodifiableSortedSet(setOfGroups);
    }

    @Override
    public Collection<String> getParentGroupNames(com.atlassian.crowd.embedded.api.Group group)
    {
        final com.atlassian.crowd.search.query.membership.MembershipQuery<com.atlassian.crowd.embedded.api.Group> membershipQuery =
                QueryBuilder.queryFor(com.atlassian.crowd.embedded.api.Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.group()).withName(group.getName()).returningAtMost(EntityQuery.ALL_RESULTS);

        final SortedSet<String> setOfGroups = new TreeSet<String>();

        Iterable<com.atlassian.crowd.embedded.api.Group> parents = crowdService.search(membershipQuery);
        for (com.atlassian.crowd.embedded.api.Group parent : parents)
        {
            if (crowdService.isGroupDirectGroupMember(group, parent))
            {
                setOfGroups.add(parent.getName());
            }
        }
        return Collections.unmodifiableSortedSet(setOfGroups);
    }


    /// CLOVER:OFF
    boolean isUserInGroup(final String groupName, final User user)
    {
        return groupManager.isUserInGroup(user.getName(), groupName);
    }

    /// CLOVER:ON

    /// CLOVER:OFF
    boolean isUserNull(final User user)
    {
        return user == null;
    }

    /// CLOVER:ON

    ///CLOVER:OFF
    List<String> getAllGroupNames()
    {
        return new ArrayList(CollectionUtils.collect(getAllGroups(), GlobalPermissionGroupAssociationUtil.GROUP_TO_GROUPNAME));
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    Collection<String> getUserGroups(final User user)
    {
        return groupManager.getGroupNamesForUser(user);
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    Collection<Group> getAllGroups()
    {
        return groupManager.getAllGroups();
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    boolean isGroupNull(final String groupName)
    {
        return (groupName == null) || (getGroup(groupName) == null);
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    void removeGroup(final Group group) throws PermissionException
    {
        try
        {
            crowdService.removeGroup(group);
        }
        catch (OperationNotPermittedException e)
        {
            throw new PermissionException(e);
        }
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    Group getGroup(final String groupName)
    {
        return crowdService.getGroup(groupName);
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    User getUser(final String userName)
    {
        return userUtil.getUser(userName);
    }
    ///CLOVER:ON
}
