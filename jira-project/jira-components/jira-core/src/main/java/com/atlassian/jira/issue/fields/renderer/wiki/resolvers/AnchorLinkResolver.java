package com.atlassian.jira.issue.fields.renderer.wiki.resolvers;

import com.atlassian.jira.issue.fields.renderer.wiki.links.AnchorLink;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.ContentLinkResolver;
import com.atlassian.renderer.links.GenericLinkParser;
import com.atlassian.renderer.links.Link;
import com.opensymphony.util.TextUtils;

import java.text.ParseException;

/** @since 3.12 */
public class AnchorLinkResolver implements ContentLinkResolver
{
    public Link createContentLink(RenderContext context, GenericLinkParser parser) throws ParseException
    {
        if (TextUtils.stringSet(parser.getAnchor()))
        {
            return new AnchorLink(parser);
        }
        else
        {
            return null;
        }
    }
}
