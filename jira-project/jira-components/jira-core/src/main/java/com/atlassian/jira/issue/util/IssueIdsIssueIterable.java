package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.collect.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class IssueIdsIssueIterable implements IssuesIterable
{
    private final Collection<Long> ids;
    private final IssueManager issueManager;

    public IssueIdsIssueIterable(final Collection<Long> issueIds, final IssueManager issueManager)
    {
        ids = Collections.unmodifiableCollection(new ArrayList<Long>(notNull("issueIds", issueIds)));
        this.issueManager = notNull("issueManager", issueManager);
    }

    public void foreach(final Consumer<Issue> sink)
    {
        CollectionUtil.foreach(new AbstractTransformIssueIterator<Long>(ids)
        {
            @Override
            protected Issue transform(final Long o)
            {
                return issueManager.getIssueObject(o);
            }
        }, sink);
    }

    public int size()
    {
        return ids.size();
    }

    public boolean isEmpty()
    {
        return ids.isEmpty();
    }

    @Override
    public String toString()
    {
        return getClass().getName() + " (" + size() + " items): " + ids;
    }
}