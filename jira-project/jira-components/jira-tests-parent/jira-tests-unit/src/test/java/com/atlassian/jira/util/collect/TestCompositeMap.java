package com.atlassian.jira.util.collect;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for CompositeMap.
 *
 * @since v5.1
 */
public class TestCompositeMap
{
    //================================================================
    // CONSTANTS
    //================================================================

    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY3 = "key3";
    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";
    private static final String VALUE3 = "value3";
    private static final String NEW_VALUE1 = "newvalue1";
    private static final String NEW_VALUE2 = "newvalue2";
    private static final String NEW_VALUE3 = "newvalue3";
    private static final String NEVER_USED = "should-never-find-this";

    private static final Map<String,String> INITIAL_MAP = ImmutableMap.<String,String>builder()
            .put(KEY1,VALUE1)
            .put(KEY2,VALUE2)
            .build();

    //================================================================
    // FACTORIES FOR TEST CONFIGURATIONS
    //================================================================

    private static Map<String,String> simpleCompositeMap()
    {
        final Map<String,String> one = newHashMap();
        one.put(KEY1, VALUE1);
        final Map<String,String> two = singletonMap(KEY2, VALUE2);
        final Map<String,String> map = CompositeMap.of(one,two);
        assertNotSame(map, one);
        assertNotSame(map, two);
        assertInitialValues(map);
        return map;
    }

    private static Map<String,String> immutableCompositeMap()
    {
        final Map<String,String> one = singletonMap(KEY1, VALUE1);
        final Map<String,String> two = singletonMap(KEY2, VALUE2);
        final Map<String,String> map = CompositeMap.of(one, two);
        assertNotSame(map, one);
        assertNotSame(map, two);
        assertInitialValues(map);
        return map;
    }

    //================================================================
    // COMMON CHECKS
    //================================================================

    private static void assertInitialValues(Map<String,String> map)
    {
        assertMapConsistency(map, KEY1, KEY2);
        assertEquals(VALUE1, map.get(KEY1));
        assertEquals(VALUE2, map.get(KEY2));
        assertNull(map.get(KEY3));
        assertMapContainsValues(map, VALUE1, VALUE2);
        assertMapDoesNotContainValues(map, VALUE3);
        assertEquals(map, INITIAL_MAP);
    }

    private static void assertInitialMinusKey1(Map<String,String> map)
    {
        assertMapConsistency(map, KEY2);
        assertNull(map.get(KEY1));
        assertEquals(VALUE2, map.get(KEY2));
        assertNull(map.get(KEY3));
        assertMapContainsValues(map, VALUE2);
        assertMapDoesNotContainValues(map, VALUE1, VALUE3);
    }

    private static void assertInitialMinusKey2(Map<String,String> map)
    {
        assertMapConsistency(map, KEY1);
        assertEquals(VALUE1, map.get(KEY1));
        assertNull(map.get(KEY2));
        assertNull(map.get(KEY3));
        assertMapContainsValues(map, VALUE1);
        assertMapDoesNotContainValues(map, VALUE2, VALUE3);
    }

    private static void assertEmpty(Map<String,String> map)
    {
        assertMapConsistency(map);
        assertNull(map.get(KEY1));
        assertNull(map.get(KEY2));
        assertNull(map.get(KEY3));
    }

