package com.atlassian.jira.bc;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

/**
 * This is a context that provides information to calls to the JIRA service layer.
 */
@PublicApi
public interface JiraServiceContext
{
    /**
     * Gets an error collection. This should be used to report any human-readable errors that occur in a JIRA
     * service method call.
     *
     * @return errorCollection
     */
    ErrorCollection getErrorCollection();

    /**
     * Returns the User who has invoked the JIRA service method.
     *
     * @return user who is performing the operation (can be null).
     * @deprecated since 6.1 use {@link #getLoggedInApplicationUser()} instead
     */
    @Deprecated
    User getLoggedInUser();

    /**
     * Returns the User who has invoked the JIRA service method.
     *
     * @return user who is performing the operation (can be null).
     */
    ApplicationUser getLoggedInApplicationUser();

    /**
     * Get an I18nHelper for localising text.
     * 
     * @return an I18nHelper for localising text.
     * @since v3.13
     */
    I18nHelper getI18nBean();
}
