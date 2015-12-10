package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.VersionHelper;
import com.atlassian.jira.project.version.Version;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Checks to see if the version passed in is released
 *
 * @since v5.0
 */
public class IsVersionReleased implements Condition
{

    private static final String VERSION_KEY = "version";

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        if (context.containsKey(VERSION_KEY))
        {
            Version version = getVersion(context);

            if (version != null)
            {
                return version.isReleased();
            }

        }
        return false;
    }

    private Version getVersion(Map<String, Object> context)
    {
        if (context.containsKey(VERSION_KEY))
        {
            return (Version) context.get(VERSION_KEY);
        }
        if (context.containsKey("helper"))
        {
            JiraHelper helper = (JiraHelper) context.get("helper");
            if (helper instanceof VersionHelper)
            {
                BrowseVersionContext versionContext = ((VersionHelper) helper).getVersionContext();
                return versionContext.getVersion();
            }

        }
        return null;
    }
}
