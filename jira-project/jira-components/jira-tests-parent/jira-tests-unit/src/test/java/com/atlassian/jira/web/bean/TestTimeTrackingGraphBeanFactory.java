package com.atlassian.jira.web.bean;

import java.util.Locale;

import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.NoopI18nHelper;

import org.junit.Test;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link TimeTrackingGraphBeanFactoryImpl}.
 *
 * @since v4.1
 */
public class TestTimeTrackingGraphBeanFactory
{
    private static final String SPENT_SHORT = "spentShort";
    private static final String ORIG_SHORT = "origShort";
    private static final String ESTIMATE_SHORT = "estimateShort";

    private static final String SPENT_LONG = "spentLong";
    private static final String ORIG_LONG = "origLong";
    private static final String ESTIMATE_LONG = "estimateLong";

    @Test
    public void testCotr() throws Exception
    {
        try
        {
            new TimeTrackingGraphBeanFactoryImpl(null);
            fail("Should not be able to pass in null JiraDurationUtils.");
        }
        catch (IllegalArgumentException expected)
        {
            //expected.
        }
    }

    @Test
    public void testCreateBeanFromIssueShort() throws Exception
    {
        final Long timespent = 300L;
        final Long originalEstimate = 20L;
        final Long remainingEstimate = 10L;

        MockIssue issue = new MockIssue(7);
        issue.setEstimate(remainingEstimate);
        issue.setOriginalEstimate(originalEstimate);
        issue.setTimeSpent(timespent);

        final NoopI18nHelper helper = new NoopI18nHelper();

        final JiraDurationUtils durationUtils = createMock(JiraDurationUtils.class);

        expect(durationUtils.getShortFormattedDuration(timespent, Locale.ENGLISH)).andReturn(SPENT_SHORT);
        expect(durationUtils.getShortFormattedDuration(originalEstimate, Locale.ENGLISH)).andReturn(ORIG_SHORT);
        expect(durationUtils.getShortFormattedDuration(remainingEstimate, Locale.ENGLISH)).andReturn(ESTIMATE_SHORT);

        expect(durationUtils.getFormattedDuration(timespent, Locale.ENGLISH)).andReturn(SPENT_LONG);
        expect(durationUtils.getFormattedDuration(originalEstimate, Locale.ENGLISH)).andReturn(ORIG_LONG);
        expect(durationUtils.getFormattedDuration(remainingEstimate, Locale.ENGLISH)).andReturn(ESTIMATE_LONG);

        replay(durationUtils);

        final TimeTrackingGraphBeanFactory factory = new TimeTrackingGraphBeanFactoryImpl(durationUtils);
        final TimeTrackingGraphBean bean = factory.createBean(issue, TimeTrackingGraphBeanFactoryImpl.Style.SHORT, helper);

        assertEquals((long)timespent, bean.getTimeSpent());
        assertEquals((long)originalEstimate, bean.getOriginalEstimate());
        assertEquals((long)remainingEstimate, bean.getRemainingEstimate());

        assertEquals(SPENT_SHORT, bean.getTimeSpentStr());
        assertEquals(ORIG_SHORT, bean.getOriginalEstimateStr());
        assertEquals(ESTIMATE_SHORT, bean.getRemainingEstimateStr());

        assertTrue(bean.getTimeSpentTooltip().contains(SPENT_LONG));
        assertTrue(bean.getOriginalEstimateTooltip().contains(ORIG_LONG));
        assertTrue(bean.getRemainingEstimateTooltip().contains(ESTIMATE_LONG));

        verify(durationUtils);
    }

    @Test
    public void testCreateBeanFromIssueNormal() throws Exception
    {
        final Long timespent = 300L;
        final Long originalEstimate = 20L;
        final Long remainingEstimate = 10L;

        MockIssue issue = new MockIssue(7);
        issue.setEstimate(remainingEstimate);
        issue.setOriginalEstimate(originalEstimate);
        issue.setTimeSpent(timespent);

        final NoopI18nHelper helper = new NoopI18nHelper();

        final JiraDurationUtils durationUtils = createMock(JiraDurationUtils.class);

        expect(durationUtils.getFormattedDuration(timespent, Locale.ENGLISH)).andReturn(SPENT_LONG).times(2);
        expect(durationUtils.getFormattedDuration(originalEstimate, Locale.ENGLISH)).andReturn(ORIG_LONG).times(2);
        expect(durationUtils.getFormattedDuration(remainingEstimate, Locale.ENGLISH)).andReturn(ESTIMATE_LONG).times(2);

        replay(durationUtils);

        final TimeTrackingGraphBeanFactory factory = new TimeTrackingGraphBeanFactoryImpl(durationUtils);
        final TimeTrackingGraphBean bean = factory.createBean(issue, TimeTrackingGraphBeanFactoryImpl.Style.NORMAL, helper);

        assertEquals((long)timespent, bean.getTimeSpent());
        assertEquals((long)originalEstimate, bean.getOriginalEstimate());
        assertEquals((long)remainingEstimate, bean.getRemainingEstimate());

        assertEquals(SPENT_LONG, bean.getTimeSpentStr());
        assertEquals(ORIG_LONG, bean.getOriginalEstimateStr());
        assertEquals(ESTIMATE_LONG, bean.getRemainingEstimateStr());

        assertTrue(bean.getTimeSpentTooltip().contains(SPENT_LONG));
        assertTrue(bean.getOriginalEstimateTooltip().contains(ORIG_LONG));
        assertTrue(bean.getRemainingEstimateTooltip().contains(ESTIMATE_LONG));

        verify(durationUtils);
    }

