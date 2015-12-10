package com.atlassian.jira.plugin.user;

import com.atlassian.crowd.embedded.api.User;
import com.google.common.collect.ImmutableList;

/**
 * @since v6.0
 */
public interface PreDeleteUserErrorsManager
{
    /**
     * Handles the collecting all the {@link WebErrorMessage} for the pre-delete-user-errors plugin point
     * @param user that will be deleted
     * @return list containing all plugin provided {@link WebErrorMessage}
     */
    ImmutableList<WebErrorMessage> getWarnings(User user);
}
