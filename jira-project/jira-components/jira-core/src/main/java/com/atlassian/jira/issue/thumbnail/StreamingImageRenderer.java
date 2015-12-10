package com.atlassian.jira.issue.thumbnail;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Image renderer using a streaming approach. Doesn't yield high quality
 * thumbnails, but doesn't need to rasterize the whole image at once.
 */
public class StreamingImageRenderer
{
    public StreamingImageRenderer() { }

    public Dimensions renderThumbnail(
            final InputStream inputStream, final File thumbnailFile, final int maxWidth, final int maxHeight)
            throws IOException
    {
        final ImageInputStream iis = ImageIO.createImageInputStream(inputStream);
        final BufferedImage bi = scaleDown(iis, maxWidth, maxHeight);
        ImageIO.write(bi, "png", thumbnailFile);
        return new Dimensions(bi.getWidth(), bi.getHeight());
    }

    private BufferedImage scaleDown(final ImageInputStream inputStream, final int maxWidth, final int maxHeight)
            throws IOException
    {
        final Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
        if (!readers.hasNext())
        {
            throw new IOException("No ImageReader available for the given ImageInputStream");
        }
        // Use the first reader, next will instantiate ImageReader which needs to be disposed
        final ImageReader reader = readers.next();
        try
        {
            final ImageReadParam param = reader.getDefaultReadParam();
            reader.setInput(inputStream);

            final Dimension original = new Dimension(reader.getWidth(0), reader.getHeight(0));
            final Dimension target = new Dimension(maxWidth, maxHeight);
            final int ratio = maintainAspectRatio(original, target);
            param.setSourceSubsampling(ratio, ratio, 0, 0);

            return reader.read(0, param);
        }
        finally
        {
            reader.dispose();
        }
    }

    public int maintainAspectRatio(final Dimension original, final Dimension target)
    {
        if (original.getWidth() > target.getWidth() || original.getHeight() > target.getHeight())
        {
            final long ratioWidth = Math.round(original.getWidth() / target.getWidth());
            final long ratioHeight = Math.round(original.getHeight() / target.getHeight());
            return (int) Math.max(ratioHeight, ratioWidth);
        }
        return 1;
    }
}