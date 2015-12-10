package com.atlassian.jira.bc.group.search;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.GroupComparator;
import com.atlassian.jira.user.util.UserManager;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;


/**
 * {@link UserManager} based implementation of {@link GroupPickerSearchService}
 *
 * @since v4.4
 */
public class GroupPickerSearchServiceImpl implements GroupPickerSearchService
{
    private UserManager userManager;

    public GroupPickerSearchServiceImpl(final UserManager userManager)
    {
        this.userManager = userManager;
    }

    /**
     * @see GroupPickerSearchService#findGroups(String)
     */
    public List<Group> findGroups(final String query)
    {
        final Collection<Group> matchingGroups = new TreeSet<Group>(GroupComparator.GROUP_COMPARATOR);
        final Collection<Group> exactMatches = new TreeSet<Group>(GroupComparator.GROUP_COMPARATOR);
        final Collection<Group> groups = userManager.getGroups();

        if(StringUtils.isBlank(query))
        {
            matchingGroups.addAll(groups);
            return new ArrayList<Group>(matchingGroups);
        }

        for (final Group group : groups)
        {
            if(group.getName().equalsIgnoreCase(query))
            {
                exactMatches.add(group);
            }
            else if(group.getName().contains(query))
            {
                matchingGroups.add(group);
            }
        }

        final List<Group> ret = new ArrayList<Group>();
        //exact matches should be returned first (JRA-26981)
        ret.addAll(exactMatches);
        ret.addAll(matchingGroups);
        return ret;
    }

    @Override
    public Group getGroupByName(String groupName)
    {
        return userManager.getGroup(groupName);
    }
}
