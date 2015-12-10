package com.atlassian.jira.plugin.link.confluence.service.rpc;

import com.atlassian.jira.plugin.link.confluence.ConfluenceSearchResult;
import com.atlassian.jira.plugin.link.confluence.ConfluenceSearchResult.ConfluenceSearchResultBuilder;

import java.util.List;

/**
 * Handles XML responses from the "search" Confluence XMLRPC method.
 *
 * @since v5.0
 */
public class SearchResponseSaxHandler extends AbstractConfluenceSaxHandler<ConfluenceSearchResult, ConfluenceSearchResultBuilder>
{
    protected SearchResponseSaxHandler()
    {
        super(new ConfluenceSearchResultBuilder());
    }

    @Override
    protected void addMember(final NameValuePair member, final ConfluenceSearchResultBuilder builder)
    {
        if ("id".equals(member.getName()))
        {
            builder.id(member.getValue());
        }
        else if ("type".equals(member.getName()))
        {
            builder.type(member.getValue());
        }
        else if ("title".equals(member.getName()))
        {
            builder.title(member.getValue());
        }
        else if ("excerpt".equals(member.getName()))
        {
            builder.excerpt(member.getValue());
        }
        else if ("url".equals(member.getName()))
        {
            builder.url(member.getValue());
        }
    }
}