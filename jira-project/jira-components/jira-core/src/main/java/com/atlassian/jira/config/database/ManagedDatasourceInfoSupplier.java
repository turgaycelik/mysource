package com.atlassian.jira.config.database;

import com.google.common.base.Supplier;
import org.ofbiz.core.entity.config.DatasourceInfo;

/**
 * Implements a minimal method of acquiring access to the OfBiz {@link DatasourceInfo} that uses the {@link
 * DatabaseConfigurationManager}'s DatabaseConfig.
 *
 * @since v4.4
 */
public class ManagedDatasourceInfoSupplier implements Supplier<DatasourceInfo>
{
    private final DatabaseConfigurationManager databaseConfigurationManager;

    public ManagedDatasourceInfoSupplier(final DatabaseConfigurationManager databaseConfigurationManager)
    {
        this.databaseConfigurationManager = databaseConfigurationManager;
    }

    @Override
    public DatasourceInfo get()
    {
        return databaseConfigurationManager.getDatabaseConfiguration().getDatasourceInfo();
    }
}
