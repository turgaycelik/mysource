package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class IssueGVsIssueIterable implements IssuesIterable
{
    private final Collection<GenericValue> issueGVs;
    private final IssueFactory issueFactory;

    public IssueGVsIssueIterable(final Collection<GenericValue> issueGVs, final IssueFactory issueFactory)
    {
        this.issueGVs = Collections.unmodifiableCollection(new ArrayList<GenericValue>(issueGVs));

        if (issueFactory == null)
        {
            throw new NullPointerException(this.getClass().getName() + " needs a not null " + IssueFactory.class.getName() + " instance");
        }
        this.issueFactory = issueFactory;
    }

    public final void foreach(final Consumer<Issue> sink)
    {
        CollectionUtil.foreach(new AbstractTransformIssueIterator<GenericValue>(issueGVs)
        {
            @Override
            protected Issue transform(final GenericValue o)
            {
                return issueFactory.getIssue(o);
            }
        }, sink);
    }

    public int size()
    {
        return issueGVs.size();
    }

    public boolean isEmpty()
    {
        return issueGVs.isEmpty();
    }

    @Override
    public String toString()
    {
        final Collection<String> issueKeys = CollectionUtil.transform(issueGVs, new Function<GenericValue, String>()
        {
            public String get(final GenericValue object)
            {
                return object.getString("key");
            }
        });

        return getClass().getName() + " (" + size() + " items): " + issueKeys;
    }
}