package com.atlassian.jira.ofbiz;

import java.sql.Connection;
import java.sql.SQLException;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.util.concurrent.ResettableLazyReference;

import org.ofbiz.core.entity.ConnectionFactory;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.config.EntityConfigUtil;

/**
 * Copyright All Rights Reserved.
 * Created: christo 12/10/2006 15:50:59
 */
public class DefaultOfBizConnectionFactory implements OfBizConnectionFactory
{
    private final EntityConfigUtil entityConfigUtil = EntityConfigUtil.getInstance();

    // Note: Calling ComponentAccessor from inside a lazy reference is deadlock-prone, but should be okay
    // here because the DatabaseConfig is unlikely to ever circle its dependencies back to this connection factory.
    @ClusterSafe
    private final ResettableLazyReference<DatabaseConfig> databaseConfigurationRef = new ResettableLazyReference<DatabaseConfig>()
    {
        @Override
        protected DatabaseConfig create() throws Exception
        {
            return ComponentAccessor.getComponent(DatabaseConfigurationManager.class).getDatabaseConfiguration();
        }
    };

    private static final class InstanceHolder
    {
        private static final DefaultOfBizConnectionFactory INSTANCE = new DefaultOfBizConnectionFactory();
    }

    public static DefaultOfBizConnectionFactory getInstance()
    {
        return InstanceHolder.INSTANCE;
    }

    public Connection getConnection() throws SQLException, DataAccessException
    {
        try
        {
            return ConnectionFactory.getConnection(getDatabaseConfig().getDatasourceName());
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public DatasourceInfo getDatasourceInfo()
    {
        return entityConfigUtil.getDatasourceInfo(getDatabaseConfig().getDatasourceName());
    }

    @Override
    public String getDelegatorName()
    {
        return getDatabaseConfig().getDelegatorName();
    }

    public void resetInstance()
    {
        databaseConfigurationRef.reset();
    }

    private DatabaseConfig getDatabaseConfig()
    {
        return databaseConfigurationRef.get();
    }
}
