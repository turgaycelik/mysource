package com.atlassian.jira.rest.v1.issues;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.IssuePickerSearchService;
import com.atlassian.jira.bc.issue.vote.VoteService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * REST resource to access and modify dashboard information.
 *
 * @since v4.0
 */
@Path ("issues")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class IssueResource
{
    private final JiraAuthenticationContext authContext;
    private final I18nHelper i18n;
    private final IssuePickerSearchService service;
    private final IssueManager issueManager;
    private final IssueService issueService;
    private final ApplicationProperties applicationProperties;
    private final ProjectManager projectManager;
    private final SimpleLinkManager simpleLinkManager;
    private final XsrfTokenGenerator xsrfTokenGenerator;
    private final VoteService voteService;
    private final WatcherManager watcherManager;
    private final IssueWorkflowManager issueWorkflowManager;

    public IssueResource(
            final JiraAuthenticationContext authContext, final I18nHelper i18n,
            final IssuePickerSearchService service, final IssueManager issueManager,
            final IssueService issueService, final ApplicationProperties applicationProperties, final ProjectManager projectManager,
            final SimpleLinkManager simpleLinkManager,
            final XsrfTokenGenerator xsrfTokenGenerator, final VoteService voteService, final WatcherManager watcherManager, final IssueWorkflowManager issueWorkflowManager)
    {
        this.authContext = authContext;
        this.i18n = i18n;
        this.service = service;
        this.issueManager = issueManager;
        this.issueService = issueService;
        this.applicationProperties = applicationProperties;
        this.projectManager = projectManager;
        this.simpleLinkManager = simpleLinkManager;
        this.xsrfTokenGenerator = xsrfTokenGenerator;
        this.voteService = voteService;
        this.watcherManager = watcherManager;
        this.issueWorkflowManager = issueWorkflowManager;
    }

    @Path ("picker")
    public IssuePickerResource getIssuePickerResource()
    {
        return new IssuePickerResource(authContext, i18n, service, issueManager, applicationProperties, projectManager);
    }

    @Path ("{issueId}/ActionsAndOperations")
    public IssueActionsAndOperationsResource getActionsAndOperationsResource(@PathParam ("issueId") String issueId)
    {
        return new IssueActionsAndOperationsResource(authContext, issueManager, i18n, issueId, simpleLinkManager, xsrfTokenGenerator, issueWorkflowManager);
    }

    @POST
    @Path ("{issueId}/voters")
    public Response addVoter(@PathParam ("issueId") Long issueId)
    {
        final User user = authContext.getLoggedInUser();
        if (notAuthenticatedRequest(user))
        {
            return Response.status(Response.Status.UNAUTHORIZED).cacheControl(NO_CACHE).build();
        }
        final IssueService.IssueResult result = issueService.getIssue(user, issueId);
        if (result.getIssue() == null)
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(NO_CACHE).build();
        }
        final VoteService.VoteValidationResult validationResult = voteService.validateAddVote(user, user, result.getIssue());
        if (validationResult.isValid())
        {
            final int votes = voteService.addVote(user, validationResult);
            return Response.ok(new VoteWatchResult(votes)).cacheControl(NO_CACHE).build();
        }
        else
        {
            final ErrorCollection errors = ErrorCollection.Builder.newBuilder().addErrorCollection(validationResult.getErrorCollection()).build();
            return Response.status(Response.Status.BAD_REQUEST).entity(errors).cacheControl(NO_CACHE).build();
        }
    }

    @DELETE
    @Path ("{issueId}/voters")
    public Response removeVoter(@PathParam ("issueId") Long issueId)
    {
        final User user = authContext.getLoggedInUser();
        if (notAuthenticatedRequest(user))
        {
            return Response.status(Response.Status.UNAUTHORIZED).cacheControl(NO_CACHE).build();
        }
        final IssueService.IssueResult result = issueService.getIssue(user, issueId);
        if (result.getIssue() == null)
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(NO_CACHE).build();
        }
        final VoteService.VoteValidationResult validationResult = voteService.validateRemoveVote(user, user, result.getIssue());
        if (validationResult.isValid())
        {
            final int votes = voteService.removeVote(user, validationResult);
            return Response.ok(new VoteWatchResult(votes)).cacheControl(NO_CACHE).build();
        }
        else
        {
            final ErrorCollection errors = ErrorCollection.Builder.newBuilder().addErrorCollection(validationResult.getErrorCollection()).build();
            return Response.status(Response.Status.BAD_REQUEST).entity(errors).cacheControl(NO_CACHE).build();
        }
    }

    @POST
    @Path ("{issueId}/watchers")
    public Response addWatcher(@PathParam ("issueId") Long issueId)
    {
        final User user = authContext.getLoggedInUser();
        if (notAuthenticatedRequest(user))
        {
            return Response.status(Response.Status.UNAUTHORIZED).cacheControl(NO_CACHE).build();
        }
        final IssueService.IssueResult result = issueService.getIssue(user, issueId);
        if (result.getIssue() == null)
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(NO_CACHE).build();
        }
        if (!watcherManager.isWatchingEnabled() || notAuthenticatedRequest(user))
        {
            return Response.status(Response.Status.BAD_REQUEST).cacheControl(NO_CACHE).build();
        }
        watcherManager.startWatching(user, result.getIssue());
        final int watcherCount = watcherManager.getWatcherCount(result.getIssue());
        return Response.ok(new VoteWatchResult(watcherCount)).cacheControl(NO_CACHE).build();
    }

    @DELETE
    @Path ("{issueId}/watchers")
    public Response removeWatcher(@PathParam ("issueId") Long issueId)
    {
        final User user = authContext.getLoggedInUser();
        if (notAuthenticatedRequest(user))
        {
            return Response.status(Response.Status.UNAUTHORIZED).cacheControl(NO_CACHE).build();
        }
        final IssueService.IssueResult result = issueService.getIssue(user, issueId);
        if (result.getIssue() == null)
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(NO_CACHE).build();
        }
        if (!watcherManager.isWatchingEnabled())
        {
            return Response.status(Response.Status.BAD_REQUEST).cacheControl(NO_CACHE).build();
        }
        watcherManager.stopWatching(user, result.getIssue());
        final int watcherCount = watcherManager.getWatcherCount(result.getIssue());
        return Response.ok(new VoteWatchResult(watcherCount)).cacheControl(NO_CACHE).build();
    }

    @XmlRootElement
    public static class VoteWatchResult
    {
        @XmlElement
        private int count = 0;

        private VoteWatchResult() { }

        public VoteWatchResult(final int count)
        {
            this.count = count;
        }

        public int voteCount()
        {
            return count;
        }
    }

     private boolean notAuthenticatedRequest(final User user)
     {
         return user == null;
     }
}
