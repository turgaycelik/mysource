/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestDateFieldSorter
{
    @Test
    public void testEquals()
    {
        DateFieldSorter sorter = new DateFieldSorter("abc");
        assertEquals(sorter, sorter);
        assertEquals(sorter.hashCode(), sorter.hashCode());
        DateFieldSorter sorter2 = new DateFieldSorter("abc");
        assertEquals(sorter, sorter2);
        assertEquals(sorter.hashCode(), sorter2.hashCode());

        assertFalse(sorter.equals(null));
        assertFalse(sorter.equals(new DateFieldSorter(null)));
        assertFalse(sorter.equals(new TextFieldSorter("abc")));
        assertFalse(sorter.equals(new DateFieldSorter("a")));
        assertFalse(sorter.equals(new Object()));
        assertFalse(sorter.equals(new IssueKeyStatisticsMapper()));
    }
}
