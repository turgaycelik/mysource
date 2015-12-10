package com.atlassian.jira.rest.v2.issue;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.ProjectCategoryJsonBean;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectCategoryImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v2.issue.project.ProjectCategoryBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collection;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since 6.3
 */
@Path ("projectCategory")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class ProjectCategoryResource
{
    private final ProjectManager projectManager;
    private final JiraBaseUrls jiraBaseUrls;
    private final JiraAuthenticationContext authContext;
    private final PermissionManager permissionManager;
    private final I18nHelper i18n;

    public ProjectCategoryResource(
            final ProjectManager projectManager,
            final JiraBaseUrls jiraBaseUrls,
            final JiraAuthenticationContext authContext,
            final PermissionManager permissionManager,
            final I18nHelper i18n)
    {
        this.projectManager = projectManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.authContext = authContext;
        this.permissionManager = permissionManager;
        this.i18n = i18n;
    }

    /**
     * Contains a representation of a project category in JSON format.
     *
     * @since v6.3
     *
     * @param id project category id
     * @return a project category
     *
     * @response.representation.200.qname
     *      projectCategory
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the project category exists. Contains representation of a project category in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.project.ProjectCategoryBean#DOC_EXAMPLE1}
     *
     * @response.representation.404.doc
     *      Returned if the project category does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @GET
    @Path ("{id}")
    public Response getProjectCategoryById(@PathParam ("id") final Long id)
    {
        if (id == null)
        {
            return fieldValueMustBeProvidedResponse("id");
        }

        final ProjectCategory projectCategory = projectManager.getProjectCategoryObject(id);
        if (projectCategory == null)
        {
            return projectCategoryNotFoundResponse(id);
        }

        return Response.ok(ProjectCategoryJsonBean.bean(projectCategory, jiraBaseUrls)).cacheControl(never()).build();
    }

    /**
     * Returns all project categories
     *
     * @since v6.3
     *
     * @return all project categories
     *
     * @response.representation.200.qname
     *      projectCategories
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a list of project categories.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.project.ProjectCategoryBean#PROJECT_CATEGORIES_EXAMPLE}
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the list of projects.
     */
    @GET
    @AnonymousAllowed
    public Response getAllProjectCategories()
    {
        final Collection<ProjectCategory> allProjectCategories = projectManager.getAllProjectCategories();

        final Iterable<ProjectCategoryJsonBean> beans = Iterables.transform(allProjectCategories, new Function<ProjectCategory, ProjectCategoryJsonBean>()
        {
            @Override
            public ProjectCategoryJsonBean apply(final ProjectCategory projectCategory)
            {
                return ProjectCategoryJsonBean.bean(projectCategory, jiraBaseUrls);
            }
        });

        return Response.ok(beans).cacheControl(never()).build();
    }

    /**
     * Create a project category via POST.
     *
     * @since v6.3
     *
     * @response.representation.201.mediaType
     *      application/json
     *
     * @response.representation.201.doc
     *      Returned if the project category is created successfully.
     *
     * @response.representation.201.example
     *      {@link com.atlassian.jira.rest.v2.issue.project.ProjectCategoryBean#DOC_EXAMPLE_CREATED}
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.project.ProjectCategoryBean#DOC_EXAMPLE_CREATE}
     *
     * @response.representation.401.doc
     *      Returned if the caller is not logged in so does not have permission to create project categories.
     *
     * @response.representation.403.doc
     *      Returned if the caller is authenticated and does not have permission to create project categories (is not global admin).
     *
     */
    @POST
    public Response createProjectCategory(final ProjectCategoryBean bean)
    {
        final ApplicationUser user = authContext.getUser();
        if (user == null)
        {
            return userMustBeAuthenticatedResponse();
        }

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return userMustBeAdminResponse();
        }

        if (StringUtils.isBlank(bean.getName()))
        {
            return fieldValueMustBeProvidedResponse("name");
        }
        if (StringUtils.isBlank(bean.getDescription()))
        {
            return fieldValueMustBeProvidedResponse("description");
        }

        final ProjectCategory projectCategory = projectManager.createProjectCategory(bean.getName(), bean.getDescription());
        final ProjectCategoryJsonBean projectCategoryBean = ProjectCategoryJsonBean.bean(projectCategory, jiraBaseUrls);
        final URI selfPath = UriBuilder.fromPath(projectCategoryBean.getSelf()).build();

        return Response.status(Response.Status.CREATED).location(selfPath).entity(projectCategoryBean).cacheControl(never()).build();
    }

    /**
     * Delete a project category.
     *
     * @since v6.3
     *
     * @response.representation.204.doc
     *      Returned if the project category is successfully deleted.
     *
     * @response.representation.401.doc
     *      Returned if the caller is not logged in so does not have permission to delete project categories.
     *
     * @response.representation.403.doc
     *      Returned if the caller is authenticated and does not have permission to delete project categories (is not global admin).
     *
     * @response.representation.404.doc
     *      Returned if the project category does not exist or the currently authenticated user does not have permission to
     *      view it.

     * @param id Id of the project category to delete.
     * @return An empty or error response.
     */
    @DELETE
    @Path ("{id}")
    public Response removeProjectCategory(@PathParam ("id") final Long id) throws AtlassianCoreException
    {
        final ApplicationUser user = authContext.getUser();
        if (user == null)
        {
            return userMustBeAuthenticatedResponse();
        }

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return userMustBeAdminResponse();
        }

        if (id == null)
        {
            return fieldValueMustBeProvidedResponse("id");
        }

        final ProjectCategory projectCategory = projectManager.getProjectCategoryObject(id);
        if (projectCategory == null)
        {
            return projectCategoryNotFoundResponse(id);
        }

        projectManager.removeProjectCategory(id);

        return Response.noContent().cacheControl(never()).build();
    }

    /**
     * Modify a project category via PUT. Any fields present in the PUT will override existing values. As a convenience, if a field
     * is not present, it is silently ignored.
     *
     * @since v6.3
     *
     * @response.representation.200.doc
     *      Returned if the project category exists and the currently authenticated user has permission to edit it.
     *
     * @response.representation.201.example
     *      {@link com.atlassian.jira.rest.v2.issue.project.ProjectCategoryBean#DOC_EXAMPLE_UPDATED}
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.project.ProjectCategoryBean#DOC_EXAMPLE_UPDATE}
     *
     * @response.representation.401.doc
     *      Returned if the caller is not logged in so does not have permission to change project categories.
     *
     * @response.representation.403.doc
     *      Returned if the caller is authenticated and does not have permission to change project categories (is not global admin).
     *
     * @response.representation.404.doc
     *      Returned if the project category does not exist or the currently authenticated user does not have permission to
     *      view it.
     */
    @PUT
    @Path ("{id}")
    public Response updateProjectCategory(@PathParam ("id") final Long id, final ProjectCategoryBean bean)
    {
        final ApplicationUser user = authContext.getUser();
        if (user == null)
        {
            return userMustBeAuthenticatedResponse();
        }

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return userMustBeAdminResponse();
        }

        if (id == null)
        {
            return fieldValueMustBeProvidedResponse("id");
        }

        final ProjectCategory projectCategory = projectManager.getProjectCategoryObject(id);
        if (projectCategory == null)
        {
            return projectCategoryNotFoundResponse(id);
        }

        final String name = bean.getName() != null ? bean.getName() : projectCategory.getName();
        final String description = bean.getDescription() != null ? bean.getDescription() : projectCategory.getDescription();
        projectManager.updateProjectCategory(new ProjectCategoryImpl(projectCategory.getId(), name, description));

        final ProjectCategory updatedProjectCategory = projectManager.getProjectCategoryObject(id);
        return Response.ok(ProjectCategoryJsonBean.bean(updatedProjectCategory, jiraBaseUrls)).cacheControl(never()).build();
    }

    private Response fieldValueMustBeProvidedResponse(String fieldName)
    {
        return Response.status(Response.Status.BAD_REQUEST).entity(i18n.getText("rest.missing.parameter", fieldName)).build();
    }

    private Response projectCategoryNotFoundResponse(Long id)
    {
        return Response.status(Response.Status.NOT_FOUND).entity(i18n.getText("rest.project.category.not.found", id.toString())).build();
    }

    private Response userMustBeAuthenticatedResponse()
    {
        return Response.status(Response.Status.UNAUTHORIZED).entity(i18n.getText("rest.authentication.no.user.logged.in")).build();
    }

    private Response userMustBeAdminResponse()
    {
        return Response.status(Response.Status.FORBIDDEN).entity(i18n.getText("rest.authorization.admin.required")).build();
    }
}
