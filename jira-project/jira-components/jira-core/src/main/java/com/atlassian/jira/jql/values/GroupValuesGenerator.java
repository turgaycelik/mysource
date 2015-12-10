package com.atlassian.jira.jql.values;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.groups.GroupManager;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Completes group names for the group custom fields.
 *
 * @since v4.0
 */
public class GroupValuesGenerator implements ClauseValuesGenerator
{
    private final GroupManager groupManager;

    public GroupValuesGenerator(GroupManager groupManager)
    {
        this.groupManager = groupManager;
    }

    public Results getPossibleValues(final User searcher, final String jqlClauseName, final String valuePrefix, final int maxNumResults)
    {
        // Get all groups and filter them by valuePrefix
        final Collection<Group> groups = getAllGroups();

        final List<ClauseValuesGenerator.Result> groupValues = new ArrayList<ClauseValuesGenerator.Result>();
        for (Group group : groups)
        {
            if (groupValues.size() == maxNumResults)
            {
                break;
            }
            final String lowerCaseGroupName = group.getName().toLowerCase();
            if (StringUtils.isBlank(valuePrefix) || lowerCaseGroupName.startsWith(valuePrefix.toLowerCase()))
            {
                groupValues.add(new ClauseValuesGenerator.Result(group.getName()));
            }
        }

        return new ClauseValuesGenerator.Results(groupValues);
    }

    protected Collection<Group> getAllGroups()
    {
        return groupManager.getAllGroups();
    }
}
