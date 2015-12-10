package com.atlassian.jira.util;

import java.util.Collection;

import org.junit.Assert;

import static org.junit.Assert.assertEquals;

/**
 * Assertions for unit tests to use on collections.
 * This was originally created to replace methods in the deprecated LegacyJiraMockTestCase.
 */
public class CollectionAssert
{
    /**
     * Check that a collection has only one element, and that is the object provided
     */
    public static void checkSingleElementCollection(Collection collection, Object expected)
    {
        assertEquals(1, collection.size());
        assertEquals(expected, collection.iterator().next());
    }

    /**
     * Asserts that the given collections have exactly the same items in an unordered manner.
     * Useful if the expected collections is expressed as a list (which is common) and you don't care about the order
     * of the incoming collection, or that collection is incompatible with List.equals() - eg Set.
     *
     * @param expected Collection
     * @param actual Collection
     */
    public static <T> void assertContainsExactly(final Collection<?> expected, final Collection<?> actual)
    {
        if (expected.size() != actual.size() || !actual.containsAll(expected))
        {
            Assert.fail("Expected a collection with items\n " + expected  + "\nbut got a collection with\n " + actual);
        }
    }
}
