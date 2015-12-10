package com.atlassian.jira.config.database;

import com.atlassian.jira.config.CustomConfigHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

/**
 * Handler for parsing datasource config from and writing datasource config to XML.
 */
public class DatabaseConfigHandler implements CustomConfigHandler<DatabaseConfig>
{
    private static final Logger log = Logger.getLogger(DatabaseConfigHandler.class);

    static final String NAME = "name";
    static final String DELEGATOR_NAME = "delegator-name";
    static final String DATABASE_TYPE = "database-type";
    static final String SCHEMA_NAME = "schema-name";

    @Override
    public Class<DatabaseConfig> getBeanClass()
    {
        return DatabaseConfig.class;
    }

    @Override
    public DatabaseConfig parse(Element element)
    {
        String name = element.elementText(NAME);
        String delegator = parseOptionalString(element, DELEGATOR_NAME, name);
        String databaseType = element.elementText(DATABASE_TYPE);
        String schemaName = element.elementText(SCHEMA_NAME);
        Datasource datasource = parseDatasource(element);
        return new DatabaseConfig(name, delegator, databaseType, schemaName, datasource);
    }

    @Override
    public void writeTo(Element element, DatabaseConfig databaseConfig)
    {
        element.addElement(NAME).setText(databaseConfig.getDatasourceName());
        element.addElement(DELEGATOR_NAME).setText(databaseConfig.getDelegatorName());
        element.addElement(DATABASE_TYPE).setText(databaseConfig.getDatabaseType());
        if (!StringUtils.isBlank(databaseConfig.getSchemaName()))
        {
            element.addElement(SCHEMA_NAME).setText(databaseConfig.getSchemaName());
        }
        writeDatasource(element, databaseConfig.getDatasource());
    }

    private void writeDatasource(Element element, Datasource datasource)
    {
        if (datasource instanceof JdbcDatasource)
        {
            ((JdbcDatasource)datasource).writeTo(element);
        }
        else if (datasource instanceof JndiDatasource)
        {
            ((JndiDatasource)datasource).writeTo(element);
        }
        else
        {
            throw new IllegalArgumentException("No datasource specified!");
        }
    }



    private Datasource parseDatasource(Element element)
    {
        Element datasourceElement = element.element(JdbcDatasource.JDBC_DATASOURCE);
        if (datasourceElement != null)
        {
            return JdbcDatasource.parse(datasourceElement);
        }
        datasourceElement = element.element(JndiDatasource.JNDI_DATASOURCE);
        if (datasourceElement != null)
        {
            return JndiDatasource.parse(datasourceElement);
        }
        throw new IllegalArgumentException("No datasource specified!");
    }

    
    private String parseOptionalString(Element element, String key, String defaultValue)
    {
        final Element optional = element.element(key);
        return (optional != null) ? element.elementText(key) : defaultValue;
    }
}
