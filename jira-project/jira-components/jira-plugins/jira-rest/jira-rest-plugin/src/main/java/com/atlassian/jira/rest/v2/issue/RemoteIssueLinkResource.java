package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.CreateValidationResult;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.DeleteByGlobalIdValidationResult;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.DeleteValidationResult;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.RemoteIssueLinkListResult;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.RemoteIssueLinkResult;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.rest.api.issue.RemoteIssueLinkCreateOrUpdateRequest;
import com.atlassian.jira.rest.api.issue.RemoteIssueLinkCreateOrUpdateResponse;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.I18nHelper;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.status;

/**
 * Responsible for handling REST calls relating to remote issue links.
 *
 * @since v5.0
 */
public class RemoteIssueLinkResource
{
    private final RemoteIssueLinkService remoteIssueLinkService;
    private final BeanBuilderFactory beanBuilderFactory;
    private final I18nHelper i18n;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public RemoteIssueLinkResource(final RemoteIssueLinkService remoteIssueLinkService, final BeanBuilderFactory beanBuilderFactory,
            final I18nHelper i18n, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.remoteIssueLinkService = remoteIssueLinkService;
        this.beanBuilderFactory = beanBuilderFactory;
        this.i18n = i18n;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    /**
     * Gets all the remote issue links for the given issue, and filter by the other parameters.
     * 
     * @param issue the issue
     * @param globalId if not null, return only the remote issue link with this globalId

     * @return a Response containing a List of RemoteIssueLinkBeans, or the error details if something went wrong
     */
    public Response getRemoteIssueLinks(final Issue issue, final String globalId)
    {
        final User user = callingUser();

        final Object entity;

        if (globalId != null)
        {
            final RemoteIssueLinkResult result = remoteIssueLinkService.getRemoteIssueLinkByGlobalId(user, issue, globalId);
            if (!result.isValid())
            {
                throw new RESTException(ErrorCollection.of(result.getErrorCollection()));
            }

            entity = convertToBean(result.getRemoteIssueLink());
        }
        else
        {
            final RemoteIssueLinkListResult result = remoteIssueLinkService.getRemoteIssueLinksForIssue(user, issue);
            if (!result.isValid())
            {
                throw new RESTException(ErrorCollection.of(result.getErrorCollection()));
            }

            entity = convertToBeans(result.getRemoteIssueLinks());
        }

        return Response.ok(entity).cacheControl(never()).build();
    }

    /**
     * Gets the remote issue link with the given id for the given issue.
     * 
     * @param issue the issue
     * @param idString the id of the remote issue link
     * @return a Response containing a RemoteIssueLinkBean, or the error details if something went wrong
     */
    public Response getRemoteIssueLinkById(final Issue issue, final String idString)
    {
        final RemoteIssueLink remoteIssueLink = getRemoteIssueLinkPrivate(issue, idString);
        final RemoteIssueLinkBean bean = beanBuilderFactory.newRemoteIssueLinkBeanBuilder(remoteIssueLink).build();
        return Response.ok(bean).cacheControl(never()).build();
    }

    /**
     * Creates or updates a remote issue link from a JSON representation. If a globalId is provided and a remote issue link
     * exists with that globalId, the remote issue link is updated. Otherwise, the remote issue link is created.
     *
     * @param issue the issue to create the link with
     * @param request the request, containing the values of the remote issue link to be created/updated
     * @param contextUriInfo uri context, for building the self link
     * @return a Response containing a RemoteIssueLinkCreateOrUpdateResponse, or the error details if something went wrong
     */
    public Response createOrUpdateRemoteIssueLink(final Issue issue, final RemoteIssueLinkCreateOrUpdateRequest request, final ContextUriInfo contextUriInfo)
    {
        final User user = callingUser();

        // Check if a remote issue link exists with the given globalId
        if (request.globalId() != null)
        {
            final RemoteIssueLinkResult result = remoteIssueLinkService.getRemoteIssueLinkByGlobalId(user, issue, request.globalId());

            if (result.isValid())
            {
                // The remote issue link exists, update it
                updateRemoteIssueLinkPrivate(result.getRemoteIssueLink(), request);
                final URI self = RemoteIssueLinkBeanBuilder.createSelfLink(result.getRemoteIssueLink(), issue, contextUriInfo);
                final RemoteIssueLinkCreateOrUpdateResponse response = newCreateOrUpdateResponse(result.getRemoteIssueLink(), self);
                return status(OK).entity(response).build();
            }

            if (!isValidForCreate(result))
            {
                throw new RESTException(ErrorCollection.of(result.getErrorCollection()));
            }
        }

        // No existing remote issue link found, create it
        return createRemoteIssueLink(issue, request, contextUriInfo);
    }

    private boolean isValidForCreate(final RemoteIssueLinkResult result)
    {
        // The result is still valid if there is only a NOT_FOUND error, as the remote issue link not existing is not an error in this case
        if (!result.isValid())
        {
            if (result.getErrorCollection().getReasons().contains(Reason.NOT_FOUND)
                    && result.getErrorCollection().getReasons().size() == 1)
            {
                return true;
            }

            // Errors other than NOT_FOUND
            return false;
        }

        // No errors
        return true;
    }

    /**
     * Creates a remote issue link.
     *
     * @param issue the issue to create the link with
     * @param request the request, containing the values of the remote issue link to be created
     * @param contextUriInfo uri context, for building the self link
     * @return a Response containing a RemoteIssueLinkCreateOrUpdateResponse, or the error details if something went wrong
     */
    private Response createRemoteIssueLink(final Issue issue, final RemoteIssueLinkCreateOrUpdateRequest request, final ContextUriInfo contextUriInfo)
    {
        final RemoteIssueLink remoteIssueLink = buildRemoteIssueLink(issue.getId(), request);
        final User user = callingUser();

        final CreateValidationResult validationResult = remoteIssueLinkService.validateCreate(user, remoteIssueLink);
        if (!validationResult.isValid())
        {
            throw new RESTException(ErrorCollection.of(validationResult.getErrorCollection()));
        }

        final RemoteIssueLinkResult result = remoteIssueLinkService.create(user, validationResult);
        if (!result.isValid())
        {
            throw new RESTException(ErrorCollection.of(validationResult.getErrorCollection()));
        }

        final URI self = RemoteIssueLinkBeanBuilder.createSelfLink(result.getRemoteIssueLink(), issue, contextUriInfo);
        final RemoteIssueLinkCreateOrUpdateResponse response = newCreateOrUpdateResponse(result.getRemoteIssueLink(), self);
        return status(CREATED).location(self).entity(response).build();
    }

    /**
     * Updates a remote issue link.
     *
     * @param issue the issue to update the link with
     * @param idString the id of the remote issue link
     * @param request the request, containing the values of the remote issue link to be updated
     * @return a Response with no content, or the error details if something went wrong
     */
    public Response updateRemoteIssueLink(final Issue issue, final String idString, final RemoteIssueLinkCreateOrUpdateRequest request)
    {
        final RemoteIssueLink oldRemoteIssueLink = getRemoteIssueLinkPrivate(issue, idString);
        updateRemoteIssueLinkPrivate(oldRemoteIssueLink, request);
        return status(NO_CONTENT).build();
    }

    /**
     * Deletes a remote issue link.
     *
     * @param issue the issue
     * @param idString the id of the remote issue link
     * @return a Response with no content, or the error details if something went wrong
     */
    public Response deleteRemoteIssueLinkById(final Issue issue, final String idString)
    {
        // Get the remote issue link to validate that it exists and belongs to the given issue
        final RemoteIssueLink remoteIssueLink = getRemoteIssueLinkPrivate(issue, idString);
        final User user = callingUser();

        final DeleteValidationResult validationResult = remoteIssueLinkService.validateDelete(user, remoteIssueLink.getId());
        if (!validationResult.isValid())
        {
            throw new RESTException(ErrorCollection.of(validationResult.getErrorCollection()));
        }

        remoteIssueLinkService.delete(user, validationResult);

        return Response.noContent().cacheControl(never()).build();
    }

    public Response deleteRemoteIssueLinkByGlobalId(final Issue issue, final String globalId)
    {
        final User user = callingUser();

        final DeleteByGlobalIdValidationResult validationResult = remoteIssueLinkService.validateDeleteByGlobalId(user, issue, globalId);
        if (!validationResult.isValid())
        {
            throw new RESTException(ErrorCollection.of(validationResult.getErrorCollection()));
        }

        remoteIssueLinkService.deleteByGlobalId(user, validationResult);

        return Response.noContent().cacheControl(never()).build();
    }

    /**
     * Gets the remote issue link with the given id, and performs some error checking. An exception will be thrown if the id
     * is invalid, the remote issue link does not exist, or the remote issue link does not belong to the given issue.
     *
     * @param issue the issue
     * @param idString the remote issue link id
     * @return a RemoteIssueLink
     */
    private RemoteIssueLink getRemoteIssueLinkPrivate(final Issue issue, final String idString)
    {
        final Long remoteIssueLinkId = getRemoteIssueLinkId(idString);
        final RemoteIssueLinkResult result = remoteIssueLinkService.getRemoteIssueLink(callingUser(), remoteIssueLinkId);
        if (!result.isValid())
        {
            throw new RESTException(ErrorCollection.of(result.getErrorCollection()));
        }

        // Check if remote issue link exists
        final RemoteIssueLink remoteIssueLink = result.getRemoteIssueLink();
        if (remoteIssueLink == null)
        {
            throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(i18n.getText("rest.issue.remotelink.with.id.not.found", idString)));
        }

        // Check that the remote issue link belongs to this issue
        if (!issue.getId().equals(remoteIssueLink.getIssueId()))
        {
            throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(i18n.getText("rest.issue.remotelink.not.for.issue", idString, issue.getKey())));
        }

