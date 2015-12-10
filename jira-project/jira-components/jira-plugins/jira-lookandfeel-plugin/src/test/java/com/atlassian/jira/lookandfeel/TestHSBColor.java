package com.atlassian.jira.lookandfeel;

import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertEquals;

public class TestHSBColor
{
    @Test
    public void testGetters()
    {
        final Color awtColor = new Color(64, 128, 64);
        final HSBColor color = new HSBColor(awtColor);
        assertEquals(120, color.getHue());
        assertEquals(50, color.getSaturation());
        assertEquals(50, color.getBrightness());
        assertEquals("#408040", color.getHexString());
        assertEquals(awtColor, color.getColor());
    }

    @Test
    public void testGetHexString()
    {
        assertEquals("#000000", new HSBColor(Color.black).getHexString());
        assertEquals("#ffffff", new HSBColor(Color.white).getHexString());
        assertEquals("#ff0000", new HSBColor(Color.red).getHexString());
        assertEquals("#00ff00", new HSBColor(Color.green).getHexString());
        assertEquals("#0000ff", new HSBColor(Color.blue).getHexString());
    }

    @Test
    public void testCreateFromHexString()
    {
        assertEquals("#408040", new HSBColor("408040").getHexString());
        assertEquals("#408040", new HSBColor("#408040").getHexString());
    }

    @Test
    public void testCreateFromHSB()
    {
        final HSBColor color = new HSBColor(120, 75, 25);
        assertEquals(120, color.getHue());
        assertEquals(75, color.getSaturation());
        assertEquals(25, color.getBrightness());
    }
    
    @Test
    public void testLighten()
    {
        final HSBColor color = new HSBColor(120, 25, 50);
        final HSBColor result = color.lightenByPercentage(10);
        assertEquals(55, result.getBrightness());
        assertEquals(color.getHue(), result.getHue());
        assertEquals(color.getSaturation(), result.getSaturation());
    }
    
    @Test
    public void testDarken()
    {
        final HSBColor color = new HSBColor(120, 25, 50);
        final HSBColor result = color.darkenByPercentage(10);
        assertEquals(45, result.getBrightness());
        assertEquals(color.getHue(), result.getHue());
        assertEquals(color.getSaturation(), result.getSaturation());
    }
    
    @Test
    public void testSaturate()
    {
        final HSBColor color = new HSBColor(120, 50, 25);
        final HSBColor result = color.saturateByPercentage(10);
        assertEquals(55, result.getSaturation());
        assertEquals(color.getHue(), result.getHue());
        assertEquals(color.getBrightness(), result.getBrightness());
    }
    
    @Test
    public void testDesaturate()
    {
        final HSBColor color = new HSBColor(120, 50, 25);
        final HSBColor result = color.desaturateByPercentage(10);
        assertEquals(45, result.getSaturation());
        assertEquals(color.getHue(), result.getHue());
        assertEquals(color.getBrightness(), result.getBrightness());
    }
}
