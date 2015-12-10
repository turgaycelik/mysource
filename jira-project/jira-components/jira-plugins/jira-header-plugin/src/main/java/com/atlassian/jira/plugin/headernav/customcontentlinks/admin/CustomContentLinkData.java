package com.atlassian.jira.plugin.headernav.customcontentlinks.admin;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Used to marshal REST requests/responses
 *
 * Candidate for moving to navlinks plugin
 */
@XmlRootElement
public class CustomContentLinkData
{
    public CustomContentLinkData(String id, String contentKey, String linkLabel, String linkUrl)
    {
        this.id = id;
        this.contentKey = contentKey;
        this.linkUrl = linkUrl;
        this.linkLabel = linkLabel;
    }

    public CustomContentLinkData()
    {
    }

    public String id;
    public String contentKey;
    public String linkUrl;
    public String linkLabel;
}
