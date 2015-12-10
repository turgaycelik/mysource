package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.plugin.PluginVersion;

import java.util.Map;

/**
 * Converts plugin version information from the backup XML to an object representation.
 *
 * @since v3.13
 */
public interface PluginVersionParser
{
    public static final String PLUGIN_VERSION_ENTITY_NAME = "PluginVersion";

    /**
     * Transforms a set of attributes into a PluginVersion.
     *
     * @param attributes is a map of key value pairs that represent the attributes of an PluginVersion. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>id (required)</li>
     * <li>name (required)</li>
     * <li>key (required)</li>
     * <li>version (required)</li>
     * </ul>
     * @return a PluginVersion if the attributes contain the required attributes.
     *
     * @throws ParseException If the attributes are invalid.
     */
    PluginVersion parse(final Map attributes) throws ParseException;
}
