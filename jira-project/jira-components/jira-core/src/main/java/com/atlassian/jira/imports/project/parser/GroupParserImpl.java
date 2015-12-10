package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalGroup;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @since v3.13
 */
public class GroupParserImpl implements GroupParser
{
    private static final String GROUP_NAME = "groupName";

    public ExternalGroup parse(final Map attributes) throws ParseException
    {
        Null.not("attributes", attributes);

        // The XML entry for Groups looks like:
        // <OSGroup id="10011" name="Dudettes"/>
        // We ignore the ID as this is only for internal use of Open Symphony, and is never exposed to JIRA.
        final String name = (String) attributes.get(GROUP_NAME);

        if (StringUtils.isEmpty(name))
        {
            throw new ParseException("A Group in the backup file is missing the groupName parameter.");
        }

        return new ExternalGroup(name);
    }
}
