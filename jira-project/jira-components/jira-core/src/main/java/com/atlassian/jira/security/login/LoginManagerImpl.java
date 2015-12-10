package com.atlassian.jira.security.login;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.security.login.CaptchaChallengeRequired;
import com.atlassian.jira.bc.security.login.DeniedReason;
import com.atlassian.jira.bc.security.login.LoginInfo;
import com.atlassian.jira.bc.security.login.LoginInfoImpl;
import com.atlassian.jira.bc.security.login.LoginLoggers;
import com.atlassian.jira.bc.security.login.LoginReason;
import com.atlassian.jira.bc.security.login.LoginResult;
import com.atlassian.jira.bc.security.login.LoginResultImpl;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.event.user.LoginEvent;
import com.atlassian.jira.event.user.LogoutEvent;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.auth.AuthorisationManager;
import com.atlassian.jira.servlet.JiraCaptchaService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.log.Log4jKit;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.seraph.auth.Authenticator;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.config.SecurityConfigFactory;
import com.google.common.collect.Sets;
import com.octo.captcha.service.CaptchaServiceException;
import org.apache.log4j.Logger;
import webwork.action.ActionContext;
import webwork.action.factory.SessionMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.lang.String.format;

/**
 * Implementation of {@link LoginManager}
 *
 * @since v4.0.1
 */
public class LoginManagerImpl implements LoginManager
{
    private static final Logger log = Logger.getLogger(LoginManagerImpl.class);
    
    private static final Logger loggerSecurityEvents = LoginLoggers.LOGIN_SECURITY_EVENTS;

    private static final String AUTHORISED_FAILURE = "com.atlassian.jira.security.login.LoginManager.AUTHORISED_FAILURE";
    private static final String ELEVATED_SECURITY_FAILURE = "com.atlassian.jira.security.login.LoginManager.ELEVATED_SECURITY_FAILURE";
    private static final String OS_CAPTCHA = "os_captcha";

    private final LoginStore loginStore;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final CrowdService crowdService;
    private final StaticDependencies staticDependencies;
    private final JiraCaptchaService jiraCaptchaService;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final EventPublisher eventPublisher;
    private final AuthorisationManager authorisationManager;


    public LoginManagerImpl(final LoginStore loginStore, final JiraAuthenticationContext jiraAuthenticationContext, final CrowdService crowdService, final JiraCaptchaService jiraCaptchaService, VelocityRequestContextFactory velocityRequestContextFactory, EventPublisher eventPublisher, AuthorisationManager authorisationManager)
    {
        this(new InternalStaticDependencies(crowdService), loginStore, jiraAuthenticationContext, crowdService, jiraCaptchaService, velocityRequestContextFactory, eventPublisher, authorisationManager);
    }

    LoginManagerImpl(final StaticDependencies staticDependencies, final LoginStore loginStore, final JiraAuthenticationContext jiraAuthenticationContext, final CrowdService crowdService, final JiraCaptchaService jiraCaptchaService, VelocityRequestContextFactory velocityRequestContextFactory, EventPublisher eventPublisher, AuthorisationManager authorisationManager)
    {
        this.loginStore = notNull("loginStore", loginStore);
        this.jiraAuthenticationContext = notNull("jiraAuthenticationContext", jiraAuthenticationContext);
        this.crowdService = notNull("crowdService", crowdService);
        this.jiraCaptchaService = notNull("jiraCaptchaService", jiraCaptchaService);
        this.staticDependencies = notNull("staticDependencies", staticDependencies);
        this.velocityRequestContextFactory = notNull("velocityRequestContextFactory", velocityRequestContextFactory);
        this.eventPublisher = eventPublisher;
        this.authorisationManager = authorisationManager;
    }

    public LoginInfo getLoginInfo(String userName)
    {
        final User user = crowdService.getUser(userName);
        if (user == null)
        {
            return null;
        }
        final LoginInfo storedLoginInfo = loginStore.getLoginInfo(user);
        return tweakLoginInfo(storedLoginInfo);
    }

    public boolean performElevatedSecurityCheck(final HttpServletRequest httpServletRequest, final String userName)
    {
        // start fresh on this attr
        httpServletRequest.removeAttribute(ELEVATED_SECURITY_FAILURE);

        LoginInfo loginInfo = getLoginInfo(userName);
        if (loginInfo == null)
        {
            return true;
        }
        if (!loginInfo.isElevatedSecurityCheckRequired())
        {
            // if they don't require security elevation then we just say they passed because...well...they have!
            return true;
        }

        // ok they have failed a few times to authenticate so we up the ante and make sure they have answered a CAPTCHA question
        final String captcha = httpServletRequest.getParameter(OS_CAPTCHA);
        final String sessionId = httpServletRequest.getSession(true).getId();

        Boolean isResponseCorrect;
        try
        {
            isResponseCorrect = jiraCaptchaService.getImageCaptchaService().validateResponseForID(sessionId, captcha);
        }
        catch (CaptchaServiceException e)
        {
            isResponseCorrect = false;
        }
        final boolean captchOK = !(isResponseCorrect != null && !isResponseCorrect);
        if (!captchOK)
        {
            //
            // because of the call back nature of Seraph we cant tell later the difference between elevated failure / authentication / authorisation failure
            // so we stuff this away to be able to tell
            //
            httpServletRequest.setAttribute(ELEVATED_SECURITY_FAILURE, true);
        }

        return captchOK;
    }

