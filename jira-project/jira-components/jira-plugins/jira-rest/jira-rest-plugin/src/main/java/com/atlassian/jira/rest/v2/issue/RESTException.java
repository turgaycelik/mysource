package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.rest.api.util.ErrorCollection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since v4.2
 */
public class RESTException extends WebApplicationException
{
    private final static Response.Status DEFAULT_STATUS = Response.Status.BAD_REQUEST;

    /**
     * Creates a new RESTException for the given issue. Whenever possible it is preferable to use {@link
     * #RESTException(javax.ws.rs.core.Response.Status, com.atlassian.jira.rest.api.util.ErrorCollection)} constructor, passing a collection of
     * errors and the status
     */
    public RESTException()
    {
        this(DEFAULT_STATUS, ErrorCollection.of());
    }

    /**
     * Creates a new RESTException for the given issue, with a collection of errors.
     * The HTTP status is derived from the ErrorCollection.
     *
     * @param errors an ErrorCollection containing the errors
     */
    public RESTException(final ErrorCollection errors)
    {
        super(createResponse(errors));
    }

    public RESTException(final Response.Status status, final String... errorMessages)
    {
        this(status, ErrorCollection.of(errorMessages));
    }

    /**
     * Creates a new RESTException for the given issue, with a collection of errors.
     *
     * @param status the HTTP status of this error (401, 403, etc)
     * @param errors an ErrorCollection containing the errors
     */
    public RESTException(final Response.Status status, final ErrorCollection errors)
    {
        super(createResponse(status.getStatusCode(), errors));
    }

    /**
     * Creates a new RESTException for the given issue and allows to nest an exception.
     *
     * @param status the HTTP status of this error (401, 403, etc)
     * @param cause the nested exception that will be logged by the ExceptionInterceptor, before returning the response to the user.
     */
    public RESTException(final Response.Status status, final Throwable cause)
    {
        super(cause, status);
    }

    /**
     * Creates a new HTTP response from the given ErrorCollection. The status is derived from the ErrorCollection.
     *
     * @param errors an ErrorCollection containing errors
     * @return a Response
     */
    private static Response createResponse(final ErrorCollection errors)
    {
        int statusCode = errors.getStatus() == null ? DEFAULT_STATUS.getStatusCode() : errors.getStatus();

        return createResponse(statusCode, errors);
    }

    /**
     * Creates a new HTTP response with the given status, returning the errors in the provided ErrorCollection.
     *
     * @param status the HTTP status to use for this response
     * @param errors an ErrorCollection containing errors
     * @return a Response
     */
    private static Response createResponse(final int status, final ErrorCollection errors)
    {
        // the issue key is not used yet, but should make it into the entity in the future...
        return Response.status(status).entity(errors).cacheControl(never()).build();
    }

    @Override
    public String toString()
    {
        return getLocalizedMessage() != null ? super.toString() : super.toString() + "(" + Response.Status.fromStatusCode(getResponse().getStatus()) + ")";
    }
}
