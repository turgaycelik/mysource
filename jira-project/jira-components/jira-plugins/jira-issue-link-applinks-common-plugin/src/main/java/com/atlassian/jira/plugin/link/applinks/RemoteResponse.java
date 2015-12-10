package com.atlassian.jira.plugin.link.applinks;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.sal.api.net.Response;

import javax.annotation.Nullable;

/**
 * Represents a response from a remote resource, e.g. REST, XMLRPC.
 *
 * @param <T> the type of entity stored in the response
 */
public class RemoteResponse<T>
{
    private final T entity;
    private final ErrorCollection errors;
    private final int statusCode;
    private final String statusText;
    private final boolean successful;

    public RemoteResponse(@Nullable final T entity, @Nullable final ErrorCollection errors, final Response response)
    {
        this.entity = entity;
        this.errors = errors;
        this.statusCode = response.getStatusCode();
        this.statusText = response.getStatusText();
        this.successful = response.isSuccessful();
    }

    public RemoteResponse(@Nullable final T entity, final Response response)
    {
        this(entity, null, response);
    }

    public T getEntity()
    {
        return entity;
    }

    /**
     * Returns true if the response entity was a non-empty ErrorCollection, false if otherwise.
     *
     * @return true if the response entity was a non-empty ErrorCollection, false if otherwise
     */
    public boolean hasErrors()
    {
        return (errors != null && errors.hasAnyErrors());
    }

    /**
     * Returns the ErrorCollection from the response entity. If the response was successful, this will generally be null.
     *
     * @return the ErrorCollection from the response entity
     */
    public ErrorCollection getErrors()
    {
        return errors;
    }

    /**
     * Checks if any of the error messages contain all of the elements in the text list.
     *
     * @param text an array of snippets to check for in the error message
     * @return true if an error message contains all of the elements in the text list, false if otherwise
     */
    public boolean containsErrorWithText(final String ... text)
    {
        if (hasErrors())
        {
            for (final String errorMessage : errors.getErrorMessages())
            {
                if (messageContainsText(errorMessage, text))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean messageContainsText(final String message, final String ... text)
    {
        for (final String snippet : text)
        {
            if (!message.contains(snippet))
            {
                return false;
            }
        }

        return true;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public String getStatusText()
    {
        return statusText;
    }

    public boolean isSuccessful()
    {
        // In some cases there will be a successful HTTP status code, but the response contains an error message.
        // In these cases, we consider the response to be unsuccessful.
        return successful && !hasErrors();
    }

    /**
     * Creates a RemoteResponse for when the endpoint requires authentication, but no credentials are available.
     * @param response response requiring authentication
     * @return RemoteResponse that requires credentials
     */
    public static <E> RemoteResponse<E> credentialsRequired(Response response)
    {
        return new RemoteResponse<E>(null, response);
    }
}