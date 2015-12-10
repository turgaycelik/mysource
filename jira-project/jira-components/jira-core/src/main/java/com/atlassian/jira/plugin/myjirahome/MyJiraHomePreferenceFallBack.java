package com.atlassian.jira.plugin.myjirahome;

import com.atlassian.crowd.embedded.api.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implements the fall-back behaviour: always returns an empty value as current My JIRA Home.
 *
 * @since 5.1
 */
class MyJiraHomePreferenceFallBack implements MyJiraHomePreference
{
    @Nonnull
    @Override
    public String findHome(@Nullable final User user)
    {
        return "";
    }

}
