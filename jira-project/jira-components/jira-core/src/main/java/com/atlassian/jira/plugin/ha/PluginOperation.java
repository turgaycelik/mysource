package com.atlassian.jira.plugin.ha;

/**
 * Represents a plugin operation (plugin enabled, disabled , etc)
 *
 * @since v6.1
 */
public class PluginOperation
{
    public static final String SEPARATOR = ":";
    private final PluginEventType eventType;
    private final String pluginKey;
    private final String moduleKey;

    public PluginOperation(String pluginEventinfo)
    {
        this.eventType = getEventTypeFromSupplementalInfo(pluginEventinfo);
        final String completeKey = getKeyFromSupplementalInfo(pluginEventinfo);
        this.pluginKey = getPluginKeyFromKey(completeKey);
        this.moduleKey = getModuleKeyFromKey(completeKey);
    }

    private String getModuleKeyFromKey(final String completeKey)
    {
        String[] keys =  completeKey.split(SEPARATOR);
        return keys.length > 1 ? keys[1] : null;
    }

    private String getPluginKeyFromKey(final String completeKey)
    {
        return completeKey.split(":")[0];
    }

    private String getKeyFromSupplementalInfo(final String pluginEventInfo)
    {
        return pluginEventInfo.substring(pluginEventInfo.indexOf("-") + 1);
    }

    private PluginEventType getEventTypeFromSupplementalInfo(final String pluginEventinfo)
    {
        return PluginEventType.valueOf(pluginEventinfo.substring(0, pluginEventinfo.indexOf("-")));
    }

    PluginEventType getPluginEventType()
    {
        return eventType;
    }

    String getPluginKey()
    {
        return pluginKey;
    }

    String getModuleKey()
    {
        return moduleKey;
    }

    String getCompleteKey()
    {
        return moduleKey != null ? pluginKey + SEPARATOR + moduleKey : pluginKey;
    }
}
