package com.atlassian.jira.sharing;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;

/**
 * Utility class to validate a set of {@link SharePermission} or {@link ShareTypeSearchParameter} for an entity .
 * 
 * @since v3.13
 */
public interface ShareTypeValidatorUtils
{
    /**
     * Validates to see if a given Set of {@link com.atlassian.jira.sharing.SharePermission} is valid. E.g. contains only one Singleton Permission and
     * no others. I.e. Globally and Privately shared.
     * 
     * @param context Contains the user trying to store permissions, the i18nBean and {@link com.atlassian.jira.util.ErrorCollection}
     * @param entity The entity the permissions are validating against
     * @return true if the set is valid, otherwise false.
     */
    boolean isValidSharePermission(JiraServiceContext context, SharedEntity entity);

    /**
     * Check to see if the passed SearchParameter is valid..
     * 
     * @param ctx Context containing the user that is to store the SharePermission, the i18n bean and an
     *        {@link com.atlassian.jira.util.ErrorCollection}
     * @param searchParameter the parameter is check.
     * @return true if the parameter is valid or false otherwise.
     */
    boolean isValidSearchParameter(JiraServiceContext ctx, ShareTypeSearchParameter searchParameter);
}
