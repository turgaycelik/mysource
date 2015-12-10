package com.atlassian.jira.config.database.jdbcurlparser;

import com.atlassian.jira.exception.ParseException;

public class MySqlUrlParser implements JdbcUrlParser
{
    public String getUrl(String hostname, String port, String instance)
    {
        String url = "jdbc:mysql://" + hostname.trim();
        if (port.trim().length() > 0)
        {
            url += ':' + port.trim();
        }
        return url + "/" + instance.trim() + "?useUnicode=true&characterEncoding=UTF8&sessionVariables=storage_engine=InnoDB";
    }

    public DatabaseInstance parseUrl(String jdbcUrl) throws ParseException
    {
        // http://dev.mysql.com/doc/refman/5.4/en/connector-j-reference-configuration-properties.html
        // jdbc:mysql://[host][,failoverhost...][:port]/[database] [?propertyName1][=propertyValue1][&propertyName2][=propertyValue2]...
        if (!jdbcUrl.startsWith("jdbc:mysql://"))
        {
            throw new ParseException("Unable to parse the MySQL JDBC URL '" + jdbcUrl + "'.");
        }
        // Strip off the protocol prefix
        String stripped = jdbcUrl.substring("jdbc:mysql://".length());
        // Split on the required slash.
        // host:port on the left and database?properties on the right
        String[] hostPort_DatabaseAndProperties = stripped.split("/", 2);
        DatabaseInstance connectionProperties = new DatabaseInstance();
        String[] hostPort = hostPort_DatabaseAndProperties[0].split(":", 2);
        connectionProperties.setHostname(hostPort[0]);
        if (hostPort.length == 1)
        {
            connectionProperties.setPort("");
        }
        else
        {
            connectionProperties.setPort(hostPort[1]);
        }
        String[] database_Properties = hostPort_DatabaseAndProperties[1].split("\\?", 2);
        connectionProperties.setInstance(database_Properties[0]);
        // We currently ignore incoming custom properties and we set hard-coded ones (eg for UTF-8).
//        if (database_Properties.length == 1)
//        {
//            connectionProperties.properties = "";
//        }
//        else
//        {
//            connectionProperties.properties = database_Properties[1];
//        }

        return connectionProperties;
    }

}
