package com.atlassian.jira.rest.v2.issue;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.version.RemoveVersionAction;
import com.atlassian.jira.bc.project.version.SwapVersionAction;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.event.project.VersionCreatedViaRestEvent;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.v2.issue.version.RemoteVersionLinkResource;
import com.atlassian.jira.rest.v2.issue.version.VersionBean;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionIssueCountsBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionMoveBean;
import com.atlassian.jira.rest.v2.issue.version.VersionUnresolvedIssueCountsBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.bc.project.version.VersionService.CreateVersionValidationResult.Reason.FORBIDDEN;
import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * @since 4.2
 */
@Path("version")
@AnonymousAllowed
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class VersionResource
{
    private VersionService versionService;
    private ProjectService projectService;
    private JiraAuthenticationContext authContext;
    private I18nHelper i18n;
    private RemoteVersionLinkResource remoteVersionLinkResource;
    private VersionBeanFactory versionBeanFactory;
    private VersionIssueCountsBeanFactory versionIssueCountsBeanFactory;
    private VersionUnresolvedIssueCountsBeanFactory versionUnresolvedIssueCountsBeanFactory;
    private DateFieldFormat dateFieldFormat;
    private EventPublisher eventPublisher;

    @SuppressWarnings ( { "UnusedDeclaration" })
    private VersionResource()
    {
        // this constructor used by tooling
    }

    @SuppressWarnings ( { "UnusedDeclaration" })
    public VersionResource(final VersionService versionService, ProjectService projectService,
            final JiraAuthenticationContext authContext, final I18nHelper i18n,
            final RemoteVersionLinkResource remoteVersionLinkResource, final VersionBeanFactory versionBeanFactory,
            final VersionIssueCountsBeanFactory versionIssueCountsBeanFactory,
            final VersionUnresolvedIssueCountsBeanFactory versionUnresolvedIssueCountsBeanFactory,
            final DateFieldFormat dateFieldFormat,
            final EventPublisher eventPublisher)
    {
        this.projectService = projectService;
        this.authContext = authContext;
        this.versionService = versionService;
        this.i18n = i18n;
        this.remoteVersionLinkResource = remoteVersionLinkResource;
        this.versionBeanFactory = versionBeanFactory;
        this.versionIssueCountsBeanFactory = versionIssueCountsBeanFactory;
        this.versionUnresolvedIssueCountsBeanFactory = versionUnresolvedIssueCountsBeanFactory;
        this.dateFieldFormat = dateFieldFormat;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Returns a project version.
     *
     * @param id a String containing the version id
     * @return a project version
     *
     * @response.representation.200.qname
     *      version
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the version exists and the currently authenticated user has permission to view it. Contains a
     *      full representation of the version.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @GET
    @Path("{id}")
    public Response getVersion(@PathParam ("id") final String id, @QueryParam("expand") String expand)
    {
        final Version version = getVersionBy(id);
        final boolean expandOps = expand != null && expand.contains(VersionBean.EXPAND_OPERATIONS);
        final boolean expandRemoteLinks = expand != null && expand.contains(VersionBean.EXPAND_REMOTE_LINKS);
        return Response.ok(versionBeanFactory.createVersionBean(version, expandOps, expandRemoteLinks)).cacheControl(never()).build();
    }



    /**
     * Modify a version via PUT. Any fields present in the PUT will override existing values. As a convenience, if a field
     * is not present, it is silently ignored.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionBean#DOC_EXAMPLE}
     *
     * @response.representation.200.doc
     *      Returned if the version exists and the currently authenticated user has permission to edit it.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to edit the version.
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @PUT
    @Path("{id}")
    public Response updateVersion(@PathParam ("id") final String id, final VersionBean bean)
    {
        if (bean.isStartDateSet() && bean.getUserStartDate() != null)
        {
            return createErrorResponse(Response.Status.BAD_REQUEST, "rest.version.edit.two.start.dates");
        }
        if (bean.isReleaseDateSet() && bean.getUserReleaseDate() != null)
        {
            return createErrorResponse(Response.Status.BAD_REQUEST, "rest.version.edit.two.release.dates");
        }

        final Version version = getVersionBy(id);

        final ServiceOutcome<Date> startDateOutcome = parseDateEdit("startDate", bean.isStartDateSet(), bean.getStartDate(), bean.getUserStartDate(), version.getStartDate());
        final Date startDate = startDateOutcome.isValid() ? startDateOutcome.getReturnedValue() : null;

        final ServiceOutcome<Date> releaseDateOutcome = parseDateEdit("releaseDate", bean.isReleaseDateSet(), bean.getReleaseDate(), bean.getUserReleaseDate(), version.getReleaseDate());
        final Date releaseDate = releaseDateOutcome.isValid() ? releaseDateOutcome.getReturnedValue() : null;

        final VersionService.VersionBuilder builder = versionService.newBuilder(version);
        if (bean.getName() != null)
        {
            builder.name(bean.getName());
        }
        if (bean.getDescription() != null)
        {
            builder.description(bean.getDescription());
        }
        builder.startDate(startDate)
                .releaseDate(releaseDate);

        final VersionService.VersionBuilderValidationResult validationResult = versionService.validateUpdate(getUser(), builder);
        validationResult.getErrorCollection().addErrorCollection(validateArchiveReleaseUpdate(bean, version));
        validationResult.getErrorCollection().addErrorCollection(startDateOutcome.getErrorCollection());
        validationResult.getErrorCollection().addErrorCollection(releaseDateOutcome.getErrorCollection());
        if (!validationResult.isValid())
        {
            throw new BadRequestWebException(ErrorCollection.of(validationResult.getErrorCollection()));
        }

        performUpdate(bean, validationResult);

        return getVersion(id, bean.getExpand());
    }

    private com.atlassian.jira.util.ErrorCollection validateArchiveReleaseUpdate(VersionBean bean, Version currentVersion)
    {
        final com.atlassian.jira.util.ErrorCollection errors = new SimpleErrorCollection();
        if (bean.isArchived() != null)
        {
            errors.addErrorCollection(validateArchived(bean, currentVersion));
        }

        if (bean.isReleased() != null)
        {
            errors.addErrorCollection(validateReleased(bean, currentVersion));
        }

        return errors;
    }

    private com.atlassian.jira.util.ErrorCollection validateArchived(VersionBean bean, Version currentVersion)
    {
        // Only perform archiving if we are not already archived.
        final com.atlassian.jira.util.ErrorCollection errors = new SimpleErrorCollection();
        if (bean.isArchived() && !currentVersion.isArchived())
        {
            final VersionService.ArchiveVersionValidationResult validationResult = versionService.validateArchiveVersion(getUser(), currentVersion);
            errors.addErrorCollection(validationResult.getErrorCollection());
        }
        // Only perform unarchive is we are currently archived
        else if (!bean.isArchived() && currentVersion.isArchived())
        {
            final VersionService.ArchiveVersionValidationResult validationResult = versionService.validateUnarchiveVersion(getUser(), currentVersion);
            errors.addErrorCollection(validationResult.getErrorCollection());
        }
        return errors;
    }

    private com.atlassian.jira.util.ErrorCollection validateReleased(VersionBean bean, Version currentVersion)
    {
        final com.atlassian.jira.util.ErrorCollection errors = new SimpleErrorCollection();
        // Only perform the release if we are currently unreleased
        if (bean.isReleased() && !currentVersion.isReleased())
        {
            final VersionService.ReleaseVersionValidationResult result;
            if (bean.isReleaseDateSet())
            {
                result = versionService.validateReleaseVersion(getUser(), currentVersion, bean.getReleaseDate());
            }
            else if (bean.getUserReleaseDate() != null)
            {
                result = versionService.validateReleaseVersion(getUser(), currentVersion, bean.getUserReleaseDate());
            }
            else // No date sent, preserve the one we have
            {
                result = versionService.validateReleaseVersion(getUser(), currentVersion, currentVersion.getReleaseDate());
            }
            errors.addErrorCollection(result.getErrorCollection());
        }
        // Only perform unrelease if we are currently released
        else if (!bean.isReleased() && currentVersion.isReleased())
        {
            final VersionService.ReleaseVersionValidationResult result;
            if (bean.isReleaseDateSet())
            {
                result = versionService.validateUnreleaseVersion(getUser(), currentVersion, bean.getReleaseDate());
            }
            else if (bean.getUserReleaseDate() != null)
            {
                result = versionService.validateUnreleaseVersion(getUser(), currentVersion, bean.getUserReleaseDate());
            }
            else // No date sent, preserve the one we have
            {
                result = versionService.validateUnreleaseVersion(getUser(), currentVersion, currentVersion.getReleaseDate());
            }
            errors.addErrorCollection(result.getErrorCollection());
        }
        return errors;
    }

    private void performUpdate(VersionBean bean, VersionService.VersionBuilderValidationResult validationResult)
    {
        final ServiceOutcome<Version> updateOutcome = versionService.update(getUser(), validationResult);
        if (!updateOutcome.isValid())
        {
            throwWebException(updateOutcome.getErrorCollection());
        }

        final Version nextVersion = updateOutcome.getReturnedValue();
        if (bean.isArchived() != null)
        {
            updateArchived(bean, nextVersion);
        }

        if (bean.isReleased() != null)
        {
            updateReleased(bean, nextVersion);
        }
    }

    private void updateArchived(VersionBean bean, Version currentVersion)
    {
        // Only perform archiving if we are not already archived.
        if (bean.isArchived() && !currentVersion.isArchived())
        {
            final VersionService.ArchiveVersionValidationResult validationResult = versionService.validateArchiveVersion(getUser(), currentVersion);
            if (validationResult.isValid())
            {
                versionService.archiveVersion(validationResult);
            }
            else
            {
                throw new RESTException(Response.Status.FORBIDDEN, ErrorCollection.of(validationResult.getErrorCollection()));
            }
        }
        // Only perform unarchive is we are currently archived
        else if (!bean.isArchived() && currentVersion.isArchived())
        {
            final VersionService.ArchiveVersionValidationResult validationResult = versionService.validateUnarchiveVersion(getUser(), currentVersion);
            if (validationResult.isValid())
            {
                versionService.unarchiveVersion(validationResult);
            }
            else
            {
                throw new RESTException(Response.Status.FORBIDDEN, ErrorCollection.of(validationResult.getErrorCollection()));
            }
        }
    }

    private void updateReleased(VersionBean bean, Version currentVersion)
    {
        // Only perform the release if we are currently unreleased
        final VersionService.ReleaseVersionValidationResult validationResult;
        if (bean.isReleased() && !currentVersion.isReleased())
        {
            if (bean.isReleaseDateSet())
            {
                validationResult = versionService.validateReleaseVersion(getUser(), currentVersion, bean.getReleaseDate());
            }
            else if (bean.getUserReleaseDate() != null)
            {
                validationResult = versionService.validateReleaseVersion(getUser(), currentVersion, bean.getUserReleaseDate());
            }
            else // No date sent, preserve the one we have
            {
                validationResult = versionService.validateReleaseVersion(getUser(), currentVersion, currentVersion.getReleaseDate());
            }
            if (validationResult.isValid())
            {
                if (bean.getMoveUnfixedIssuesTo() != null)
                {
                    final long moveToId = getVersionIdFromSelfLink(bean.getMoveUnfixedIssuesTo().getPath());
                    final VersionService.VersionResult result = versionService.getVersionById(authContext.getUser(), moveToId);
                    if (!result.isValid())
                    {
                        throw new NotFoundWebException(ErrorCollection.of(result.getErrorCollection()));
                    }

                    versionService.moveUnreleasedToNewVersion(getUser(), currentVersion, result.getVersion());
                }
                versionService.releaseVersion(validationResult);
            }
            else
            {
                throw new RESTException(Response.Status.FORBIDDEN, ErrorCollection.of(validationResult.getErrorCollection()));
            }
        }
        // Only perform unrelease if we are currently released
        else if (!bean.isReleased() && currentVersion.isReleased())
        {
            if (bean.isReleaseDateSet())
            {
                validationResult = versionService.validateUnreleaseVersion(getUser(), currentVersion, bean.getReleaseDate());
            }
            else if (bean.getUserReleaseDate() != null)
            {
                validationResult = versionService.validateUnreleaseVersion(getUser(), currentVersion, bean.getUserReleaseDate());
            }
            else // No date sent, preserve the one we have
            {
                validationResult = versionService.validateUnreleaseVersion(getUser(), currentVersion, currentVersion.getReleaseDate());
            }
            if (validationResult.isValid())
            {
                versionService.unreleaseVersion(validationResult);
            }
            else
            {
                throw new RESTException(Response.Status.FORBIDDEN, ErrorCollection.of(validationResult.getErrorCollection()));
            }
        }
    }



    /**
     * Create a version via POST.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionBean#DOC_CREATE_EXAMPLE}
     *      Supply only one of releaseDate or userReleaseDate but not both.
     *
     * @response.representation.201.mediaType
     *      application/json
     *
     * @response.representation.201.doc
     *      Returned if the version is created successfully.
     *
     * @response.representation.201.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionBean#DOC_CREATE_EXAMPLE}
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to edit the version.
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @POST
    public Response createVersion(final VersionBean bean)
    {
        if (isBlank(bean.getProject()) && bean.getProjectId() == null)
        {
            return createErrorResponse(Response.Status.BAD_REQUEST, "rest.version.create.no.project");
        }
        if (bean.isStartDateSet() && bean.getUserStartDate() != null)
        {
            return createErrorResponse(Response.Status.BAD_REQUEST, "rest.version.create.two.start.dates");
        }
        if (bean.isReleaseDateSet() && bean.getUserReleaseDate() != null)
        {
            return createErrorResponse(Response.Status.BAD_REQUEST, "rest.version.create.two.release.dates");
        }

        final User user = getUser();
        //We must use the manager because you can create a version without browse permission. The service tries to
        //ensure the user has browse permission.
        final ProjectService.GetProjectResult getResult = bean.getProjectId() != null ?
                projectService.getProjectByIdForAction(user, bean.getProjectId(), ProjectAction.EDIT_PROJECT_CONFIG) :
                projectService.getProjectByKeyForAction(user, bean.getProject(), ProjectAction.EDIT_PROJECT_CONFIG);
        if (!getResult.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.version.no.create.permission", bean.getProject())));
        }

        final ServiceOutcome<Date> startDateOutcome = parseDate("startDate", bean.isStartDateSet(), bean.getStartDate(), bean.getUserStartDate());
        final Date startDate = startDateOutcome.isValid() ? startDateOutcome.getReturnedValue() : null;

        final ServiceOutcome<Date> releaseDateOutcome = parseDate("releaseDate", bean.isReleaseDateSet(), bean.getReleaseDate(), bean.getUserReleaseDate());
        final Date releaseDate = releaseDateOutcome.isValid() ? releaseDateOutcome.getReturnedValue() : null;

        final VersionService.VersionBuilder builder = versionService.newBuilder()
                .projectId(getResult.getProject().getId())
                .name(bean.getName())
                .description(bean.getDescription())
                .startDate(startDate)
                .releaseDate(releaseDate);
        final VersionService.VersionBuilderValidationResult validateCreateResult = versionService.validateCreate(user, builder);
        validateCreateResult.getErrorCollection().addErrorCollection(startDateOutcome.getErrorCollection());
        validateCreateResult.getErrorCollection().addErrorCollection(releaseDateOutcome.getErrorCollection());

        if (!validateCreateResult.isValid())
        {
            if (validateCreateResult.getSpecificReasons().contains(FORBIDDEN))
            {
                return createErrorResponse(Response.Status.NOT_FOUND,
                        ErrorCollection.of(i18n.getText("rest.version.no.create.permission", bean.getProject())));
            }
            else
            {
                return createErrorResponse(Response.Status.BAD_REQUEST, validateCreateResult);
            }
        }

        final ServiceOutcome<Version> versionOutcome = versionService.create(user, validateCreateResult);
        if (!versionOutcome.isValid())
        {
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, versionOutcome);
        }

        eventPublisher.publish(new VersionCreatedViaRestEvent(versionOutcome.getReturnedValue()));

        final boolean expandOps = bean.getExpand() != null && bean.getExpand().contains(VersionBean.EXPAND_OPERATIONS);
        final boolean expandRemoteLinks = bean.getExpand() != null && bean.getExpand().contains(VersionBean.EXPAND_REMOTE_LINKS);
        final VersionBean versionBean = versionBeanFactory.createVersionBean(versionOutcome.getReturnedValue(), expandOps, expandRemoteLinks);
        return Response.status(Response.Status.CREATED)
                .entity(versionBean)
                .location(versionBean.getSelf())
                .cacheControl(never()).build();
    }

    private Response createErrorResponse(Response.Status status, String key)
    {
        return createErrorResponse(status, ErrorCollection.of(i18n.getText(key)));
    }

    private Response createErrorResponse(Response.Status status, ServiceResult outcome)
    {
        return createErrorResponse(status, ErrorCollection.of(outcome.getErrorCollection()));
    }

    private Response createErrorResponse(Response.Status status, ErrorCollection errorCollection)
    {
        return Response.status(status).entity(errorCollection).cacheControl(never()).build();
    }

    private ServiceOutcome<Date> parseDate(String fieldName, boolean set, Date date, String dateStr)
    {
        if (set)
        {
            return ServiceOutcomeImpl.ok(date);
        }

        if (StringUtils.isEmpty(dateStr))
        {
            return ServiceOutcomeImpl.ok(null);
        }

        try
        {
            return ServiceOutcomeImpl.ok(dateFieldFormat.parseDatePicker(dateStr));
        }
        catch (IllegalArgumentException exc)
        {
            final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            errorCollection.addError(fieldName, i18n.getText("admin.errors.incorrect.date.format", dateFieldFormat.getFormatHint()));
            return ServiceOutcomeImpl.from(errorCollection, null);
        }
    }

    private ServiceOutcome<Date> parseDateEdit(String fieldName, boolean set, Date date, String dateStr, Date originalDate)
    {
        if (set)
        {
            if (date == null || !date.equals(originalDate))
            {
                return ServiceOutcomeImpl.ok(date);
            }
            else
            {
                return ServiceOutcomeImpl.ok(originalDate);
            }
        }

        if (dateStr == null)
        {
            return ServiceOutcomeImpl.ok(originalDate);
        }

        if (dateStr.length() == 0)
        {
            return ServiceOutcomeImpl.ok(null);
        }

        try
        {
            return ServiceOutcomeImpl.ok(dateFieldFormat.parseDatePicker(dateStr));
        }
        catch (IllegalArgumentException exc)
        {
            final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            errorCollection.addError(fieldName, i18n.getText("admin.errors.incorrect.date.format", dateFieldFormat.getFormatHint()));
            return ServiceOutcomeImpl.from(errorCollection, null);
        }
    }



    /**
     * Delete a project version.
     * @param id The version to delete
     * @param moveFixIssuesTo The version to set fixVersion to on issues where the deleted version is the fix version,
     * If null then the fixVersion is removed.
     * @param moveAffectedIssuesTo The version to set affectedVersion to on issues where the deleted version is the affected version,
     * If null then the affectedVersion is removed.
     * @return An empty or error response.
     *
     *
     * @response.representation.204.doc
     *      Returned if the version is successfully deleted.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to delete the version.
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @DELETE
    @Path("{id}")
    public Response delete(@PathParam ("id") final String id, @QueryParam ("moveFixIssuesTo") String moveFixIssuesTo,
            @QueryParam("moveAffectedIssuesTo") String moveAffectedIssuesTo)
    {
        final Version version = getVersionBy(id);

        // Get the actions to handle on delete
        final VersionService.VersionAction fixAction;
        final VersionService.VersionAction affectedAction;
        if (moveFixIssuesTo != null)
        {
            fixAction = new SwapVersionAction(getVersionIdFromSelfLink(moveFixIssuesTo));
        }
        else
        {
            fixAction = new RemoveVersionAction();
        }
        if (moveAffectedIssuesTo != null)
        {
            affectedAction = new SwapVersionAction(getVersionIdFromSelfLink(moveAffectedIssuesTo));
        }
        else
        {
            affectedAction = new RemoveVersionAction();
        }
        final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(authContext.getUser());
        final VersionService.ValidationResult deleteValidationResult =
                versionService.validateDelete(serviceContext, version.getId(), affectedAction, fixAction);

        checkDeleteResult(deleteValidationResult);

        versionService.delete(serviceContext, deleteValidationResult);

        return Response.noContent().cacheControl(never()).build();
    }

    private void checkDeleteResult(VersionService.ValidationResult validationResult)
    {
        if (!validationResult.isValid())
        {
            if (validationResult.getReasons().contains(VersionService.ValidationResult.Reason.FORBIDDEN))
            {
                throw new NotAuthorisedWebException(ErrorCollection.of(validationResult.getErrorCollection()));
            }
            if (validationResult.getReasons().contains(VersionService.MoveVersionValidationResult.Reason.NOT_FOUND))
            {
                throw new NotFoundWebException(ErrorCollection.of(validationResult.getErrorCollection()));
            }
            else
            {
                throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(validationResult.getErrorCollection()));
            }
        }
    }



    /**
     * Returns a bean containing the number of fixed in and affected issues for the given version.
     *
     * @param id a String containing the version id
     * @return an issue counts bean
     *
     * @response.representation.200.qname
     *      issue Count Bean
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the version exists and the currently authenticated user has permission to view it. Contains
     *      counts of issues fixed in and affecting this version.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionIssueCountsBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @GET
    @Path("{id}/relatedIssueCounts")
    public Response getVersionRelatedIssues(@PathParam ("id") final String id)
    {
        final Version version = getVersionBy(id);

        final long fixIssueCount = versionService.getFixIssuesCount(version);
        final long affectsIssueCount = versionService.getAffectsIssuesCount(version);

        return Response.ok(versionIssueCountsBeanFactory.createVersionBean(
                version, fixIssueCount, affectsIssueCount)).cacheControl(never()).build();
    }

    /**
     * Returns the number of unresolved issues for the given version
     *
     * @param id a String containing the version id
     * @return an unresolved issue count bean
     *
     * @response.representation.200.qname
     *      issuesUnresolvedCount
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the version exists and the currently authenticated user has permission to view it. Contains
     *      counts of issues unresolved in this version.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionUnresolvedIssueCountsBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @GET
    @Path("{id}/unresolvedIssueCount")
    public Response getVersionUnresolvedIssues(@PathParam ("id") final String id)
    {
        final User loggedInUser = getUser();
        final Version version = getVersionBy(id);

        final long unresolvedIssueCount = versionService.getUnresolvedIssuesCount(loggedInUser, version);

        return Response.ok(versionUnresolvedIssueCountsBeanFactory.createVersionBean(
                version, unresolvedIssueCount)).cacheControl(never()).build();
    }



    /**
     * Modify a version's sequence within a project.
     *
     * The move version bean has 2 alternative field value pairs:
     * <dl>
     *     <dt>position</dt><dd>An absolute position, which may have a value of 'First', 'Last', 'Earlier' or 'Later'</dd>
     *     <dt>after</dt><dd>A version to place this version after.  The value should be the self link of another version</dd>
     * </dl>
     *
     * @param id a String containing the version id
     * @param bean a MoveVersionBean that describes the move to be performed.
     * @return a project version
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionMoveBean#DOC_EXAMPLE}
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionMoveBean#DOC_EXAMPLE2}
     *
     * @response.representation.200.qname
     *      version
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the version exists and the currently authenticated user has permission to view it. Contains a
     *      full representation of the version moved.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the version, or target of the version to move after does not exist or the currently authenticated
     *      user does not have permission to view it.
     */
    @POST
    @Path("/{id}/move")
    public Response moveVersion(@PathParam ("id") final String id, VersionMoveBean bean)
    {
        // Backbone makes it really hard to plug into the move method, so for ease all move methods get the operations
        final String expand = "operations";

        final long versionId = parseVersionId(id);

        final User user = getUser();
        // The version can be moved to the top or bottom or after another version
        if (bean.position != null)
        {
            switch (bean.position)
            {
                case Earlier:
                {
                    final VersionService.MoveVersionValidationResult moveValidationResult = versionService.validateIncreaseVersionSequence(user, versionId);
                    checkMoveResult(moveValidationResult);
                    versionService.increaseVersionSequence(moveValidationResult);
                    break;
                }
                case Later:
                {
                    final VersionService.MoveVersionValidationResult moveValidationResult = versionService.validateDecreaseVersionSequence(user, versionId);
                    checkMoveResult(moveValidationResult);
                    versionService.decreaseVersionSequence(moveValidationResult);
                    break;
                }
                case First:
                {
                    final VersionService.MoveVersionValidationResult moveValidationResult = versionService.validateMoveToStartVersionSequence(user, versionId);
                    checkMoveResult(moveValidationResult);
                    versionService.moveToStartVersionSequence(moveValidationResult);
                    break;
                }
                case Last:
                {
                    final VersionService.MoveVersionValidationResult moveValidationResult = versionService.validateMoveToEndVersionSequence(user, versionId);
                    checkMoveResult(moveValidationResult);
                    versionService.moveToEndVersionSequence(moveValidationResult);
                    break;
                }
                default :
                {
                    throw new RESTException(Response.Status.BAD_REQUEST, i18n.getText("admin.errors.version.move.target.invalid"));
                }
            }
        }
        else if (bean.after != null)
        {
            // Get the id from the bean.after URI
            final long afterVersionId = getVersionIdFromSelfLink(bean.after.getPath());
            final VersionService.MoveVersionValidationResult moveValidationResult = versionService.validateMoveVersionAfter(user, versionId, afterVersionId);
            checkMoveResult(moveValidationResult);
            versionService.moveVersionAfter(moveValidationResult);
        }

        return getVersion(id, expand);
    }



    /**
     * Returns the remote version links for a given global ID.
     *
     * @param globalId the global ID of the remote resource that is linked to the versions
     * @return a remote version link
     *
     * @response.representation.200.qname
     *      remoteVersionLinks
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Contains a list of remote version links.  Any existing links that the user does not
     *      have permission to see will be filtered out.  The user must have BROWSE permission
     *      for the project to see links to its versions.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.RemoteVersionLinkResourceExamples#LINKS_BY_GLOBAL_ID}
     */
    @GET
    @Path("remotelink")
    public Response getRemoteVersionLinks(@QueryParam("globalId") final String globalId)
    {
        return remoteVersionLinkResource.getRemoteVersionLinksByGlobalId(globalId);
    }




    /**
     * Returns the remote version links associated with the given version ID.
     *
     * @param versionId a String containing the version ID
     * @return the remote links for that project version
     *
     * @response.representation.200.qname
     *      remoteVersionLinks
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the version exists and the currently authenticated user has permission to view it, which
     *      is restricted to those users with BROWSE permission for the project. Contains a full representation
     *      of the remote version links.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.RemoteVersionLinkResourceExamples#LINKS_BY_VERSION_ID}
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @GET
    @Path("{versionId}/remotelink")
    public Response getRemoteVersionLinksByVersionId(@PathParam("versionId") final String versionId)
    {
        return remoteVersionLinkResource.getRemoteVersionLinksByVersionId(parseVersionId(versionId));
    }

    /**
     * A REST sub-resource representing a remote version link
     *
     * @param versionId a String containing the version id
     * @param globalId The id of the remote issue link to be returned.  If {@code null} (not provided) all
     *          remote links for the issue are returned.
     * <p>
     * Remote version links follow the same general rules that Issue Links do, except that they are permitted to
     * use any arbitrary well-formed JSON data format with no restrictions imposed.  It is recommended, but not
     * required, that they follow the same format used for Remote Issue Links, as described at
     * <a href="https://developer.atlassian.com/display/JIRADEV/Fields+in+Remote+Issue+Links">https://developer.atlassian.com/display/JIRADEV/Fields+in+Remote+Issue+Links</a>.
     * </p>
     *
     * @return if no globalId is specified, a {@code Response} containing a {@code RemoteEntityLinksJsonBean}s
     *      is returned.  Otherwise, a Response containing a {@code RemoteEntityLinkJsonBean} with the given
     *      {@code globalId} is returned.
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Information on the remote version link (or links)
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.RemoteVersionLinkResourceExamples#LINK1}
     *
     * @response.representation.404.doc
     *      Returned if the version or remote version link does not exist or if the user does not have
     *      the BROWSE permission for the project that owns the specified version
     */
    @GET
    @Path("{versionId}/remotelink/{globalId}")
    public Response getRemoteVersionLink(@PathParam("versionId") final String versionId, @PathParam("globalId") final String globalId)
    {
        if (StringUtils.isBlank(globalId))
        {
            return remoteVersionLinkResource.getRemoteVersionLinksByVersionId(parseVersionId(versionId));
        }
        return remoteVersionLinkResource.getRemoteVersionLink(parseVersionId(versionId), globalId);
    }

    /**
     * Create a remote version link via POST.  The link's global ID will be taken from the
     * JSON payload if provided; otherwise, it will be generated.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.RemoteVersionLinkResourceExamples#CREATE_OR_UPDATE}
     *
     * @response.representation.201.doc
     *      Returned if the version is created successfully.  The document will has no content,
     *      and a {@code Location:} header contains the self-URI for the newly created link.
     *
     * @response.representation.201.example
     *      Returned if the remote version link is created or updated successfully.
     *      The document has no content, and a {@code Location:} header contains the
     *      self-URI for the newly created link.
     *
     * @response.representation.400.doc
     *      Returned if the JSON payload is empty or malformed
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to edit the version.
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @POST
    @Path("{versionId}/remotelink")
    public Response createOrUpdateRemoteVersionLink(
            @PathParam("versionId") final String versionId,
            String json)
    {
        return remoteVersionLinkResource.putRemoteVersionLink(parseVersionId(versionId), null, json);
    }


    /**
     * Create a remote version link via POST using the provided global ID.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.RemoteVersionLinkResourceExamples#CREATE_OR_UPDATE}
     *
     * @response.representation.201.doc
     *      Returned if the remote version link is created or updated successfully.
     *      The document has no content, and a {@code Location:} header contains the
     *      self-URI for the newly created link.
     *
     * @response.representation.400.doc
     *      Returned if the JSON payload is empty or malformed
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to edit the version.
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @POST
    @Path("{versionId}/remotelink/{globalId}")
    public Response createOrUpdateRemoteVersionLink(
            @PathParam("versionId") final String versionId,
            @PathParam("globalId") final String globalId,
            String json)
    {
        return remoteVersionLinkResource.putRemoteVersionLink(parseVersionId(versionId), globalId, json);
    }


    /**
     * Delete all remote version links for a given version ID.
     *
     * @param versionId The version for which to delete ALL existing remote version links
     * @return An empty or error response.
     *
     * @response.representation.204.doc
     *      Returned if the remote version links are successfully deleted.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have administrative rights to the project
     *      and thereby cannot delete remote links to the version.
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist, the currently authenticated user does not have permission to
     *      view it, or the version does not have any remote links to delete
     */
    @DELETE
    @Path("{versionId}/remotelink")
    public Response deleteRemoteVersionLinksByVersionId(
            @PathParam("versionId") final String versionId)
    {
        return remoteVersionLinkResource.deleteRemoteVersionLinksByVersionId(parseVersionId(versionId));
    }




    /**
     * Delete a specific remote version link with the given version ID and global ID.
     *
     * @param versionId The version ID of the remote link
     * @param globalId The global ID of the remote link
     * @return An empty or error response.
     *
     * @response.representation.204.doc
     *      Returned if the remote version link is successfully deleted.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have administrative rights to the project
     *      and thereby cannot delete remote links to the version.
     *
     * @response.representation.404.doc
     *      Returned if the version does not exist, the currently authenticated user does not have permission to
     *      view it, or the version does not have a link for the given global ID
     */
     @DELETE
    @Path("{versionId}/remotelink/{globalId}")
    public Response deleteRemoteVersionLink(
            @PathParam("versionId") final String versionId,
            @PathParam("globalId") final String globalId)
    {
        return remoteVersionLinkResource.deleteRemoteVersionLink(parseVersionId(versionId), globalId);
    }



    private long getVersionIdFromSelfLink(String path)
    {
        return parseVersionId(path.substring(path.lastIndexOf('/') + 1));
    }

    private void checkMoveResult(VersionService.MoveVersionValidationResult moveValidationResult)
    {
        if (moveValidationResult.getErrorCollection().hasAnyErrors())
        {
            if (moveValidationResult.getReasons().contains(VersionService.MoveVersionValidationResult.Reason.FORBIDDEN))
            {
                throw new NotAuthorisedWebException(ErrorCollection.of(moveValidationResult.getErrorCollection()));
            }
            if (moveValidationResult.getReasons().contains(VersionService.MoveVersionValidationResult.Reason.NOT_FOUND) ||
                moveValidationResult.getReasons().contains(VersionService.MoveVersionValidationResult.Reason.SCHEDULE_AFTER_VERSION_NOT_FOUND))
            {
                throw new NotFoundWebException(ErrorCollection.of(moveValidationResult.getErrorCollection()));
            }
            else
            {
                throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(moveValidationResult.getErrorCollection()));
            }
        }
    }

    private void throwWebException(com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        throw new RESTException(ErrorCollection.of(errorCollection));
    }

    private long parseVersionId(String id)
    {
        try
        {
            return Long.parseLong(id);
        }
        catch (NumberFormatException exc)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.errors.version.not.exist.with.id", id)));
        }
    }

    private Version getVersionBy(String id)
    {
        final Long versionId = parseVersionId(id);
        final VersionService.VersionResult versionResult = versionService.getVersionById(authContext.getUser(), versionId);
        if (!versionResult.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(versionResult.getErrorCollection()));
        }
        return versionResult.getVersion();
    }

    private User getUser()
    {
        return ApplicationUsers.toDirectoryUser(authContext.getUser());
    }
}
