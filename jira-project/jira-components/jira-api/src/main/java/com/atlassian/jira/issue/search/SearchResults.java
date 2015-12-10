package com.atlassian.jira.issue.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.component.IssuePage;
import com.atlassian.jira.web.component.IssuePager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@PublicApi
public class SearchResults implements IssuePager
{
    /**
     * A collection of {@link com.atlassian.jira.issue.search.SearchResults.Page} objects
     */
    private Collection<Page> pages;
    private static final int PAGES_TO_LIST = 5;

    private final int start;
    private final int max;
    private final int total;
    private final List<Issue> issues;

    /**
     * Construct searchResults using a list of issues.  The issues returned by {@link #getIssues()} will
     * be a subset of the issues passed in.
     *
     * @param issues        A list of {@link com.atlassian.jira.issue.Issue} objects
     * @param pagerFilter   Representing which issues to limit the results to
     */
    public SearchResults(final List<Issue> issues, final PagerFilter<Issue> pagerFilter)
    {
        // Reset the pager filters start value if the current value is not sane
        if (issues.size() < pagerFilter.getStart())
        {
            pagerFilter.setStart(0);
        }
        start = pagerFilter.getStart();
        total = issues.size();
        max = pagerFilter.getMax();
        this.issues = pagerFilter.getCurrentPage(issues);
    }

    /**
     * Construct searchResults using the issues that should be displayed, and the 'total' number of issues.
     * This is used when a search does not wish to load the entire list of issues into memory.
     *
     * @param issuesInPage      A list of {@link com.atlassian.jira.issue.Issue} objects
     * @param totalIssueCount   The count of the number of issues returned
     * @param pagerFilter       Representing the users preference for paging
     */
    public SearchResults(final List<Issue> issuesInPage, final int totalIssueCount, final PagerFilter pagerFilter)
    {
        // Reset the pager filters start value if the current value is not sane
        if (totalIssueCount < pagerFilter.getStart())
        {
            pagerFilter.setStart(0);
        }
        start = pagerFilter.getStart();
        total = totalIssueCount;
        max = pagerFilter.getMax();
        issues = issuesInPage;
    }

    /**
     * Construct searchResults using the issues that should be displayed, and the 'total' number of issues.
     * This is used when we do a stable search and want to return a max of the selected page's length, not the
     * stable search limit.
     *
     * @param issuesInPage      A list of {@link com.atlassian.jira.issue.Issue} objects
     * @param totalIssueCount   The count of the number of issues returned
     * @param maxIssueCount    The maximum number of issues to include in the search
     * @param startIndex       The index of the first issue in the search
     */
    public SearchResults(final List<Issue> issuesInPage, final int totalIssueCount, int maxIssueCount, int startIndex)
    {
        // Reset the pager filters start value if the current value is not sane
        if (totalIssueCount < startIndex)
        {
            startIndex = 0;
        }
        start = startIndex;
        total = totalIssueCount;
        max = maxIssueCount;
        issues = issuesInPage;
    }

    /**
     * Get the issues available in this page.
     * @return A list of {@link com.atlassian.jira.issue.Issue} objects
     */
    public List<Issue> getIssues()
    {
        return issues;
    }

    public int getStart()
    {
        return start;
    }

    public int getEnd()
    {
        return Math.min(start + max, total);
    }

    public int getTotal()
    {
        return total;
    }

    public int getNextStart()
    {
        return start + max;
    }

    public int getPreviousStart()
    {
        return Math.max(0, start - max);
    }

    /**
     * Return the 'readable' start (ie 1 instead of 0)
     */
    public int getNiceStart()
    {
        if ((getIssues() == null) || (getIssues().size() == 0))
        {
            return 0;
        }

        return getStart() + 1;
    }

    public List<Page> getPages()
    {
        if (pages == null)
        {
            pages = generatePages();
        }

        return restrictPages(pages, total);
    }

    /**
     * generates a collection of page objects which keep track of the pages for display
     *
     */
    List<Page> generatePages()
    {
        if (total == 0)
        {
            return Collections.emptyList();
        }
        if (max <= 0)
        {
            throw new IllegalArgumentException("Issue per page should be 1 or greater.");
        }

        final List<Page> pages = new ArrayList<Page>();

        int pageNumber = 1;
        for (int index = 0; index < total; index += max)
        {
            final boolean isCurrentPage = (start >= index) && (start < index + max);
            pages.add(new Page(index, pageNumber, isCurrentPage));
            pageNumber++;
        }

        return pages;
    }

    /**
     * Restrict the pagers to a certain number of pages on either side of the current page.
     * <p/>
     * The number of pages to list is stored in {@link #PAGES_TO_LIST}.
     */
    List<Page> restrictPages(final Collection<Page> pages, final int size)
    {
        final List<Page> pagesToDisplay = new ArrayList<Page>(2 * PAGES_TO_LIST);

        // enhance the calculation so that at least
        // PAGES_TO_LIST-1 pages are always shown
        //
        // calculate sliding window
        final int maxpage = (size + max - 1) / max; // 1 .. n
        int firstpage = 1; // 1 .. n
        int lastpage = firstpage + PAGES_TO_LIST + PAGES_TO_LIST - 2; // 1 .. n
        if (lastpage < maxpage)
        {
            final int ourpage = (getStart() / max) + 1; // 1 .. n
            if (ourpage - firstpage > PAGES_TO_LIST - 1)
            {
                lastpage = ourpage + PAGES_TO_LIST - 1;
                if (lastpage > maxpage)
                {
                    lastpage = maxpage;
                }
                firstpage = lastpage - PAGES_TO_LIST - PAGES_TO_LIST + 2;
            }
        }
        else if (lastpage > maxpage)
        {
            lastpage = maxpage;
        }

        final int minstart = (firstpage - 1) * max;
        final int maxstart = (lastpage - 1) * max;
        for (final Page page : pages)
        {
            if (page.getStart() <= size)
            {
                final boolean largerThanMin = page.getStart() >= minstart;
                final boolean smallerThanMax = page.getStart() <= maxstart;
                if (largerThanMin && smallerThanMax)
                {
                    pagesToDisplay.add(page);
                }
            }
        }
        return pagesToDisplay;
    }

    static class Page implements IssuePage
    {
        private final int start;
        private final int pageNumber;
        private final boolean currentPage;

        public Page(final int start, final int pageNumber, final boolean isCurrentPage)
        {
            this.start = start;
            this.pageNumber = pageNumber;
            currentPage = isCurrentPage;
        }

        public boolean isCurrentPage()
        {
            return currentPage;
        }

        public int getStart()
        {
            return start;
        }

        public int getPageNumber()
        {
            return pageNumber;
        }
    }
}
