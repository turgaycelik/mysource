package com.atlassian.jira.issue.fields.renderer.wiki;

import com.atlassian.jira.issue.fields.renderer.wiki.links.JiraAttachmentLink;
import com.atlassian.jira.issue.fields.renderer.wiki.links.JiraUserProfileLink;
import com.atlassian.renderer.DefaultIconManager;
import com.atlassian.renderer.Icon;
import com.atlassian.renderer.links.UrlLink;

import java.util.HashMap;
import java.util.Map;

/**
 * Icon manager for Jira that adds icons that the renderer will show that are specific to Jira.
 */
public class JiraIconManager extends DefaultIconManager
{
    protected Map getIconsMap()
    {
        @SuppressWarnings ({ "unchecked" })
        Map<String,Icon> iconsMap = super.getIconsMap();
        if (iconsMap != null)
        {
            iconsMap = new HashMap<String,Icon>(iconsMap);
            iconsMap.put(JiraAttachmentLink.ATTACHMENT_ICON, Icon.makeRenderIcon("icons/link_attachment_7.gif", Icon.ICON_RIGHT, 7, 7));
            // JRADEV-825
            iconsMap.remove(UrlLink.EXTERNAL_ICON);
        }
        return iconsMap;
    }
}