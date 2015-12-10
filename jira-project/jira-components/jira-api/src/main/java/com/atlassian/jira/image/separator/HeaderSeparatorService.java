package com.atlassian.jira.image.separator;

import com.atlassian.annotations.PublicApi;

/**
 * A service for creating a colourful header separators.  These are not cached so the client shoud do its own caching.
 *
 * @since v4.0
 */
@PublicApi
public interface HeaderSeparatorService
{

    /**
     * Get the bytes for a separator image for the given colours.  Allows for easy streaming.
     * <p/>
     * Input strings can ontain a leading hash (#) and can be a 3 char or 6 char hex string.  See any web tutorial for
     * what colour the string represents.
     * If hex colors are wrong, default colours are returned.  Black arrow and a transparent white background.
     *
     * @param colorHex           The main color of the separator
     * @param backgroundColorHex The background colour of the separator. This will also be transparent.  Useful for IE6
     * @return An array of bytes representing an image.
     */
    byte[] getSeparator(String colorHex, String backgroundColorHex);
}
