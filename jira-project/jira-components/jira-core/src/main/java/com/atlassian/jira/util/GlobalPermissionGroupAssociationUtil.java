package com.atlassian.jira.util;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This is a utility class to find out information about {@link Permissions#ADMINISTER} and
 * {@link Permissions#SYSTEM_ADMIN} global permission information. You can use this class to find out
 * if you are removing all the groups that grant a provided user the permission. You can also use
 * this to find out the groups that the provided user is a member of and which is associated with
 * the global permission.
 *
 * @since 3.12
 */
public class GlobalPermissionGroupAssociationUtil
{
    /**
     * Transforms a {@link Group} to its name as a String.
     */
    public static final Transformer GROUP_TO_GROUPNAME = new Transformer() {

        public Object transform(Object object)
        {
            return ((Group) object).getName();
        }
    };

    private final GlobalPermissionManager globalPermissionManager;
    private final GroupManager groupManager;

    public GlobalPermissionGroupAssociationUtil(GlobalPermissionManager globalPermissionManager, GroupManager groupManager)
    {
        this.globalPermissionManager = globalPermissionManager;
        this.groupManager = groupManager;
    }

    /**
     * Return true if the user is trying to remove all the groups that grant them the system administration permission.
     *
     * @param groupsToLeave a Collection of {@link String}, group names, that the user is trying to
     * unassociate/remove.
     * @param user performing this operation.
     *
     * @return true if removing all groups that grant the privlage, false otherwise.
     */
    public boolean isRemovingAllMySysAdminGroups(Collection<String> groupsToLeave, ApplicationUser user)
    {
        Collection<String> sysAdminGroups = getSysAdminMemberGroups(user);
        sysAdminGroups.removeAll(groupsToLeave);
        return sysAdminGroups.isEmpty();
    }

    /**
     * Returns All the groupNames that have global "System Administration" permission that this user is a member of.
     *
     * @param user the user performing this operation.
     * @return a Collection of {@link String} group names that the global System Admin permission is associated
     * with and which the user is in.
     */
    public Collection<String> getSysAdminMemberGroups(ApplicationUser user)
    {
        return getMemberGroupNames(user, Permissions.SYSTEM_ADMIN);
    }

    /**
     * Return true if the user is trying to remove all the groups that grant them the administration permission.
     *
     * @param groupsToLeave a Collection of {@link String}, group names, that the user is trying to
     * unassociate/remove.
     * @param user performing this operation.
     *
     * @return true if removing all groups that grant the privlage, false otherwise.
     */
    public boolean isRemovingAllMyAdminGroups(Collection<String> groupsToLeave, ApplicationUser user)
    {
        Collection<String> startingAdminGroups = getAdminMemberGroups(user);
        startingAdminGroups.removeAll(groupsToLeave);

        return startingAdminGroups.isEmpty();
    }

    /**
     * Returns All the groupNames that have global "Administration" permission that this user is a member of.
     *
     * @param user performing this operation.
     * @return a Collection of {@link String} group names that the global Admin permission is associated
     * with and which the user is in.
     */
    public Collection<String> getAdminMemberGroups(ApplicationUser user)
    {
        return getMemberGroupNames(user, Permissions.ADMINISTER);
    }

    /**
     * Determines, based on the users permissions, if the group can be deleted.
     *
     * @param user performing this operation.
     * @param groupName the group to delete
     * @return true if the user is has the {@link Permissions#SYSTEM_ADMIN} permission or if
     * the group is not associated with the {@link com.atlassian.jira.security.Permissions#SYSTEM_ADMIN} permission.
     */
    public boolean isUserAbleToDeleteGroup(User user, String groupName)
    {
        return globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user) ||
               !globalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN).contains(groupName);
    }

    /**
     * Determines which groups will be visible to the current user. If the user is a {@link Permissions#SYSTEM_ADMIN}
     * then they can see all the groups, otherwise they will not be able to see the group names associated with
     * the {@link Permissions#SYSTEM_ADMIN} permission.
     *
     * @param currentUser performing the operation
     * @param groupNames the full set of possible group names the user might see
     * @return the groupNames list if they user has {@link com.atlassian.jira.security.Permissions#SYSTEM_ADMIN}
     * rights, otherwise a collection that does not contain the SYS_ADMIN group names.
     */
    public List<String> getGroupNamesModifiableByCurrentUser(User currentUser, Collection<String> groupNames )
    {
        List<String> visibleGroups = (groupNames != null) ? new ArrayList<String>(groupNames) : new ArrayList<String>();

        if (!globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser))
        {
            Collection<String> sysAdminGroupNames = globalPermissionManager.getGroupNames(Permissions.SYSTEM_ADMIN);
            if (sysAdminGroupNames != null)
            {
                visibleGroups.removeAll(sysAdminGroupNames);
            }
        }
        Collections.sort(visibleGroups);
        return visibleGroups;
    }

    /**
     * Determines which groups will be visible to the current user. If the user is a {@link Permissions#SYSTEM_ADMIN}
     * then they can see all the groups, otherwise they will not be able to see the groups associated with
     * the {@link Permissions#SYSTEM_ADMIN} permission.
     *
     * @param currentUser performing the operation
     * @param groups the full set of possible groups the user might see
     * @return the {@link Group groups} list if they user has {@link com.atlassian.jira.security.Permissions#SYSTEM_ADMIN}
     * rights, otherwise a collection that does not contain the SYS_ADMIN groups.
     */
    public List<Group> getGroupsModifiableByCurrentUser(User currentUser, List<Group> groups)
    {
        List<Group> visibleGroups = (groups != null) ? new ArrayList<Group>(groups) : new ArrayList<Group>();
        if (!globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser))
        {
            Collection<Group> sysAdminGroups = globalPermissionManager.getGroupsWithPermission(Permissions.SYSTEM_ADMIN);
            if (sysAdminGroups != null)
            {
                visibleGroups.removeAll(sysAdminGroups);
            }
        }
        Collections.sort(visibleGroups);
        return visibleGroups;
    }

    /**
     * Get all groups which have neither {@link Permissions#SYSTEM_ADMIN} or {@link Permissions#ADMINISTER}
     *
     * @param groups the full set of possible groups the user might see
     * @return the {@link Group groups} list if they user has {@link com.atlassian.jira.security.Permissions#SYSTEM_ADMIN}
     * rights, otherwise a collection that does not contain the SYS_ADMIN groups.
     */
    public List<Group> getNonAdminGroups(List<Group> groups)
    {
        List<Group> visibleGroups = (groups != null) ? new ArrayList<Group>(groups) : new ArrayList<Group>();
        Collection<Group> sysAdminGroups = globalPermissionManager.getGroupsWithPermission(Permissions.SYSTEM_ADMIN);
        if (sysAdminGroups != null)
        {
            visibleGroups.removeAll(sysAdminGroups);
        }

        Collection<Group> adminGroups = globalPermissionManager.getGroupsWithPermission(Permissions.ADMINISTER);
        if (sysAdminGroups != null)
        {
            visibleGroups.removeAll(adminGroups);
        }

        Collections.sort(visibleGroups);
        return visibleGroups;
    }

    Collection<String> getMemberGroupNames(ApplicationUser user, int permissionType)
    {
        Collection<String> permissionTypeGroups = globalPermissionManager.getGroupNames(permissionType);
        Collection<String> userGroups = groupManager.getGroupNamesForUser(user);

        if (userGroups == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return CollectionUtils.intersection(userGroups, permissionTypeGroups);
        }
    }
}
