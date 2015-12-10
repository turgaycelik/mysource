package com.atlassian.jira.issue.fields.renderer.wiki.links;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.util.URLCodec;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.BaseLink;
import com.atlassian.renderer.links.GenericLinkParser;
import org.ofbiz.core.entity.GenericEntityException;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Collection;

/**
 * Defines a link to a jira attachment. 
 */
public class JiraAttachmentLink extends BaseLink
{
    private Attachment attachment;
    public static final String ATTACHMENT_ICON = "attachment";

    public JiraAttachmentLink(GenericLinkParser parser, RenderContext context) throws ParseException
    {
        super(parser);
        Issue issue = (Issue) context.getParam(AtlassianWikiRenderer.ISSUE_CONTEXT_KEY);
        if(issue == null || issue.getGenericValue() == null)
        {
            throw new ParseException("Can not resolve attachment with name " + parser.getAttachmentName() + " no issue in context.", 0);
        }
        try
        {
            attachment = getAttachment(issue, parser.getAttachmentName());

            if (attachment != null)
            {
                url = buildAttachmentUrl(context, attachment);
                setTitle(attachment.getFilename() + " attached to " + issue.getKey());
                iconName = ATTACHMENT_ICON;
            }

            if (linkBody.startsWith("^") && linkBody.length() > 1)
                linkBody = linkBody.substring(1);
        }
        catch(Exception e)
        {
            // just don't create a link
            attachment = null;
        }

    }

    public Attachment getAttachment()
    {
        return attachment;
    }

    private Attachment getAttachment(Issue issue, String attachementName) throws GenericEntityException
    {
        Attachment attachment = null;

        Collection<Attachment> attachments = issue.getAttachments();
        for (final Attachment tempAttachement : attachments)
        {
            if (tempAttachement.getFilename().equals(attachementName))
            {
                // Since the list is sorted by filename and date we know the first is the most recent
                attachment = tempAttachement;
                break;
            }
        }
        return attachment;
    }

    private String buildAttachmentUrl(RenderContext context, Attachment attachment) throws UnsupportedEncodingException
    {
        String encodedAttachmentId = URLCodec.encode(attachment.getId().toString(), context.getCharacterEncoding());
        return context.getSiteRoot() + "/secure/attachment/" + encodedAttachmentId + "/" + encodedAttachmentId + "_" +
                URLCodec.encode(attachment.getFilename(), context.getCharacterEncoding());
    }

}