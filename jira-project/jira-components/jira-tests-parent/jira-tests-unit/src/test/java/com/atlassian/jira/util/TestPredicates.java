package com.atlassian.jira.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link com.atlassian.jira.util.Predicates}.
 *
 * @since v4.1
 */
public class TestPredicates
{
    @Test
    public void testTruePredicate() throws Exception
    {
        final Predicate<Object> objectPredicate = Predicates.truePredicate();
        assertTrue(objectPredicate.evaluate(null));
        assertTrue(objectPredicate.evaluate(1L));
        assertTrue(objectPredicate.evaluate(""));
    }

    @Test
    public void testFalsePredicate() throws Exception
    {
        final Predicate<Object> objectPredicate = Predicates.falsePredicate();
        assertFalse(objectPredicate.evaluate(null));
        assertFalse(objectPredicate.evaluate(1L));
        assertFalse(objectPredicate.evaluate(""));
    }

    @Test
    public void testNotPredicate() throws Exception
    {
        final Predicate<Integer> objectPredicate = Predicates.not(Predicates.falsePredicate());
        assertTrue(objectPredicate.evaluate(1));
    }

    @Test
    public void testNotPredicateNullArgument() throws Exception
    {
        try
        {
            Predicates.not(null);
        }
        catch (IllegalArgumentException expected)
        {
            //expected.
        }
    }
}
