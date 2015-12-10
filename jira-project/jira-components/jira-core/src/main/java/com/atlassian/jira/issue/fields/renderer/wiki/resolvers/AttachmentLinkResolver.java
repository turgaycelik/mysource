package com.atlassian.jira.issue.fields.renderer.wiki.resolvers;

import com.atlassian.jira.issue.fields.renderer.wiki.links.JiraAttachmentLink;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.ContentLinkResolver;
import com.atlassian.renderer.links.GenericLinkParser;
import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.links.UnresolvedLink;
import com.opensymphony.util.TextUtils;

import java.text.ParseException;

/** @since 3.12 */
public class AttachmentLinkResolver implements ContentLinkResolver
{
    public Link createContentLink(RenderContext context, GenericLinkParser parser) throws ParseException
    {
        if (TextUtils.stringSet(parser.getAttachmentName()))
        {
            JiraAttachmentLink attachmentLink = new JiraAttachmentLink(parser, context);

            if (attachmentLink.getAttachment() == null)
            {
                return new UnresolvedLink(parser.getOriginalLinkText());
            }

            return attachmentLink;
        }
        else
        {
            return null;
        }

    }
}
