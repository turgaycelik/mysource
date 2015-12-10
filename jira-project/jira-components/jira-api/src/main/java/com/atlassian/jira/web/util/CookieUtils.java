package com.atlassian.jira.web.util;

import com.atlassian.jira.util.JiraUrlCodec;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This class handles utility methods for managing HTTP cookies.
 */
public class CookieUtils
{

    private static final Logger LOG = Logger.getLogger(CookieUtils.class);

    private static final Pattern CONGLOMERATE_KEYPAIR_PATTERN = Pattern.compile("^(.+)?=(.+)$");
    
    /** session id cookie name */
    public static final String JSESSIONID = "JSESSIONID";

    private static final String MSG_DUPLICATE_SESSION_IDS =
            "Found multiple JSESSIONID cookies, when rendering link to screenshot applet page. You may have another Java webapp at context path '/' (in which case this message is harmless), or there may be a problem in IE attaching the screenshot via the screenshot applet.";

    /**
     * Gets the session id as a string from the given array of Cookie objects if only one {@link #JSESSIONID} cookie is
     * found. If none or more than one {@link #JSESSIONID} cookies are found, returns null.
     * @param cookies the cookies.
     * @return session value if unique one is found.
     */
    public static String getSingleSessionId(Cookie[] cookies)
    {
        if (cookies == null)
        {
            return null;
        }

        String sessionId = null;
        boolean foundASessionId = false;
        for (Cookie cookie : cookies)
        {
            if (JSESSIONID.equals(cookie.getName()))
            {
                if (!foundASessionId)
                {
                    sessionId = cookie.getValue();
                    foundASessionId = true;
                }
                else
                {
                    // We found a second JSESSIONID cookie.
                    // This message spams the logs in some circumstances that should not be warnings.
                    // eg on EAC/J and Extranet Confluence run on the same host. Hence I dropped the priority of this from WARN to INFO.
                    LOG.debug(MSG_DUPLICATE_SESSION_IDS);
                    return null;
                }
            }
        }
        return sessionId;
    }

    /**
     * Returns true if one or more cookies with name of {@link #JSESSIONID} is found.
     * @param cookies an array of Cookie objects
     * @return true if one or more sessionid cookies are found
     */
    public static boolean hasSessionId(Cookie[] cookies)
    {
        if (cookies == null)
        {
            return false;
        }

        for (final Cookie cooky : cookies)
        {
            if (JSESSIONID.equals(cooky.getName()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new {@link #JSESSIONID} cookie to the HTTP response if the given sessionId is not null. No cookie is set
     * if given sessionId is null.
     * @param request    HTTP request
     * @param response   HTTP response
     * @param sessionId  session ID to set as a value of the cookie
     */
    public static void setSessionCookie(HttpServletRequest request, HttpServletResponse response, String sessionId)
    {
        if (sessionId != null)
        {
            Cookie cookie = new Cookie(JSESSIONID, sessionId);
            cookie.setPath(cookiePathFor(request));
            response.addCookie(cookie);
        }
    }


    /**
     * Get path for newly created cookie.
     *
     * @param httpServletRequest current request
     * @return cookie path reflecting the current request's path
     */
    public static String cookiePathFor(HttpServletRequest httpServletRequest)
    {
        String requestPath = httpServletRequest.getContextPath();
        return StringUtils.isNotEmpty(requestPath) ? requestPath : "/";
    }

    /**
     * Creat cookie with path reflecting the context path of <tt>currentRequest</tt>.
     *
     * @param name new cookie's name
     * @param value new cookie's value
     * @param currentRequest current request
     * @return new cookie
     */
    public static Cookie createCookie(String name, String value, HttpServletRequest currentRequest)
    {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(cookiePathFor(currentRequest));
        return cookie;
    }

    /**
     * Get value of cookie with given <tt>cookieName</tt> from <tt>currentRequest</tt>, or <code>null</code>,
     * if the request does not have such cookie.
     *
     * @param cookieName name of the cookie to retrieve
     * @param currentRequest current request
     * @return value of the cookie, or <code>null</code>, if no cookie with such name exists
     */
    public static String getCookieValue(String cookieName, HttpServletRequest currentRequest)
    {
        notNull("cookieName", cookieName);
        notNull("currentRequest", currentRequest);
        final Cookie[] cookies = currentRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies)
            {
                if (cookieName.equals(cookie.getName()))
                {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Parse the key/value pairs out of a conglomerate cookie.
     *
     * The returned map will not contain empty values (zero length strings: they are discarded).
     *
     * @param cookieName the name of the cookie
     * @param currentRequest current request
     * @return the non-null map of key/value pairs
     */
    public static Map<String, String> parseConglomerateCookie(String cookieName, HttpServletRequest currentRequest) {
        Map<String, String> result = new LinkedHashMap<String, String>();

        final String cookieValue = CookieUtils.getCookieValue(cookieName, currentRequest);
        if (StringUtils.isNotBlank(cookieValue))
        {
            final String[] values = cookieValue.split("[|]");
            for (String rawKeyValuePair : values)
            {
                if (StringUtils.isNotBlank(rawKeyValuePair)) {
                    String keyValuePair = JiraUrlCodec.decode(rawKeyValuePair, "UTF-8");
                    if (StringUtils.isNotBlank(keyValuePair))
                    {
                        Matcher m = CONGLOMERATE_KEYPAIR_PATTERN.matcher(keyValuePair);
                        if (m.matches()) {
                            final String key = m.group(1);
                            final String value = m.group(2);
                            result.put(key, value);
                        }
                    }
                }
            }
        }

        return result;
    }

    public static Cookie createConglomerateCookie(final String cookieName, final Map<String, String> map, final HttpServletRequest request)
    {
        StringBuilder cookieValue = new StringBuilder();
        String sep = "";
        for (Map.Entry<String, String> keyValuePair : map.entrySet())
        {
            if (StringUtils.isNotBlank(keyValuePair.getValue())) {
                String s = JiraUrlCodec.encode(keyValuePair.getKey() + "=" + keyValuePair.getValue(), "UTF-8");
                cookieValue.append(sep).append(s);
                sep = "|";
            }
        }

        Cookie cookie = createCookie(cookieName, (cookieValue.toString()), request);
        cookie.setMaxAge(Integer.MAX_VALUE);
        return cookie;
    }
}
