package com.atlassian.jira.bc.group;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.JiraServiceContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * GroupService contains methods for managing {@link Group}'s in JIRA.
 *
 * @since v3.12
 */
@PublicApi
public interface GroupService
{
    /**
     * Validates if the group provided can be deleted in JIRA. If there are any problems deleting the group
     * they will be reported in the error collection and the method will return false. Comment and worklogs 
     * visibility can be restricted by groups. If there are any associated comments or worklogs and a swap
     * group is not provided then errors will be added to the error collection.
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param groupName identifies the group to delete.
     * @param swapGroup identifies the group to change comment and worklog visibility to.
     * @return true if you can delete the group, false otherwise.
     *
     * @since v3.12
     */
    boolean validateDelete(JiraServiceContext jiraServiceContext, String groupName, String swapGroup);

    /**
     * This will delete a group from JIRA. This method will remove the group from any notifications schemes,
     * any associated permissions, and any associated project roles. This method will also update any
     * {@link com.atlassian.jira.issue.comments.Comment}'s and {@link com.atlassian.jira.issue.worklog.Worklog}'s
     * that have visibility restrictions set to the current group such that their restrictions will be changed to
     * the swapGroup.
     *
     * This method assumes that the {@link #validateDelete(com.atlassian.jira.bc.JiraServiceContext, String, String)}
     * method has been called and that it did not generate any errors.
     *
     * You must have {@link com.atlassian.jira.security.Permissions#ADMINISTER} permissions or higher to invoke this
     * method.
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param groupName identifies the group to delete.
     * @param swapGroup identifies the group to change comment and worklog visibility to.
     * @return true if the operation succeeds, false otherwise.
     * @since v3.12
     */
    boolean delete(JiraServiceContext jiraServiceContext, String groupName, String swapGroup);

    /**
     * Performs validation to see if the provided user (identified by username) can be added to the provided group
     * by the current user (as specified in the jiraServiceContext). If there are any problems with adding the user to
     * the groups this method will return false and the error will be reported in the errorCollection.
     *
     * The operation will not be valid if external user management is enabled, the groups or user does
     * not exist, the user is already a member of all the groups, or the current user does not have permission
     * to add the user to the groups.
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param groupsToJoin a collection of {@link String}'s that represent groupNames that the user should be added to.
     * This must not be null.
     * @param userName identifies the user to be added to the groups, must not be null.
     * @return true if you can successfully add the user to the groups, false otherwise.
     *
     * @since v3.12
     */
    boolean validateAddUserToGroup(JiraServiceContext jiraServiceContext, Collection groupsToJoin, String userName);

    /**
     * Performs validation to see if the provided users (identified by the userNames collection) can be added to the
     * provided group by the current user (as specified in the jiraServiceContext). If there are any problems with
     * adding the users to the groups this method will returns an unsuccessful result and the errors will be reported
     * in the errorCollection.
     *
     * The operation will not be valid if external user management is enabled, the groups or user does
     * not exist, the user is already a member of the groups, or the current user does not have permission
     * to add the user to the groups.
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param groupsToJoin a collection of {@link String}'s that represent groupNames that the user should be added to.
     * This must not be null.
     * @param userNames collection of {@link String} userNames that identifies the users to be added to the groups,
     * must not be null.
     * @return a {@link com.atlassian.jira.bc.group.GroupService.BulkEditGroupValidationResult} which if the users
     * can be added to the groups will have isSuccess be true. Otherwise isSuccess will be false and if there was
     * any problem validating the users to add the
     * {@link com.atlassian.jira.bc.group.GroupService.BulkEditGroupValidationResult#getInvalidChildren()} method will
     * contain the usernames that were not valid to add to the groups.
     *
     * @since v3.12
     */
    BulkEditGroupValidationResult validateAddUsersToGroup(JiraServiceContext jiraServiceContext, Collection groupsToJoin, Collection userNames);

