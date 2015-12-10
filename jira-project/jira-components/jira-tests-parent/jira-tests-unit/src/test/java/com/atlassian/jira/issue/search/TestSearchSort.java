package com.atlassian.jira.issue.search;

import com.atlassian.fugue.Option;
import com.atlassian.query.clause.Property;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;

import com.google.common.collect.ImmutableList;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

public class TestSearchSort
{
    @Test
    public void testConstructorBad() throws Exception
    {
        try
        {
            new SearchSort("", (String)null);
        }
        catch (IllegalArgumentException e) { }

        try
        {
            new SearchSort(null, SortOrder.DESC);
        }
        catch (IllegalArgumentException e) { }

        try
        {
            new SearchSort("something", (SortOrder) null);
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testStringConstructor()
    {
        SearchSort sort = new SearchSort("foo", "bar");
        assertEquals("bar", sort.getField());
        assertEquals(SortOrder.ASC.name(), sort.getOrder());
    }
    @Test
    public void testToStringWithProperty(){
        SearchSort sort = new SearchSort("field",
                Option.some(new Property(ImmutableList.of("k1","k2"),ImmutableList.of("p1","p2"))),SortOrder.ASC);

        assertThat(sort.toString(), Matchers.equalTo("field[k1.k2].p1.p2 ASC"));
    }
    @Test
    public void testSearchSortParseString() throws Exception
    {
        assertSame(null, SortOrder.parseString(null));
        assertSame(null, SortOrder.parseString(""));
        
        assertSame(SortOrder.DESC, SortOrder.parseString("deSC"));
        assertSame(SortOrder.ASC, SortOrder.parseString("ASC"));
        assertSame(SortOrder.ASC, SortOrder.parseString("BLAHH"));
    }
}
