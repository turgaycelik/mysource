package com.atlassian.jira.issue.index.indexers.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestFieldIndexerUtil
{
    @Test
    public void testGetValueForSortingTrims() throws Exception
    {
        assertEquals("test", FieldIndexerUtil.getValueForSorting("   test  "));
    }

    @Test
    public void testGetValueForSortingTrimsLarge() throws Exception
    {
        assertEquals(
            "test",
            FieldIndexerUtil.getValueForSorting("                                                               test                                                           "));
    }

    @Test
    public void testGetValueForSortingLarge() throws Exception
    {
        assertEquals(
            50,
            FieldIndexerUtil.getValueForSorting(
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789").length());
    }

    @Test
    public void testGetValueForSortingNull() throws Exception
    {
        assertEquals(String.valueOf('\ufffd'), FieldIndexerUtil.getValueForSorting(null));
    }

}
