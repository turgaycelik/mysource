package com.atlassian.jira.imports.project.core;

import com.atlassian.jira.plugin.PluginVersion;
import com.atlassian.jira.util.dbc.Null;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @since v3.13
 */
public class BackupSystemInformationImpl implements BackupSystemInformation
{
    private final List pluginVersions;
    private final String buildNumber;
    private final String edition;
    private final boolean unassignedIssuesAllowed;
    private final Map issueIdToKeyMap;
    private final int entityCount;

    public BackupSystemInformationImpl(final String buildNumber, final String edition, final List pluginVersions, final boolean unassignedIssuesAllowed, final Map issueIdToKeyMap, final int entityCount)
    {
        Null.not("pluginVersions", pluginVersions);

        this.buildNumber = buildNumber;
        this.edition = edition;
        this.pluginVersions = Collections.unmodifiableList(pluginVersions);
        this.unassignedIssuesAllowed = unassignedIssuesAllowed;
        this.issueIdToKeyMap = Collections.unmodifiableMap(issueIdToKeyMap);
        this.entityCount = entityCount;
    }

    public String getBuildNumber()
    {
        return buildNumber;
    }

    public String getEdition()
    {
        return edition;
    }

    public List getPluginVersions()
    {
        return pluginVersions;
    }

    public boolean unassignedIssuesAllowed()
    {
        return unassignedIssuesAllowed;
    }

    public String getIssueKeyForId(final String issueId)
    {
        return (String) issueIdToKeyMap.get(issueId);
    }

    public int getEntityCount()
    {
        return entityCount;
    }

    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n").append("PluginVersions: ").append("\n");
        for (final Object pluginVersion1 : pluginVersions)
        {
            final PluginVersion pluginVersion = (PluginVersion) pluginVersion1;
            sb.append("\n").append("     ").append(pluginVersion);
        }
        sb.append("\n");
        sb.append("JIRA Build #: ").append(getBuildNumber()).append("\n");
        sb.append("JIRA Edition: ").append(getEdition()).append("\n");
        sb.append("Allow unassigned issues: ").append(unassignedIssuesAllowed()).append("\n");
        return sb.toString();
    }
}
