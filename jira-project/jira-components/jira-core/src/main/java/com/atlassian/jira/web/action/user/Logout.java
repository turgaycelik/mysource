/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.user;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import webwork.action.ActionContext;

public class Logout extends JiraWebActionSupport
{
    private static final String LOGOUT = "logout";
    private static final String CONFIRM = "confirm";
    private static final String ALREADY_LOGGED_OUT = "alreadyloggedout";

    private final LoginManager loginManager;
    private final XsrfInvocationChecker xsrfInvocationChecker;
    private final JiraAuthenticationContext authenticationContext;
    private RememberMeService rememberMeService;

    public Logout(final LoginManager loginManager, final XsrfInvocationChecker xsrfInvocationChecker,
            RememberMeService rememberMeService, final JiraAuthenticationContext authenticationContext)
    {
        this.loginManager = loginManager;
        this.xsrfInvocationChecker = xsrfInvocationChecker;
        this.authenticationContext = authenticationContext;
        this.rememberMeService = rememberMeService;
    }

    /**
     * Logs the user out of JIRA and shows the log-out page.
     * For details of the logic behind the XSRF check see, {@link com.atlassian.jira.security.login.JiraLogoutServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
     * @return {@link #LOGOUT} if the user presents a valid xsrf token (his / her session hasn't expired).
     *
     * If the XSRF token is not valid, we return {@link #CONFIRM} when there is an authenticated user; Otherwise,
     * {@link #ALREADY_LOGGED_OUT} is returned.
     */
    @Override
    protected String doExecute()
    {
        final XsrfCheckResult result = xsrfInvocationChecker.checkWebRequestInvocation(ActionContext.getRequest());
        if (result.isValid())
        {
            loginManager.logout(ActionContext.getRequest(), ActionContext.getResponse());
            return LOGOUT;
        }
        if (isUserAuthenticated())
        {
            return CONFIRM;
        }
        else
        {
            return ALREADY_LOGGED_OUT;
        }
    }

    /**
     * <p>Checks the JIRA application properties to see whether log-out confirmation is enabled, logs the user out if
     * necessary, and renders the view accordingly. </p>
     *
     * <p>If the log-out confirmation property is enabled, it will take the user to a log-out confirmation page.
     *
     * Otherwise, it will log the user out and show the log-out page.</p>
     * <p>The log-out confirmation page executes the doExecute() command.
     *
     * For details of the logic behind the XSRF check see, {@link com.atlassian.jira.security.login.JiraLogoutServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
     *
     * @return When there's a valid XSRF token, {@link #CONFIRM} if the application properties indicate that log-out s
     * hould be confirmed for all users, if the user has remember turned on and the application property is set to "cookie" we return also return
     * {@link #CONFIRM}; Otherwise, {@link #LOGOUT} is returned.
     *
     * If the XSRF token is not valid, we return {@link #CONFIRM} when there is an authenticated user; Otherwise, 
     * {@link #ALREADY_LOGGED_OUT} is returned.
     */
    @Override
    public String doDefault()
    {
        final XsrfCheckResult result = xsrfInvocationChecker.checkWebRequestInvocation(ActionContext.getRequest());
        if (result.isValid())
        {
            String logOutPropertyValue = getApplicationProperties().getDefaultBackedString(APKeys.JIRA_OPTION_LOGOUT_CONFIRM);

            if ("always".equals(logOutPropertyValue))
            {
                return CONFIRM;
            }
            else if ("cookie".equals(logOutPropertyValue))
            {
                if (rememberMeService.getRememberMeCookieAuthenticatedUsername(ActionContext.getRequest(), ActionContext.getResponse()) != null)
                {
                    return CONFIRM;
                }
            }
            loginManager.logout(ActionContext.getRequest(), ActionContext.getResponse());
            return LOGOUT;
        }
        else if (isUserAuthenticated())
        {
            return CONFIRM;
        }
        else
        {
            return ALREADY_LOGGED_OUT;
        }        
    }

    private boolean isUserAuthenticated()
    {
        return authenticationContext.getLoggedInUser() != null;
    }
}