    @Override
    public boolean authoriseForLogin(@Nonnull final ApplicationUser user, final HttpServletRequest httpServletRequest)
    {
        notNull("user", user);

        httpServletRequest.removeAttribute(AUTHORISED_FAILURE);

        final boolean authorised = authorisationManager.authoriseForLogin(user, httpServletRequest);
        if (!authorised)
        {
            //
            // because of the call back nature of Seraph we cant tell later the difference between elevated failure / authentication / authorisation failure
            // so we stuff this away to be able to tell
            //
            httpServletRequest.setAttribute(AUTHORISED_FAILURE, true);
        }
        return authorised;
    }

    @Override
    public Set<String> getRequiredRoles(HttpServletRequest httpServletRequest)
    {
        return authorisationManager.getRequiredRoles(httpServletRequest);
    }

    @Override
    public boolean authoriseForRole(@Nullable ApplicationUser user, HttpServletRequest httpServletRequest, String role)
    {
        httpServletRequest.removeAttribute(AUTHORISED_FAILURE);

        final boolean authorised = authorisationManager.authoriseForRole(user, httpServletRequest, role);
        if (!authorised)
        {
            //
            // because of the call back nature of Seraph we cant tell later the difference between elevated failure / authentication / authorisation failure
            // so we stuff this away to be able to tell
            //
            httpServletRequest.setAttribute(AUTHORISED_FAILURE, true);
        }
        return authorised;
    }

    public LoginResult authenticate(final User user, final String password)
    {
        notNull("user", user);

        LoginReason reason;

        final String userName = user.getName();
        LoginInfo loginInfo = getLoginInfo(userName);
        if (loginInfo.isElevatedSecurityCheckRequired())
        {
            reason = LoginReason.AUTHENTICATION_DENIED;
        }
        else
        {
            reason = staticDependencies.authenticate(user, password) ? LoginReason.OK : LoginReason.AUTHENTICATED_FAILED;
        }
        loginInfo = tweakLoginInfo(recordLoginAttempt(user, reason == LoginReason.OK));

        //
        // this call is not done from Seraph but rather from areas like the RPC SOAP plugin
        //
        logSecurityEvents(user, loginInfo, reason);

        return new LoginResultImpl(reason, loginInfo, user.getName());
    }

    public LoginResult authenticateWithoutElevatedCheck(final User user, final String password)
    {
        notNull("user", user);

        LoginReason reason;

        final String userName = user.getName();
        reason = staticDependencies.authenticate(user, password) ? LoginReason.OK : LoginReason.AUTHENTICATED_FAILED;
        LoginInfo loginInfo = tweakLoginInfo(recordLoginAttempt(user, reason == LoginReason.OK));

        //
        // this call is not done from Seraph but rather from areas like the RPC SOAP plugin
        //
        logSecurityEvents(user, loginInfo, reason);

        return new LoginResultImpl(reason, loginInfo, userName);

    }

    public LoginInfo onLoginAttempt(final HttpServletRequest httpServletRequest, final String userName, final boolean loginSuccessful)
    {
        final User user = crowdService.getUser(userName);
        if (user == null)
        {
            return null;
        }
        final LoginInfo loginInfo = tweakLoginInfo(recordLoginAttempt(user, loginSuccessful));

        LoginReason reason = loginSuccessful ? LoginReason.OK : LoginReason.AUTHENTICATED_FAILED;
        if (!loginSuccessful)
        {
            if (httpServletRequest.getAttribute(ELEVATED_SECURITY_FAILURE) != null)
            {
                // See above as to why I know this is true!
                reason = LoginReason.AUTHENTICATION_DENIED;
            }
            else if (httpServletRequest.getAttribute(AUTHORISED_FAILURE) != null)
            {
                // See above as to why I know this is true!
                reason = LoginReason.AUTHORISATION_FAILED;
            }
        }
        recordLoginResultInRequest(httpServletRequest, new LoginResultImpl(reason, loginInfo, userName, getLoginDeniedReasons(httpServletRequest)));

        //
        // this code is definitely called via  a Seraph path and is the best place for that type of logging
        //
        logSecurityEvents(user, loginInfo, reason);

        return loginInfo;
    }

    public void logout(final HttpServletRequest request, final HttpServletResponse response)
    {
        notNull("request", request);
        notNull("response", response);

        final User loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        String userName = loggedInUser == null ? "unknown" : loggedInUser.getName();

        /**
         * This code is convaluted and overly active but its what the old JIRA common-logout jsp used to do.
         * We may want to revist it in the future.
         */
        final HttpSession currentSession = request.getSession(false);
        if (currentSession != null)
        {
            currentSession.invalidate();
        }
        final HttpSession newSession = request.getSession(true);
        ActionContext.setSession(new SessionMap(newSession)); // this is so because the old session is still floating around, and dies when you try to access it
        try
        {
            staticDependencies.getAuthenticator().logout(request, response);
        }
        catch (AuthenticatorException e)
        {
            // really? the method signature says it will happen but I doubt it
            ///CLOVER:OFF
            log.error(e);
            ///CLOVER:ON
        }
        loggerSecurityEvents.info("The user '" + userName + "' has logged out.");

        jiraAuthenticationContext.clearLoggedInUser();
        request.setAttribute("jira.logout.page.executed",Boolean.TRUE);

        eventPublisher.publish(new LogoutEvent(loggedInUser));
    }

