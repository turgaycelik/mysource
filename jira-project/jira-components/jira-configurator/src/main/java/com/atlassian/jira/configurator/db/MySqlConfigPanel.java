package com.atlassian.jira.configurator.db;

import com.atlassian.jira.config.database.JdbcDatasource;
import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.configurator.gui.ConfigPanelBuilder;
import com.atlassian.jira.exception.ParseException;

import javax.swing.*;

public class MySqlConfigPanel extends DatabaseConfigPanel
{
    private JTextField tfHostname = new JTextField(TEXT_FIELD_COLUMNS);
    private JTextField tfPort = new JTextField(TEXT_FIELD_COLUMNS);
    private JTextField tfDatabase = new JTextField(TEXT_FIELD_COLUMNS);
    private JTextField tfUsername = new JTextField(TEXT_FIELD_COLUMNS);
    private JTextField tfPassword = new JPasswordField(TEXT_FIELD_COLUMNS);
    private JPanel configPanel;

    @Override
    public String getDisplayName()
    {
        return "MySQL";
    }

    @Override
    public String getClassName()
    {
        return "com.mysql.jdbc.Driver";
    }

    @Override
    public String getUrl(String jiraHome)
    {
        String url = "jdbc:mysql://" + tfHostname.getText();
        if (tfPort.getText().trim().length() > 0)
        {
            url += ':' + tfPort.getText();
        }
        return url + "/" + tfDatabase.getText() + "?useUnicode=true&characterEncoding=UTF8&sessionVariables=storage_engine=InnoDB";
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
        // MySQL should use the default schema for the logged in User.
        return null;
    }

    @Override
    public JPanel getPanel()
    {
        if (configPanel == null)
        {
            ConfigPanelBuilder panelBuilder = new ConfigPanelBuilder();
            panelBuilder.add("Hostname", tfHostname);
            tfHostname.setToolTipText("The hostname or IP address of the MySQL server");
            panelBuilder.add("Port", tfPort);
            tfPort.setText("3306");
            tfPort.setToolTipText("The port number that MySQL is running on. Leave blank for default (3306)");
            panelBuilder.add("Database", tfDatabase);
            tfDatabase.setToolTipText("The database to connect to");
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
        ConnectionProperties connectionProperties = parseUrl(datasourceBuilder.getJdbcUrl());

        tfHostname.setText(connectionProperties.host);
        tfPort.setText(connectionProperties.port);
        tfDatabase.setText(connectionProperties.database);
        // For now we ignore user-configured properties and hard-code the UTF-8 stuff
    }

    ConnectionProperties parseUrl(final String jdbcUrl) throws ParseException
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
        ConnectionProperties connectionProperties = new ConnectionProperties();
        String[] hostPort = hostPort_DatabaseAndProperties[0].split(":", 2);
        connectionProperties.host = hostPort[0];
        if (hostPort.length == 1)
        {
            connectionProperties.port = "";
        }
        else
        {
            connectionProperties.port = hostPort[1];
        }
        String[] database_Properties = hostPort_DatabaseAndProperties[1].split("\\?", 2);
        connectionProperties.database = database_Properties[0];
        if (database_Properties.length == 1)
        {
            connectionProperties.properties = "";
        }
        else
        {
            connectionProperties.properties = database_Properties[1];
        }

        return connectionProperties;
    }

    @Override
    public void validate() throws ValidationException
    {
        // Hostname can be empty, defaults to 127.0.0.1
        // Port can be empty, but must be a valid number if not
        validatePortNumber(tfPort.getText());
        validateNotBlank("Database", tfDatabase.getText());
    }

    class ConnectionProperties
    {
        String host;
        String port;
        String database;
        String properties;
    }
}
