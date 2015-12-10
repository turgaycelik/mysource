package com.atlassian.jira.startup;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.appconsistency.clustering.ClusterLicenseCheck;
import com.atlassian.jira.appconsistency.clustering.NodeIdCheck;
import com.atlassian.jira.appconsistency.clustering.SharedHomeCheck;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.ClusterNodeProperties;
import com.atlassian.jira.config.util.JiraHome;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.Lists;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Represents the set of sanity checks that must be done as soon as clustering config is known.
 *
 * @since v6.1
 */
public class JiraClusteringConfigChecklist
{
    private static final Logger log = Logger.getLogger(JiraClusteringConfigChecklist.class);
    private final JiraStartupLogger startupLogger = new JiraStartupLogger();
    private final ClusterNodeProperties clusterNodeProperties;
    private final StartupCheck[] checklist;
    private volatile boolean checksDone;
    private volatile boolean success;
    private List<StartupCheck> failedStartupChecks = Lists.newArrayList();

    public JiraClusteringConfigChecklist(final ClusterManager clusterManager,
            final ClusterNodeProperties clusterNodeProperties, final I18nHelper i18nHelper,
            final JiraHome jiraHome)
    {
        this.clusterNodeProperties = clusterNodeProperties;
        this.checklist = new StartupCheck[] {
            new SharedHomeCheck(clusterNodeProperties, i18nHelper, jiraHome),
            new NodeIdCheck(clusterNodeProperties, i18nHelper),
            new ClusterLicenseCheck(clusterManager, i18nHelper)
        };
    }

    public boolean startupOK()
    {
        // only do this once, and only if there is an ha file
        if (!checksDone)
        {
            success = true;
            if (clusterNodeProperties.propertyFileExists())
            {
                log.debug("Performing Clustering start up checks");
                success = doStartupChecks();
                checksDone = true;
            }
        }
        return success;
    }

    private boolean doStartupChecks()
    {
        boolean success = true;
        log.debug("Doing clustering config checklist");
        for (final StartupCheck startupCheck : checklist)
        {
            log.debug("Doing startup check " + startupCheck.getName());
            if (!startupCheck.isOk())
            {
                // Log the Checker's fault message
                startupLogger.printMessage(startupCheck.getFaultDescription(), Level.FATAL);
                failedStartupChecks.add(startupCheck);
                success = false;
            }
        }
        // All checks passed
        return success;
    }

    /**
     * @return the List of{@link com.atlassian.jira.startup.StartupCheck} that failed, if any or an empty list if none.
     */
    public List<StartupCheck> getFailedStartupChecks()
    {
        return failedStartupChecks;
    }
}
