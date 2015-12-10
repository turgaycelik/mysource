package com.atlassian.jira.cluster;

import javax.annotation.Nullable;

/**
 * This is used to lookup cluster properties from the underlying properties file.
 *
 * @since v6.1
 */
public interface ClusterNodeProperties
{
    /**
     *
     * @param key key for the property you want to look up
     * @return String value of the property, null if it does not exist
     */

    @Nullable
    String getProperty(String key);

    /**
     * Get the shared home for a clustered or HA installation.
     * Will return null if no shared home is set.
     * @return
     */
    String getSharedHome();

    /**
     * Get the node id for a clustered or HA installation.
     * Will return null if no node id is set.
     * @return
     */
    String getNodeId();

    /**
     *  Called to reload the cluster properties.
     */
    void refresh();

    /**
     * The cluster.properties file is optional.  If it exists then JIRA is assumed to be in a cluster
     * @return
     */
    boolean propertyFileExists();

    /**
     * whether the underlying cluster.properties file is valid or not
     * @return
     */
    boolean isValid();

}
