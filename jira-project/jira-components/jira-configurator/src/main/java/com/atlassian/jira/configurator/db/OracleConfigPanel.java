package com.atlassian.jira.configurator.db;

import com.atlassian.jira.config.database.DatabaseType;
import com.atlassian.jira.config.database.JdbcDatasource;
import com.atlassian.jira.config.database.jdbcurlparser.DatabaseInstance;
import com.atlassian.jira.config.database.jdbcurlparser.JdbcUrlParser;
import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.configurator.gui.ConfigPanelBuilder;
import com.atlassian.jira.exception.ParseException;

import javax.swing.*;

public class OracleConfigPanel extends DatabaseConfigPanel
{
    private JTextField tfHostname = new JTextField(TEXT_FIELD_COLUMNS);
    // Set the default Port Number. Blank is not valid for the Oracle JDBC drivers included in JIRA.
    private JTextField tfPort = new JTextField("1521", TEXT_FIELD_COLUMNS);
    private JTextField tfServiceName = new JTextField(TEXT_FIELD_COLUMNS);
    private JTextField tfUsername = new JTextField(TEXT_FIELD_COLUMNS);
    private JTextField tfPassword = new JPasswordField(TEXT_FIELD_COLUMNS);
    private JPanel configPanel;

    private final JdbcUrlParser urlParser = DatabaseType.ORACLE.getJdbcUrlParser();

    @Override
    public String getDisplayName()
    {
        return "Oracle";
    }

    @Override
    public String getClassName()
    {
        return "oracle.jdbc.OracleDriver";
    }

    @Override
    public String getUrl(String jiraHome)
    {
        try
        {
            return urlParser.getUrl(tfHostname.getText(), tfPort.getText(), tfServiceName.getText());
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getUsername()
    {
        return tfUsername.getText();
    }

    @Override
    public String getPassword()
    {
        return tfPassword.getText();
    }

    @Override
    public String getSchemaName()
    {
        // Oracle should use the default schema for the logged in user
        return null;
    }

    @Override
    public JPanel getPanel()
    {
        if (configPanel == null)
        {
            ConfigPanelBuilder panelBuilder = new ConfigPanelBuilder();
            panelBuilder.add("Hostname", tfHostname);
            tfHostname.setToolTipText("The hostname or IP address of the Oracle server");
            panelBuilder.add("Port", tfPort);
            tfPort.setToolTipText("The port number that Oracle is running on. (Default is 1521)");
            panelBuilder.add("Service", tfServiceName);
            tfServiceName.setToolTipText("Service Name or SID of the Oracle database. (eg 'ORCL'. For Express Edition use 'XE')");
            panelBuilder.add("Username", tfUsername);
            tfUsername.setToolTipText("The username used to login");
            panelBuilder.add("Password", tfPassword);
            tfPassword.setToolTipText("The password used to login");
            configPanel = panelBuilder.getPanel();
        }
        return configPanel;
    }

    @Override
    public void setSettings(final Settings settings) throws ParseException
    {
        final JdbcDatasource.Builder datasourceBuilder = settings.getJdbcDatasourceBuilder();
        tfUsername.setText(datasourceBuilder.getUsername());
        tfPassword.setText(datasourceBuilder.getPassword());

        // parse the URL.
        DatabaseInstance connectionProperties = urlParser.parseUrl(datasourceBuilder.getJdbcUrl());

        tfHostname.setText(connectionProperties.getHostname());
        tfPort.setText(connectionProperties.getPort());
        tfServiceName.setText(connectionProperties.getInstance());
    }

    @Override
    public void validate() throws ValidationException
    {
        // Hostname can be empty, defaults to 127.0.0.1
        // Oracle JDBC drivers don't seem to allow a blank Port Number.
        if (tfPort.getText().trim().length() == 0)
        {
            throw new ValidationException("Please supply a Port Number to connect to. (Default Oracle port is 1521).");
        }
        validatePortNumber(tfPort.getText());
    }

    class OracleConnectionProperties
    {
        String host;
        String port;
        String sid;
    }
}
