package com.atlassian.jira.configurator.db;

import com.atlassian.jira.config.database.jdbcurlparser.DatabaseInstance;
import com.atlassian.jira.config.database.jdbcurlparser.OracleUrlParser;
import com.atlassian.jira.exception.ParseException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestOracleUrlParser
{
    @Test
    public void test_getUrl() throws Exception
    {
        OracleUrlParser oracleUrlParser = new OracleUrlParser();

        assertEquals("jdbc:oracle:thin:@//blah:123/MYORA", oracleUrlParser.getUrl("blah", "123", "MYORA"));
    }

    @Test
    public void testInvalid()
    {
        OracleUrlParser oracleUrlParser = new OracleUrlParser();
        try
        {
            oracleUrlParser.parseUrl("jdbc:oracle:fat:");
            fail();
        }
        catch (ParseException e)
        {
            // cool
        }
    }

    @Test
    public void testMissingAt()
    {
        OracleUrlParser oracleUrlParser = new OracleUrlParser();
        try
        {
            oracleUrlParser.parseUrl("jdbc:oracle:thin:localhost:SID");
            fail();
        }
        catch (ParseException e)
        {
            // cool
        }
    }

    @Test
    public void testMinimal() throws Exception
    {
        OracleUrlParser oracleUrlParser = new OracleUrlParser();
        DatabaseInstance connectionProperties = oracleUrlParser.parseUrl("jdbc:oracle:thin:@:ORCL");
        assertEquals("", connectionProperties.getHostname());
        assertEquals("", connectionProperties.getPort());
        assertEquals("ORCL", connectionProperties.getInstance());
    }

    @Test
    public void testFull() throws Exception
    {
        OracleUrlParser oracleUrlParser = new OracleUrlParser();
        DatabaseInstance connectionProperties = oracleUrlParser.parseUrl("jdbc:oracle:thin:@db.acme.com:1522:ORCL");
        assertEquals("db.acme.com", connectionProperties.getHostname());
        assertEquals("1522", connectionProperties.getPort());
        assertEquals("ORCL", connectionProperties.getInstance());
    }

    @Test
    public void testDefaultHost() throws Exception
    {
        OracleUrlParser oracleUrlParser = new OracleUrlParser();
        DatabaseInstance connectionProperties = oracleUrlParser.parseUrl("jdbc:oracle:thin:@:1522:ORCL");
        assertEquals("", connectionProperties.getHostname());
        assertEquals("1522", connectionProperties.getPort());
        assertEquals("ORCL", connectionProperties.getInstance());
    }

    @Test
    public void testDefaultPort() throws Exception
    {
        OracleUrlParser oracleUrlParser = new OracleUrlParser();
        DatabaseInstance connectionProperties = oracleUrlParser.parseUrl("jdbc:oracle:thin:@localhost:ORCL");
        assertEquals("localhost", connectionProperties.getHostname());
        assertEquals("", connectionProperties.getPort());
        assertEquals("ORCL", connectionProperties.getInstance());
    }

    @Test
    public void testSlashMinimal() throws Exception
    {
        OracleUrlParser oracleUrlParser = new OracleUrlParser();
        DatabaseInstance connectionProperties = oracleUrlParser.parseUrl("jdbc:oracle:thin:@///XE");
        assertEquals("", connectionProperties.getHostname());
        assertEquals("", connectionProperties.getPort());
        assertEquals("XE", connectionProperties.getInstance());
    }

    @Test
    public void testSlashFull() throws Exception
    {
        OracleUrlParser oracleUrlParser = new OracleUrlParser();
        DatabaseInstance connectionProperties = oracleUrlParser.parseUrl("jdbc:oracle:thin:@//db.acme.com:1522/MYORA");
        assertEquals("db.acme.com", connectionProperties.getHostname());
        assertEquals("1522", connectionProperties.getPort());
        assertEquals("MYORA", connectionProperties.getInstance());
    }

    @Test
    public void testSlashDefaultHost() throws Exception
    {
        OracleUrlParser oracleUrlParser = new OracleUrlParser();
        DatabaseInstance connectionProperties = oracleUrlParser.parseUrl("jdbc:oracle:thin:@//:1522/ORCL2");
        assertEquals("", connectionProperties.getHostname());
        assertEquals("1522", connectionProperties.getPort());
        assertEquals("ORCL2", connectionProperties.getInstance());
    }

    @Test
    public void testSlashDefaultPort() throws Exception
    {
        OracleUrlParser oracleUrlParser = new OracleUrlParser();
        DatabaseInstance connectionProperties = oracleUrlParser.parseUrl("jdbc:oracle:thin:@//localhost/BORCL");
        assertEquals("localhost", connectionProperties.getHostname());
        assertEquals("", connectionProperties.getPort());
        assertEquals("BORCL", connectionProperties.getInstance());
    }
}
