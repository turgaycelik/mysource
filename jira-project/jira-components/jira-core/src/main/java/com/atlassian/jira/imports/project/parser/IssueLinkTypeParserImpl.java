package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalIssueLinkType;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @since v3.13
 */
public class IssueLinkTypeParserImpl implements IssueLinkTypeParser
{
    private static final String ID = "id";
    private static final String LINK_NAME = "linkname";
    private static final String STYLE = "style";

    public ExternalIssueLinkType parse(final Map attributes) throws ParseException
    {
        if (attributes == null)
        {
            throw new IllegalArgumentException("The 'attributes' parameter cannot be null.");
        }

        // <IssueLinkType id="10001" linkname="jira_subtask_link" inward="jira_subtask_inward" outward="jira_subtask_outward" style="jira_subtask"/>
        // <IssueLinkType id="10000" linkname="Duplicate" inward="is duplicated by" outward="duplicates"/>
        final String id = (String) attributes.get(ID);
        final String linkname = (String) attributes.get(LINK_NAME);
        final String style = (String) attributes.get(STYLE);

        // Validate the data
        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("No 'id' field for IssueLinkType.");
        }
        if (StringUtils.isEmpty(linkname))
        {
            throw new ParseException("No 'linkname' field for IssueLinkType " + id + ".");
        }
        // style is optional and currently only used for subtasks

        final ExternalIssueLinkType externalIssueLinkType = new ExternalIssueLinkType();
        externalIssueLinkType.setId(id);
        externalIssueLinkType.setLinkname(linkname);
        externalIssueLinkType.setStyle(style);

        return externalIssueLinkType;
    }
}
