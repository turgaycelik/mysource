package com.atlassian.jira.appconsistency.db;

import com.atlassian.jira.config.database.SystemDatabaseConfigurationLoader;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.startup.StartupCheck;
import com.google.common.base.Supplier;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.config.DatasourceInfo;

/**
 * A DatabaseCheck that looks in the entityengine.xml for an anticipated
 * misconfiguration, the combination of setting schema-name to PUBLIC
 * (upper case) on a database other than HSQL.
 */
public class PublicSchemaConfigCheck implements StartupCheck
{
    private static final Logger log = Logger.getLogger(PublicSchemaConfigCheck.class);

    private static final String FIELD_TYPE_NAME_HSQL = "hsql";
    private static final String SCHEMA_PUBLIC = "PUBLIC";

    private static final String KEY_PUBLIC_SCHEMA_CONFIG_DISABLED = "atlassian.jira.dbcheck.publicschemaconfig.disabled";
    private static final String URL_DOCS = "http://www.atlassian.com/software/jira/docs/latest/databases/index.html";
    private Supplier<DatasourceInfo> datasourceInfoSupplier;

    /**
     * Uses the given databaseConfigurationManager to acquire a reference to the {@link DatasourceInfo} to use to
     * perform this configuration check.
     *
     * @param datasourceInfoSupplier to get the DatasourceInfo.
     */
    public PublicSchemaConfigCheck(Supplier<DatasourceInfo> datasourceInfoSupplier)
    {
        this.datasourceInfoSupplier = datasourceInfoSupplier;
    }

    public String getName()
    {
        return "Schema and field-type-name config in entityengine.xml";
    }

    /**
     * Returns false if the schema is PUBLIC (all caps) and the field type ain't HSQL. Returns false otherwise. This
     * check can also be disabled by setting the {@link #KEY_PUBLIC_SCHEMA_CONFIG_DISABLED} property to "true".
     * If disabled, it'll always return true.
     * @return the result of the check
     */
    public boolean isOk()
    {
        if (Boolean.valueOf(JiraSystemProperties.getInstance().getProperty(KEY_PUBLIC_SCHEMA_CONFIG_DISABLED)))
        {
            log.info("Disabling public schema config check");
        }
        else
        {
            DatasourceInfo dsi = datasourceInfoSupplier.get();
            if (dsi != null)
            {
                String fieldTypeName = dsi.getFieldTypeName();
                String schemaName = dsi.getSchemaName();
                if (SCHEMA_PUBLIC.equals(schemaName))
                {
                    // hsql is the only database that should have a schema of "PUBLIC" (in uppercase)
                    return FIELD_TYPE_NAME_HSQL.equals(fieldTypeName);
                }
            }
            else
            {
                log.error("Could not read the datasource info!");
            }
        }
        return true;
    }

    public String getFaultDescription()
    {
        return "You have an error in your " + SystemDatabaseConfigurationLoader.FILENAME_DBCONFIG + " file.\n" +
                "The schema-name=\"PUBLIC\" only works for a field-type-name of \"hsql\".\n" +
                "Please refer to the JIRA database documentation at the following URL: " + URL_DOCS;
    }

    public String getHTMLFaultDescription()
    {
        return "<p>You have an error in your " + SystemDatabaseConfigurationLoader.FILENAME_DBCONFIG + " file. " +
                "The schema-name=\"PUBLIC\" only works for a field-type-name of \"hsql\". " +
                "Please refer to the <a href=\"" + URL_DOCS + "\">JIRA database documentation</a>.</p>";
    }

    @Override
    public void stop()
    {
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
