package com.atlassian.jira.rest.exception;

import com.atlassian.jira.rest.api.util.ErrorCollection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * This exception is thrown when a caller does not have permission to access a JIRA REST resource.
 *
 * @since v5.0
 */
public class ForbiddenWebException extends WebApplicationException
{
    /**
     * Creates a new ForbiddenWebException for the given issue. Whenever possible it is preferable to use {@link
     * #ForbiddenWebException(com.atlassian.jira.rest.api.util.ErrorCollection)} constructor, passing a collection
     * of errors.
     */
    public ForbiddenWebException()
    {
        this(ErrorCollection.of());
    }

    /**
     * Creates a new ForbiddenWebException for the given issue, with a collection of errors.
     *
     * @param errors an ErrorCollection containing the errors
     */
    public ForbiddenWebException(ErrorCollection errors)
    {
        super(createResponse(errors));
    }

    /**
     * Creates a new HTTP response with status 403 (forbidden), returning the errors in the provided ErrorCollection.
     *
     * @param errors an ErrorCollection containing errors
     * @return a Response
     */
    private static Response createResponse(ErrorCollection errors)
    {
        // the issue key is not used yet, but should make it into the entity in the future...
        return Response.status(Response.Status.FORBIDDEN).entity(errors).cacheControl(never()).build();
    }
}
