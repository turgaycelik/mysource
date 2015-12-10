/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestNumericFieldStatisticsMapper
{
    @Test
    public void testEquals()
    {
        String documentConstant = "abc";
        NumericFieldStatisticsMapper sorter = new NumericFieldStatisticsMapper(documentConstant);
        assertEquals(sorter, sorter);
        assertEquals(sorter.hashCode(), sorter.hashCode());

        // Ensure we have a new string object
        NumericFieldStatisticsMapper sorter2 = new NumericFieldStatisticsMapper(new String("abc"));
        assertEquals(sorter, sorter2);
        assertEquals(sorter.hashCode(), sorter2.hashCode());

        assertFalse(sorter.equals(new NumericFieldStatisticsMapper("def")));
        assertFalse(sorter.equals(null));
        assertFalse(sorter.equals(new Object()));
        assertFalse(sorter.equals(new NumericFieldStatisticsMapper(null)));
        // Ensure a different sorter with the same document constant is not equal
        assertFalse(sorter.equals(new TextFieldSorter(documentConstant)));
        assertFalse(sorter.equals(new IssueKeyStatisticsMapper()));
    }
}
