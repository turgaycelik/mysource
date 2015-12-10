package com.atlassian.jira.issue.fields.renderer.wiki.resolvers;

import com.atlassian.jira.issue.fields.renderer.wiki.links.JiraIssueLink;
import com.atlassian.jira.util.JiraKeyUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.ContentLinkResolver;
import com.atlassian.renderer.links.GenericLinkParser;
import com.atlassian.renderer.links.Link;

import java.text.ParseException;

/** @since 3.12 */
public class JiraIssueLinkResolver implements ContentLinkResolver
{
    public Link createContentLink(RenderContext context, GenericLinkParser parser) throws ParseException
    {
        if (JiraKeyUtils.validIssueKey(parser.getNotLinkBody()))
        {
            return new JiraIssueLink(parser.getNotLinkBody(), parser.getLinkBody());
        }
        else
        {
            return null;
        }
    }
}
