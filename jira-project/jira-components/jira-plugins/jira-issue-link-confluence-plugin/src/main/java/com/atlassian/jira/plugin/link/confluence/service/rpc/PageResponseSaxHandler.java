package com.atlassian.jira.plugin.link.confluence.service.rpc;

import com.atlassian.jira.plugin.link.confluence.ConfluencePage;
import com.atlassian.jira.plugin.link.confluence.ConfluencePage.ConfluencePageBuilder;

import java.util.List;

/**
 * Handles XML responses from the "getSpaces" Confluence XMLRPC method.
 *
 * @since v5.0
 */
public class PageResponseSaxHandler extends AbstractConfluenceSaxHandler<ConfluencePage, ConfluencePageBuilder>
{
    protected PageResponseSaxHandler()
    {
        super(new ConfluencePageBuilder());
    }

    @Override
    protected void addMember(final NameValuePair member, final ConfluencePageBuilder builder)
    {
        if ("id".equals(member.getName()))
        {
            builder.pageId(member.getValue());
        }
        else if ("title".equals(member.getName()))
        {
            builder.title(member.getValue());
        }
        else if ("url".equals(member.getName()))
        {
            builder.url(member.getValue());
        }
    }
}
