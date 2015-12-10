/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestLongFieldStatisticsMapper
{
    @Test
    public void testEquals()
    {
        String documentConstant = "abc";
        LongFieldStatisticsMapper sorter = new LongFieldStatisticsMapper(documentConstant);
        assertEquals(sorter, sorter);
        assertEquals(sorter.hashCode(), sorter.hashCode());

        // Ensure we have a new string object
        LongFieldStatisticsMapper sorter2 = new LongFieldStatisticsMapper(new String("abc"));
        assertEquals(sorter, sorter2);
        assertEquals(sorter.hashCode(), sorter2.hashCode());

        assertFalse(sorter.equals(new LongFieldStatisticsMapper("def")));
        assertFalse(sorter.equals(null));
        assertFalse(sorter.equals(new Object()));
        assertFalse(sorter.equals(new LongFieldStatisticsMapper(null)));
        // Ensure a different sorter with the same document constant is not equal
        assertFalse(sorter.equals(new TextFieldSorter(documentConstant)));
        assertFalse(sorter.equals(new IssueKeyStatisticsMapper()));

        // Unless it's a TimeTrackingStatisticsMapper
        assertTrue(sorter.equals(new TimeTrackingStatisticsMapper(documentConstant)));
    }
}
