package com.atlassian.jira.lookandfeel.rest;

import com.atlassian.jira.lookandfeel.AutoLookAndFeelManager;
import com.atlassian.jira.security.JiraAuthenticationContext;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Path("auto")
@Produces ({ MediaType.APPLICATION_JSON })
public class AutoLookAndFeelResource
{
    private final AutoLookAndFeelManager autoLookAndFeelManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public AutoLookAndFeelResource(final AutoLookAndFeelManager autoLookAndFeelManager,
            final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.autoLookAndFeelManager = autoLookAndFeelManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @GET
    @Path("justupdated")
    public Response isJustUpdated()
    {
        final boolean justUpdated = autoLookAndFeelManager.isJustUpdated(jiraAuthenticationContext.getLoggedInUser());
        return Response.ok(new IsJustUpdatedBean(justUpdated)).build();
    }

    @PUT
    @Path("restorebackup")
    public Response restoreBackup()
    {
        autoLookAndFeelManager.restoreBackupColorScheme();
        return Response.noContent().build();
    }

    @XmlRootElement
    private static class IsJustUpdatedBean
    {
        @XmlElement
        private boolean isJustUpdated;

        public IsJustUpdatedBean(final boolean isJustUpdated)
        {
            this.isJustUpdated = isJustUpdated;
        }
    }
}
