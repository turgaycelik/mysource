package com.atlassian.jira.index.ha;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.util.AbstractTransformIssueIterator;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.collect.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class NullAwareIssueIdsIssueIterable implements IssuesIterable
{
    private final Collection<Long> ids;
    private final IssueManager issueManager;

    public NullAwareIssueIdsIssueIterable(final Collection<Long> issueIds, final IssueManager issueManager)
    {
        ids = Collections.unmodifiableCollection(new ArrayList<Long>(notNull("issueIds", issueIds)));
        this.issueManager = notNull("issueManager", issueManager);
    }

    public void foreach(final Consumer<Issue> sink)
    {
        for (Long id : ids)
        {
            MutableIssue issue = issueManager.getIssueObject(id);
            if (issue != null)
            {
                sink.consume(issue);
            }
        }
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