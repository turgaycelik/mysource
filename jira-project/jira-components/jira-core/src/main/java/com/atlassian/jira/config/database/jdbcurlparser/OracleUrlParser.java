package com.atlassian.jira.config.database.jdbcurlparser;

import com.atlassian.jira.exception.ParseException;

public class OracleUrlParser implements JdbcUrlParser
{
    public String getUrl(String hostname, String port, String instance)
    {
        // http://docs.oracle.com/cd/E11882_01/java.112/e16548/urls.htm#JJDBC28292
        // http://www.orafaq.com/wiki/JDBC#Thin_driver
        // JRA-29755: We use the "new syntax" so we will work with SID or Service name (needed for clustered Oracle)
        //    jdbc:oracle:thin:@//[HOST][:PORT]/SERVICE
        return "jdbc:oracle:thin:@//" + hostname.trim() + ':' + port.trim() + "/" + instance.trim();
    }

    public DatabaseInstance parseUrl(final String jdbcUrl) throws ParseException
    {
        DatabaseInstance oracleConnectionProperties = new DatabaseInstance();
        // http://www.herongyang.com/jdbc/Oracle-JDBC-Driver-Connection-URL.html
        //    jdbc:oracle:thin:[user/password]@[host][:port]:SID
        //    jdbc:oracle:thin:[user/password]@//[host][:port]/SID

        if (!jdbcUrl.startsWith("jdbc:oracle:thin:"))
        {
            throw new ParseException("Unable to parse the Oracle JDBC URL '" + jdbcUrl +
                    "'. It should start with protocol prefix 'jdbc:oracle:thin:'.");
        }
        // Strip off the protocol prefix
        String stripped = jdbcUrl.substring("jdbc:oracle:thin:".length());
        // Get the text after the @
        String[] split = stripped.split("@", 2);
        if (split.length == 1)
        {
            throw new ParseException("Unable to parse the Oracle JDBC URL '" + jdbcUrl +
                    "'. Expected to find a '@' before the host name.");
        }
        String props = split[1];
        // There are two slightly different formats and we need to handle both.
        // We used to build URLs with the "old syntax" and now we build with the "new syntax" so we could encounter either.
        // Furthermore, customers could manually edit the dbconfig.xml file.
        if (props.startsWith("//"))
        {
            // New Syntax. Looks like:
            //    jdbc:oracle:thin:[user/password]@//[host][:port]/SID

            // Strip off the //
            props = props.substring(2);
            String[] hostPort_Sid = props.split("/", 2);
            if (hostPort_Sid.length == 1)
            {
                throw new ParseException("Unable to parse the Oracle JDBC URL '" + jdbcUrl +
                        "'. Missing '/' before the SID.");
            }
            oracleConnectionProperties.setInstance(hostPort_Sid[1]);
            String[] hostPort = hostPort_Sid[0].split(":");
            oracleConnectionProperties.setHostname(hostPort[0]);
            if (hostPort.length == 1)
            {
                oracleConnectionProperties.setPort("");
            }
            else
            {
                oracleConnectionProperties.setPort(hostPort[1]);
            }
        }
        else
        {
            // Old Syntax. Looks like:
            //    jdbc:oracle:thin:[user/password]@[host][:port]:SID

            String[] host_Port_Sid = props.split(":", 3);
            if (host_Port_Sid.length == 1)
            {
                throw new ParseException("Unable to parse the Oracle JDBC URL '" + jdbcUrl +
                        "'. Missing ':' before the SID.");
            }
            if (host_Port_Sid.length == 2)
            {
                // port must be missing
                oracleConnectionProperties.setHostname(host_Port_Sid[0]);
                oracleConnectionProperties.setPort("");
                oracleConnectionProperties.setInstance(host_Port_Sid[1]);
            }
            else
            {
                oracleConnectionProperties.setHostname(host_Port_Sid[0]);
                oracleConnectionProperties.setPort(host_Port_Sid[1]);
                oracleConnectionProperties.setInstance(host_Port_Sid[2]);
            }
        }

        return oracleConnectionProperties;
    }

}
