package com.atlassian.jira.issue.fields;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class TestAbstractProgressBarSystemField
{
    final Long negative = new Long(-2);
    final Long zero = new Long(0);
    final Long one = new Long(1);
    final Long two = new Long(2);
    final Long three = new Long(3);

    @Test
    public void testCalculateProgressPercentageBothNegative()
    {
        // negative params aren't allowed
        try
        {
            AbstractProgressBarSystemField.calculateProgressPercentage(negative, negative);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testCalculateProgressPercentageRemainingNegative()
    {
        try
        {
            AbstractProgressBarSystemField.calculateProgressPercentage(one, negative);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }
    @Test
    public void testCalculateProgressPercentageSpentNegative()
    {
        try
        {
            AbstractProgressBarSystemField.calculateProgressPercentage(negative, one);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }
    @Test
    public void testCalculateProgressPercentageBothNull()
    {
        // nulls produce null
        assertNull(AbstractProgressBarSystemField.calculateProgressPercentage(null, null));
    }

    @Test
    public void testCalculateProgressPercentageSpentNull()
    {
        // normal cases
        assertEquals(0, AbstractProgressBarSystemField.calculateProgressPercentage(null, one).longValue());
        assertEquals(0, AbstractProgressBarSystemField.calculateProgressPercentage(null, two).longValue());
        assertEquals(0, AbstractProgressBarSystemField.calculateProgressPercentage(null, three).longValue());
    }

    @Test
    public void testCalculateProgressPercentageRemainingNull()
    {
        assertEquals(100, AbstractProgressBarSystemField.calculateProgressPercentage(one, null).longValue());
        assertEquals(100, AbstractProgressBarSystemField.calculateProgressPercentage(two, null).longValue());
        assertEquals(100, AbstractProgressBarSystemField.calculateProgressPercentage(three, null).longValue());
    }

    @Test
    public void testCalculateProgressPercentage()
    {
        assertEquals(50, AbstractProgressBarSystemField.calculateProgressPercentage(one, one).longValue());
        assertEquals(33, AbstractProgressBarSystemField.calculateProgressPercentage(one, two).longValue());
        assertEquals(25, AbstractProgressBarSystemField.calculateProgressPercentage(one, three).longValue());

        assertEquals(66, AbstractProgressBarSystemField.calculateProgressPercentage(two, one).longValue());
        assertEquals(50, AbstractProgressBarSystemField.calculateProgressPercentage(two, two).longValue());
        assertEquals(40, AbstractProgressBarSystemField.calculateProgressPercentage(two, three).longValue());

        assertEquals(75, AbstractProgressBarSystemField.calculateProgressPercentage(three, one).longValue());
        assertEquals(60, AbstractProgressBarSystemField.calculateProgressPercentage(three, two).longValue());
        assertEquals(50, AbstractProgressBarSystemField.calculateProgressPercentage(three, three).longValue());

        assertEquals(100, AbstractProgressBarSystemField.calculateProgressPercentage(one, zero).longValue());
        assertEquals(0, AbstractProgressBarSystemField.calculateProgressPercentage(zero, one).longValue());
    }
    
    @Test
    public void testCalculateProgressPercentageZeroes()
    {
        // 0 out of 0 produces null
        assertNull(AbstractProgressBarSystemField.calculateProgressPercentage(zero, zero));
    }
}