    @Test
    public void testCreateBeanFromAggregateBeanShort() throws Exception
    {
        final Long timespent = 300L;
        final Long originalEstimate = 20L;
        final Long remainingEstimate = 10L;

        final AggregateTimeTrackingBean aggregate = new AggregateTimeTrackingBean(originalEstimate, remainingEstimate, timespent, 1);
        final NoopI18nHelper helper = new NoopI18nHelper();

        final JiraDurationUtils durationUtils = createMock(JiraDurationUtils.class);

        expect(durationUtils.getShortFormattedDuration(timespent, Locale.ENGLISH)).andReturn(SPENT_SHORT);
        expect(durationUtils.getShortFormattedDuration(originalEstimate, Locale.ENGLISH)).andReturn(ORIG_SHORT);
        expect(durationUtils.getShortFormattedDuration(remainingEstimate, Locale.ENGLISH)).andReturn(ESTIMATE_SHORT);

        expect(durationUtils.getFormattedDuration(timespent, Locale.ENGLISH)).andReturn(SPENT_LONG);
        expect(durationUtils.getFormattedDuration(originalEstimate, Locale.ENGLISH)).andReturn(ORIG_LONG);
        expect(durationUtils.getFormattedDuration(remainingEstimate, Locale.ENGLISH)).andReturn(ESTIMATE_LONG);

        replay(durationUtils);

        final TimeTrackingGraphBeanFactory factory = new TimeTrackingGraphBeanFactoryImpl(durationUtils);
        final TimeTrackingGraphBean bean = factory.createBean(aggregate, TimeTrackingGraphBeanFactoryImpl.Style.SHORT, helper);

        assertEquals((long)timespent, bean.getTimeSpent());
        assertEquals((long)originalEstimate, bean.getOriginalEstimate());
        assertEquals((long)remainingEstimate, bean.getRemainingEstimate());

        assertEquals(SPENT_SHORT, bean.getTimeSpentStr());
        assertEquals(ORIG_SHORT, bean.getOriginalEstimateStr());
        assertEquals(ESTIMATE_SHORT, bean.getRemainingEstimateStr());

        assertTrue(bean.getTimeSpentTooltip().contains(SPENT_LONG));
        assertTrue(bean.getOriginalEstimateTooltip().contains(ORIG_LONG));
        assertTrue(bean.getRemainingEstimateTooltip().contains(ESTIMATE_LONG));

        verify(durationUtils);
    }

    @Test
    public void testCreateBeanFromAggregateBeanNormal() throws Exception
    {
        final Long timespent = 300L;
        final Long originalEstimate = 20L;
        final Long remainingEstimate = 10L;

        final AggregateTimeTrackingBean aggregate = new AggregateTimeTrackingBean(originalEstimate, remainingEstimate, timespent, 1);
        final NoopI18nHelper helper = new NoopI18nHelper();

        final JiraDurationUtils durationUtils = createMock(JiraDurationUtils.class);

        expect(durationUtils.getFormattedDuration(timespent, Locale.ENGLISH)).andReturn(SPENT_LONG).times(2);
        expect(durationUtils.getFormattedDuration(originalEstimate, Locale.ENGLISH)).andReturn(ORIG_LONG).times(2);
        expect(durationUtils.getFormattedDuration(remainingEstimate, Locale.ENGLISH)).andReturn(ESTIMATE_LONG).times(2);

        replay(durationUtils);

        final TimeTrackingGraphBeanFactory factory = new TimeTrackingGraphBeanFactoryImpl(durationUtils);
        final TimeTrackingGraphBean bean = factory.createBean(aggregate, TimeTrackingGraphBeanFactoryImpl.Style.NORMAL, helper);

        assertEquals((long)timespent, bean.getTimeSpent());
        assertEquals((long)originalEstimate, bean.getOriginalEstimate());
        assertEquals((long)remainingEstimate, bean.getRemainingEstimate());

        assertEquals(SPENT_LONG, bean.getTimeSpentStr());
        assertEquals(ORIG_LONG, bean.getOriginalEstimateStr());
        assertEquals(ESTIMATE_LONG, bean.getRemainingEstimateStr());

        assertTrue(bean.getTimeSpentTooltip().contains(SPENT_LONG));
        assertTrue(bean.getOriginalEstimateTooltip().contains(ORIG_LONG));
        assertTrue(bean.getRemainingEstimateTooltip().contains(ESTIMATE_LONG));

        verify(durationUtils);
    }
}
