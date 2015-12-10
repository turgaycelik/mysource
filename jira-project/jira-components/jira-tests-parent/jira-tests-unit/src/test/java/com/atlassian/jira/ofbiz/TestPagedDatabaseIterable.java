package com.atlassian.jira.ofbiz;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import com.atlassian.jira.mock.ofbiz.MockOfBizListIterator;
import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.collect.CloseableIterator;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test case for PagedDatabaseIterable
 */
public class TestPagedDatabaseIterable extends AbstractDatabaseIterableTestCase
{
    private static final List<Long> IDS_123 = Lists.newArrayList(new Long(1), new Long(2), new Long(3));

    @Test
    public void testSize()
    {
        final PagedDatabaseIterable<String, Long> pagedDatabaseIterable = new PagedDatabaseIterable<String, Long>(IDS_123)
        {
            @Override
            protected OfBizListIterator createListIterator(final List<Long> ids)
            {
                return new MockOfBizListIterator(Collections.<GenericValue> emptyList());
            }

            @Override
            protected Resolver<GenericValue, String> getResolver()
            {
                return new Resolver<GenericValue, String>()
                {
                    public String get(final GenericValue input)
                    {
                        return "";
                    }
                };
            }
        };

        assertEquals(3, pagedDatabaseIterable.size());
        assertFalse(pagedDatabaseIterable.isEmpty());
    }

    @Test
    public void testEmptyList()
    {
        final PagedDatabaseIterable<Long, Long> pagedDatabaseIterable = new PagedDatabaseIterable<Long, Long>(Collections.<Long> emptyList())
        {
            @Override
            protected OfBizListIterator createListIterator(final List<Long> ids)
            {
                return null;
            }

            @Override
            protected Resolver<GenericValue, Long> getResolver()
            {
                return null;
            }
        };

        assertEquals(0, pagedDatabaseIterable.size());
        assertTrue(pagedDatabaseIterable.isEmpty());
        assertNotNull(pagedDatabaseIterable.iterator());
        assertFalse(pagedDatabaseIterable.iterator().hasNext());
        try
        {
            pagedDatabaseIterable.iterator().next();
            fail("Should have barfed with NoSuchElementException");
        }
        catch (final NoSuchElementException ignored)
        {}
    }

    /**
     * When the PagedDatabaseIterable has no key resolver, then it doesnt keep things in id order.
     */
    @Test
    public void testWithoutSorting()
    {
        final List<Long> idList = Lists.newArrayList(new Long(5), new Long(1), new Long(2), new Long(4), new Long(3));
        final List<Long> expectedList = Lists.newArrayList(new Long(2), new Long(5), new Long(1), new Long(3), new Long(4));

        final OfBizListIterator ofBizListIterator = getOfBizIteratorThatOrdersLikeThis(expectedList);
        mockController.replay();

        final PagedDatabaseIterable<Long, Long> pagedDatabaseIterable = new PagedDatabaseIterable<Long, Long>(idList)
        {
            @Override
            protected OfBizListIterator createListIterator(final List<Long> ids)
            {
                return ofBizListIterator;
            }

            @Override
            protected Resolver<GenericValue, Long> getResolver()
            {
                return new IdGVResolver();
            }
        };

        // is it in the order as made by the database iterator database iterator order
        assertIterableIsInThisOrder(pagedDatabaseIterable, expectedList);
    }

    /**
     * When the PagedDatabaseIterable has a key resolver, then it must keep them in id list order
     */
    @Test
    public void testWithSorting()
    {
        final List<Long> idList = Lists.newArrayList(new Long(5), new Long(1), new Long(2), new Long(4), new Long(3));
        final List<Long> expectedList = idList;

        // our db order is not like our input id order by the result must be
        final List<Long> dbIterList = Lists.newArrayList(new Long(1), new Long(2), new Long(3), new Long(4), new Long(5));

        final Resolver<Long, Long> keyResolver = new IdentityResolver<Long>();

        final OfBizListIterator ofBizListIterator = getOfBizIteratorThatOrdersLikeThis(dbIterList);
        mockController.replay();

        final PagedDatabaseIterable<Long, Long> pagedDatabaseIterable = new PagedDatabaseIterable<Long, Long>(idList, keyResolver)
        {
            @Override
            protected OfBizListIterator createListIterator(final List<Long> ids)
            {
                return ofBizListIterator;
            }

            @Override
            protected Resolver<GenericValue, Long> getResolver()
            {
                return new IdGVResolver();
            }
        };

        // is it in the order as made by the database iterator database iterator order
        assertIterableIsInThisOrder(pagedDatabaseIterable, expectedList);
    }

    /**
     * If the number of ids is larger than a certain page size, then multiple database queries will be made
     */
    @Test
    public void testDatabaseQueryPaging()
    {
        final List<Long> expectedList = getIdsBetweenXandYExclusive(0, 20);

        final List<Long> dbList1 = getIdsBetweenXandYExclusive(0, 10);
        final List<Long> dbList2 = getIdsBetweenXandYExclusive(10, 20);

        final OfBizListIterator ofBizListIterator1 = getOfBizIteratorThatOrdersLikeThis(dbList1);
        final OfBizListIterator ofBizListIterator2 = getOfBizIteratorThatOrdersLikeThis(dbList2);
        mockController.replay();

        final AtomicLong ofbizCallCount = new AtomicLong();

        final PagedDatabaseIterable<Long, Long> pagedDatabaseIterable = new PagedDatabaseIterable<Long, Long>(expectedList, null, 10)
        {
            @Override
            protected OfBizListIterator createListIterator(final List<Long> ids)
            {
                assertTrue(ofbizCallCount.get() < 2);
                final long callCount = ofbizCallCount.incrementAndGet();
                if (callCount == 1)
                {
                    return ofBizListIterator1;
                }
                else
                {
                    return ofBizListIterator2;
                }
            }

            @Override
            protected Resolver<GenericValue, Long> getResolver()
            {
                return new IdGVResolver();
            }
        };

        // is it in the order as made by the database iterator database iterator order
        assertIterableIsInThisOrder(pagedDatabaseIterable, expectedList);
    }

