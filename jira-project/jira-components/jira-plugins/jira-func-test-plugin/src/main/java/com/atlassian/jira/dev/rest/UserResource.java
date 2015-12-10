package com.atlassian.jira.dev.rest;

import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * @since v6.0
 */
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON})
@Consumes ({MediaType.APPLICATION_JSON})
@Path ("currentuser")
public class UserResource
{
    private final LoginService loginService;

    public UserResource(LoginService loginService) {this.loginService = loginService;}

    @POST
    @Path("logout")
    public void logout(@Context HttpServletRequest request, @Context HttpServletResponse response)
    {
        loginService.logout(request, response);
    }

    @DELETE
    @Path("session")
    public void session(@Context HttpServletRequest request)
    {
        final HttpSession session = request.getSession(false);
        if (session != null)
        {
            session.invalidate();
        }
    }
}
