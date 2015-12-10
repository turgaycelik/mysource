package com.atlassian.jira.web.action.issue.navigator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.0
 */
@XmlRootElement
public class ToolOptionItem
{
    @XmlElement
    public final String id;
    @XmlElement
    public final boolean relativeLink;
    @XmlElement
    public final String link;
    @XmlElement
    public final String label;
    @XmlElement
    public final String title;
    @XmlElement
    public final String rel;

    public ToolOptionItem(String id, String label, String link, String title)
    {
        this(id, label, link, false, title, null);
    }

    public ToolOptionItem(String id, String label, String link, boolean relativeLink, String title)
    {
        this(id, label, link, relativeLink, title, null);
    }

    public ToolOptionItem(String id, String label, String link, String title, String rel)
    {
        this(id, label, link, false, title, null);
    }

    public ToolOptionItem(String id, String label, String link, boolean relativeLink, String title, String rel)
    {
        this.link = link;
        this.relativeLink = relativeLink;
        this.label = label;
        this.id = id;
        this.title = title;
        this.rel = rel;
    }

    public String getLabel()
    {
        return label;
    }

    public String getId()
    {
        return id;
    }

    public String getLink()
    {
        return link;
    }

    public boolean isRelativeLink()
    {
        return relativeLink;
    }

    public String getTitle()
    {
        return title;
    }
}
