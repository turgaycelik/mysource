package com.atlassian.jira.ofbiz;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;

public class TestOfBizDateFieldComparator
{
    @Test
    public void testNullGVs() throws Exception
    {
        OfBizDateFieldComparator comparator = new OfBizDateFieldComparator("foo");
        GenericValue gv = new MockGenericValue("dude");
        
        assertEquals(0, comparator.compare(null, null));
        assertEquals(-1, comparator.compare(gv, null));
        assertEquals(1, comparator.compare(null, gv));
    }

    @Test
    public void testNullFieldValues() throws Exception
    {
        OfBizDateFieldComparator comparator = new OfBizDateFieldComparator("foo");
        GenericValue gv1 = new MockGenericValue("Project", FieldMap.build("foo", null));
        GenericValue gv2 = new MockGenericValue("Project", FieldMap.build("foo", null).add("bar", "tweak"));
        GenericValue gv3 = new MockGenericValue("Project", FieldMap.build("foo", new Timestamp(0)));

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
        OfBizDateFieldComparator comparator = new OfBizDateFieldComparator("foo");

        GenericValue gv1 = new MockGenericValue("Project", FieldMap.build("foo", new Timestamp(1000000000000L)));
        GenericValue gv2 = new MockGenericValue("Project", FieldMap.build("foo", new Timestamp(1285896204696L)));
        GenericValue gv3 = new MockGenericValue("Project", FieldMap.build("foo", new Timestamp(1285896204696L)));

        assertEquals(0, comparator.compare(gv2, gv3));
        assertEquals(0, comparator.compare(gv3, gv2));
        assertEquals(-1, comparator.compare(gv1, gv2));
        assertEquals(1, comparator.compare(gv2, gv1));
    }

    @Test
    public void testOrder() throws Exception
    {
        OfBizDateFieldComparator comparator = new OfBizDateFieldComparator("foo");

        GenericValue gv1 = new MockGenericValue("Project", FieldMap.build("foo", new Timestamp(1185896204696L)));
        GenericValue gv2 = new MockGenericValue("Project", FieldMap.build("foo", new Timestamp(1285896204696L)));
        GenericValue gv3 = new MockGenericValue("Project", FieldMap.build("foo", new Timestamp(1385896204696L)));
        GenericValue gvNull = new MockGenericValue("Project", FieldMap.build("foo", null));

        List<GenericValue> list = Arrays.asList(gv3, null, gv1, gvNull, gv2);
        Collections.sort(list, comparator);

        assertEquals(gv1, list.get(0));
        assertEquals(gv2, list.get(1));
        assertEquals(gv3, list.get(2));
        assertEquals(gvNull, list.get(3));
        assertEquals(null, list.get(4));
    }
}
