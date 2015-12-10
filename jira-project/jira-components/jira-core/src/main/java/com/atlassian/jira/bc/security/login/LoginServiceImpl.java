package com.atlassian.jira.bc.security.login;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.seraph.auth.AuthenticationErrorType;
import com.atlassian.seraph.filter.BaseLoginFilter;
import com.atlassian.seraph.filter.LoginFilterRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Implementation of LoginManager
 *
 * @since v4.0.1
 */
public class LoginServiceImpl implements LoginService
{
    private final LoginManager loginManager;
    private final ApplicationProperties applicationProperties;
    private final UserManager userManager;
    private final JiraContactHelper contactHelper;
    private final JiraAuthenticationContext authenticationContext;

    public LoginServiceImpl(LoginManager loginManager, ApplicationProperties applicationProperties,
            UserManager userManager, JiraContactHelper contactHelper, JiraAuthenticationContext authenticationContext)
    {
        this.contactHelper = contactHelper;
        this.authenticationContext = authenticationContext;
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.loginManager = notNull("loginManager", loginManager);
        this.userManager = notNull("userManager", userManager);
    }

    public LoginInfo getLoginInfo(final String userName)
    {
        return loginManager.getLoginInfo(userName);
    }

    public boolean isElevatedSecurityCheckAlwaysShown()
    {
        return loginManager.isElevatedSecurityCheckAlwaysShown();
    }

    public void resetFailedLoginCount(final User user)
    {
        loginManager.resetFailedLoginCount(user);
    }

    public LoginResult authenticate(final User user, final String password)
    {
        return loginManager.authenticate(user, password);
    }

    public void logout(final HttpServletRequest request, final HttpServletResponse response)
    {
        loginManager.logout(request, response);
    }

    public LoginProperties getLoginProperties(final User remoteUser, final HttpServletRequest request)
    {
        notNull("request", request);

        // see loginform.jsp for example of how this needs to work
        final LoginResult lastLoginResult = (LoginResult) request.getAttribute(LoginService.LOGIN_RESULT);

        final LoginInfo loginInfo = lastLoginResult == null ? null : lastLoginResult.getLoginInfo();

        final boolean loginSucceeded = remoteUser != null;
        final boolean loginError = BaseLoginFilter.LOGIN_ERROR.equals(LoginFilterRequest.getAuthenticationStatus(request));
        final boolean communicationError = AuthenticationErrorType.CommunicationError.equals(LoginFilterRequest.getAuthenticationErrorType(request));
        final boolean captchaFailure = (lastLoginResult != null && lastLoginResult.getReason() == LoginReason.AUTHENTICATION_DENIED);
        final boolean loginFailedCausedByPermissions = (lastLoginResult != null && lastLoginResult.getReason() == LoginReason.AUTHORISATION_FAILED);
        final boolean isElevatedSecurityCheckShown = isElevatedSecurityCheckShown(loginInfo);

        if (LoginLoggers.LOGIN_GADGET_LOG.isDebugEnabled())
        {
            LoginLoggers.LOGIN_GADGET_LOG.debug("Gadget login called with lastLoginResult : " + String.valueOf(lastLoginResult));
        }

        return LoginProperties.builder().
                loginSucceeded(loginSucceeded).
                loginError(loginError).
                communicationError(communicationError).
                allowCookies(applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOW_COOKIES)).
                externalPasswordManagement(!userManager.hasPasswordWritableDirectory()).
                externalUserManagement(applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT)).
                isPublicMode(isPublicMode()).
                isElevatedSecurityCheckShown(isElevatedSecurityCheckShown).
                captchaFailure(captchaFailure).
                loginFailedByPermissions(loginFailedCausedByPermissions).
                contactAdminLink(getContactLink(request)).
                build();
    }

    private String getContactLink(HttpServletRequest request)
    {
        return contactHelper.getAdministratorContactLinkHtml(JiraUrl.constructBaseUrl(request), authenticationContext.getI18nHelper());
    }

    boolean isPublicMode()
    {
        return JiraUtils.isPublicMode();
    }

    private boolean isElevatedSecurityCheckShown(final LoginInfo loginInfo)
    {
        return isElevatedSecurityCheckAlwaysShown() || (loginInfo != null && loginInfo.isElevatedSecurityCheckRequired());
    }
}
