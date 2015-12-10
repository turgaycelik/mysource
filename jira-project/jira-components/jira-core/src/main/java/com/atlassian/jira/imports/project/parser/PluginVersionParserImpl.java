package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.plugin.PluginVersion;
import com.atlassian.jira.plugin.PluginVersionImpl;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 * @since v3.13
 */
public class PluginVersionParserImpl implements PluginVersionParser
{
    public PluginVersion parse(final Map attributes) throws ParseException
    {
        Null.not("The 'attributes' parameter cannot be null.", attributes);

        //<PluginVersion id="10000" name="Admin Menu Sections" key="jira.webfragments.admin" version="1.0"/>

        final String idStr = (String) attributes.get("id");
        final String name = (String) attributes.get("name");
        final String key = (String) attributes.get("key");
        final String version = (String) attributes.get("version");
        final String created = (String) attributes.get("created");

        // Validate the data
        if (StringUtils.isEmpty(idStr))
        {
            throw new ParseException("No 'id' field for PluginVersion.");
        }
        Long id = null;
        try
        {
            id = new Long(idStr);
        }
        catch (final NumberFormatException e)
        {
            throw new ParseException("Unable to parse the id '" + idStr + "' into a long.");
        }
        if (StringUtils.isEmpty(name))
        {
            throw new ParseException("No 'name' field for PluginVersion " + id + ".");
        }
        if (StringUtils.isEmpty(key))
        {
            throw new ParseException("No 'key' field for PluginVersion " + id + ".");
        }
        if (StringUtils.isEmpty(version))
        {
            throw new ParseException("No 'version' field for PluginVersion " + id + ".");
        }

        Date createdDate = null;
        if (created != null)
        {
            createdDate = java.sql.Timestamp.valueOf(created);
        }

        return new PluginVersionImpl(id, key, name, version, createdDate);
    }
}
