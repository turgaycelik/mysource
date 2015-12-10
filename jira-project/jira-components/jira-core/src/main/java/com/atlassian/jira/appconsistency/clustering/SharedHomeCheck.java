package com.atlassian.jira.appconsistency.clustering;

import java.io.File;
import java.io.IOException;

import com.atlassian.jira.cluster.ClusterNodeProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.startup.StartupCheck;
import com.atlassian.jira.util.I18nHelper;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Startup check that verifies the shared home is set correctly in cluster.properties
 *
 * @since v6.1
 */
public class SharedHomeCheck implements StartupCheck
{
    private static final Logger LOG = Logger.getLogger(SharedHomeCheck.class);

    static final String NAME = "JIRA Cluster Shared Home Check";

    private final ClusterNodeProperties clusterNodeProperties;
    private final JiraHome jiraHome;
    private final I18nHelper i18nHelper;

    private String faultDescription;

    public SharedHomeCheck(final ClusterNodeProperties clusterNodeProperties, final I18nHelper i18nHelper, final JiraHome jiraHome)
    {
        this.clusterNodeProperties = clusterNodeProperties;
        this.i18nHelper = i18nHelper;
        this.jiraHome = jiraHome;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public boolean isOk()
    {
        LOG.debug("Performing JIRA Cluster Shared Home Check");

        if (StringUtils.isBlank(clusterNodeProperties.getSharedHome()))
        {
            faultDescription = i18nHelper.getText("startup.shared.home.check.missing");
            return false;
        }

        File localHome = jiraHome.getLocalHome();
        File sharedHome = jiraHome.getHome();

        //Canonicalize the directories in case the user has set up symlinks
        try
        {
            localHome = localHome.getCanonicalFile();
            sharedHome = sharedHome.getCanonicalFile();
        }
        catch (IOException e)
        {
            LOG.error("I/O error canonicalizing home directory: " + e, e);
        }

        if (localHome.equals(sharedHome))
        {
            LOG.error("Shared home is the same as local home (" + localHome.getPath() + ")");
            faultDescription = i18nHelper.getText("startup.shared.home.check.sameaslocal", jiraHome.getLocalHomePath());
            return false;
        }

        return true;
    }

    @Override
    public String getFaultDescription()
    {
        return faultDescription;
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
