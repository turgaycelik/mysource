/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.fugue.Option;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.security.GlobalPermissionEntry;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.GlobalPermissionGroupAssociationUtil;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.opensymphony.util.TextUtils;

import org.apache.commons.lang.StringUtils;

@SuppressWarnings ("UnusedDeclaration")
@WebSudoRequired
public class GlobalPermissions extends ProjectActionSupport
{
    private static final class Actions
    {
        private static final String VIEW = "view";
        private static final String ADD = "add";
        private static final String DEL = "del";
        private static final String DELETE = "delete";
        private static final String CONFIRM = "confirm";
    }

    private Map<String, String> globalPermTypes;
    private String groupName;
    private String globalPermType = "";
    private String action = Actions.VIEW;
    private final GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil;

    private final GlobalPermissionManager globalPermissionManager;
    private final UserUtil userUtil;
    private final GroupManager groupManager;

    public GlobalPermissions(GlobalPermissionManager globalPermissionManager, GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil, UserUtil userUtil, GroupManager groupManager)
    {
        super();
        this.globalPermissionGroupAssociationUtil = globalPermissionGroupAssociationUtil;
        this.globalPermissionManager = globalPermissionManager;
        this.userUtil = userUtil;
        this.groupManager = groupManager;
    }

    @Override
    public String doDefault() throws Exception
    {
        return SUCCESS;
    }

    public void doValidation()
    {
        if (StringUtils.isNotBlank(globalPermType))
        {
            Option<GlobalPermissionType> globalPermissionOpt = globalPermissionManager.getGlobalPermission(globalPermType);
            if (globalPermissionOpt.isEmpty())
            {
                addError("globalPermType", getText("admin.errors.permissions.inexistent.permission"));
            }
            else
            {
                GlobalPermissionType globalPermissionType = globalPermissionOpt.get();
                if (groupName != null)
                {
                    //check if the group exists unless we're trying to remove a group from a global permission!
                    if (!Actions.DEL.equals(action) && !Actions.CONFIRM.equals(action))
                    {
                        Group group = groupManager.getGroup(groupName);
                        if (group == null)
                        {
                            addError("groupName", getText("admin.errors.permissions.inexistent.group", "'" + groupName + "'"));
                        }
                        // JRA-22984 Prevent admin groups from being added to JIRA USE permisson
                        if (globalPermType.equals(GlobalPermissionKey.USE.getKey()) && getAdministrativeGroups().contains(group))
                        {
                            addError("groupName", getText("admin.errors.permissions.group.notallowed.for.permission", groupName, getText(globalPermissionType.getNameI18nKey())));
                        }
                    }
                    else
                    {
                        //check that the group we're trying to remove is part of one of the permissions
                        final Collection<String> groupNames = globalPermissionManager.getGroupNamesWithPermission(globalPermissionType.getGlobalPermissionKey());
                        if(!groupNames.contains(groupName))
                        {
                            addErrorMessage(getText("admin.errors.permissions.delete.group.not.in.permission", groupName, getText(globalPermissionType.getNameI18nKey())));
                        }
                    }
                }
                else
                {
                    if(!globalPermissionType.isAnonymousAllowed() && Actions.ADD.equals(action)) {
                        addError("groupName", getText("admin.errors.permissions.group.notallowed.for.permission", getText("admin.common.words.anyone"), getText(globalPermissionType.getNameI18nKey())));
                    }
                }
            }

            // If the user is trying to add a hidden perm they should not be allowed to do so
            validateAdd();

            // If the user is deleting then we need to make sure they do not remove themselves from admin groups
            validateDelete();
        }
        else
        {
            if (Actions.ADD.equals(action))
            {
                addError("globalPermType", getText("admin.errors.permissions.must.select.permission"));
            }
        }

        super.doValidation();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (StringUtils.isNotBlank(globalPermType))
        {
            Option<GlobalPermissionType> globalPermissionOpt = globalPermissionManager.getGlobalPermission(globalPermType);
            if(globalPermissionOpt.isDefined())
            {
                GlobalPermissionType globalPermissionType = globalPermissionOpt.get();
                if (Actions.DEL.equals(action))
                {
                    removePermission(globalPermissionType, groupName);
                    action = Actions.VIEW;
                    return getPermissionRedirect();
                }
                else if (Actions.CONFIRM.equals(action))
                {
                    return "confirm";
                }
                else if (Actions.ADD.equals(action))
                {
                    final Group group = (groupName == null ? null : groupManager.getGroup(groupName));
                    createPermission(globalPermissionType, group);
                    return getPermissionRedirect();
                }
            }
        }

        return getResult();
    }

