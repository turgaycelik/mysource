package com.atlassian.jira.issue.search.managers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroup;

import java.util.Collection;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultIssueSearcherManager implements IssueSearcherManager
{
    private final SearchHandlerManager manager;

    public DefaultIssueSearcherManager(final SearchHandlerManager manager)
    {
        this.manager = notNull("manager", manager);
    }

    @Override
    public Collection<IssueSearcher<?>> getSearchers(final User searcher, final SearchContext context)
    {
        return manager.getSearchers(searcher, context);
    }

    @Override
    public Collection<IssueSearcher<?>> getAllSearchers()
    {
        return manager.getAllSearchers();
    }

    @Override
    public Collection<SearcherGroup> getSearcherGroups(final SearchContext searchContext)
    {
        return getSearcherGroups();
    }

    @Override
    public Collection<SearcherGroup> getSearcherGroups()
    {
        return manager.getSearcherGroups();
    }

    @Override
    public IssueSearcher<?> getSearcher(final String id)
    {
        return manager.getSearcher(id);
    }

    @Override
    public void refresh()
    {
        manager.refresh();
    }
}
