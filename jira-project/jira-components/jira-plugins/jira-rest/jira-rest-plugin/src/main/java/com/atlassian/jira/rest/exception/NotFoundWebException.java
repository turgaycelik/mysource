package com.atlassian.jira.rest.exception;

import com.atlassian.jira.rest.api.util.ErrorCollection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * This exception is thrown when a JIRA REST resource is not found, or it cannot be read for some other reason (such as
 * security checks).
 *
 * @since v4.2
 */
public class NotFoundWebException extends WebApplicationException
{
    /**
     * Creates a new NotFoundWebException for the given issue. Whenever possible it is preferable to use {@link
     * #NotFoundWebException(com.atlassian.jira.rest.api.util.ErrorCollection)} constructor, passing a collection of
     * errors.
     */
    public NotFoundWebException()
    {
        this(ErrorCollection.of());
    }

    /**
     * Creates a new NotFoundWebException.
     *
     * @param cause the underlying cause of the exception
     */
    public NotFoundWebException(Throwable cause)
    {
        super(cause, createResponse(ErrorCollection.of()));
    }

    /**
     * Creates a new NotFoundWebException, with a collection of errors.
     *
     * @param errors an ErrorCollection containing the errors
     */
    public NotFoundWebException(ErrorCollection errors)
    {
        super(createResponse(errors));
    }

    /**
     * Creates a new NotFoundWebException, with a collection of errors and a cause.
     *
     * @param cause the underlying cause of the exception
     * @param errors an ErrorCollection containing the errors
     */
    public NotFoundWebException(Throwable cause, ErrorCollection errors)
    {
        super(cause, createResponse(errors));
    }

    /**
     * Creates a new HTTP response with status 404 (not found), returning the errors in the provided ErrorCollection.
     *
     * @param errors an ErrorCollection containing errors
     * @return a Response
     */
    private static Response createResponse(ErrorCollection errors)
    {
        // the issue key is not used yet, but should make it into the entity in the future...
        return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
    }
}
