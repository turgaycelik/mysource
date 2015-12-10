package com.atlassian.jira.util.ofbiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @since v3.13
 */
public class TestGenericValueUtils
{
    @Test
    public void testTransformToLongIds() throws Exception
    {
        // null
        assertNull(GenericValueUtils.transformToLongIds(null));
        // empty Collection
        Long[] longs = GenericValueUtils.transformToLongIds(Collections.<GenericValue>emptyList());
        assertTrue(longs.length == 0);
        // now a list that includes a null value
        longs = GenericValueUtils.transformToLongIds(CollectionBuilder.newBuilder(createGenericValue(5L), null, createGenericValue(2L)).asList());
        assertEquals(3, longs.length);
        assertEquals(5L, longs[0].longValue());
        // null GenericValue gets an ID of -1
        assertEquals(-1L, longs[1].longValue());
        assertEquals(2L, longs[2].longValue());
    }

    @Test
    public void testTransformToLongIdsList() throws Exception
    {
        // null
        assertNull(GenericValueUtils.transformToLongIdsList(null));
        // empty Collection
        List<Long> longs = GenericValueUtils.transformToLongIdsList(Collections.<GenericValue>emptyList());
        assertTrue(longs.size() == 0);
        // now a list that includes a null value
        longs = GenericValueUtils.transformToLongIdsList(CollectionBuilder.newBuilder(createGenericValue(5L), null, createGenericValue(2L)).asList());
        assertEquals(3, longs.size());
        assertEquals(5, longs.get(0).longValue());
        // null GenericValue gets an ID of -1
        assertEquals(-1, longs.get(1).longValue());
        assertEquals(2, longs.get(2).longValue());
    }

    @Test
    public void testTransformToStrings() throws Exception
    {
        // null
        assertNull(GenericValueUtils.transformToStrings(null, "fred"));
        // empty Collection
        String[] strings = GenericValueUtils.transformToStrings(Collections.<GenericValue>emptyList(), "fred");
        assertTrue(strings.length == 0);
        // now a list that includes a null value
        strings = GenericValueUtils.transformToStrings(CollectionBuilder.newBuilder(createGenericValue("fred", "dead"), null, createGenericValue("fred", "red")).asList(), "fred");
        assertEquals(3, strings.length);
        assertEquals("dead", strings[0]);
        // null GenericValue gets an ID of -1
        assertEquals("-1", strings[1]);
        assertEquals("red", strings[2]);
    }

    @Test
    public void testTransformToStringIds() throws Exception
    {
        // null
        assertNull(GenericValueUtils.transformToStringIds(null));
        // empty Collection
        String[] strings = GenericValueUtils.transformToStringIds(Collections.<GenericValue>emptyList());
        assertTrue(strings.length == 0);
        // now a list that includes a null value
        strings = GenericValueUtils.transformToStringIds(CollectionBuilder.newBuilder(createGenericValue("5"), null, createGenericValue("2")).asList());
        assertEquals(3, strings.length);
        assertEquals("5", strings[0]);
        // null GenericValue gets an ID of -1
        assertEquals("-1", strings[1]);
        assertEquals("2", strings[2]);
    }

    @Test
    public void testTransformToStringIdsList() throws Exception
    {
        // null
        assertNull(GenericValueUtils.transformToStringIdsList(null));
        // empty Collection
        List<String> strings = GenericValueUtils.transformToStringIdsList(Collections.<GenericValue>emptyList());
        assertTrue(strings.size() == 0);
        // now a list that includes a null value
        strings = GenericValueUtils.transformToStringIdsList(CollectionBuilder.newBuilder(createGenericValue("5"), null, createGenericValue("2")).asList());
        assertEquals(3, strings.size());
        assertEquals("5", strings.get(0));
        // null GenericValue gets an ID of -1
        assertEquals("-1", strings.get(1));
        assertEquals("2", strings.get(2));
    }

    @Test
    public void testGetCommaSeparatedList()
    {
        assertNull(GenericValueUtils.getCommaSeparatedList(null, "hi"));
        List<GenericValue> genericValues = new ArrayList<GenericValue>();
        // test empty list
        assertEquals("", GenericValueUtils.getCommaSeparatedList(genericValues, "hi"));
        // Single value
        MockGenericValue gv = new MockGenericValue("Stephen", EasyMap.build("colour", "red", "flavour", "cherry"));
        genericValues.add(gv);
        assertEquals("red", GenericValueUtils.getCommaSeparatedList(genericValues, "colour"));
        assertEquals("cherry", GenericValueUtils.getCommaSeparatedList(genericValues, "flavour"));
        // more values
        gv = new MockGenericValue("Stephen", EasyMap.build("colour", "black", "flavour", "blackberry"));
        genericValues.add(gv);
        assertEquals("red, black", GenericValueUtils.getCommaSeparatedList(genericValues, "colour"));
        assertEquals("cherry, blackberry", GenericValueUtils.getCommaSeparatedList(genericValues, "flavour"));
    }

    @Test
    public void testGetCommaSeparatedListInvalidParams()
    {
        List<GenericValue> genericValues = new ArrayList<GenericValue>();
        try
        {
            assertEquals("", GenericValueUtils.getCommaSeparatedList(genericValues, null));
            fail("Should have thrown Exception");
        }
        catch (IllegalArgumentException ex)
        {
            // Expected
        }

        try
        {
            assertEquals("", GenericValueUtils.getCommaSeparatedList(genericValues, ""));
            fail("Should have thrown Exception");
        }
        catch (IllegalArgumentException ex)
        {
            // Expected
        }
    }


    private GenericValue createGenericValue(final String id)
    {
        MockGenericValue gv = new MockGenericValue("Test");
        gv.set("id", id);
        return gv;
    }

    private GenericValue createGenericValue(final String key, String value)
    {
        MockGenericValue gv = new MockGenericValue("Test");
        gv.set(key, value);
        return gv;
    }

    private GenericValue createGenericValue(final Long id)
    {
        MockGenericValue gv = new MockGenericValue("Test");
        gv.set("id", id);
        return gv;
    }
}
