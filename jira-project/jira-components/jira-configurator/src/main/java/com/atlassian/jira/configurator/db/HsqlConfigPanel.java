package com.atlassian.jira.configurator.db;

import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.exception.ParseException;

import javax.swing.*;

public class HsqlConfigPanel extends DatabaseConfigPanel
{
    private JPanel panel;

    @Override
    public void validate() throws ValidationException
    {
        // no user entered properties
    }

    @Override
    public String getDisplayName()
    {
        return "HSQL";
    }

    @Override
    public String getClassName()
    {
        return "org.hsqldb.jdbcDriver";
    }

    @Override
    public String getUrl(String jiraHome)
    {
        return "jdbc:hsqldb:" + jiraHome + "/database/jiradb";
    }

    @Override
    public String getUsername()
    {
        return "sa";
    }

    @Override
    public String getPassword()
    {
        return "";
    }

    @Override
    public String getSchemaName()
    {
        // Schema name is always "PUBLIC" for HSQL
        return "PUBLIC";
    }

    @Override
    public JPanel getPanel()
    {
        if (panel == null)
        {
            panel = new JPanel();
            panel.add(new JLabel("The built-in HSQL database is auto-configured."));
        }
        return panel;
    }

    @Override
    public void setSettings(final Settings settings) throws ParseException
    {
        // Nothing to remember
    }

    @Override
    public void testConnection(String jiraHome)
    {
        // Nothing to test - this is an internal DB.
    }
}
