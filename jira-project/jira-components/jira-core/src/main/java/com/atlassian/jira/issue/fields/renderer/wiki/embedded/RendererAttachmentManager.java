package com.atlassian.jira.issue.fields.renderer.wiki.embedded;

import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.issue.thumbnail.ThumbnailedImage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.URLCodec;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.attachments.RendererAttachment;
import com.atlassian.renderer.embedded.EmbeddedImage;
import com.atlassian.renderer.embedded.EmbeddedResource;
import com.atlassian.renderer.v2.RenderUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

/**
 * Implemenation of the RendererAttachmentManager that converts thumbnails and attachments into useable objects
 * for the wiki renderer.
 */
public class RendererAttachmentManager implements com.atlassian.renderer.attachments.RendererAttachmentManager
{

    private static final Logger log = Logger.getLogger(RendererAttachmentManager.class);

    private AttachmentManager attachmentManager;
    private ThumbnailManager thumbnailManager;
    private JiraAuthenticationContext authenticationContext;

    public RendererAttachmentManager(AttachmentManager attachmentManager, ThumbnailManager thumbnailManager, JiraAuthenticationContext authenticationContext)
    {
        this.attachmentManager = attachmentManager;
        this.thumbnailManager = thumbnailManager;
        this.authenticationContext = authenticationContext;
    }

    public RendererAttachment getAttachment(RenderContext context, EmbeddedResource resource)
    {
        // Make sure we have an issue and a usable issueGV from the issue
        Issue issue = (Issue) context.getParam(AtlassianWikiRenderer.ISSUE_CONTEXT_KEY);
        if (resource.isInternal() && issue == null || issue.getGenericValue() == null)
        {
            log.debug("No usable issue stored in the context, unable to resolve filename '" + resource.getFilename() + "'");
            throw new RuntimeException("No usable issue stored in the context, unable to resolve filename '" + resource.getFilename() + "'");
        }

        Attachment attachment = null;

        Collection<Attachment> attachments;
        attachments = issue.getAttachments();
        for (final Attachment tempAttachement : attachments)
        {
            if (tempAttachement.getFilename().equals(resource.getFilename()))
            {
                // Since the list is sorted by filename and date we know the first is the most recent
                attachment = tempAttachement;
                break;
            }
        }
        if (attachment == null)
        {
            return null;
        }
        else
        {
            return convertToRendererAttachment(attachment, context, resource);
        }
    }

    public RendererAttachment getThumbnail(RendererAttachment attachment, RenderContext context, EmbeddedImage image)
    {
        if (attachment != null && image.isThumbNail())
        {
            try
            {
                Attachment jiraAttachment = attachmentManager.getAttachment(new Long(attachment.getId()));
                if (thumbnailManager.isThumbnailable(jiraAttachment))
                {
                    try
                    {
                        ThumbnailedImage thumbnail = thumbnailManager.toThumbnailedImage(thumbnailManager.getThumbnail(jiraAttachment));
                        if (thumbnail != null)
                        {
                            return convertToRendererAttachment(thumbnail, jiraAttachment, context, image);
                        }

                    }
                    catch (Exception e)
                    {
                        log.warn("Error looking up thumbnails in RendererAttachmentManager.", e);
                    }
                }
            }
            catch (GenericEntityException e)
            {
                log.warn("Error resolving attachment with id: " + attachment.getId(), e);
            }
        }
        return null;
    }

    public boolean systemSupportsThumbnailing()
    {
        return true;
    }

    private RendererAttachment convertToRendererAttachment(Attachment attachment, RenderContext context, EmbeddedResource resource)
    {
        return new RendererAttachment(attachment.getId().longValue(), attachment.getFilename(), attachment.getMimetype(),
                attachment.getAuthorKey(), null, buildAttachmentUrl(context, attachment), null, null, attachment.getCreated());
    }

    private RendererAttachment convertToRendererAttachment(ThumbnailedImage thumbnail, Attachment attachment, RenderContext context, EmbeddedResource resource)
    {
        return new RendererAttachment(thumbnail.getAttachmentId(), thumbnail.getFilename(), attachment.getMimetype(),
                attachment.getAuthorKey(), null, thumbnail.getImageURL(), null, null, attachment.getCreated());
    }

    private String createError(RenderContext context, EmbeddedResource resource)
    {
        return context.addRenderedContent(RenderUtils.error(context, "Unable to resolve filename '" + resource.getFilename() + "'", originalLink(resource), false));
    }

    private String originalLink(EmbeddedResource resource)
    {
        return "!" + resource.getOriginalLinkText() + "!";
    }

    private String buildAttachmentUrl(RenderContext context, Attachment attachment)
    {
        String encodedAttachmentId;
        String encodedFilename;
        try
        {
            encodedAttachmentId = URLCodec.encode(attachment.getId().toString(), context.getCharacterEncoding());
            encodedFilename = URLCodec.encode(attachment.getFilename(), context.getCharacterEncoding());
        }
        catch (UnsupportedEncodingException uee)
        {
            encodedAttachmentId = attachment.getId().toString();
            encodedFilename = attachment.getFilename();
        }
        return context.getSiteRoot() + "/secure/attachment/" + encodedAttachmentId + "/" + encodedAttachmentId + "_" + encodedFilename;
    }
}
