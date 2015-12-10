package com.atlassian.jira.rest.exception;

import com.atlassian.jira.rest.api.util.ErrorCollection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * This exception is thrown when a JIRA REST resource aborts in an unforeseen manner).
 *
 * @since v4.4
 */
public class ServerErrorWebException extends WebApplicationException
{
    /**
     * Creates a new ServerErrorWebException for the given issue. Whenever possible it is preferable to use {@link
     * #ServerErrorWebException(com.atlassian.jira.rest.api.util.ErrorCollection)} constructor, passing a collection of
     * errors.
     */
    public ServerErrorWebException()
    {
        this(ErrorCollection.of());
    }

    /**
     * Creates a new ServerErrorWebException.
     *
     * @param cause the underlying cause of the exception
     */
    public ServerErrorWebException(Throwable cause)
    {
        super(cause, createResponse(ErrorCollection.of()));
    }

    /**
     * Creates a new ServerErrorWebException, with a collection of errors.
     *
     * @param errors an ErrorCollection containing the errors
     */
    public ServerErrorWebException(ErrorCollection errors)
    {
        super(createResponse(errors));
    }

    /**
     * Creates a new ServerErrorWebException, with a collection of errors and a cause.
     *
     * @param cause the underlying cause of the exception
     * @param errors an ErrorCollection containing the errors
     */
    public ServerErrorWebException(Throwable cause, ErrorCollection errors)
    {
        super(cause, createResponse(errors));
    }

    /**
     * Creates a new HTTP response with status 500 (internal server error), returning the errors in the provided ErrorCollection.
     *
     * @param errors an ErrorCollection containing errors
     * @return a Response
     */
    private static Response createResponse(ErrorCollection errors)
    {
        // the issue key is not used yet, but should make it into the entity in the future...
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errors).cacheControl(never()).build();
    }
}
