package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * REST endpoint to get properties needed for the Login form and to check if Login was successful.
 *
 * @since v4.0
 */
@Path ("/login")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class LoginGadgetResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final LoginService loginService;

    public LoginGadgetResource(final JiraAuthenticationContext authenticationContext, final LoginService loginService)
    {
        this.authenticationContext = authenticationContext;
        this.loginService = loginService;
    }

    /**
     * Login should have already occurred by now due to the Seraph Login filter.  We can look at the {@link
     * com.atlassian.jira.bc.security.login.LoginResult} to see how things went.
     *
     * @return 200 successful response always with the {@link LoginProperties} sent back.
     */
    @POST
    public Response checkLogin(@Context HttpServletRequest request)
    {
        final com.atlassian.jira.bc.security.login.LoginProperties loginProperties = loginService.getLoginProperties(authenticationContext.getLoggedInUser(), request);
        return Response.ok(new LoginProperties(loginProperties)).cacheControl(NO_CACHE).build();
    }


    ///CLOVER:OFF

    @XmlRootElement
    public static class LoginProperties
    {
        @XmlElement
        private boolean allowCookies;
        @XmlElement
        private boolean externalUserManagement;
        @XmlElement
        private boolean isPublicMode;
        @XmlElement
        private boolean isElevatedSecurityCheckShown;
        @XmlElement
        private boolean loginSucceeded;
        @XmlElement
        private boolean captchaFailure;
        @XmlElement
        private boolean loginError;
        @XmlElement
        private boolean communicationError;
        @XmlElement
        private String contactAdminLink;

        @XmlElement
        private boolean loginFailedByPermissions;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private LoginProperties()
        {}

        public LoginProperties(com.atlassian.jira.bc.security.login.LoginProperties loginProperties)
        {
            this.loginSucceeded = loginProperties.isLoginSucceeded();
            this.loginError = loginProperties.isLoginError();
            this.communicationError = loginProperties.isCommunicationError();
            this.allowCookies = loginProperties.isAllowCookies();
            this.externalUserManagement = loginProperties.isExternalUserManagement();
            this.isPublicMode = loginProperties.isPublicMode();
            this.isElevatedSecurityCheckShown = loginProperties.isElevatedSecurityCheckShown();
            this.captchaFailure = loginProperties.isCaptchaFailure();
            this.loginFailedByPermissions = loginProperties.getLoginFailedByPermissions();
            this.contactAdminLink = loginProperties.getContactAdminLink();
        }
    }
    ///CLOVER:ON
}

