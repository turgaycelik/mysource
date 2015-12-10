package com.atlassian.jira.plugin.user;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;

import java.util.List;

/**
 * Interface defines the implementation point for plugins wishing to interact with the
 * pre-delete-user-errors plugin point. Implementing this interface and returning a
 * non-empty collection of {@link WebErrorMessage} indicates the provided user should
 * not be deleted.
 *
 * @since v6.0
 */
@PublicSpi
public interface PreDeleteUserErrors
{
    /**
     * This will be called when a user is about to be deleted. Returning a non-empty
     * list of {@link WebErrorMessage} will prevent the user from being deleted.
     * @param user that is being considered for deletion
     * @return a list of {@link WebErrorMessage} about to the provided user
     */
    List<WebErrorMessage> getPreDeleteUserErrors(final User user);
}
