package com.atlassian.jira.functest.framework.upm;

import org.json.JSONObject;

public class PluginStatus
{
    final private boolean enabled;
    final private boolean userInstalled;
    private final String pluginKey;
    private final JSONObject originalJson;

    public PluginStatus(final String pluginKey, final JSONObject originalJson, final boolean enabled, final boolean userInstalled)
    {
        this.pluginKey = pluginKey;
        this.originalJson = originalJson;
        this.enabled = enabled;
        this.userInstalled = userInstalled;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public boolean isUserInstalled()
    {
        return userInstalled;
    }

    public JSONObject getOriginalJson()
    {
        return originalJson;
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    @Override
    public String toString()
    {
        return String.format("%s(userInstalled: %b, enabled: %b)", pluginKey, userInstalled, enabled);
    }
}
