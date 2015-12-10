package com.atlassian.jira.image.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.awt.*;

/**
 * Default omplementation of the Image Utility class
 *
 * @since v4.0
 */
public class ImageUtilsImpl implements ImageUtils
{
    private static final Logger log = Logger.getLogger(ImageUtilsImpl.class);

    public Color getColor(String hexString, boolean transparent)
    {
        if (StringUtils.isNotBlank(hexString))
        {
            try
            {
                if (hexString.startsWith("#"))
                {
                    hexString = hexString.substring(1, hexString.length());
                }
                if (hexString.length() == 3)
                {
                    // 3 char hex strings
                    String s = hexString.substring(0, 1);
                    int r = Integer.parseInt(s + s, 16);
                    s = hexString.substring(1, 2);
                    int g = Integer.parseInt(s + s, 16);
                    s = hexString.substring(2, 3);
                    int b = Integer.parseInt(s + s, 16);
                    int a = transparent ? 0 : 255;
                    return new Color(r, g, b, a);
                }
                else if (hexString.length() == 6)
                {
                    // 6 char hex strings
                    int r = Integer.parseInt(hexString.substring(0, 2), 16);
                    int g = Integer.parseInt(hexString.substring(2, 4), 16);
                    int b = Integer.parseInt(hexString.substring(4, 6), 16);
                    int a = transparent ? 0 : 255;
                    return new Color(r, g, b, a);
                }
                else
                {
                    log.warn("Color must be a 6 or 3 char hex string (with an optional leading hash) but was " + hexString);
                }
            }
            catch (NumberFormatException nfe)
            {
                log.warn("Color must be a 6 or 3 char hex string (with an optional leading hash)", nfe);
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                log.warn("Color must be a 6 or 3 char hex string (with an optional leading hash)", e);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                log.warn("Color must be a 6 or 3 char hex string (with an optional leading hash)", e);
            }
        }
        return null;
    }
}