    public boolean isElevatedSecurityCheckAlwaysShown()
    {
        return getMaxAuthenticationAttemptsAllowed() <= 0;
    }

    public void resetFailedLoginCount(final User user)
    {
        loginStore.resetFailedLoginCount(user);
    }

    /**
     * Examines the HttpServletRequest, and determines the DeniedReason's that may have cause authentication to be
     * denied by looking at the {@link #ELEVATED_SECURITY_FAILURE} attribute. Currently the only reason why this
     * attribute would be set is because a user is required to pass a CAPTCHA challenge.
     *
     * @return a Set<DeniedReason>, containing the reasons that may have caused authentication to be denied
     * @param request a HttpServletRequest
     *
     * @see #ELEVATED_SECURITY_FAILURE
     */
    protected Set<DeniedReason> getLoginDeniedReasons(HttpServletRequest request)
    {
        Set<DeniedReason> loginDeniedDueTo = Sets.newHashSet();
        if (request.getAttribute(ELEVATED_SECURITY_FAILURE) != null)
        {
            String loginJsp = format("%s/login.jsp", velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl());
            loginDeniedDueTo.add(new CaptchaChallengeRequired(loginJsp));
        }
        
        return loginDeniedDueTo;
    }

    private void logSecurityEvents(final User user, final LoginInfo loginInfo, final LoginReason reason)
    {
        final String userName = user != null? user.getName() : "null";
        if (reason == LoginReason.AUTHENTICATION_DENIED)
        {
            loggerSecurityEvents.warn("The user '" + userName + "' is required to answer a CAPTCHA elevated security check.  Failure count equals " + loginInfo.getCurrentFailedLoginCount());
        }
        else if (reason == LoginReason.AUTHENTICATED_FAILED)
        {
            loggerSecurityEvents.warn("The user '" + userName + "' has FAILED authentication.  Failure count equals " + loginInfo.getCurrentFailedLoginCount());
        }
        else if (reason == LoginReason.AUTHORISATION_FAILED)
        {
            loggerSecurityEvents.warn("The user '" + userName + "' is NOT AUTHORIZED to perform this request");
        }
        else
        {
            Log4jKit.putUserToMDC(userName);
            
            loggerSecurityEvents.info("The user '" + userName + "' has PASSED authentication.");
            eventPublisher.publish(new LoginEvent(user));
        }
    }

    private LoginInfo recordLoginAttempt(final User user, final boolean loginSuccessful)
    {
        return loginStore.recordLoginAttempt(user, loginSuccessful);
    }

    private LoginResultImpl recordLoginResultInRequest(final HttpServletRequest httpServletRequest, final LoginResultImpl loginResult)
    {
        httpServletRequest.setAttribute(LoginService.LOGIN_RESULT, loginResult);
        return loginResult;
    }

    /**
     * This will tweak the LoginInfo object such that it sets elevatedSecurityCheckRequired if indeed it is needed
     *
     * @param storedLoginInfo the store LoginInfo from the store
     *
     * @return a new copied and tweaked LoginInfo object
     */
    private LoginInfo tweakLoginInfo(final LoginInfo storedLoginInfo)
    {
        long maxLoginAttempts = getMaxAuthenticationAttemptsAllowed();
        boolean elevatedSecurityCheckRequired = nvl(storedLoginInfo.getCurrentFailedLoginCount(), 0) >= maxLoginAttempts;

        return new LoginInfoImpl(storedLoginInfo, elevatedSecurityCheckRequired);
    }

    private long nvl(final Long value, final long defaultValue)
    {
        return value == null ? defaultValue : value;
    }

    private long getMaxAuthenticationAttemptsAllowed()
    {
        return loginStore.getMaxAuthenticationAttemptsAllowed();
    }


    /**
     * The only purpose of this interface is to allow a static call to be factored out for testing
     */
    interface StaticDependencies
    {
        Authenticator getAuthenticator();

        boolean authenticate(User user, String password);
    }

    ///CLOVER:OFF
    private static class InternalStaticDependencies implements StaticDependencies
    {
        final CrowdService crowdService;
        private InternalStaticDependencies(final CrowdService crowdService)
        {
            this.crowdService = crowdService;
        }

        public Authenticator getAuthenticator()
        {
            return SecurityConfigFactory.getInstance().getAuthenticator();
        }

        public boolean authenticate(final User user, final String password)
        {
            try
            {
                return crowdService.authenticate(user.getName(), password) != null;
            }
            catch (FailedAuthenticationException e)
            {
                return false;
            }
        }
    }
    ///CLOVER:ON
}
