package com.atlassian.jira.configurator.db;

import com.atlassian.jira.config.database.DatabaseType;
import com.atlassian.jira.config.database.jdbcurlparser.DatabaseInstance;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.configurator.gui.ConfigPanelBuilder;
import com.atlassian.jira.exception.ParseException;

import javax.swing.*;

public class PostgresConfigPanel extends CommonConfigPanel
{
    private JTextField tfDatabase = new JTextField(TEXT_FIELD_COLUMNS);
    // set scheme name to "public" by default.
    private JTextField tfSchemaName = new JTextField("public", TEXT_FIELD_COLUMNS);

    public PostgresConfigPanel()
    {
        super(DatabaseType.POSTGRES);
    }

    @Override
    public String getDisplayName()
    {
        return "PostgresQL";
    }

    public String getSchemaName()
    {
        return tfSchemaName.getText();
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

        // Set default values
        tfPort.setText("5432");

        // Set tool tips (base ones are set in CommonConfigPanel)
        tfDatabase.setToolTipText("The Postgres database to connect to");
        tfSchemaName.setToolTipText("Normally the schema-name is set to 'public'.");
        return panelBuilder.getPanel();
    }

    @Override
    protected void setUrlSettings(DatabaseInstance connectionProperties) throws ParseException
    {
        tfHostname.setText(connectionProperties.getHostname());
        tfPort.setText(connectionProperties.getPort());
        tfDatabase.setText(connectionProperties.getInstance());
    }

    protected void setSchemaName(final String schemaName)
    {
        tfSchemaName.setText(schemaName);
    }

    @Override
    public void validate() throws ValidationException
    {
        super.validate();
        validateNotBlank("Database", tfDatabase.getText());
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