        return remoteIssueLink;
    }

    /**
     * Updates a remote issue link.
     *
     * @param oldRemoteIssueLink the remote issue link to update
     * @param request the request, containing the values of the remote issue link to be updated
     */
    private void updateRemoteIssueLinkPrivate(final RemoteIssueLink oldRemoteIssueLink, final RemoteIssueLinkCreateOrUpdateRequest request)
    {
        final RemoteIssueLink remoteIssueLink = buildRemoteIssueLink(oldRemoteIssueLink.getIssueId(), request, oldRemoteIssueLink.getId());
        final User user = callingUser();

        final RemoteIssueLinkService.UpdateValidationResult validationResult = remoteIssueLinkService.validateUpdate(user, remoteIssueLink);
        if (!validationResult.isValid())
        {
            throw new RESTException(ErrorCollection.of(validationResult.getErrorCollection()));
        }

        final RemoteIssueLinkResult result = remoteIssueLinkService.update(user, validationResult);
        if (!result.isValid())
        {
            throw new RESTException(ErrorCollection.of(validationResult.getErrorCollection()));
        }
    }

    /**
     * Generates a RemoteIssueLink from an Issue and a RemoteIssueLinkCreateRequest.
     *
     * @param issueId the id of the issue
     * @param request the request
     * @param id the id of the remote issue link
     * @return a RemoteIssueLink
     */
    private RemoteIssueLink buildRemoteIssueLink(final Long issueId, final RemoteIssueLinkCreateOrUpdateRequest request, final Long id)
    {
        return new RemoteIssueLinkBuilder()
                .issueId(issueId)
                .id(id)
                .globalId(request.globalId())
                .title(request.title())
                .summary(request.summary())
                .url(request.url())
                .iconUrl(request.iconUrl())
                .iconTitle(request.iconTitle())
                .relationship(request.relationship())
                .resolved(request.resolved())
                .statusIconUrl(request.statusIconUrl())
                .statusIconTitle(request.statusIconTitle())
                .statusIconLink(request.statusIconLink())
                .applicationType(request.applicationType())
                .applicationName(request.applicationName())
                .build();
    }

    private RemoteIssueLink buildRemoteIssueLink(final Long issueId, final RemoteIssueLinkCreateOrUpdateRequest request)
    {
        return buildRemoteIssueLink(issueId, request, null);
    }

    /**
     * Converts a String to a Long, throwing a RESTException if the input is invalid.
     * 
     * @param idString a String representing a remote issue link id
     * @return a Long
     */
    private Long getRemoteIssueLinkId(final String idString)
    {
        final Long remoteIssueLinkId;
        try
        {
            remoteIssueLinkId = Long.parseLong(idString);
        }
        catch (NumberFormatException e)
        {
            throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(i18n.getText("rest.issue.remotelink.invalid.id", idString)));
        }

        return remoteIssueLinkId;
    }

    private RemoteIssueLinkBean convertToBean(final RemoteIssueLink remoteIssueLink)
    {
        return beanBuilderFactory.newRemoteIssueLinkBeanBuilder(remoteIssueLink).build();
    }

    private List<RemoteIssueLinkBean> convertToBeans(final List<RemoteIssueLink> remoteIssueLinks)
    {
        final List<RemoteIssueLinkBean> beans = new ArrayList<RemoteIssueLinkBean>(remoteIssueLinks.size());

        for (final RemoteIssueLink remoteIssueLink : remoteIssueLinks)
        {
            beans.add(convertToBean(remoteIssueLink));
        }

        return beans;
    }

    private RemoteIssueLinkCreateOrUpdateResponse newCreateOrUpdateResponse(final RemoteIssueLink remoteIssueLink, final URI self)
    {
        return new RemoteIssueLinkCreateOrUpdateResponse()
                .id(remoteIssueLink.getId())
                .self(self.toString());
    }

    private User callingUser()
    {
        return jiraAuthenticationContext.getLoggedInUser();
    }
}
