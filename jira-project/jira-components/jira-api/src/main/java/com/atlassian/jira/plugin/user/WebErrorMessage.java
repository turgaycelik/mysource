package com.atlassian.jira.plugin.user;

import com.atlassian.annotations.PublicApi;

import java.net.URI;

/**
 * Interface to be used with user management plugin points to encapsulate error messages
 * and a useful information for displaying those error messages to a user. An error
 * message should contain a full sentence returned by {@link #getDescription()}, a
 * short {@link #getSnippet() snippet} of a message to serve as a summary of the error
 * message, and optionally a URI to link to further information when only the snippet
 * is presented to the user.
 *
 * @since v6.0
 */
@PublicApi
public interface WebErrorMessage
{
    /**
     * Returns a message containing the reason an error message has been
     * generated.
     *
     * @return an error message; must not be {@code null}
     */
    public String getDescription();

    /**
     * Returns a short form of the error message that getDescription would
     * provide.
     *
     * @return short form representation of getDescription; must not be {@code null}
     */
    public String getSnippet();

    /**
     * Returns a {@link java.net.URI} that can be used to direct users
     * to pages with more information that can be tied to the snippet
     *
     * @return a uri, or {@code null} if none is available
     */
    public URI getURI();
}
