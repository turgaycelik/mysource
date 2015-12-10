package com.atlassian.jira.util.collect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.jira.util.Supplier;

import com.google.common.collect.Lists;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestMultiMaps
{
    @Test
    public void testCreator() throws Exception
    {
        final MultiMap<String, String, Set<String>> map = MultiMaps.create(new HashMap<String, Set<String>>(), new Supplier<Set<String>>()
        {
            public Set<String> get()
            {
                return new HashSet<String>();
            }
        });

        map.putSingle("one", "one");
        map.putSingle("one", "two");
        map.putSingle("one", "three");
        map.putSingle("one", "four");
        map.putSingle("one", "two");

        map.putSingle("two", "one");
        map.putSingle("two", "two");
        map.putSingle("two", "two");

        assertEquals(2, map.size());
        assertEquals(4, map.allValues().size());
        assertEquals(6, map.sizeAll());

        assertTrue(map.contains("one"));
        assertTrue(map.contains("two"));
        assertTrue(map.contains("three"));
        assertTrue(map.contains("four"));
        assertFalse(map.contains("five"));

        assertTrue(map.containsValue(new HashSet<String>(Lists.newArrayList("one", "two"))));
        assertFalse(map.containsValue(new HashSet<String>(Lists.newArrayList("one", "two", "three"))));
        assertTrue(map.containsValue(new HashSet<String>(Lists.newArrayList("one", "two", "three", "four"))));

        assertEquals(4, map.get("one").size());
        assertEquals(2, map.get("two").size());
    }
}
