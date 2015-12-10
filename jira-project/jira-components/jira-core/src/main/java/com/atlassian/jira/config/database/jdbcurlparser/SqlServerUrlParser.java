package com.atlassian.jira.config.database.jdbcurlparser;

import com.atlassian.jira.exception.ParseException;

public class SqlServerUrlParser extends AbstractJdbcUrlParser
{
    private static final String MICROSOFT_DRIVER_PREFIX = "jdbc:sqlserver://";
    private static final String JTDS_DRIVER_PREFIX = "jdbc:jtds:sqlserver://";

    private String protocolPrefix;

    public String getUrl(String hostname, String port, String instance)
    {
        // http://jtds.sourceforge.net/faq.html
        // jdbc:jtds:<server_type>://<server>[:<port>][/<database>][;<property>=<value>[;...]]

        port = port.trim();
        if (port.length() > 0)
        {
            port = ":" + port;
        }
        instance = instance.trim();
        if (instance.length() > 0)
        {
            instance = "/" + instance;
        }
        return JTDS_DRIVER_PREFIX + hostname.trim() + port + instance;
    }

    public DatabaseInstance parseUrl(String jdbcUrl) throws ParseException
    {
        // The incoming URL could be using the jTDS driveres or the microsoft drivers
        if (jdbcUrl.startsWith(MICROSOFT_DRIVER_PREFIX))
        {
            protocolPrefix = MICROSOFT_DRIVER_PREFIX;
            return parseUrlMicrosoftDriver(jdbcUrl);
        }
        else
        {
            protocolPrefix = JTDS_DRIVER_PREFIX;
            return parseUrlJtdsDriver(jdbcUrl);
        }
    }

    private DatabaseInstance parseUrlJtdsDriver(String jdbcUrl) throws ParseException
    {
        // http://jtds.sourceforge.net/faq.html
        // jdbc:jtds:sqlserver://<server>[:<port>][/<database>][;<property>=<value>[;...]]
        DatabaseInstance databaseInstance = new DatabaseInstance();
        String stripped = removeProtocolPrefix(jdbcUrl);
        // remove properties from the end if any
        stripped = stripped.split(";", 2)[0];
        // split out the instance
        String[] hostPort_Instance = stripped.split("/");
        if (hostPort_Instance.length > 1)
        {
            databaseInstance.setInstance(hostPort_Instance[1]);
        }
        // split out the port
        String[] host_Port = hostPort_Instance[0].split(":", 2);
        if (host_Port.length > 1)
        {
            databaseInstance.setPort(host_Port[1]);
        }
        // Set the hostname
        databaseInstance.setHostname(host_Port[0]);
        
        return databaseInstance;
    }

    private DatabaseInstance parseUrlMicrosoftDriver(String jdbcUrl) throws ParseException
    {
        // http://msdn.microsoft.com/en-us/library/ms378428(SQL.90).aspx
        // jdbc:sqlserver://[serverName[\instanceName][:portNumber]][;property=value[;property=value]]
        DatabaseInstance databaseInstance = new DatabaseInstance();
        String stripped = removeProtocolPrefix(jdbcUrl);
        // remove properties from the end if any
        stripped = stripped.split(";", 2)[0];
        // Check for port number
        String[] hostInstance_Port = stripped.split(":", 2);
        if (hostInstance_Port.length > 1)
        {
            databaseInstance.setPort(hostInstance_Port[1]);
        }
        // split out the instance
        String[] host_instance = hostInstance_Port[0].split("\\\\");
        databaseInstance.setHostname(host_instance[0]);
        if (host_instance.length > 1)
        {
            databaseInstance.setInstance(host_instance[1]);
        }

        return databaseInstance;
    }

    protected String getProtocolPrefix() throws ParseException
    {
        return protocolPrefix;
    }
}
