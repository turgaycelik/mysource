/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.statistics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestCreatorStatisticsMapper
{
    @Test
    public void testEquals()
    {
        CreatorStatisticsMapper sorter = new CreatorStatisticsMapper(null, null);
        assertEquals(sorter, sorter);
        assertEquals(sorter.hashCode(), sorter.hashCode());

        CreatorStatisticsMapper sorter2 = new CreatorStatisticsMapper(null, null);
        assertEquals(sorter, sorter2);
        assertEquals(sorter.hashCode(), sorter2.hashCode());

        assertFalse(sorter.equals(null));
        assertFalse(sorter.equals(new Object()));
        assertFalse(sorter.equals(new IssueKeyStatisticsMapper()));
        // Should never be equal to the Assignee Statistics mapper
        assertFalse(sorter.equals(new AssigneeStatisticsMapper(null, null)));
    }
}
