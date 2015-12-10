package com.atlassian.jira.web.action.issue;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.query.Query;

/**
 * Utility for getting search results for issue navigation
 *
 * @since v5.2
 */
@Internal
public interface IssueNavigatorSearchResultsHelper
{
    SearchResultsInfo getSearchResults(Query query, boolean isPageChanged) throws SearchException;

    void ensureAnIssueIsSelected(SearchResultsInfo searchResults, boolean isPagingToPreviousPage);

    void resetPagerAndSelectedIssue();

}
