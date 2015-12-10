package com.atlassian.jira.image.util;

import java.awt.*;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestImageUtilImpl
{
    private static Color getColor(final String r, final String g, final String b, final int a)
    {
        return new Color(Integer.parseInt(r, 16), Integer.parseInt(g, 16), Integer.parseInt(b, 16), a);
    }

    @Test
    public void testGetColorNulls()
    {
        final ImageUtils util = new ImageUtilsImpl();
        assertNull(util.getColor(null, true));
        assertNull(util.getColor(null, false));
        assertNull(util.getColor("", false));
        assertNull(util.getColor("", false));
        assertNull(util.getColor("nick", true));
        assertNull(util.getColor("1234", true));
        assertNull(util.getColor("1", true));
        assertNull(util.getColor("12", true));
        assertNull(util.getColor("21345", true));
        assertNull(util.getColor("1234567", true));
        assertNull(util.getColor("123456789", true));
        assertNull(util.getColor("12fu12", true));
        assertNull(util.getColor("nic", true));
        assertNull(util.getColor("nichol", true));
    }

    @Test
    public void testGetColor()
    {
        final ImageUtils util = new ImageUtilsImpl();
        assertEquals(getColor("12", "34", "56", 0), util.getColor("123456", true));
        assertEquals(Color.BLACK, util.getColor("000000", false));
        assertEquals(Color.WHITE, util.getColor("ffffff", false));
        assertEquals(Color.BLUE, util.getColor("0000ff", false));
        assertEquals(Color.RED, util.getColor("ff0000", false));
        assertEquals(Color.GREEN, util.getColor("00ff00", false));
        assertEquals(Color.BLACK, util.getColor("000", false));
        assertEquals(Color.WHITE, util.getColor("fff", false));
        assertEquals(Color.BLUE, util.getColor("00f", false));
        assertEquals(Color.RED, util.getColor("f00", false));
        assertEquals(Color.GREEN, util.getColor("0f0", false));
        assertEquals(getColor("12", "34", "56", 0), util.getColor("#123456", true));
        assertEquals(Color.BLACK, util.getColor("#000000", false));
        assertEquals(Color.WHITE, util.getColor("#ffffff", false));
        assertEquals(Color.BLUE, util.getColor("#0000ff", false));
        assertEquals(Color.RED, util.getColor("#ff0000", false));
        assertEquals(Color.GREEN, util.getColor("#00ff00", false));
        assertEquals(Color.BLACK, util.getColor("#000", false));
        assertEquals(Color.WHITE, util.getColor("#fff", false));
        assertEquals(Color.BLUE, util.getColor("#00f", false));
        assertEquals(Color.RED, util.getColor("#f00", false));
        assertEquals(Color.GREEN, util.getColor("#0f0", false));

        assertEquals(getColor("12", "34", "56", 255), util.getColor("#123456", false));
        assertEquals(getColor("00", "00", "00", 0), util.getColor("#000", true));
    }
}
