package com.atlassian.query.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.query.order.OrderByImpl}.
 *
 * @since v4.0
 */
public class TestOrderByImpl
{
    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new OrderByImpl((Collection<SearchSort>)null);
            fail("Expected an error");
        }
        catch (IllegalArgumentException e) { }

        try
        {
            new OrderByImpl((SearchSort[])null);
            fail("Expected an error");
        }
        catch (IllegalArgumentException e) { }

        try
        {
            new OrderByImpl(new SearchSort("dumpper", SortOrder.DESC), null);
            fail("Expected an error");
        }
        catch (IllegalArgumentException e) { }

        try
        {
            new OrderByImpl(CollectionBuilder.newBuilder(new SearchSort("dump", SortOrder.ASC), null).asList());
            fail("Expected an error.");
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testGetSorts() throws Exception
    {
        List<SearchSort> inputSorts = CollectionBuilder.newBuilder(new SearchSort("one"), new SearchSort("two")).asMutableList();
        List<SearchSort> expectedSorts = new ArrayList<SearchSort>(inputSorts);

        OrderByImpl impl = new OrderByImpl(inputSorts);
        inputSorts.clear();

        assertEquals(expectedSorts, impl.getSearchSorts());
    }
}
