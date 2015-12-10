package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class IssueObjectIssuesIterable implements IssuesIterable
{
    private final Collection<Issue> issueObjects;

    public IssueObjectIssuesIterable(final Collection<? extends Issue> issueObjects)
    {
        this.issueObjects = Collections.unmodifiableCollection(new ArrayList<Issue>(issueObjects));
    }

    protected Issue transform(final Object o)
    {
        return (Issue) o;
    }

    public final void foreach(final Consumer<Issue> sink)
    {
        CollectionUtil.foreach(issueObjects, sink);
    }

    public int size()
    {
        return issueObjects.size();
    }

    public boolean isEmpty()
    {
        return issueObjects.isEmpty();
    }

    @Override
    public String toString()
    {
        final Collection<String> issueKeys = CollectionUtil.transform(issueObjects, new Function<Issue, String>()
        {
            public String get(final Issue object)
            {
                return object.getKey();
            }
        });

        return getClass().getName() + " (" + size() + " items): " + issueKeys;
    }
}