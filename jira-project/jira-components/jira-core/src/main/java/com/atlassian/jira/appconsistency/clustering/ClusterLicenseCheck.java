package com.atlassian.jira.appconsistency.clustering;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.startup.StartupCheck;
import com.atlassian.jira.util.I18nHelper;

import org.apache.log4j.Logger;

/**
 * @since v6.3
 */
public class ClusterLicenseCheck implements StartupCheck
{
    private static final Logger LOG = Logger.getLogger(ClusterLicenseCheck.class);

    static final String NAME = "JIRA Cluster License Check";
    static final String FAULT_DESC = "startup.cluster.license.check";

    private final ClusterManager clusterManager;
    private final I18nHelper i18nHelper;

    public ClusterLicenseCheck(final ClusterManager clusterManager, final I18nHelper i18nHelper)
    {
        this.clusterManager = clusterManager;
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
        LOG.debug("Performing JIRA Cluster License Check");
        return clusterManager.isClusterLicensed();
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
