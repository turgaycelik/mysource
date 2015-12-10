package com.atlassian.jira.rest.v2.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.atlassian.fugue.Either;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarPickerHelper;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentService;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.ProjectJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.StatusJsonBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.api.issue.IssueTypeWithStatusJsonBean;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.util.AttachmentHelper;
import com.atlassian.jira.rest.util.StatusHelper;
import com.atlassian.jira.rest.v2.issue.component.ComponentBean;
import com.atlassian.jira.rest.v2.issue.project.ProjectBeanFactory;
import com.atlassian.jira.rest.v2.issue.project.ProjectFinder;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugins.rest.common.multipart.FilePart;
import com.atlassian.plugins.rest.common.multipart.MultipartFormParam;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.XsrfCheckFailedException;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * @since 4.2
 */
@Path ("project")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class ProjectResource
{
    private ProjectService projectService;
    private UserManager userManager;
    private JiraAuthenticationContext authContext;
    private UriInfo uriInfo;
    private ProjectManager projectManager;
    private AvatarResourceHelper avatarResourceHelper;
    private VersionService versionService;
    private ProjectComponentService projectComponentService;
    private ProjectBeanFactory projectBeanFactory;
    private VersionBeanFactory versionBeanFactory;
    private PermissionManager permissionManager;
    private AvatarService avatarService;
    private JiraBaseUrls jiraBaseUrls;
	private WorkflowManager workflowManager;
	private IssueTypeSchemeManager issueTypeSchemeManager;
	private ResourceUriBuilder uriBuilder;
	private StatusHelper statusHelper;
    private ProjectFinder projectFinder;
    private XsrfInvocationChecker xsrfChecker;

    private ProjectResource()
    {
        // this constructor used by tooling
    }

    public ProjectResource(
			final ProjectService projectService,
			final JiraAuthenticationContext authContext,
			final UriInfo uriInfo,
			final VersionService versionService,
			final ProjectComponentService projectComponentService,
			final AvatarService avatarService,
			final UserManager userManager,
			final ProjectBeanFactory projectBeanFactory,
			final VersionBeanFactory versionBeanFactory,
			final PermissionManager permissionManager,
			final ProjectManager projectManager,
			final AvatarManager avatarManager,
			final AvatarPickerHelper avatarPickerHelper,
			final AttachmentHelper attachmentHelper,
			final JiraBaseUrls jiraBaseUrls,
			final WorkflowManager workflowManager,
			final IssueTypeSchemeManager issueTypeSchemeManager,
			final ResourceUriBuilder uriBuilder,
			final StatusHelper statusHelper,
            final ProjectFinder projectFinder,
            final XsrfInvocationChecker xsrfChecker)
    {
		this.workflowManager = workflowManager;
		this.issueTypeSchemeManager = issueTypeSchemeManager;
		this.uriBuilder = uriBuilder;
		this.statusHelper = statusHelper;
        this.projectFinder = projectFinder;
        this.avatarResourceHelper = new AvatarResourceHelper(authContext, avatarManager, avatarPickerHelper, attachmentHelper, userManager);
        this.permissionManager = permissionManager;
        this.avatarService = avatarService;
        this.projectService = projectService;
        this.authContext = authContext;
        this.versionService = versionService;
        this.projectComponentService = projectComponentService;
        this.userManager = userManager;
        this.projectBeanFactory = projectBeanFactory;
        this.versionBeanFactory = versionBeanFactory;
        this.uriInfo = uriInfo;
        this.projectManager = projectManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.xsrfChecker = xsrfChecker;
    }

    /**
     * Contains a full representation of a project in JSON format.
     *
     * <p>
     * All project keys associated with the project will only be returned if <code>expand=projectKeys</code>.
     * <p>
     *
     * @param projectIdOrKey the project id or project key
     * @return a project
     *
     * @response.representation.200.qname
     *      project
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the project exists and the user has permission to view it. Contains a full representation
     *      of a project in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.project.ProjectBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the project is not found, or the calling user does not have permission to view it.
     */
    @GET
    @Path ("{projectIdOrKey}")
    public Response getProject(@PathParam ("projectIdOrKey") final String projectIdOrKey, @QueryParam ("expand") final String expand)
    {
        return getProjectForView(projectIdOrKey).left().on(new Function<Project, Response>()
        {
            @Override
            public Response apply(final Project project)
            {
                return Response.ok(projectBeanFactory.fullProject(project, StringUtils.defaultString(expand))).cacheControl(never()).build();
            }
        });
    }

    /**
     * Contains a full representation of a the specified project's versions.
     *
     * @param projectIdOrKey the project id or project key
     * @return the passed project's versions.
     *
     * @response.representation.200.qname
     *      versions
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the project exists and the user has permission to view its versions. Contains a full
     *      representation of the project's versions in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.version.VersionBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.404.doc
     *      Returned if the project is not found, or the calling user does not have permission to view it.
     */
    @GET
    @Path ("{projectIdOrKey}/versions")
    public Response getProjectVersions(@PathParam ("projectIdOrKey") final String projectIdOrKey, @QueryParam ("expand") final String expand)
    {
        return getProjectForView(projectIdOrKey).left().on(new Function<Project, Response>()
        {
            @Override
            public Response apply(final Project project)
            {
                final VersionService.VersionsResult versionResult = versionService.getVersionsByProject(authContext.getLoggedInUser(), project);
                if (!versionResult.isValid())
                {
                    throw new NotFoundWebException(ErrorCollection.of(versionResult.getErrorCollection()));
                }

                final boolean expandOps = expand != null && expand.contains("operations");
                return Response.ok(versionBeanFactory.createVersionBeans(versionResult.getVersions(), expandOps)).cacheControl(never()).build();
            }
        });
    }

    /**
     * Contains a full representation of a the specified project's components.
     *
     * @param projectIdOrKey the project id or project key
     * @return the passed project's components.
     *
     * @response.representation.200.qname
     *      components
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the project exists and the user has permission to view its components. Contains a full
     *      representation of the project's components in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.component.ComponentBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.404.doc
     *      Returned if the project is not found, or the calling user does not have permission to view it.
     */
    @GET
    @Path ("{projectIdOrKey}/components")
    public Response getProjectComponents(@PathParam ("projectIdOrKey") final String projectIdOrKey)
    {
        return getProjectForView(projectIdOrKey).left().on(new Function<Project, Response>()
        {
            @Override
            public Response apply(final Project project)
            {
                final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
                final Collection<ProjectComponent> projectComponents = projectComponentService.findAllForProject(errorCollection, project.getId());
                if (errorCollection.hasAnyErrors())
                {
                    throw new NotFoundWebException(ErrorCollection.of(errorCollection));
                }

                final Long assigneeType = project.getAssigneeType();
                final long safeAssigneeType = assigneeType == null ? AssigneeTypes.PROJECT_LEAD : assigneeType;
                return Response.ok(ComponentBean.asFullBeans(projectComponents, jiraBaseUrls, project.getLeadUserName(), safeAssigneeType, userManager, avatarService, permissionManager, projectManager)).cacheControl(never()).build();
            }
        });
    }

    /**
     * Returns all projects which are visible for the currently logged in user. If no user is logged in, it returns the
     * list of projects that are visible when using anonymous access.
     *
     * @since v4.3
     *
     * @return all projects for which the user has the BROWSE project permission. If no user is logged in,
     *         it returns all projects, which are visible when using anonymous access.
     *
     * @response.representation.200.qname
     *      projects
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a list of projects for which the user has the BROWSE, ADMINISTER or PROJECT_ADMIN project permission.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.project.ProjectBean#PROJECTS_EXAMPLE}
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the list of projects.
     */
    @GET
    public Response getAllProjects()
    {
        final ServiceOutcome<List<Project>> outcome = projectService.getAllProjectsForAction(authContext.getUser(), ProjectAction.VIEW_PROJECT);
        if (outcome.getErrorCollection().hasAnyErrors())
        {
            final ErrorCollection errors = ErrorCollection.of(outcome.getErrorCollection());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errors).cacheControl(never()).build();
        }
        else
        {
            final List<ProjectJsonBean> beans = new ArrayList<ProjectJsonBean>();
            for (Project project : outcome.getReturnedValue())
            {
                final ProjectJsonBean projectBean = ProjectJsonBean.shortBean(project, jiraBaseUrls);
                beans.add(projectBean);
            }
            return Response.ok(beans).cacheControl(never()).build();
        }
    }

    /**
     * Returns all avatars which are visible for the currently logged in user.  The avatars are grouped into
     * system and custom.
     *
     * @since v5.0
     * @param projectIdOrKey project id or project key
     *
     * @return all avatars for which the user has the BROWSE project permission.
     *
     * @response.representation.200.qname
     *      avatars
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a map containing a list of avatars for both custom an system avatars, which the user has the BROWSE project permission.
     *
     * @response.representation.200.example
     *      {@link AvatarBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.404.doc
     *      Returned if the currently authenticated user does not have VIEW PROJECT permission.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the list of avatars.
     */
    @GET
    @Path ("{projectIdOrKey}/avatars")
    public Response getAllAvatars(@PathParam ("projectIdOrKey") final String projectIdOrKey)
    {
        return getProjectForView(projectIdOrKey).left().on(new Function<Project, Response>()
        {
            @Override
            public Response apply(final Project project)
            {
                final Avatar selectedAvatar = project.getAvatar();
                final Long selectedAvatarId = selectedAvatar.getId();

                return Response.ok(avatarResourceHelper.getAllAvatars(Avatar.Type.PROJECT, project.getId().toString(), selectedAvatarId)).build();
            }
        });
    }

    /**
     * Converts temporary avatar into a real avatar
     *
     * @param projectIdOrKey project id or project key
     * @param croppingInstructions cropping instructions
     * @return created avatar
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.AvatarCroppingBean#DOC_EDIT_EXAMPLE}
     *
     * @response.representation.201.qname
     *      avatar
     *
     * @response.representation.201.mediaType
     *      application/json
     *
     * @response.representation.201.doc
     *      Returns created avatar
     *
     * @response.representation.201.example
     *      {@link AvatarBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *      Returned if the cropping coordinates are invalid
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to pick avatar
     *
     * @response.representation.404.doc
     *      Returned if the currently authenticated user does not have EDIT PROJECT permission.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while converting temporary avatar to real avatar
     */
    @POST
    @Path ("{projectIdOrKey}/avatar")
    public Response createAvatarFromTemporary(@PathParam ("projectIdOrKey") final String projectIdOrKey, final AvatarCroppingBean croppingInstructions)
    {
        return getProjectForEdit(projectIdOrKey).left().on(new Function<Project, Response>()
        {
            @Override
            public Response apply(final Project project)
            {
                return avatarResourceHelper.createAvatarFromTemporary(Avatar.Type.PROJECT, project.getId().toString(), croppingInstructions);
            }
        });
    }

    @PUT
    @Path ("{projectIdOrKey}/avatar")
    public Response updateProjectAvatar(final @PathParam ("projectIdOrKey") String projectIdOrKey, final AvatarBean avatarBean)
    {
        return getProjectForEdit(projectIdOrKey).left().on(new Function<Project, Response>()
        {
            @Override
            public Response apply(final Project project)
            {
                final String id = avatarBean.getId();
                Long avatarId;
                try
                {
                    avatarId = id == null ? null : Long.valueOf(id);
                }
                catch (NumberFormatException e)
                {
                    avatarId = null;
                }
                final ProjectService.UpdateProjectValidationResult updateProjectValidationResult =
                        projectService.validateUpdateProject(authContext.getLoggedInUser(), project.getName(), project.getKey(),
                                project.getDescription(), project.getLeadUserName(), project.getUrl(), project.getAssigneeType(),
                                avatarId);

                if (!updateProjectValidationResult.isValid())
                {
                    throwWebException(updateProjectValidationResult.getErrorCollection());
                }

                projectService.updateProject(updateProjectValidationResult);
                return Response.status(Response.Status.NO_CONTENT).cacheControl(never()).build();
            }
        });
    }


    /**
     * Creates temporary avatar
     *
     * @since v5.0
     *
     * @param projectIdOrKey Project id or project key
     * @param filename name of file being uploaded
     * @param size size of file
     * @param request servlet request
     * @return temporary avatar cropping instructions
     *
     * @response.representation.201.qname
     *      avatar
     *
     * @response.representation.201.mediaType
     *      application/json
     *
     * @response.representation.201.doc
     *      temporary avatar cropping instructions
     *
     * @response.representation.201.example
     *      {@link com.atlassian.jira.rest.v2.issue.AvatarCroppingBean#DOC_EXAMPLE}
     *
     * @response.representation.403.doc
     *      Returned if the request does not contain a valid XSRF token
     *
     * @response.representation.400.doc
     *      Validation failed. For example filesize is beyond max attachment size.
     *
     * @response.representation.404.doc
     *      Returned if the currently authenticated user does not have EDIT PROJECT permission.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while converting temporary avatar to real avatar
     */
    @POST
    @Consumes (MediaType.WILDCARD)
    @Path ("{projectIdOrKey}/avatar/temporary")
    public Response storeTemporaryAvatar(final @PathParam ("projectIdOrKey") String projectIdOrKey, final @QueryParam ("filename") String filename,
            final @QueryParam ("size") Long size, final @Context HttpServletRequest request)
    {
        final XsrfCheckResult xsrfCheckResult = xsrfChecker.checkWebRequestInvocation(ExecutingHttpRequest.get());
        if (xsrfCheckResult.isRequired() && !xsrfCheckResult.isValid())
        {
            throw new XsrfCheckFailedException();
        }

        return getProjectForEdit(projectIdOrKey).left().on(new Function<Project, Response>()
        {
            @Override
            public Response apply(final Project project)
            {
                return avatarResourceHelper.storeTemporaryAvatar(Avatar.Type.PROJECT, project.getId().toString(), filename, size, request);
            }
        });
    }

    /**
     * Creates temporary avatar using multipart. The response is sent back as JSON stored in a textarea. This is because
     * the client uses remote iframing to submit avatars using multipart. So we must send them a valid HTML page back from
     * which the client parses the JSON.
     *
     * @since v5.0
     *
     * @param projectIdOrKey Project id or project key
     * @param request servlet request
     *
     * @return temporary avatar cropping instructions
     *
     * @response.representation.201.qname
     *      avatar
     *
     * @response.representation.201.mediaType
     *      text/html
     *
     * @response.representation.201.doc
     *      temporary avatar cropping instructions embeded in HTML page. Error messages will also be embeded in the page.
     *
     * @response.representation.201.example
     *      {@link com.atlassian.jira.rest.v2.issue.AvatarCroppingBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the currently authenticated user does not have EDIT PROJECT permission.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while converting temporary avatar to real avatar
     */
    @POST
    @Consumes (MediaType.MULTIPART_FORM_DATA)
    @Path ("{projectIdOrKey}/avatar/temporary")
    @Produces ({ MediaType.TEXT_HTML })
    public Response storeTemporaryAvatarUsingMultiPart(@PathParam ("projectIdOrKey") String projectIdOrKey, final @MultipartFormParam ("avatar") FilePart filePart, final @Context HttpServletRequest request)
    {
        final XsrfCheckResult xsrfCheckResult = xsrfChecker.checkWebRequestInvocation(ExecutingHttpRequest.get());
        if (xsrfCheckResult.isRequired() && !xsrfCheckResult.isValid())
        {
            throw new XsrfCheckFailedException();
        }

        return getProjectForEdit(projectIdOrKey).left().on(new Function<Project, Response>()
        {
            @Override
            public Response apply(final Project project)
            {
                return avatarResourceHelper.storeTemporaryAvatarUsingMultiPart(Avatar.Type.PROJECT, project.getId().toString(), filePart, request);
            }
        });
    }


    /**
     * Deletes avatar
     *
     * @since v5.0
     *
     * @param projectIdOrKey Project id or project key
     * @param id database id for avatar
     * @return temporary avatar cropping instructions
     *
     * @response.representation.204.mediaType
     *      application/json
     *
     * @response.representation.204.doc
     *      Returned if the avatar is successfully deleted.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to delete the component.
     *
     * @response.representation.404.doc
     *      Returned if the avatar does not exist or the currently authenticated user does not have permission to
     *      delete it.
     */
    @DELETE
    @Path ("{projectIdOrKey}/avatar/{id}")
    public Response deleteAvatar(final @PathParam ("projectIdOrKey") String projectIdOrKey, final @PathParam ("id") Long id)
    {
        return getProjectForEdit(projectIdOrKey).left().on(new Function<Project, Response>()
        {
            @Override
            public Response apply(final Project project)
            {
                return avatarResourceHelper.deleteAvatar(id);
            }
        });
    }


	/**
	 * Get all issue types with valid status values for a project
	 *
	 * @since v6.0
	 *
	 * @param projectIdOrKey Project id or project key
	 * @return collection of issue types with possi
	 *
	 * @response.representation.200.mediaType
	 *      application/json
	 *
	 * @response.representation.200.doc
	 *      Returned if the project exists and the user has permission to view its components. Contains a full
	 *      representation of issue types with status values which are valid for each issue type.
	 *
	 * @response.representation.200.example
	 *     {@link com.atlassian.jira.rest.v2.issue.IssueTypeWithStatusJsonBeanExample#DOC_EXAMPLE}
	 *
	 * @response.representation.400.doc
	 *      Returned if the project is not found, or the calling user does not have permission to view it.
	 */
    @GET
    @Path ("{projectIdOrKey}/statuses")
    public Response getAllStatuses(@PathParam ("projectIdOrKey") final String projectIdOrKey)
    {
        return getEitherProjectForViewOrErrors(projectIdOrKey).left().map(asErrorResponse(Response.Status.BAD_REQUEST)).left().on(new Function<Project, Response>()
        {
            @Override
            public Response apply(final Project project)
            {
                final Collection<IssueType> issueTypesForProject = issueTypeSchemeManager.getIssueTypesForProject(project);
                final Collection<IssueTypeWithStatusJsonBean> issueTypesWithStatuses = Lists.newArrayList();

                for (final IssueType issueType : issueTypesForProject)
                {
                    final JiraWorkflow workflow = workflowManager.getWorkflow(project.getId(), issueType.getId());
                    final ImmutableList<StatusJsonBean> statusJsonBeans = ImmutableList.copyOf(
                            Iterables.transform(workflow.getLinkedStatusObjects(), new Function<Status, StatusJsonBean>()
                            {
                                @Override
                                public StatusJsonBean apply(final Status status)
                                {
                                    return statusHelper.createStatusBean(status, uriInfo, StatusResource.class);
                                }
                            }));

                    issueTypesWithStatuses.add(createIssueTypeWithStatuses(issueType, statusJsonBeans));
                }
                return Response.ok(issueTypesWithStatuses).cacheControl(NO_CACHE).build();
            }
        });
    }

	private void throwWebException(final com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        throw new RESTException(ErrorCollection.of(errorCollection));
    }

	private IssueTypeWithStatusJsonBean createIssueTypeWithStatuses(final IssueType issueType, final ImmutableList<StatusJsonBean> statusJsonBeans)
	{
		return new IssueTypeWithStatusJsonBean(
				uriBuilder.build(uriInfo, IssueTypeResource.class, issueType.getId()).toString(),
				issueType.getId(),
				issueType.getName(),
				issueType.isSubTask(),
				statusJsonBeans);
	}

    private Either<Response, Project> getProjectForView(final String projectIdOrKey)
    {
        return getEitherProjectForViewOrErrors(projectIdOrKey).left().map(asErrorResponse(Response.Status.NOT_FOUND));
    }

    private Either<Response, Project> getProjectForEdit(final String projectIdOrKey)
    {
        return getEitherProjectForEditOrErrors(projectIdOrKey).left().map(asErrorResponse(Response.Status.NOT_FOUND));
    }

    private Either<ErrorCollection, Project> getEitherProjectForViewOrErrors(final String projectIdOrKey)
    {
        return getEitherProjectOrErrors(projectIdOrKey, ProjectAction.VIEW_PROJECT);
    }

    private Either<ErrorCollection, Project> getEitherProjectForEditOrErrors(final String projectIdOrKey)
    {
        return getEitherProjectOrErrors(projectIdOrKey, ProjectAction.EDIT_PROJECT_CONFIG);
    }

    private Either<ErrorCollection, Project> getEitherProjectOrErrors(final String projectIdOrKey, final ProjectAction action)
    {
        final ProjectService.GetProjectResult projectResult = projectFinder.getGetProjectForActionByIdOrKey(authContext.getUser(), projectIdOrKey, action);

        if (!projectResult.isValid())
        {
            return Either.left(ErrorCollection.of(projectResult.getErrorCollection()));
        }
        return Either.right(projectResult.getProject());
    }

    private Function<ErrorCollection, Response> asErrorResponse(final Response.Status status) {
        return new Function<ErrorCollection, Response>()
        {
            @Override
            public Response apply(final ErrorCollection errors)
            {
                return Response.status(status).entity(errors).cacheControl(never()).build();
            }
        };
    }
}
