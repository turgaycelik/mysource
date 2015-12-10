package com.atlassian.jira.configurator.db;

import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.ValidationException;

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

public abstract class AbstractConnectionConfig implements ConnectionConfig
{
    /**
     * Apply the current user entered settings into the given Settings object.
     * @param newSettings The Settings object to set the values in.
     */
    public void saveSettings(final Settings newSettings) throws ValidationException
    {
        newSettings.getJdbcDatasourceBuilder()
                .setDriverClassName(getClassName())
                .setJdbcUrl(getUrl())
                .setUsername(getUsername())
                .setPassword(getPassword());
    }

    public void testConnection() throws ClassNotFoundException, SQLException, ValidationException
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
        driver.connect(this.getUrl(), props);
    }

    private static ClassLoader getDriverClassLoader()
    {
        List<URL> jars = new ArrayList<URL>();
        // Find all the jars in the <tomcat-home>/lib directory
        File libDir = new File("../lib");
        if (!libDir.exists() || !libDir.isDirectory())
        {
            // presumably we are in dev-mode (running inside IDEA)
            return DatabaseConfigPanel.class.getClassLoader();
        }
        for (File file: libDir.listFiles())
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
