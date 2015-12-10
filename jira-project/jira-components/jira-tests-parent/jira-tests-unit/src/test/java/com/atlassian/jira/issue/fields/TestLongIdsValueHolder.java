package com.atlassian.jira.issue.fields;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;

public class TestLongIdsValueHolder
{
    @Test
    public void testLongIdsValueHolderHandlesNewValues()
    {
        final LongIdsValueHolder holder = new LongIdsValueHolder(newArrayList("nv_NEW_COMPONENT1"));

        assertEquals(newHashSet("NEW_COMPONENT1"), holder.getValuesToAdd());
        assertEquals("", holder.getInputText());
        assertEquals(Lists.<Long>newArrayList(), newArrayList(holder));
    }

    @Test
    public void testLongIdsValueHolderRemovesBadValues()
    {
        final LongIdsValueHolder holder = new LongIdsValueHolder(newArrayList("baddy"));

        assertEquals(Sets.<String>newHashSet(), holder.getValuesToAdd());
        assertEquals("baddy", holder.getInputText());
        assertEquals(Lists.<Long>newArrayList(), newArrayList(holder));
    }

    @Test
    public void testLongIdsValueHolderHandlesExistingIds()
    {
        final LongIdsValueHolder holder = new LongIdsValueHolder(newArrayList("10000"));

        assertEquals(Sets.<String>newHashSet(), holder.getValuesToAdd());
        assertEquals("", holder.getInputText());
        assertEquals(newArrayList(10000L), newArrayList(holder));
    }

    @Test
    public void testLongIdsValueHolderHandlesMultipleInputs()
    {
        final LongIdsValueHolder holder = new LongIdsValueHolder(newArrayList("10000", "nv_NEW_COMPONENT1", "baddy", "", "nv_", "10231", "nv_1.0"));

        assertEquals(newHashSet("NEW_COMPONENT1", "1.0"), holder.getValuesToAdd());
        assertEquals("baddy", holder.getInputText());
        assertEquals(newArrayList(10000L, 10231L), newArrayList(holder));
    }
}