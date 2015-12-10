package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.concurrent.Barrier;
import com.atlassian.jira.concurrent.BarrierFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

/**
 * Backdoor for manipulating barriers remotely.
 *
 * @since v5.2
 */
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@Path ("/barrier")
public class BarrierBackdoor
{
    private final BarrierFactory barrierFactory;

    public BarrierBackdoor(BarrierFactory barrierFactory)
    {
        this.barrierFactory = barrierFactory;
    }

    /**
     * Raises the barrier with the given name.
     */
    @POST
    @Path ("raise")
    public void raise(@QueryParam ("barrierName") String name)
    {
        barrierWithName(name).raise();
    }

    /**
     * Lowers the barrier with the given name.
     */
    @POST
    @Path ("lower")
    public void lower(@QueryParam ("barrierName") String name)
    {
        barrierWithName(name).lower();
    }

    private Barrier barrierWithName(String name)
    {
        if (StringUtils.isEmpty(name))
        {
            throw new WebApplicationException(404);
        }

        return barrierFactory.getBarrier(name);
    }
}
