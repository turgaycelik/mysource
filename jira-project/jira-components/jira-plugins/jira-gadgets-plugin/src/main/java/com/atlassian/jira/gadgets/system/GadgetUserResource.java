package com.atlassian.jira.gadgets.system;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * REST endpoint to get the currently logged in user.
 *
 * @since v4.0
 */
@Path ("/currentUser")
@Produces ({ MediaType.APPLICATION_JSON })
public class GadgetUserResource
{
    private final JiraAuthenticationContext authenticationContext;

    public GadgetUserResource(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    @GET
    public Response getCurrentUser()
    {
        // this should never be null
        final User user = authenticationContext.getLoggedInUser();

        if (user == null)
        {

            return Response.status(401).cacheControl(NO_CACHE).build();
        }

        return Response.ok(new UserBean(user.getName(), user.getDisplayName(), user.getEmailAddress())).cacheControl(NO_CACHE).build();
    }

    ///CLOVER:OFF
    @XmlRootElement
    public static class UserBean
    {
        @XmlElement
        private String username;
        @XmlElement
        private String fullName;
        @XmlElement
        private String email;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private UserBean()
        {}

        UserBean(String username, String fullName, String email)
        {
            this.username = username;
            this.fullName = fullName;
            this.email = email;
        }
    }
///CLOVER:ON
}