    /**
     * If the number of ids is larger than a certain page size, then multiple database queries will be made but sorting
     * should still be respected
     */
    @Test
    public void testDatabaseQueryPagingWithSorting()
    {
        final List<Long> expectedList = getIdsBetweenXandYExclusive(0, 20);
        Collections.reverse(expectedList);

        final List<Long> dbList1 = getIdsBetweenXandYExclusive(0, 10);
        final List<Long> dbList2 = getIdsBetweenXandYExclusive(10, 20);

        final OfBizListIterator ofBizListIterator1 = getOfBizIteratorThatOrdersLikeThis(dbList1);
        final OfBizListIterator ofBizListIterator2 = getOfBizIteratorThatOrdersLikeThis(dbList2);
        mockController.replay();

        final AtomicLong ofbizCallCount = new AtomicLong();

        final PagedDatabaseIterable<Long, Long> pagedDatabaseIterable = new PagedDatabaseIterable<Long, Long>(expectedList,
            new IdentityResolver<Long>(), 10)
        {
            @Override
            protected OfBizListIterator createListIterator(final List<Long> ids)
            {
                assertTrue(ofbizCallCount.get() < 2);
                final long callCount = ofbizCallCount.incrementAndGet();
                if (callCount == 1)
                {
                    return ofBizListIterator1;
                }
                else
                {
                    return ofBizListIterator2;
                }
            }

            @Override
            protected Resolver<GenericValue, Long> getResolver()
            {
                return new IdGVResolver();
            }
        };

        // is it in the order as made by the database iterator database iterator order
        assertIterableIsInThisOrder(pagedDatabaseIterable, expectedList);
    }

    @Test
    public void testRemoveOnIterator_Sorted()
    {
        final List<Long> idList = Lists.newArrayList(new Long(5), new Long(1), new Long(2), new Long(4), new Long(3));

        // our db order is not like our input id order by the result must be
        final List<Long> dbIterList = Lists.newArrayList(new Long(1), new Long(2), new Long(3), new Long(4), new Long(5));

        final Resolver<Long, Long> keyResolver = new IdentityResolver<Long>();

        final OfBizListIterator ofBizListIterator = getOfBizIteratorThatOrdersLikeThis(dbIterList);
        mockController.replay();

        final PagedDatabaseIterable<Long, Long> pagedDatabaseIterable = new PagedDatabaseIterable<Long, Long>(idList, keyResolver)
        {
            @Override
            protected OfBizListIterator createListIterator(final List<Long> ids)
            {
                return ofBizListIterator;
            }

            @Override
            protected Resolver<GenericValue, Long> getResolver()
            {
                return new IdGVResolver();
            }
        };

        final CloseableIterator<Long> closeableIterator = pagedDatabaseIterable.iterator();
        final Long longValue = closeableIterator.next();
        assertEquals(new Long(5), longValue);
        closeableIterator.remove();
        assertEquals(new Long(1), closeableIterator.next());
    }

    /**
     *
     */
    @Test
    public void testDatabaseQueryPaging_IteratorRemove()
    {
        final List<Long> expectedList = getIdsBetweenXandYExclusive(0, 2);

        final List<Long> dbList1 = getIdsBetweenXandYExclusive(0, 1);
        final List<Long> dbList2 = getIdsBetweenXandYExclusive(1, 2);

        final OfBizListIterator ofBizListIterator1 = getOfBizIteratorThatOrdersLikeThis(dbList1);
        final OfBizListIterator ofBizListIterator2 = getOfBizIteratorThatOrdersLikeThis(dbList2);
        mockController.replay();

        final AtomicLong ofbizCallCount = new AtomicLong();

        final Resolver<Long, Long> keyResolver = new IdentityResolver<Long>();
        final PagedDatabaseIterable<Long, Long> pagedDatabaseIterable = new PagedDatabaseIterable<Long, Long>(expectedList, keyResolver, 1)
        {
            @Override
            protected OfBizListIterator createListIterator(final List<Long> ids)
            {
                assertTrue(ofbizCallCount.get() < 2);
                final long callCount = ofbizCallCount.incrementAndGet();
                if (callCount == 1)
                {
                    return ofBizListIterator1;
                }
                else
                {
                    return ofBizListIterator2;
                }
            }

            @Override
            protected Resolver<GenericValue, Long> getResolver()
            {
                return new IdGVResolver();
            }
        };

        final CloseableIterator<Long> closeableIterator = pagedDatabaseIterable.iterator();
        final long longValue = closeableIterator.next();
        assertEquals(0L, longValue);
        closeableIterator.remove();
        closeableIterator.next();
        closeableIterator.close();
    }
}
