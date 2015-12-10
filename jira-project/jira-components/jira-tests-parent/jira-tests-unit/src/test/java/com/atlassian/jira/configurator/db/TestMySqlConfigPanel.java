package com.atlassian.jira.configurator.db;

import com.atlassian.jira.exception.ParseException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestMySqlConfigPanel
{
    @Test
    public void testInvalidUrl()
    {
        MySqlConfigPanel mySqlConfigPanel = new MySqlConfigPanel();
        try
        {
            mySqlConfigPanel.parseUrl("asdf");
            fail("Should not parse");
        }
        catch (ParseException e)
        {
            // expected
        }
    }

    @Test
    public void testMinimalUrl() throws Exception
    {
        MySqlConfigPanel mySqlConfigPanel = new MySqlConfigPanel();
        MySqlConfigPanel.ConnectionProperties connectionProperties = mySqlConfigPanel.parseUrl("jdbc:mysql:///");
        assertEquals("", connectionProperties.host);
        assertEquals("", connectionProperties.port);
        assertEquals("", connectionProperties.database);
        assertEquals("", connectionProperties.properties);
    }

    @Test
    public void testFullUrl() throws Exception
    {
        MySqlConfigPanel mySqlConfigPanel = new MySqlConfigPanel();
        MySqlConfigPanel.ConnectionProperties connectionProperties = mySqlConfigPanel.parseUrl("jdbc:mysql://db.acme.com:3999/jira?foo=bar&stuff");
        assertEquals("db.acme.com", connectionProperties.host);
        assertEquals("3999", connectionProperties.port);
        assertEquals("jira", connectionProperties.database);
        assertEquals("foo=bar&stuff", connectionProperties.properties);
    }

    @Test
    public void testDefaultPort() throws Exception
    {
        MySqlConfigPanel mySqlConfigPanel = new MySqlConfigPanel();
        MySqlConfigPanel.ConnectionProperties connectionProperties = mySqlConfigPanel.parseUrl("jdbc:mysql://localhost/jira");
        assertEquals("localhost", connectionProperties.host);
        assertEquals("", connectionProperties.port);
        assertEquals("jira", connectionProperties.database);
        assertEquals("", connectionProperties.properties);
    }

    @Test
    public void testDefaultHost() throws Exception
    {
        MySqlConfigPanel mySqlConfigPanel = new MySqlConfigPanel();
        MySqlConfigPanel.ConnectionProperties connectionProperties = mySqlConfigPanel.parseUrl("jdbc:mysql://:4000/jira");
        assertEquals("", connectionProperties.host);
        assertEquals("4000", connectionProperties.port);
        assertEquals("jira", connectionProperties.database);
        assertEquals("", connectionProperties.properties);
    }

    @Test
    public void testDefaultDatabase() throws Exception
    {
        MySqlConfigPanel mySqlConfigPanel = new MySqlConfigPanel();
        MySqlConfigPanel.ConnectionProperties connectionProperties = mySqlConfigPanel.parseUrl("jdbc:mysql://localhost:3666/?foo=bar");
        assertEquals("localhost", connectionProperties.host);
        assertEquals("3666", connectionProperties.port);
        assertEquals("", connectionProperties.database);
        assertEquals("foo=bar", connectionProperties.properties);
    }

}
