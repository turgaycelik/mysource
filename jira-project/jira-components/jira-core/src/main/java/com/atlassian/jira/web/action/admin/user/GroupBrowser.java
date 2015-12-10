/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.user;

import com.atlassian.core.util.StringUtils;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.util.GroupToPermissionSchemeMapper;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.GlobalPermissionGroupAssociationUtil;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.AbstractBrowser;
import com.atlassian.jira.web.bean.GroupBrowserFilter;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.ActionContext;
import webwork.util.BeanUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@WebSudoRequired
public class GroupBrowser extends AbstractBrowser
{
    private List<Group> groups;
    private String addName;
    private GroupToPermissionSchemeMapper groupPermissionSchemeMapper;
    private final GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil;
    private final CrowdService crowdService;
    private final GroupManager groupManager;
    private final UserManager userManager;
    private final CrowdDirectoryService crowdDirectoryService;

    public GroupBrowser(GroupToPermissionSchemeMapper groupToPermissionSchemeMapper, UserManager userManager, GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil,
            CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, GroupManager groupManager)
    {
        this.globalPermissionGroupAssociationUtil = globalPermissionGroupAssociationUtil;
        this.crowdService = crowdService;
        this.crowdDirectoryService = crowdDirectoryService;
        this.groupManager = groupManager;
        this.userManager = userManager;
        if (groupToPermissionSchemeMapper != null)
        {
            this.groupPermissionSchemeMapper = groupToPermissionSchemeMapper;
        }
        else
        {
            addErrorMessage(getText("groupbrowser.error.retrieve.group"));
        }
    }

    public GroupBrowser(GroupManager groupManager, UserManager userManager, CrowdService crowdService, CrowdDirectoryService crowdDirectoryService,
            GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil) throws GenericEntityException
    {
        this(new GroupToPermissionSchemeMapper(ComponentAccessor.getPermissionSchemeManager(),
                                                ComponentAccessor.getPermissionManager()),
                userManager,
                globalPermissionGroupAssociationUtil,
                crowdService,
                crowdDirectoryService,
                groupManager);
    }

    protected String doExecute() throws Exception
    {
        resetPager();

        BeanUtil.setProperties(params, getFilter());

        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doAdd() throws Exception
    {
        if (! addNewGroup())
        {
            return ERROR;
        }
        return doExecute();
    }

    private boolean addNewGroup()
    {
        if (org.apache.commons.lang.StringUtils.isEmpty(addName))
        {
            addError("addName", getText("admin.errors.cannot.add.groups.invalid.group.name"));
            return false;
        }

        //JRA-12112: If external *user* management is enabled, we do not allow the addtion of a new user.
        if (!userManager.hasGroupWritableDirectory())
        {
            addErrorMessage(getText("admin.errors.cannot.add.groups.directories.read.only"));
            return false;
        }

        if (crowdService.getGroup(addName) == null)
        {
            try
            {
                crowdService.addGroup(new ImmutableGroup(addName));
            }
            catch (OperationNotPermittedException e)
            {
                addError("addName", getText("groupbrowser.error.add", addName));
                log.error("Error occurred adding group : " + addName, e);
            }
            catch (InvalidGroupException e)
            {
                addError("addName", getText("groupbrowser.error.add", addName));
                log.error("Error occurred adding group : " + addName, e);
            }
            catch (OperationFailedException e)
            {
                addError("addName", getText("groupbrowser.error.add", addName));
                log.error("Error occurred adding group : " + addName, e);
            }
            addName = null;
        }
        else
        {
            addError("addName", getText("groupbrowser.error.group.exists"));
        }
        return true;
    }

    public PagerFilter getPager()
    {
        return getFilter();
    }

    public void resetPager()
    {
        ActionContext.getSession().put(SessionKeys.GROUP_FILTER, null);
    }

    public GroupBrowserFilter getFilter()
    {
        GroupBrowserFilter filter = (GroupBrowserFilter) ActionContext.getSession().get(SessionKeys.GROUP_FILTER);

        if (filter == null)
        {
            filter = new GroupBrowserFilter();
            ActionContext.getSession().put(SessionKeys.GROUP_FILTER, filter);
        }

        return filter;
    }

    /**
     * Return the current 'page' of issues (given max and start) for the current filter
     */
    public List getCurrentPage()
    {
        return getFilter().getCurrentPage(getBrowsableItems());
    }

    public List getBrowsableItems()
    {
        if (groups == null)
        {
            try
            {
                groups = getFilter().getFilteredGroups();
            }
            catch (Exception e)
            {
                log.error("Exception getting groups: " + e, e);
                return Collections.emptyList();
            }
        }

        return groups;
    }

    public String getAddName()
    {
        return addName;
    }

    public void setAddName(String addName)
    {
        this.addName = addName.trim();
    }

    public String escapeAmpersand(String str)
    {
        return StringUtils.replaceAll(str, "&", "%26");
    }

    public Collection<User> getUsersForGroup(Group group)
    {
        return groupManager.getUsersInGroup(group);
    }


    public Collection getPermissionSchemes(String groupName)
    {
        if (groupPermissionSchemeMapper != null)
        {
            return groupPermissionSchemeMapper.getMappedValues(groupName);
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    private Boolean hasGroupWritableDirectory = null;
    public boolean hasGroupWritableDirectory()
    {
        if (hasGroupWritableDirectory == null)
        {
            hasGroupWritableDirectory = new Boolean(userManager.hasGroupWritableDirectory());
        }
        return hasGroupWritableDirectory.booleanValue();
    }

    public boolean isUserAbleToDeleteGroup(String groupName)
    {
        return globalPermissionGroupAssociationUtil.isUserAbleToDeleteGroup(getLoggedInUser(), groupName);
    }

    /**
     * Return true if any directory supports nested groups.
     * @return true if any directory supports nested groups.
     */
    public boolean isNestedGroupsEnabledForAnyDirectory()
    {
        for (Directory directory : crowdDirectoryService.findAllDirectories())
        {
            if (crowdDirectoryService.supportsNestedGroups(directory.getId()))
            {
                return true;
            }
        }
        return false;
    }

}
