package com.atlassian.jira.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Class to wrap around the encoding of query strings. This has ordinarily been done by using the JIRA encoding.
 */
public class JiraUrlCodec
{
    private static final Logger log = Logger.getLogger(JiraUrlCodec.class);

    /**
     * URL encode the passed value using the passed character encoding, or if the encoding doesn't exist, the system
     * encoding.
     *
     * @param value the value to encode.
     * @param encoding the character encoding to use
     * @param spacesEncodedasHexValue if true spaces are encoded with the the hex value '%20', otherwise if false then the character '+'.
     * @return the encoded value.
     */
    @HtmlSafe
    public static String encode(String value, String encoding, boolean spacesEncodedasHexValue)
    {
        try
        {
            return URLCodec.encode(value, encoding, spacesEncodedasHexValue);
        }
        catch (UnsupportedEncodingException e)
        {
            log.warn("Unable to encode '" + value + "' with encoding '" + encoding + "'. Encoding with system defaults.");
            return systemDefaultEncoding(value, spacesEncodedasHexValue);
        }
    }

    /**
     * URL encode the passed value using the passed character encoding, or if the encoding doesn't exist, the system
     * encoding. Spaces are encoded as the '+' character.
     *
     * @param value the value to encode.
     * @param encoding the character encoding to use
     * @return the encoded value.
     */
    @HtmlSafe
    public static String encode(String value, String encoding)
    {
        return encode(value, encoding,  false);
    }

    /**
     * URL encode the passed value using the configured JIRA character encoding, or if the encoding doesn't exist,
     * the system encoding. Spaces are encoded as the '+' character.
     *
     * @param value the value to encode.
     * @return the encoded value.
     */
    @HtmlSafe
    public static String encode(String value)
    {
        return encode(value, false);
    }

     /**
     * URL encode the passed value using the configured JIRA character encoding, or if the encoding doesn't exist,
     * the system encoding.
     *
     * @param spacesEncodedasHexValue if true spaces are encoded with the the hex value '%20', otherwise if false then the character '+'.
     * @param value the value to encode.
     * @return the encoded value.
     */
    @HtmlSafe
    public static String encode(String value, boolean spacesEncodedasHexValue)
    {
        return encode(value, ComponentAccessor.getApplicationProperties().getEncoding(), spacesEncodedasHexValue);
    }

    /**
     * URL decode the passed value using the passed character encoding, or if the encoding doesn't exist, the system
     * encoding.
     *
     * @param value the value to decode.
     * @param encoding the character encoding to use
     * @return the decoded value.
     */
    public static String decode(String value, String encoding)
    {
        try
        {
            return URLCodec.decode(value, encoding);
        }
        catch (UnsupportedEncodingException e)
        {
            log.warn("Unable to dencode '" + value + "' with encoding '" + encoding + "'. Encoding with system defaults.");
            return URLDecoder.decode(value);
        }
    }

    /**
     * URL decode the passed value using the configured JIRA character encoding, or if the encoding doesn't exist,
     * the system encoding.
     *
     * @param value the value to decode.
     * @return the decoded value.
     */
    public static String decode(String value)
    {
        return decode(value, ComponentAccessor.getApplicationProperties().getEncoding());
    }

    static String systemDefaultEncoding(final String value, boolean spacesAsHexValue)
    {
        String encodedResult = URLEncoder.encode(value);
        // Java encoding encodes ' '  as '+', so need to replace it with our '%20' encoding.
        if (spacesAsHexValue)
        {
            encodedResult = encodedResult.replaceAll("\\+", "%20");
        }
        return encodedResult;
    }
}