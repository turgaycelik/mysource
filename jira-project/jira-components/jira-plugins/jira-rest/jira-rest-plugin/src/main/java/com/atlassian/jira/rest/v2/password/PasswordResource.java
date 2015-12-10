package com.atlassian.jira.rest.v2.password;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.plugin.user.PasswordPolicyManager;
import com.atlassian.jira.plugin.user.WebErrorMessage;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.collect.ImmutableList;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @since v6.1
 */
@Path ("password")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class PasswordResource
{
    private final PasswordPolicyManager passwordPolicyManager;
    private final UserManager userManager;

    public PasswordResource(PasswordPolicyManager passwordPolicyManager, UserManager userManager)
    {
        this.passwordPolicyManager = passwordPolicyManager;
        this.userManager = userManager;
    }

    /**
     * Returns user-friendly statements governing the system's password policy.
     *
     * @param hasOldPassword whether or not the user will be required to enter their current password.  Use
     *      {@code false} (the default) if this is a new user or if an administrator is forcibly changing
     *      another user's password.
     * @return a response containing a JSON array of the user-facing messages.  If no policy is set, then
     *      this will be an empty list.
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns an array of message strings.
     */
    @GET
    @Path("policy")
    public Response getPasswordPolicy(@QueryParam("hasOldPassword") @DefaultValue("false") boolean hasOldPassword)
    {
        return Response.ok(passwordPolicyManager.getPolicyDescription(hasOldPassword)).build();
    }

    @POST
    @Path("policy/createUser")
    public Response policyCheckCreateUser(PasswordPolicyCreateUserBean bean)
    {
        if (bean == null || isEmpty(bean.getUsername()) || isEmpty(bean.getPassword()))
        {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.ok(snippets(passwordPolicyManager.checkPolicy(bean.getUsername(),
                bean.getDisplayName(), bean.getEmailAddress(), bean.getPassword()))).build();
    }

    @POST
    @Path("policy/updateUser")
    public Response policyCheckUpdateUser(PasswordPolicyUpdateUserBean bean)
    {
        if (bean == null || isEmpty(bean.getUsername()) || isEmpty(bean.getNewPassword()))
        {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        final ApplicationUser existing = userManager.getUserByName(bean.getUsername());
        if (existing == null)
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(snippets(passwordPolicyManager.checkPolicy(existing, bean.getOldPassword(),
                bean.getNewPassword()))).build();
    }

    private static List<String> snippets(Collection<WebErrorMessage> messages)
    {
        final ImmutableList.Builder<String> snippets = ImmutableList.builder();
        for (WebErrorMessage message : messages)
        {
            snippets.add(message.getSnippet());
        }
        return snippets.build();
    }
}
