package com.atlassian.jira.configurator.db;

import com.atlassian.jira.config.database.DatabaseType;
import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.exception.ParseException;

import javax.swing.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * An abstract class that represents the GUI Config panel for a particular DB type.
 * Each DB type will require a separate subclass with DB-specific operations.
 */
public abstract class DatabaseConfigPanel
{
    public static final int TEXT_FIELD_COLUMNS = 20;

    @Override
    public String toString()
    {
        return getDisplayName();
    }

    /**
     * Verify that the settings are valid.
     *
     * @throws ValidationException if any config is invalid.
     */
    public abstract void validate() throws ValidationException;

    public abstract String getDisplayName();

    public abstract String getClassName();

    /**
     * Returns the JDBC URL for this DB config.
     * <p>
     * The current jira-home is passed because it used by HSQL.
     *
     * @param jiraHome The current configured jira-home
     * @return the JDBC URL for this DB config.
     * @throws ValidationException If the underlying configuration is invalid for this DB type. eg for Postgres, "Database" (instance) is a required field
     */
    public abstract String getUrl(String jiraHome) throws ValidationException;

    public abstract String getUsername();

    public abstract String getPassword();

    /**
     * For DB's that want to allow entity-engine to set a schema-name, this will return the user's selected Schema-name
     * @return the user's selected Schema-name
     */
    public abstract String getSchemaName();
    
    public abstract JPanel getPanel();

    int validatePortNumber(final String portText) throws ValidationException
    {
        if (portText.trim().length() == 0)
        {
            return -1;
        }
        try
        {
            int port = Integer.parseInt(portText);
            if (port < 0 || port > 65535)
            {
                throw new ValidationException("Port number out of range.");
            }
            return port;
        }
        catch (NumberFormatException e)
        {
            throw new ValidationException("Port must be a valid number.");
        }
    }

    void validateNotBlank(final String fieldName, final String text) throws ValidationException
    {
        if (text == null || text.trim().length() == 0)
        {
            throw new ValidationException("Please set a value for " + fieldName);
        }
    }

    public abstract void setSettings(final Settings settings) throws ParseException;

    /**
     * Apply the current user entered settings into the given Settings object.
     * @param newSettings The Settings object to set the values in.
     * @param jiraHome the jira home where the settings should be saved
     * @throws ValidationException if any of the new settings are Invalid
     */
    public void saveSettings(final Settings newSettings, String jiraHome) throws ValidationException
    {
        final String driverClassName = getClassName();
        final DatabaseType databaseType;
        try
        {
            databaseType = DatabaseType.forJdbcDriverClassName(driverClassName);
        }
        catch (IllegalArgumentException iae)
        {
            throw new ValidationException(iae.getMessage());
        }
        newSettings.getJdbcDatasourceBuilder()
                .setDatabaseType(databaseType)
                .setDriverClassName(driverClassName)
                .setJdbcUrl(getUrl(jiraHome))
                .setUsername(getUsername())
                .setPassword(getPassword());
        newSettings.setSchemaName(getSchemaName());
    }

    public void testConnection(String jiraHome) throws ClassNotFoundException, SQLException, ValidationException
    {
        // We will try to dynamically ClassLoad the JDBC Driver.
        // Get a new ClassLoader each time, as the user may throw the JDBC jar into the directory while we are running.
        Class driverClass = Class.forName(this.getClassName(), true, getDriverClassLoader());
        Driver driver;
        try
        {
            driver = (Driver) driverClass.newInstance();
        }
        catch (InstantiationException ex)
        {
            // Unexpected - this should only happen if the named class is abstract (or an interface).
            throw new RuntimeException(ex);
        }
        catch (IllegalAccessException ex)
        {
            // Unexpected.
            throw new RuntimeException(ex);
        }

        // Set the Timeout Limit - otherwise we could hang forever on a failed connection attempt.
        DriverManager.setLoginTimeout(10);
        // We cannot user DriverManager to get the connection because we are using a child ClassLoader.
        Properties props = new Properties();
        props.setProperty("user", getUsername());
        props.setProperty("password", getPassword());
        driver.connect(this.getUrl(jiraHome), props);
    }

    private static ClassLoader getDriverClassLoader()
    {
        List<URL> jars = new ArrayList<URL>();
        // Find all the jars in the <tomcat-home>/lib directory
        File libDir = new File("../lib");
        if (!libDir.exists() || !libDir.isDirectory())
        {
            // presumably we are in dev-mode (running inside IDEA)
            // Look in the default Tomcat installation folder:
            libDir = new File("tomcat/lib");
        }
        File[] files = libDir.listFiles();
        if (files == null)
        {
            return new URLClassLoader(new URL[0]);
        }
        for (File file: files)
        {
            if (file.getName().endsWith(".jar"))
            {
                try
                {
                    jars.add(file.toURI().toURL());
                }
                catch (MalformedURLException ex)
                {
                    System.err.println("Unexpected MalformedURLException on file '" + file.getAbsolutePath() + "'.");
                    ex.printStackTrace(System.err);
                    // We will soldier on - this may not be the jar we need
                }
            }
        }
        return new URLClassLoader(jars.toArray(new URL[jars.size()]));
    }
}
