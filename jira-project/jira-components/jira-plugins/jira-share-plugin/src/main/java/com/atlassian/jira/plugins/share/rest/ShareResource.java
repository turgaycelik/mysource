package com.atlassian.jira.plugins.share.rest;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugins.share.ShareBean;
import com.atlassian.jira.plugins.share.ShareService;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * A REST resource that allows sharing various entities (Issues, Searchers, JQL) via e-mail.
 *
 * @since v5.0
 */
@Path("/")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class ShareResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final UserPreferencesManager userPreferencesManager;
    private final SearchRequestService searchRequestService;
    private final ShareService shareService;
    private final IssueService issueService;

    public ShareResource(
            @ComponentImport final JiraAuthenticationContext authenticationContext,
            @ComponentImport final UserPreferencesManager userPreferencesManager,
            @ComponentImport final SearchRequestService searchRequestService,
            final ShareService shareService,
            @ComponentImport final IssueService issueService)
    {
        this.authenticationContext = authenticationContext;
        this.userPreferencesManager = userPreferencesManager;
        this.searchRequestService = searchRequestService;
        this.shareService = shareService;
        this.issueService = issueService;
    }

    /**
     * Shares an issue via email with a number of users and email-addressees
     *
     * @param issueKey the issue to create the remote issue link for
     * @param shareBean a shareBean to share an issue
     * @return a 204 HTTP status if everything goes well, otherwise the Response contains the error details
     *
     * @response.representation.200.doc
     *      Returned if the issue was shared successfully.
     *
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     *
     * @response.representation.400.doc
     *      Returned if the calling user does not have permission to share the issue or the issue was not found
     */
    @POST
    @Path ("issue/{key}")
    public Response shareIssue(@PathParam ("key") final String issueKey, final ShareBean shareBean)
    {
        ApplicationUser user = authenticationContext.getUser();
        IssueService.IssueResult issueResult = issueService.getIssue(ApplicationUsers.toDirectoryUser(user), issueKey);
        if (!issueResult.isValid())
        {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorCollection.of(issueResult.getErrorCollection()))
                    .cacheControl(never())
                    .build();
        }

        //See JRADEV-20654
        //This is to enable the ability to share issue with yourself even
        //when the "My Changes" preference is set to "Do not notify me"
        Set<String> userNames = shareBean.getUsernames();
        if (user != null &&
                !userPreferencesManager.getExtendedPreferences(user).getBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES) &&
                userNames.contains(user.getName()))
        {
            userNames.remove(user.getName());
            shareBean.getEmails().add(user.getEmailAddress());
        }

        ShareService.ValidateShareIssueResult result = shareService.validateShareIssue(user, shareBean, issueResult.getIssue());
        if (!result.isValid())
        {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorCollection.of(result.getErrorCollection()))
                    .cacheControl(never())
                    .build();
        }

        shareService.shareIssue(result);
        return Response.ok().cacheControl(never()).build();
    }

    /**
     * Shares a saved filter via e-mail.
     *
     * @param id the id of the filter being looked up
     * @param shareBean A {@link ShareBean} containing information for the share operation
     * @return a Response
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a 200 if a filter was successfully shared
     *
     *
     * @response.representation.400.doc
     *     Returned if there is a problem looking up the filter given the id or if no users or e-mail to share with were
     *     provided or if the user sharing doesn't have permission to browse users in JIRA
     */
    @POST
    @Path ("filter/{id}")
    public Response shareSearchRequest(@PathParam ("id") Long id, final ShareBean shareBean)
    {
        final ApplicationUser user = authenticationContext.getUser();
        final JiraServiceContextImpl context = new JiraServiceContextImpl(user);
        final SearchRequest filter = searchRequestService.getFilter(context, id);
        if (filter == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorCollection.of(context.getErrorCollection())).cacheControl(never()).build();
        }

        ShareService.ValidateShareSearchRequestResult result = shareService.validateShareSearchRequest(user, shareBean, filter);
        if (!result.isValid())
        {
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorCollection.of(result.getErrorCollection())).cacheControl(never()).build();
        }

        shareService.shareSearchRequest(result);
        return Response.ok().cacheControl(never()).build();
    }

    /**
     * Shares a jql search via e-mail.
     *
     * @param shareBean A {@link ShareBean} containing information for the share operation
     * @return a Response
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a 200 if an search via jql was successfully shared
     *
     *
     * @response.representation.400.doc
     *     Returned if no jql query was provided or no users or e-mails to share with were provided or
     *     if the user sharing doesn't have permission to browse users in JIRA
     */
    @POST
    @Path ("search")
    public Response shareSearch(final ShareBean shareBean)
    {
        final ApplicationUser user = authenticationContext.getUser();
        final ShareService.ValidateShareSearchRequestResult result = shareService.validateShareSearchRequest(
                user, shareBean, null);
        if (!result.isValid())
        {
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorCollection.of(result.getErrorCollection())).cacheControl(never()).build();
        }

        shareService.shareSearchRequest(result);
        return Response.ok().cacheControl(never()).build();
    }
}
