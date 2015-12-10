package com.atlassian.jira.functest.framework.util;

import com.atlassian.jira.functest.framework.util.SearchRendererValueResults;
import com.atlassian.jira.functest.framework.util.Searchers;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulates Searchers, their values and search results
 * allowing all of these to be returned with one ajax request.
 *
 * @since v5.1
 */
@XmlRootElement
@JsonIgnoreProperties
public class SearchResults
{
    @XmlElement
    public final Searchers searchers;
    @XmlElement
    public final SearchRendererValueResults values;

    public SearchResults()
    {
        this.searchers = null;
        this.values = null;
    }

    public SearchResults(Searchers searchers, SearchRendererValueResults values)
    {
        this.searchers = searchers;
        this.values = values;
    }
}

