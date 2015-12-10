package com.atlassian.jira.issue.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 */
public class TestAggregateTimeTrackingBean
{
    private static final Long ZERO_ESTIMATE = new Long(0);
    private static final Long ONE_ESTIMATE = new Long(1);
    private static final Long BIG_ESTIMATE = new Long(99999);

    @Test
    public void testAdditionWithNullPreservation()
    {
        Long returnedValue;

        returnedValue = AggregateTimeTrackingBean.addAndPreserveNull(null, null);
        assertNull(returnedValue);
        returnedValue = AggregateTimeTrackingBean.addAndPreserveNull(ONE_ESTIMATE, null);
        assertEquals(ONE_ESTIMATE, returnedValue);
        returnedValue = AggregateTimeTrackingBean.addAndPreserveNull(null, ONE_ESTIMATE);
        assertEquals(ONE_ESTIMATE, returnedValue);
        returnedValue = AggregateTimeTrackingBean.addAndPreserveNull(ONE_ESTIMATE, ONE_ESTIMATE);
        assertEquals(new Long(ONE_ESTIMATE.longValue() + ONE_ESTIMATE.longValue()), returnedValue);
        returnedValue = AggregateTimeTrackingBean.addAndPreserveNull(ONE_ESTIMATE, BIG_ESTIMATE);
        assertEquals(new Long(BIG_ESTIMATE.longValue() + 1), returnedValue);
        returnedValue = AggregateTimeTrackingBean.addAndPreserveNull(BIG_ESTIMATE, BIG_ESTIMATE);
        assertEquals(new Long(BIG_ESTIMATE.longValue() + BIG_ESTIMATE.longValue()), returnedValue);
    }

    @Test
    public void testFindGreaterEstimate() {
        Long returnedValue;
        returnedValue = AggregateTimeTrackingBean.getTheGreaterOfEstimates(ONE_ESTIMATE,ONE_ESTIMATE,BIG_ESTIMATE);
        assertNotNull(returnedValue);
        assertEquals(new Long(ONE_ESTIMATE.longValue() + BIG_ESTIMATE.longValue()), returnedValue);

        returnedValue = AggregateTimeTrackingBean.getTheGreaterOfEstimates(ONE_ESTIMATE,null,null);
        assertNotNull(returnedValue);
        assertEquals(ONE_ESTIMATE, returnedValue);

        returnedValue = AggregateTimeTrackingBean.getTheGreaterOfEstimates(null,ONE_ESTIMATE,BIG_ESTIMATE);
        assertNotNull(returnedValue);
        assertEquals(new Long(ONE_ESTIMATE.longValue() + BIG_ESTIMATE.longValue()), returnedValue);

        returnedValue = AggregateTimeTrackingBean.getTheGreaterOfEstimates(BIG_ESTIMATE,ZERO_ESTIMATE,BIG_ESTIMATE);
        assertNotNull(returnedValue);
        assertEquals(BIG_ESTIMATE, returnedValue);

        returnedValue = AggregateTimeTrackingBean.getTheGreaterOfEstimates(null,null,null);
        assertNull(returnedValue);
    }

    @Test
    public void testBumpGreaterEstimate() {
        AggregateTimeTrackingBean bean;

        bean = new AggregateTimeTrackingBean(null, null, null, 0);
        assertNull(bean.getGreastestSubTaskEstimate());

        bean = new AggregateTimeTrackingBean(null, null, null, 0);
        bean.bumpGreatestSubTaskEstimate(null, null, null);
        assertNull(bean.getGreastestSubTaskEstimate());

        bean.bumpGreatestSubTaskEstimate(null, null, ZERO_ESTIMATE);
        assertNotNull(bean.getGreastestSubTaskEstimate());
        assertEquals(ZERO_ESTIMATE, bean.getGreastestSubTaskEstimate());

        bean.bumpGreatestSubTaskEstimate(null, ONE_ESTIMATE, ZERO_ESTIMATE);
        assertNotNull(bean.getGreastestSubTaskEstimate());
        assertEquals(ONE_ESTIMATE, bean.getGreastestSubTaskEstimate());

        bean.bumpGreatestSubTaskEstimate(ONE_ESTIMATE, BIG_ESTIMATE, ONE_ESTIMATE);
        assertNotNull(bean.getGreastestSubTaskEstimate());
        assertEquals(new Long(BIG_ESTIMATE.longValue() + ONE_ESTIMATE.longValue()), bean.getGreastestSubTaskEstimate());

        bean.bumpGreatestSubTaskEstimate(BIG_ESTIMATE, ONE_ESTIMATE, ONE_ESTIMATE);
        assertNotNull(bean.getGreastestSubTaskEstimate());
        assertEquals(new Long(BIG_ESTIMATE.longValue() + ONE_ESTIMATE.longValue()), bean.getGreastestSubTaskEstimate());

        bean.bumpGreatestSubTaskEstimate(new Long(BIG_ESTIMATE.longValue() + BIG_ESTIMATE.longValue()), ONE_ESTIMATE, ONE_ESTIMATE);
        assertNotNull(bean.getGreastestSubTaskEstimate());
        assertEquals(new Long(BIG_ESTIMATE.longValue() + BIG_ESTIMATE.longValue()), bean.getGreastestSubTaskEstimate());

    }

}
