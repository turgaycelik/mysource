package com.atlassian.jira.util.collect;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Simple test for {@link com.atlassian.jira.util.collect.MapBuilder}.
 *
 * @since v4.0
 */
public class TestMapBuilder
{
    @Test
    public void testAdd() throws Exception
    {
        final MapBuilder<String, Object> mapBuilder = MapBuilder.newBuilder();
        mapBuilder.add("a", null).add("b", "b").add(null, "c");

        final Map<String, Object> expectedMap = new HashMap<String, Object>();
        expectedMap.put("a", null);
        expectedMap.put("b", "b");
        expectedMap.put(null, "c");
        final Map<String, Object> actualMap = mapBuilder.toMap();

        assertEquals(expectedMap, actualMap);

        mapBuilder.add("d", "e");

        assertEquals(expectedMap, actualMap);

        expectedMap.put("d", "e");
        assertEquals(expectedMap, mapBuilder.toMap());
    }

    @Test
    public void testAddIfValueNotNull() throws Exception
    {
        final MapBuilder<String, Object> mapBuilder = MapBuilder.newBuilder();
        mapBuilder.addIfValueNotNull("a", null).add("b", "b").add(null, "c");

        final Map<String, Object> expectedMap = new HashMap<String, Object>();
        expectedMap.put("b", "b");
        expectedMap.put(null, "c");
        final Map<String, Object> actualMap = mapBuilder.toMap();

        assertEquals(expectedMap, actualMap);

        mapBuilder.addIfValueNotNull("d", "e");

        assertEquals(expectedMap, actualMap);

        expectedMap.put("d", "e");
        assertEquals(expectedMap, mapBuilder.toMap());
    }

    @Test
    public void testAddAll()
    {
        final MapBuilder<String, Object> mapBuilder = MapBuilder.newBuilder();
        assertEquals(0, mapBuilder.toMap().size());
        mapBuilder.addAll(null);
        assertEquals(0, mapBuilder.toMap().size());
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        mapBuilder.addAll(map);
        assertEquals(3, mapBuilder.toMap().size());
        assertTrue(mapBuilder.toMap().containsKey("two"));
        assertTrue(mapBuilder.toMap().containsValue(3));
    }

    @Test
    public void testBuild()
    {
        final String key1 = "one";
        final Object value1 = new Object();

        final String key2 = "two";
        final Object value2 = new Object();

        final String key3 = "three";
        final Integer value3 = 3;

        final String key4 = "four";
        final Double value4 = 4.0;

        Map<String, Object> objs = new HashMap<String, Object>();
        objs.put(key1, value1);

        assertEquals(objs, MapBuilder.build(key1, value1));

        objs.put(key2, value2);
        assertEquals(objs, MapBuilder.build(key1, value1, key2, value2));

        objs.put(key3, value3);
        assertEquals(objs, MapBuilder.build(key1, value1, key2, value2, key3, value3));

        objs.put(key4, value4);
        assertEquals(objs, MapBuilder.build(key1, value1, key2, value2, key3, value3, key4, value4));
    }

    @Test
    public void testNewBuilder()
    {
        final String key1 = "one";
        final Object value1 = new Object();

        final String key2 = "two";
        final Object value2 = new Object();

        final String key3 = "three";
        final Integer value3 = 3;

        final String key4 = "four";
        final Double value4 = 4.0;

        Map<String, Object> objs = new HashMap<String, Object>();
        objs.put(key1, value1);

        assertEquals(objs, MapBuilder.newBuilder(key1, value1).toMap());

        objs.put(key2, value2);
        assertEquals(objs, MapBuilder.newBuilder(key1, value1, key2, value2).toMap());

        objs.put(key3, value3);
        assertEquals(objs, MapBuilder.newBuilder(key1, value1, key2, value2, key3, value3).toMap());

        objs.put(key4, value4);
        assertEquals(objs, MapBuilder.newBuilder(key1, value1, key2, value2, key3, value3, key4, value4).toMap());
    }
}