    private void validateDelete()
    {
        if (Actions.DEL.equals(action) || Actions.CONFIRM.equals(action))
        {
            // if we're deleting an admin permission, check that another group exists
            if (globalPermType.equals(GlobalPermissionKey.ADMINISTER.getKey()))
            {
                boolean removingAllPerms = globalPermissionGroupAssociationUtil.isRemovingAllMyAdminGroups(Arrays.asList(groupName), getLoggedInApplicationUser())
                                           && !globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, getLoggedInApplicationUser());

                if (removingAllPerms)
                {
                    addErrorMessage(getText("admin.errors.permissions.no.permission"));
                }
            }
            else if (globalPermType.equals(GlobalPermissionKey.SYSTEM_ADMIN.getKey()))
            {
                if (!globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, getLoggedInApplicationUser()))
                {
                    addErrorMessage(getText("admin.errors.permissions.no.permission.sys.admin.only"));
                }
                else if (globalPermissionGroupAssociationUtil.isRemovingAllMySysAdminGroups(Arrays.asList(groupName), getLoggedInApplicationUser()))
                {
                    addErrorMessage(getText("admin.errors.permissions.no.permission.sys.admin"));
                }
            }
        }
    }

    private void validateAdd()
    {
        if (Actions.ADD.equals(action))
        {
            if (!getGlobalPermTypes().containsKey(globalPermType))
            {
                addErrorMessage(getText("admin.errors.permissions.not.have.permission.to.add"));
            }
        }
    }

    private String getPermissionRedirect() throws Exception
    {
        return getRedirect("GlobalPermissions!default.jspa");
    }

    private void createPermission(GlobalPermissionType globalPermissionType, Group group)
    {
        String groupName = (group == null ? null : group.getName());
        // We need to get all the groupNames for the permission type and see if the list contains this groupName
        if (!globalPermissionManager.getGroupNamesWithPermission(globalPermissionType.getGlobalPermissionKey()).contains(groupName))
        {
            globalPermissionManager.addPermission(globalPermissionType, groupName);
        }

    }

    private void removePermission(GlobalPermissionType globalPermissionType, String groupName)
    {
        final Group group = (groupName == null ? null : groupManager.getGroup(groupName));

        String groupToDelete = null;
        if(group != null)
        {
            groupToDelete = group.getName();
        }
        else if(groupName != null)
        {
            //JRA-15911: Check if by chance the group returned was null but we did have a groupName
            groupToDelete = groupName;
        }
        globalPermissionManager.removePermission(globalPermissionType, groupToDelete);
    }

    public Collection<GlobalPermissionEntry> getPermissionGroups(String globalPermType)
    {
        return globalPermissionManager.getPermissions(GlobalPermissionKey.of(globalPermType));
    }

    public Collection getGroups()
    {
        return groupManager.getAllGroups();
    }

    public String getGlobalPermType()
    {
        return globalPermType;
    }

    public String getPermTypeName()
    {
        return globalPermissionManager.getGlobalPermission(globalPermType).fold(
                returnUnknownString,
                new Function<GlobalPermissionType, String>()
                {
                    @Override
                    public String apply(final GlobalPermissionType globalPermissionType)
                    {
                        return getText(globalPermissionType.getNameI18nKey());
                    }
                }
        );
    }

    public void setGlobalPermType(String globalPermType)
    {
        this.globalPermType = globalPermType;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        if (TextUtils.stringSet(groupName))
        {
            this.groupName = groupName;
        }
        else
        {
            this.groupName = null;
        }
    }

    public void setAction(String action)
    {
        if (Actions.DEL.equalsIgnoreCase(action) || Actions.DELETE.equalsIgnoreCase(action))
        {
            this.action = Actions.DEL;
        }
        else if (Actions.CONFIRM.equalsIgnoreCase(action))
        {
            this.action = Actions.CONFIRM;
        }
        else
        {
            this.action = Actions.ADD;
        }
    }

    public boolean isConfirm()
    {
        return Actions.CONFIRM.equalsIgnoreCase(action);
    }

    public Map<String, String> getGlobalPermTypes()
    {
        if (globalPermTypes == null)
        {
            globalPermTypes = new LinkedHashMap<String, String>();

            // Only show the SYS_ADMIN perm to those who have it. We know this permission exists.
            if (globalPermissionManager.hasPermission(GlobalPermissionKey.SYSTEM_ADMIN, getLoggedInApplicationUser()))
            {
                GlobalPermissionType sysAdminPermission = globalPermissionManager.getGlobalPermission(GlobalPermissionKey.SYSTEM_ADMIN).get();
                globalPermTypes.put(GlobalPermissionKey.SYSTEM_ADMIN.getKey(), getText(sysAdminPermission.getNameI18nKey()));
            }
            permissionMapHelper(GlobalPermissionKey.ADMINISTER, globalPermTypes);
            permissionMapHelper(GlobalPermissionKey.USE, globalPermTypes);
            permissionMapHelper(GlobalPermissionKey.USER_PICKER, globalPermTypes);
            permissionMapHelper(GlobalPermissionKey.CREATE_SHARED_OBJECTS, globalPermTypes);
            permissionMapHelper(GlobalPermissionKey.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, globalPermTypes);
            permissionMapHelper(GlobalPermissionKey.BULK_CHANGE, globalPermTypes);

            // The above list is ordered by order of insertion. We want the plugin global permissions to be inserted after.
            for (GlobalPermissionType globalPermissionType : globalPermissionManager.getAllGlobalPermissions())
            {
                // Only add things that aren't already in the map
                if(!globalPermTypes.containsKey(globalPermissionType.getKey()) && !GlobalPermissionKey.SYSTEM_ADMIN.equals(globalPermissionType.getGlobalPermissionKey()))
                {
                    globalPermTypes.put(globalPermissionType.getKey(), getText(globalPermissionType.getNameI18nKey()));
                }
            }
        }

        return globalPermTypes;
    }

    private void permissionMapHelper(GlobalPermissionKey permissionKey, Map<String, String> map)
    {
        Option<GlobalPermissionType> permissionOpt = globalPermissionManager.getGlobalPermission(permissionKey);
        if(permissionOpt.isDefined())
        {
            GlobalPermissionType permission = permissionOpt.get();
            map.put(permission.getKey(), getText(permission.getNameI18nKey()));
        }
    }

    public String getDescription(String permType)
    {
        return globalPermissionManager.getGlobalPermission(permType).fold(
            returnUnknownString,
            new Function<GlobalPermissionType, String>()
            {
                @Override
                public String apply(final GlobalPermissionType globalPermissionType)
                {
                    return getText(globalPermissionType.getDescriptionI18nKey());
                }
            }
        );
    }

    public boolean hasExceededUserLimit()
    {
        return userUtil.hasExceededUserLimit();
    }

    private Collection<Group> getAdministrativeGroups()
    {
        final Collection<Group> groups = new ArrayList<Group>(globalPermissionManager.getGroupsWithPermission(GlobalPermissionKey.ADMINISTER));
        groups.addAll(globalPermissionManager.getGroupsWithPermission(GlobalPermissionKey.SYSTEM_ADMIN));
        return Collections.unmodifiableCollection(groups);
    }

    private final Supplier<String> returnUnknownString = new Supplier<String>()
    {
        @Override
        public String get()
        {
            return getText("common.words.unknown");
        }
    };
}
