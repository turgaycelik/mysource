package com.atlassian.jira.web.action.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.session.SessionPagerFilterManager;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.session.SessionSelectedIssueManager;
import com.atlassian.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility for getting search results for issue navigation
 *
 * @since v5.2
 */
public class IssueNavigatorSearchResultsHelperImpl implements IssueNavigatorSearchResultsHelper
{
    private static final int FIND_ISSUE_WINDOW_SIZE = 20;

    private final SearchProvider searchProvider;
    private final JiraAuthenticationContext authContext;
    private final SearchActionHelper searchActionHelper;
    private final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;

    public IssueNavigatorSearchResultsHelperImpl(SearchProvider searchProvider, JiraAuthenticationContext authContext,
                                                 SearchActionHelper searchActionHelper,
                                                 SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory)
    {
        this.searchProvider = searchProvider;
        this.authContext = authContext;
        this.searchActionHelper = searchActionHelper;
        this.sessionSearchObjectManagerFactory = sessionSearchObjectManagerFactory;
    }

    @Override
    public SearchResultsInfo getSearchResults(Query query, boolean isPageChanged) throws SearchException
    {
        SearchResultsInfo sr = new SearchResultsInfo();
        final PagerFilter navigatorPager = getPager();

        // A consistent view of the selected issue data (avoids race condition from Ajax setting it
        // while we generate the search results).
        final SessionSelectedIssueManager.SelectedIssueData selectedIssueData = getSessionSelectedIssueManager().getCurrentObject();

        final boolean isReturningToSearch = !isPageChanged && selectedIssueData != null;

        if (isReturningToSearch)
        {
            final Predicate<Issue> currentIssuePredicate;
            final Predicate<Issue> nextIssuePredicate;
            final int expectedIndex;
            currentIssuePredicate = new Predicate<Issue>()
            {
                public boolean evaluate(final Issue input)
                {
                return input.getId().equals(selectedIssueData.getSelectedIssueId());
                }
            };
            nextIssuePredicate = new Predicate<Issue>()
            {
                public boolean evaluate(final Issue input)
                {
                return input.getId().equals(selectedIssueData.getNextIssueId());
                }
            };
            expectedIndex = selectedIssueData.getSelectedIssueIndex();

            // Bounds of the search window (which may only partially intersect some pages).
            final int fromIndex = Math.max(0, expectedIndex - FIND_ISSUE_WINDOW_SIZE);
            final int toIndexExclusive = expectedIndex + FIND_ISSUE_WINDOW_SIZE + 1;

            List<PagerFilter> searchWindowPagers = pagersToSearch(navigatorPager, fromIndex, expectedIndex, toIndexExclusive);
            List<SearchResults> searchWindowResults = new ArrayList<SearchResults>();

            // Look for the last viewed issue in the search window near its expected position.
            for (PagerFilter pager : searchWindowPagers)
            {
                SearchResults result = searchProvider.search(query, getLoggedInUser(), new PagerFilter(pager));
                Issue currentIssue = CollectionUtil.findFirstMatch(intersection(result.getIssues(), pager, fromIndex, toIndexExclusive), currentIssuePredicate);
                if (currentIssue != null)
                {
                    setPagerStart(pager.getStart());
                    sr.selectedIssueId = currentIssue.getId();
                    sr.searchResults = result;
                    break;
                }
                searchWindowResults.add(result);
            }

            if (sr.searchResults == null)
            {
                // The last viewed issue wasn't found in the search window, so check the next issue.
                for (int i = 0; i < searchWindowPagers.size(); i++)
                {
                    PagerFilter pager = searchWindowPagers.get(i);
                    SearchResults result = searchWindowResults.get(i);
                    Issue nextIssue = CollectionUtil.findFirstMatch(intersection(result.getIssues(), pager, fromIndex, toIndexExclusive), nextIssuePredicate);
                    if (nextIssue != null)
                    {
                        setPagerStart(pager.getStart());
                        sr.selectedIssueId = nextIssue.getId();
                        sr.searchResults = result;
                        break;
                    }
                }
            }

            if (sr.searchResults == null)
            {
                // The next issue wasn't found either...
                if (searchWindowResults.isEmpty())
                {
                    // This should mean there are no results for the search, but fallback to the old search just in case.
                    sr.searchResults = searchProvider.search(query, getLoggedInUser(), navigatorPager);
                }
                else
                {
                    // ...so put them on the page where the issue would have been...
                    SearchResults result = searchWindowResults.get(0);
                    if (!result.getIssues().isEmpty())
                    {
                        setPagerStart(result.getStart());
                        sr.searchResults = result;
                    }
                    else
                    {
                        // ...unless it was the lone issue on that page, so just revert to first page of results.
                        PagerFilter pager = new PagerFilter(navigatorPager);
                        pager.setStart(0);
                        sr.searchResults = searchProvider.search(query, getLoggedInUser(), pager);
                    }
                }
            }
        }
        else
        {
            sr.searchResults = searchProvider.search(query, getLoggedInUser(), navigatorPager);
        }

        return sr;
    }

