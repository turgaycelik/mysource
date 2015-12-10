package com.atlassian.jira.appconsistency.db;

import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.database.JndiDatasource;
import com.atlassian.jira.config.database.ManagedDatasourceInfoSupplier;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link PublicSchemaConfigCheck}.
 *
 * @since v4.4
 */
public class TestPublicSchemaConfigCheck
{
    @Test
    public void isOkNonHsql()
    {
        final DatabaseConfig databaseConfig = new DatabaseConfig("fooDb", "skimar", new JndiDatasource("jndi;yo"));
        final DatabaseConfigurationManager mockDcm = EasyMock.createStrictMock(DatabaseConfigurationManager.class);
        EasyMock.expect(mockDcm.getDatabaseConfiguration()).andReturn(databaseConfig);
        EasyMock.replay(mockDcm);

        PublicSchemaConfigCheck pscc = new PublicSchemaConfigCheck(new ManagedDatasourceInfoSupplier(mockDcm));
        Assert.assertTrue(pscc.isOk());
        EasyMock.verify(mockDcm);
    }

    @Test
    public void isOkHsql()
    {
        final DatabaseConfig databaseConfig = new DatabaseConfig("hsql", "PUBLIC", new JndiDatasource("jndi;yo"));
        final DatabaseConfigurationManager mockDcm = EasyMock.createStrictMock(DatabaseConfigurationManager.class);
        EasyMock.expect(mockDcm.getDatabaseConfiguration()).andReturn(databaseConfig);
        EasyMock.replay(mockDcm);

        PublicSchemaConfigCheck pscc = new PublicSchemaConfigCheck(new ManagedDatasourceInfoSupplier(mockDcm));
        Assert.assertTrue(pscc.isOk());
        EasyMock.verify(mockDcm);
    }

    @Test
    public void isNotOkHsql()
    {
        final DatabaseConfig databaseConfig = new DatabaseConfig("bogusql", "PUBLIC", new JndiDatasource("jndi;yo"));
        final DatabaseConfigurationManager mockDcm = EasyMock.createStrictMock(DatabaseConfigurationManager.class);
        EasyMock.expect(mockDcm.getDatabaseConfiguration()).andReturn(databaseConfig);
        EasyMock.replay(mockDcm);

        PublicSchemaConfigCheck pscc = new PublicSchemaConfigCheck(new ManagedDatasourceInfoSupplier(mockDcm));
        Assert.assertFalse(pscc.isOk());
        EasyMock.verify(mockDcm);
    }


}