    private static void assertMapConsistency(Map<String,String> map, String... expectedKeys)
    {
        final Set<String> keySet = map.keySet();
        final Set<Map.Entry<String,String>> entrySet = map.entrySet();
        final Collection<String> values = map.values();
        final Set<String> expectedKeySet = new HashSet<String>(asList(expectedKeys));

        // Quick sanity check because it gives a more useful error message than most of the others
        assertEquals(expectedKeySet, keySet);

        // Verify isEmpty() and size() for map, keySet(), entrySet(), and values()
        assertEquals(expectedKeySet.isEmpty(), map.isEmpty());
        assertEquals(expectedKeySet.isEmpty(), keySet.isEmpty());
        assertEquals(expectedKeySet.isEmpty(), entrySet.isEmpty());
        assertEquals(expectedKeySet.isEmpty(), values.isEmpty());
        assertEquals(expectedKeySet.size(), map.size());
        assertEquals(expectedKeySet.size(), keySet.size());
        assertEquals(expectedKeySet.size(), entrySet.size());
        assertEquals(expectedKeySet.size(), values.size());

        // Check hash codes and keySet().containsAll(...)
        assertTrue(keySet.containsAll(expectedKeySet));
        assertExpectedHashCodes(map);

        // Verify iteration (does not verify any kind of ordering or correct key-value association)
        final Set<String> seen = new HashSet<String>();
        final Iterator<String> keySetIter = keySet.iterator();
        final Iterator<Map.Entry<String,String>> entrySetIter = entrySet.iterator();
        final Iterator<String> valuesIter = values.iterator();
        for (String key : expectedKeys)
        {
            assertTrue(map.containsKey(key));
            assertTrue(keySet.contains(key));
            assertTrue(keySetIter.hasNext());
            final String foundKey = keySetIter.next();
            if (!seen.add(foundKey))
            {
                fail("Found duplicate key during iteration: " + foundKey);
            }
            assertTrue(entrySetIter.hasNext());
            entrySetIter.next();
            assertTrue(valuesIter.hasNext());
            valuesIter.next();
        }
        assertFalse(keySetIter.hasNext());
        assertFalse(entrySetIter.hasNext());
        assertFalse(valuesIter.hasNext());
        assertEquals(expectedKeys.length, seen.size());

        // Should ever see any of these
        assertFalse(seen.contains(NEVER_USED));
        assertFalse(keySet.contains(NEVER_USED));
        assertFalse(map.containsKey(NEVER_USED));
        assertFalse(map.containsValue(NEVER_USED));
    }

    private static void assertExpectedHashCodes(Map<String,String> map)
    {
        int hash = 0;
        for (Map.Entry<String,String> entry : map.entrySet())
        {
            final String key = entry.getKey();
            final String value = entry.getValue();
            final int entryHash = ((key != null) ? key.hashCode() : 0) ^ ((value != null) ? value.hashCode() : 0);
            assertEquals(String.valueOf(entry.getKey()), entryHash, entry.hashCode());
            hash += entryHash;
        }
        assertEquals(hash, map.hashCode());

        // This should be redundant
        assertEquals(newHashMap(map).hashCode(), map.hashCode());
    }

    private static void assertMapContainsValues(Map<String,String> map, String... values)
    {
        final Set<String> expectedValues = new HashSet<String>(asList(values));
        final Set<String> actualValues = new HashSet<String>(map.values());
        assertEquals(expectedValues, actualValues);
        for (String value : expectedValues)
        {
            assertTrue(value, map.containsValue(value));
        }
    }

    private static void assertMapDoesNotContainValues(Map<String,String> map, String... values)
    {
        for (String value : values)
        {
            assertFalse(value, map.containsValue(value));
        }
    }

    private static void shouldHaveFailed()
    {
        fail("Expected an UnsupportedOperationException and did not get one");
    }

    //================================================================
    // TESTS
    //================================================================

