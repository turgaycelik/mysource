package com.atlassian.jira.pageobjects.util;

import javax.inject.Inject;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

/**
 * Class that can be used to manipulate the current user's session on the server.
 *
 * @since v5.0.1
 */
public class UserSessionHelper
{
    @Inject
    private WebDriver driver;

    @Inject
    private JavascriptRunner executor;

    public void clearWebSudo()
    {
        executor.clearWebSudo();
    }

    /**
     * Sets the JSESSIONID value to garbage. Doing this will essentially invalidate the cookie, forcing JIRA to create
     * treat the request as a request that comes in with an expired JSESSIONID.
     */
    public void invalidateSession()
    {
        driver.manage().deleteCookieNamed("JSESSIONID");
        driver.manage().addCookie(new Cookie("JSESSIONID", "nonsense"));
    }

    /**
     * Deletes the JSESSIONID cookie. Not sending this value forces the Session Expired page error (sending a garbage
     * value displays the XSRF page instead).
     */
    public void deleteSession()
    {
        driver.manage().deleteCookieNamed("JSESSIONID");
    }

    public void destoryAllXsrfTokens()
    {
        executor.destroyAllXsrfTokens();
    }
}
