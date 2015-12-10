package com.atlassian.jira.rest.api.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean representing an icon.
 *
 * @since v5.0
 */
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name="icon")
public class IconBean
{
    @XmlElement
    private String url16x16;

    @XmlElement
    private String title;

    @XmlElement
    private String link;

    //Needed so that JAXB works.
    public IconBean(){}

    public IconBean(String url16x16, String title, String link)
    {
        this.url16x16 = url16x16;
        this.title = title;
        this.link = link;
    }

    public String getUrl16x16()
    {
        return url16x16;
    }

    public String getTitle()
    {
        return title;
    }

    public String getLink()
    {
        return link;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        IconBean that = (IconBean) o;

        if (!link.equals(that.link)) { return false; }
        if (url16x16 != null ? !url16x16.equals(that.url16x16) : that.url16x16 != null) { return false; }
        if (title != null ? !title.equals(that.title) : that.title != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = url16x16 != null ? url16x16.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + link.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "IconBean{" +
                "url16x16='" + url16x16 + '\'' +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
