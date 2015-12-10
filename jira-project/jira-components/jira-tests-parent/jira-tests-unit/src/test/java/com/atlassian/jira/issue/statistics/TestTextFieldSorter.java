/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestTextFieldSorter
{
    @Test
    public void testEquals()
    {
        TextFieldSorter sorter = new TextFieldSorter("abc");
        assertEquals(sorter, sorter);
        assertEquals(sorter.hashCode(), sorter.hashCode());
        TextFieldSorter sorter2 = new TextFieldSorter("abc");
        assertEquals(sorter, sorter2);
        assertEquals(sorter.hashCode(), sorter2.hashCode());

        assertFalse(sorter.equals(null));
        assertFalse(sorter.equals(new TextFieldSorter(null)));
        assertFalse(sorter.hashCode() == new TextFieldSorter(null).hashCode());
        assertFalse(sorter.equals(new TextFieldSorter("a")));
        assertFalse(sorter.hashCode() == new TextFieldSorter("a").hashCode());
        assertFalse(sorter.equals(new Object()));
        assertFalse(sorter.hashCode() == new Object().hashCode());
        assertFalse(sorter.equals(new IssueKeyStatisticsMapper()));
        assertFalse(sorter.hashCode() == new IssueKeyStatisticsMapper().hashCode());
    }
}
