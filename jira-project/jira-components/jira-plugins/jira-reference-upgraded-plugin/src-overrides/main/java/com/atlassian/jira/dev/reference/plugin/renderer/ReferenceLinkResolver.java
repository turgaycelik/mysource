package com.atlassian.jira.dev.reference.plugin.renderer;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.ContentLinkResolver;
import com.atlassian.renderer.links.GenericLinkParser;
import com.atlassian.renderer.links.Link;

import java.text.ParseException;

/** @since 3.12 */
public class ReferenceLinkResolver implements ContentLinkResolver
{
    private final JiraAuthenticationContext authenticationContext;

    public ReferenceLinkResolver(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    public Link createContentLink(RenderContext context, GenericLinkParser parser) throws ParseException
    {
        if (parser.getNotLinkBody().startsWith("="))
        {
            return new ReferenceGoogleLink(parser, context, authenticationContext.getI18nHelper());
        }
        else
        {
            return null;
        }
    }
}
