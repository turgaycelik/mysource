package com.atlassian.jira.plugin.report.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.issue.MockIssue;

import org.junit.Test;
import org.springframework.test.annotation.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestTimeTrackingReportProcessor
{
    @Test
    public void testEmpty()
    {
        final Set empty = Collections.EMPTY_SET;
        final TimeTrackingReport.Processor processor = new TimeTrackingReport.Processor(null, null, null);
        assertEquals(0, processor.getDecoratedIssues(empty).size());
    }

    @Test
    public void testTransformSingleIssue()
    {
        final Issue issue = new MockIssue((long) 1);
        final Set<Issue> issues = new HashSet<Issue>(Arrays.asList(new Issue[] { issue }));
        final TimeTrackingReport.Processor processor = new TimeTrackingReport.Processor(null, null, new Comparator()
        {
            public int compare(final Object arg0, final Object arg1)
            {
                return 0;
            }
        });

        final List decoratedIssues = processor.getDecoratedIssues(issues);
        assertEquals(1, decoratedIssues.size());
        final Object decoratedIssue = decoratedIssues.get(0);
        assertNotNull(decoratedIssue);
        assertTrue(decoratedIssue instanceof ReportIssue);

        try
        {
            decoratedIssues.add(issue); // Expect exception to be thrown
            fail("should be an immutable list");
        } catch (UnsupportedOperationException yay) {}
    }

    @Test
    public void testSourceListMustContainIssues()
    {
        final TimeTrackingReport.Processor processor = new TimeTrackingReport.Processor(null, null, null);
        try
        {
            processor.getDecoratedIssues(EasyList.build("not an issue")); // Expect exception to be thrown
            fail("should have thrown IllArg");
        } catch (ClassCastException yay) {}
    }

    @Test
    public void testSourceListMustNotContainNulls()
    {
        final TimeTrackingReport.Processor processor = new TimeTrackingReport.Processor(null, null, null);

        try
        {
            processor.getDecoratedIssues(EasyList.build((Object) null)); // Expect exception to be thrown
            fail("should have thrown IllArg");
        } catch (IllegalArgumentException yay) {}
    }

    @Test
    public void testComparatorIsCalled() throws Exception
    {
        final AtomicInteger calledCount = new AtomicInteger();
        final Comparator comparator = new Comparator()
        {
            public int compare(final Object arg0, final Object arg1)
            {
                calledCount.getAndIncrement();
                return 0;
            }
        };
        final TimeTrackingReport.Processor processor = new TimeTrackingReport.Processor(null, null, comparator);
        final Issue issue = new MockIssue((long) 1);
        final Issue issue2 = new MockIssue((long) 2);
        final Set<Issue> issues = new HashSet<Issue>(Arrays.asList(new Issue[] { issue, issue2 }));
        processor.getDecoratedIssues(issues);
        assertEquals(1, calledCount.get());
    }
}
