package com.atlassian.jira.web.action.filter;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.web.action.filter.TestFilterViewTabs}.
 *
 * @since v3.13
 */
public class TestFilterViewTabs
{
    private static final FilterViewTabs.Tab TEST_TAB = new FilterViewTabs.Tab("testTab");

    @Test
    public void testTabNameEquals() throws Exception
    {
        assertFalse(TEST_TAB.nameEquals("myTab"));
        assertTrue(TEST_TAB.nameEquals("testTab"));
    }

    @Test
    public void testGetTab()
    {
        FilterViewTabs filterViewTabs = new FilterViewTabs(ImmutableList.of(FilterViewTabs.SEARCH, FilterViewTabs.PROJECT), TEST_TAB);

        //check for null argument. Should return null.
        assertNull(filterViewTabs.getTab(null));

        //empty string should return null argument.
        assertNull(filterViewTabs.getTab(""));

        //project should be returned.
        assertEquals(FilterViewTabs.PROJECT, filterViewTabs.getTab(FilterViewTabs.PROJECT.getName()));

        //search should be returned.
        assertEquals(FilterViewTabs.SEARCH, filterViewTabs.getTab(FilterViewTabs.SEARCH.getName()));

        //my should still return null.
        assertNull(filterViewTabs.getTab(FilterViewTabs.MY.getName()));
    }

    @Test
    public void testGetTabSafely()
    {
        FilterViewTabs filterViewTabs = new FilterViewTabs(ImmutableList.of(FilterViewTabs.SEARCH, FilterViewTabs.PROJECT), TEST_TAB);

        //check for null argument. Should return the default.
        assertEquals(TEST_TAB, filterViewTabs.getTabSafely(null));

        //empty string should return default argument.
        assertEquals(TEST_TAB, filterViewTabs.getTabSafely(""));

        //project should be returned.
        assertEquals(FilterViewTabs.PROJECT, filterViewTabs.getTabSafely(FilterViewTabs.PROJECT.getName()));

        //search should be returned.
        assertEquals(FilterViewTabs.SEARCH, filterViewTabs.getTabSafely(FilterViewTabs.SEARCH.getName()));

        //my should return default.
        assertEquals(TEST_TAB, filterViewTabs.getTabSafely(FilterViewTabs.MY.getName()));
    }

    @Test
    public void testIsValid()
    {
        FilterViewTabs filterViewTabs = new FilterViewTabs(ImmutableList.of(FilterViewTabs.POPULAR, FilterViewTabs.PROJECT), TEST_TAB);

        //check for null argument. Should return false.
        assertFalse(filterViewTabs.isValid(null));

        //empty string should return false.
        assertFalse(filterViewTabs.isValid(""));

        //project should return true.
        assertTrue(filterViewTabs.isValid(FilterViewTabs.POPULAR.getName()));

        //search should return true.
        assertTrue(filterViewTabs.isValid(FilterViewTabs.PROJECT.getName()));

        //the default tab should be false.
        assertFalse(filterViewTabs.isValid(TEST_TAB.getName()));
    }
}
