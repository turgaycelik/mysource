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
import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueKey;
import com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBeanSelfFunctions;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.rest.v2.entity.property.BasePropertyWithKeyResource;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

/**
 * @since v6.2
 */
@Path ("issue/{issueIdOrKey}/properties")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class IssuePropertyResource
{
    private final BasePropertyWithKeyResource<Issue> delegate;

    public IssuePropertyResource(IssuePropertyService issuePropertyService, JiraAuthenticationContext authContext,
            JiraBaseUrls jiraBaseUrls, I18nHelper i18n)
    {
        this.delegate = new BasePropertyWithKeyResource<Issue>(issuePropertyService, authContext, jiraBaseUrls,
                i18n, new IssueKey.IsValidIssueKeyPredicate(), new EntityPropertyBeanSelfFunctions.IssuePropertySelfFunction(), EntityPropertyType.ISSUE_PROPERTY);

    }

    /**
     * Returns the keys of all properties for the issue identified by the key or by the id.
     *
     * @param issueIdOrKey the issue from which keys will be returned.
     * @return a response containing EntityPropertiesKeysBean.
     *
     * @response.representation.200.qname
     *      issue-properties-keys
     * @response.representation.200.doc
     *      Returned if the issue was found.
     * @response.representation.200.mediaType
     *      application/json
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.entity.property.EntityPropertyResourceExamples#GET_PROPERTIES_KEYS_RESPONSE_200}
     * @response.representation.400.doc
     *      Returned if the issue key or id is invalid.
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to view the issue.
     * @response.representation.404.doc
     *      Returned if the issue with given key or id does not exist or if the property with given key is not found.
     */
    @ExperimentalApi
    @GET
    public Response getPropertiesKeys(@PathParam ("issueIdOrKey") final String issueIdOrKey)
    {
        return delegate.getPropertiesKeys(issueIdOrKey);
    }

    /**
     * Sets the value of the specified issue's property.
     * <p>
     *     You can use this resource to store a custom data against the issue identified by the key or by the id. The user
     *     who stores the data is required to have permissions to edit the issue.
     * </p>
     *
     * @param issueIdOrKey the issue on which the property will be set.
     * @param issuePropertyKey the key of the issue's property. The maximum length of the key is 255 bytes.
     * @param request the request containing value of the issue's property. The value has to a valid, non-empty JSON conforming
     *  to http://tools.ietf.org/html/rfc4627. The maximum length of the property value is 32768 bytes.
     *
     * @response.representation.200.doc
     *      Returned if the issue property is successfully updated.
     * @response.representation.201.doc
     *      Returned if the issue property is successfully created.
     * @response.representation.400.doc
     *      Returned if the issue key or id is invalid.
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to edit the issue.
     * @response.representation.404.doc
     *      Returned if the issue with given key or id does not exist.
     */
    @ExperimentalApi
    @PUT
    @Path ("/{propertyKey}")
    public Response setProperty(@PathParam ("issueIdOrKey") final String issueIdOrKey,
            @PathParam ("propertyKey") final String issuePropertyKey, @Context final HttpServletRequest request)
    {
        return delegate.setProperty(issueIdOrKey, issuePropertyKey, request);
    }

    /**
     * Returns the value of the property with a given key from the issue identified by the key or by the id. The user who retrieves
     * the property is required to have permissions to read the issue.
     *
     * @param issueIdOrKey the issue from which the property will be returned.
     * @param issuePropertyKey the key of the property to return.
     * @return a response containing {@link com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBean}.
     *
     * @response.representation.200.qname
     *      issue-property
     * @response.representation.200.doc
     *      Returned if the issue property was found.
     * @response.representation.200.mediaType
     *      application/json
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.entity.property.EntityPropertyResourceExamples#GET_PROPERTY_RESPONSE_200}
     * @response.representation.400.doc
     *      Returned if the issue key or id is invalid.
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to view the issue.
     * @response.representation.404.doc
     *      Returned if the issue with given key or id does not exist or if the property with given key is not found.
     */
    @ExperimentalApi
    @GET
    @Path ("/{propertyKey}")
    public Response getProperty(@PathParam ("issueIdOrKey") final String issueIdOrKey,
            @PathParam ("propertyKey") final String issuePropertyKey)
    {
        return delegate.getProperty(issueIdOrKey, issuePropertyKey);
    }

    /**
     * Removes the property from the issue identified by the key or by the id. Ths user removing the property is required
     * to have permissions to edit the issue.
     *
     * @param issueIdOrKey the issue from which the property will be removed.
     * @param propertyKey the key of the property to remove.
     *
     * @return a 204 HTTP status if everything goes well.
     *
     * @response.representation.204.doc
     *      Returned if the issue property was removed successfully.
     * @response.representation.400.doc
     *      Returned if the issue key or id is invalid.
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to edit the issue.
     * @response.representation.404.doc
     *      Returned if the issue with given key or id does not exist or if the property with given key is not found.
     */
    @ExperimentalApi
    @DELETE
    @Path ("/{propertyKey}")
    public Response deleteProperty(@PathParam ("issueIdOrKey") String issueIdOrKey,
            @PathParam ("propertyKey") String propertyKey)
    {
        return delegate.deleteProperty(issueIdOrKey, propertyKey);
    }
}
