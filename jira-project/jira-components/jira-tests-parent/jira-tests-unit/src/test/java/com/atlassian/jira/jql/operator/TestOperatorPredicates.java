package com.atlassian.jira.jql.operator;

import java.util.Comparator;

import com.atlassian.jira.util.Predicate;
import com.atlassian.query.operator.Operator;

import org.easymock.MockControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestOperatorPredicates
{

    MockControl mockComparatorControl = null;
    Comparator comparator = null;

    @Before
    public void setUp()
    {
        mockComparatorControl = MockControl.createControl(Comparator.class);
        comparator = (Comparator) mockComparatorControl.getMock();
    }

    @Test
    public void testLikeWithComparator()
    {
        try
        {
            Operator.LIKE.getPredicateForValue(comparator, 5);
            fail("Like operator did not throw IllegalStateException exception on getPredicate with comparator");
        }
        catch (IllegalStateException e)
        {
            // Expected
        }

    }
    
    @Test
    public void testNotLikeWithComparator()
    {
        try
        {
            Operator.NOT_LIKE.getPredicateForValue(comparator, 5);
            fail("Not Like operator did not throw IllegalStateException exception on getPredicate with comparator");
        }
        catch (IllegalStateException e)
        {
            // Expected
        }

    }

    @Test
    public void testEqualsWithComparator()
    {
        try
        {
            Operator.EQUALS.getPredicateForValue(comparator, 5);
            fail("Equals operator did not throw IllegalStateException exception on getPredicate with comparator");
        }
        catch (IllegalStateException e)
        {
            // Expected
        }

    }

    @Test
    public void testNotEqualsWithComparator()
    {
        try
        {
            Operator.NOT_EQUALS.getPredicateForValue(comparator, 5);
            fail("Should not be able to get != with a comparator.");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    @Test
    public void testIn()
    {
        try
        {
            Operator.IN.getPredicateForValue(comparator, 5);
            fail("Should not be able to get in with a comparator.");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    @Test
    public void testNotIn()
    {
        try
        {
            Operator.NOT_IN.getPredicateForValue(comparator, 5);
            fail("Should not be able to get in with a comparator.");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    @Test
    public void testIs()
    {
        try
        {
            Operator.IS.getPredicateForValue(comparator, 5);
            fail("Should not be able to get in with a comparator.");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    @Test
    public void testIsNot()
    {
        try
        {
            Operator.IS_NOT.getPredicateForValue(comparator, 5);
            fail("Should not be able to get in with a comparator.");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    protected <T> void checkCompare(Predicate<T> pred, T fieldValue, T operandValue, int returnValue, boolean predResult)
    {
        comparator.compare(operandValue, fieldValue);
        mockComparatorControl.setReturnValue(returnValue);
        mockComparatorControl.replay();

        assertEquals(predResult, pred.evaluate(operandValue));

        mockComparatorControl.verify();
        mockComparatorControl.reset();
    }

    @Test
    public void testLessThan()
    {        
        Predicate<Integer> pred = Operator.LESS_THAN.getPredicateForValue(comparator, 5);

        checkCompare(pred, 5, -1, 1, false);
        checkCompare(pred, 5, 5, 0, false);
        checkCompare(pred, 5, 4, 1, false);
        checkCompare(pred, 5, 6, -1, true);
    }

    @Test
    public void testGreaterThan()
    {
        Predicate<Integer> pred = Operator.GREATER_THAN.getPredicateForValue(comparator, 5);

        checkCompare(pred, 5, -1, 1, true);
        checkCompare(pred, 5, 5, 0, false);
        checkCompare(pred, 5, 4, 1, true);
        checkCompare(pred, 5, 6, -1, false);
    }

    @Test
    public void testLessThanOrEqual()
    {
        Predicate<Integer> pred = Operator.LESS_THAN_EQUALS.getPredicateForValue(comparator, 5);

        checkCompare(pred, 5, -1, 1, false);
        checkCompare(pred, 5, 5, 0, true);
        checkCompare(pred, 5, 4, 1, false);
        checkCompare(pred, 5, 6, -1, true);
    }

    @Test
    public void testGreaterThanOrEqual()
    {
        Predicate<Integer> pred = Operator.GREATER_THAN_EQUALS.getPredicateForValue(comparator, 5);

        checkCompare(pred, 5, -1, 1, true);
        checkCompare(pred, 5, 5, 0, true);
        checkCompare(pred, 5, 4, 1, true);
        checkCompare(pred, 5, 6, -1, false);
    }

}
