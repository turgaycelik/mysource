package com.atlassian.jira.security.auth.trustedapps;

import java.util.Set;

/**
 * Persistent store for {@link TrustedApplicationData}
 *
 * @since v3.12
 */
public interface TrustedApplicationStore
{
    String ENTITY_NAME = "TrustedApplication";

    static final class Fields
    {
        static final String ID = "id";
        static final String APPLICATION_ID = "applicationId";
        static final String CREATED = "created";
        static final String CREATED_BY = "createdBy";
        static final String UPDATED = "updated";
        static final String UPDATED_BY = "updatedBy";
        static final String NAME = "name";
        static final String PUBLIC_KEY = "publicKey";
        static final String TIMEOUT = "timeout";
        static final String IP_MATCH = "ipMatch";
        static final String URL_MATCH = "urlMatch";

        private Fields() {}
    }

    /**
     * Persist a {@link TrustedApplicationData}
     *
     * @param trustedApplicationData the thing to save (create or update). Must not be null.
     * @return the updated or created data object.
     */
    TrustedApplicationData store(TrustedApplicationData trustedApplicationData);

    /**
     * Find a {@link TrustedApplicationData} given an application ID.
     *
     * @param applicationId the id of the application
     *
     * @return the {@link TrustedApplicationData} if found, null otherwise.
     */
    TrustedApplicationData getByApplicationId(String applicationId);

    /**
     * Find a {@link TrustedApplicationData} given an ID.
     *
     * @param id the id of the application
     *
     * @return the {@link TrustedApplicationData} if found, null otherwise.
     */
    TrustedApplicationData getById(long id);

    /**
     * Find all {@link TrustedApplicationData} objects in the database.
     *
     * @return a Collection of all {@link TrustedApplicationData} in the database. Empty (not null) if none exist.
     */
    Set<TrustedApplicationData> getAll();

    /**
     * Deletes the {@link TrustedApplicationData} with the specified ID.
     *
     * @param id the id of the application to delete
     * @return true if the application was successfully deleted, false otherwise.
     */
    boolean delete(long id);

    /**
     * Deletes the {@link TrustedApplicationData} with the specified application ID.
     *
     * @param applicationId the id of the application to delete
     * @return true if the application was successfully deleted, false otherwise.
     */
    boolean delete(String applicationId);
}