    @Override
    public void ensureAnIssueIsSelected(final SearchResultsInfo sr, boolean isPagingToPreviousPage)
    {
        final List<Issue> issuesInPage = sr.searchResults.getIssues();
        int selectedIssueIndex;
        Long nextIssueId = null;
        if (!issuesInPage.isEmpty())
        {
            if (isPagingToPreviousPage)
            {
                sr.selectedIssueId = issuesInPage.get(issuesInPage.size() - 1).getId();
                selectedIssueIndex = sr.searchResults.getStart() + issuesInPage.size() - 1;
            }
            else
            {
                int index = CollectionUtil.indexOf(issuesInPage, new Predicate<Issue>()
                {
                    public boolean evaluate(final Issue input)
                    {
                        return input.getId().equals(sr.selectedIssueId);
                    }
                });
                boolean isSelectedIssueInPage = index >= 0;
                if (isSelectedIssueInPage)
                {
                    selectedIssueIndex = sr.searchResults.getStart() + index;
                    if (index < issuesInPage.size() - 1)
                    {
                        nextIssueId = issuesInPage.get(index + 1).getId();
                    }
                }
                else
                {
                    sr.selectedIssueId = issuesInPage.get(0).getId();
                    selectedIssueIndex = sr.searchResults.getStart();
                    if (issuesInPage.size() > 1)
                    {
                        nextIssueId = issuesInPage.get(1).getId();
                    }
                }
            }
            getSessionSelectedIssueManager().setCurrentObject(new SessionSelectedIssueManager.SelectedIssueData(sr.selectedIssueId, selectedIssueIndex, nextIssueId));
        }
        else
        {
            clearSelectedIssue();
        }
    }

    @Override
    public void resetPagerAndSelectedIssue()
    {
        searchActionHelper.resetPager();
        clearSelectedIssue();
    }

    /**
     * Returns a list of pagers to search with its order optimized around the expected index.
     */
    private static List<PagerFilter> pagersToSearch(PagerFilter pager, int fromIndex, int expectedIndex, int toIndexExclusive)
    {
        final int pageSize = pager.getPageSize();
        List<PagerFilter> pagers = new ArrayList<PagerFilter>();

        PagerFilter expectedPagePager = PagerFilter.newPageAlignedFilter(expectedIndex, pageSize);
        if (isOverlapping(expectedPagePager, fromIndex, toIndexExclusive))
        {
            pagers.add(expectedPagePager);
        }

        PagerFilter left = expectedPagePager.getStart() == 0 ? null : new PagerFilter(expectedPagePager.getStart() - pageSize, pageSize);
        PagerFilter right = new PagerFilter(expectedPagePager.getStart() + pageSize, pageSize);

        while ((left != null && isOverlapping(left, fromIndex, toIndexExclusive)) || isOverlapping(right, fromIndex, toIndexExclusive))
        {
            if (left != null && isOverlapping(left, fromIndex, toIndexExclusive))
            {
                pagers.add(left);
                left = left.getStart() == 0 ? null : new PagerFilter(left.getStart() - pageSize, pageSize);
            }
            if (isOverlapping(right, fromIndex, toIndexExclusive))
            {
                pagers.add(right);
                right = new PagerFilter(right.getStart() + pageSize, pageSize);
            }
        }

        return pagers;
    }

    private static boolean isOverlapping(PagerFilter pager, int fromIndex, int toIndexExclusive)
    {
        return pager.getStart() < toIndexExclusive && fromIndex < pager.getStart() + pager.getPageSize();
    }

    /**
     * Returns the intersection of Issues from the Given Page, with the given fromIndex and toIndexExclusive.
     *
     * @param issuesInPage A List of issues in the "current page" (Note that this could be smaller than the actual page size if we are on the last page or further).
     * @param pager The Pager that defines the current page.
     * @param fromIndex From Index (inclusive) to use for our intersection.
     * @param toIndexExclusive to Index (exclusive) to use for our intersection.
     * @return the intersection of Issues from the Given Page, with the given fromIndex and toIndexExclusive.
     */
    static List<Issue> intersection(List<Issue> issuesInPage, PagerFilter pager, int fromIndex, int toIndexExclusive)
    {
        // Check if there are enough Issues in the page for our fromIndex
        if (fromIndex >= pager.getStart() + issuesInPage.size())
        {
            return Collections.emptyList();
        }

        if (isOverlapping(pager, fromIndex, toIndexExclusive))
        {
            int offset = pager.getStart();
            return issuesInPage.subList(
                    Math.max(0, fromIndex - offset),
                    Math.min(Math.max(0, toIndexExclusive - offset), issuesInPage.size())
            );
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private void clearSelectedIssue()
    {
        getSessionSelectedIssueManager().setCurrentObject(new SessionSelectedIssueManager.SelectedIssueData(null, 0, null));
    }

    private User getLoggedInUser()
    {
        return authContext.getLoggedInUser();
    }

    private void setPagerStart(int start)
    {
        getPager().setStart(start);
    }

    private PagerFilter getPager()
    {
        PagerFilter pager = getSessionPagerFilterManager().getCurrentObject();

        if (pager == null)
        {
            pager = searchActionHelper.resetPager();
        }
        return pager;
    }

    private SessionSelectedIssueManager getSessionSelectedIssueManager()
    {
        return sessionSearchObjectManagerFactory.createSelectedIssueManager();
    }

    private SessionPagerFilterManager getSessionPagerFilterManager()
    {
        return sessionSearchObjectManagerFactory.createPagerFilterManager();
    }
}
