package com.atlassian.jira.util.system.check;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestJvmVersionUtil
{
    @Test
    public void testGetMajorVersion() throws Exception
    {
        final JvmVersionUtil util = new JvmVersionUtil();
        assertEquals(6, util.getMajorVersion("1.6.0"));
        assertEquals(6, util.getMajorVersion("1.6.0-15"));
        assertEquals(6, util.getMajorVersion("1.6.0-15_b03"));

        assertEquals(5, util.getMajorVersion("1.5.0"));
        assertEquals(5, util.getMajorVersion("1.5.0-15"));
        assertEquals(5, util.getMajorVersion("1.5.0-15_b03"));

        assertEquals(-1, util.getMajorVersion("5"));
        assertEquals(-1, util.getMajorVersion("1.monkey.0"));
    }

    @Test
    public void testGetMinorVersion() throws Exception
    {
        final JvmVersionUtil util = new JvmVersionUtil();
        assertEquals(15, util.getMinorVersion("1.6.0_15"));
        assertEquals(15, util.getMinorVersion("1.6.0_15-b03"));
        assertEquals(15, util.getMinorVersion("1.5.0_15-b03"));

        assertEquals(14, util.getMinorVersion("1.6.0_14"));
        assertEquals(14, util.getMinorVersion("1.6.0_14-b03"));
        assertEquals(14, util.getMinorVersion("1.5.0_14-b03"));

        assertEquals(0, util.getMinorVersion("1.6.0"));
        assertEquals(0, util.getMinorVersion("1.6.0-10"));
        assertEquals(-1, util.getMinorVersion("1.6.0_monkey-b4"));
        assertEquals(-1, util.getMinorVersion("1.6.0_monkey"));
    }
    
    @Test
    public void testGetBuildNumber() throws Exception
    {
        final JvmVersionUtil util = new JvmVersionUtil();
        assertEquals(0, util.getBuildNumber("1.6.0"));
        assertEquals(0, util.getBuildNumber("1.6.0_15"));
        assertEquals(3, util.getBuildNumber("1.6.0_15-b03"));
        assertEquals(3, util.getBuildNumber("1.5.0_15-b03"));

        assertEquals(12, util.getBuildNumber("1.6.0_15-b12"));
        assertEquals(12, util.getBuildNumber("1.5.0_15-b12"));


        assertEquals(-1, util.getBuildNumber("1.6.15-bMonkey"));
        assertEquals(-1, util.getBuildNumber("1.6.15-b"));
    }

}
