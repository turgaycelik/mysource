package com.atlassian.jira.issue.fields.renderer.wiki.embedded;

import com.atlassian.renderer.attachments.RendererAttachmentManager;
import com.atlassian.renderer.embedded.DefaultEmbeddedResourceRenderer;
import com.atlassian.renderer.embedded.EmbeddedImage;

import java.util.HashMap;

public class JiraEmbeddedResourceRenderer extends DefaultEmbeddedResourceRenderer
{
    public JiraEmbeddedResourceRenderer(RendererAttachmentManager attachmentManager)
    {
        super(attachmentManager);
    }

    protected HashMap getRenderMap()
    {
        if (renderMap == null)
        {
            renderMap = super.getRenderMap();
            //override the super's EmbeddedImage key entry with JIRA's implementation
            renderMap.put(EmbeddedImage.class, new JiraEmbeddedImageRenderer(attachmentManager));
        }
        return renderMap;
    }
}
