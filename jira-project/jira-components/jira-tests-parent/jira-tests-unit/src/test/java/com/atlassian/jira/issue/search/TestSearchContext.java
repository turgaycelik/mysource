package com.atlassian.jira.issue.search;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Tests for the SearchContext.
 */
public class TestSearchContext
{
    private SearchContext searchContext1;
    private SearchContext searchContext2;

    @Before
    public void setUp()
    {
        new MockComponentWorker().init();
        searchContext1 = new SearchContextImpl();
        searchContext2 = new SearchContextImpl();
    }

    @After
    public void tearDownWorker()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testEquals()
    {
        assertTrue("sc1 equal to sc2", searchContext1.equals(searchContext2));
        assertTrue("sc2 equal to sc1", searchContext2.equals(searchContext1));
        assertTrue("sc1 equal to sc1", searchContext1.equals(searchContext1));
        assertTrue("sc2 equal to sc2", searchContext2.equals(searchContext2));
    }

    @Test
    public void testHashCode()
    {
        assertEquals("sc1.hashcode equal to sc2.hashcode", searchContext1.hashCode(), searchContext2.hashCode());
    }
}
