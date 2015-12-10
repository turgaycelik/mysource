package com.atlassian.jira.rest.v1.endpoints;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.CorsAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */

@Path("endpoint")
@AnonymousAllowed
@Produces({ MediaType.APPLICATION_JSON })
@CorsAllowed
public class EndPointResource
{
    @GET
    public EndPointInfo getEndPointInfo()
    {
        return new EndPointInfo();
    }

    @XmlRootElement
    public static class EndPointInfo
    {
        @XmlElement
        private String nevilleBartos;
        @XmlElement
        private String robbo;

        public EndPointInfo()
        {
            nevilleBartos = "There's no end points here. Here, there's no end points, alright? End points *no*, Robbo?";
            robbo = "No end points!";
        }
    }
}
