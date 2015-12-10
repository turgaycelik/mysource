package com.atlassian.jira.config.database;

/**
 * Already setup.
 *
 * @since v4.4
 */
public class MockDatabaseConfigurationManager implements DatabaseConfigurationManager
{

    @Override
    public void setDatabaseConfiguration(DatabaseConfig databaseConfiguration)
    {
        throw new RuntimeException();
    }

    @Override
    public DatabaseConfig getDatabaseConfiguration()
    {
        throw new RuntimeException();

    }

    @Override
    public void doNowOrWhenDatabaseActivated(Runnable runnable, String desc)
    {
        throw new RuntimeException();
    }

    @Override
    public void doNowOrWhenDatabaseConfigured(Runnable runnable, String desc)
    {
        throw new RuntimeException();
    }

    @Override
    public boolean isDatabaseSetup()
    {
        return true;
    }

    @Override
    public void activateDatabase()
    {
        throw new RuntimeException();
    }

    @Override
    public DatabaseConfig getInternalDatabaseConfiguration()
    {
        throw new RuntimeException();
    }

    @Override
    public void createDbConfigFromEntityDefinition()
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
