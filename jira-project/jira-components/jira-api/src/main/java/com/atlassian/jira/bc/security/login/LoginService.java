package com.atlassian.jira.bc.security.login;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The LoginService keeps track of users login activities.
 *
 * @since v4.0.1
 */
@PublicApi
public interface LoginService
{
    /**
     * This is set into the the request for the last login result
     */
    String LOGIN_RESULT = "com.atlassian.jira.security.login.LoginManager.LoginResult";

    /**
     * This is called to get LoginInfo about a given user.
     *
     * @param userName the name of the user in play.  This MUST not be null.
     *
     * @return a {@link LoginInfo} object
     */
    LoginInfo getLoginInfo(String userName);

    /**
     * @return true if the elevated security check (such as CAPTCHA) is always shown
     */
    boolean isElevatedSecurityCheckAlwaysShown();

    /**
     * This can be called to reset the failed login count of a user
     *
     * @param user               the user to authorise.  This MUST not be null.
     */
    void resetFailedLoginCount(User user);

    /**
     * This can be called to see if an user knows the given password.  Services such as SOAP and XML-RPC may use this to
     * validate a request.
     * <p/>
     * If the user requests elevatedSecurity then this will always fail with LoginReason.AUTHENTICATION_DENIED
     *
     * @param user     the user to authenticate.  This MUST not be null.
     * @param password the password to authenticate against
     *
     * @return true if the user can be authenticated
     */
    LoginResult authenticate(User user, String password);

    /**
     * Log out of JIRA
     * @param request the current servlet request
     * @param response the current servlet response
     */
    void logout(HttpServletRequest request, HttpServletResponse response);

    /**
     * Returns loginProperties needed to render the login gadget or login form.
     *
     * @param remoteUser the currently logged in user
     * @param request the incoming http request which may contain user credentials
     * @return LoginProperties with information to render the login gadget/form
     */
    LoginProperties getLoginProperties(final User remoteUser, final HttpServletRequest request);
}
