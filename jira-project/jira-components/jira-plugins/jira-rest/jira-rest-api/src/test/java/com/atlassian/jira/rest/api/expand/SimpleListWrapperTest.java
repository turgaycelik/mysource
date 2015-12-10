package com.atlassian.jira.rest.api.expand;

import com.atlassian.plugins.rest.common.expand.entity.ListWrapperCallback;
import com.atlassian.plugins.rest.common.expand.parameter.Indexes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SLWrapper.
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleListWrapperTest
{
    @Mock private Indexes indexes;

    @Test
    public void testListWrapperWithNullList()
    {
        try
        {
            SimpleListWrapper.ofList(null, 10);
            fail("Expected NullPointerException");
        }
        catch (NullPointerException e)
        {
            // success
        }
    }

    @Test
    public void testListWrapperWithNegativeMaxResults()
    {
        List<Integer> list = Arrays.asList(1, 2);
        when(indexes.getIndexes(list.size())).thenReturn(new TreeSet<Integer>(list));

        try
        {
            SimpleListWrapper.ofList(list, -1);
            fail("Expected an IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
    }

    @Test
    public void testListWrapperWithZeroMaxResults()
    {
        List<Integer> list = Arrays.asList(1, 2);
        when(indexes.getIndexes(list.size())).thenReturn(new TreeSet<Integer>(list));

        ListWrapperCallback<Integer> cb = SimpleListWrapper.ofList(list, 0);
        List<Integer> items = cb.getItems(indexes);
        assertEquals(0, items.size());
    }
}