    /**
     * Performs validation to see if the provided groups (identified by the groupNames collection) can be added to the
     * provided group by the current user (as specified in the jiraServiceContext). If there are any problems with
     * adding the users to the groups this method will returns an unsuccessful result and the errors will be reported
     * in the errorCollection.
     *
     * The operation will not be valid if external user management is enabled, the parent or child groups do
     * not exist, the child is already a member of the groups, or the current user does not have permission
     * to add the group to the groups.
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param groupsToJoin a collection of {@link String}'s that represent groupNames that the user should be added to.
     * This must not be null.
     * @param groupNames collection of {@link String} groupNames that identifies the groups to be added to the groups,
     * must not be null.
     * @return a {@link com.atlassian.jira.bc.group.GroupService.BulkEditGroupValidationResult} which if the groups
     * can be added to the groups will have isSuccess be true. Otherwise isSuccess will be false and if there was
     * any problem validating the users to add the
     * {@link com.atlassian.jira.bc.group.GroupService.BulkEditGroupValidationResult#getInvalidChildren()} method will
     * contain the groups that were not valid to add to the groups.
     *
     * @since v4.3
     */
    BulkEditGroupValidationResult validateAddGroupsToGroup(JiraServiceContext jiraServiceContext, Collection<String> groupsToJoin, Collection<String> groupNames);

    /**
     * This method will add the provided users to the specified groups. It is assumed that either
     * {@link #validateAddUsersToGroup(com.atlassian.jira.bc.JiraServiceContext, java.util.Collection, java.util.Collection)}
     * or {@link #validateAddUserToGroup(com.atlassian.jira.bc.JiraServiceContext, java.util.Collection, String)} has
     * been called and not returned any errors. This method will not perform any validation other than simple
     * permissions checks.
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param groupsToJoin a collection of {@link String}'s that represent groupNames that the user should be added to.
     * This must not be null.
     * @param userNames collection of {@link String} userNames that identifies the users to be added to the groups,
     * must not be null.
     * @return true if the operation succeeds, false otherwise.
     */
    boolean addUsersToGroups(JiraServiceContext jiraServiceContext, Collection<String> groupsToJoin, Collection<String> userNames);

    /**
     * This method will add the provided groups to the specified groups. It is assumed that
     * {@link #validateAddGroupsToGroup(com.atlassian.jira.bc.JiraServiceContext, java.util.Collection, java.util.Collection)}
     * has been called and not returned any errors. This method will not perform any validation other than simple
     * permissions checks.
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param groupsToJoin a collection of {@link String}'s that represent groupNames that the child group should be added to.
     * This must not be null.
     * @param childNames collection of {@link String} childNames that identifies the child groups to be added to the groups,
     * must not be null.
     * @return true if the operation succeeds, false otherwise.
     */
    boolean addGroupsToGroups(JiraServiceContext jiraServiceContext, Collection<String> groupsToJoin, Collection<String> childNames);

    /**
     * Performs validation to see if the users identified in {@link GroupRemoveChildMapper mapper} can be removed from
     * their respective groups by the current user (as specified in the jiraServiceContext). If there are any problems with
     * removing the users from the groups this method will return false and the errors will be reported in the errorCollection.
     *
     * The operation will not be valid if external user management is enabled, the groups or users do
     * not exist, the user is not currently a member of the groups, or the current user does not have permission
     * to remove the users from the groups.
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param mapper represents which users to remove from which groups.
     * @return true only if the operation is valid.
     *
     * @since v4.3
     */
    boolean validateRemoveUsersFromGroups(JiraServiceContext jiraServiceContext, GroupRemoveChildMapper mapper);

    /**
     * Performs validation to see if the groups identified in {@link GroupRemoveChildMapper mapper} can be removed from
     * their respective groups by the current user (as specified in the jiraServiceContext). If there are any problems with
     * removing the groups from the groups this method will return false and the errors will be reported in the errorCollection.
     *
     * The operation will not be valid if external user management is enabled, the parent or child groups do
     * not exist, the group is not currently a member of the groups, or the current user does not have permission
     * to remove the groups from the groups.
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param mapper represents which groups to remove from which groups.
     * @return true only if the operation is valid.
     *
     * @since v3.12
     */
    boolean validateRemoveGroupsFromGroups(JiraServiceContext jiraServiceContext, GroupRemoveChildMapper mapper);

    /**
     * Performs validation to see if the user can be removed from the groups by the current user (as specified in
     * the jiraServiceContext). If there are any problems with removing the user from the groups this method will
     * return false and the errors will be reported in the errorCollection.
     *
     * The operation will not be valid if external user management is enabled, the groups or user does
     * not exist, the user is not currently a member of the groups, or the current user does not have permission
     * to remove the user from the groups.
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param groupsToLeave the group names to remove the user from.
     * @param userName the name of the user to remove from the groupsToLeave.
     * @return true only if the operation is valid.
     *
     * @since v3.12
     */
    boolean validateRemoveUserFromGroups(JiraServiceContext jiraServiceContext, List groupsToLeave, String userName);

