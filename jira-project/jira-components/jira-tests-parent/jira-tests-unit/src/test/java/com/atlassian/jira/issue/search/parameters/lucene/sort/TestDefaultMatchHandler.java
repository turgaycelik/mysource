package com.atlassian.jira.issue.search.parameters.lucene.sort;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 *
 * @since v5.1
 */
public class TestDefaultMatchHandler
{
    private void assertSingleton(String expectedValue, Collection<String> collection)
    {
        assertTrue(collection instanceof List);
        assertEquals(1, collection.size());
        assertEquals(expectedValue, ((List<String>) collection).get(0));
    }

    private void assertResults(DefaultMatchHandler handler, Object... expectedValues)
    {
        assertResults(handler.getResults(), expectedValues);
    }

    private void assertResults(List<String>[] results, Object... expectedValues)
    {
        assertEquals("wrong array size", expectedValues.length, results.length);
        for (int i=0; i<expectedValues.length; ++i)
        {
            if (expectedValues[i] == null)
            {
                assertNull("i=" + i, results[i]);
            }
            else if (expectedValues[i] instanceof String)
            {
                assertEquals(asList(expectedValues[i]), results[i]);
            }
            else if (expectedValues[i] instanceof List<?>)
            {
                assertEquals(expectedValues[i], results[i]);
            }
        }
    }

    @Test
    public void testCorrectNumberOfSlots()
    {
        assertResults(new DefaultMatchHandler(0));
        assertResults(new DefaultMatchHandler(3), null, null, null);
        assertResults(new DefaultMatchHandler(4), null, null, null, null);
        assertResults(new DefaultMatchHandler(8), null, null, null, null, null, null, null, null);
    }

    @Test
    public void testNullValuesOnly()
    {
        final DefaultMatchHandler handler = new DefaultMatchHandler(5);
        handler.handleMatchedDocument(1, null);
        handler.handleMatchedDocument(3, null);
        final List<String>[] results = handler.getResults();

        assertEquals(5, results.length);
        assertNull(results[0]);
        assertSingleton(null, results[1]);
        assertNull(results[2]);
        assertSingleton(null, results[3]);
        assertNull(results[4]);

        // These should share a singleton list of (null)
        assertSame(results[1], results[3]);
    }

    @Test
    public void testNullValuesLater()
    {
        final DefaultMatchHandler handler = new DefaultMatchHandler(5);
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

        // These should share a singleton list of (null)
        assertSame(results[2], results[3]);
    }

    @Test
    public void testGetDistinctCollectionsForRepeats()
    {
        final DefaultMatchHandler handler = new DefaultMatchHandler(5);
        handler.handleMatchedDocument(3, "Fred");
        handler.handleMatchedDocument(4, "Fred");
        handler.handleMatchedDocument(3, "Harry");
        handler.handleMatchedDocument(4, "Harry");
        final List<String> firstResult = handler.getResults()[4];
        handler.handleMatchedDocument(1, "Fred");
        handler.handleMatchedDocument(2, "Fred");
        handler.handleMatchedDocument(3, "George");
        handler.handleMatchedDocument(4, "George");
        final List<String>[] results = handler.getResults();

        assertResults(results, null, "Fred", "Fred", asList("Fred", "Harry", "George"), asList("Fred", "Harry", "George"));
        // These should share a singleton list of "Fred"
        assertSame(results[1], results[2]);
        // but once the list is > 1 in size, it should get reused but not shared
        assertSame(firstResult, results[4]);
        assertNotSame(results[3], results[4]);
    }

    @Test
    public void testSimpleStuff()
    {
        final DefaultMatchHandler handler = new DefaultMatchHandler(5);
        handler.handleMatchedDocument(4, "Fred");
        handler.handleMatchedDocument(1, "George");
        handler.handleMatchedDocument(2, "Ginny");

        final List<String>[] results = handler.getResults();

        assertResults(results, null, "George", "Ginny", null, "Fred");
    }

    @Test
    public void testMultipleValuesReceived()
    {
        final DefaultMatchHandler handler = new DefaultMatchHandler(5);
        handler.handleMatchedDocument(4, "Fred");
        handler.handleMatchedDocument(1, "Percy");
        handler.handleMatchedDocument(1, "George");
        handler.handleMatchedDocument(2, "George");
        handler.handleMatchedDocument(1, "Ron");
        handler.handleMatchedDocument(2, "Ginny");

        final List<String>[] results = handler.getResults();
        assertResults(results, null, asList("Percy", "George", "Ron"), asList("George", "Ginny"), null, "Fred");
    }
}
