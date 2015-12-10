/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.component.ComponentAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupBrowserFilter extends PagerFilter
{
    String nameFilter;

    public String getNameFilter()
    {
        return nameFilter;
    }

    public void setNameFilter(final String nameFilter)
    {
        this.nameFilter = FilterUtils.verifyString(nameFilter);
    }

    public List<Group> getFilteredGroups() throws Exception
    {
        @SuppressWarnings("unchecked")
        final Collection<Group> groups = ComponentAccessor.getGroupManager().getAllGroups();

        if (nameFilter == null)
        {
            return new ArrayList<Group>(groups);
        }

        final List<Group> filteredGroups = new ArrayList<Group>();

        // get list of filtered users
        for (final Group group : groups)
        {
            if ((nameFilter == null) || (group.getName().toLowerCase().indexOf(nameFilter.toLowerCase()) >= 0))
            {
                filteredGroups.add(group);
            }
        }

        return filteredGroups;
    }
}
