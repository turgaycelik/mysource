package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;

import java.util.Collection;
import java.util.Iterator;

public abstract class AbstractTransformIssueIterator<T> implements Iterator<Issue>
{
    protected final Iterator<T> iterator;

    protected AbstractTransformIssueIterator(final Collection<T> objects)
    {
        iterator = objects.iterator();
    }

    public Issue nextIssue()
    {
        return transform(iterator.next());
    }

    public Issue next()
    {
        return nextIssue();
    }

    protected abstract Issue transform(T o);

    public boolean hasNext()
    {
        return iterator.hasNext();
    }

    public void remove()
    {
        throw new UnsupportedOperationException("Cannot remove an issue from an Issue Iterator");
    }
}