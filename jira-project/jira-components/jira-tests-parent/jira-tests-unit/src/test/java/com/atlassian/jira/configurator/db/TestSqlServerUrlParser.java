package com.atlassian.jira.configurator.db;

import com.atlassian.jira.config.database.jdbcurlparser.DatabaseInstance;
import com.atlassian.jira.config.database.jdbcurlparser.SqlServerUrlParser;
import com.atlassian.jira.exception.ParseException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestSqlServerUrlParser
{
    @Test
    public void testGetUrl() throws Exception
    {
        SqlServerUrlParser sqlServerUrlParser = new SqlServerUrlParser();
        
        assertEquals("jdbc:jtds:sqlserver://localhost:8543/JIRA", sqlServerUrlParser.getUrl("    localhost    ", " 8543  ", "  JIRA    "));
        assertEquals("jdbc:jtds:sqlserver://192.168.3.201/JIRA", sqlServerUrlParser.getUrl("192.168.3.201", "   ", "JIRA"));
        assertEquals("jdbc:jtds:sqlserver://localhost:4433", sqlServerUrlParser.getUrl("localhost", " 4433  ", ""));
        assertEquals("jdbc:jtds:sqlserver://db.acme.com:4433", sqlServerUrlParser.getUrl("db.acme.com", " 4433  ", " "));
    }

    @Test
    public void testParseError()
    {
        SqlServerUrlParser sqlServerUrlParser = new SqlServerUrlParser();
        try
        {
            sqlServerUrlParser.parseUrl("jdbc:notjtds:sqlserver://localhost:4444/JIRA");
            fail();
        }
        catch (ParseException e)
        {
            // goody
        }
    }

    @Test
    public void testParseJtds() throws Exception
    {
        SqlServerUrlParser sqlServerUrlParser = new SqlServerUrlParser();
        DatabaseInstance dbInstance;

        dbInstance = sqlServerUrlParser.parseUrl("jdbc:jtds:sqlserver://localhost:4444/JIRA");
        assertEquals("localhost", dbInstance.getHostname());
        assertEquals("4444", dbInstance.getPort());
        assertEquals("JIRA", dbInstance.getInstance());

        // jdbc:jtds:sqlserver://<server>[:<port>][/<database>][;<property>=<value>[;...]]
        dbInstance = sqlServerUrlParser.parseUrl("jdbc:jtds:sqlserver://localhost:4444/JIRA;dude=true;lady=false");
        assertEquals("localhost", dbInstance.getHostname());
        assertEquals("4444", dbInstance.getPort());
        assertEquals("JIRA", dbInstance.getInstance());

        dbInstance = sqlServerUrlParser.parseUrl("jdbc:jtds:sqlserver://db.some.com/myinstance;dude=true;lady=false");
        assertEquals("db.some.com", dbInstance.getHostname());
        assertEquals("", dbInstance.getPort());
        assertEquals("myinstance", dbInstance.getInstance());

        dbInstance = sqlServerUrlParser.parseUrl("jdbc:jtds:sqlserver://12.12.12.12:333;dude=true;lady=false");
        assertEquals("12.12.12.12", dbInstance.getHostname());
        assertEquals("333", dbInstance.getPort());
        assertEquals("", dbInstance.getInstance());
    }
    @Test
    public void testParseMsDriverWrongPrefix()
    {
        SqlServerUrlParser sqlServerUrlParser = new SqlServerUrlParser();
        try
        {
            sqlServerUrlParser.parseUrl("jdbc:sqlserver:\\");
            fail();
        }
        catch (ParseException e)
        {
            // cool
        }
    }

    @Test
    public void testParseMsDriverMin() throws ParseException
    {
        SqlServerUrlParser sqlServerUrlParser = new SqlServerUrlParser();
        DatabaseInstance connectionProperties = sqlServerUrlParser.parseUrl("jdbc:sqlserver://");
        assertEquals("", connectionProperties.getHostname());
        assertEquals("", connectionProperties.getPort());
        assertEquals("", connectionProperties.getInstance());
    }

    @Test
    public void testParseMsDriverFull() throws ParseException
    {
        //  jdbc:postgresql://host:port/database
        SqlServerUrlParser sqlServerUrlParser = new SqlServerUrlParser();
        DatabaseInstance connectionProperties = sqlServerUrlParser.parseUrl("jdbc:sqlserver://dbserver\\jira:123");
        assertEquals("dbserver", connectionProperties.getHostname());
        assertEquals("123", connectionProperties.getPort());
        assertEquals("jira", connectionProperties.getInstance());
    }

    @Test
    public void testParseMsDriverDefaultPort() throws ParseException
    {
        //  jdbc:postgresql://host:port/database
        SqlServerUrlParser sqlServerUrlParser = new SqlServerUrlParser();
        DatabaseInstance connectionProperties = sqlServerUrlParser.parseUrl("jdbc:sqlserver://dbserver\\jira");
        assertEquals("dbserver", connectionProperties.getHostname());
        assertEquals("", connectionProperties.getPort());
        assertEquals("jira", connectionProperties.getInstance());
    }

    @Test
    public void testParseMsDriverDefaultInstance() throws ParseException
    {
        //  jdbc:postgresql://host:port/database
        SqlServerUrlParser sqlServerUrlParser = new SqlServerUrlParser();
        DatabaseInstance connectionProperties = sqlServerUrlParser.parseUrl("jdbc:sqlserver://dbserver:32");
        assertEquals("dbserver", connectionProperties.getHostname());
        assertEquals("32", connectionProperties.getPort());
        assertEquals("", connectionProperties.getInstance());
    }    
}
