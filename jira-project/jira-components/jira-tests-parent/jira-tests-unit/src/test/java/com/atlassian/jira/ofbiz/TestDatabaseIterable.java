package com.atlassian.jira.ofbiz;

import java.util.List;
import java.util.NoSuchElementException;

import com.atlassian.jira.util.collect.CloseableIterator;

import com.google.common.collect.Lists;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test case for DatabaseIterable
 */
public class TestDatabaseIterable extends AbstractDatabaseIterableTestCase
{
    @Test
    public void testSize()
    {
        final DatabaseIterable<?> iterable = new DatabaseIterable<String>(3, null)
        {
            @Override
            protected OfBizListIterator createListIterator()
            {
                return null;
            }
        };
        assertEquals(3, iterable.size());
        assertFalse(iterable.isEmpty());
    }

    @Test
    public void testEmpty()
    {
        final DatabaseIterable<?> iterable = new DatabaseIterable<String>(0, null)
        {
            @Override
            protected OfBizListIterator createListIterator()
            {
                return null;
            }
        };

        assertEquals(0, iterable.size());
        assertTrue(iterable.isEmpty());
        assertNotNull(iterable.iterator());
        assertFalse(iterable.iterator().hasNext());
        try
        {
            iterable.iterator().next();
            fail("Should have barfed with NoSuchElementException");
        }
        catch (final NoSuchElementException ignored)
        {}
    }

    @Test
    public void testRemove()
    {
        final OfBizListIterator ofBizListIterator = mockController.getMock(OfBizListIterator.class);

        final DatabaseIterable<Long> iterable = new DatabaseIterable<Long>(1, new IdGVResolver())
        {
            @Override
            protected OfBizListIterator createListIterator()
            {
                return ofBizListIterator;
            }
        };
        mockController.replay();

        final CloseableIterator<Long> iterator = iterable.iterator();
        try
        {
            iterator.remove();
            fail("Should have barfed wity a UnsupportedOperationException");
        }
        catch (final UnsupportedOperationException ignored)
        {}
    }

    /**
     * Can we navigate along a set of data as expected
     */
    @Test
    public void testBasicIteration()
    {
        final List<Long> expectedList = Lists.newArrayList(new Long(2), new Long(5), new Long(1), new Long(3), new Long(4));

        final OfBizListIterator ofBizListIterator = getOfBizIteratorThatOrdersLikeThis(expectedList);
        mockController.replay();

        final DatabaseIterable<Long> iterable = new DatabaseIterable<Long>(expectedList.size(), new IdGVResolver())
        {
            @Override
            protected OfBizListIterator createListIterator()
            {
                return ofBizListIterator;
            }
        };

        // is it in the order as made by the database iterator database iterator order
        assertIterableIsInThisOrder(iterable, expectedList);
    }

    /**
     * Once an interator is closed is cant be called on again
     */
    @Test
    public void testIteratorAfterClose()
    {
        final List<Long> expectedList = Lists.newArrayList(new Long(2), new Long(5), new Long(1), new Long(3), new Long(4));

        final OfBizListIterator ofBizListIterator = getOfBizIteratorThatOrdersLikeThis(expectedList);
        mockController.replay();

        final DatabaseIterable<Long> iterable = new DatabaseIterable<Long>(expectedList.size(), new IdGVResolver())
        {
            @Override
            protected OfBizListIterator createListIterator()
            {
                return ofBizListIterator;
            }
        };
        assertIterableIsInThisOrder(iterable, expectedList);
    }
}
