package com.atlassian.jira.plugin.viewissue.issuelink.rest;

import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.plugin.viewissue.issuelink.RemoteIssueLinkUtils;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * A REST resource that provides functionality for remote issue links.
 *
 * @since v5.0
 */
@AnonymousAllowed
@Path ("remoteIssueLink")
public class RemoteIssueLinkResource
{
    private final static Logger LOG = LoggerFactory.getLogger(RemoteIssueLinkResource.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SimpleLinkManager simpleLinkManager;
    private final RemoteIssueLinkService remoteIssueLinkService;
    private final PluginAccessor pluginAccessor;

    public RemoteIssueLinkResource(
            final JiraAuthenticationContext jiraAuthenticationContext,
            final SimpleLinkManager simpleLinkManager,
            final RemoteIssueLinkService remoteIssueLinkService,
            final PluginAccessor pluginAccessor)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.simpleLinkManager = simpleLinkManager;
        this.remoteIssueLinkService = remoteIssueLinkService;
        this.pluginAccessor = pluginAccessor;
    }

    @GET
    @Path ("/linkType")
    @Produces ({ MediaType.APPLICATION_JSON })
    public Response getIssueLinkTypes(@QueryParam ("issueId") final long issueId)
    {
        final Map<String, Object> params = MapBuilder.<String, Object>newBuilder().add("issueId", issueId).toMap();
        final JiraHelper helper = new JiraHelper(ExecutingHttpRequest.get(), null, params);
        final Collection<SimpleLink> linkTypes = simpleLinkManager.getLinksForSection("create-issue-link-types", jiraAuthenticationContext.getLoggedInUser(), helper);
        final Collection<LinkTypeBean> beans = convertToLinkTypeBeans(linkTypes);
        return Response.ok(beans).cacheControl(never()).build();
    }

    @GET
    @Path ("/render/{id}")
    @Produces (MediaType.TEXT_HTML)
    public Response getRemoteIssueLinkHtml(@PathParam ("id") final long id)
    {
        RemoteIssueLinkService.RemoteIssueLinkResult result = remoteIssueLinkService.getRemoteIssueLink(jiraAuthenticationContext.getLoggedInUser(), id);
        if (!result.isValid())
        {
            ErrorCollection errors = ErrorCollection.of(result.getErrorCollection());
            final Response.Status status = (errors.getStatus() == null) ? Response.Status.BAD_REQUEST : Response.Status.fromStatusCode(errors.getStatus());
            LOG.error("Failed to retrieve the remote issue link '{}': {}", id, errors.toString());
            return Response.status(status).entity(errors).cacheControl(never()).build();
        }
        LOG.debug("Successfully retrieved the remote issue link '{}'", id);

        try
        {
            final String entity = RemoteIssueLinkUtils.getFinalHtml(result.getRemoteIssueLink(), pluginAccessor);
            return Response.ok(entity).cacheControl(never()).build();
        }
        catch (Exception e)
        {
            // Prevent long stack traces appearing in the logs
            LOG.error("Error occurred while generating final HTML for remote issue link: " + getExceptionMessagesForAllCauses(e));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).cacheControl(never()).build();
        }
    }

    private Collection<LinkTypeBean> convertToLinkTypeBeans(final Collection<SimpleLink> linkTypes)
    {
        return Collections2.transform(linkTypes, new Function<SimpleLink, LinkTypeBean>()
        {
            @Override
            public LinkTypeBean apply(final SimpleLink from)
            {
                return new LinkTypeBean(from.getId(), from.getLabel(), from.getUrl(), from.getParams().get("focused-field-name"));
            }
        });
    }

    /**
     * Used when we don't want to print the full stack trace, but we do want to see the list of causes.
     *
     * @param e the root exception
     * @return a string concatenation of the toString() methods of the chain of causes
     */
    private String getExceptionMessagesForAllCauses(Throwable e)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(e.toString());
        e = e.getCause();
        while (e != null)
        {
            sb.append(" Caused by: ").append(e.toString());
            e = e.getCause();
        }

        return sb.toString();
    }
}
