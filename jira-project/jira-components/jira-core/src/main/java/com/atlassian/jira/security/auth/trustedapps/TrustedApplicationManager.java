package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.OnDemand;

import java.util.Set;

/**
 * Manager for handling {@link TrustedApplicationInfo}
 * 
 * @since v3.12
 */
public interface TrustedApplicationManager
{
    /**
     * Find all {@link TrustedApplicationInfo} objects in the system.
     *
     * @return a Collection of all {@link TrustedApplicationInfo}. Empty (not null) if none exist.
     */
    Set<TrustedApplicationInfo> getAll();

    /**
     * Find a {@link TrustedApplicationInfo} given an application ID.
     *
     * @param applicationId the id of the application
     *
     * @return the {@link TrustedApplicationInfo} if found, null otherwise.
     */
    TrustedApplicationInfo get(String applicationId);

    /**
     * Find a {@link TrustedApplicationInfo} given an ID.
     *
     * @param id the id of the application
     *
     * @return the {@link TrustedApplicationInfo} if found, null otherwise.
     */
    TrustedApplicationInfo get(long id);

    /**
     * Deletes the {@link TrustedApplicationInfo} with the specified ID.
     *
     * @param user the user who is performing the delete
     * @param id the id of the application to delete
     * @return true if the application was successfully deleted, false otherwise.
     */
    boolean delete(User user, long id);

    /**
     * Deletes the {@link TrustedApplicationInfo} with the specified application ID.
     *
     * @param user the user who is performing the delete
     * @param applicationId the id of the application to delete
     * @return true if the application was successfully deleted, false otherwise.
     */
    boolean delete(User user, String applicationId);

    /**
     * Persist a {@link TrustedApplicationInfo}
     *
     * @param user the user who is performing the store
     * @param info the thing to save (create or update). Must not be null.
     * @return the updated or created business object.
     */
    TrustedApplicationInfo store(User user, TrustedApplicationInfo info);
    
    /**
     * Persist a {@link TrustedApplicationInfo}
     *
     * @param user the user who is performing the store
     * @param info the thing to save (create or update). Must not be null.
     * @return the updated or created business object.
     */
    @OnDemand
    TrustedApplicationInfo store(String user, TrustedApplicationInfo info);
}