    @Test(expected = IllegalArgumentException.class)
    public void testOf_nullMapOne_illegal()
    {
        CompositeMap.of(null, emptyMap());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOf_nullMapTwo_illegal()
    {
        CompositeMap.of(emptyMap(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOf_nullMapBoth_illegal()
    {
        CompositeMap.of(null, null);
    }

    @Test
    public void testOf_emptyMapOne_noShortCircuit()
    {
        final Map<String,String> one = emptyMap();
        final Map<String,String> two = singletonMap(KEY2, VALUE2);
        final Map<String,String> map = CompositeMap.of(one, two);
        assertNotSame(one, map);
        assertNotSame(two, map);
    }

    @Test
    public void testOf_emptyMapTwo_shortCircuit()
    {
        final Map<String,String> one = singletonMap(KEY1, VALUE1);
        final Map<String,String> two = emptyMap();
        final Map<String,String> map = CompositeMap.of(one, two);
        assertSame(one, map);
    }

    @Test
    public void testGet()
    {
        // These factories implicitly test simple gets
        simpleCompositeMap();
        immutableCompositeMap();
    }

    @Test
    public void testPut_newValue()
    {
        final Map<String,String> map = simpleCompositeMap();
        assertNull(map.put(KEY3, VALUE3));
        assertMapConsistency(map, KEY1, KEY2, KEY3);
        assertEquals(VALUE3, map.get(KEY3));
        assertEquals(3, map.size());
        assertFalse(map.isEmpty());
        assertMapContainsValues(map, VALUE1, VALUE2, VALUE3);
    }

    @Test
    public void testPut_replaceValueCurrentlyInMapOne()
    {
        final Map<String,String> map = simpleCompositeMap();
        assertEquals(VALUE1, map.put(KEY1, VALUE3));
        assertEquals(VALUE3, map.get(KEY1));
        assertMapConsistency(map, KEY1, KEY2);
        assertFalse(map.containsValue(VALUE1));
        assertTrue(map.containsValue(VALUE2));
        assertTrue(map.containsValue(VALUE3));
        assertMapContainsValues(map, VALUE2, VALUE3);
        assertMapDoesNotContainValues(map, VALUE1);
    }

    @Test
    public void testPut_replaceValueRemovedFromMapOne()
    {
        final Map<String,String> map = simpleCompositeMap();
        assertEquals(VALUE1, map.remove(KEY1));
        assertInitialMinusKey1(map);

        assertNull(map.put(KEY1, VALUE3));
        assertEquals(VALUE3, map.get(KEY1));
        assertMapConsistency(map, KEY1, KEY2);
        assertFalse(map.containsValue(VALUE1));
        assertTrue(map.containsValue(VALUE2));
        assertTrue(map.containsValue(VALUE3));
        assertMapContainsValues(map, VALUE2, VALUE3);
        assertMapDoesNotContainValues(map, VALUE1);
    }

    @Test
    public void testPut_replaceValueCurrentlyInMapTwo()
    {
        final Map<String,String> map = simpleCompositeMap();
        assertEquals(VALUE2, map.put(KEY2, VALUE3));
        assertEquals(VALUE3, map.get(KEY2));
        assertMapConsistency(map, KEY1, KEY2);
        assertMapContainsValues(map, VALUE1, VALUE3);
        assertMapDoesNotContainValues(map, VALUE2);
    }

    @Test
    public void testPut_replaceValueRemovedFromMapTwo()
    {
        final Map<String,String> map = simpleCompositeMap();
        assertEquals(VALUE2, map.remove(KEY2));
        assertInitialMinusKey2(map);

        assertNull(map.put(KEY2, VALUE3));
        assertEquals(VALUE3, map.get(KEY2));
        assertMapConsistency(map, KEY1, KEY2);
        assertMapContainsValues(map, VALUE1, VALUE3);
        assertMapDoesNotContainValues(map, VALUE2);
    }

    @Test
    public void testPut_immutableMapOne_unsupported()
    {
        final Map<String,String> map = immutableCompositeMap();
        try
        {
            map.put(VALUE3, KEY3);
            shouldHaveFailed();
        }
        catch (UnsupportedOperationException uoe)
        {
            assertInitialValues(map);
        }
    }

    @Test
    public void testPutAll_replaceMapOneValueAndAddNewKey()
    {
        final Map<String,String> map = simpleCompositeMap();
        final Map<String,String> source = newHashMap();
        source.put(KEY1, NEW_VALUE1);
        source.put(KEY3, NEW_VALUE3);
        map.putAll(source);

        assertEquals(NEW_VALUE1, map.get(KEY1));
        assertEquals(VALUE2, map.get(KEY2));
        assertEquals(NEW_VALUE3, map.get(KEY3));
        assertMapConsistency(map, KEY1, KEY2, KEY3);
        assertMapContainsValues(map, NEW_VALUE1, VALUE2, NEW_VALUE3);
        assertMapDoesNotContainValues(map, VALUE1, NEW_VALUE2, VALUE3);
    }

    @Test
    public void testPutAll_replaceValuesInBothMaps()
    {
        final Map<String,String> map = simpleCompositeMap();
        final Map<String,String> source = newHashMap();
        source.put(KEY1, VALUE1);
        source.put(KEY2, NEW_VALUE2);
        map.putAll(source);

        assertEquals(VALUE1, map.get(KEY1));
        assertEquals(NEW_VALUE2, map.get(KEY2));
        assertNull(map.get(KEY3));
        assertMapConsistency(map, KEY1, KEY2);
        assertMapContainsValues(map, VALUE1, NEW_VALUE2);
        assertMapDoesNotContainValues(map, NEW_VALUE1, VALUE2, VALUE3, NEW_VALUE3);
    }

    @Test
    public void testPutAll_replacePreviouslyRemovedValues()
    {
        final Map<String,String> map = simpleCompositeMap();
        final Map<String,String> source = newHashMap();

        assertNull(map.put(KEY3, VALUE3));
        assertEquals(VALUE1, map.remove(KEY1));
        assertEquals(VALUE2, map.remove(KEY2));
        assertEquals(VALUE3, map.remove(KEY3));
        assertEmpty(map);

        source.put(KEY1, NEW_VALUE1);
        source.put(KEY2, NEW_VALUE2);
        source.put(KEY3, NEW_VALUE3);
        map.putAll(source);

        assertEquals(NEW_VALUE1, map.get(KEY1));
        assertEquals(NEW_VALUE2, map.get(KEY2));
        assertEquals(NEW_VALUE3, map.get(KEY3));
        assertMapConsistency(map, KEY1, KEY2, KEY3);
        assertMapContainsValues(map, NEW_VALUE1, NEW_VALUE2, NEW_VALUE3);
        assertMapDoesNotContainValues(map, VALUE1, VALUE2, VALUE3);
    }

    @Test
    public void testPutAll_sourceMapIsEmpty()
    {
        final Map<String,String> map = simpleCompositeMap();
        final Map<String,String> source = emptyMap();
        map.putAll(source);
        assertInitialValues(map);
    }

    @Test
    public void testPutAll_sourceMapIsComposite()
    {
        final Map<String,String> target = newHashMap();
        target.putAll(simpleCompositeMap());
        assertInitialValues(target);
    }

    @Test
    public void testPutAll_bothMapsAreComposite()
    {
        final Map<String,String> map = simpleCompositeMap();
        map.clear();
        assertEmpty(map);
        map.putAll(immutableCompositeMap());
        assertInitialValues(map);
    }

    @Test
    public void testPutAll_targetMapIsImmutableComposite_unsupported()
    {
        final Map<String,String> map = immutableCompositeMap();
        try
        {
            map.putAll(singletonMap(KEY3, VALUE3));
            shouldHaveFailed();
        }
        catch (UnsupportedOperationException uoe)
        {
            assertInitialValues(map);
        }
    }

    @Test
    public void testPutRemove_null()
    {
        final Map<String,String> map = simpleCompositeMap();
        map.put(null, VALUE3);
        map.put(KEY1, null);
        map.put(KEY2, null);
        map.put(KEY3, null);
        assertEquals(null, map.get(KEY1));
        assertEquals(null, map.get(KEY2));
        assertEquals(null, map.get(KEY3));
        assertEquals(VALUE3, map.get(null));
        assertMapConsistency(map, KEY1, KEY2, KEY3, null);
        assertMapContainsValues(map, null, VALUE3);
        assertMapDoesNotContainValues(map, VALUE1, VALUE2);

        assertNull(map.remove("x" + KEY1));
        assertFalse(map.keySet().remove("x" + KEY2));
        assertFalse(map.entrySet().remove(entry("x" + KEY3, (String)null)));
        assertMapConsistency(map, KEY1, KEY2, KEY3, null);
        assertMapContainsValues(map, null, VALUE3);
        assertMapDoesNotContainValues(map, VALUE1, VALUE2);

        assertEquals(VALUE3, map.remove(null));
        assertNull(map.remove(KEY1));
        assertTrue(map.keySet().remove(KEY2));
        assertTrue(map.entrySet().remove(entry(KEY3, (String) null)));
        assertEmpty(map);
    }

    @Test
    public void testRemove_fromMapOne()
    {
        final Map<String,String> map = simpleCompositeMap();
        assertEquals(VALUE1, map.remove(KEY1));
        assertInitialMinusKey1(map);
        assertNull(map.remove(KEY1));
        assertInitialMinusKey1(map);
    }

    @Test
    public void testRemove_fromMapTwo()
    {
        final Map<String,String> map = simpleCompositeMap();
        assertEquals(VALUE2, map.remove(KEY2));
        assertInitialMinusKey2(map);
        assertNull(map.remove(KEY2));
        assertInitialMinusKey2(map);
    }

    @Test
    public void testRemove_notFound()
    {
        final Map<String,String> map = simpleCompositeMap();
        assertNull(map.remove(KEY3));
        assertInitialValues(map);
    }

    @Test
    public void testClear()
    {
        final Map<String,String> map = simpleCompositeMap();
        map.clear();
        assertEmpty(map);
    }

    @Test
    public void testClear_immutableMapOne_unsupported()
    {
        final Map<String,String> map = immutableCompositeMap();
        try
        {
            map.clear();
            shouldHaveFailed();
        }
        catch (UnsupportedOperationException uoe)
        {
            assertInitialValues(map);
        }
    }

    @Test
    public void testKeySetClear()
    {
        final Map<String,String> map = simpleCompositeMap();
        map.keySet().clear();
        assertEmpty(map);
    }

    @Test
    public void testKeySetContainsAll()
    {
        // Note: true case is heavily tested by assertMapConsistency(...), so
        // the focus here is on edge cases
        final Set<String> keySet = simpleCompositeMap().keySet();
        assertTrue(keySet.containsAll(ImmutableSet.<String>of()));
        assertTrue(keySet.containsAll(asList(KEY1)));
        assertTrue(keySet.containsAll(asList(KEY1,KEY1)));
        assertTrue(keySet.containsAll(asList(KEY2)));
        assertTrue(keySet.containsAll(asList(KEY1,KEY2)));
        assertFalse(keySet.containsAll(asList(KEY3)));
        assertFalse(keySet.containsAll(asList(KEY1,KEY2,KEY3)));
        assertFalse(keySet.containsAll(asList(KEY1,KEY3)));
        assertFalse(keySet.containsAll(asList(KEY2,KEY3)));
        keySet.remove(KEY1);
        assertFalse(keySet.containsAll(asList(KEY1)));
        assertTrue(keySet.containsAll(asList(KEY2)));
        assertFalse(keySet.containsAll(asList(KEY1,KEY2)));
    }

    @Test
    public void testKeySetRetainAll_cleared()
    {
        Map<String,String> map = simpleCompositeMap();
        assertTrue(map.keySet().retainAll(ImmutableSet.of()));
        assertEmpty(map);
        map = simpleCompositeMap();
        assertTrue(map.keySet().retainAll(ImmutableSet.of(KEY3)));
        assertEmpty(map);
    }

    @Test
    public void testKeySetRetainAll_removeFromMapOne()
    {
        final Map<String,String> map = simpleCompositeMap();
        assertTrue(map.keySet().retainAll(ImmutableSet.of(KEY2)));
        assertInitialMinusKey1(map);
    }

    @Test
    public void testKeySetRetainAll_removeFromMapTwo()
    {
        final Map<String,String> map = simpleCompositeMap();
        assertTrue(map.keySet().retainAll(ImmutableSet.of(KEY1)));
        assertInitialMinusKey2(map);
    }

    @Test
    public void testKeySetRetainAll_unchanged()
    {
        final Map<String,String> map = simpleCompositeMap();
        assertFalse(map.keySet().retainAll(ImmutableSet.of(KEY1,KEY2,KEY3)));
        assertInitialValues(map);
    }

    @Test
    public void testKeySetRemoveAll_cleared()
    {
        final Map<String,String> map = simpleCompositeMap();
        assertTrue(map.keySet().removeAll(ImmutableSet.of(KEY1, KEY2, KEY3)));
        assertEmpty(map);
    }

    @Test
    public void testKeySetRemoveAll_removeFromMapOne()
    {
        final Map<String,String> map = simpleCompositeMap();
        assertTrue(map.keySet().removeAll(ImmutableSet.of(KEY1)));
        assertInitialMinusKey1(map);
    }

    @Test
    public void testKeySetRemoveAll_removeFromMapTwo()
    {
        final Map<String,String> map = simpleCompositeMap();
        assertTrue(map.keySet().removeAll(ImmutableSet.of(KEY2)));
        assertInitialMinusKey2(map);
    }

    @Test
    public void testKeySetRemoveAll_unchanged()
    {
        Map<String,String> map = simpleCompositeMap();
        assertFalse(map.keySet().removeAll(ImmutableSet.of()));
        assertInitialValues(map);
        map = simpleCompositeMap();
        assertFalse(map.keySet().removeAll(ImmutableSet.of(KEY3)));
        assertInitialValues(map);
    }

    @Test
    public void testKeySetLiveness_removeFirst()
    {
        final Map<String,String> map = simpleCompositeMap();
        final Set<String> keys = map.keySet();
        final Iterator<String> iter = keys.iterator();
        assertTrue(iter.hasNext());
        if (KEY1.equals(iter.next()))
        {
            map.remove(KEY2);
            assertInitialMinusKey2(map);       // gone from the map
            assertFalse(keys.contains(KEY2));  // gone from the keySet
        }
        else
        {
            map.remove(KEY1);
            assertInitialMinusKey1(map);       // gone from the map
            assertFalse(keys.contains(KEY1));  // gone from the keySet
        }
        assertFalse(iter.hasNext());  // and iterator shouldn't find a next
    }

    @Test
    public void testKeySetLiveness_removeSecond()
    {
        final Map<String,String> map = simpleCompositeMap();
        final Set<String> keys = map.keySet();
        final Iterator<String> iter = keys.iterator();
        assertTrue(iter.hasNext());
        if (KEY1.equals(iter.next()))
        {
            assertTrue(iter.hasNext());
            assertEquals(KEY2, iter.next());
            map.remove(KEY2);
            assertInitialMinusKey2(map);       // gone from the map
            assertFalse(keys.contains(KEY2));  // gone from the keySet
        }
        else
        {
            assertTrue(iter.hasNext());
            assertEquals(KEY1, iter.next());
            map.remove(KEY1);
            assertInitialMinusKey1(map);       // gone from the map
            assertFalse(keys.contains(KEY1));  // gone from the keySet
        }
        assertFalse(iter.hasNext());  // and iterator shouldn't find a next
    }

    @Test
    public void testKeySetIteratorRemove()
    {
        final Map<String,String> map = simpleCompositeMap();
        final Iterator<String> iter = map.keySet().iterator();
        assertTrue(iter.hasNext());
        final String firstKey = iter.next();
        iter.remove();
        assertTrue(iter.hasNext());
        if (KEY1.equals(firstKey))
        {
            assertInitialMinusKey1(map);
            assertEquals(KEY2, iter.next());
            iter.remove();
        }
        else
        {
            assertInitialMinusKey2(map);
            assertEquals(KEY1, iter.next());
            iter.remove();
        }
        assertFalse(iter.hasNext());
        assertEmpty(map);
    }

    @Test
    public void testEntrySetClear()
    {
        final Map<String,String> map = simpleCompositeMap();
        final Set<String> keySet = map.keySet();
        final Set<Map.Entry<String,String>> entrySet = map.entrySet();
        entrySet.clear();
        assertTrue(keySet.isEmpty());
        assertTrue(entrySet.isEmpty());
        assertEmpty(map);
    }

    @Test
    public void testEntrySetIteratorLiveness()
    {
        final Map<String,String> map = simpleCompositeMap();
        final Set<String> keys = map.keySet();
        final Set<Map.Entry<String,String>> entries = map.entrySet();
        final Iterator<Map.Entry<String,String>> iter = entries.iterator();
        assertTrue(iter.hasNext());
        Map.Entry<String,String> entry = iter.next();
        if (KEY1.equals(entry.getKey()))
        {
            assertEquals(VALUE1, entry.getValue());
            map.remove(KEY2);
            assertFalse(keys.contains(KEY2));
            assertInitialMinusKey2(map);
            assertFalse(iter.hasNext());
        }
        else
        {
            assertEquals(VALUE2, entry.getValue());
            map.remove(KEY1);
            assertFalse(keys.contains(KEY1));
            assertInitialMinusKey1(map);
            assertFalse(iter.hasNext());
        }
        assertFalse(iter.hasNext());
    }

    @Test
    public void testEntrySetIteratorRemoveLiveness()
    {
        final Map<String,String> map = simpleCompositeMap();
        final Set<String> keys = map.keySet();
        final Set<Map.Entry<String,String>> entries = map.entrySet();
        final Iterator<Map.Entry<String,String>> iter = entries.iterator();
        assertTrue(iter.hasNext());
        Map.Entry<String,String> entry = iter.next();
        if (KEY1.equals(entry.getKey()))
        {
            assertEquals(VALUE1, entry.getValue());
            iter.remove();
            assertEquals(1, keys.size());
            assertFalse(keys.contains(KEY1));
            assertNull(entry.getValue());
            assertInitialMinusKey1(map);
            assertTrue(iter.hasNext());
            entry = iter.next();
            assertEquals(KEY2, entry.getKey());
            assertEquals(VALUE2, entry.getValue());
        }
        else
        {
            assertEquals(VALUE2, entry.getValue());
            iter.remove();
            assertEquals(1, keys.size());
            assertFalse(keys.contains(KEY2));
            assertNull(entry.getValue());
            assertInitialMinusKey2(map);
            assertTrue(iter.hasNext());
            entry = iter.next();
            assertEquals(KEY1, entry.getKey());
            assertEquals(VALUE1, entry.getValue());
        }
        assertFalse(iter.hasNext());
    }

    @Test
    public void testMapEntrySetValue()
    {
        final Map<String,String> map = simpleCompositeMap();
        for (Map.Entry<String,String> entry : map.entrySet())
        {
            entry.setValue("new" + entry.getValue());
        }
        assertEquals(NEW_VALUE1, map.get(KEY1));
        assertEquals(NEW_VALUE2, map.get(KEY2));
        assertNull(map.get(KEY3));
        assertMapConsistency(map, KEY1, KEY2);
        assertMapContainsValues(map, NEW_VALUE1, NEW_VALUE2);
    }

    @Test
    public void testMapEntrySetValue_immutableMapOne_unsupported()
    {
        final Map<String,String> map = immutableCompositeMap();
        try
        {
            map.entrySet().iterator().next().setValue(VALUE3);
        }
        catch (UnsupportedOperationException uoe)
        {
            assertInitialValues(map);
        }
    }

    @Test
    public void testValues_multipleKeysHaveSameValue()
    {
        final Map<String,String> map = simpleCompositeMap();
        map.put(KEY2, VALUE1);
        map.put(KEY3, VALUE1);
        assertMapConsistency(map, KEY1, KEY2, KEY3);
        assertMapContainsValues(map, VALUE1);
        assertMapDoesNotContainValues(map, VALUE2, VALUE3);
        assertEquals(VALUE1, map.get(KEY1));
        assertEquals(VALUE1, map.get(KEY2));
        assertEquals(VALUE1, map.get(KEY3));
        final Collection<String> values = map.values();
        assertEquals(3, values.size());
        for (String value : values)
        {
            assertEquals(VALUE1, value);
        }
    }

    private static <K,V> TestMapEntry<K,V> entry(K key, V value)
    {
        return new TestMapEntry<K,V>(key, value);
    }

    static class TestMapEntry<K,V> implements Map.Entry<K,V>
    {
        private final K key;
        private final V value;

        TestMapEntry(K key, V value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey()
        {
            return key;
        }

        @Override
        public V getValue()
        {
            return value;
        }

        @Override
        public V setValue(V value)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public int hashCode()
        {
            return ((key != null) ? key.hashCode() : 0) ^
                   ((value != null) ? value.hashCode() : 0);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (! (obj instanceof Map.Entry))
            {
                return false;
            }
            final Map.Entry<?,?> other = (Map.Entry<?,?>)obj;
            return ((key != null) ? key.equals(other.getKey()) : (other.getKey() == null)) &&
                   ((value != null) ? value.equals(other.getValue()) : (other.getValue() == null));
        }
    }
}
