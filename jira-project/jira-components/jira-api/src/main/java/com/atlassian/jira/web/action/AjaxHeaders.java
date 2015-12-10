package com.atlassian.jira.web.action;

import com.atlassian.crowd.embedded.api.User;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;

import static com.atlassian.jira.user.util.Users.isAnonymous;

/**
 * Utility class for dealing with AJAX request headers.
 *
 * @since v5.0
 */
public class AjaxHeaders
{
    /**
     * The request header that contains the username.
     */
    private static final String X_AUSERNAME = "X-AUSERNAME";

    /**
     * The request header that indicates whether it's a PJAX request or not.
     */
    private static final String X_PJAX = "X-PJAX";

    /**
     * Checks whether the effective user has change between the time when the view issue page was loaded and the time
     * when the current request was made (this can happen due to session timeouts, logging out in another window, etc).
     * <p/>
     * This method looks at the <code>{@value #X_AUSERNAME}</code> request header to determine the user that was
     * effective at the time the page was loaded. If the request header is not present, then that is considered a match
     * with anything.
     *
     * @param request a HttpServletRequest
     * @param user a User
     * @return true if the  request
     */
    public static boolean requestUsernameMatches(HttpServletRequest request, User user)
    {
        String username = request.getHeader(X_AUSERNAME);
        if (username == null)
        {
            return true;
        }

        // JRADEV-20655 The username is uri-encoded before sending to properly handle non-ascii usernames.
        try
        {
            username = URLDecoder.decode(username, "UTF-8");
        }
        catch (UnsupportedEncodingException e) { }

        if (isAnonymous(user))
        {
            return "".equals(username);
        }

        return username.equals(user.getName());
    }

    /**
     * Returns true if the HTTP request contains the {@value #X_PJAX} header.
     *
     * @param request a JiraWebActionSupport
     * @return a boolean
     */
    public static boolean isPjaxRequest(HttpServletRequest request)
    {
        return Boolean.valueOf(request.getHeader(X_PJAX));
    }
}
