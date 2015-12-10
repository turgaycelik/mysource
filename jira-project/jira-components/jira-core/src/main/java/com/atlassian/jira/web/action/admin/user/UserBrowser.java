/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.AbstractBrowser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.bean.UserBrowserFilter;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;
import webwork.util.BeanUtil;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@WebSudoRequired
public class UserBrowser extends AbstractBrowser
{
    private List<User> users;
    private final UserUtil userUtil;
    private final CrowdService crowdService;
    private final CrowdDirectoryService crowdDirectoryService;
    private final UserManager userManager;
    private final AvatarService avatarService;
    private final SimpleLinkManager simpleLinkManager;

    public UserBrowser(final UserUtil userUtil, final CrowdService crowdService,
            final CrowdDirectoryService crowdDirectoryService, final UserManager userManager,
            final AvatarService avatarService, final SimpleLinkManager simpleLinkManager)
    {
        this.userUtil = userUtil;
        this.crowdService = crowdService;
        this.crowdDirectoryService = crowdDirectoryService;
        this.userManager = userManager;
        this.avatarService = avatarService;
        this.simpleLinkManager = simpleLinkManager;
    }

    // Protected -----------------------------------------------------
    protected String doExecute() throws Exception
    {
        String emailFilterParam = getSingleParam("emailFilter");
        String groupParam = getSingleParam("group");
        String maxUsersPerPage = getSingleParam("max");

        if ("".equals(emailFilterParam) || "".equals(groupParam))
        {
            resetPager();
            setStart("0");
        }

        BeanUtil.setProperties(params, getFilter());

        // if the max users displayed per page parameter has been set, preserve it after a reset
        if (TextUtils.stringSet(maxUsersPerPage))
        {
            getFilter().setMax(Integer.parseInt(maxUsersPerPage));
        }

        // JRA-12989 - Reset the start to 0 if number of items returned is less than the pager start
        if (getBrowsableItems().size() <= getPager().getStart())
        {
            setStart("0");
        }

        return super.doExecute();
    }

    public PagerFilter getPager()
    {
        return getFilter();
    }

    public void resetPager()
    {
        ActionContext.getSession().put(SessionKeys.USER_FILTER, null);
    }

    public UserBrowserFilter getFilter()
    {
        UserBrowserFilter filter = (UserBrowserFilter) ActionContext.getSession().get(SessionKeys.USER_FILTER);

        if (filter == null)
        {
            filter = new UserBrowserFilter(getLocale());
            ActionContext.getSession().put(SessionKeys.USER_FILTER, filter);
        }

        return filter;
    }

    /**
     * Return the current 'page' of issues (given max and start) for the current filter
     */
    public List<User> getCurrentPage()
    {
        return getFilter().getCurrentPage(getBrowsableItems());
    }

    public List<User> getBrowsableItems()
    {
        if (users == null)
        {
            try
            {
                users = getFilter().getFilteredUsers();
            }
            catch (Exception e)
            {
                log.error("Exception getting users: " + e, e);
                throw new RuntimeException(e);
            }
        }

        return users;
    }

    public Iterator getGroups()
    {
        final GroupQuery<Group> query = new GroupQuery<Group>(Group.class, GroupType.GROUP, NullRestrictionImpl.INSTANCE, 0, EntityQuery.ALL_RESULTS);
        return crowdService.search(query).iterator();
    }

    public Iterator getGroupsForUser(User user)
    {
        final MembershipQuery<String> membershipQuery =
                QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user.getName()).returningAtMost(EntityQuery.ALL_RESULTS);
        return crowdService.search(membershipQuery).iterator();
    }

    public String getDirectoryForUser(User user)
    {
        return crowdDirectoryService.findDirectoryById(user.getDirectoryId()).getName();
    }

    /**
     * Convenience method to use from JSP's to access total number of users
     *
     * @return the Users
     */
    public Collection<User> getUsers()
    {
        return getBrowsableItems();
    }

    public boolean isRemoteUserPermittedToEditSelectedUser(User user)
    {
        if (userManager.canUpdateUser(user))
        {
            return user != null && (isSystemAdministrator() || !getGlobalPermissionManager().hasPermission(Permissions.SYSTEM_ADMIN, user));
        }
        return false;
    }

    public boolean isRemoteUserPermittedToEditSelectedUsersGroups(User user)
    {
        return userManager.canUpdateGroupMembershipForUser(user);
    }

    public UserUtil getUserUtil()
    {
        return userUtil;
    }

    public boolean hasReachedUserLimit()
    {
        return !userUtil.canActivateNumberOfUsers(1);
    }

    public URI getAvatarUrl(final String username)
    {
        return avatarService.getAvatarURL(getLoggedInUser(), username, Avatar.Size.SMALL);
    }

    public Collection<SimpleLink> getOpsbarLinks()
    {
        return simpleLinkManager.getLinksForSection("system.admin.userbrowser.opsbar", getLoggedInUser(), getJiraHelper());
    }

    private JiraHelper getJiraHelper()
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        return new JiraHelper(ServletActionContext.getRequest(), null, params);
    }
}
