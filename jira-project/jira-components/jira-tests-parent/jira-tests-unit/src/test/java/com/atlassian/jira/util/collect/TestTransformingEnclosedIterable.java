package com.atlassian.jira.util.collect;

import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Resolver;

import com.google.common.collect.Lists;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * A test class for DecoratingIterable
 *
 * @since v3.13
 */
public class TestTransformingEnclosedIterable
{
    @Test
    public void testDelegation()
    {
        final EnclosedIterable<Long> delegateIterable = new MockCloseableIterable<Long>(Lists.newArrayList(666L));
        final Resolver<Long, Long> decorator = new Resolver<Long, Long>()
        {
            public Long get(final Long input)
            {
                return input + 1;
            }
        };
        final TransformingEnclosedIterable<Long, Long> decoratingIterable = new TransformingEnclosedIterable<Long, Long>(delegateIterable, decorator);
        assertFalse(decoratingIterable.isEmpty());
        assertEquals(1, decoratingIterable.size());
        decoratingIterable.foreach(new Consumer<Long>()
        {
            public void consume(final Long element)
            {
                assertEquals(667L, element.longValue());
            }
        });
    }
}
