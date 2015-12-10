/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.worklog;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;

public class TestWorkRatio
{
    GenericValue issue1 = new MockGenericValue("issue", EasyMap.build("timeoriginalestimate", new Long(10), "timespent", new Long(5)));
    GenericValue issue2 = new MockGenericValue("issue", EasyMap.build("timeoriginalestimate", new Long(20), "timespent", new Long(40)));
    GenericValue issue3 = new MockGenericValue("issue");
    GenericValue issue4 = new MockGenericValue("issue", EasyMap.build("timeoriginalestimate", new Long(12), "timespent", new Long(8)));

    @Test
    public void testGetWorkRatio()
    {
        assertEquals(50, WorkRatio.getWorkRatio(issue1));
        assertEquals(200, WorkRatio.getWorkRatio(issue2));
        assertEquals(-1, WorkRatio.getWorkRatio(issue3));
        assertEquals(66, WorkRatio.getWorkRatio(issue4));
    }

    @Test
    public void testGetPaddedWorkRatio()
    {
        assertEquals("00050", WorkRatio.getPaddedWorkRatio(issue1));
        assertEquals("00200", WorkRatio.getPaddedWorkRatio(issue2));
        assertEquals("-1", WorkRatio.getPaddedWorkRatio(issue3));
        assertEquals("00066", WorkRatio.getPaddedWorkRatio(issue4));
    }
}
