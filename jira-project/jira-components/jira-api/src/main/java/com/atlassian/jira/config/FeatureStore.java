package com.atlassian.jira.config;

import com.atlassian.jira.exception.DataAccessException;

import java.util.List;
import java.util.Set;

/**
 *
 *
 * @since v6.0
 */
public interface FeatureStore
{

    /**
     * Permanently removes the feature from the system.
     *
     * @param featureKey the feature's id, must not be null.
     * @param userKey the user who the feature is for. null if site feature
     * @throws DataAccessException if there is a back-end storage problem.
     */
     void delete(String featureKey, String userKey) throws DataAccessException;

    /**
     * Creates an feature with the properties of the given feature.
     *
     * @param featureName the name of the feature to create
     * @param userKey the user who the feature is for. null if site feature
     * @throws DataAccessException if there is a back-end storage problem.
     */
     void create(String featureName, String userKey) throws DataAccessException;

    /**
     * Returns a list of all enabled features for a user
     * @param userKey the {@link com.atlassian.jira.user.ApplicationUser} key
     * @return the enabled features for a user or an empty set
     * @throws DataAccessException
     */
     Set<String> getUserFeatures(String userKey)  throws DataAccessException;

    /**
     * Returns a list of all enabled site features
     * @return the enabled site features or an empty set
     * @throws DataAccessException
     */
    Set<String> getSiteFeatures()  throws DataAccessException;
}