    /**
     * This method will remove the provided users from the specified groups. It is assumed that either
     * {@link #validateRemoveUsersFromGroups(com.atlassian.jira.bc.JiraServiceContext, GroupRemoveChildMapper)}
     * or {@link #validateRemoveUserFromGroups(com.atlassian.jira.bc.JiraServiceContext, java.util.List, String)}
     * been called and not returned any errors. This method will not perform any validation other than simple
     * permissions checks.
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param mapper represents which users to remove from which groups.
     * @return true if the operation succeeds, false otherwise.
     */
    boolean removeUsersFromGroups(JiraServiceContext jiraServiceContext, GroupRemoveChildMapper mapper);

    /**
     * This method will remove the provided child groups from the specified groups. It is assumed that
     * {@link #validateRemoveGroupsFromGroups(com.atlassian.jira.bc.JiraServiceContext, GroupRemoveChildMapper)}
     * has been called and not returned any errors. This method will not perform any validation other than simple
     * permissions checks.
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param mapper represents which child groups to remove from which groups.
     * @return true if the operation succeeds, false otherwise.
     */
    boolean removeGroupsFromGroups(JiraServiceContext jiraServiceContext, GroupRemoveChildMapper mapper);

    /**
     * This method will return the count of all {@link com.atlassian.jira.issue.comments.Comment}'s and
     * {@link com.atlassian.jira.issue.worklog.Worklog}'s that have the named group set as its
     * visibility restriction.
     *
     * @param groupName identifies the group that the worklog or comments visibility is restricted by.
     * @return the number of comments and worklogs that have their visibility restricted by
     * the named group.
     * @since v3.12
     */
    long getCommentsAndWorklogsGuardedByGroupCount(String groupName);

    /**
     * This is a validation utility method that will determine if the specified groups are the only groups that are
     * granting the current user their {@link com.atlassian.jira.security.Permissions#ADMINISTER} permissions.
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param groupNames identifies the groups in question.
     * @return true if the specified groups are the only ones granting the administrator rights, false otherwise.
     *
     * @since v3.12
     */
    boolean areOnlyGroupsGrantingUserAdminPermissions(JiraServiceContext jiraServiceContext, Collection groupNames);

    /**
     * This is a validation utility method that will determine if the current user is only a JIRA Administrator
     * and they are trying to delete a group that is associated with JIRA System Administrators.
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param groupName identifies the group in question.
     * @return true if the current user is a JIRA Administrator and they are trying to delete a group that is
     * associated with JIRA System Administrators, false otherwise.
     *
     * @since v3.12
     */
    boolean isAdminDeletingSysAdminGroup(JiraServiceContext jiraServiceContext, String groupName);

    /**
     * Return the name of groups that are members of this Group.
     * @param group to search for.
     * @return names of child Groups
     */
    Collection<String> getChildGroupNames(Group group);

    /**
     * Return the name of groups that are parents of this Group.
     * @param group to search for.
     * @return names of parent Groups
     */
    Collection<String> getParentGroupNames(Group group);

    /**
     * This class is used for a return type for edit group validation. If there is a problem with validation
     * that is caused by invalid users then the list of invalid users will be contained in this result.
     *
     * @since v3.12
     */
    @PublicApi
    public class BulkEditGroupValidationResult
    {
        private final Collection invalidChildren;
        private final boolean success;

        public BulkEditGroupValidationResult(Collection usersAlreadyInAGroup, boolean success)
        {
            this.invalidChildren = usersAlreadyInAGroup;
            this.success = success;
        }

        public BulkEditGroupValidationResult(boolean success)
        {
            this(Collections.EMPTY_LIST, success);
        }

        /**
         * Returns the users who caused the result to be a failure.
         *
         * @return a collection of {@link String} userNames.
         * @since v3.12
         */
        public Collection getInvalidChildren()
        {
            return invalidChildren;
        }

        /**
         * @return true if the validation is a success, false otherwise
         *
         * @since v3.12
         */
        public boolean isSuccess()
        {
            return success;
        }
    }
}
