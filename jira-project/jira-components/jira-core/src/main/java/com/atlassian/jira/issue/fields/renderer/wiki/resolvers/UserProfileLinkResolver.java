package com.atlassian.jira.issue.fields.renderer.wiki.resolvers;

import com.atlassian.jira.issue.fields.renderer.wiki.links.JiraUserProfileLink;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.ContentLinkResolver;
import com.atlassian.renderer.links.GenericLinkParser;
import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.links.UnresolvedLink;

import java.text.ParseException;

/** @since 3.12 */
public class UserProfileLinkResolver implements ContentLinkResolver
{
    public Link createContentLink(RenderContext context, GenericLinkParser parser) throws ParseException
    {
        if (parser.getNotLinkBody().startsWith("~"))
        {
            JiraUserProfileLink profileLink = new JiraUserProfileLink(parser, context);

            if (profileLink.getUser() == null)
            {
                return new UnresolvedLink(parser.getOriginalLinkText());
            }

            return profileLink;
        }
        else
        {
            return null;
        }
    }
}
