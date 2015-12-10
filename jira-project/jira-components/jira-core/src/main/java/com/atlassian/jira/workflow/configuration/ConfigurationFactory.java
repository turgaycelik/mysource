package com.atlassian.jira.workflow.configuration;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.InfrastructureException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.google.common.base.Supplier;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.config.Configuration;
import com.opensymphony.workflow.config.DefaultConfiguration;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.config.EntityConfigUtil;

/**
 * Creates an instance of the OSWorkflow {@link com.opensymphony.workflow.config.Configuration Configuration}
 * to be used by JIRA.
 *
 * @since v5.1
 */
public class ConfigurationFactory implements Supplier<Configuration>
{
    private Logger log = Logger.getLogger(ConfigurationFactory.class);

    @Override
    public Configuration get()
    {
        try
        {
            final Configuration configuration = new DefaultConfiguration();
            configuration.load(ClassLoaderUtils.getResource("osworkflow.xml", getClass()));
            // This is here because there is a concurrency bug in osworkflow such that the configuration
            // does not safely initialize its GenericDelegator. If we do not "prime" the reference to the
            // delegator then you can run into issues where you get a null pointer when concurrently trying
            // to create issues. DO NOT REMOVE THIS BLOCK OF CODE!!
            try
            {
                configuration.getWorkflowStore();
                return configuration;
            }
            catch (StoreException e)
            {
                throw new DataAccessException(e);
            }
        }
        catch (FactoryException e)
        {
            log.error("Error loading OSWorkflow Configuration: " + e, e);
            throw new InfrastructureException("Error loading osworkflow.xml file: " + e, e);
        }
    }
}
