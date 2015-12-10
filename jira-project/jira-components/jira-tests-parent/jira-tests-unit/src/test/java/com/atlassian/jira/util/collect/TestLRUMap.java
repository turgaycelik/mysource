package com.atlassian.jira.util.collect;

import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestLRUMap
{
    @Test
    public void testCapacityNotExceeded() throws Exception
    {
        final Map<Integer, String> map = LRUMap.newLRUMap(5);
        for (int i = 0; i < 10; i++)
        {
            map.put(i, Integer.toString(i));
        }

        assertEquals(5, map.size());
    }

    @Test
    public void testOldestRemoved() throws Exception
    {
        final Map<Integer, String> map = LRUMap.newLRUMap(3);
        for (int i = 0; i < 5; i++)
        {
            map.put(i, Integer.toString(i));
        }

        assertFalse(map.containsKey(0));
        assertFalse(map.containsKey(1));
        assertTrue(map.containsKey(2));
        assertTrue(map.containsKey(3));
        assertTrue(map.containsKey(4));
    }

    @Test
    public void testGetReorders() throws Exception
    {
        final Map<Integer, String> map = LRUMap.newLRUMap(5);
        for (int i = 0; i < 5; i++)
        {
            map.put(i, Integer.toString(i));
        }

        map.get(3);

        final Iterator<Integer> it = map.keySet().iterator();
        assertEquals(new Integer(0), it.next());
        assertEquals(new Integer(1), it.next());
        assertEquals(new Integer(2), it.next());
        assertEquals(new Integer(4), it.next());
        assertEquals(new Integer(3), it.next());
    }

    @Test
    public void testOldestRemovedAfterGetReorder() throws Exception
    {
        final Map<Integer, String> map = LRUMap.newLRUMap(5);
        for (int i = 0; i < 5; i++)
        {
            map.put(i, Integer.toString(i));
        }

        map.get(1);
        map.put(5, Integer.toString(5));
        map.put(6, Integer.toString(6));
        assertFalse(map.containsKey(0));
        assertTrue(map.containsKey(1));
        assertFalse(map.containsKey(2));
        assertTrue(map.containsKey(3));
        assertTrue(map.containsKey(4));
        assertTrue(map.containsKey(5));
        assertTrue(map.containsKey(6));
    }
}
