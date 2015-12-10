package com.atlassian.jira.dev.reference.plugin;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Simple REST Resource. It is not an end-point as there is no such thing in proper REST :-P
 * 
 * @since v4.3
 */
@Path ("endpoint")
public class ThisIsNotAnRestEndPoint
{
    @GET
    @AnonymousAllowed
    @Produces ({ MediaType.APPLICATION_JSON })
    public Response invoke()
    {
        return Response.ok(new ReturnValue()).cacheControl(never()).build();
    }

    @XmlRootElement (name = "result")
    public static class ReturnValue
    {
        @XmlElement
        private Boolean endpoint;

        public ReturnValue()
        {
            this.endpoint = false;
        }
    }

    private static javax.ws.rs.core.CacheControl never()
    {
        javax.ws.rs.core.CacheControl cacheNever = new javax.ws.rs.core.CacheControl();
        cacheNever.setNoStore(true);
        cacheNever.setNoCache(true);

        return cacheNever;
    }

}
