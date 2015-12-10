package com.atlassian.jira.cluster;

import com.atlassian.jira.index.ha.NodeReindexService;

/**
 * Start and stop clustered services
 *
 * @since v6.1
 */
public interface ClusterServicesManager
{
    void startServices();

    void stopServices();
}
