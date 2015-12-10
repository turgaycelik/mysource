package com.atlassian.jira.config.database;

import com.atlassian.jira.util.RuntimeIOException;

/**
 * DatabaseConfigurationLoader is responsible for loading and persisting database configuration from and to the home
 * directory.
 *
 * @since v4.4
 */
public interface DatabaseConfigurationLoader
{

    /**
     * Returns true only if the database configuration file already exists in the JIRA home directory.
     *
     * @return whether the config file exists.
     */
    boolean configExists();

    /**
     * Reads and deserialises the {@link DatabaseConfig} from its permanent store.
     *
     * @return the database configuration.
     * @throws RuntimeIOException if the config cannot be loaded due to a missing file or bad permissions.
     * @throws RuntimeException if the config cannot be loaded for example due to a corrupt file format.
     */
    DatabaseConfig loadDatabaseConfiguration() throws RuntimeException, RuntimeIOException;

    /**
     * Serialises and persists the given config to its permanent store.
     *
     * @param config the config to store.
     * @throws RuntimeIOException if there was some IO problem saving.
     */
    void saveDatabaseConfiguration(DatabaseConfig config) throws RuntimeIOException;
}
