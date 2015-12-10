package com.atlassian.jira.rest.internal;

import com.atlassian.jira.license.LicenseBannerHelper;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * Resource used by the UI to set a user's remember me setting.
 *
 * @since v6.3
 */
@Produces ( { MediaType.APPLICATION_JSON })
@Consumes ( { MediaType.APPLICATION_JSON })
@Path ("licensebanner")
public class LicenseBannerResource
{
    private final LicenseBannerHelper helper;

    public LicenseBannerResource(final LicenseBannerHelper helper) {this.helper = helper;}

    @POST
    @Path("remindlater")
    public Response remindMeLater()
    {
        helper.remindMeLater();
        return Response.noContent().cacheControl(never()).build();
    }

    @DELETE
    @Path("remindlater")
    public Response removeRemindMeLater()
    {
        helper.clearRemindMe();
        return Response.noContent().cacheControl(never()).build();
    }

    @POST
    @Path("remindnever")
    public Response remindMeNever()
    {
        helper.remindMeNever();
        return Response.noContent().cacheControl(never()).build();
    }
}
