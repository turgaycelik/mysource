package com.atlassian.jira.imports.project.populator;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilder;
import com.atlassian.jira.imports.project.parser.PluginVersionParser;
import com.atlassian.jira.imports.project.parser.PluginVersionParserImpl;
import com.atlassian.jira.plugin.PluginVersion;

import java.util.Map;

/**
 * Converts PluginVersion information and sets it on the {@link com.atlassian.jira.imports.project.populator.BackupOverviewPopulator}.
 *
 * @since v3.13
 */
public class PluginVersionPopulator implements BackupOverviewPopulator
{
    private PluginVersionParser pluginVersionParser;

    public void populate(final BackupOverviewBuilder backupOverviewBuilder, final String elementName, final Map attributes) throws ParseException
    {
        if (PluginVersionParser.PLUGIN_VERSION_ENTITY_NAME.equals(elementName))
        {
            final PluginVersion pluginVersion = getPluginVersionParser().parse(attributes);
            backupOverviewBuilder.addPluginVersion(pluginVersion);
        }
    }

    PluginVersionParser getPluginVersionParser()
    {
        if (pluginVersionParser == null)
        {
            pluginVersionParser = new PluginVersionParserImpl();
        }
        return pluginVersionParser;
    }
}
