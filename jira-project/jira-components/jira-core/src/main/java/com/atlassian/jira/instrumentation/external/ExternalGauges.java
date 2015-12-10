package com.atlassian.jira.instrumentation.external;

import com.atlassian.jira.config.database.DatabaseConfigurationLoader;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

/**
 * This collects all he other external gauges and creates them
 *
 * @since v4.4
 */
public class ExternalGauges
{
    private final DatabaseConfigurationLoader databaseConfigurationLoader;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public ExternalGauges(VelocityRequestContextFactory velocityRequestContextFactory, DatabaseConfigurationLoader databaseConfigurationLoader)
    {
        this.databaseConfigurationLoader = databaseConfigurationLoader;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    public ExternalGauges installInstruments()
    {
        new ClassLoadingExternalGauges();
        new MemoryExternalGauges();
        new DomainObjectsExternalGauges();
        new ThreadExternalGauges();
        new GarbageCollectionsExternalOp();
        new DatabaseExternalGauges(velocityRequestContextFactory, databaseConfigurationLoader).installInstruments();

        return this;
    }
}
