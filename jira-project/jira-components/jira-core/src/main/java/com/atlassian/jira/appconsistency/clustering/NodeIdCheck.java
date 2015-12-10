package com.atlassian.jira.appconsistency.clustering;

import com.atlassian.jira.cluster.ClusterNodeProperties;
import com.atlassian.jira.startup.StartupCheck;
import com.atlassian.jira.util.I18nHelper;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Startup check that verifies the node id is set in cluster.properties
 *
 * @since v6.1
 */
public class NodeIdCheck implements StartupCheck
{
    private static final Logger log = Logger.getLogger(NodeIdCheck.class);

    static final String NAME = "JIRA Cluster Node ID Check";
    static final String FAULT_DESC = "startup.node.id.check";

    private final ClusterNodeProperties clusterNodeProperties;
    private final I18nHelper i18nHelper;

    public NodeIdCheck(ClusterNodeProperties clusterNodeProperties, final I18nHelper i18nHelper)
    {
        this.clusterNodeProperties = clusterNodeProperties;
        this.i18nHelper = i18nHelper;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public boolean isOk()
    {
        log.debug("Performing JIRA Cluster Node ID Check");
        return StringUtils.isNotBlank(clusterNodeProperties.getNodeId());
    }

    @Override
    public String getFaultDescription()
    {
        return i18nHelper.getText(FAULT_DESC);
    }

    @Override
    public String getHTMLFaultDescription()
    {
        return getFaultDescription();
    }

    @Override
    public void stop()
    {
    }
}
