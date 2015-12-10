package com.atlassian.jira.avatar;

import java.util.List;

import com.atlassian.annotations.ExperimentalApi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Provides lists of system and custom avatars. {@link TypeAvatarService#getAvatars(com.atlassian.jira.user.ApplicationUser,
 * String)}
 *
 * @since v6.3
 */
@ExperimentalApi
public class SystemAndCustomAvatars
{
    private final List<Avatar> systemAvatars;
    private final List<Avatar> customAvatars;

    public SystemAndCustomAvatars(final List<Avatar> systemAvatars, final List<Avatar> customAvatars)
    {
        this.systemAvatars = systemAvatars;
        this.customAvatars = customAvatars;
    }

    public Iterable<Avatar> getCustomAvatars()
    {
        return ImmutableList.copyOf(customAvatars);
    }

    public Iterable<Avatar> getSystemAvatars()
    {
        return ImmutableList.copyOf(systemAvatars);
    }
}
