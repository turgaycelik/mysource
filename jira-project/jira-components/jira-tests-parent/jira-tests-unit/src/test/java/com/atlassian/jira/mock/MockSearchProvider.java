/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock;

import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;

import org.apache.lucene.search.Collector;

public class MockSearchProvider implements SearchProvider
{
    List results = Collections.EMPTY_LIST;

    public MockSearchProvider()
    {
    }

    public SearchResults search(Query query, User searcher, PagerFilter pager) throws SearchException
    {
        return new SearchResults(results, new PagerFilter());
    }

    @Override
    public SearchResults search(Query query, ApplicationUser searcher, PagerFilter pager) throws SearchException
    {
        return new SearchResults(results, new PagerFilter());
    }

    public long searchCount(Query query, User searcher) throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long searchCount(Query query, ApplicationUser searcher) throws SearchException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public long searchCountOverrideSecurity(final Query query, final User searcher) throws SearchException
    {
        return 0;
    }

    @Override
    public long searchCountOverrideSecurity(Query query, ApplicationUser searcher) throws SearchException
    {
        return 0;
    }

    public void search(Query query, User user, Collector collector) throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void search(Query query, ApplicationUser searcher, Collector collector) throws SearchException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void search(final Query query, final User searcher, final Collector collector, final org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void search(Query query, ApplicationUser searcher, Collector collector, org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void searchOverrideSecurity(final Query query, final User user, final Collector collector)
            throws SearchException
    {
    }

    @Override
    public void searchOverrideSecurity(Query query, ApplicationUser searcher, Collector collector)
            throws SearchException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void searchAndSort(Query query, User user, Collector collector, PagerFilter pagerFilter) throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void searchAndSort(Query query, ApplicationUser searcher, Collector collector, PagerFilter pager)
            throws SearchException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void searchAndSortOverrideSecurity(final Query query, final User user, final Collector collector, final PagerFilter pagerFilter)
            throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void searchAndSortOverrideSecurity(Query query, ApplicationUser searcher, Collector collector, PagerFilter pager)
            throws SearchException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public long searchCountIgnorePermissions(SearchRequest request, User searchUser) throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    public void setResults(List results)
    {
        this.results = results;
    }

    public SearchResults search(Query query, User searcher, PagerFilter pager, org.apache.lucene.search.Query andQuery) throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public SearchResults search(Query query, ApplicationUser searcher, PagerFilter pager, org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public SearchResults searchOverrideSecurity(final Query query, final User searcher, final PagerFilter pager, final org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        return null;
    }

    @Override
    public SearchResults searchOverrideSecurity(Query query, ApplicationUser searcher, PagerFilter pager, org.apache.lucene.search.Query andQuery)
            throws SearchException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

}
