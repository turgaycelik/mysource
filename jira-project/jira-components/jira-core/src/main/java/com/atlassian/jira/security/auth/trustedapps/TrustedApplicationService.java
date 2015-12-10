package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.bc.JiraServiceContext;

import java.util.Comparator;
import java.util.Set;

/**
 * Contains methods for managing {@link TrustedApplicationInfo} objects in JIRA
 *
 * @since v3.12
 */
public interface TrustedApplicationService
{
    /**
     * Find all {@link TrustedApplicationInfo} objects in the system.
     *
     * @param jiraServiceContext jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @return a Collection of all {@link TrustedApplicationInfo}. Empty (not null) if none exist.
     */
    Set<TrustedApplicationInfo> getAll(JiraServiceContext jiraServiceContext);

    /**
     * Find a {@link TrustedApplicationInfo} given an application ID.
     *
     * @param jiraServiceContext jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param applicationId the id of the application
     * @return the {@link TrustedApplicationInfo} if found, null otherwise.
     */
    TrustedApplicationInfo get(JiraServiceContext jiraServiceContext, String applicationId);

    /**
     * Find a {@link TrustedApplicationInfo} given an ID.
     *
     * @param jiraServiceContext jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param id the id of the application
     * @return the {@link TrustedApplicationInfo} if found, null otherwise.
     */
    TrustedApplicationInfo get(JiraServiceContext jiraServiceContext, long id);

    /**
     * Deletes the {@link TrustedApplicationInfo} with the specified ID.
     *
     * @param jiraServiceContext jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param id   the id of the application to delete
     * @return true if the application was successfully deleted, false otherwise.
     */
    boolean delete(JiraServiceContext jiraServiceContext, long id);

    /**
     * Persist a {@link TrustedApplicationInfo}
     *
     * @param jiraServiceContext jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param info the thing to save (create or update). Must not be null.
     * @return the updated or created business object.
     */
    TrustedApplicationInfo store(JiraServiceContext jiraServiceContext, TrustedApplicationInfo info);

    /**
     * Validate that the information contained in the builder is valid and able to be saved.
     *
     * @param jiraServiceContext jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param builder contains the SimpleTrustedApplication info
     * @return true if validation passed
     */
    boolean validate(JiraServiceContext jiraServiceContext, SimpleTrustedApplication builder);

    /**
     * Compares two given TrustedApplicationInfo objects by their names and returns the result of the comparison.
     */
    Comparator<TrustedApplicationInfo> NAME_COMPARATOR = new Comparator<TrustedApplicationInfo>()
    {
        public int compare(final TrustedApplicationInfo info1, final TrustedApplicationInfo info2)
        {
            return info1.getName().compareTo(info2.getName());
        }
    };
}
