package com.atlassian.jira.plugin.link.confluence.service.rpc;

import com.atlassian.jira.plugin.link.confluence.ConfluenceSpace;
import com.atlassian.jira.plugin.link.confluence.ConfluenceSpace.ConfluenceSpaceBuilder;

import java.util.List;

/**
 * Handles XML responses from the "getSpaces" Confluence XMLRPC method.
 *
 * @since v5.0
 */
public class SpaceResponseSaxHandler extends AbstractConfluenceSaxHandler<ConfluenceSpace, ConfluenceSpaceBuilder>
{
    protected SpaceResponseSaxHandler()
    {
        super(new ConfluenceSpaceBuilder());
    }

    @Override
    protected void addMember(final NameValuePair member, final ConfluenceSpaceBuilder builder)
    {
        if ("key".equals(member.getName()))
        {
            builder.key(member.getValue());
        }
        else if ("name".equals(member.getName()))
        {
            builder.name(member.getValue());
        }
        else if ("type".equals(member.getName()))
        {
            builder.type(member.getValue());
        }
        else if ("url".equals(member.getName()))
        {
            builder.url(member.getValue());
        }
    }
}
