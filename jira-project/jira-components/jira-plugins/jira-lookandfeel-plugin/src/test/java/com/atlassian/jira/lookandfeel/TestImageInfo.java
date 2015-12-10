package com.atlassian.jira.lookandfeel;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestImageInfo
{
    @Test
    public void testColorCount() throws IOException
    {
        final BufferedImage image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("square.png"));
        final ImageInfo imageInfo = new ImageInfo(image);
        final List<ColorCount> result = imageInfo.getColorRatio();

        assertEquals(5, result.size());
        assertColorCount(fromRGB(16, 16, 64),    900, result.get(0));
        assertColorCount(fromRGB(0, 0, 0),       700, result.get(1));
        assertColorCount(fromRGB(255, 255, 255), 500, result.get(2));
        assertColorCount(fromRGB(64, 128, 64),   300, result.get(3));
        assertColorCount(fromRGB(255, 0, 0),     100, result.get(4));
    }

    @Test
    public void testTopLeftPixel() throws IOException
    {
        final BufferedImage image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("square.png"));
        final ImageInfo imageInfo = new ImageInfo(image);
        assertEquals(fromRGB(255, 0, 0), imageInfo.getTopLeftPixel());
    }

    private static void assertColorCount(final HSBColor expectedColor, final int expectedCount, final ColorCount actual)
    {
        assertEquals(expectedColor, actual.getColor());
        assertEquals(expectedCount, actual.getCount());
    }

    private static HSBColor fromRGB(final int r, final int g, final int b)
    {
        return new HSBColor(new Color(r, g, b));
    }
}
