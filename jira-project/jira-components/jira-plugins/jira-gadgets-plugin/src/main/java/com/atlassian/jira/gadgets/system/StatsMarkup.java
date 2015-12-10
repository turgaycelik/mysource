package com.atlassian.jira.gadgets.system;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

///CLOVER:OFF
/**
 *
 */
@XmlRootElement
public class StatsMarkup
{
    @XmlElement
    private String html;
    @XmlElement
    private List<String> classes;

    public StatsMarkup(String html, List<String> classes)
    {
        this.html = html;
        this.classes = classes;
    }

    public StatsMarkup(String html)
    {
        this.html = html;
    }

    private StatsMarkup()
    {
    }

    public String getHtml()
    {
        return html;
    }

    public List<String> getClasses()
    {
        return classes;
    }
}
