package com.atlassian.jira.image.dropdown;

import com.atlassian.jira.image.util.ImageUtils;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Default implementation that uses raws the image on a created buffered image. This is not cached so consumers should
 * look after their own caching.
 *
 * @since v4.0
 */
public class DefaultDropDownCreatorService implements DropDownCreatorService
{
    private static final Logger log = Logger.getLogger(DefaultDropDownCreatorService.class);

    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final Color DEFAULT_BG_COLOR = new Color(255, 255, 255, 0);

    private final ImageUtils imageUtils;

    public DefaultDropDownCreatorService(ImageUtils imageUtils)
    {
        this.imageUtils = imageUtils;
    }

    public byte[] getDropdown(final String colorHex, final String backgroundColorHex)
    {
        final BufferedImage image = new BufferedImage(7, 4, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = image.createGraphics();
        final Color backgroundColor = imageUtils.getColor(backgroundColorHex, false);
        graphics.setColor(backgroundColor == null ? DEFAULT_BG_COLOR : backgroundColor);
        graphics.fillRect(0, 0, 7, 4);

        final Color color = imageUtils.getColor(colorHex, false);
        graphics.setColor(color == null ? DEFAULT_COLOR : color);
        graphics.drawLine(0, 0, 6, 0);  //    *******
        graphics.drawLine(1, 1, 5, 1);  //     *****
        graphics.drawLine(2, 2, 4, 2);  //      ***
        graphics.drawLine(3, 3, 3, 3);  //       *

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
