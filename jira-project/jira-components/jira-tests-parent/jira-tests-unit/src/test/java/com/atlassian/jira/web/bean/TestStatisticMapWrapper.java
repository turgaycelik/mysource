/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestStatisticMapWrapper
{   
    /**
     * test that getPercentage on the highest percentage value returns the same number that was passed in
     */
    @Test
    public void testGetPercentage()
    {
        int totalCount = 10;
        StatisticMapWrapper<String,Long> smw = new StatisticMapWrapper<String,Long>((Map<String,Long>)EasyMap.build(null, new Long(9), "other", new Long(1)), totalCount, 0);
        assertEquals(90, smw.getPercentage(null));
        assertEquals(totalCount, smw.getTotalCount());
    }

    @Test
    public void testGetIrrelevantPercentage()
    {
        int totalCount = 8;
        StatisticMapWrapper<String,Long> smw = new StatisticMapWrapper<String,Long>((Map<String,Long>)EasyMap.build(null, new Long(7), "other", new Long(1)), totalCount, 2);
        assertEquals(25, smw.getIrrelevantPercentage());
        assertEquals(totalCount, smw.getTotalCount());
    }

    @Test
    public void testGetIrrelevantCount()
    {
        int totalCount = 8;
        StatisticMapWrapper<String,Long> smw = new StatisticMapWrapper<String,Long>((Map<String,Long>)EasyMap.build(null, new Long(7), "other", new Long(1)), totalCount, 2);
        assertEquals(2, smw.getIrrelevantCount());
        assertEquals(totalCount, smw.getTotalCount());
    }
    
    @Test
    public void testGetLargestPercentage()
    {
        int totalCount = 100;
        StatisticMapWrapper<Long,Long> smw = new StatisticMapWrapper<Long,Long>((Map<Long,Long>)EasyMap.build(new Long(1), new Long(1), new Long(2), new Long(99)), totalCount, 0);
        assertEquals(99, smw.getLargestPercentage());
        assertEquals(totalCount, smw.getTotalCount());
    }

    @Test
    public void testGetLargestPercentageWithDuplicate()
    {
        int totalCount = 100;
        StatisticMapWrapper<Long,Long> smw = new StatisticMapWrapper<Long,Long>((Map<Long,Long>)EasyMap.build(new Long(1), new Long(20), new Long(2), new Long(40), new Long(3), new Long(40)), totalCount, 0);
        assertEquals(40, smw.getLargestPercentage());
        assertEquals(totalCount, smw.getTotalCount());
    }

    @Test
    public void testGetNullKeyValueWithNoNullEntry()
    {
        int totalCount = 60;
        StatisticMapWrapper<Long,Long> smw = new StatisticMapWrapper<Long,Long>((Map<Long,Long>)EasyMap.build(new Long(1), new Long(20), new Long(2), new Long(40)), totalCount, 0);
        assertEquals(-1, smw.getNullKeyValue());
        assertEquals(totalCount, smw.getTotalCount());
    }

    @Test
    public void testGetNullKeyValueWithNullEntry()
    {
        int totalCount = 60;
        StatisticMapWrapper<Long,Long> smw = new StatisticMapWrapper<Long,Long>((Map<Long,Long>)EasyMap.build(null, new Long(20), new Long(2), new Long(40)), totalCount, 0);
        assertEquals(20, smw.getNullKeyValue());
        assertEquals(totalCount, smw.getTotalCount());
    }
}
