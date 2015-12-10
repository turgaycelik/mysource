package com.atlassian.jira.issue.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.issue.MockIssue;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;


public class TestIssueAggregatorTimeTrackingCalculator
{
    private static final Long ZERO_ESTIMATE = new Long(0);
    private static final Long ONE_ESTIMATE = new Long(1);
    private static final Long BIG_ESTIMATE = new Long(99999);

    private int count;

    @Test
    public void testNullIssue()
    {
        // does this even make sense
        AggregateTimeTrackingCalculator calculator = new IssueImplAggregateTimeTrackingCalculator(null, null);
        try
        {
            calculator.getAggregates(null);
            fail("Should have thrown a IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {
        }
    }

    @Test
    public void testIssueIsSubTaskWithNullValues()
    {
        AggregateTimeTrackingCalculator calculator = new IssueImplAggregateTimeTrackingCalculator(null, null);
        AggregateTimeTrackingBean bean = calculator.getAggregates(getIssue(null, null, null, true));
        assertNotNull(bean);
        assertNull(bean.getRemainingEstimate());
        assertNull(bean.getOriginalEstimate());
        assertNull(bean.getTimeSpent());
    }

    @Test
    public void testIssueIsSubTaskWithNonNullValues()
    {
        AggregateTimeTrackingCalculator calculator = new IssueImplAggregateTimeTrackingCalculator(null, null);
        AggregateTimeTrackingBean bean = calculator.getAggregates(getIssue(ZERO_ESTIMATE, ONE_ESTIMATE, BIG_ESTIMATE, true));
        assertNotNull(bean);
        assertEquals(ZERO_ESTIMATE, bean.getRemainingEstimate());
        assertEquals(ONE_ESTIMATE, bean.getOriginalEstimate());
        assertEquals(BIG_ESTIMATE, bean.getTimeSpent());
    }

    @Test
    public void testNullSubTasksCollectionNonNullParentValues()
    {
        AggregateTimeTrackingCalculator calculator = new IssueImplAggregateTimeTrackingCalculator(null, null);
        AggregateTimeTrackingBean bean = calculator.getAggregates(getIssue(ZERO_ESTIMATE, ONE_ESTIMATE, BIG_ESTIMATE, null));
        assertNotNull(bean);
        assertEquals(ZERO_ESTIMATE, bean.getRemainingEstimate());
        assertEquals(ONE_ESTIMATE, bean.getOriginalEstimate());
        assertEquals(BIG_ESTIMATE, bean.getTimeSpent());
    }

    @Test
    public void testNoSubTasksNonNullParentValues()
    {
        AggregateTimeTrackingCalculator calculator = new IssueImplAggregateTimeTrackingCalculator(null, null);
        AggregateTimeTrackingBean bean = calculator.getAggregates(getIssue(ZERO_ESTIMATE, ONE_ESTIMATE, BIG_ESTIMATE, Collections.EMPTY_LIST));
        assertNotNull(bean);
        assertEquals(ZERO_ESTIMATE, bean.getRemainingEstimate());
        assertEquals(ONE_ESTIMATE, bean.getOriginalEstimate());
        assertEquals(BIG_ESTIMATE, bean.getTimeSpent());
    }

    @Test
    public void testNoSubTasksNullParentValues()
    {
        AggregateTimeTrackingCalculator calculator = new IssueImplAggregateTimeTrackingCalculator(null, null);
        AggregateTimeTrackingBean bean = calculator.getAggregates(getIssue(null, null, null, Collections.EMPTY_LIST));
        assertNotNull(bean);
        assertEquals(null, bean.getRemainingEstimate());
        assertEquals(null, bean.getOriginalEstimate());
        assertEquals(null, bean.getTimeSpent());
    }

    @Test
    public void testNullSubTasksNonNullParentValues()
    {
        AggregateTimeTrackingCalculator calculator = new IssueImplAggregateTimeTrackingCalculator(new IssueImplAggregateTimeTrackingCalculator.PermissionChecker()
        {
            public boolean hasPermission(Issue subTask)
            {
                return true;
            }
        });
        AggregateTimeTrackingBean bean = calculator.getAggregates(getIssue(ZERO_ESTIMATE, ONE_ESTIMATE, BIG_ESTIMATE, EasyList.build(
                getIssue(null, null, null, true), getIssue(null, null, null, true), getIssue(null, null, null, true)
        )));
        assertNotNull(bean);
        assertEquals(ZERO_ESTIMATE, bean.getRemainingEstimate());
        assertEquals(ONE_ESTIMATE, bean.getOriginalEstimate());
        assertEquals(BIG_ESTIMATE, bean.getTimeSpent());
    }

    @Test
    public void testNonNullSubTasksNonNullParentValues()
    {
        AggregateTimeTrackingCalculator calculator = new IssueImplAggregateTimeTrackingCalculator(new IssueImplAggregateTimeTrackingCalculator.PermissionChecker()
        {
            public boolean hasPermission(Issue subTask)
            {
                return true;
            }
        });
        AggregateTimeTrackingBean bean = calculator.getAggregates(getIssue(ZERO_ESTIMATE, ONE_ESTIMATE, BIG_ESTIMATE, EasyList.build(
                getIssue(ONE_ESTIMATE, ONE_ESTIMATE, ONE_ESTIMATE, true), getIssue(ONE_ESTIMATE, ONE_ESTIMATE, ONE_ESTIMATE, true), getIssue(ONE_ESTIMATE, ONE_ESTIMATE, ONE_ESTIMATE, true)
        )));
        assertNotNull(bean);
        assertEquals(new Long(ZERO_ESTIMATE.longValue() + 3), bean.getRemainingEstimate());
        assertEquals(new Long(ONE_ESTIMATE.longValue() + 3), bean.getOriginalEstimate());
        assertEquals(new Long(BIG_ESTIMATE.longValue() + 3), bean.getTimeSpent());
    }


    @Test
    public void testMixedSubTasksValuesNonNullParentValues()
    {
        AggregateTimeTrackingCalculator calculator = new IssueImplAggregateTimeTrackingCalculator(new IssueImplAggregateTimeTrackingCalculator.PermissionChecker()
        {
            public boolean hasPermission(Issue subTask)
            {
                return true;
            }
        });
        AggregateTimeTrackingBean bean = calculator.getAggregates(getIssue(ZERO_ESTIMATE, ONE_ESTIMATE, BIG_ESTIMATE, EasyList.build(
                getIssue(ONE_ESTIMATE, null, ONE_ESTIMATE, true), getIssue(null, ONE_ESTIMATE, ONE_ESTIMATE, true), getIssue(ONE_ESTIMATE, ONE_ESTIMATE, null, true)
        )));
        assertNotNull(bean);
        assertEquals(new Long(ZERO_ESTIMATE.longValue() + 2), bean.getRemainingEstimate());
        assertEquals(new Long(ONE_ESTIMATE.longValue() + 2), bean.getOriginalEstimate());
        assertEquals(new Long(BIG_ESTIMATE.longValue() + 2), bean.getTimeSpent());
    }


    @Test
    public void testWithoutPermission()
    {
        AggregateTimeTrackingCalculator calculator = new IssueImplAggregateTimeTrackingCalculator(new IssueImplAggregateTimeTrackingCalculator.PermissionChecker()
        {
            public boolean hasPermission(Issue subTask)
            {
                return false;
            }
        });
        AggregateTimeTrackingBean bean = calculator.getAggregates(getIssue(ZERO_ESTIMATE, ONE_ESTIMATE, BIG_ESTIMATE, EasyList.build(
                getIssue(ONE_ESTIMATE, null, ONE_ESTIMATE, true), getIssue(null, ONE_ESTIMATE, ONE_ESTIMATE, true), getIssue(ONE_ESTIMATE, ONE_ESTIMATE, null, true)
        )));
        assertNotNull(bean);
        assertEquals(ZERO_ESTIMATE, bean.getRemainingEstimate());
        assertEquals(ONE_ESTIMATE, bean.getOriginalEstimate());
        assertEquals(BIG_ESTIMATE, bean.getTimeSpent());
    }

    @Test
    public void testWithoutPermissionAt50Percent()
    {
        count = 0;
        AggregateTimeTrackingCalculator calculator = new IssueImplAggregateTimeTrackingCalculator(new IssueImplAggregateTimeTrackingCalculator.PermissionChecker()
        {
            public boolean hasPermission(Issue subTask)
            {
                count++;
                if (count % 2 == 0) {
                    return false;
                }
                return true;
            }
        });
        List subTasks = new ArrayList();
        for (int i = 0; i < 10; i++) {
            subTasks.add(getIssue(ZERO_ESTIMATE, ONE_ESTIMATE, BIG_ESTIMATE, true));
        }
        AggregateTimeTrackingBean bean = calculator.getAggregates(getIssue(ZERO_ESTIMATE, ONE_ESTIMATE, BIG_ESTIMATE, subTasks));
        assertNotNull(bean);
        assertEquals(new Long(ZERO_ESTIMATE.longValue() * 6), bean.getRemainingEstimate());
        assertEquals(new Long(ONE_ESTIMATE.longValue() * 6), bean.getOriginalEstimate());
        assertEquals(new Long(BIG_ESTIMATE.longValue() * 6), bean.getTimeSpent());
        assertEquals(5,bean.getSubTaskCount());
    }

    private Issue getIssue(Long remainingEstimate, Long orginalEstimate, Long timeSpent)
    {
        return getIssue(remainingEstimate, orginalEstimate, timeSpent, false);
    }

    private Issue getIssue(Long remainingEstimate, Long orginalEstimate, Long timeSpent, final boolean isSubTask)
    {
        MockIssue mockIssue = new MockIssue()
        {
            public boolean isSubTask()
            {
                return isSubTask;
            }
        };
        mockIssue.setEstimate(remainingEstimate);
        mockIssue.setOriginalEstimate(orginalEstimate);
        mockIssue.setTimeSpent(timeSpent);

        return mockIssue;
    }

    private Issue getIssue(Long remainingEstimate, Long orginalEstimate, Long timeSpent, Collection subTaskIssues)
    {
        MockIssue mockIssue = (MockIssue) getIssue(remainingEstimate, orginalEstimate, timeSpent);
        mockIssue.setSubTaskObjects(subTaskIssues);
        return mockIssue;
    }
}
