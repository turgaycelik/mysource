package com.atlassian.jira.issue.fields.renderer.wiki.embedded;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderContextOutputType;
import com.atlassian.renderer.attachments.RendererAttachment;
import com.atlassian.renderer.attachments.RendererAttachmentManager;
import com.atlassian.renderer.embedded.EmbeddedImage;
import com.atlassian.renderer.embedded.EmbeddedImageRenderer;
import com.atlassian.renderer.embedded.EmbeddedResource;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.v2.components.HtmlEscaper;
import com.opensymphony.util.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class JiraEmbeddedImageRenderer extends EmbeddedImageRenderer
{
    private final RendererAttachmentManager attachmentManager;

    private long attachmentId;
    private String attachmentSrc;
    private String attachmentName;
    private boolean isThumbNail;

    public JiraEmbeddedImageRenderer(RendererAttachmentManager attachmentManager)
    {
        super(attachmentManager);
        this.attachmentManager = attachmentManager;
    }

    @Override
    public String renderResource(EmbeddedResource resource, RenderContext context)
    {
        EmbeddedImage image = (EmbeddedImage) resource;
        isThumbNail = image.isThumbNail();
        if (isThumbNail && !image.isExternal())
        {
            try
            {
                RendererAttachment attachment = getAttachment(context, resource);
                attachmentId = attachment.getId();
                attachmentSrc = attachment.getSrc();
                attachmentName = attachment.getFileName();
            }
            catch (RuntimeException re)
            {
                // Give up on the thumbnail
            }
        }

        // TODO IN JIRA 6.0 - Restore delegation to the superclass JRADEV-13033 
        // return super.renderResource(resource, context);
        return renderResourceCompat(resource, context);
    }

    // TODO IN JIRA 6.0 - Remove all of this except the thumbnail wrapping
    @Override
    protected String writeImage(String imageTag, Map<Object, Object> imageParams, RenderContext context)
    {
        return writeImage(imageTag, extractCenteredParam(imageParams));
    }

    private String writeImage(String imageTag, boolean centered)
    {
        if (!centered && !isThumbNail)
        {
            return imageTag;
        }

        final StringBuilder sb = new StringBuilder(imageTag.length() + 64);
        if (centered)
        {
            sb.append("<div align=\"center\">");
        }

        if (isThumbNail)
        {
            //wrap the thumbnail image with the link to the actual attachment (the full-sized version) JRA-11198
            sb.append("<a id=\"").append(attachmentId).append("_thumb\" href=\"")
                    .append(attachmentSrc).append("\" title=\"").append(attachmentName)
                    .append("\">");
        }

        sb.append(imageTag);

        if (isThumbNail)
        {
            sb.append("</a>");
        }

        if (centered)
        {
            sb.append("</div>");
        }
        return sb.toString();
    }

    private boolean extractCenteredParam(Map imageParams)
    {
        final String align = (String)imageParams.get("align");
        final boolean centered = "center".equalsIgnoreCase(align) || "centre".equalsIgnoreCase(align);
        if (centered)
        {
            imageParams.remove("align");
        }
        return centered;
    }

    private String originalLink(EmbeddedResource resource)
    {
        return "!" + resource.getOriginalLinkText() + '!';
    }

    private String renderResourceCompat(EmbeddedResource resource, RenderContext context)
    {
        String token;

        final EmbeddedImage image = (EmbeddedImage)resource;

        RendererAttachment attachment = null;
        if (!image.isExternal())
        {
            try
            {
                attachment = getAttachment(context, resource);
            }
            catch(RuntimeException re)
            {
                return context.addRenderedContent(RenderUtils.error(re.getMessage()));
            }
        }

        Map<Object,Object> imageParams = new HashMap<Object,Object>();
        imageParams.putAll(image.getProperties());
        if (context.isRenderingForWysiwyg())
        {
            imageParams.put("imagetext", resource.getOriginalLinkText());
        }
        if (image.isThumbNail())
        {
            if (image.isExternal())
            {
                token = context.addRenderedContent(RenderUtils.error(context, "Can only create thumbnails for attached images", originalLink(resource), false));
            }
            else if (!attachmentManager.systemSupportsThumbnailing())
            {
                token = context.addRenderedContent(RenderUtils.error(context, "This installation can not generate thumbnails: no image support in Java runtime", originalLink(resource), false));
            }
            else if (attachment == null && !context.isRenderingForWysiwyg())
            {
                token = context.addRenderedContent(RenderUtils.error(context, "Attachment '" + image.getFilename() + "' was not found", originalLink(resource), false));
            }
            else
            {
                token = context.addRenderedContent(generateThumbnail(imageParams, attachment, context, image));
            }
        }
        else
        {
            StringBuilder imageUrl = new StringBuilder();

            if (image.isExternal())
            {
                imageUrl.append(image.getUrl());
            }
            else
            {
                if (attachment != null)
                {
                    // For internal attachments, the src attribute in RenderAttachment is an absolute path, but without the domain name...
                    // When rendering for Word output, we need to prefix it with the full URL (CONF-5293)
                    if (context.getOutputType().equals(RenderContextOutputType.WORD))
                    {
                        // Grab the context path
                        String contextPath = context.getSiteRoot();
                        String domain = context.getBaseUrl();

                        // The baseUrl contains the context path, so strip it off (if it exists)
                        // This is quite dodgy, but fiddling with the URL passed into the RenderAttachment is going to be troublesome
                        if (contextPath != null && contextPath.length() != 0)
                        {
                            final int index = domain.indexOf(contextPath);
                            if (index != -1)
                            {
                                domain = domain.substring(0, index);
                            }
                        }

                        imageUrl.append(domain);
                    }

                    imageUrl.append(attachment.getSrc());
                }
            }

            boolean centered = extractCenteredParam(imageParams);
            token = context.addRenderedContent(writeImage("<img src=\"" + HtmlEscaper.escapeAll(imageUrl.toString(), false) + "\" " + outputParameters(imageParams) + "/>", centered));
        }

        return token;
    }

    private String generateThumbnail(Map<Object,Object> imageParams, RendererAttachment attachment, RenderContext context, EmbeddedImage embeddedImage)
    {
        if (attachment != null && TextUtils.stringSet(attachment.getComment()) && !imageParams.containsKey("title") && !imageParams.containsKey("TITLE"))
        {
            imageParams.put("title", attachment.getComment());
        }

        RendererAttachment thumb = null;
        if (attachment != null)
        {
            try
            {
                thumb = getThumbnail(attachment, context, embeddedImage);
            }
            catch (RuntimeException re)
            {
                return context.addRenderedContent(RenderUtils.error(re.getMessage()));
            }
        }

        boolean centered = extractCenteredParam(imageParams);
        if (thumb != null)
        {
            return writeImage(thumb.wrapGeneratedElement("<img src=\"" + thumb.getSrc() + "\" " + outputParameters(imageParams) + "/>"), centered);
        }
        else
        {
            // even if the attachment is invalid, we still need to create some markup for it
            return writeImage("<img " + outputParameters(imageParams) + "/>", centered);
        }
    }

    private String outputParameters(Map<Object,Object> params)
    {
        if (params.isEmpty())
        {
            return " border='0' ";
        }

        final StringBuilder buff = new StringBuilder(params.size() * 20);
        final Map<Object,Object> sortedParams = new TreeMap<Object,Object>(params);

        boolean hasNoBorderSet = true;
        for (final Map.Entry<Object,Object> param : sortedParams.entrySet())
        {
            final String key = (String)param.getKey();
            if (hasNoBorderSet && "border".equals(key))
            {
                hasNoBorderSet = false;
            }
            buff.append(HtmlEscaper.escapeAll(key, true)).append("=\"").
                    append(HtmlEscaper.escapeAll((String)param.getValue(), true)).append("\" ");
        }

        if (hasNoBorderSet)
        {
            buff.append(" border='0' ");
        }
        return buff.toString();
    }
}


