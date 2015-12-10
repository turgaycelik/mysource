package com.atlassian.jira.util;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestEvaluateAllPredicate
{
    @Test
    public void testSinglePredicateTrue() throws Exception
    {
        final AtomicBoolean pred1Called = new AtomicBoolean(false);
        Predicate<String> pred1 = new Predicate<String>()
        {
            public boolean evaluate(final String input)
            {
                pred1Called.set(true);
                return true;
            }
        };

        Predicate<String> evalAllPred = new EvaluateAllPredicate<String>(pred1);
        assertTrue(evalAllPred.evaluate("blah"));
        assertTrue(pred1Called.get());
    }

    @Test
    public void testTwoPredicatesTrue() throws Exception
    {
        final AtomicBoolean pred1Called = new AtomicBoolean(false);
        Predicate<String> pred1 = new Predicate<String>()
        {
            public boolean evaluate(final String input)
            {
                pred1Called.set(true);
                return true;
            }
        };

        final AtomicBoolean pred2Called = new AtomicBoolean(false);
        Predicate<String> pred2 = new Predicate<String>()
        {
            public boolean evaluate(final String input)
            {
                pred2Called.set(true);
                return true;
            }
        };

        Predicate<String> evalAllPred = new EvaluateAllPredicate<String>(pred1, pred2);
        assertTrue(evalAllPred.evaluate("blah"));
        assertTrue(pred1Called.get());
        assertTrue(pred2Called.get());
    }

    @Test
    public void testTwoPredicatesFirstFail() throws Exception
    {
        final AtomicBoolean pred1Called = new AtomicBoolean(false);
        Predicate<String> pred1 = new Predicate<String>()
        {
            public boolean evaluate(final String input)
            {
                pred1Called.set(true);
                return false;
            }
        };

        Predicate<String> pred2 = new Predicate<String>()
        {
            public boolean evaluate(final String input)
            {
                fail("Should not have called evaluate after a fail");
                return true;
            }
        };

        Predicate<String> evalAllPred = new EvaluateAllPredicate<String>(pred1, pred2);
        assertFalse(evalAllPred.evaluate("blah"));
        assertTrue(pred1Called.get());
    }

    @Test
    public void testTwoPredicatesSecondFail() throws Exception
    {
        final AtomicBoolean pred1Called = new AtomicBoolean(false);
        Predicate<String> pred1 = new Predicate<String>()
        {
            public boolean evaluate(final String input)
            {
                pred1Called.set(true);
                return true;
            }
        };

        final AtomicBoolean pred2Called = new AtomicBoolean(false);
        Predicate<String> pred2 = new Predicate<String>()
        {
            public boolean evaluate(final String input)
            {
                pred2Called.set(true);
                return false;
            }
        };

        Predicate<String> evalAllPred = new EvaluateAllPredicate<String>(pred1, pred2);
        assertFalse(evalAllPred.evaluate("blah"));
        assertTrue(pred1Called.get());
        assertTrue(pred2Called.get());
    }

}
