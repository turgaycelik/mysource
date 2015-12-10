package com.atlassian.jira.rest.v2.issue;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.bc.project.property.ProjectPropertyService;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.v2.entity.property.BasePropertyWithKeyResource;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraKeyUtils;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import com.google.common.base.Predicate;

import static com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBeanSelfFunctions.ProjectPropertySelfFunction;

/**
 * @since v6.2
 */
@Path ("project/{projectIdOrKey}/properties")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class ProjectPropertyResource
{
    private final BasePropertyWithKeyResource<Project> delegate;

    public ProjectPropertyResource(ProjectPropertyService projectPropertyService, JiraAuthenticationContext authContext,
            JiraBaseUrls jiraBaseUrls, I18nHelper i18n)
    {
        this.delegate = new BasePropertyWithKeyResource<Project>(projectPropertyService, authContext, jiraBaseUrls, i18n,
                new IsValidProjectKeyPredicate(), new ProjectPropertySelfFunction(), EntityPropertyType.ISSUE_PROPERTY);
    }

    /**
     * Returns the keys of all properties for the project identified by the key or by the id.
     *
     * @param projectIdOrKey the project from which keys will be returned.
     * @return a response containing EntityPropertiesKeysBean.
     *
     * @response.representation.200.qname
     *      project-properties-keys
     * @response.representation.200.doc
     *      Returned if the project was found.
     * @response.representation.200.mediaType
     *      application/json
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.entity.property.EntityPropertyResourceExamples#GET_PROPERTIES_KEYS_RESPONSE_200}
     * @response.representation.400.doc
     *      Returned if the project key or id is invalid.
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to browse the project.
     * @response.representation.404.doc
     *      Returned if the project with given key or id does not exist or if the property with given key is not found.
     */
    @ExperimentalApi
    @GET
    public Response getPropertiesKeys(@PathParam ("projectIdOrKey") final String projectIdOrKey)
    {
        return delegate.getPropertiesKeys(projectIdOrKey);
    }

    /**
     * Sets the value of the specified project's property.
     * <p>
     *     You can use this resource to store a custom data against the project identified by the key or by the id. The user
     *     who stores the data is required to have permissions to administer the project.
     * </p>
     *
     * @param projectIdOrKey the project on which the property will be set.
     * @param propertyKey the key of the project's property. The maximum length of the key is 255 bytes.
     * @param request the request containing value of the project's property. The value has to a valid, non-empty JSON conforming
     *  to http://tools.ietf.org/html/rfc4627. The maximum length of the property value is 32768 bytes.
     *
     * @response.representation.200.doc
     *      Returned if the project property is successfully updated.
     * @response.representation.201.doc
     *      Returned if the project property is successfully created.
     * @response.representation.400.doc
     *      Returned if the project key or id is invalid.
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to administer the project.
     * @response.representation.404.doc
     *      Returned if the project with given key or id does not exist.
     */
    @ExperimentalApi
    @PUT
    @Path ("/{propertyKey}")
    public Response setProperty(@PathParam ("projectIdOrKey") final String projectIdOrKey,
            @PathParam ("propertyKey") final String propertyKey, @Context final HttpServletRequest request)
    {
        return delegate.setProperty(projectIdOrKey, propertyKey, request);
    }

    /**
     * Returns the value of the property with a given key from the project identified by the key or by the id. The user who retrieves
     * the property is required to have permissions to read the project.
     *
     * @param projectIdOrKey the project from which the property will be returned.
     * @param propertyKey the key of the property to return.
     * @return a response containing {@link com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBean}.
     *
     * @response.representation.200.qname
     *      project-property
     * @response.representation.200.doc
     *      Returned if the project property was found.
     * @response.representation.200.mediaType
     *      application/json
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.entity.property.EntityPropertyResourceExamples#GET_PROPERTY_RESPONSE_200}
     * @response.representation.400.doc
     *      Returned if the project key or id is invalid.
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to browse the project.
     * @response.representation.404.doc
     *      Returned if the project with given key or id does not exist or if the property with given key is not found.
     */
    @ExperimentalApi
    @GET
    @Path ("/{propertyKey}")
    public Response getProperty(@PathParam ("projectIdOrKey") final String projectIdOrKey,
            @PathParam ("propertyKey") final String propertyKey)
    {
        return delegate.getProperty(projectIdOrKey, propertyKey);
    }

    /**
     * Removes the property from the project identified by the key or by the id. Ths user removing the property is required
     * to have permissions to administer the project.
     *
     * @param projectIdOrKey the project from which the property will be removed.
     * @param propertyKey the key of the property to remove.
     *
     * @return a 204 HTTP status if everything goes well.
     *
     * @response.representation.204.doc
     *      Returned if the project property was removed successfully.
     * @response.representation.400.doc
     *      Returned if the project key or id is invalid.
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to edit the project.
     * @response.representation.404.doc
     *      Returned if the project with given key or id does not exist or if the property with given key is not found.
     */
    @ExperimentalApi
    @DELETE
    @Path ("/{propertyKey}")
    public Response deleteProperty(@PathParam ("projectIdOrKey") String projectIdOrKey,
            @PathParam ("propertyKey") String propertyKey)
    {
        return delegate.deleteProperty(projectIdOrKey, propertyKey);
    }

    private static class IsValidProjectKeyPredicate implements Predicate<String>
    {
        @Override
        public boolean apply(final String projectKey)
        {
            return JiraKeyUtils.validProjectKey(projectKey);
        }
    }

}
