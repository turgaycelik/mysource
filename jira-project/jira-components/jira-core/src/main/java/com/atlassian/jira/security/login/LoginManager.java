package com.atlassian.jira.security.login;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.security.login.LoginInfo;
import com.atlassian.jira.bc.security.login.LoginResult;
import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * The LoginManager keeps track of users login activities.
 *
 * @since v4.0
 */
public interface LoginManager
{

    /**
     * This is called to get LoginInfo about a given user.
     *
     * @param userName the name of the user in play.  This MUST not be null.
     *
     * @return a {@link LoginInfo} object
     */
    LoginInfo getLoginInfo(String userName);

    /**
     * This is called to see whether the user has passed an extended security check (such as CAPTCHA)
     *
     * @param httpServletRequest the HTTP request in play
     * @param userName           the name of the user in play.  This MUST not be null.
     *
     * @return true if they have passed the extended security check
     */
    boolean performElevatedSecurityCheck(HttpServletRequest httpServletRequest, String userName);

    /**
     * This is called after a login attempt has been made.  It allows the LoginManager to update information about a
     * users login history.
     *
     * @param httpServletRequest the HTTP request in play
     * @param userName           the name of the user in play.  This MUST not be null.
     * @param loginSuccessful    whether the login attempt was sucessful or not
     *
     * @return the updated {@link LoginInfo} about the user
     */
    LoginInfo onLoginAttempt(HttpServletRequest httpServletRequest, String userName, boolean loginSuccessful);

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
     * This can be called to see if an user knows the given password.  Services such as SOAP and XML-RPC may use this to
     * validate a request.
     * <p/>
     * Calling this method will not cause the request to fail if the user is required to do an elevated
     * security check on normal login.
     *
     *
     * @param user     the user to authenticate.  This MUST not be null.
     * @param password the password to authenticate against
     *
     * @return true if the user can be authenticated
     */
    LoginResult authenticateWithoutElevatedCheck(final User user, final String password);

    /**
     * This is called to see if an autenticated user is allowed to login JIRA in the context of a web request.
     * <p/>
     * At this stage the user has had their username and password authenticated but we need to see if they can be
     * authorised to use JIRA.
     *
     * @param user               the user to authorise.  This MUST not be null.
     * @param httpServletRequest the web request in play
     *
     * @return true if the user can be authorised for login
     */
    boolean authoriseForLogin(@Nonnull ApplicationUser user, HttpServletRequest httpServletRequest);

    /**
     * Gets the set of role strings that are examined by Seraph to decide if a user is authorised to execute a request.
     *
     * @param httpServletRequest the request in play
     * @return a set of roles
     */
    Set<String> getRequiredRoles(final HttpServletRequest httpServletRequest);


    /**
     * This is called to see if an autenticated user is allowed to execute the web request given the required role
     * <p/>
     *
     * @param user               the user to authorise.  This MAY be null.
     * @param httpServletRequest the web request in play
     *
     * @return true if the user can be authorised for this request
     */
    public boolean authoriseForRole(@Nullable ApplicationUser user, HttpServletRequest httpServletRequest, String role);

    /**
     * This is called to logout the current user ourt and destroy their JIRA session
     *
     * @param httpServletRequest  the HTTP request in play
     * @param httpServletResponse the HTTP response in play
     */
    void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse);

    /**
     * @return true if the elevated security check (such as CAPTCHA) is always shown
     */
    boolean isElevatedSecurityCheckAlwaysShown();

    /**
     * This can be called to reset the failed login count of a user
     *
     * @param user the user to authorise.  This MUST not be null.
     */
    void resetFailedLoginCount(User user);
}
