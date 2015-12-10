package com.atlassian.jira.config.database;

import com.atlassian.config.bootstrap.AtlassianBootstrapManager;
import com.atlassian.config.bootstrap.BootstrapException;
import com.atlassian.jira.util.dbc.Assertions;
import org.dom4j.Element;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.config.JndiDatasourceInfo;

import java.sql.Connection;

/**
 * A JNDI datasource
 */
public final class JndiDatasource implements Datasource
{
    static final String JNDI_DATASOURCE = "jndi-datasource";
    static final String JNDI_NAME = "jndi-name";

    private final String jndiName;

    public JndiDatasource(String jndiName)
    {
        this.jndiName = Assertions.notBlank("JNDI name", jndiName);
    }

    public JndiDatasource(JndiDatasourceInfo jndi)
    {
        this(Assertions.notNull("JNDI info", jndi).getJndiName());
    }

    /**
     * Get the JNDI name
     *
     * @return The JNDI name
     */
    public String getJndiName()
    {
        return jndiName;
    }

    @Override
    public Connection getConnection(AtlassianBootstrapManager bootstrapManager) throws BootstrapException
    {
        return bootstrapManager.getTestDatasourceConnection(jndiName);
    }

    @Override
    public DatasourceInfo getDatasource(String datasourceName, String databaseType, String schemaName)
    {
        JndiDatasourceInfo jndiInfo = new JndiDatasourceInfo(jndiName, "default");
        return new DatasourceInfo(datasourceName, databaseType, schemaName, jndiInfo);
    }

    @Override
    public String getDescriptorValue(String databaseType)
    {
        return databaseType + " " + jndiName;
    }

    @Override
    public String getDescriptorLabel()
    {
        return "Database JNDI config";
    }

    static JndiDatasource parse(Element datasourceElement)
    {
        String jndiName = datasourceElement.elementText(JNDI_NAME);
        return new JndiDatasource(jndiName);
    }
    
    void writeTo(Element element)
    {
        final Element jndi = element.addElement(JNDI_DATASOURCE);
        jndi.addElement(JNDI_NAME).setText(getJndiName());
    }
}

