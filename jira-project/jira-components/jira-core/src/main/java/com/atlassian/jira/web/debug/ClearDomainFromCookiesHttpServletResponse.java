package com.atlassian.jira.web.debug;

import java.lang.reflect.Field;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

/**
 * Clears domain parameter from all addCookie requests. It can be also used for the purpose of {@link
 * BreakpointReadyHttpServletResponse}
 * <p/>
 * It only gets setup in dev.mode and clears all domain parameters from cookies this se needed because on dev mode we
 * usually run on localhost and one cannot set domain cookies for localhost
 *
 * @since v6.3
 */
@SuppressWarnings ("UnnecessaryLocalVariable")
public class ClearDomainFromCookiesHttpServletResponse extends BreakpointReadyHttpServletResponse
{

    private Field cookieDomainField;

    public ClearDomainFromCookiesHttpServletResponse(final HttpServletResponse delegate)
    {

        super(delegate);
        try
        {
            cookieDomainField = Cookie.class.getDeclaredField("domain");
            cookieDomainField.setAccessible(true);
        }
        catch (final NoSuchFieldException e)
        {
            cookieDomainField = null;
        }
    }

    @Override
    public void addCookie(final Cookie cookie)
    {
        //we cannot simply use cookie.setDomain(null); because tomcat wants to lowercase domain name and lowercase on
        //null does not work
        try
        {
            cookieDomainField.set(cookie, null);
        }
        catch (final IllegalAccessException ignore)
        {

        }
        super.addCookie(cookie);
    }

    @Override
    public void setHeader(final String name, final String value)
    {
        super.setHeader(name, getCookieValue(name, value));
    }

    @Override
    public void addHeader(final String name, final String value)
    {
        super.addHeader(name, getCookieValue(name, value));
    }

    private String getCookieValue(final String name, final String value)
    {
        if ("Set-Cookie".equals(name) && StringUtils.isNotBlank(value))
        {
            return value.replaceFirst("(?i)\\s*Domain=.+?;\\s*", " ");
        }
        return value;
    }
}
