package com.atlassian.jira.rest.v2.issue;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.issue.vote.VoteService;
import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.bc.issue.watcher.WatchingDisabledException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.NotificationJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.WorklogJsonBean;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.notification.AdhocNotificationService;
import com.atlassian.jira.notification.NotificationBuilder;
import com.atlassian.jira.rest.api.issue.RemoteIssueLinkCreateOrUpdateRequest;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.api.util.StringList;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.watcher.WatcherOps;
import com.atlassian.jira.rest.v2.issue.worklog.WorklogResource;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.Transformed;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.mail.MailFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import com.opensymphony.workflow.loader.ActionDescriptor;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import static com.atlassian.jira.notification.AdhocNotificationServiceImpl.makeBuilder;
import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since v4.2
 */
@Path ("issue")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class IssueResource
{
    private IssueFinder issueFinder;
    private UserManager userManager;
    private IssueWorkflowManager issueWorkflowManager;

    private JiraAuthenticationContext authContext;
    private VoteService voteService;

    private I18nHelper i18n;

    private WatcherOps watcherOps;
    private WatcherService watcherService;
    private AdhocNotificationService notificationService;

    private BeanBuilderFactory beanBuilderFactory;
    private ContextUriInfo contextUriInfo;
    private AssignIssueResource assignIssueResource;
    private CreateIssueResource createIssueResource;
    private DeleteIssueResource deleteIssueResource;
    private UpdateIssueResource updateIssueResource;
    private RemoteIssueLinkResource remoteIssueLinkResource;
    private WorklogResource worklogResource;
    private CommentResource commentResource;
    private JiraBaseUrls jiraBaseUrls;

    /**
     * This constructor needed by doclet.
     */
    @SuppressWarnings ({ "UnusedDeclaration" })
    private IssueResource()
    {
    }

    public IssueResource
            (
                    final JiraAuthenticationContext authContext,
                    final UserManager userManager,
                    final VoteService voteService,
                    final I18nHelper i18n,
                    final WatcherOps watcherOps,
                    final WatcherService watcherService,
                    final BeanBuilderFactory beanBuilderFactory,
                    final ContextUriInfo contextUriInfo,
                    final IssueFinder issueFinder,
                    final CreateIssueResource createIssueResource,
                    final UpdateIssueResource updateIssueResource,
                    final DeleteIssueResource deleteIssueResource,
                    final RemoteIssueLinkResource remoteIssueLinkResource,
                    final WorklogResource worklogResource,
                    final CommentResource commentResource,
                    final IssueWorkflowManager issueWorkflowManager,
                    final AssignIssueResource assignIssueResource,
                    final AdhocNotificationService notificationService,
                    final JiraBaseUrls jiraBaseUrls)
    {
        this.jiraBaseUrls = jiraBaseUrls;
        this.authContext = Assertions.notNull(authContext);
        this.userManager = Assertions.notNull(userManager);
        this.voteService = Assertions.notNull(voteService);
        this.i18n = Assertions.notNull(i18n);
        this.watcherOps = Assertions.notNull(watcherOps);
        this.watcherService = Assertions.notNull(watcherService);
        this.beanBuilderFactory = Assertions.notNull(beanBuilderFactory);
        this.contextUriInfo = Assertions.notNull(contextUriInfo);
        this.issueFinder = Assertions.notNull(issueFinder);
        this.createIssueResource = Assertions.notNull(createIssueResource);
        this.updateIssueResource = Assertions.notNull(updateIssueResource);
        this.deleteIssueResource = Assertions.notNull(deleteIssueResource);
        this.remoteIssueLinkResource = Assertions.notNull(remoteIssueLinkResource);
        this.worklogResource = Assertions.notNull(worklogResource);
        this.commentResource = Assertions.notNull(commentResource);
        this.issueWorkflowManager = Assertions.notNull(issueWorkflowManager);
        this.assignIssueResource = Assertions.notNull(assignIssueResource);
        this.notificationService = Assertions.notNull(notificationService);
    }

    /**
     * Get a list of the transitions possible for this issue by the current user, along with fields that are required and their types.
     *
     * <p>
     * Fields will only be returned if <code>expand=transitions.fields</code>.
     * <p>
     * The fields in the metadata correspond to the fields in the transition screen for that transition.
     * Fields not in the screen will not be in the metadata.
     *
     * @param issueIdOrKey the issue whose transitions you want to view
     * @return a response containing a Map of TransitionFieldBeans for each transition possible by the current user.
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of the transitions possible for the specified issue and the fields required to perform the transition.
     *
     * @response.representation.200.example
     *      {@link TransitionsMetaBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the requested issue is not found or the user does not have permission to view it.
     */
    @GET
    @Path("{issueIdOrKey}/transitions")
    public Response getTransitions(@PathParam ("issueIdOrKey") final String issueIdOrKey, @QueryParam("transitionId") final String transitionId)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);

        try
        {
            if (StringUtils.isNotBlank(transitionId))
            {
                Integer.valueOf(transitionId);
            }
        }
        catch (NumberFormatException e)
        {
            throw new RESTException(Response.Status.BAD_REQUEST, i18n.getText("rest.transition.error.id.not.integer"));
        }

        List<ActionDescriptor> actions = issueWorkflowManager.getSortedAvailableActions(issue, authContext.getUser());

        List <TransitionBean> transitions = new ArrayList<TransitionBean>();

        for (ActionDescriptor action : actions)
        {
            if (StringUtils.isNotBlank(transitionId)  && !Integer.valueOf(transitionId).equals(action.getId()))
            {
               continue;
            }
            TransitionBean transitionMetaBean = beanBuilderFactory.newTransitionMetaBeanBuilder()
                    .issue(issue)
                    .action(action)
                    .build();

            transitions.add(transitionMetaBean);
        }
        TransitionsMetaBean transitionsMetaBean = new TransitionsMetaBean(transitions);

        return Response.ok(transitionsMetaBean).cacheControl(never()).build();
    }



    /**
     * Perform a transition on an issue.
     * When performing the transition you can udate or set other issue fields.
     * <p>
     * The fields that can be set on transtion, in either the fields parameter or the update parameter can be determined
     * using the <b>/rest/api/2/issue/{issueIdOrKey}/transitions?expand=transitions.fields</b> resource.
     * If a field is not configured to appear on the transition screen, then it will not be in the transition metadata, and a field
     * validation error will occur if it is submitted.
     *
     * @param issueIdOrKey the issue you want to transition
     * @param issueUpdateBean The json containing the transition to peform and which field values to update.
     *
     * @response.representation.404.doc
     *      The issue does not exist or the user does not have permission to view it
     *
     * @response.representation.400.doc
     *      If there is no transition specified.
     *
     * @response.representation.204.doc
     *      Returned if the transition was successful.
     *
     * @request.representation.example
     *       {@link ResourceExamples#TRANSITION_DOC_EXAMPLE}
     */
    @POST
    @Path("{issueIdOrKey}/transitions")
    public Response doTransition(@PathParam("issueIdOrKey") final String issueIdOrKey, final IssueUpdateBean issueUpdateBean)
    {
        // first check for a transition
        if (issueUpdateBean.getTransition() == null)
        {
            throw new RESTException(Response.Status.BAD_REQUEST, i18n.getText("rest.transition.error.no.transition"));
        }

        try
        {
            Integer.parseInt(issueUpdateBean.getTransition().getId());
        }
        catch (NumberFormatException e)
        {
            throw new RESTException(Response.Status.BAD_REQUEST, i18n.getText("rest.transition.error.id.not.integer"));
        }

        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        return updateIssueResource.transitionIssue(issue, issueUpdateBean);
    }

    /**
     * Remove your vote from an issue. (i.e. "unvote")
     * @param issueIdOrKey the issue the current user is unvoting on
     * @return a Response containing either NO_CONTENT or an error message.
     *
     * @response.representation.204.doc
     *      Nothing is returned on success.
     *
     * @response.representation.404.doc
     *      Returned if the user cannot remove a vote for any reason. (The user did not vote on the issue,
     *      the user is the reporter, voting is disabled, the issue does not exist, etc.)
     *
     */
    @DELETE
    @Path("{issueIdOrKey}/votes")
    public Response removeVote(@PathParam("issueIdOrKey") final String issueIdOrKey)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);

        final VoteService.VoteValidationResult validationResult = voteService.validateRemoveVote(authContext.getLoggedInUser(), authContext.getLoggedInUser(), issue);
        if (!validationResult.isValid())
        {
            throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(validationResult.getErrorCollection()));
        }
        else
        {
            voteService.removeVote(authContext.getLoggedInUser(), validationResult);
            return NO_CONTENT();
        }
    }

    /**
     * Cast your vote in favour of an issue.
     *
     * @param issueIdOrKey the issue to vote for
     * @return a Response containing NO_CONTENT or an error message
     *
     * @response.representation.204.doc
     *      Nothing is returned on success.
     *
     * @response.representation.404.doc
     *      Returned if the user cannot vote for any reason. (The user is the reporter, the user does
     *      not have permission to vote, voting is disabled in the instance, the issue does not exist, etc.)
     */
    @POST
    @Path("{issueIdOrKey}/votes")
    public Response addVote(@PathParam("issueIdOrKey") final String issueIdOrKey)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);

        final VoteService.VoteValidationResult validationResult = voteService.validateAddVote(authContext.getLoggedInUser(), authContext.getLoggedInUser(), issue);
        if (!validationResult.isValid())
        {
            throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(validationResult.getErrorCollection()));
        }
        else
        {
            voteService.addVote(authContext.getLoggedInUser(), validationResult);
            return NO_CONTENT();
        }
    }

    /**
     * A REST sub-resource representing the voters on the issue.
     *
     * @param issueIdOrKey the issue to view voting information for
     * @return a Response containing a VoteBean
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Information about voting on the current issue.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.VoteBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the user cannot view the issue in question or voting is disabled.
     */
    @GET
    @Path("{issueIdOrKey}/votes")
    public Response getVotes(@PathParam("issueIdOrKey") final String issueIdOrKey)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        final User user = authContext.getLoggedInUser();

        if (voteService.isVotingEnabled())
        {
            List<UserBean> voters;

            final boolean hasVoted = voteService.hasVoted(issue, user);
            final ServiceOutcome<? extends Collection<User>> outcome = voteService.viewVoters(issue, user);
            if (outcome.isValid())
            {
                voters = new ArrayList<UserBean>(Transformed.collection(outcome.getReturnedValue(), new Function<User, UserBean>()
                {
                    public UserBean get(final User input)
                    {
                        return new UserBeanBuilder(jiraBaseUrls).user(input).buildShort();
                    }
                }));
            }
            else
            {
                voters = new ArrayList<UserBean>();
            }

            final URI selfUri = contextUriInfo.getBaseUriBuilder().path(IssueResource.class).path(issue.getKey()).path("votes").build();
            final VoteBean voteBean = new VoteBean(selfUri, hasVoted, issue.getVotes(), voters);
            return Response.ok(voteBean).cacheControl(never()).build();
        }
        else
        {
            throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(i18n.getText("issue.operations.voting.disabled")));
        }
    }


    /**
     * Returns a full representation of the issue for the given issue key.
     *
     * <p>
     *  An issue JSON consists of the issue key, a collection of fields,
     *  a link to the workflow transition sub-resource, and (optionally) the HTML rendered values of any fields that support it
     *  (e.g. if wiki syntax is enabled for the description or comments).
     *
     *  <p>
     *  The <code>fields</code> param (which can be specified multiple times) gives a comma-separated list of fields
     *  to include in the response. This can be used to retrieve a subset of fields.
     *  A particular field can be excluded by prefixing it with a minus.
     *  <p>
     *   By default, all (<code>*all</code>) fields are returned in this get-issue resource. Note: the default is different
     *   when doing a jql search -- the default there is just navigable fields (<code>*navigable</code>).
     *  <ul>
     *      <li><code>*all</code> - include all fields</li>
     *      <li><code>*navigable</code> - include just navigable fields</li>
     *      <li><code>summary,comment</code> - include just the summary and comments</li>
     *      <li><code>-comment</code> - include everything except comments (the default is <code>*all</code> for get-issue)</li>
     *      <li><code>*all,-comment</code> - include everything except comments</li>
     *  </ul>
     *
     *  <p>
     *  JIRA will attempt to identify the issue by the <code>issueIdOrKey</code> path parameter. This can be an issue id,
     *  or an issue key. If the issue cannot be found via an exact match, JIRA will also look for the issue in a case-insensitive way, or
     *  by looking to see if the issue was moved. In either of these cases, the request will proceed as normal (a 302 or other redirect
     *  will <b>not</b> be returned). The issue key contained in the response will indicate the current value of issue's key.
     *
     * @param issueIdOrKey the issue id or key to request (i.e. JRA-1330)
     * @param fields the list of fields to return for the issue. By default, all fields are returned.
     * @return a Response containing a IssueBean
     *
     * @response.representation.200.qname
     *      issue
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA issue in JSON format.
     *
     * @response.representation.200.example
     *      {@link IssueResourceExamples#GET_RESPONSE_200}
     *
     * @response.representation.404.doc
     *     Returned if the requested issue is not found, or the user does not have permission to view it.
     */
    @GET
    @Path("{issueIdOrKey}")
    public Response getIssue(@PathParam ("issueIdOrKey") final String issueIdOrKey,
            @QueryParam ("fields") List<StringList> fields, @QueryParam("expand") String expand)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);

        IncludedFields include = IncludedFields.includeAllByDefault(fields);
        final IssueBeanBuilder issueBeanBuilder = beanBuilderFactory.newIssueBeanBuilder(issue, include);
        final IssueBean bean = issueBeanBuilder.expand(expand).build();
        return Response.ok(bean).cacheControl(never()).build();
    }

    /**
     * Returns the list of watchers for the issue with the given key.
     *
     * @param issueIdOrKey the issue key to request (i.e. JRA-1330)
     * @return a Response containing a WatchersBean
     *
     * @response.representation.200.qname
     *      watchers
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns the list of watchers for an issue.
     *
     * @response.representation.200.example
     *      {@link WatchersBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if the requested issue is not found, or the user does not have permission to view it.
     */
    @GET
    @Path("{issueIdOrKey}/watchers")
    public Response getIssueWatchers(@PathParam("issueIdOrKey") String issueIdOrKey)
    {
        Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        WatchersBean watchers = watcherOps.getWatchers(issue, authContext.getLoggedInUser());

        return Response.ok(watchers).cacheControl(never()).build();
    }

    /**
     * Adds a user to an issue's watcher list.
     *
     * @param issueIdOrKey a String containing an issue key
     * @param userName the name of the user to add to the watcher list. If no name is specified, the current user is added.
     * @return nothing
     *
     * @request.representation.example
     *      "fred"
     *
     * @response.representation.400.doc
     *      Returned if there is a problem with the received user representation.
     *
     * @response.representation.204.doc
     *      Returned if the watcher was added successfully.
     *
     * @response.representation.401.doc
     *      Returned if the calling user does not have permission to add the watcher to the issue's list of watchers.
     *
     * @response.representation.404.doc
     *      Returned if either the issue or the user does not exist.
     */
    @POST
    @Path("{issueIdOrKey}/watchers")
    public Response addWatcher(@PathParam("issueIdOrKey") String issueIdOrKey, String userName)
    {
        try
        {
            final User watchUser = getUserFromPost(userName);
            if (watchUser == null)
            {
                return BAD_REQUEST();
            }
            Issue issue = issueFinder.getIssueObject(issueIdOrKey);
            ServiceOutcome<List<User>> outcome = watcherService.addWatcher(issue, authContext.getLoggedInUser(), watchUser);
            if (!outcome.isValid())
            {
                throw new NotAuthorisedWebException(ErrorCollection.of(outcome.getErrorCollection()));
            }

            return NO_CONTENT();
        }
        catch (WatchingDisabledException e)
        {
            throw new NotFoundWebException(e);
        }
    }

    private User getUserFromPost(final String body)
    {
        if (StringUtils.isEmpty(body))
        {
            return authContext.getLoggedInUser();
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();
        try
        {
            JsonParser jp = factory.createJsonParser(body);
            JsonNode obj = mapper.readTree(jp);
            if (obj.isTextual())
            {
                String userName = obj.getTextValue();
                if (StringUtils.isEmpty(userName))
                {
                    return authContext.getLoggedInUser();
                }
                else
                {
                    return userManager.getUser(userName);
                }
            }
            else
            {
                throw new WebApplicationException(BAD_REQUEST());
            }
        }
        catch (JsonParseException e)
        {
            throw new WebApplicationException(e, BAD_REQUEST());
        }
        catch (JsonProcessingException e)
        {
            throw new WebApplicationException(e, BAD_REQUEST());
        }
        catch (IOException e)
        {
            throw new WebApplicationException(e, BAD_REQUEST());
        }
    }

    /**
     * Removes a user from an issue's watcher list.
     *
     * @param issueIdOrKey a String containing an issue key.
     * @param userName a String containing the name of the user to remove from the watcher list. Must not be null.
     * @return a 204 HTTP status if everything goes well
     *
     * @response.representation.204.doc
     *      Returned if the watcher was removed successfully.

     * @response.representation.400.doc
     *      Returned if a user name query parameter is not supplied.
     *
     * @response.representation.401.doc
     *      Returned if the calling user does not have permission to remove the watcher from the issue's list of
     *      watchers.
     *
     * @response.representation.404.doc
     *      Returned if either the issue does not exist.
     */
    @DELETE
    @Path("{issueIdOrKey}/watchers")
    public Response removeWatcher(@PathParam("issueIdOrKey") String issueIdOrKey, @QueryParam ("username") String userName)
    {
        try
        {
            if (userName == null)
            {
                return BAD_REQUEST();
            }
            final User unwatchUser = userManager.getUserEvenWhenUnknown(userName);

            Issue issue = issueFinder.getIssueObject(issueIdOrKey);
            ServiceOutcome<List<User>> outcome = watcherService.removeWatcher(issue, authContext.getLoggedInUser(), unwatchUser);
            if (!outcome.isValid())
            {
                throw new NotAuthorisedWebException(ErrorCollection.of(outcome.getErrorCollection()));
            }

                return NO_CONTENT();
        }
        catch (WatchingDisabledException e)
        {
            throw new NotFoundWebException();
        }
    }

    /**
     * Creates an issue or a sub-task from a JSON representation.
     * <p>
     * The fields that can be set on create, in either the fields parameter or the update parameter can be determined
     * using the <b>/rest/api/2/issue/createmeta</b> resource.
     * If a field is not configured to appear on the create screen, then it will not be in the createmeta, and a field
     * validation error will occur if it is submitted.
     * <p/>
     * Creating a sub-task is similar to creating a regular issue, with two important differences:
     * <ul>
     *     <li>the <code>issueType</code> field must correspond to a sub-task issue type (you can use
     *     <code>/issue/createmeta</code> to discover sub-task issue types), and</li>
     *     <li>you must provide a <code>parent</code> field in the issue create request containing the id or key of the
     *     parent issue.</li>
     * </ul>
     *
     * @request.representation.example
     *      {@link IssueResourceExamples#CREATE_REQUEST}
     *
     * @response.representation.201.qname
     *      issue
     *
     * @response.representation.201.mediaType application/json
     *
     * @response.representation.201.doc
     *      Returns a link to the created issue.
     *
     * @response.representation.201.example
     *      {@link IssueResourceExamples#CREATE_RESPONSE_201}
     *
     * @response.representation.400.doc
     *     Returned if the input is invalid (e.g. missing required fields, invalid field values, and so forth).
     *
     * @response.representation.400.example
     *      {@link IssueResourceExamples#CREATE_RESPONSE_400}
     *
     * @param createRequest an issue create request
     * @return an com.atlassian.jira.rest.api.issue.IssueCreateResponse
     */
    @POST
    public Response createIssue(IssueUpdateBean createRequest)
    {
        return createIssueResource.createIssue(createRequest, contextUriInfo);
    }

    /**
     * Creates issues or sub-tasks from a JSON representation.
     * <p>
     * Creates many issues in one bulk operation.
     * <p/>
     * Creating a sub-task is similar to creating a regular issue. More details can be found in createIssue section:
     * {@link IssueResource#createIssue(IssueUpdateBean)}}
     *
     * @request.representation.example
     *      {@link IssueResourceExamples#BULK_CREATE_REQUEST}
     *
     * @response.representation.201.qname
     *      issue
     *
     * @response.representation.201.mediaType application/json
     *
     * @response.representation.201.doc
     *      Returns a link to the created issues.
     *
     * @response.representation.201.example
     *      {@link IssueResourceExamples#BULK_CREATE_RESPONSE_201}
     *
     * @response.representation.400.doc
     *     Returned if the input is invalid (e.g. missing required fields, invalid field values, and so forth).
     *
     * @response.representation.400.example
     *      {@link IssueResourceExamples#BULK_CREATE_RESPONSE_400}
     *
     * @param createRequest {@link IssuesUpdateBean} which wraps issues to create in collection of {@link }IssueUpdateBean}
     * @return an com.atlassian.jira.rest.api.issue.IssueCreateResponse which encapsulates information about created issues and/or errors
     */

    @POST
    @Path("/bulk")
    public Response createIssues(final IssuesUpdateBean createRequest)
    {
        return createIssueResource.createIssues(createRequest, contextUriInfo);
    }

    /**
     * Delete an issue.
     *
     * If the issue has subtasks you must set the parameter deleteSubtasks=true to delete the issue.
     * You cannot delete an issue without its subtasks also being deleted.
     *
     * @param issueIdOrKey a String containing an issue id or key
     * @param deleteSubtasks a String of true or false indicating that any subtasks should also be deleted.  If the
     * issue has no subtasks this parameter is ignored.  If the issue has subtasks and this parameter is missing or false,
     * then the issue will not be deleted and an error will be returned.
     *
     * @return a 204 HTTP status if everything goes well
     *
     * @response.representation.204.doc
     *      Returned if the issue was removed successfully.
     *
     * @response.representation.400.doc
     *      Returned if an error occurs.
     *
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     *
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to delete the issue.
     *
     * @response.representation.404.doc
     *      Returned if the issue does not exist.
     */
    @DELETE
    @Path("{issueIdOrKey}")
    public Response deleteIssue(@PathParam("issueIdOrKey") String issueIdOrKey, @QueryParam ("deleteSubtasks") String deleteSubtasks)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);

        return deleteIssueResource.deleteIssue(issue, deleteSubtasks, contextUriInfo);
    }

    /**
     * Returns the meta data for creating issues. This includes the available projects, issue types and fields,
     * including field types and whether or not those fields are required.
     * Projects will not be returned if the user does not have permission to create issues in that project.
     *
     * <p>
     * The fields in the createmeta correspond to the fields in the create screen for the project/issuetype.
     * Fields not in the screen will not be in the createmeta.
     *
     * <p>
     * Fields will only be returned if <code>expand=projects.issuetypes.fields</code>.
     * <p>
     * The results can be filtered by project and/or issue type, given by the query params.
     *
     * @param projectIds combined with the projectKeys param, lists the projects with which to filter the results. If absent, all projects are returned.
     *  This parameter can be specified multiple times, and/or be a comma-separated list.
     *  Specifiying a project that does not exist (or that you cannot create issues in) is not an error, but it will not be in the results.
     * @param projectKeys combined with the projectIds param, lists the projects with which to filter the results. If null, all projects are returned.
     *  This parameter can be specified multiple times, and/or be a comma-separated list.
     *  Specifiying a project that does not exist (or that you cannot create issues in) is not an error, but it will not be in the results.
     * @param issuetypeIds combinded with issuetypeNames, lists the issue types with which to filter the results. If null, all issue types are returned.
     *  This parameter can be specified multiple times, and/or be a comma-separated list.
     *  Specifiying an issue type that does not exist is not an error.
     * @param issuetypeNames combinded with issuetypeIds, lists the issue types with which to filter the results. If null, all issue types are returned.
     *  This parameter can be specified multiple times, but is NOT interpreted as a comma-separated list.
     *  Specifiying an issue type that does not exist is not an error.
     * @return a Response containing a CreateMetaBean
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns the meta data for creating issues.
     *
     * @response.representation.200.example
     *      {@link IssueResourceExamples#GET_CREATEMETA_RESPONSE_200}
     *
     * @response.representation.403.doc
     *     Returned if the user does not have permission to view any of the requested projects.
     */
    @GET
    @Path("createmeta")
    public Response getCreateIssueMeta(@QueryParam ("projectIds") final List<StringList> projectIds,
            @QueryParam ("projectKeys") final List<StringList> projectKeys,
            @QueryParam("issuetypeIds") final List<StringList> issuetypeIds,
            @QueryParam("issuetypeNames") final List<String> issuetypeNames)
    {
        final CreateMetaBean bean = beanBuilderFactory.newCreateMetaBeanBuilder()
                .projectIds(projectIds)
                .projectKeys(projectKeys)
                .issueTypeIds(issuetypeIds)
                .issueTypeNames(issuetypeNames)
                .build();

        return Response.ok(bean).cacheControl(never()).build();
    }

    /**
     * Returns the meta data for editing an issue.
     * <p>
     * The fields in the editmeta correspond to the fields in the edit screen for the issue.
     * Fields not in the screen will not be in the editemeta.
     *
     * @param issueIdOrKey the issue whose edit meta data you want to view
     * @return a response containing a Map of FieldBeans for fields editable by the current user.
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a response containing a Map of FieldBeans for fields editable by the current user.
     *
     * @response.representation.200.example
     *      {@link EditMetaBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the requested issue is not found or the user does not have permission to view it.
     */
    @GET
    @Path("{issueIdOrKey}/editmeta")
    public Response getEditIssueMeta(@PathParam ("issueIdOrKey") final String issueIdOrKey)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);

        final EditMetaBean bean = beanBuilderFactory.newEditMetaBeanBuilder()
                .issue(issue)
                .build();

        return Response.ok(bean).cacheControl(never()).build();
    }

    /**
     * Edits an issue from a JSON representation.
     * <p>
     * The issue can either be updated by setting explicit the field value(s)
     * or by using an operation to change the field value.
     * <p>
     * The fields that can be updated, in either the fields parameter or the update parameter, can be determined
     * using the <b>/rest/api/2/issue/{issueIdOrKey}/editmeta</b> resource.<br>
     * If a field is not configured to appear on the edit screen, then it will not be in the editmeta, and a field
     * validation error will occur if it is submitted.
     * <p>
     * Specifying a "field_id": field_value in the "fields" is a shorthand for a "set" operation in the "update" section.<br>
     * Field should appear either in "fields" or "update", not in both.
     *
     * @param issueIdOrKey the issue id or key to update (i.e. JRA-1330)
     *
     * @request.representation.example
     *      {@link ResourceExamples#UPDATE_DOC_EXAMPLE}
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returned if it updated the issue succesfully.
     *
     * @response.representation.400.doc
     *      Returned if the requested issue update failed.
     *
     * @return an com.atlassian.jira.rest.api.issue.IssueCreateResponse
     */
    @PUT
    @Path("{issueIdOrKey}")
    public Response editIssue(@PathParam ("issueIdOrKey") final String issueIdOrKey, IssueUpdateBean updateRequest)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        return updateIssueResource.editIssue(issue, updateRequest);
    }

    /**
     * Assigns an issue to a user.
     * You can use this resource to assign issues when the user submitting the request has the assign permission but not the
     * edit issue permission.
     * If the name is "-1" automatic assignee is used.  A null name will remove the assignee.
     *
     * @param issueIdOrKey a String containing an issue key
     * @param assigneeBean A UserBean with the name of the user to assign the issue to.
     * @return nothing
     *
     * @request.representation.example
     *      {@link UserBean#REF_DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *      Returned if there is a problem with the received user representation.
     *
     * @response.representation.204.doc
     *      Returned if the issue is successfully assigned.
     *
     * @response.representation.401.doc
     *      Returned if the calling user does not have permission to assign the issue.
     *
     * @response.representation.404.doc
     *      Returned if either the issue or the user does not exist.
     */
    @PUT
    @Path("{issueIdOrKey}/assignee")
    public Response assign(@PathParam("issueIdOrKey") String issueIdOrKey, UserBean assigneeBean)
    {
        String assigneeName = assigneeBean == null ? null : assigneeBean.getName();

        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        return assignIssueResource.assignIssue(issue, assigneeName);
    }

    /**
     * A REST sub-resource representing the remote issue links on the issue.
     *
     * @param issueIdOrKey the issue to view the remote issue links for
     * @param globalId The id of the remote issue link to be returned.  If null (not provided) all remote links for the
     * issue are returned.
     * <p>For a fullexplanation of Issue Link fields please refer to
     * <a href="https://developer.atlassian.com/display/JIRADEV/Fields+in+Remote+Issue+Links">https://developer.atlassian.com/display/JIRADEV/Fields+in+Remote+Issue+Links</a></p>
     * @return if no globalId is specified, a Response containing a List of RemoteIssueLinkBeans is returned. Otherwise, a
     * Response containing a RemoteIssueLinkBean with the given globalId is returned.
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Information on the remote issue links for the current issue.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.RemoteIssueLinkResourceExamples#GET_LIST_EXAMPLE}
     *
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     *
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to view the remote issue links, or if issue linking is
     *      disabled.
     *
     * @response.representation.404.doc
     *      Returned if the issue or remote issue link do not exist.
     */
    @GET
    @Path("{issueIdOrKey}/remotelink")
    public Response getRemoteIssueLinks(@PathParam("issueIdOrKey") final String issueIdOrKey, @QueryParam("globalId") final String globalId)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        return remoteIssueLinkResource.getRemoteIssueLinks(issue, globalId);
    }

    /**
     * Get the remote issue link with the given id on the issue.
     *
     * @param issueIdOrKey the issue to view the remote issue links for
     * @param linkId the id of the remote issue link
     * @return a Response containing a RemoteIssueLinkBean.
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Information on the remote issue link with the given id.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.RemoteIssueLinkResourceExamples#GET_EXAMPLE}
     *
     * @response.representation.400.doc
     *      Returned if the linkId is not a valid number, or if the remote issue link with the given id does not belong to
     *      the given issue.
     *
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     *
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to view the remote issue link, or if issue linking is
     *      disabled.
     *
     * @response.representation.404.doc
     *      Returned if the issue or remote issue link do not exist.
     */
    @GET
    @Path("{issueIdOrKey}/remotelink/{linkId}")
    public Response getRemoteIssueLinkById(@PathParam("issueIdOrKey") final String issueIdOrKey, @PathParam("linkId") final String linkId)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        return remoteIssueLinkResource.getRemoteIssueLinkById(issue, linkId);
    }

    /**
     * Creates or updates a remote issue link from a JSON representation. If a globalId is provided and a remote issue link
     * exists with that globalId, the remote issue link is updated. Otherwise, the remote issue link is created.
     *
     * @param issueIdOrKey the issue to create the remote issue link for
     * @param request a request to create or update a remote issue link
     * @return a RemoteIssueLinkCreateOrUpdateResponse
     *
     * @request.representation.example
     *      {@link RemoteIssueLinkResourceExamples#CREATE_OR_UPDATE_REQUEST}
     *
     * @response.representation.200.qname
     *      remote issue link
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a link to the created/updated remote issue link.
     *
     * @response.representation.200.example
     *      {@link RemoteIssueLinkResourceExamples#CREATE_OR_UPDATE_RESPONSE_200}
     *
     * @response.representation.400.doc
     *     Returned if the input is invalid (e.g. missing required fields, invalid values, and so forth).
     *
     * @response.representation.400.example
     *      {@link RemoteIssueLinkResourceExamples#CREATE_OR_UPDATE_RESPONSE_400}
     *
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     *
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to create/update the remote issue link, or if issue linking
     *      is disabled.
     */
    @POST
    @Path("{issueIdOrKey}/remotelink")
    public Response createOrUpdateRemoteIssueLink(@PathParam("issueIdOrKey") final String issueIdOrKey, final RemoteIssueLinkCreateOrUpdateRequest request)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        return remoteIssueLinkResource.createOrUpdateRemoteIssueLink(issue, request, contextUriInfo);
    }

    /**
     * Updates a remote issue link from a JSON representation. Any fields not provided are set to null.
     *
     * @param issueIdOrKey the issue to update the remote issue link for
     * @param updateRequest a request to update a remote issue link
     * @return a Response containing either NO_CONTENT or an error message.
     *
     * @request.representation.example
     *      {@link RemoteIssueLinkResourceExamples#CREATE_OR_UPDATE_REQUEST}
     *
     * @response.representation.204.doc
     *      Returned if the remote issue link was updated successfully.
     *
     * @response.representation.400.doc
     *      Returned if the input is invalid (e.g. missing required fields, invalid values, and so forth).
     *
     * @response.representation.400.example
     *      {@link RemoteIssueLinkResourceExamples#CREATE_OR_UPDATE_RESPONSE_400}
     *
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     *
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to update the remote issue link, or if issue linking is
     *      disabled.
     */
    @PUT
    @Path("{issueIdOrKey}/remotelink/{linkId}")
    public Response updateRemoteIssueLink(@PathParam("issueIdOrKey") final String issueIdOrKey, @PathParam("linkId") final String linkId, final RemoteIssueLinkCreateOrUpdateRequest updateRequest)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        return remoteIssueLinkResource.updateRemoteIssueLink(issue, linkId, updateRequest);
    }

    /**
     * Delete the remote issue link with the given id on the issue.
     *
     * @response.representation.204.doc
     *      Returned if the remote issue link was removed successfully.
     *
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     *
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to delete the remote issue link, or if issue linking is
     *      disabled.
     *
     * @response.representation.404.doc
     *      Returned if the issue or remote issue link do not exist.
     *
     * @param issueIdOrKey the issue to create the remote issue link for
     * @param remoteIssueLinkId the id of the remote issue link
     * @return a 204 HTTP status if everything goes well, otherwise the Response contains the error details
     */
    @DELETE
    @Path("{issueIdOrKey}/remotelink/{linkId}")
    public Response deleteRemoteIssueLinkById(@PathParam("issueIdOrKey") final String issueIdOrKey, @PathParam("linkId") final String remoteIssueLinkId)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        return remoteIssueLinkResource.deleteRemoteIssueLinkById(issue, remoteIssueLinkId);
    }

    /**
     * Delete the remote issue link with the given global id on the issue.
     *
     * @response.representation.204.doc
     *      Returned if the remote issue link was removed successfully.
     *
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     *
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to delete the remote issue link, or if issue linking is
     *      disabled.
     *
     * @response.representation.404.doc
     *      Returned if the issue or remote issue link do not exist.
     *
     * @param issueIdOrKey the issue to create the remote issue link for
     * @param globalId the global id of the remote issue link
     * @return a 204 HTTP status if everything goes well, otherwise the Response contains the error details
     */
    @DELETE
    @Path("{issueIdOrKey}/remotelink")
    public Response deleteRemoteIssueLinkByGlobalId(@PathParam("issueIdOrKey") final String issueIdOrKey, @QueryParam("globalId") final String globalId)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        return remoteIssueLinkResource.deleteRemoteIssueLinkByGlobalId(issue, globalId);
    }

    /**
     * Returns all work logs for an issue.
     *
     * @param issueIdOrKey the worklogs belongs to
     * @return All worklogs for the issue
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      returns a collection of worklogs associated with the issue, with count and pagination information.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.issue.fields.rest.json.beans.WorklogWithPaginationBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the issue with the given id/key does not exist or if the currently authenticated user does not
     *      have permission to view it.
     */
    @GET
    @Path("{issueIdOrKey}/worklog")
    public Response getIssueWorklog(@PathParam ("issueIdOrKey") final String issueIdOrKey)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        return worklogResource.getIssueWorklogs(issue);
    }

    /**
     * Returns a specific worklog.
     *
     * @param issueIdOrKey a string containing the issue id or key the worklog belongs to
     * @param worklogId a String containing the work log id
     * @return a worklog
     *
     * @response.representation.200.qname
     *      worklog
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the work log with the given id exists and the currently authenticated user has permission to
     *      view it. The returned response contains a full representation of the work log in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.issue.fields.rest.json.beans.WorklogJsonBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the work log with the given id does not exist or if the currently authenticated user does not
     *      have permission to view it.
     */
    @GET
    @Path ("{issueIdOrKey}/worklog/{id}")
    public Response getWorklog(@PathParam ("issueIdOrKey") final String issueIdOrKey, @PathParam ("id") final String worklogId)
    {
        Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        return worklogResource.getWorklogForIssue(worklogId, issue);
    }

    /**
     * Updates an existing worklog entry using its JSON representation.
     *
     * @param issueIdOrKey a string containing the issue id or key the worklog belongs to
     * @param worklogId id of the worklog to be updated
     * @param adjustEstimate (optional) allows you to provide specific instructions to update the remaining time estimate of the issue.  Valid values are
     *      <ul>
     *      <li>"new" - sets the estimate to a specific value</li>
     *      <li>"leave"- leaves the estimate as is</li>
     *      <li>"auto"- Default option.  Will automatically adjust the value based on the new timeSpent specified on the worklog</li> </ul>
     * @param newEstimate (required when "new" is selected for adjustEstimate) the new value for the remaining estimate field.
     * @param request json body of request converted to a {@link com.atlassian.jira.issue.fields.rest.json.beans.WorklogJsonBean}
     * @return updated worklog
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.issue.fields.rest.json.beans.WorklogJsonBean#DOC_EXAMPLE}
     *
     * @response.representation.200.doc
     *      Returned if update was successful
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.issue.fields.rest.json.beans.WorklogJsonBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *     Returned if the input is invalid (e.g. missing required fields, invalid values, and so forth).
     *
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to update the worklog
     */
    @PUT
    @Path("{issueIdOrKey}/worklog/{id}")
    public Response updateWorklog(@PathParam ("issueIdOrKey") final String issueIdOrKey, @PathParam ("id") final String worklogId, @QueryParam("adjustEstimate") String adjustEstimate, @QueryParam("newEstimate") String newEstimate, WorklogJsonBean request)
    {
        if (request.getId() != null && !request.getId().equals(worklogId))
        {
            throw new RESTException(Response.Status.BAD_REQUEST, i18n.getText("rest.worklog.error.id.mismatch"));
        }
        request.setId(worklogId);

        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        return worklogResource.updateWorklog(issue, request, new WorklogResource.WorklogAdjustmentRequest(adjustEstimate, newEstimate, null, null), contextUriInfo);
    }

    /**
     * Deletes an existing worklog entry .
     *
     * @param issueIdOrKey a string containing the issue id or key the worklog belongs to
     * @param worklogId id of the worklog to be deleted
     * @param adjustEstimate (optional) allows you to provide specific instructions to update the remaining time estimate of the issue.  Valid values are
     *      <ul>
     *      <li>"new" - sets the estimate to a specific value</li>
     *      <li>"leave"- leaves the estimate as is</li>
     *      <li>"manual" - specify a specific amount to increase remaining estimate by</li>
     *      <li>"auto"- Default option.  Will automatically adjust the value based on the new timeSpent specified on the worklog</li> </ul>
     * @param newEstimate (required when "new" is selected for adjustEstimate) the new value for the remaining estimate field. e.g. "2d"
     * @param increaseBy (required when "manual" is selected for adjustEstimate) the amount to increase the remaining estimate by e.g. "2d"
     * @return No Content Response
     *
     * @response.representation.204.doc
     *      Returned if delete was successful
     *
     * @response.representation.400.doc
     *     Returned if the input is invalid (e.g. missing required fields, invalid values, and so forth).
     *
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to delete the worklog
     */
    @DELETE
    @Path ("{issueIdOrKey}/worklog/{id}")
    public Response deleteWorklog(@PathParam ("issueIdOrKey") final String issueIdOrKey, @PathParam ("id") final String worklogId, @QueryParam ("adjustEstimate") String adjustEstimate, @QueryParam ("newEstimate") String newEstimate, @QueryParam ("increaseBy") String increaseBy)
    {
        WorklogJsonBean request = new WorklogJsonBean();
        request.setId(worklogId);
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        return worklogResource.deleteWorklog(issue, request, new WorklogResource.WorklogAdjustmentRequest(adjustEstimate, newEstimate, null, increaseBy), contextUriInfo);
    }

    /**
     * Adds a new worklog entry to an issue.
     *
     * @param issueIdOrKey a string containing the issue id or key the worklog will be added to
     * @param adjustEstimate (optional) allows you to provide specific instructions to update the remaining time estimate of the issue.  Valid values are
     *      <ul>
     *      <li>"new" - sets the estimate to a specific value</li>
     *      <li>"leave"- leaves the estimate as is</li>
     *      <li>"manual" - specify a specific amount to increase remaining estimate by</li>
     *      <li>"auto"- Default option.  Will automatically adjust the value based on the new timeSpent specified on the worklog</li> </ul>
     * @param newEstimate (required when "new" is selected for adjustEstimate) the new value for the remaining estimate field. e.g. "2d"
     * @param reduceBy (required when "manual" is selected for adjustEstimate) the amount to reduce the remaining estimate by e.g. "2d"
     * @param request json body of request converted to a {@link com.atlassian.jira.issue.fields.rest.json.beans.WorklogJsonBean}
     * @return the added worklog
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.issue.fields.rest.json.beans.WorklogJsonBean#DOC_EXAMPLE}
     *
     * @response.representation.201.doc
     *      Returned if add was successful
     *
     * @response.representation.400.doc
     *     Returned if the input is invalid (e.g. missing required fields, invalid values, and so forth).
     *
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to add the worklog
     */
    @POST
    @Path ("{issueIdOrKey}/worklog")
    public Response addWorklog(@PathParam ("issueIdOrKey") final String issueIdOrKey, @QueryParam ("adjustEstimate") String adjustEstimate, @QueryParam ("newEstimate") String newEstimate, @QueryParam ("reduceBy") String reduceBy, WorklogJsonBean request)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        return worklogResource.addWorklog(issue, request, new WorklogResource.WorklogAdjustmentRequest(adjustEstimate, newEstimate, reduceBy, null), contextUriInfo);
    }

    /**
     * Returns all comments for an issue.
     *
     * @param issueIdOrKey to get comments for
     * @param expand optional flags: renderedBody (provides body rendered in HTML)
     * @return all comments for the issue
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      returns a collection of comments associated with the issue, with count and pagination information.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.issue.fields.rest.json.beans.CommentsWithPaginationJsonBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the issue with the given id/key does not exist or if the currently authenticated user does not
     *      have permission to view it.
     */
    @GET
    @Path ("{issueIdOrKey}/comment")
    public Response getComments(@PathParam ("issueIdOrKey") final String issueIdOrKey, @QueryParam("expand")
                                String expand)
    {
        return commentResource.getComments(issueIdOrKey, expand);
    }

    /**
     * Returns a single comment.
     *
     * @param commentId the ID of the comment to request
     * @param issueIdOrKey of the issue the comment belongs to
     * @param expand optional flags: renderedBody (provides body rendered in HTML)
     * @return a Response containing a CommentJsonBean
     *
     * @response.representation.200.qname
     *      comment
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA comment in JSON format.
     *
     * @response.representation.200.example
     *      {@link CommentJsonBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if the requested comment is not found, or the user does not have permission to view it.
     */
    @GET
    @Path ("{issueIdOrKey}/comment/{id}")
    public Response getComments(@PathParam ("issueIdOrKey") final String issueIdOrKey, @PathParam ("id") final String commentId,
                                @QueryParam("expand") String expand)
    {
        return commentResource.getComment(issueIdOrKey, commentId, expand);
    }

    /**
     * Updates an existing comment using its JSON representation.
     *
     * @param issueIdOrKey a string containing the issue id or key the comment belongs to
     * @param commentId id of the comment to be updated
     * @param expand optional flags: renderedBody (provides body rendered in HTML)
     * @param request json body of request converted to a {@link com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean}
     * @return updated Comment
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean#DOC_UPDATE_EXAMPLE}
     *
     * @response.representation.200.doc
     *      Returned if update was successful
     *
     * @response.representation.200.example
     *  {@link com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *     Returned if the input is invalid (e.g. missing required fields, invalid values, and so forth).
     *
     */
    @PUT
    @Path ("{issueIdOrKey}/comment/{id}")
    public Response updateComment(@PathParam ("issueIdOrKey") final String issueIdOrKey, @PathParam ("id") final String commentId,
                                  @QueryParam("expand") String expand, CommentJsonBean request)
    {
        return commentResource.updateComment(issueIdOrKey, commentId, expand, request);
    }

    /**
     * Deletes an existing comment .
     *
     * @param issueIdOrKey a string containing the issue id or key the comment belongs to
     * @param commentId id of the comment to be deleted
     * @return No Content Response
     *
     * @response.representation.204.doc
     *      Returned if delete was successful
     *
     * @response.representation.400.doc
     *     Returned if the input is invalid (e.g. missing required fields, invalid values, and so forth).
     */
    @DELETE
    @Path ("{issueIdOrKey}/comment/{id}")
    public Response deleteComment(@PathParam ("issueIdOrKey") final String issueIdOrKey, @PathParam ("id") final String commentId)
    {
       return commentResource.deleteComment(issueIdOrKey, commentId);
    }

    /**
     * Adds a new comment to an issue.
     *
     * @param issueIdOrKey a string containing the issue id or key the comment will be added to
     * @param expand optional flags: renderedBody (provides body rendered in HTML)
     * @param request json body of request converted to a {@link com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean}
     * @return the added comment
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean#DOC_UPDATE_EXAMPLE}
     *
     * @response.representation.201.doc
     *      Returned if add was successful
     *
     * @response.representation.201.example
     *   {@link com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *     Returned if the input is invalid (e.g. missing required fields, invalid values, and so forth).
     *
     */
    @POST
    @Path ("{issueIdOrKey}/comment")
    public Response addComment(@PathParam ("issueIdOrKey") final String issueIdOrKey, @QueryParam("expand")
                               String expand, CommentJsonBean request)
    {
        return commentResource.addComment(issueIdOrKey, expand, request);
    }

    /**
     * Sends a notification (email) to the list or recipients defined in the request.
     *
     * @param issueIdOrKey a string containing the issue id or key the comment will be added to
     * @param request json body of request converted to a {@link com.atlassian.jira.issue.fields.rest.json.beans.NotificationJsonBean}
     * @return <empty>
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.issue.fields.rest.json.beans.NotificationJsonBean#DOC_UPDATE_EXAMPLE}
     *
     * @response.representation.204.doc
     *      Returned if adding to the mail queue was successful
     *
     * @response.representation.400.doc
     *     Returned if the input is invalid (e.g. missing required fields, invalid values, and so forth).
     *
     * @response.representation.403.doc
     *     Returned is outgoing emails are disabled OR no SMTP server is defined.
     */
    @POST
    @Path ("{issueIdOrKey}/notify")
    public Response notify(@PathParam ("issueIdOrKey") final String issueIdOrKey, NotificationJsonBean request)
    {
        if (MailFactory.getSettings().isSendingDisabled())
        {
            throw new RESTException(Response.Status.FORBIDDEN, ErrorCollection.of(i18n.getText("rest.error.outgoing.mail.disabled")));
        }
        if (MailFactory.getServerManager().getDefaultSMTPMailServer() == null)
        {
            throw new RESTException(Response.Status.FORBIDDEN, ErrorCollection.of(i18n.getText("rest.error.no.smtp.defined")));
        }

        final User user = authContext.getLoggedInUser();
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);

        final ErrorCollection errors = ErrorCollection.of();

        final ServiceOutcome<NotificationBuilder> notificationBuilder = makeBuilder(notificationService.makeBuilder(), request, i18n);
        if (!notificationBuilder.isValid())
        {
            // don't throw an exception quite yet, we want to continue validating, giving maximum feedback
            errors.addErrorCollection(notificationBuilder.getErrorCollection());
        }

        final AdhocNotificationService.ValidateNotificationResult result = notificationService.validateNotification(notificationBuilder.getReturnedValue(), user, issue);
        if (!result.isValid())
        {
            throw new RESTException(Response.Status.BAD_REQUEST, errors.addErrorCollection(result.getErrorCollection()));
        }

        notificationService.sendNotification(result);

        return Response.noContent().cacheControl(never()).build();
    }

    /**
     * Returns a Response with a status code of 400.
     *
     * @return a Response with a status code of 400.
     */
    protected Response BAD_REQUEST()
    {
        return Response.status(Response.Status.BAD_REQUEST).cacheControl(never()).build();
    }

    /**
     * Returns a Response with a status code of 204.
     *
     * @return a Response with a status code of 204
     */
    protected static Response NO_CONTENT()
    {
        return Response.noContent().cacheControl(never()).build();
    }

    public Collection<FieldMetaBean> getRequiredFields(final FieldScreenRenderer fieldScreenRenderer, final Issue issue)
    {
        final Collection<FieldMetaBean> fields = new ArrayList<FieldMetaBean>();

        for (FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderer.getFieldScreenRenderTabs())
        {
            for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing())
            {
                if (fieldScreenRenderLayoutItem.isShow(issue))
                {
                    OrderableField orderableField = fieldScreenRenderLayoutItem.getOrderableField();

                    // JRA-16112 - This is a hack that is here because the resolution field is "special". You can not
                    // make the resolution field required and therefore by default the FieldLayoutItem for resolution
                    // returns false for the isRequired method. This is so that you can not make the resolution field
                    // required for issue creation. HOWEVER, whenever the resolution system field is shown it is
                    // required because the edit template does not provide a none option and indicates that it is
                    // required. THEREFORE, when the field is included on a transition screen we will do a special
                    // check to make the FieldLayoutItem claim it is required IF we run into the resolution field.
                    if (IssueFieldConstants.RESOLUTION.equals(orderableField.getId()))
                    {
                        fieldScreenRenderLayoutItem =
                                new FieldScreenRenderLayoutItemImpl(fieldScreenRenderLayoutItem.getFieldScreenLayoutItem(), fieldScreenRenderLayoutItem.getFieldLayoutItem())
                                {
                                    public boolean isRequired()
                                    {
                                        return true;
                                    }
                                };
                    }
// TODO refactor
//                    final FieldTypeInfo fieldTypeInfo = getFieldTypeInfo(fieldScreenRenderLayoutItem.getFieldLayoutItem(), issue);
//
//                    final FieldMetaBean bean = FieldMetaBean.newBean()
//                            .typeInfo(fieldTypeInfo);
//                    fields.add(bean);
                }
            }
        }
        return fields;
    }




}
