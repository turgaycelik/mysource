package com.atlassian.jira.config.database;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.help.HelpUrl;
import com.atlassian.jira.help.HelpUrls;
import com.atlassian.jira.help.StaticHelpUrls;
import com.atlassian.jira.startup.JiraStartupLogger;
import com.google.common.collect.ImmutableList;

import java.util.Iterator;

import static com.atlassian.jira.config.database.DatabaseType.forJdbcDriverClassName;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Registers required DB drivers and provides db-specific error messages when things go wrong.
 *
 * @since v5.2
 */
@Internal
public class DatabaseDriverRegisterer
{
    public static DatabaseDriverRegisterer forType(DatabaseType type)
    {
        return new DatabaseDriverRegisterer(notNull("type", type), type.getJdbcDriverClassName());
    }


    public static DatabaseDriverRegisterer forDriverClass(String className)
    {
        return new DatabaseDriverRegisterer(findForDriverClass(notNull("driverClassName", className)), className);
    }

    private static DatabaseType findForDriverClass(String className)
    {
        try
        {
            return forJdbcDriverClassName(className);
        }
        catch (IllegalArgumentException iae)
        {
            return DatabaseType.UKNOWN;
        }
    }

    private final DatabaseType databaseType;
    private final String driverClassName;

    private DatabaseDriverRegisterer(DatabaseType databaseType, String driverClassName)
    {
        this.databaseType = databaseType;
        this.driverClassName = driverClassName;
    }

    public DatabaseType databaseType()
    {
        return databaseType;
    }

    /**
     * Registers the driver. If the driver is not found on the classpath, prints an error message in the logs and
     * throws IllegalStateException (yes, it does both things! log it and throw it FTW:)
     *
     * @throws InvalidDatabaseDriverException if the driver is not found
     */
    public void registerDriver()
    {
        try
        {
            Class.forName(driverClassName);
        }
        catch (ClassNotFoundException ex)
        {
            final Iterator<String> message = getErrorMessage().iterator();
            JiraStartupLogger.log().fatal(message.next());
            while (message.hasNext())
            {
                JiraStartupLogger.log().info(message.next());
            }
            throw new InvalidDatabaseDriverException(driverClassName, "JDBC Driver class '" + driverClassName + " could not be loaded.'", ex);
        }
    }

    /**
     * Get error message (for failed registration) specific to the database type.
     *
     * @return message as a list of strings (non-i18ned)
     */
    public Iterable<String> getErrorMessage()
    {
        final ImmutableList.Builder<String> builder = ImmutableList.<String>builder()
                .add("Driver for the database " + databaseType.getDisplayName() + " not found. Ensure it is installed in the 'lib' directory.");
        if (databaseType == DatabaseType.MY_SQL)
        {
            builder.add("If you are upgrading a standalone distribution of JIRA, this may be due to "
                    + "the fact that JIRA no longer ships with MySQL drivers.");
        }
        final HelpUrl link = getDbConfigLink(StaticHelpUrls.getInstance());
        if (link != null)
        {
            builder.add("Please visit " + link.getUrl() + " for more information.");
        }
        return builder.build();
    }

    public boolean isDriverRegistered()
    {
        try
        {
            Class.forName(driverClassName);
            return true;
        }
        catch (ClassNotFoundException ex)
        {
            return false;
        }
    }

    /**
     * Get link to DB configuration page for given database.
     *
     * @return link to DB configuration page. <code>null</code> if no such link exists.
     */
    private HelpUrl getDbConfigLink(HelpUrls urls)
    {
        if (!hasDbconfigLink(databaseType.getTypeName(), urls))
        {
            return null;
        }
        return getDbConfigLink(databaseType, urls);
    }

    public static HelpUrl getDbConfigLink(DatabaseType type, HelpUrls urls)
    {
        return getDbConfigLink(type.getTypeName(), urls);
    }

    private static HelpUrl getDbConfigLink(String key, HelpUrls urls)
    {
        return urls.getUrl(withDbConfigPrefix(key));
    }

    private static boolean hasDbconfigLink(String key, HelpUrls urls)
    {
        return urls.getUrlKeys().contains(withDbConfigPrefix(key));
    }

    private static String withDbConfigPrefix(String key)
    {
        return "dbconfig." + key;
    }
}
