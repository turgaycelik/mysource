package com.atlassian.jira.rest.v1.admin;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.user.UserAdminHistoryManager;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.CorsAllowed;
import com.atlassian.plugins.rest.common.security.XsrfCheckFailedException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * REST endpoint for setting the user's administration page history. Each time a link with the class "admin-item-link"
 * is clicked, an ajax request is made to this endpoint to update the admin page history. The listener is defined in
 * jira-global.js and attached in header.js
 *
 * @since v4.1
 */

@Path ("adminHistory")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED })
@Produces ({ MediaType.APPLICATION_JSON })
@CorsAllowed
public class AdminHistoryResource
{
    private final UserAdminHistoryManager adminHistoryManager;
    private final JiraAuthenticationContext authenticationContext;
    private final XsrfInvocationChecker xsrfChecker;

    public AdminHistoryResource(UserAdminHistoryManager adminHistoryManager, JiraAuthenticationContext authenticationContext, XsrfInvocationChecker xsrfChecker)
    {
        this.adminHistoryManager = adminHistoryManager;
        this.authenticationContext = authenticationContext;
        this.xsrfChecker = xsrfChecker;
    }

    @POST
    @Path ("store")
    public void setHistory(AdminHistoryLink key)
    {
        final XsrfCheckResult xsrfCheckResult = xsrfChecker.checkWebRequestInvocation(ExecutingHttpRequest.get());
        if (xsrfCheckResult.isRequired() && !xsrfCheckResult.isValid())
        {
            throw new XsrfCheckFailedException();
        }

        final User user = authenticationContext.getLoggedInUser();

        adminHistoryManager.addAdminPageToHistory(user, key.id, key.url);
    }

    /**
     *
     */
    @XmlRootElement
    public static class AdminHistoryLink
    {
        // The link id of the admin link
        @XmlElement
        private String id;
        // The url of the link (break ties between sections)
        @XmlElement
        private String url;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private AdminHistoryLink()
        {}

        AdminHistoryLink(String id, String url)
        {
            this.id = id;
            this.url = url;
        }
    }

}
