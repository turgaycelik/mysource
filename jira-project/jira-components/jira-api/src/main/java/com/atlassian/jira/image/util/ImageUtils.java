package com.atlassian.jira.image.util;

import java.awt.*;

/**
 * Util class for calculating common things for image manipulation/creation.
 *
 * @since v4.0
 */
public interface ImageUtils
{
    /**
     * Turn a hex string into a {@link Color}
     * Input string can ontain a leading hash (#) and can be a 3 char or 6 char hex string.  See any web tutorial for
     * what colour the string represents.
     * If hex colors are wrong, null is returned.
     *
     * @param hexString   The hex representation to convert
     * @param transparent whether or not to add transparency
     * @return The equiv Color or null if there was a problem parsing the string
     */
    Color getColor(String hexString, boolean transparent);
}
