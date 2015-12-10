package com.atlassian.jira.functest.framework.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of searcher group that contains only visible searchers
 * @since v5.1
 */
@XmlRootElement
public class FilteredSearcherGroup
{
    @XmlElement
    private List<Searcher> searchers;
    @XmlElement
    private String type;
    @XmlElement
    private String title;

    public FilteredSearcherGroup()
    {
        this(null);
    }

    public FilteredSearcherGroup(final String type)
    {
        this.type = type;
        this.title = null;
        this.searchers = new ArrayList<Searcher>();
    }

    public void addSearcher(Searcher searcher)
    {
        searchers.add(searcher);
    }

    public List<Searcher> getSearchers()
    {
        return searchers;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getType()
    {
        return type;
    }
}

