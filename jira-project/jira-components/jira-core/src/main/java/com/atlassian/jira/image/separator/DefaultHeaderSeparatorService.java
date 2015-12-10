package com.atlassian.jira.image.separator;

import com.atlassian.jira.image.util.ImageUtils;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Default implementation that uses raws the image on a created buffered image.
 * This is not cached so consumers should look after their own caching.
 *
 * @since v4.0
 */
public class DefaultHeaderSeparatorService implements HeaderSeparatorService
{
    private static final Logger log = Logger.getLogger(DefaultHeaderSeparatorService.class);

    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final Color DEFAULT_BG_COLOR = new Color(255, 255, 255, 0);

    private final ImageUtils imageUtils;

    public DefaultHeaderSeparatorService(ImageUtils imageUtils)
    {
        this.imageUtils = imageUtils;
    }

    public byte[] getSeparator(final String colorHex, final String backgroundColorHex)
    {
        final BufferedImage image = new BufferedImage(1, 13, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = image.createGraphics();
        final Color backgroundColor = imageUtils.getColor(backgroundColorHex, false);
        graphics.setColor(backgroundColor == null ? DEFAULT_BG_COLOR : backgroundColor);
        graphics.drawLine(0, 0, 0, 12);

        final Color color = imageUtils.getColor(colorHex, false);
        graphics.setColor(color == null ? DEFAULT_COLOR : color);
        graphics.drawLine(0, 1, 0, 1);
        graphics.drawLine(0, 3, 0, 3);
        graphics.drawLine(0, 5, 0, 5);
        graphics.drawLine(0, 7, 0, 7);
        graphics.drawLine(0, 9, 0, 9);
        graphics.drawLine(0, 11, 0, 11);

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try
        {
            ImageIO.write(image, "png", byteArrayOutputStream);
        }
        catch (IOException e)
        {
            log.error("Wow!  An IOException occured with a ByteArrayOutputStream.  Is that even possible?", e);
            throw new RuntimeException(e);
        }

        return byteArrayOutputStream.toByteArray();
    }

}
