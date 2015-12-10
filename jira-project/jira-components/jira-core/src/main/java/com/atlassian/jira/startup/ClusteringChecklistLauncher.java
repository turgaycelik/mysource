package com.atlassian.jira.startup;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.appconsistency.clustering.ClusterLicenseCheck;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.ClusterNodeProperties;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.license.LicenseJohnsonEventRaiser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;

import java.util.List;
import javax.servlet.ServletContext;

import static com.atlassian.johnson.event.EventLevel.ERROR;

/**
 * Launcher for the {@link com.atlassian.jira.startup.JiraClusteringConfigChecklist}.
 *
 * @since v6.1
 */
public class ClusteringChecklistLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(ClusteringChecklistLauncher.class);
    private final ServletContext servletContext;


    public ClusteringChecklistLauncher()
    {
        this.servletContext = ServletContextProvider.getServletContext();
    }

    @VisibleForTesting
    ClusteringChecklistLauncher(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    @Override
    public void start()
    {
        final ClusterManager clusterManager = ComponentAccessor.getComponentOfType(ClusterManager.class);
        final ClusterNodeProperties clusterNodeProperties = ComponentAccessor.getComponentOfType(ClusterNodeProperties.class);
        final I18nHelper i18nHelper = ComponentAccessor.getI18nHelperFactory().getInstance((User)null);
        final JiraHome jiraHome = ComponentAccessor.getComponentOfType(JiraHome.class);
        JiraClusteringConfigChecklist jiraClusteringConfigChecklist =
                new JiraClusteringConfigChecklist(clusterManager, clusterNodeProperties, i18nHelper, jiraHome);
        if (jiraClusteringConfigChecklist.startupOK())
        {
            log.info("JIRA clustering startup checks completed successfully.");
        }
        else
        {
            final List<StartupCheck> failedChecks = jiraClusteringConfigChecklist.getFailedStartupChecks();
            for (StartupCheck failedCheck : failedChecks)
            {
                raiseJohnson(failedCheck);
            }
        }
    }

    private void raiseJohnson(final StartupCheck failedCheck)
    {
        String desc = failedCheck.getFaultDescription();
        log.fatal(failedCheck.getName() + " failed: " + desc);
        log.fatal("Clustering startup check failed.");
        final EventType eventType;
        if (failedCheck instanceof ClusterLicenseCheck)
        {
            eventType = EventType.get(LicenseJohnsonEventRaiser.CLUSTERING_UNLICENSED);
        }
        else
        {
            eventType = EventType.get(LicenseJohnsonEventRaiser.CLUSTERING);
        }
        Event event = new Event(eventType, failedCheck.getHTMLFaultDescription(), EventLevel.get(ERROR));
        JohnsonEventContainer.get(servletContext).addEvent(event);
    }

    @Override
    public void stop()
    {
    }
}
