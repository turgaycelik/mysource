
package com.atlassian.jira.action.admin.export;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.database.Datasource;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.BuildUtilsInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class TestDefaultSaxEntitiesExporter
{
    private static final String MYSQL = "mysql";
    private static final String POSTGRES = "postgres72";
    private static final String UNKNOWN = "l33+ h4x0r !!!!11!1!1!1";

    @Mock OfBizDelegator delegator;
    @Mock ApplicationProperties applicationProperties;
    @Mock BuildUtilsInfo buildUtilsInfo;
    @Mock DatabaseConfigurationManager databaseConfigurationManager;
    @Mock Datasource datasource;
    @Mock ComponentAccessor.Worker worker;

    DatabaseConfig databaseConfig;
    DefaultSaxEntitiesExporter exporter;

    @Before
    public void initializeWorker()
    {
        ComponentAccessor.initialiseWorker(worker);
        when(worker.getComponent(DatabaseConfigurationManager.class)).thenReturn(databaseConfigurationManager);
    }
    @Test
    public void testDefaultFetchSizeIsIntegerMinValueOnMySql()
    {
        assertFetchSize(Integer.MIN_VALUE, MYSQL, null);
    }

    @Test
    public void testDefaultFetchSizeIsSameAsPublicConstantOnPostgres()
    {
        assertFetchSize(DefaultSaxEntitiesExporter.DEFAULT_FETCH_SIZE, POSTGRES, null);
    }

    @Test
    public void testDefaultFetchSizeIsSameAsPublicConstantOnUnknown()
    {
        assertFetchSize(DefaultSaxEntitiesExporter.DEFAULT_FETCH_SIZE, UNKNOWN, null);
    }

    @Test
    public void testMinusOneUsesDefaultOnMySql()
    {
        assertFetchSize(Integer.MIN_VALUE, MYSQL, "-1");
    }

    @Test
    public void testMinusOneUsesDefaultOnPostgres()
    {
        assertFetchSize(DefaultSaxEntitiesExporter.DEFAULT_FETCH_SIZE, POSTGRES, "-1");
    }

    @Test
    public void testMinusOneUsesDefaultOnUnknown()
    {
        assertFetchSize(DefaultSaxEntitiesExporter.DEFAULT_FETCH_SIZE, UNKNOWN, "-1");
    }

    @Test
    public void testSettingOverridesDefaultForMySql()
    {
        assertFetchSize(42, MYSQL, "42");
    }

    @Test
    public void testSettingOverridesDefaultForPostgres()
    {
        assertFetchSize(42, POSTGRES, "42");
    }

    @Test
    public void testSettingOverridesDefaultForUnknown()
    {
        assertFetchSize(42, UNKNOWN, "42");
    }

    private void assertFetchSize(final int expectedFetchSize, final String databaseType, final String propertyValue)
    {
        databaseConfig = new DatabaseConfig(databaseType, "schema", datasource);
        when(databaseConfigurationManager.getDatabaseConfiguration()).thenReturn(databaseConfig);
        when(applicationProperties.getDefaultBackedString(APKeys.Export.FETCH_SIZE)).thenReturn(propertyValue);
        exporter = new DefaultSaxEntitiesExporter(delegator, applicationProperties, buildUtilsInfo);

        assertEquals(expectedFetchSize, exporter.getFindOptions().getFetchSize());
    }
}
