package com.atlassian.jira.config.database.jdbcurlparser;

/**
 * Represents a database instance as parsed out of a JDBC URL.
 * That is, the servername and port number that the DB is running on, as well as the "custom" instance field.
 */
public class DatabaseInstance
{
    private String hostname = "";
    private String port = "";
    private String instance = "";

    public String getHostname()
    {
        return hostname;
    }

    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

    public String getPort()
    {
        return port;
    }

    public void setPort(String port)
    {
        this.port = port;
    }

    /**
     * The Instance field represents the "custom" extra property for the database.
     * ie on Oracle the "SID", on MySQL or Postgres the "Database", or on SQL Server the "Instance"
     * @return Instance field 
     */
    public String getInstance()
    {
        return instance;
    }

    public void setInstance(String instance)
    {
        this.instance = instance;
    }

}
