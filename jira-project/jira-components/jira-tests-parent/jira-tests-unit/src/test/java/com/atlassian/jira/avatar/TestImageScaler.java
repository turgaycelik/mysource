package com.atlassian.jira.avatar;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link com.atlassian.jira.avatar.ImageScaler}.
 *
 * @since v4.0
 */
public class TestImageScaler
{
    @Test
    public void testGetSelectedImageDataSelectAll()
    {
        ImageScaler is = new ImageScaler();
        BufferedImage source = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Selection all = new Selection(0, 0, source.getWidth(), source.getHeight());
        final RenderedImage selectedImageData = is.getSelectedImageData(source, all, source.getWidth());
        assertEquals(300, selectedImageData.getWidth());
        assertEquals(300, selectedImageData.getHeight());
    }

    @Test
    public void testGetSelectedImageDataSelectAuto()
    {
        ImageScaler is = new ImageScaler();
        BufferedImage source = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        final RenderedImage selectedImageData = is.getSelectedImageData(source, null, source.getWidth());
        assertEquals(300, selectedImageData.getWidth());
        assertEquals(300, selectedImageData.getHeight());
    }

    @Test
    public void testGetSelectedImageDataScaleDown()
    {
        ImageScaler is = new ImageScaler();
        BufferedImage source = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Selection all = new Selection(0, 0, source.getWidth(), source.getHeight());
        final RenderedImage selectedImageData = is.getSelectedImageData(source, all, 100);
        assertEquals(100, selectedImageData.getWidth());
        assertEquals(100, selectedImageData.getHeight());
    }
}
