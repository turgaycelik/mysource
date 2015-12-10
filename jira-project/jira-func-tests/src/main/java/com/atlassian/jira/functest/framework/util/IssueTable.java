package com.atlassian.jira.functest.framework.util;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;
import java.util.Map;

/**
 * Serialisable IssueTable object.
 *
 * This class was pulled from IssueTableResource.IssueTable to prevent coupling to the gadgets project.
 * IssueTableResource should eventually use IssueTableService and therefore not need it's own internal IssueTable.
 *
 * @since v5.1
 */
///CLOVER:OFF
@JsonSerialize
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueTable
{
    @JsonProperty
    private Map<String, String> columnSortJql;
    @JsonProperty
    private String description;
    @JsonProperty
    private int displayed;
    @JsonProperty
    private int end;
    @JsonProperty
    private List<Long> issueIds;
    @JsonProperty
    private List<String> issueKeys;
    @JsonProperty
    private boolean jiraHasIssues;
    @JsonProperty
    private int page;
    @JsonProperty
    private int pageSize;
    @JsonProperty
    private int startIndex;
    @JsonProperty
    private String table;
    @JsonProperty
    private String title;
    @JsonProperty
    private int total;
    @JsonProperty
    private String url;
    @JsonProperty
    private Map<String, Object> sortBy;

    @SuppressWarnings ({ "UnusedDeclaration", "unused" })
    private IssueTable()
    {
    }

    public IssueTable(String table, int displayed, List<Long> issueIds, Map<String, Object> sortBy, int total, int startIndex, int end, int page, int pageSize,
            String url, String title, String description, Map<String, String> columnSortJql, boolean jiraHasIssues, List<String> issueKeys)
    {
        this.table = table;
        this.displayed = displayed;
        this.issueIds = issueIds;
        this.sortBy = sortBy;
        this.total = total;
        this.startIndex = startIndex;
        this.end = end;
        this.page = page;
        this.pageSize = pageSize;
        this.url = url;
        this.title = title;
        this.description = description;
        this.columnSortJql = columnSortJql;
        this.jiraHasIssues = jiraHasIssues;
    }

    public String getTable()
    {
        return table;
    }

    public int getTotal()
    {
        return total;
    }

    public boolean getJiraHasIssues()
    {
        return jiraHasIssues;
    }

    public List<Long> getIssueIds()
    {
        return issueIds;
    }

    public Map<String, Object> getSortBy()
    {
        return sortBy;
    }
}