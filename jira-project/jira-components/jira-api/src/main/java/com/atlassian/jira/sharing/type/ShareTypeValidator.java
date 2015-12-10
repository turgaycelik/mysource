package com.atlassian.jira.sharing.type;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;

/**
 * Interface that defines the validation of a particular {@link ShareType}. Validation is checking that its associated
 * {@link SharePermission} or {@link ShareTypeSearchParameter} is valid.
 */

public interface ShareTypeValidator
{
    /**
     * Key used for any error messages.
     */
    static final String ERROR_KEY = "shares";

    static final String DELEGATED_ERROR_KEY = "shares_delegated";

    /**
     * Checks if the passed SharePermission is valid for the associated ShareType.
     *
     * @param ctx Context containing the user that is to store the SharePermission, the i18n bean and an {@link com.atlassian.jira.util.ErrorCollection}
     * @param permission the SharePermission to check.
     * @return true if the permission is valid or false otherwise.
     */
    public boolean checkSharePermission(JiraServiceContext ctx, SharePermission permission);

    /**
     * Check to see if the passed SearchParameter is valid for the ShareType.
     *
     * @param ctx Context containing the user that is to store the SharePermission, the i18n bean and an {@link com.atlassian.jira.util.ErrorCollection}
     * @param searchParameter the parameter is check.
     * @return true if the parameter is valid or false otherwise.
     */
    public boolean checkSearchParameter(JiraServiceContext ctx, ShareTypeSearchParameter searchParameter);

}
