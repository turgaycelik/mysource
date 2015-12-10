/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserUtil;
import com.opensymphony.util.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FilterUtils
{
    /**
     * Returned string is non-null IFF there is a true value (ie some text)
     */
    public static String verifyString(String s)
    {
        if (TextUtils.stringSet(TextUtils.noNull(s).trim()))
        {
            return s;
        }
        else
        {
            return null;
        }
    }

    /**
     * Retirned string array is non-null IFF there is a true value (ie some text)
     */
    public static String[] verifyStringArray(String[] sa)
    {
        List<String> result = new ArrayList<String>();

        for (final String aSa : sa)
        {
            String s = verifyString(aSa);
            if (s != null)
            {
                result.add(s);
            }
        }

        if (result.size() == 0)
        {
            return null;
        }
        else
        {
            String[] resultSa = new String[result.size()];
            int count = 0;
            for (final String aResult : result)
            {
                resultSa[count++] = aResult;
            }

            return resultSa;
        }
    }

    public static Long verifyLong(Long id)
    {
        if (id != null && id.longValue() > 0)
        {
            return id;
        }

        return null;
    }

    /**
     * Get the groups that this filter can be shared with.  If this is a regular user, this will be the user's groups.
     * If the user is an admin, it will return all groups.
     *
     * @return A collection of strings representing the groups.
     */
    public static Collection getGroups(User user)
    {
        Collection<String> groups = new ArrayList<String>();

        if (user == null)
            return null;

        UserUtil userUtil = ComponentAccessor.getComponent(UserUtil.class);
        GroupManager groupManager = ComponentAccessor.getComponent(GroupManager.class);

        if (ComponentAccessor.getPermissionManager().hasPermission(Permissions.ADMINISTER, user))
        {
            //have to convert groups to group names
            for (Group group : groupManager.getAllGroups())
            {
                groups.add(group.getName());
            }
        }
        else
        {
            groups = userUtil.getGroupNamesForUser(user.getName());
        }
        return groups;
    }
}
