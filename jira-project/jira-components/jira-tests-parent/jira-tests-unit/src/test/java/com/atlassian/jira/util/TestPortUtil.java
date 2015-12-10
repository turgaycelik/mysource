package com.atlassian.jira.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link PortUtil}.
 *
 * @since v4.0
 */
public class TestPortUtil
{
    @Test
    public void testValidatePortString() throws Exception
    {
        assertFalse(PortUtil.isValidPort("-1"));
        for (int i = 0; i <= 0xffff; i++)
        {
            assertTrue(PortUtil.isValidPort(String.valueOf(i)));
        }
        assertFalse(PortUtil.isValidPort(String.valueOf(0xffff + 1)));
        assertFalse(PortUtil.isValidPort(String.valueOf("   ")));
        assertFalse(PortUtil.isValidPort(null));
        assertTrue(PortUtil.isValidPort("    122      "));
    }

    @Test
    public void testValidatePortInt() throws Exception
    {
        assertFalse(PortUtil.isValidPort(-1));
        for (int i = 0; i <= 0xffff; i++)
        {
            assertTrue(PortUtil.isValidPort(i));
        }
        assertFalse(PortUtil.isValidPort(0xffff + 1));
    }

    @Test
    public void testGetPort() throws Exception
    {
        assertEquals(-1, PortUtil.parsePort(String.valueOf(-1)));
        for (int i = 0; i <= 0xffff; i++)
        {
            assertEquals(i, PortUtil.parsePort(String.valueOf(i)));
        }
        assertEquals(-1, PortUtil.parsePort(String.valueOf(0xffff + 1)));
        assertEquals(-1, PortUtil.parsePort(String.valueOf("   ")));
        assertEquals(-1, PortUtil.parsePort(null));

        assertEquals(122, PortUtil.parsePort("    122      "));
    }
}
