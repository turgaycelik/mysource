package com.atlassian.jira.imports.project.taskprogress;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v3.13
 */
public class TestTaskProgressInterval
{
    @Test
    public void testGetSubInterval() throws Exception
    {
        TaskProgressInterval taskProgressInterval = new TaskProgressInterval(null, 20, 60);

        TaskProgressInterval subInterval = taskProgressInterval.getSubInterval(0, 25);
        assertEquals(20, subInterval.getStartPercent());
        assertEquals(30, subInterval.getEndPercent());
                
        subInterval = taskProgressInterval.getSubInterval(50, 100);
        assertEquals(40, subInterval.getStartPercent());
        assertEquals(60, subInterval.getEndPercent());
    }
}
