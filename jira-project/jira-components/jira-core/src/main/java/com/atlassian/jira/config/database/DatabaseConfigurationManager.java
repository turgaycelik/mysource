package com.atlassian.jira.config.database;

/**
 * Manager implementation that provides save, load and related operations for JIRA's database configuration.
 *
 * @since v4.4
 */
public interface DatabaseConfigurationManager
{
    /**
     * Persists the given config as JIRA's database configuration. This must only be done once.
     *
     * @param databaseConfiguration the config to use for JIRA's database connection.
     * @throws RuntimeException if the database configuration is wrong.
     */
    void setDatabaseConfiguration(DatabaseConfig databaseConfiguration);

    /**
     * Gets the current database configuration.
     *
     * @return the config if configured.
     * @throws RuntimeException if the database has not been configured or the config can't be read.
     */
    DatabaseConfig getDatabaseConfiguration();

    /**
     * Requests that work that needs to read from and write to the database be done. If the database is already active,
     * the runnable will be run now in the calling thread. If not, it will be enqueued until the database configuration
     * is provided and the database is activated via {@link #activateDatabase()}. Then it will be done in the thread
     * that calls {@link #activateDatabase()}.
     * <p/>
     * Callers can be sure that the database schema will already be updated to fit the model when the given runnable is
     * run.
     *
     * @param runnable The work that must be done if or when the database is activated.
     * @param desc A description (for logging purposes) of the work.
     */
    void doNowOrWhenDatabaseActivated(Runnable runnable, String desc);

    /**
     * Requests that work dependent on the database configuration be done. If the database is already configured, the
     * runnable will be run now in the calling thread. If not, it will be enqueued until the database configuration is
     * provided but before the database is actually activated via {@link #activateDatabase()}. Then it will be done in
     * the thread that calls {@link #activateDatabase()}.
     * <p/>
     * The puropse of this method is to execute tasks that want to check or read the database configuration before the
     * schema is modified to fit the current model.
     *
     * @param runnable The work that must be done if or when the database is configured.
     * @param desc A description (for logging purposes) of the work.
     */
    void doNowOrWhenDatabaseConfigured(Runnable runnable, String desc);

    /**
     * Returns true only if the database configuration is available.
     *
     * @return whether the database is set up.
     */
    boolean isDatabaseSetup();

    /**
     * Initialises the first connection to the database and causes each enqueued runnable that registered with {@link
     * #doNowOrWhenDatabaseActivated(Runnable, String)} to be run. Should only be run once per application boot.
     */
    void activateDatabase();

    /**
     * Provides the {@link DatabaseConfig} that represents the embedded JIRA database. This does not imply that this
     * config is current or that there is any current configuration for the JIRA database.
     *
     * @return the embedded database config.
     */
    DatabaseConfig getInternalDatabaseConfiguration();

    /**
     * Creates a new  dbconfig.xml usinfg the values in entityengine.xml if there are any
     * Allows for upgrade from pre 5.0 instances
     */
    void createDbConfigFromEntityDefinition();
}
