/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestReporterStatisticsMapper
{
    @Test
    public void testEquals()
    {
        ReporterStatisticsMapper sorter = new ReporterStatisticsMapper(null, null);
        assertEquals(sorter, sorter);
        assertEquals(sorter.hashCode(), sorter.hashCode());

        ReporterStatisticsMapper sorter2 = new ReporterStatisticsMapper(null, null);
        assertEquals(sorter, sorter2);
        assertEquals(sorter.hashCode(), sorter2.hashCode());

        assertFalse(sorter.equals(null));
        assertFalse(sorter.equals(new Object()));
        assertFalse(sorter.equals(new IssueKeyStatisticsMapper()));
        // Should never be equal to the Reporter Statistics mapper
        assertFalse(sorter.equals(new AssigneeStatisticsMapper(null, null)));
    }
}
