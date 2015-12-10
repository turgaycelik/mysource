package com.atlassian.jira.web.servlet;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Utility methods for setting headers on the HTTP response.
 *
 * @since v5.0
 */
public class HttpResponseHeaders
{
    /**
     * Logger for this HttpResponse instance.
     */
    private static final Logger log = LoggerFactory.getLogger(HttpResponseHeaders.class);

    /**
     * Approximately one year, in seconds.
     */
    private static final long ABOUT_ONE_YEAR = 60 * 60 * 24 * 365;

    /**
     * Approximately one year, in milliseconds.
     */
    private static final long ABOUT_ONE_YEAR_MILLIS = MILLISECONDS.convert(ABOUT_ONE_YEAR, SECONDS);

    /**
     * The Cache-control value to allow private caching for about one year.
     */
    private static final String CACHE_PRIVATELY_ONE_YEAR = "private, max-age=" + ABOUT_ONE_YEAR;

    /**
     * Sets the <b><code>Cache-control</code></b> and <b><code>Expires</code></b> headers to allow browsers, but not
     * proxies, to cache the response for up to approximately 1 year. This kind of caching is appropriate for non-public
     * content such as avatars, attachments, thumbnails, etc. More specifically, this sets the following headers:
     * <p/>
     * <pre>
     *   <code>Cache-control</code>: {@value #CACHE_PRIVATELY_ONE_YEAR}
     *   <code>Expires</code>: Wed, 31 Dec 1969 23:59:59 GMT
     * </pre>
     * <p/>
     * The <code>Expires</code> header is set to a date in the past to prevent HTTP 1.0 proxies from caching what is non
     * publicly-accessible content. Since the <code>Cache-control</code> header takes precendence over the
     * <code>Expires</code> header, HTTP 1.1-capable proxies will still cache the data privately.
     * <p/>
     * If the response already has a <code>Cache-control</code> header, it will be overwritten.
     *
     * @param response the HttpServletResponse on which we will set the header
     * @since v5.0
     */
    public static void cachePrivatelyForAboutOneYear(HttpServletResponse response)
    {
        if (response == null)
        {
            throw new NullPointerException("response");
        }

        response.setHeader("Cache-control", CACHE_PRIVATELY_ONE_YEAR); // HTTP 1.1 proxies may cache privately
        response.setDateHeader("Expires", -1); // HTTP 1.0 proxies may not cache (they don't understand private caching)
    }

    /**
     * Sets the <b><code>Cache-control</code></b> and <b><code>Expires</code></b> headers to allow browsers and proxies
     * to cache the response for up to approximately 1 year. This kind of caching is appropriate for public content such
     * as icons, sprites, etc.
     * <p/>
     * <pre>
     *   <code>Cache-control</code>: {@value #CACHE_PRIVATELY_ONE_YEAR}
     *   <code>Expires</code>: "now" + 1 year
     * </pre>
     * <p/>
     * If the response already has a <code>Cache-control</code> header, it will be overwritten.
     *
     * @param response the HttpServletResponse on which we will set the header
     * @since v5.0
     */
    public static void cachePubliclyForAboutOneYear(HttpServletResponse response)
    {
        if (response == null)
        {
            throw new NullPointerException("response");
        }

        response.setHeader("Cache-control", CACHE_PRIVATELY_ONE_YEAR);                  // HTTP 1.1 proxies may cache privately
        response.setDateHeader("Expires", currentTimeMillis() + ABOUT_ONE_YEAR_MILLIS); // HTTP 1.0 proxies may cache for ~1 year
    }

    private HttpResponseHeaders()
    {
    }
}
