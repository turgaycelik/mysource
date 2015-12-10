package com.atlassian.jira.plugin.viewissue.issuelink;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Factory for encoding and decoding globalIds.
 *
 * @since v5.0
 */
public class GlobalIdFactory
{
    private static final String ENCODING = "UTF-8";

    /**
     * Encode the given Map of values to a String.
     *
     * @param keys the order in which the keys will be appended to the result
     * @param values the values to encode as a String
     * @return a String
     */
    public static String encode(final List<String> keys, final Map<String, String> values)
    {
        final List<NameValuePair> params = new ArrayList<NameValuePair>();

        for (final String key : keys)
        {
            params.add(new BasicNameValuePair(key, values.get(key)));
        }

        return URLEncodedUtils.format(params, ENCODING);
    }

    /**
     * Decode the given String to a Map of values.
     *
     * @param globalId the String to decode
     * @param keys the order in which the keys should appear. If the keys are not in this order an IllegalArgumentException is thrown.
     * @return a Map of values
     */
    public static Map<String, String> decode(final String globalId, final List<String> keys)
    {
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        final Scanner scanner = new Scanner(globalId);

        try
        {
            URLEncodedUtils.parse(params, scanner, ENCODING);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("globalId is invalid, expected format is: " + getExpectedFormat(keys) + ", found: " + globalId, e);
        }

        // Check that we have the right number of keys
        if (params.size() != keys.size())
        {
            throw new IllegalArgumentException("globalId is invalid, expected format is: " + getExpectedFormat(keys) + ", found: " + globalId);
        }

        // Get the values, and make sure the keys are in the correct order
        final Map<String, String> result = new HashMap<String, String>(params.size());
        for (int i = 0; i < params.size(); i++)
        {
            final NameValuePair param = params.get(i);
            if (!param.getName().equals(keys.get(i)))
            {
                throw new IllegalArgumentException("globalId is invalid, expected format is: " + getExpectedFormat(keys) + ", found: " + globalId);
            }

            result.put(param.getName(), param.getValue());
        }

        return result;
    }

    private static String getExpectedFormat(final List<String> keys)
    {
        final StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (final String key : keys)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                sb.append("&");
            }

            sb.append(key).append("=<").append(key).append(">");
        }

        return sb.toString();
    }
}
