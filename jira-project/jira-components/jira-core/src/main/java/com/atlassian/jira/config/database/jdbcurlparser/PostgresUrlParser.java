package com.atlassian.jira.config.database.jdbcurlparser;

import com.atlassian.jira.exception.ParseException;

public class PostgresUrlParser extends AbstractJdbcUrlParser
{
    public String getUrl(String hostname, String port, String instance) throws ParseException
    {
        //  http://doc.postgresintl.com/jdbc/ch03s03.html
        //  jdbc:postgresql:database
        //  jdbc:postgresql://host/database
        //  jdbc:postgresql://host:port/database

        // database is mandatory
        if (instance == null || instance.length() == 0)
        {
            throw new ParseException("Database is a required field");
        }

        String host = hostname.trim();
        port = port.trim();
        if (port.length() == 0)
        {
            port = "";
        }
        else
        {
            port = ":" + port;
        }

        if (host.length() == 0)
        {
            if (port.length() == 0)
            {
                // Host and port are both blank. We use a special simple form of the URL:
                return "jdbc:postgresql:" + instance.trim();
            }
            else
            {
                // Postgres JDBC cannot handle a URL with blank hostname, but explicit portnumber.
                // We will convert the blank hostname to "localhost"
                host = "localhost";
            }
        }

        return "jdbc:postgresql://" + host + port + "/" + instance.trim();
    }

    public DatabaseInstance parseUrl(String jdbcUrl) throws ParseException
    {
        //  http://doc.postgresintl.com/jdbc/ch03s03.html
        DatabaseInstance databaseInstance = new DatabaseInstance();
        //  jdbc:postgresql:database
        //  jdbc:postgresql://host/database
        //  jdbc:postgresql://host:port/database
        String stripped = removeProtocolPrefix(jdbcUrl);
        if (!stripped.startsWith("//"))
        {
            databaseInstance.setInstance(stripped);
            return databaseInstance;
        }
        // remove leading slashes
        stripped = stripped.substring(2);
        // Split on the slash
        String[] hostPort_Database = stripped.split("/", 2);
        if (hostPort_Database.length != 2)
        {
            throw new ParseException("Unable to parse the JDBC URL '" + jdbcUrl + "'. Missing '/' separator.");
        }
        databaseInstance.setInstance(hostPort_Database[1]);
        String[] hostPort = hostPort_Database[0].split(":");
        databaseInstance.setHostname(hostPort[0]);
        if (hostPort.length > 1)
        {
            databaseInstance.setPort(hostPort[1]);
        }

        return databaseInstance;
    }

    protected String getProtocolPrefix()
    {
        return "jdbc:postgresql:";
    }
}
