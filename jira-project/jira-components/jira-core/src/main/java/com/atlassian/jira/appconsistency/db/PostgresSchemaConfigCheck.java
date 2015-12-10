package com.atlassian.jira.appconsistency.db;

import com.atlassian.jira.startup.StartupCheck;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.google.common.base.Supplier;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.config.DatasourceInfo;

import java.util.Locale;

import static com.atlassian.jira.config.database.SystemDatabaseConfigurationLoader.FILENAME_DBCONFIG;

/**
 * A checker that will print a log message if the database is Postgres and the schema name contains upper case.
 * <p/>
 * see JRA-16780.
 *
 * @since v4.0
 */
public class PostgresSchemaConfigCheck implements StartupCheck
{
    private static final Logger log = Logger.getLogger(PostgresSchemaConfigCheck.class);

    private static final String FIELD_TYPE_NAME_POSTGRES72 = "postgres72";
    private static final String FIELD_TYPE_NAME_POSTGRES = "postgres";
    private boolean loggedError = false;
    private final ExternalLinkUtil externalLinkUtil;
    private Supplier<DatasourceInfo> datasourceInfoSupplier;

    public PostgresSchemaConfigCheck(Supplier<DatasourceInfo> datasourceInfoSupplier, ExternalLinkUtil externalLinkUtil)
    {
        this.datasourceInfoSupplier = datasourceInfoSupplier;
        this.externalLinkUtil = externalLinkUtil;
    }

    public String getName()
    {
        return "Postgres upper case schema name check.";
    }

    /**
     * Always returns true and will log a message if the database is POSTGRES and the configured schema name contains
     * upper case characters.
     *
     * @return true
     */
    public boolean isOk()
    {

        DatasourceInfo dsi = getDatasourceInfo();
        if (dsi != null)
        {
            String fieldTypeName = dsi.getFieldTypeName();

            if (FIELD_TYPE_NAME_POSTGRES.equals(fieldTypeName) || FIELD_TYPE_NAME_POSTGRES72.equals(fieldTypeName))
            {
                String schemaName = dsi.getSchemaName();
                if (!isSchemaNameValid(schemaName))
                {
                    log.error("The schema name '" + schemaName + "' in your " + FILENAME_DBCONFIG
                            + " file contains upper case characters and JIRA only supports lower case schemas in POSTGRES.");
                    log.error("JIRA will work as long as the real schema name in the Postgres database is really lower case. ");
                    log.error("Please refer to the JIRA database documentation at the following URL: " + externalLinkUtil.getProperty("external.link.jira.doc.postgres.db.config"));
                    this.loggedError = true;
                }
            }
        }
        else
        {
            log.error("Could not read the datasource info!");
            this.loggedError = true;
        }
        return true;
    }

    DatasourceInfo getDatasourceInfo()
    {
        return datasourceInfoSupplier.get();
    }

    // NOTE: only for testing purposes
    boolean isLoggedError()
    {
        return loggedError;
    }

    public void setLoggedError(final boolean loggedError)
    {
        this.loggedError = loggedError;
    }

    private boolean isSchemaNameValid(final String schemaName)
    {
        if (schemaName == null)
        {
            return false;
        }
        final String lowerCaseSchemaName = schemaName.toLowerCase(Locale.ENGLISH);
        return lowerCaseSchemaName.equals(schemaName);
    }

    // NOTE: Never called since this StartUp checker just produces a log message and does not lock up JIRA.
    public String getFaultDescription()
    {
        return "You have an error in your " + FILENAME_DBCONFIG + " file.\n" +
                "The schema-name contains upper case letters and JIRA only supports lower case SCHEMAs in POSTGRES.\n" +
                "Please refer to the JIRA database documentation at the following URL: " + externalLinkUtil.getProperty("external.link.jira.doc.postgres.db.config");
    }

    // NOTE: Never called since this StartUp checker just produces a log message and does not lock up JIRA.
    public String getHTMLFaultDescription()
    {
        return "<p>You have an error in your " + FILENAME_DBCONFIG + " file. " +
                "The schema-name=\"PUBLIC\" only works for a field-type-name of \"hsql\". " +
                "Please refer to the <a href=\"" + externalLinkUtil.getProperty("external.link.jira.doc.postgres.db.config") + "\">JIRA database documentation</a>.</p>";
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
