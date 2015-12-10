package com.atlassian.jira.rest.v2.search;

import com.atlassian.jira.rest.api.issue.JsonTypeBean;
import com.atlassian.jira.rest.v2.issue.IssueBean;
import com.atlassian.jira.rest.v2.issue.IssueResourceExamples;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.rest.api.util.StringList.fromList;
import static com.atlassian.jira.rest.api.util.StringList.fromQueryParam;
import static com.google.common.collect.Sets.newHashSet;

/**
 * JAXB bean for returning search results.
 *
 * @since v4.3
 */
@XmlRootElement
@JsonSerialize (include = JsonSerialize.Inclusion.NON_NULL)
public class SearchResultsBean
{
    /**
     * The example SearchResultsBean used in automatically-generated documentation.
     */
    public static final SearchResultsBean DOC_EXAMPLE = new SearchResultsBean(0, 50, 1, Arrays.asList(IssueResourceExamples.ISSUE_SHORT));

    public String expand;
    public Integer startAt;
    public Integer maxResults;
    public Integer total;
    public List<IssueBean> issues;
    public Set<String> warningMessages;
    public HashMap<String, String> names;
    public Map<String, JsonTypeBean> schema;

    public SearchResultsBean()
    {
    }

    public SearchResultsBean(Integer startAt, Integer maxResults, Integer total, List<IssueBean> issues)
    {
        this.startAt = startAt;
        this.maxResults = maxResults;
        this.total = total;
        this.issues = issues;

        pullUpSchemaAndNames(issues);
    }

    public SearchResultsBean(Integer startAt, Integer maxResults, Integer total, List<IssueBean> issues, Set<String> warningMessages)
    {
        this(startAt, maxResults, total, issues);
        if (warningMessages != null && !warningMessages.isEmpty())
        {
            this.warningMessages = warningMessages;
        }
    }

    /**
     * Pull up the schema and names from the issues in the search result.
     *
     * @param issues a List&lt;IssueBean&gt;
     */
    private void pullUpSchemaAndNames(List<IssueBean> issues)
    {
        for (IssueBean issue : issues)
        {
            pullUpNames(issue);
            pullUpSchema(issue);
        }
    }

    private void pullUpNames(IssueBean issue)
    {
        pullUpExpandParam(issue, "names");
        addToNames(issue.names());
        issue.names(null);
    }

    private void pullUpSchema(IssueBean issue)
    {
        pullUpExpandParam(issue, "schema");
        addToSchema(issue.schema());
        issue.schema(null);
    }

    private void pullUpExpandParam(IssueBean issue, String field)
    {
        Set<String> fieldToRemove = Collections.singleton(field);

        // remove from the issue
        Set<String> issueExpand = Sets.difference(Sets.newHashSet(issue.expand()), fieldToRemove);
        issue.expand(issueExpand);

        // add to the search results
        Set<String> searchExpand = newHashSet(fromQueryParam(expand).asList());
        expand = fromList(Sets.union(searchExpand, Collections.<String>singleton(field))).toQueryParam();
    }

    private void addToSchema(@Nullable Map<String, JsonTypeBean> issueSchema)
    {
        if (issueSchema != null)
        {
            if (schema == null) { schema = Maps.newHashMap(); }
            schema.putAll(issueSchema);
        }
    }

    private void addToNames(@Nullable Map<String, String> issueNames)
    {
        if (issueNames != null)
        {
            if (this.names == null) { this.names = Maps.newHashMap(); }
            this.names.putAll(issueNames);
        }
    }
}
