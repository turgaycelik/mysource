package com.atlassian.jira.plugin.report.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.mock.issue.MockIssue;

import org.apache.commons.collections.PredicateUtils;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestReportIssueTimes
{
    @Rule public MockComponentContainer container = new MockComponentContainer(this);

    @Test
    public void testGetOriginalEstimate()
    {
        final MockIssue issue = createMockIssue();
        assertEquals(issue.getOriginalEstimate().toString(), createReportIssue(issue).getOriginalEstimate());
    }

    @Test
    public void testGetTimeSpentUnknown()
    {
        final MockIssue issue = new MockIssue();
        assertEquals(MockDurationFormatter.UNKNOWN, createReportIssue(issue).getTimeSpent());
    }

    @Test
    public void testGetTimeSpent()
    {
        final MockIssue issue = createMockIssue();
        assertEquals(issue.getTimeSpent().toString(), createReportIssue(issue).getTimeSpent());
    }

    @Test
    public void testGetRemainingEstimateUnknown()
    {
        final MockIssue issue = new MockIssue();
        assertEquals(MockDurationFormatter.UNKNOWN, createReportIssue(issue).getRemainingEstimate());
    }

    @Test
    public void testGetRemainingEstimate()
    {
        final MockIssue issue = createMockIssue();
        assertEquals(issue.getEstimate().toString(), createReportIssue(issue).getRemainingEstimate());
    }

    @Test
    public void testGetAccuracy()
    {
        final MockIssue issue = createMockIssue();
        assertEquals("3", createReportIssue(issue).getAccuracy());
    }

    @Test
    public void testSimpleGetAggregateOriginalEstimateUnknown()
    {
        final MockIssue issue = new MockIssue();
        assertEquals(MockDurationFormatter.UNKNOWN, createReportIssue(issue).getAggregateOriginalEstimate());
    }

    @Test
    public void testSimpleGetAggregateOriginalEstimate()
    {
        final MockIssue issue = createMockIssue();
        assertEquals(issue.getOriginalEstimate().toString(), createReportIssue(issue).getAggregateOriginalEstimate());
    }

    @Test
    public void testSimpleGetAggregateTimeSpentUnknown()
    {
        final MockIssue issue = new MockIssue();
        assertEquals(MockDurationFormatter.UNKNOWN, createReportIssue(issue).getAggregateTimeSpent());
    }

    @Test
    public void testSimpleGetAggregateTimeSpent()
    {
        final MockIssue issue = createMockIssue();
        assertEquals(issue.getTimeSpent().toString(), createReportIssue(issue).getAggregateTimeSpent());
    }

    @Test
    public void testSimpleGetAggregateRemainingEstimateUnknown()
    {
        final MockIssue issue = new MockIssue();
        assertEquals(MockDurationFormatter.UNKNOWN, createReportIssue(issue).getAggregateRemainingEstimate());
    }

    @Test
    public void testSimpleGetAggregateRemainingEstimate()
    {
        final MockIssue issue = createMockIssue();
        assertEquals(issue.getEstimate().toString(), createReportIssue(issue).getAggregateRemainingEstimate());
    }

    @Test
    public void testSimpleGetAggregateAccuracy()
    {
        final MockIssue issue = createMockIssue();
        assertEquals("3", createReportIssue(issue).getAggregateAccuracy());
    }

    @Test
    public void testIssueWithSubTasks()
    {
        ReportIssue issue = createReportIssue(createSubTaskedIssue());
        assertEquals(2, issue.getSubTasks().size());
        List subtasks = new ArrayList(issue.getSubTasks());
        ReportIssue subtask = (ReportIssue) subtasks.get(0);
        assertTrue(subtask.isSubTask());
        assertFalse(subtask.isOrphan());
    }

    @Test
    public void testGetAggregateOriginalEstimate()
    {
        final MockIssue issue = createSubTaskedIssue();
        assertEquals("30", createReportIssue(issue).getAggregateOriginalEstimate());
    }

    @Test
    public void testGetAggregateTimeSpent()
    {
        assertEquals("6", createReportIssue(createSubTaskedIssue()).getAggregateTimeSpent());
    }

    @Test
    public void testGetAggregateRemainingEstimate()
    {
        assertEquals("15", createReportIssue(createSubTaskedIssue()).getAggregateRemainingEstimate());
    }

    @Test
    public void testGetSubtasksReturnsReportIssues()
    {
        final ReportIssue issue = createReportIssue(createSubTaskedIssue());
        for (Iterator iterator = issue.getSubTasks().iterator(); iterator.hasNext();)
        {
            ReportIssue subTask = (ReportIssue) iterator.next();
            assertTrue(subTask.isSubTask());
            assertFalse(subTask.isOrphan());
        }
    }

    @Test
    public void testGetAggregateAccuracy()
    {
        assertEquals("9", createReportIssue(createSubTaskedIssue()).getAggregateAccuracy());
    }

    @Test
    public void testIsTrackedWithSomethingAndNulls()
    {
        assertIsTracked(true, new Long(1), null);
    }

    @Test
    public void testIsTrackedWithSomethingAndNone()
    {
        assertIsTracked(true, new Long(1), new Long(0));
    }

    @Test
    public void testIsNotTrackedWithNullAndNull()
    {
        assertIsTracked(false, null, null);
    }

    @Test
    public void testIsNotTrackedWithNullAndNone()
    {
        assertIsTracked(false, null, new Long(0));
    }

    @Test
    public void testIsNotTrackedWithNoneAndNull()
    {
        assertIsTracked(false, new Long(0), null);
    }

    @Test
    public void testIsNotTrackedWithNoneAndNone()
    {
        assertIsTracked(false, new Long(0), new Long(0));
    }

    private void assertIsTracked(boolean assertion, Long first, Long second) {
        assertEquals(assertion, ReportIssue.isTracked(first, second, second));
        assertEquals(assertion, ReportIssue.isTracked(second, first, second));
        assertEquals(assertion, ReportIssue.isTracked(second, second, first));
    }

    private MockIssue createSubTaskedIssue()
    {
        final MockIssue subTaskedIssue = createMockIssue();
        subTaskedIssue.setSubTaskObjects(EasyList.build(createMockIssue(), createMockIssue()));
        return subTaskedIssue;
    }

    private ReportIssue createReportIssue(MockIssue issue)
    {
        AggregateTimeTrackingCalculatorFactory factory = CalculatorFactory.get("TestReportIssueTimes");

        return new ReportIssue(issue, factory.getCalculator(issue), new MockDurationFormatter(), new MockAccuracyCalculator(), new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return 0;
            }
        }, PredicateUtils.truePredicate());
    }

    private MockIssue createMockIssue()
    {
        final class MyIssue extends MockIssue
        {
            private Issue parent;

            public Issue getParentObject()
            {
                return parent;
            }

            void setParent(Issue parent)
            {
                this.parent = parent;
            }

            public void setSubTaskObjects(Collection subTaskObjects)
            {
                super.setSubTaskObjects(subTaskObjects);
                for (Iterator iterator = subTaskObjects.iterator(); iterator.hasNext();)
                {
                    MyIssue child = (MyIssue) iterator.next();
                    child.setParent(this);
                }
            }
        }
        MyIssue issue = new MyIssue();
        issue.setOriginalEstimate(new Long(10));
        issue.setEstimate(new Long(5));
        issue.setTimeSpent(new Long(2));
        return issue;
    }

    private static class MockDurationFormatter implements DurationFormatter
    {
        public static final String UNKNOWN = "Unknown";

        public String format(Long duration)
        {
            return duration == null ? UNKNOWN : duration.toString();
        }

        public String shortFormat(Long duration)
        {
            return format(duration);
        }
    }

    private static class MockAccuracyCalculator implements AccuracyCalculator
    {
        public String calculateAndFormatAccuracy(Long originalEst, Long timeEst, Long timeSpent)
        {
            if (originalEst == null || timeEst == null || timeSpent == null)
            {
                return MockDurationFormatter.UNKNOWN;
            }
            Long accuracy = new Long(Math.abs(originalEst.longValue() - timeEst.longValue() - timeSpent.longValue()));

            return accuracy.toString();
        }

        public Long calculateAccuracy(Long originalEstimate, Long remainingEstimate, Long timeSpent)
        {
            if (originalEstimate == null || remainingEstimate == null || timeSpent == null)
            {
                return null;
            }
            return new Long(Math.abs(originalEstimate.longValue() - remainingEstimate.longValue() - timeSpent.longValue()));
        }

        public int onSchedule(Long originalEst, Long timeEst, Long timeSpent)
        {
            return 0;
        }
    }
}
