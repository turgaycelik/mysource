/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.user;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.jira.bc.group.GroupRemoveChildMapper;
import com.atlassian.jira.bc.group.GroupService;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.GlobalPermissionGroupAssociationUtil;
import org.apache.commons.lang.StringUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class EditUserGroups extends ViewUser
{
    private String[] groupsToJoin = new String[0];
    private String[] groupsToLeave = new String[0];
    private Collection<String> memberGroups;
    private Collection<String> nonMemberGroups;
    private String join;
    private String leave;

    private final GroupService groupService;
    private final GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil;

    public EditUserGroups(CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil, GroupService groupService, final UserPropertyManager userPropertyManager, final UserManager userManager)
    {
        super(crowdService, crowdDirectoryService, userPropertyManager, userManager);
        this.globalPermissionGroupAssociationUtil = globalPermissionGroupAssociationUtil;
        this.groupService = groupService;
    }

    protected void doValidation()
    {
        super.doValidation();

        if (StringUtils.isNotBlank(getJoin()))
        {
            List<String> groupsToJoinList = (groupsToJoin != null) ? Arrays.asList(groupsToJoin) : Collections.<String>emptyList();
            if (!groupsToJoinList.isEmpty())
            {
                groupService.validateAddUserToGroup(getJiraServiceContext(), groupsToJoinList, name);
            }
            else
            {
                getJiraServiceContext().getErrorCollection().addErrorMessage(getText("admin.errors.groups.must.select.one.to.join"));
            }
        }
        if (StringUtils.isNotBlank(getLeave()))
        {
            List<String> groupsToLeaveList = (groupsToLeave != null) ? Arrays.asList(groupsToLeave) : Collections.<String>emptyList();
            if (!groupsToLeaveList.isEmpty())
            {
                groupService.validateRemoveUserFromGroups(getJiraServiceContext(), groupsToLeaveList, name);
            }
            else
            {
                getJiraServiceContext().getErrorCollection().addErrorMessage(getText("admin.errors.groups.must.select.one.to.leave"));
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (StringUtils.isNotBlank(getJoin()))
        {
            List<String> groupsToJoinList = (groupsToJoin != null) ? Arrays.asList(groupsToJoin) : Collections.<String>emptyList();
            groupService.addUsersToGroups(getJiraServiceContext(), groupsToJoinList, EasyList.build(name));
            nonMemberGroups = null;
        }
        else if (StringUtils.isNotBlank(getLeave()))
        {
            List<String> groupsToLeaveList = (groupsToLeave != null) ? Arrays.asList(groupsToLeave) : Collections.<String>emptyList();
            GroupRemoveChildMapper mapper = new GroupRemoveChildMapper();
            mapper.register(name, groupsToLeaveList);
            groupService.removeUsersFromGroups(getJiraServiceContext(), mapper);
            memberGroups = null;
        }
        return returnComplete("EditUserGroups!default.jspa?name=" + URLEncoder.encode(getName()));
    }

    public Collection getMemberGroups()
    {
        if (memberGroups == null && getUser() != null)
        {
            memberGroups = globalPermissionGroupAssociationUtil.getGroupNamesModifiableByCurrentUser(getLoggedInUser(), getUserGroups());

            // remove any nested groups - We only want direct ones.
            for (Iterator<String> iter = memberGroups.iterator(); iter.hasNext();)
            {
                Group group = crowdService.getGroup(iter.next());
                if (group == null || !crowdService.isUserDirectGroupMember(getUser(), group))
                {
                    iter.remove();
                }
            }
        }

        return memberGroups;
    }

    /**
     * This method returns all the groups the current user is not currently a member of.  If the current user is not a
     * {@link Permissions#SYSTEM_ADMIN} then this method will also filter out all groups that are assigned to the
     * SYSTEM_ADMIN global permission.
     * @return a collection of groups that the user is not currently a member of.
     */
    public Collection getNonMemberGroups()
    {
        if (nonMemberGroups == null && getUser() != null)
        {
            final Query<String> query = new GroupQuery<String>(String.class, GroupType.GROUP, NullRestrictionImpl.INSTANCE, 0, GroupQuery.ALL_RESULTS);
            List<String> allGroupNames = new ArrayList<String>();
            for (final String s : crowdService.search(query))
            {
                allGroupNames.add(s);
            }

            nonMemberGroups = globalPermissionGroupAssociationUtil.getGroupNamesModifiableByCurrentUser(getLoggedInUser(), allGroupNames);
            nonMemberGroups.removeAll(getUserGroups());
        }

        return nonMemberGroups;
    }

    public void setGroupsToJoin(String[] groupsToJoin)
    {
        this.groupsToJoin = groupsToJoin;
    }

    public void setGroupsToLeave(String[] groupsToLeave)
    {
        this.groupsToLeave = groupsToLeave;
    }


    public String getJoin()
    {
        return join;
    }

    public void setJoin(String join)
    {
        this.join = join;
    }

    public String getLeave()
    {
        return leave;
    }

    public void setLeave(String leave)
    {
        this.leave = leave;
    }
}
