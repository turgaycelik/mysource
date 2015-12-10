package com.atlassian.jira.plugin.myjirahome;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;

/**
 * Fired when a user changes his My JIRA Home location. The event should not contain the location itself
 * but the complete plugin module keys instead.
 *
 * @since 5.1
 */
@PublicApi
public class MyJiraHomeChangedEvent
{
    private final User user;
    private final String fromHomePluginModuleKey;
    private final String toHomePluginModuleKey;

    public MyJiraHomeChangedEvent(final User user, final String fromHomePluginModuleKey, final String toHomePluginModuleKey) {
        this.user = user;
        this.fromHomePluginModuleKey = fromHomePluginModuleKey;
        this.toHomePluginModuleKey = toHomePluginModuleKey;
    }

    public User getUser()
    {
        return user;
    }

    public String getFromHomePluginModuleKey()
    {
        return fromHomePluginModuleKey;
    }

    public String getToHomePluginModuleKey()
    {
        return toHomePluginModuleKey;
    }
}
