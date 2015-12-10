package com.atlassian.jira.security.auth.rememberme;

import com.atlassian.seraph.service.rememberme.DefaultRememberMeConfiguration;

import javax.servlet.http.HttpServletRequest;

/**
 * This is the SPI implementation of {@link com.atlassian.seraph.spi.rememberme.RememberMeConfiguration}
 *
 * @since v4.2
 */
public class JiraRememberMeConfiguration extends DefaultRememberMeConfiguration
{
    @Override
    public boolean isCookieHttpOnly(HttpServletRequest httpServletRequest)
    {
        // At the moment the func test framework cannot handle HttpOnly cookies.  Until it can we
        // will return false here and not use them
        return true;
    }
}
