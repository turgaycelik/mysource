package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Some utility functions for dealing with {@link UserFilter} instances.
 *
 * @since v6.2
 */
public class UserFilterUtils
{
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_GROUPS = "groups";
    private static final String KEY_ROLEIDS = "roleIds";
    public static final Function<ProjectRole,Long> GET_ROLE_ID_FUNCTION = new Function<ProjectRole, Long>()
    {
        @Override
        public Long apply(final ProjectRole projectRole)
        {
            return projectRole.getId();
        }
    };

    /**
     * Intersect the groups of the userFilter with {@code groups}.
     * <p>
     * If the filter is disabled and {@code groups} is not empty, a new filter that's enabled and with the groups will be returned.
     * <p>
     * If the filter is enabled, its groups will be intersected with the incoming one and a new filter
     * with the intersection will be returned.
     *
     * @param userFilter the user filter to applied the intersection of groups. Will not be changed
     * @param groups a set of groups to be intersected with the filter
     * @return the updated filter or the original one, if no changes.
     */
    public static UserFilter intersectGroups(final UserFilter userFilter, final Set<String> groups)
    {
        final boolean newGroupNotEmpty = CollectionUtils.isNotEmpty(groups);
        if (!userFilter.isEnabled())
        {
            // not enabled, make it enabled and add the groups, if not empty
            if (newGroupNotEmpty)
            {
                return new UserFilter(true, null, groups);
            }
        }
        else
        {
            final Set<String> intersectedGroups = findIntersectedGroups(userFilter.getGroups(), groups);
            if (intersectedGroups != userFilter.getGroups())
            {
                return new UserFilter(true, userFilter.getRoleIds(), intersectedGroups);
            }
        }
        return userFilter;
    }

    private static Set<String> findIntersectedGroups(final Set<String> groups, final Set<String> newGroups)
    {
        return groups == null ? newGroups : (newGroups == null ? groups : Sets.intersection(groups, newGroups));
    }

    /**
     * Convert the {@code userFilter} to {@code JSONObject}
     * @param userFilter the user filter to be converted
     * @param projectRoleManager optional, if provided, will be used to filter invalid project roles
     * @throws JSONException
     */
    public static JSONObject toJson(final UserFilter userFilter, final ProjectRoleManager projectRoleManager) throws JSONException
    {
        checkNotNull(userFilter, "userFilter");
        final JSONObject object = new JSONObject();
        object.put(KEY_ENABLED, userFilter.isEnabled());
        if (userFilter.isEnabled())
        {
            object.put(KEY_GROUPS, new JSONArray(sortGroups(userFilter.getGroups())));
            object.put(KEY_ROLEIDS, new JSONArray(
                    projectRoleManager != null ?
                        filterRemovedRoleIds(userFilter.getRoleIds(), projectRoleManager) :
                        userFilter.getRoleIds()));
        }
        return object;
    }

    public static UserFilter fromJson(JSONObject json) throws JSONException
    {
        checkNotNull(json, "json");
        boolean isEnabled = json.getBoolean(KEY_ENABLED);
        if (!isEnabled)
        {
            return UserFilter.DISABLED;
        }
        return new UserFilter(true, getRoleIds(json.getJSONArray(KEY_ROLEIDS)), getGroups(json.getJSONArray(KEY_GROUPS)));
    }

    public static UserFilter fromJsonString(String jsonString) throws JSONException
    {
        if (StringUtils.isEmpty(jsonString))
        {
            return UserFilter.DISABLED;
        }
        else
        {
            return UserFilterUtils.fromJson(new JSONObject(jsonString));
        }
    }

    private static Collection<Long> getRoleIds(final JSONArray jsonArray) throws JSONException
    {
        Set<Long> roleIds = Sets.newHashSetWithExpectedSize(jsonArray.length());
        for (int i = 0 ; i<jsonArray.length(); i++)
        {
            roleIds.add(jsonArray.getLong(i));
        }

        return roleIds;
    }

