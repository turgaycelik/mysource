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
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBeanSelfFunctions;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.rest.v2.entity.property.BasePropertyResource;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

/**
 *
 */
@Path("comment/{commentId}/properties")
@AnonymousAllowed
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class CommentPropertyResource
{
    private final BasePropertyResource<Comment> delegate;

    public CommentPropertyResource(CommentPropertyService commentPropertyService, JiraAuthenticationContext authenticationContext,
                                   JiraBaseUrls jiraBaseUrls, I18nHelper i18n)
    {
        delegate = new BasePropertyResource<Comment>(commentPropertyService, authenticationContext, jiraBaseUrls, i18n,
                new EntityPropertyBeanSelfFunctions.CommentPropertySelfFunction(), EntityPropertyType.COMMENT_PROPERTY);
    }


    /**
     * Returns the keys of all properties for the comment identified by the key or by the id.
     *
     * @param commentId the comment from which keys will be returned.
     * @return a response containing EntityPropertiesKeysBean.
     *
     * @response.representation.200.qname
     *      comment-properties-keys
     * @response.representation.200.doc
     *      Returned if the comment was found.
     * @response.representation.200.mediaType
     *      application/json
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.entity.property.EntityPropertyResourceExamples#GET_PROPERTIES_KEYS_RESPONSE_200}
     * @response.representation.400.doc
     *      Returned if the comment key or id is invalid.
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to browse the comment.
     * @response.representation.404.doc
     *      Returned if the comment with given key or id does not exist or if the property with given key is not found.
     */
    @ExperimentalApi
    @GET
    public Response getPropertiesKeys(@PathParam("commentId") final String commentId)
    {
        return delegate.getPropertiesKeys(commentId);
    }

    /**
     * Sets the value of the specified comment's property.
     * <p>
     *     You can use this resource to store a custom data against the comment identified by the key or by the id. The user
     *     who stores the data is required to have permissions to administer the comment.
     * </p>
     *
     * @param commentId the comment on which the property will be set.
     * @param propertyKey the key of the comment's property. The maximum length of the key is 255 bytes.
     * @param request the request containing value of the comment's property. The value has to a valid, non-empty JSON conforming
     *  to http://tools.ietf.org/html/rfc4627. The maximum length of the property value is 32768 bytes.
     *
     * @response.representation.200.doc
     *      Returned if the comment property is successfully updated.
     * @response.representation.201.doc
     *      Returned if the comment property is successfully created.
     * @response.representation.400.doc
     *      Returned if the comment key or id is invalid.
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to administer the comment.
     * @response.representation.404.doc
     *      Returned if the comment with given key or id does not exist.
     */
    @ExperimentalApi
    @PUT
    @Path ("/{propertyKey}")
    public Response setProperty(@PathParam ("commentId") final String commentId,
                                @PathParam ("propertyKey") final String propertyKey, @Context final HttpServletRequest request)
    {
        return delegate.setProperty(commentId, propertyKey, request);
    }

    /**
     * Returns the value of the property with a given key from the comment identified by the key or by the id. The user who retrieves
     * the property is required to have permissions to read the comment.
     *
     * @param commentId the comment from which the property will be returned.
     * @param propertyKey the key of the property to return.
     * @return a response containing {@link com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBean}.
     *
     * @response.representation.200.qname
     *      comment-property
     * @response.representation.200.doc
     *      Returned if the comment property was found.
     * @response.representation.200.mediaType
     *      application/json
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.entity.property.EntityPropertyResourceExamples#GET_PROPERTY_RESPONSE_200}
     * @response.representation.400.doc
     *      Returned if the comment key or id is invalid.
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to browse the comment.
     * @response.representation.404.doc
     *      Returned if the comment with given key or id does not exist or if the property with given key is not found.
     */
    @ExperimentalApi
    @GET
    @Path ("/{propertyKey}")
    public Response getProperty(@PathParam ("commentId") final String commentId,
                                @PathParam ("propertyKey") final String propertyKey)
    {
        return delegate.getProperty(commentId, propertyKey);
    }

    /**
     * Removes the property from the comment identified by the key or by the id. Ths user removing the property is required
     * to have permissions to administer the comment.
     *
     * @param commentId the comment from which the property will be removed.
     * @param propertyKey the key of the property to remove.
     *
     * @return a 204 HTTP status if everything goes well.
     *
     * @response.representation.204.doc
     *      Returned if the comment property was removed successfully.
     * @response.representation.400.doc
     *      Returned if the comment key or id is invalid.
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     * @response.representation.403.doc
     *      Returned if the calling user does not have permission to edit the comment.
     * @response.representation.404.doc
     *      Returned if the comment with given key or id does not exist or if the property with given key is not found.
     */
    @ExperimentalApi
    @DELETE
    @Path ("/{propertyKey}")
    public Response deleteProperty(@PathParam ("commentId") String commentId,
                                   @PathParam ("propertyKey") String propertyKey)
    {
        return delegate.deleteProperty(commentId, propertyKey);
    }
}
