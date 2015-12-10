package com.atlassian.jira.rest.auth;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.ActionContextKit;
import com.atlassian.jira.bc.security.login.DeniedReason;
import com.atlassian.jira.bc.security.login.LoginReason;
import com.atlassian.jira.bc.security.login.LoginResult;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.util.CookieUtils;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.seraph.config.SecurityConfig;
import com.atlassian.seraph.config.SecurityConfigFactory;
import com.atlassian.seraph.filter.BaseLoginFilter;
import com.atlassian.seraph.filter.PasswordBasedLoginFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static com.atlassian.jira.rest.api.http.CacheControl.never;


/**
 * Implement a REST resource for acquiring a session cookie.
 * @since v4.2
 */
// Implement REST authentication per https://extranet.atlassian.com/display/DEV/Rest+Authentication+Specification+Proposal
@Path ("session")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@AnonymousAllowed
public class Login
{
    private final LoginService loginService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final I18nHelper i18n;

    public Login(final LoginService loginService, final JiraAuthenticationContext jiraAuthenticationContext, final VelocityRequestContextFactory velocityRequestContextFactory, I18nHelper i18n)
    {
        this.loginService = loginService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.i18n = i18n;
    }

    /**
     * Returns information about the currently authenticated user's session. If the caller is not authenticated they
     * will get a 401 Unauthorized status code.
     *
     * @return JSON containing information about the current user
     *
     * @response.representation.200.example
     *     {@link CurrentUser#DOC_EXAMPLE}
     *
     * @response.representation.401.doc
     *      Returned if the caller is not authenticated.
     *
     * @throws java.net.URISyntaxException if the self URI is invalid somehow
     */
    @GET
    public Response currentUser() throws URISyntaxException
    {
        final User user = jiraAuthenticationContext.getLoggedInUser();
        if (user == null)
        {
            throw new NotAuthorisedWebException(ErrorCollection.of(i18n.getText("rest.authentication.no.user.logged.in")));
        }

        final String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
        final String fullPath = baseUrl + "/rest/api/latest/user?username=" + JiraUrlCodec.encode(user.getName());
        final URI selfUri = new URI(fullPath);

        final CurrentUser currentUser = new CurrentUser()
                .userName(user.getName())
                .self(selfUri)
                .loginInfo(new LoginInfo(loginService.getLoginInfo(user.getName())));
        return Response.ok(currentUser).cacheControl(never()).build();
    }

