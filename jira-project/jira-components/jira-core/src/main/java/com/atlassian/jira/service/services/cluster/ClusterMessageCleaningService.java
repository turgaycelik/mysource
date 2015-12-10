package com.atlassian.jira.service.services.cluster;

import java.util.Date;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.cluster.OfBizClusterMessageStore;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.service.AbstractService;
import com.atlassian.jira.util.JiraDurationUtils;

import com.opensymphony.module.propertyset.PropertySet;

import org.apache.log4j.Logger;

/**
 * Service for flushing the cluster message table
 *
 * @since v6.3
 */
public class ClusterMessageCleaningService extends AbstractService
{
    private static final Logger LOGGER = Logger.getLogger(ClusterMessageCleaningService.class);
    public static final String RETENTION_PERIOD = "RETENTION_PERIOD";

    private final OfBizClusterMessageStore ofBizClusterMessageStore;
    private final JiraDurationUtils jiraDurationUtils;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    //retention window in seconds
    private long retentionPeriod;

    public ClusterMessageCleaningService(OfBizClusterMessageStore ofBizClusterMessageStore, final JiraDurationUtils jiraDurationUtils, JiraAuthenticationContext jiraAuthenticationContext) {
        this.ofBizClusterMessageStore = ofBizClusterMessageStore;
        this.jiraDurationUtils = jiraDurationUtils;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public void init(PropertySet props) throws ObjectConfigurationException
    {
        super.init(props);

        if (hasProperty(RETENTION_PERIOD))
        {
            String retention = getProperty(RETENTION_PERIOD);
            try
            {
                retentionPeriod = jiraDurationUtils.parseDuration(retention, jiraAuthenticationContext.getLocale());
            }
            catch (InvalidDurationException e)
            {
                LOGGER.error("Invalid Duration specified in service configuration", e);
            }
        }
    }

    @Override
    public void run()
    {
        final Date deleteBeforeTime = new Date(new Date().getTime() - (retentionPeriod * 1000));
        ofBizClusterMessageStore.deleteMessagesBefore(deleteBeforeTime);
    }

    @Override
    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("INDEXCLEANERSERVICE", "services/com/atlassian/jira/service/services/index/indexcleanerservice.xml", null);
    }

    public long getRetentionPeriod()
    {
        return retentionPeriod;
    }

}
