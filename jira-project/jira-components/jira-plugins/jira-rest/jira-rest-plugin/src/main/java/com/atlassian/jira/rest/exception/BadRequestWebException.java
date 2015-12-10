package com.atlassian.jira.rest.exception;

import com.atlassian.jira.rest.api.util.ErrorCollection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * This exception is thrown when a client provides invalid input to a REST resource.
 *
 * @since v4.4
 */
public class BadRequestWebException extends WebApplicationException
{
    /**
     * Creates a new BadRequestWebException for the given issue. Whenever possible it is preferable to use {@link
     * #BadRequestWebException(com.atlassian.jira.rest.api.util.ErrorCollection)} constructor, passing a collection of
     * errors.
     */
    public BadRequestWebException()
    {
        this(ErrorCollection.of());
    }

    /**
     * Creates a new BadRequestWebException.
     *
     * @param cause the underlying cause of the exception
     */
    public BadRequestWebException(Throwable cause)
    {
        super(cause, createResponse(ErrorCollection.of()));
    }

    /**
     * Creates a new BadRequestWebException, with a collection of errors.
     *
     * @param errors an ErrorCollection containing the errors
     */
    public BadRequestWebException(ErrorCollection errors)
    {
        super(createResponse(errors));
    }

    /**
     * Creates a new BadRequestWebException, with a collection of errors and a cause.
     *
     * @param cause the underlying cause of the exception
     * @param errors an ErrorCollection containing the errors
     */
    public BadRequestWebException(Throwable cause, ErrorCollection errors)
    {
        super(cause, createResponse(errors));
    }

    /**
     * Creates a new HTTP response with status 400 (bad request), returning the errors in the provided ErrorCollection.
     *
     * @param errors an ErrorCollection containing errors
     * @return a Response
     */
    private static Response createResponse(ErrorCollection errors)
    {
        return Response.status(Response.Status.BAD_REQUEST).entity(errors).cacheControl(never()).build();
    }
}
