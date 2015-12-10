package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST endpoint to retrieve the instance name html.
 *
 * @since v4.0
 */
@Path("/instanceName")
@AnonymousAllowed
@Produces({MediaType.TEXT_PLAIN})
public class InstanceNameResource
{
    private final ApplicationProperties applicationProperties;

    public InstanceNameResource(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @GET
    public Response getIntro() throws Exception
    {
        final String html = applicationProperties.getText(APKeys.JIRA_TITLE);

        return Response.ok(html).cacheControl(NO_CACHE).build();
    }

}
