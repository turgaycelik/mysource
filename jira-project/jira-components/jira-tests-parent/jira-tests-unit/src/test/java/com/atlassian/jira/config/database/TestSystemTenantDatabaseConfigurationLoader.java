package com.atlassian.jira.config.database;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Assert;
import org.junit.Test;
import org.ofbiz.core.entity.config.JdbcDatasourceInfo;

/**
 * Unit test for {@link SystemDatabaseConfigurationLoader}.
 *
 * @since v4.4
 */
public class TestSystemTenantDatabaseConfigurationLoader
{
    @Test
    public void loadAndSave()
    {
        final String configString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<jira-database-config>\n"
                + "  <name>Tomato Sauce</name>\n"
                + "  <delegator-name>bzzt</delegator-name>\n"
                + "  <database-type>zoinkdb</database-type>\n"
                + "  <schema-name>schematic</schema-name>\n"
                + "  <jdbc-datasource>\n"
                + "    <url>jdbc:hsqldb:/Users/chris/jirahome/database/jiradb</url>\n"
                + "    <driver-class>java.lang.Void</driver-class>\n"
                + "    <username>turing</username>\n"
                + "    <password>secrecy</password>\n"
                + "    <pool-min-size>3</pool-min-size>\n"
                + "    <pool-max-size>99</pool-max-size>\n"
                + "    <pool-max-idle>59</pool-max-idle>\n"
                + "  </jdbc-datasource>\n"
                + "</jira-database-config>\n";
        final StringReader reader = new StringReader(configString);
        final StringWriter writer = new StringWriter();
        SystemDatabaseConfigurationLoader stdcl = new SystemDatabaseConfigurationLoader(null)
        {
            @Override
            Reader getReader() throws FileNotFoundException
            {
                return reader;
            }

            @Override
            Writer getWriter() throws IOException
            {
                return writer;
            }
        };
        final DatabaseConfig databaseConfig = stdcl.loadDatabaseConfiguration();
        Assert.assertEquals("Tomato Sauce", databaseConfig.getDatasourceName());
        Assert.assertEquals("bzzt", databaseConfig.getDelegatorName());
        Assert.assertEquals("zoinkdb", databaseConfig.getDatabaseType());
        final JdbcDatasourceInfo jdbcDatasource = databaseConfig.getDatasourceInfo().getJdbcDatasource();
        Assert.assertEquals("java.lang.Void", jdbcDatasource.getDriverClassName());
        Assert.assertEquals("turing", jdbcDatasource.getUsername());
        Assert.assertEquals("secrecy", jdbcDatasource.getPassword());
        Assert.assertEquals("jdbc:hsqldb:/Users/chris/jirahome/database/jiradb", jdbcDatasource.getUri());
        Assert.assertEquals(3, jdbcDatasource.getConnectionPoolInfo().getMinSize());
        Assert.assertEquals(99, jdbcDatasource.getConnectionPoolInfo().getMaxSize());
        stdcl.saveDatabaseConfiguration(databaseConfig);
        Assert.assertEquals(configString, writer.toString());
    }

    @Test
    public void loadAndSaveNoDelegator()
    {
        final String configString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<jira-database-config>\n"
                + "  <name>Tomato Sauce</name>\n"
                + "  <database-type>zoinkdb</database-type>\n"
                + "  <schema-name>schematic</schema-name>\n"
                + "  <jdbc-datasource>\n"
                + "    <url>jdbc:hsqldb:/Users/chris/jirahome/database/jiradb</url>\n"
                + "    <driver-class>java.lang.Void</driver-class>\n"
                + "    <username>turing</username>\n"
                + "    <password>secrecy</password>\n"
                + "    <pool-min-size>3</pool-min-size>\n"
                + "    <pool-max-size>99</pool-max-size>\n"
                + "    <pool-max-idle>59</pool-max-idle>\n"
                + "  </jdbc-datasource>\n"
                + "</jira-database-config>\n";
        final StringReader reader = new StringReader(configString);
        final StringWriter writer = new StringWriter();
        SystemDatabaseConfigurationLoader stdcl = new SystemDatabaseConfigurationLoader(null)
        {
            @Override
            Reader getReader() throws FileNotFoundException
            {
                return reader;
            }

            @Override
            Writer getWriter() throws IOException
            {
                return writer;
            }
        };
        final DatabaseConfig databaseConfig = stdcl.loadDatabaseConfiguration();
        Assert.assertEquals("Tomato Sauce", databaseConfig.getDatasourceName());
        Assert.assertEquals("Tomato Sauce", databaseConfig.getDelegatorName());
        Assert.assertEquals("zoinkdb", databaseConfig.getDatabaseType());
        final JdbcDatasourceInfo jdbcDatasource = databaseConfig.getDatasourceInfo().getJdbcDatasource();
        Assert.assertEquals("java.lang.Void", jdbcDatasource.getDriverClassName());
        Assert.assertEquals("turing", jdbcDatasource.getUsername());
        Assert.assertEquals("secrecy", jdbcDatasource.getPassword());
        Assert.assertEquals("jdbc:hsqldb:/Users/chris/jirahome/database/jiradb", jdbcDatasource.getUri());
        Assert.assertEquals(3, jdbcDatasource.getConnectionPoolInfo().getMinSize());
        Assert.assertEquals(99, jdbcDatasource.getConnectionPoolInfo().getMaxSize());
        stdcl.saveDatabaseConfiguration(databaseConfig);
        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<jira-database-config>\n"
                + "  <name>Tomato Sauce</name>\n"
                + "  <delegator-name>Tomato Sauce</delegator-name>\n"
                + "  <database-type>zoinkdb</database-type>\n"
                + "  <schema-name>schematic</schema-name>\n"
                + "  <jdbc-datasource>\n"
                + "    <url>jdbc:hsqldb:/Users/chris/jirahome/database/jiradb</url>\n"
                + "    <driver-class>java.lang.Void</driver-class>\n"
                + "    <username>turing</username>\n"
                + "    <password>secrecy</password>\n"
                + "    <pool-min-size>3</pool-min-size>\n"
                + "    <pool-max-size>99</pool-max-size>\n"
                + "    <pool-max-idle>59</pool-max-idle>\n"
                + "  </jdbc-datasource>\n"
                + "</jira-database-config>\n";
        Assert.assertEquals(expected, writer.toString());
    }
}
