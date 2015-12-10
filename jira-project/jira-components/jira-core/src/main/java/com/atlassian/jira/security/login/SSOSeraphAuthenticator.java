package com.atlassian.jira.security.login;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.integration.http.CrowdHttpAuthenticator;
import com.atlassian.crowd.integration.rest.service.factory.RestCrowdHttpAuthenticationFactory;
import com.atlassian.crowd.integration.seraph.v25.CrowdAuthenticator;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;


/**
 * Seraph Authenticator for providing single signon with Crowd.
 *
 * @since v4.3
 */

public class SSOSeraphAuthenticator extends CrowdAuthenticator
{
    private static final String JIRA_USER_DASHBOARD_CURRENT_PAGE = "jira.user.dashboard.current.page";

    public SSOSeraphAuthenticator()
    {
        this(RestCrowdHttpAuthenticationFactory.getAuthenticator());
    }

    public SSOSeraphAuthenticator(CrowdHttpAuthenticator crowdHttpAuthenticator)
    {
        super(crowdHttpAuthenticator);
    }

    protected void logoutUser(HttpServletRequest request)
    {
        HttpSession session = request.getSession();

        // We need to remove this single attribute from the JIRA session since
        // it is used to display the default dashboard or a configured custom user dashboard
        // When a user is logged out we should always just show the default dashboard and not a configured one.
        session.removeAttribute(JIRA_USER_DASHBOARD_CURRENT_PAGE);
    }

    protected Principal getUser(String username)
    {
        final User user = getCrowdService().getUser(username);
        return user != null ? getUserManager().getUserByName(user.getName()) : null;
    }


    /**
     * This is called to refresh the Principal object that has been retreived from the HTTP session.
     * <p/>
     * By default this will called {@link #getUser(String)} again to get a fresh user.
     *
     * @param httpServletRequest the HTTP request in play
     * @param principal          the Principal in play
     * @return a fresh up to date principal
     */
    @Override
    protected Principal refreshPrincipalObtainedFromSession(HttpServletRequest httpServletRequest, Principal principal)
    {
        Principal freshPrincipal = principal;
        if (principal != null && principal.getName() != null)
        {
            if (principal instanceof ApplicationUser)
            {
                freshPrincipal = getUserManager().getUserByKey(((ApplicationUser) principal).getKey());
            }
            else
            {
                freshPrincipal = getUser(principal.getName());
            }
            putPrincipalInSessionContext(httpServletRequest, freshPrincipal);
        }
        return freshPrincipal;
    }

    /**
     * Get a fresh version of the Crowd Read Write service from Pico Container.
     *
     * @return fresh version of the Crowd Read Write service from Pico Container.
     */
    private CrowdService getCrowdService()
    {
        return ComponentAccessor.getComponent(CrowdService.class);
    }

    private UserManager getUserManager() {
        return ComponentAccessor.getUserManager();
    }
}
