package com.atlassian.jira.util.system;

import javax.servlet.ServletContext;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.NodeStateManager;
import com.atlassian.jira.cluster.NotClusteredException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.upgrade.UpgradeLauncher;
import com.atlassian.scheduler.SchedulerRuntimeException;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.core.LifecycleAwareSchedulerService;

import org.apache.log4j.Logger;

import static com.atlassian.jira.component.ComponentAccessor.getMailQueue;

/**
 * Since this class causes PICO to commit suicide, it is very careful to be completely stateless and not keep references
 * to any things inside PICO itself.
 *
 * @since v4.0
 */
public class JiraSystemRestarterImpl implements JiraSystemRestarter
{
    private static final Logger log = Logger.getLogger(JiraSystemRestarterImpl.class);
    private static final String JIRA_IS_ABOUT_TO_BE_INTERNALLY_RESTARTED = "JIRA is about to be internally restarted";
    private static final String JIRA_HAS_BEEN_INTERNALLY_RESTARTED = "JIRA has been internally restarted";

    public void ariseSirJIRA()
    {
        log.info(JIRA_IS_ABOUT_TO_BE_INTERNALLY_RESTARTED);

        stopServicesInOtherThreads();

        restartPico();

        startServicesInOtherThreads();

        quiescePassiveNode();

        log.info(JIRA_HAS_BEEN_INTERNALLY_RESTARTED);
    }

    public void ariseSirJIRAandUpgradeThySelf(ServletContext servletContext)
    {
        log.info(JIRA_IS_ABOUT_TO_BE_INTERNALLY_RESTARTED);

        stopServicesInOtherThreads();

        restartPico();

        UpgradeLauncher.checkIfUpgradeNeeded(servletContext);

        startServicesInOtherThreads();

        quiescePassiveNode();

        log.info(JIRA_HAS_BEEN_INTERNALLY_RESTARTED);
    }

    @SuppressWarnings("deprecation")
    private void restartPico()
    {
        // Reinitialise all the manager objects
        log.info("Restarting JIRA code components...");
        ManagerFactory.globalRefresh();
        log.info("JIRA code components started");
    }

    private void stopServicesInOtherThreads()
    {
        // Stop the scheduler
        log.info("Stopping the JIRA Scheduler...");
        getSchedulerService().shutdown();
        log.info("JIRA Scheduler Stopped");

        log.info("Emptying the JIRA Mail Queue...");
        try
        {
            getMailQueue().sendBuffer();
            log.info("JIRA Mail Queue emptied");
        }
        catch (final Exception e)
        {
            log.warn("Failed to empty the Mail Queue: " + e.getMessage(), e);
        }
    }

    private void startServicesInOtherThreads()
    {
        // Start the scheduler, if the node is inactive.
        ClusterManager clusterManager = ComponentAccessor.getComponent(ClusterManager.class);
        if (clusterManager.isActive())
        {
            log.info("Restarting the JIRA Scheduler...");
            try
            {
                getSchedulerService().start();
            }
            catch (SchedulerServiceException e)
            {
                throw new SchedulerRuntimeException("Unable to restart the JIRA scheduler", e);
            }
            log.info("JIRA Scheduler started");
        }
    }

    private void quiescePassiveNode()
    {
        // If this node is in a cluster and passive, then we should make all is quiet
        final ClusterManager clusterManager = ComponentAccessor.getComponent(ClusterManager.class);
        if (!clusterManager.isActive())
        {
            final NodeStateManager nodeStateManager = ComponentAccessor.getComponent(NodeStateManager.class);
            try
            {
                nodeStateManager.deactivate();
            }
            catch (final NotClusteredException e)
            {
                // No need to deactivate
            }
        }
    }

    private static LifecycleAwareSchedulerService getSchedulerService()
    {
        return ComponentAccessor.getComponent(LifecycleAwareSchedulerService.class);
    }
}
