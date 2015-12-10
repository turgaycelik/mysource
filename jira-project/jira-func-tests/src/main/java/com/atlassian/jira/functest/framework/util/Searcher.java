package com.atlassian.jira.functest.framework.util;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Searcher
 * @since v5.0
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class Searcher
{
    @XmlElement
    private String name;

    @XmlElement
    private String key;

    @XmlElement
    private Boolean isShown;

    @XmlElement
    private String id;

    @XmlElement
    private Long lastViewed;

    public Searcher()
    {
    }

    public Searcher(final String id, final String name)
    {
        this.id = id;
        this.name = name;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getKey()
    {
        return key;
    }

    public Boolean getShown()
    {
        return isShown;
    }

    public Long getLastViewed()
    {
        return lastViewed;
    }
}
