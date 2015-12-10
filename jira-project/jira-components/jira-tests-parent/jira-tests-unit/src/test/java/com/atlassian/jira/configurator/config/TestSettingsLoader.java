package com.atlassian.jira.configurator.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.atlassian.jira.exception.ParseException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestSettingsLoader
{
    @Test
    public void testEmpty() throws IOException
    {
        String properties = "";
        final InputStream propertiesInputStream = new ByteArrayInputStream(properties.getBytes());
        try
        {
            SettingsLoader.getJiraHomeValue(propertiesInputStream);
            fail();
        }
        catch (ParseException e)
        {
            // expected
        }
    }

    @Test
    public void testMissing() throws IOException
    {
        String properties =
                "###########################\n"
                + "# Note for Windows Users\n"
                + "###########################\n"
                + "#\n"
                + "# Each backslash in your path must be written as a forward slash.\n"
                + "# - For example:\n"
                + "# c:\\jira\\data\n"
                + "#\n"
                + "# should be written as:\n"
                + "#\n"
                + "# c:/jira/data\n"
                + "#jira.home =\n"
                + "jira.home.other = blah\n"
                + "\n"
                + "jira.title = JIRA\n"
                + "\n"
                + "jira.disable.login.gadget = false";
        final InputStream propertiesInputStream = new ByteArrayInputStream(properties.getBytes());
        try
        {
            SettingsLoader.getJiraHomeValue(propertiesInputStream);
            fail();
        }
        catch (ParseException e)
        {
            // expected
        }
    }

    @Test
    public void testRealFileNoHome() throws Exception
    {
        String properties =
                "###########################\n"
                + "# Note for Windows Users\n"
                + "###########################\n"
                + "#\n"
                + "# Each backslash in your path must be written as a forward slash.\n"
                + "# - For example:\n"
                + "# c:\\jira\\data\n"
                + "#\n"
                + "# should be written as:\n"
                + "#\n"
                + "# c:/jira/data\n"
                + "jira.home =\n"
                + "\n"
                + "jira.title = JIRA\n"
                + "\n"
                + "jira.disable.login.gadget = false";
        final InputStream propertiesInputStream = new ByteArrayInputStream(properties.getBytes());
        String jiraHome = SettingsLoader.getJiraHomeValue(propertiesInputStream);
        assertEquals("", jiraHome);
    }

    @Test
    public void testRealFileHomeConfigured() throws Exception
    {
        String properties =
                "###########################\n"
                + "# Note for Windows Users\n"
                + "###########################\n"
                + "#\n"
                + "# Each backslash in your path must be written as a forward slash.\n"
                + "# - For example:\n"
                + "# c:\\jira\\data\n"
                + "#\n"
                + "# should be written as:\n"
                + "#\n"
                + "# c:/jira/data\n"
                + "jira.home = /opt/Atlassian/jira_home\n"
                + "\n"
                + "jira.title = JIRA\n"
                + "\n"
                + "jira.disable.login.gadget = false";
        final InputStream propertiesInputStream = new ByteArrayInputStream(properties.getBytes());
        String jiraHome = SettingsLoader.getJiraHomeValue(propertiesInputStream);
        assertEquals("/opt/Atlassian/jira_home", jiraHome);
    }

    @Test
    public void testRealFileWindowsPathForwardSlashes() throws Exception
    {
        String properties =
                "###########################\n"
                + "# Note for Windows Users\n"
                + "###########################\n"
                + "#\n"
                + "# Each backslash in your path must be written as a forward slash.\n"
                + "# - For example:\n"
                + "# c:\\jira\\data\n"
                + "#\n"
                + "# should be written as:\n"
                + "#\n"
                + "# c:/jira/data\n"
                + "jira.home = C:/Program Files (x86)/Atlassian/Application Data/JIRA\n"
                + "\n"
                + "jira.title = JIRA\n"
                + "\n"
                + "jira.disable.login.gadget = false";
        final InputStream propertiesInputStream = new ByteArrayInputStream(properties.getBytes());
        String jiraHome = SettingsLoader.getJiraHomeValue(propertiesInputStream);
        assertEquals("C:/Program Files (x86)/Atlassian/Application Data/JIRA", jiraHome);
    }

    @Test
    public void testRealFileWindowsPathBackSlashes() throws Exception
    {
        String properties =
                "###########################\n"
                + "# Note for Windows Users\n"
                + "###########################\n"
                + "#\n"
                + "# Each backslash in your path must be written as a forward slash.\n"
                + "# - For example:\n"
                + "# c:\\jira\\data\n"
                + "#\n"
                + "# should be written as:\n"
                + "#\n"
                + "# c:/jira/data\n"
                + "jira.home = C:\\\\Program Files (x86)\\\\Atlassian\\\\Application Data\\\\JIRA\n"
                + "\n"
                + "jira.title = JIRA\n"
                + "\n"
                + "jira.disable.login.gadget = false";
        final InputStream propertiesInputStream = new ByteArrayInputStream(properties.getBytes());
        String jiraHome = SettingsLoader.getJiraHomeValue(propertiesInputStream);
        assertEquals("C:\\Program Files (x86)\\Atlassian\\Application Data\\JIRA", jiraHome);
    }

    @Test
    public void testRealFileWindowsPathBackSlashesColonEscaped() throws Exception
    {
        String properties =
                "###########################\n"
                + "# Note for Windows Users\n"
                + "###########################\n"
                + "#\n"
                + "# Each backslash in your path must be written as a forward slash.\n"
                + "# - For example:\n"
                + "# c:\\jira\\data\n"
                + "#\n"
                + "# should be written as:\n"
                + "#\n"
                + "# c:/jira/data\n"
                + "jira.home = C\\:\\\\Program Files (x86)\\\\Atlassian\\\\Application Data\\\\JIRA\n"
                + "\n"
                + "jira.title = JIRA\n"
                + "\n"
                + "jira.disable.login.gadget = false";
        final InputStream propertiesInputStream = new ByteArrayInputStream(properties.getBytes());
        String jiraHome = SettingsLoader.getJiraHomeValue(propertiesInputStream);
        assertEquals("C:\\Program Files (x86)\\Atlassian\\Application Data\\JIRA", jiraHome);
    }

}
