package com.atlassian.jira.bc.issue.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;

import java.util.Collection;

/**
 * Simple Bean for returning back the results of an Issue Picker provider
 */
@PublicApi
public class IssuePickerResults
{
    private final Collection<Issue> issues;
    private final Collection<String> keyTerms;
    private final Collection<String> summaryTerms;

    private final String label;
    private final String id;
    private final int totalIssues;

    /**
     * Full constructor for results
     *
     * @param issues       The issues that were selected
     * @param total        The total number of issues that match criteria
     * @param keyTerms     The terms that searched for in the key
     * @param summaryTerms The terms that were searched for in the summary
     * @param label        The section label
     * @param id           The unique id of the section
     */
    public IssuePickerResults(final Collection<Issue> issues, final int total, final Collection<String> keyTerms, final Collection<String> summaryTerms, final String label, final String id)
    {
        this.issues = issues;
        this.keyTerms = keyTerms;
        this.summaryTerms = summaryTerms;
        this.label = label;
        this.id = id;
        totalIssues = total;
    }

    public Collection<Issue> getIssues()
    {
        return issues;
    }

    public Collection<String> getKeyTerms()
    {
        return keyTerms;
    }

    public String getLabel()
    {
        return label;
    }

    public Collection<String> getSummaryTerms()
    {
        return summaryTerms;
    }

    public String getId()
    {
        return id;
    }

    public int getTotalIssues()
    {
        return totalIssues;
    }
}
