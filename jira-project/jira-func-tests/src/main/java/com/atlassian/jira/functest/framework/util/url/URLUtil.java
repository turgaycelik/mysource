package com.atlassian.jira.functest.framework.util.url;

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since v3.13
 */
public class URLUtil
{
    private static final Logger log = Logger.getLogger(URLUtil.class);
    private static final Pattern URL_PATTERN = Pattern.compile("(.*)\\?([^&]+)(.*)");

    /**
     * Compares 2 URL strings for equality.  Two URLs will be equal, if their baseURL is the same, and they have the
     * same parameters (regardless of ordering).  The baseURL does not include contextPath.
     * <p/>
     * For example: ViewWorkflowSteps.jspa?workflowMode=live&workflowName=Dude AND /jira/ViewWorkflowSteps.jspa?workflowName=Dude&workflowMode=live
     * are deemed to be equal by this method.
     * <p/>
     * Amazingly enough I couldn't find an open source helper that already does this!
     *
     * @param controlLink The link assumed to be correct.
     * @param testLink The link being tested.
     * @return True if the two links are correct.
     */
    public static boolean compareURLStrings(String controlLink, String testLink)
    {
        if (testLink.indexOf(controlLink) != -1)
        {
            return true;
        }
        //got a query string? Perhaps the params are in different order.
        else if (controlLink.indexOf("?") != -1)
        {
            String[] controlStrings = controlLink.split("\\?");
            String[] testStrings = testLink.split("\\?");
            //check the baseURLs the same!
            if (testStrings[0].indexOf(controlStrings[0]) == -1)
            {
                log.info("Tested URL '" + testStrings[0] + "' did not containt control URL '" + controlStrings[0] + "'");
                return false;
            }

            Map controlParams = getParameters(controlStrings[1]);
            Map testParams = getParameters(testStrings[1]);
            if (!controlParams.equals(testParams))
            {
                log.info("Parameters of the tested URL '" + testLink + "' don't equal the control URL '" + controlLink + "'");
                return false;
            }
            return true;
        }

        return false;
    }

    /**
     * @param url the url to process e.g. <code>http://www.atlassian.com/search?blablabla=haha&test=two</code>
     * @param paramName the name of the parameter to retrieve the value of
     * @return the value of the query parameter from the URL string; null if there was no query part or the fieldId
     *         could not be found.
     */
    public static String getQueryParamValueFromUrl(String url, String paramName)
    {
        String[] urlParts = url.split("\\?");
        if (urlParts.length == 1)
        {
            return null;
        }

        return getParameters(urlParts[1]).get(paramName);
    }

    /**
     * @param url the url to process
     * @param paramName the name of the parameter to retrieve the value of
     * @return the value of the query parameter from the URL string; null if there was no query part or the fieldId
     *         could not be found.
     */
    public static String getQueryParamValueFromUrl(URL url, String paramName)
    {
        return getQueryParamValueFromUrl(url.toString(), paramName);
    }

    private static Map<String, String> getParameters(String controlQueryString)
    {
        StringTokenizer tokenizer = new StringTokenizer(controlQueryString, "&=");
        Map<String, String> controlParams = new HashMap<String, String>();
        while (tokenizer.hasMoreTokens())
        {
            String paramKey = tokenizer.nextToken();
            String paramValue = tokenizer.nextToken();
            controlParams.put(paramKey, paramValue);
        }
        return controlParams;
    }

    /**
     * Adds a token to a given url in the FIRST position
     *
     * @param token the token
     * @param url the url in question
     * @return the url with the token in place
     */
    public static String addXsrfToken(final String token, final String url)
    {
        final Matcher m = URL_PATTERN.matcher(url);
        if (m.matches())
        {
            return new StringBuilder(m.group(1))
                    .append("?atl_token=").append(token).append("&")
                    .append(m.group(2))
                    .append(m.group(3)).toString();
        }
        else
        {
            return url + "?atl_token=" + token;
        }
    }

    /**
     * Can be called to encode a parameter part of an URL
     *
     * @param urlParam the parameter to be plaved into a URL
     * @return the encoded data
     */
    public static String encode(String urlParam)
    {
        try
        {
            return URLEncoder.encode(urlParam, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return "ThisCanNeverHappenInJava";
        }
    }

    /**
     * The opposite of {@link #encode(String)}
     *
     * @param urlParam the param to decode
     * @return the decoded value
     */
    public static String decode(String urlParam)
    {
        try
        {
            return URLDecoder.decode(urlParam, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return "ThisCanNeverHappenInJava";
        }
    }
}
