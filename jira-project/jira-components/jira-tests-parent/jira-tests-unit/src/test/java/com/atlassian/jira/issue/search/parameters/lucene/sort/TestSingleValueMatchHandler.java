package com.atlassian.jira.issue.search.parameters.lucene.sort;


import java.util.Collection;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @since v5.1
 */
public class TestSingleValueMatchHandler
{
    private void assertSingleton(String expectedValue, Collection<String> collection)
    {
        assertTrue(collection instanceof List);
        assertEquals(1, collection.size());
        assertEquals(expectedValue, ((List<String>) collection).get(0));
    }

    private void assertResults(SingleValueMatchHandler handler, String... expectedValues)
    {
        assertResults(handler.getResults(), expectedValues);
    }

    private void assertResults(List<String>[] results, String... expectedValues)
    {
        assertEquals("wrong array size", expectedValues.length, results.length);
        for (int i=0; i<expectedValues.length; ++i)
        {
            if (expectedValues[i] == null)
            {
                assertNull("i=" + i, results[i]);
            }
            else
            {
                assertSingleton(expectedValues[i], results[i]);
            }
        }
    }

    @Test
    public void testCorrectNumberOfSlots()
    {
        assertResults(new SingleValueMatchHandler(0));
        assertResults(new SingleValueMatchHandler(3), null, null, null);
        assertResults(new SingleValueMatchHandler(4), null, null, null, null);
        assertResults(new SingleValueMatchHandler(8), null, null, null, null, null, null, null, null);
    }

    @Test
    public void testNullValuesOnly()
    {
        final SingleValueMatchHandler handler = new SingleValueMatchHandler(5);
        handler.handleMatchedDocument(1, null);
        handler.handleMatchedDocument(3, null);
        final List<String>[] results = handler.getResults();

        assertEquals(5, results.length);
        assertNull(results[0]);
        assertSingleton(null, results[1]);
        assertNull(results[2]);
        assertSingleton(null, results[3]);
        assertNull(results[4]);

        assertSame(results[1], results[3]);
    }

    @Test
    public void testNullValuesLater()
    {
        final SingleValueMatchHandler handler = new SingleValueMatchHandler(5);
        handler.handleMatchedDocument(1, "Fred");
        handler.handleMatchedDocument(2, null);
        handler.handleMatchedDocument(3, null);
        final List<String>[] results = handler.getResults();

        assertEquals(5, results.length);
        assertNull(results[0]);
        assertSingleton("Fred", results[1]);
        assertSingleton(null, results[2]);
        assertSingleton(null, results[3]);
        assertNull(results[4]);

        assertSame(results[2], results[3]);
    }

    @Test
    public void testGetSameSingletonForRepeatedValue()
    {
        final SingleValueMatchHandler handler = new SingleValueMatchHandler(5);
        handler.handleMatchedDocument(4, "Fred");
        final List<String> firstResult = handler.getResults()[4];
        handler.handleMatchedDocument(1, "Fred");
        handler.handleMatchedDocument(2, "Fred");
        final List<String>[] results = handler.getResults();

        assertResults(results, null, "Fred", "Fred", null, "Fred");
        assertSame(firstResult, results[1]);
        assertSame(firstResult, results[2]);
        assertSame(firstResult, results[4]);
    }

    @Test
    public void testSimpleStuff()
    {
        final SingleValueMatchHandler handler = new SingleValueMatchHandler(5);
        handler.handleMatchedDocument(4, "Fred");
        handler.handleMatchedDocument(1, "George");
        handler.handleMatchedDocument(2, "Ginny");

        final List<String>[] results = handler.getResults();

        assertResults(results, null, "George", "Ginny", null, "Fred");
    }

    @Test
    public void testMultipleValuesReceived()
    {
        final SingleValueMatchHandler handler = new SingleValueMatchHandler(5);
        handler.handleMatchedDocument(4, "Fred");
        handler.handleMatchedDocument(1, "Percy");
        handler.handleMatchedDocument(1, "George");
        handler.handleMatchedDocument(2, "Ginny");

        final List<String>[] results = handler.getResults();
        assertResults(results, null, "George", "Ginny", null, "Fred");
    }
}