    private static Collection<String> getGroups(final JSONArray jsonArray) throws JSONException
    {
        Set<String> groups = Sets.newHashSetWithExpectedSize(jsonArray.length());
        for (int i = 0 ; i<jsonArray.length(); i++)
        {
            groups.add(jsonArray.getString(i));
        }

        return groups;
    }

    public static UserFilter getFilterWithoutRemovedGroupsAndRoles(final UserFilter filter, final Collection<Group> allGroups, final ProjectRoleManager projectRoleManager)
    {
        return filter != null && filter.isEnabled() ?
                new UserFilter(true, filterRemovedRoleIds(filter.getRoleIds(), projectRoleManager), filterRemovedGroups(filter.getGroups(), allGroups)) :
                filter;
    }

    public static Set<String> filterRemovedGroups(final Set<String> groups, final Collection<Group> allGroups)
    {
        if (CollectionUtils.isEmpty(groups))
        {
            return groups;
        }
        else
        {
            final ImmutableSet<String> allGroupNames = ImmutableSet.copyOf(Iterables.transform(allGroups, new Function<Group, String>()
            {
                @Override
                public String apply(final Group input)
                {
                    return input.getName();
                }
            }));
            // remove groups that have been removed
            return ImmutableSet.copyOf(Iterables.filter(groups, new Predicate<String>()
            {
                @Override
                public boolean apply(final String input)
                {
                    return allGroupNames.contains(input);
                }
            }));
        }
    }

    @VisibleForTesting
    static Set<Long> filterRemovedRoleIds(final Set<Long> roleIds, final ProjectRoleManager projectRoleManager)
    {
        if (CollectionUtils.isEmpty(roleIds))
        {
            return roleIds;
        }
        else
        {
            return ImmutableSet.copyOf(Iterables.transform(getProjectRoles(projectRoleManager, roleIds), GET_ROLE_ID_FUNCTION));
        }
    }

    /**
     * Get the {@link com.atlassian.jira.security.roles.ProjectRole} objects sorted by role name.
     */
    public static List<ProjectRole> getProjectRoles(final ProjectRoleManager projectRoleManager, final Set<Long> roleIds)
    {
        // a little optimization, get individual when one role id only, otherwise get all and filter
        final List<ProjectRole> projectRoles = roleIds.size() > 1 ?
                getProjectRolesFromAll(projectRoleManager, roleIds) :
                getProjectRolesIndividually(projectRoleManager, roleIds);

        if (projectRoles.size() > 1)
        {
            Collections.sort(projectRoles, new Comparator<ProjectRole>()
            {
                @Override
                public int compare(ProjectRole p1, ProjectRole p2)
                {
                    return org.apache.commons.lang3.ObjectUtils.compare(p1.getName(), p2.getName());
                }
            });
        }
        return projectRoles;
    }

    private static List<ProjectRole> getProjectRolesIndividually(final ProjectRoleManager projectRoleManager, final Set<Long> roleIds)
    {
        if (roleIds.isEmpty())
        {
            return ImmutableList.of();
        }
        else
        {
            final ProjectRole projectRole = projectRoleManager.getProjectRole(roleIds.iterator().next());
            return projectRole != null ? ImmutableList.of(projectRole) : ImmutableList.<ProjectRole>of();
        }
    }

    private static List<ProjectRole> getProjectRolesFromAll(final ProjectRoleManager projectRoleManager, final Set<Long> roleIds)
    {
        // get all project roles and filter, hopefully more efficient than looping through all role ids
        //  and calling getProjectRole one by one
        return Lists.newArrayList(Collections2.filter(projectRoleManager.getProjectRoles(), new Predicate<ProjectRole>()
        {
            @Override
            public boolean apply(final ProjectRole input)
            {
                return roleIds.contains(input.getId());
            }
        }));
    }

    public static List<String> sortGroups(final Collection<String> groups)
    {
        List<String> groupList = Lists.newArrayList(groups);
        Collections.sort(groupList);
        return groupList;
    }
}
