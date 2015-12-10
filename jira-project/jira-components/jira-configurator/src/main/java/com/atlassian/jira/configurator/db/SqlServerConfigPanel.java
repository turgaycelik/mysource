package com.atlassian.jira.configurator.db;

import com.atlassian.jira.config.database.DatabaseType;
import com.atlassian.jira.config.database.jdbcurlparser.DatabaseInstance;
import com.atlassian.jira.configurator.gui.ConfigPanelBuilder;
import com.atlassian.jira.exception.ParseException;

import javax.swing.*;

public class SqlServerConfigPanel extends CommonConfigPanel
{
    private JTextField tfDatabase = new JTextField(TEXT_FIELD_COLUMNS);
    private JTextField tfSchemaName = new JTextField(TEXT_FIELD_COLUMNS);

    public SqlServerConfigPanel()
    {
        super(DatabaseType.SQL_SERVER);
    }

    @Override
    public String getDisplayName()
    {
        return "SQL Server";
    }

    @Override
    protected JPanel buildConfigPanel()
    {
        ConfigPanelBuilder panelBuilder = new ConfigPanelBuilder();
        panelBuilder.add("Hostname", tfHostname);
        panelBuilder.add("Port", tfPort);
        panelBuilder.add("Database", tfDatabase);
        panelBuilder.add("Username", tfUsername);
        panelBuilder.add("Password", tfPassword);
        panelBuilder.add("Schema", tfSchemaName);

        tfPort.setText("1433");
        tfSchemaName.setText("dbo");

        tfDatabase.setToolTipText("The database instance to connect to");
        tfSchemaName.setToolTipText("Set an explicit schema, or leave blank to use the default schema for the database");
        return panelBuilder.getPanel();
    }

    @Override
    protected void setUrlSettings(final DatabaseInstance connectionProperties) throws ParseException
    {
        tfHostname.setText(connectionProperties.getHostname());
        tfPort.setText(connectionProperties.getPort());
        tfDatabase.setText(connectionProperties.getInstance());
    }

    @Override
    public String getSchemaName()
    {
        return tfSchemaName.getText();
    }

    @Override
    protected void setSchemaName(final String schemaName)
    {
        // SQL Server should use this
        tfSchemaName.setText(schemaName);
    }

    @Override
    protected String getHostname()
    {
        return tfHostname.getText().trim();
    }

    @Override
    protected String getPort()
    {
        return tfPort.getText().trim();
    }

    @Override
    protected String getInstance()
    {
        return tfDatabase.getText().trim();
    }
}
