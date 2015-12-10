package com.atlassian.jira.dev.reference.plugin.rpc;

import com.atlassian.plugin.PluginAccessor;

/**
 * Default implementation of {@link com.atlassian.jira.dev.reference.plugin.rpc.ReferenceSoapService}.
 *
 * @since v4.4
 */
public class ReferenceSoapServiceImpl implements ReferenceSoapService {

    private final PluginAccessor pluginAccessor;

    public ReferenceSoapServiceImpl(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    public String getVersion() {
        return pluginAccessor.getPlugin("com.atlassian.jira.dev.reference-plugin").getPluginInformation().getVersion();
    }

    public String concatenateThreeStrings(String paramOne, String paramTwo, String paramThree) {
        return paramOne + paramTwo + paramThree; // FTW!
    }

    public int add(int one, int two) {
        return one + two; // FTW!
    }
}
