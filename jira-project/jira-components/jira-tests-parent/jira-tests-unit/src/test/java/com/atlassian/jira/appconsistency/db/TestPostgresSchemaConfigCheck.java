package com.atlassian.jira.appconsistency.db;

import com.atlassian.jira.web.util.ExternalLinkUtil;

import com.google.common.base.Suppliers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.config.DatasourceInfo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestPostgresSchemaConfigCheck
{
    @Mock private DatasourceInfo mockDatasourceInfo;
    @Mock private ExternalLinkUtil mockExternalLinkUtil;

    @Test
    public void testLogMessage() throws Exception
    {
        when(mockExternalLinkUtil.getProperty("external.link.jira.doc.postgres.db.config")).thenReturn("blah");
        when(mockDatasourceInfo.getFieldTypeName()).thenReturn("postgres");
        when(mockDatasourceInfo.getSchemaName()).thenReturn("Test");

        final PostgresSchemaConfigCheck configCheck = new PostgresSchemaConfigCheck(null, mockExternalLinkUtil)
        {
            DatasourceInfo getDatasourceInfo()
            {
                return mockDatasourceInfo;
            }
        };

        assertTrue(configCheck.isOk());
        assertTrue(configCheck.isLoggedError());
    }

    @Test
    public void testLogMessagePostgres72() throws Exception
    {
        when(mockExternalLinkUtil.getProperty("external.link.jira.doc.postgres.db.config")).thenReturn("blah");
        when(mockDatasourceInfo.getFieldTypeName()).thenReturn("postgres72");
        when(mockDatasourceInfo.getSchemaName()).thenReturn("Test");

        final PostgresSchemaConfigCheck configCheck = new PostgresSchemaConfigCheck(null, mockExternalLinkUtil)
        {
            DatasourceInfo getDatasourceInfo()
            {
                return mockDatasourceInfo;
            }
        };

        assertTrue(configCheck.isOk());
        assertTrue(configCheck.isLoggedError());
    }

    @Test
    public void testHappyPath() throws Exception
    {
        when(mockDatasourceInfo.getFieldTypeName()).thenReturn("postgres");
        when(mockDatasourceInfo.getSchemaName()).thenReturn("test");

        final PostgresSchemaConfigCheck configCheck =
                new PostgresSchemaConfigCheck(Suppliers.ofInstance(mockDatasourceInfo), mockExternalLinkUtil);

        assertTrue(configCheck.isOk());
        assertFalse(configCheck.isLoggedError());
    }

    @Test
    public void testLogMessageNoDatasource() throws Exception
    {
        final PostgresSchemaConfigCheck configCheck = new PostgresSchemaConfigCheck(null, mockExternalLinkUtil)
        {
            DatasourceInfo getDatasourceInfo()
            {
                return null;
            }
        };

        assertTrue(configCheck.isOk());
        assertTrue(configCheck.isLoggedError());
    }
}

