package com.atlassian.jira.cluster;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nullable;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.util.concurrent.ResettableLazyReference;

import com.google.common.collect.Maps;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Responsible for loading the cluster properties from file, if it exists
 *
 * @since v6.1
 */
@EventComponent
public class ClusterNodePropertiesImpl implements ClusterNodeProperties
{
    private static final Logger log = Logger.getLogger(ClusterNodePropertiesImpl.class);

    public static final String JIRA_CLUSTER_CONFIG_PROPERTIES = "cluster.properties";
    public static final String JIRA_SHARED_HOME = "jira.shared.home";
    public static final String JIRA_NODE_ID = "jira.node.id";

    private final File overlayFile;
    private final JiraHome jiraHome;

    public ClusterNodePropertiesImpl(JiraHome jiraHome)
    {
        this.jiraHome = jiraHome;
        overlayFile = new File(jiraHome.getLocalHomePath(), JIRA_CLUSTER_CONFIG_PROPERTIES);
    }

    @Nullable
    public String getProperty(String property)
    {
        return clusterPropertiesRef.get().get(property);
    }

    @Override
    public String getSharedHome()
    {
        return getProperty(JIRA_SHARED_HOME);
    }

    @Override
    public String getNodeId()
    {
        return getProperty(JIRA_NODE_ID);
    }

    public void refresh()
    {
        clusterPropertiesRef.reset();
    }

    @Override
    public boolean propertyFileExists()
    {
        return overlayFile.exists();
    }

    @Override
    public boolean isValid()
    {
        return (propertyFileExists() && allRequiredPropertiesExist());
    }

    private boolean allRequiredPropertiesExist()
    {
        Map<String, String> properties = clusterPropertiesRef.get();
        // We require JIRA_SHARED_HOME && JIRA_NODE_ID
        return isNotBlank(properties.get(JIRA_SHARED_HOME)) && isNotBlank(properties.get(JIRA_NODE_ID));
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    @ClusterSafe
    /** This is a reference to the holder for the local cluster.properties */
    private final ResettableLazyReference<Map<String, String>> clusterPropertiesRef = new ResettableLazyReference<Map<String, String>>()
    {
        protected Map<String, String> create() throws Exception
        {
            // We want to turn the Properties object into an immutable HashMap
            return Maps.fromProperties(loadProperties());
        }

        private Properties loadProperties()
        {
            Properties properties = new Properties();
            InputStream in = null;
            try
            {
                if (overlayFile.exists())
                {
                    in = new FileInputStream(overlayFile);
                    properties.load(in);
                }
            }
            catch (final IOException e)
            {
                log.warn("Could not load config properties from '" + overlayFile + "'.");
            }
            finally
            {
                IOUtils.closeQuietly(in);
            }

            return properties;
        }
    };
}
