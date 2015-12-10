package com.atlassian.jira.rest.v2.search;

import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB bean for search requests.
 *
 * @since v4.3
 */
@XmlRootElement
public class SearchRequestBean
{
    /**
     * Example representation for use in auto-generated docs.
     */
    public static final SearchRequestBean DOC_EXAMPLE = new SearchRequestBean("project = HSP", 0, 15, Arrays.asList("summary", "status", "assignee"));

    /**
     * A JQL query string.
     */
    public String jql;

    /**
     * The index of the first issue to return (0-based).
     */
    public Integer startAt;

    /**
     * The maximum number of issues to return (defaults to 50). The maximum allowable value is dictated by the JIRA
     * property 'jira.search.views.default.max'. If you specify a value that is higher than this number, your search
     * results will be truncated.
     */
    public Integer maxResults;

    /**
     * The list of fields to return for each issue. If null, all fields are returned.
     */
    public List<String> fields;

    /**
     * Whether to validate the JQL query.
     */
    public Boolean validateQuery;

    /**
     * The list of issue parameters to expand on each issue.
     */
    public List<String> expand;

    public SearchRequestBean()
    {
    }

    public SearchRequestBean(String jql, Integer startAt, Integer maxResults, List<String> fields)
    {
        this.jql = jql;
        this.startAt = startAt;
        this.maxResults = maxResults;
        this.fields = fields;
    }
}
