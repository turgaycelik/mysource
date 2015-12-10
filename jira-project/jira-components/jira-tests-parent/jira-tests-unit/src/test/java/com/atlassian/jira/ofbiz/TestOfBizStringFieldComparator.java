package com.atlassian.jira.ofbiz;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;

public class TestOfBizStringFieldComparator
{
    @Test
    public void testNullGVs() throws Exception
    {
        OfBizStringFieldComparator comparator = new OfBizStringFieldComparator("foo");
        GenericValue gv = new MockGenericValue("dude");
        
        assertEquals(0, comparator.compare(null, null));
        assertEquals(-1, comparator.compare(gv, null));
        assertEquals(1, comparator.compare(null, gv));
    }

    @Test
    public void testNullFieldValues() throws Exception
    {
        OfBizStringFieldComparator comparator = new OfBizStringFieldComparator("foo");
        GenericValue gv1 = new MockGenericValue("Project", FieldMap.build("foo", null));
        GenericValue gv2 = new MockGenericValue("Project", FieldMap.build("foo", null).add("bar", "tweak"));
        GenericValue gv3 = new MockGenericValue("Project", FieldMap.build("foo", "bar"));

        assertEquals(0, comparator.compare(gv1, gv2));
        assertEquals(0, comparator.compare(gv2, gv1));
        assertEquals(-1, comparator.compare(gv3, gv2));
        assertEquals(1, comparator.compare(gv2, gv3));
        assertEquals(-1, comparator.compare(gv1, null));
        assertEquals(1, comparator.compare(null, gv1));
    }

    @Test
    public void testSimple()
    {
        OfBizStringFieldComparator comparator = new OfBizStringFieldComparator("foo");
        GenericValue gv1 = new MockGenericValue("Project", FieldMap.build("foo", "Bat"));
        GenericValue gv2 = new MockGenericValue("Project", FieldMap.build("foo", "Cat"));
        GenericValue gv3 = new MockGenericValue("Project", FieldMap.build("foo", "Cat"));

        assertEquals(0, comparator.compare(gv2, gv3));
        assertEquals(0, comparator.compare(gv3, gv2));
        assertEquals(-1, comparator.compare(gv1, gv2));
        assertEquals(1, comparator.compare(gv2, gv1));
    }

    @Test
    public void testCaseInsensitive()
    {
        OfBizStringFieldComparator comparator = new OfBizStringFieldComparator("foo");
        GenericValue gv1 = new MockGenericValue("Project", FieldMap.build("foo", "bat"));
        GenericValue gv2 = new MockGenericValue("Project", FieldMap.build("foo", "Bat"));
        GenericValue gv3 = new MockGenericValue("Project", FieldMap.build("foo", "cat"));
        GenericValue gv4 = new MockGenericValue("Project", FieldMap.build("foo", "Cat"));

        assertEquals(0, comparator.compare(gv1, gv2));
        assertEquals(0, comparator.compare(gv2, gv1));
        assertEquals(0, comparator.compare(gv3, gv4));
        assertEquals(0, comparator.compare(gv4, gv3));
        assertEquals(-1, comparator.compare(gv1, gv3));
        assertEquals(-1, comparator.compare(gv1, gv4));
        assertEquals(-1, comparator.compare(gv2, gv3));
        assertEquals(-1, comparator.compare(gv2, gv4));
        assertEquals(1, comparator.compare(gv3, gv1));
        assertEquals(1, comparator.compare(gv4, gv1));
        assertEquals(1, comparator.compare(gv3, gv2));
        assertEquals(1, comparator.compare(gv4, gv2));
    }

    @Test
    public void testOrder() throws Exception
    {
        OfBizStringFieldComparator comparator = new OfBizStringFieldComparator("foo");

        GenericValue gvBat = new MockGenericValue("Project", FieldMap.build("foo", "bat"));
        GenericValue gvCat = new MockGenericValue("Project", FieldMap.build("foo", "cat"));
        GenericValue gvFat = new MockGenericValue("Project", FieldMap.build("foo", "fat"));
        GenericValue gvNull = new MockGenericValue("Project", FieldMap.build("foo", null));

        List<GenericValue> list = Arrays.asList(gvFat, null, gvBat, gvNull, gvCat);
        Collections.sort(list, comparator);

        assertEquals(gvBat, list.get(0));
        assertEquals(gvCat, list.get(1));
        assertEquals(gvFat, list.get(2));
        assertEquals(gvNull, list.get(3));
        assertEquals(null, list.get(4));
    }
}
