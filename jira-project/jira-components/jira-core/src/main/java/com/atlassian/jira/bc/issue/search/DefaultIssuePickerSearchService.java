package com.atlassian.jira.bc.issue.search;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.util.profiling.UtilTimerStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Currently uses {@link com.atlassian.jira.bc.issue.search.HistoryIssuePickerSearchProvider} and
 * {@link com.atlassian.jira.bc.issue.search.LuceneCurrentSearchIssuePickerSearchProvider} to return issues.
 * History items are returned before Current search items.
 *
 * @see com.atlassian.jira.bc.issue.search.IssuePickerSearchService
 */
public class DefaultIssuePickerSearchService implements IssuePickerSearchService
{
    static DefaultIssuePickerSearchService create(final IssuePickerSearchProvider... providers)
    {
        return new DefaultIssuePickerSearchService(providers);
    }

    private final List<IssuePickerSearchProvider> searchProvidersCollection;
    private static final String RUNNING_ISSUE_PICKER_SEARCH = "Running issue-picker search: ";

    // for dependency injection
    public DefaultIssuePickerSearchService(HistoryIssuePickerSearchProvider historyProvider, LuceneCurrentSearchIssuePickerSearchProvider searchProvider)
    {
        this(new IssuePickerSearchProvider[]{historyProvider, searchProvider});
    }

    // for testing
    private DefaultIssuePickerSearchService(final IssuePickerSearchProvider... providers)
    {
        searchProvidersCollection = new ArrayList<IssuePickerSearchProvider>(Arrays.asList(providers));
    }

    /**
     * @see com.atlassian.jira.bc.issue.search.IssuePickerSearchService
     */
    public Collection<IssuePickerResults> getResults(final JiraServiceContext context, final IssuePickerParameters issuePickerParams)
    {
        final String timer = RUNNING_ISSUE_PICKER_SEARCH + issuePickerParams.getQuery();
        UtilTimerStack.push(timer);
        try
        {
            final Collection<IssuePickerResults> results = new ArrayList<IssuePickerResults>();
            int issuesRemaining = issuePickerParams.getLimit();
            for (final IssuePickerSearchProvider issuePickerSearchProvider : searchProvidersCollection)
            {
                if (issuePickerSearchProvider.handlesParameters(context.getLoggedInUser(), issuePickerParams))
                {
                    issuesRemaining--; // remove one item for heading

                    final IssuePickerResults result = issuePickerSearchProvider.getResults(context, issuePickerParams, issuesRemaining);

                    int size = result.getIssues().size();
                    if (size == 0)
                    {
                        size = 1; // if no items returned - add 1 for the no items row
                    }
                    issuesRemaining -= size;

                    results.add(result);
                    if (issuesRemaining <= 0)
                    {
                        return results;
                    }
                }
            }
            return results;
        }
        finally
        {
            UtilTimerStack.pop(timer);
        }
    }
}
