package com.atlassian.jira.user;

import java.util.Collection;
import java.util.Set;

import com.atlassian.annotations.PublicApi;

import com.google.common.collect.ImmutableSet;

/**
 * Represents user filtering setting.
 *
 * If isEnabled() is false, then it always refers to all active users.
 *
 * If isEnabled() is true, it's subject to the restrictions from {@link UserFilter#getGroups()} and {@link UserFilter#getRoleIds()}.
 *
 * Current implementation supports filters by groups and/or project roles. The filters are OR-ed, i.e.,
 * if 2 group filters and 3 project role filters are specified, the final set of users allowed is the union of
 * the 5 sets of users from the 5 filters. If no filters are specified and isEnabled() is true, no users are allowed.
 *
 * @since v6.2
 */
@PublicApi
public class UserFilter
{
    private final boolean enabled;
    private final Set<String> groups;
    private final Set<Long> roleIds;

    public final static UserFilter DISABLED = new UserFilter(false, null, null);
    public final static UserFilter ENABLED_NO_USERS = new UserFilter(true, null, null);

    public UserFilter(boolean enabled, final Collection<Long> roleIds, final Collection<String> groups)
    {
        this.enabled = enabled;
        this.groups = groups == null ? ImmutableSet.<String>of() : ImmutableSet.copyOf(groups);
        this.roleIds = roleIds == null ? ImmutableSet.<Long>of() : ImmutableSet.copyOf(roleIds);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * <p>This value is only considered if {@link UserFilter#isEnabled()} returns true.</p>
     *
     * <p>If null or empty, it means NO users are allowed from group restriction point of view.
     *  Note that some users might still be allowed due to project role based restriction. {@link UserFilter#getRoleIds()}</p>
     * <p>If not empty, it means only users from the groups are allowed.
     *  Note that some other users might still be allowed due to project role based restriction.</p>
     * @return
     */
    public Set<String> getGroups()
    {
        return groups;
    }

    /**
     * <p>This value is only considered if {@link UserFilter#isEnabled()} returns true.</p>
     *
     * <p>If null or empty, it means NO users are allowed from project role restriction point of view.
     *  Note that some users might still be allowed due to group based restriction. {@link UserFilter#getGroups()}</p>
     * <p>If not empty, it means only users from the project roles are allowed.
     *  Note that some other users might still be allowed due to group based restriction.</p>
     * @return
     */
    public Set<Long> getRoleIds()
    {
        return roleIds;
    }
}
