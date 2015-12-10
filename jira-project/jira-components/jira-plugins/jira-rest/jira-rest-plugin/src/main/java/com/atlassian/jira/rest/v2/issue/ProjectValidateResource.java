package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.component.ComponentBean;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.Collections;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static java.util.Collections.singletonList;

/**
 * @since 5.0
 */
@Path ("projectvalidate")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class ProjectValidateResource
{
    public static final ErrorCollection DOC_EXAMPLE;
    static
    {
        final SimpleErrorCollection errors = new SimpleErrorCollection();
        errors.addError("projectKey", "A project with that project key already exists.");
        DOC_EXAMPLE = ErrorCollection.of(errors);
    }

    private ProjectService projectService;
    private JiraAuthenticationContext authContext;

    /**
     * This constructor needed by doclet.
     */
    @SuppressWarnings ({ "UnusedDeclaration" })
    private ProjectValidateResource()
    {
    }

    public ProjectValidateResource(ProjectService projectService, JiraAuthenticationContext authContext)
    {
        this.projectService = projectService;
        this.authContext = authContext;
    }

    /**
     * Validates a project key.
     *
     * @param key the project key
     * @return an ErrorCollection containing any errors. If the project key is valid, the ErrorCollection will be empty.
     *
     * @response.representation.200.qname
     *      errorCollection
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns an ErrorCollection containing any validation errors for the project key.
     *
     * @response.representation.200.example
     *      {@link #DOC_EXAMPLE}
     */
    @GET
    @Path ("key")
    public Response getProject(@QueryParam ("key") final String key)
    {
        final JiraServiceContext context = getContext();
        projectService.isValidProjectKey(context, key);

        final ErrorCollection errors = ErrorCollection.of(context.getErrorCollection());
        return Response.ok(errors).cacheControl(never()).build();
    }

    private JiraServiceContext getContext()
    {
        return new JiraServiceContextImpl(authContext.getLoggedInUser());
    }

}