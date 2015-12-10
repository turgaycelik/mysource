package com.atlassian.jira.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MultiMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestMapUtils
{
    @Test
    public void testInvertMap_Empty() throws Exception
    {
        Map map = new HashMap();
        MultiMap actual = MapUtils.invertMap(map);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testInvertMap() throws Exception
    {
        Map map = new HashMap();
        map.put(1, "A");
        map.put(2, "B");
        map.put(3, "B");
        MultiMap actual = MapUtils.invertMap(map);
        assertEquals(2, actual.size());
        // A
        Collection value = (Collection) actual.get("A");
        assertTrue(value.contains(1));
        assertEquals(1, value.size());
        // B
        value = (Collection) actual.get("B");
        assertTrue(value.contains(2));
        assertTrue(value.contains(3));
        assertEquals(2, value.size());
    }
}