    /**
     * Creates a new session for a user in JIRA. Once a session has been successfully created it can be used to access
     * any of JIRA's remote APIs and also the web UI by passing the appropriate HTTP Cookie header.
     * <p/>
     * Note that it is generally preferrable to use HTTP BASIC authentication with the REST API. However, this resource
     * may be used to mimic the behaviour of JIRA's log-in page (e.g. to display log-in errors to a user).
     *
     * @param credentials the username and password to authenticate
     * @param request injected by Jersey
     * @param response injected by Jersey
     *
     * @return an AuthSuccess entity
     *
     * @request.representation.doc
     *      The POST should contain a username and password of the user being authenticated.
     *
     * @request.representation.qname
     *      credentials
     *      
     * @request.representation.example
     *      {@link AuthParams#DOC_EXAMPLE}
     *
     * @response.representation.200.doc
     *      Returns information about the caller's session if the caller is authenticated.
     *      <p/>
     *      Note that the response contains the <code>Set-Cookie</code> HTTP headers that <b>must</b> be honoured by the
     *      caller. If you are using a cookie-aware HTTP client then it will handle all <code>Set-Cookie</code> headers
     *      automatically. This is important because setting the <code>JSESSIONID</code> cookie alone may not be
     *      sufficient for the authentication to work.
     *
     * @response.representation.200.example
     *      {@link AuthSuccess#DOC_EXAMPLE}
     *
     * @response.representation.403.doc
     *      Returned if the login is denied due to a CAPTCHA requirement, throtting, or any other reason. In case of a
     *      403 status code it is possible that the supplied credentials are valid but the user is not allowed to log in
     *      at this point in time.
     *
     * @response.representation.401.doc
     *      Returned if the login fails due to invalid credentials.
     *
     * @throws com.atlassian.seraph.auth.AuthenticatorException if the DefaultAuthenticator explodes
     */
    @POST
    public Response login(final AuthParams credentials, @Context HttpServletRequest request, @Context HttpServletResponse response)
    {
        // JRADEV-3987: reuse the login code from the Seraph filter
        String loginOutcome = new LoginResourceFilter(credentials.username, credentials.password).login(request, response);
        if (BaseLoginFilter.LOGIN_SUCCESS.equals(loginOutcome))
        {
            final SessionInfo sessionInfo = new SessionInfo(CookieUtils.JSESSIONID, request.getSession().getId());
            final LoginInfo loginInfo = new LoginInfo(loginService.getLoginInfo(credentials.username));
            final AuthSuccess authSuccess = new AuthSuccess(sessionInfo, loginInfo);

            return Response.ok(authSuccess).build();
        }

        LoginResult loginResult = (LoginResult) request.getAttribute(LoginService.LOGIN_RESULT);
        if (loginResult != null && loginResult.getReason() == LoginReason.AUTHENTICATION_DENIED)
        {
            stampDeniedReasonsOnResponse(response, loginResult.getDeniedReasons());
            return Response.status(Response.Status.FORBIDDEN).entity(ErrorCollection.of(i18n.getText("rest.login.denied"))).build();
        }

        response.setHeader("WWW-Authenticate", "JIRA REST POST");
        return Response.status(Response.Status.UNAUTHORIZED).entity(ErrorCollection.of(i18n.getText("rest.login.failed"))).build();
    }

    /**
     * Logs the current user out of JIRA, destroying the existing session, if any.
     *
     * @param request injected by Jersey
     * @param response injected by Jersey
     * @return 401 if the called is not authenticated. NO_CONTENT if the successful.
     *
     * @response.representation.204.doc
     *      Returned if the user was successfully logged out.
     *
     * @response.representation.401.doc
     *      Returned if the caller is not authenticated.
     */
    @DELETE
    public Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response)
    {
        if (jiraAuthenticationContext.getLoggedInUser() == null)
        {
            throw new NotAuthorisedWebException(ErrorCollection.of(i18n.getText("rest.authentication.no.user.logged.in")));
        }
        loginService.logout(request, response);
        // JRADEV-2292 This isn't really necessary for REST, as far as I can tell, but it is needed to make the ActionCleanupDelayFilter happy.
        // It freaks out because it thinks someone isn't cleaning up after themselves.
        ActionContextKit.resetContext();
        return Response.noContent().build();
    }

    /**
     * Stamps the '{@value com.atlassian.jira.bc.security.login.DeniedReason#X_DENIED_HEADER}' header on the response
     * object.
     *
     * @param response a HttpServletResponse
     * @param deniedReasons a Set of DeniedReason
     */
    protected void stampDeniedReasonsOnResponse(HttpServletResponse response, Set<DeniedReason> deniedReasons)
    {
        // JRADEV-2132: set the X_DENIED_HEADER values
        for (DeniedReason reason : deniedReasons)
        {
            response.setHeader(DeniedReason.X_DENIED_HEADER, reason.asString());
        }
    }

    /**
     * Extends the {@link PasswordBasedLoginFilter} from Seraph in order to reuse its login code.
     */
    private class LoginResourceFilter extends PasswordBasedLoginFilter
    {
        private final String username;
        private final String password;

        private LoginResourceFilter(String username, String password)
        {
            this.username = username;
            this.password = password;
        }

        @Override
        protected UserPasswordPair extractUserPasswordPair(HttpServletRequest request)
        {
            return new UserPasswordPair(username, password, false);
        }

        @Override
        protected SecurityConfig getSecurityConfig()
        {
            return SecurityConfigFactory.getInstance();
        }
    }
}
