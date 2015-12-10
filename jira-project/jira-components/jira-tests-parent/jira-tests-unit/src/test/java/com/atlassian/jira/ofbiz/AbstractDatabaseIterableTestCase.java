package com.atlassian.jira.ofbiz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.collect.EnclosedIterable;

import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * A base class with some common methods
 *
 * @since v3.13
 */
public abstract class AbstractDatabaseIterableTestCase extends MockControllerTestCase
{
    protected List<Long> getIdsBetweenXandYExclusive(final int start, final int endExclusive)
    {
        final List<Long> list = new ArrayList<Long>();
        for (int j = start; j < endExclusive; j++)
        {
            list.add(new Long(j));
        }
        return list;
    }

    protected <T> void assertIterableIsInThisOrder(final EnclosedIterable<T> closeableIterable, final List<T> expectedList)
    {
        assertEquals(expectedList.size(), closeableIterable.size());
        if (expectedList.size() == 0)
        {
            assertFalse(closeableIterable.isEmpty());
        }
        final Iterator<T> expected = expectedList.iterator();
        closeableIterable.foreach(new Consumer<T>()
        {
            public void consume(final T actual)
            {
                assertEquals(expected.next(), actual);
            }
        });
    }

    /**
     * This creates a OfBizListIterator that has GenericValues with and id of the expectedIdList and also this will
     * expect a close at the end of the call.
     *
     * @param expectedIdList a List of Long objects
     * @return an OfBizListIterator that iterators this list
     */
    protected OfBizListIterator getOfBizIteratorThatOrdersLikeThis(final List<Long> expectedIdList)
    {
        final OfBizListIterator ofBizListIterator = mockController.getMock(OfBizListIterator.class);
        for (final Long expectedId : expectedIdList)
        {
            final MockGenericValue gv = new MockGenericValue("MockGV", EasyMap.build("id", expectedId));

            ofBizListIterator.next();
            mockController.setReturnValue(gv);
        }
        // there will be one extra call to next() that is to return null
        ofBizListIterator.next();
        mockController.setReturnValue(null);

        ofBizListIterator.close();

        return ofBizListIterator;
    }

    class IdentityResolver<I> implements Resolver<I, I>
    {
        public I get(final I input)
        {
            return input;
        }
    }

    class IdGVResolver implements Resolver<GenericValue, Long>
    {
        public Long get(final GenericValue input)
        {
            return input.getLong("id");
        }
    }
}
