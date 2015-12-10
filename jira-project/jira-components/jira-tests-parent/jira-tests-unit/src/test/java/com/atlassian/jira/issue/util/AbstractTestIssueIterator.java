package com.atlassian.jira.issue.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.collect.EnclosedIterable;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * An abstract test harness for testing issue iterators.  Classes must implement {@link #getIterable(java.util.List)}
 */
public abstract class AbstractTestIssueIterator<C extends EnclosedIterable<Issue>>
{
    MockGenericValue issue1 = new MockGenericValue("Issue");
    MockGenericValue issue2 = new MockGenericValue("Issue");

    @Before
    public void setUp() throws Exception
    {
        issue1.set("id", new Long(100));
        issue2.set("id", new Long(200));
    }

    protected abstract C getIterable(List<? extends GenericValue> issuesGVs);

    @Test
    public void testNoIssues()
    {
        assertIteratorEquals(new ArrayList<GenericValue>(), getIterable(new ArrayList<GenericValue>()));
    }

    @Test
    public void testSingleIssueNext()
    {
        final List<GenericValue> issues = Lists.<GenericValue>newArrayList(issue1);
        assertIteratorEquals(issues, getIterable(issues));
    }

    @Test
    public void testTwoIssuesNext()
    {
        final List<GenericValue> issues = Lists.<GenericValue>newArrayList(issue1, issue2);
        assertIteratorEquals(issues, getIterable(issues));
    }

    private void assertIteratorEquals(final List<GenericValue> expected, final C it)
    {
        assertEquals(expected.size(), it.size());
        final AtomicInteger count = new AtomicInteger(0);
        final Iterator<GenericValue> ids = expected.iterator();
        it.foreach(new Consumer<Issue>()
        {
            public void consume(final Issue element)
            {
                assertEquals(ids.next().get("id"), element.getId());
                count.incrementAndGet();
            }
        });
        assertEquals(expected.size(), count.get());
        assertFalse(ids.hasNext());
    }
}
