package com.atlassian.jira.startup;

import javax.servlet.ServletContext;

import com.atlassian.instrumentation.operations.OpTimerFactory;
import com.atlassian.jdk.utilities.runtimeinformation.RuntimeInformationFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.index.ha.DisasterRecoveryLauncher;
import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.upgrade.ConsistencyCheckImpl;
import com.atlassian.jira.upgrade.PluginSystemLauncher;
import com.atlassian.jira.util.devspeed.JiraDevSpeedTimer;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.threadlocal.BruteForceThreadLocalCleanup;
import com.atlassian.threadlocal.RegisteredThreadLocals;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * This implementation of JiraLauncher contains all of the smarts of what to start in which order to ensure that JIRA
 * starts properly.
 *
 * @since v4.3
 */
public class DefaultJiraLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(DefaultJiraLauncher.class);

    private final ChecklistLauncher startupChecklist;
    private final BootstrapContainerLauncher bootstrapContainerLauncher;
    private final ComponentContainerLauncher componentContainerLauncher;
    private final NotificationInstanceKiller notificationInstanceKiller;
    private final DatabaseLauncher databaseLauncher;
    private final PluginSystemLauncher pluginSystemLauncher;
    private final ConsistencyCheckImpl consistencyChecker;
    private final SystemInfoLauncher systemInfoLauncher;
    private final FailedPluginsLauncher preDbFailedPluginsLauncher;
    private final FailedPluginsLauncher postDbFailedPluginsLauncher;
    private final ClusteringLauncher clusteringLauncher;
    private final ClusteringChecklistLauncher clusteringChecklistLauncher;
    private final ActiveServicesLauncher activeServicesLauncher;
    private final JiraProperties jiraSystemProperties;
    private final TenancyLauncher tenancyLauncher;
    private final DisasterRecoveryLauncher disasterRecoveryLauncher;

    public DefaultJiraLauncher()
    {
        this.jiraSystemProperties = JiraSystemProperties.getInstance();
        this.startupChecklist = new ChecklistLauncher(jiraSystemProperties);
        this.bootstrapContainerLauncher = new BootstrapContainerLauncher();
        this.componentContainerLauncher = new ComponentContainerLauncher();
        this.notificationInstanceKiller = new NotificationInstanceKiller();
        this.databaseLauncher = new DatabaseLauncher(jiraSystemProperties);
        this.pluginSystemLauncher = new PluginSystemLauncher();
        this.consistencyChecker = new ConsistencyCheckImpl();
        this.systemInfoLauncher = new SystemInfoLauncher();
        this.preDbFailedPluginsLauncher = new FailedPluginsLauncher();
        this.postDbFailedPluginsLauncher = new FailedPluginsLauncher();
        this.clusteringLauncher = new ClusteringLauncher();
        this.clusteringChecklistLauncher = new ClusteringChecklistLauncher();
        this.activeServicesLauncher = new ActiveServicesLauncher();
        this.tenancyLauncher = new TenancyLauncher();
        this.disasterRecoveryLauncher = new DisasterRecoveryLauncher();
    }

    @Override
    public void start()
    {
        JiraDevSpeedTimer.run(getStartupName(), new Runnable()
        {
            public void run()
            {
                preDbLaunch();
                postDbLaunch();
            }
        });
        DefaultIndexManager.flushThreadLocalSearchers();  // JRA-29587
    }

    /**
     * Things that can or must be done before the database is configured.
     */
    private void preDbLaunch()
    {
        systemInfoLauncher.start();
        bootstrapContainerLauncher.start();
        //JRADEV-17174: We must have a bootstrap container before we do the startup checks
        startupChecklist.start();
        preDbFailedPluginsLauncher.start();
    }

    /**
     * Those things that need the database to have been configured.
     */
    private void postDbLaunch()
    {
        if (JiraStartupChecklist.startupOK())
        {
            final DatabaseConfigurationManager dbcm = ComponentAccessor.getComponentOfType(DatabaseConfigurationManager.class);
            final ServletContext servletContext = ServletContextProvider.getServletContext();

            dbcm.doNowOrWhenDatabaseConfigured(new Runnable()
            {
                @Override
                public void run()
                {
                    new DatabaseChecklistLauncher(dbcm, servletContext, jiraSystemProperties).start();
                }
            }, "Database Checklist Launcher");

            dbcm.doNowOrWhenDatabaseActivated(new Runnable()
            {
                @Override
                public void run()
                {
                    //JRADEV-20303 If the build number fails get out of Dodge
                    if (!JohnsonEventContainer.get(servletContext).hasEvents())
                    {
                        componentContainerLauncher.start(); //start pico
                        databaseLauncher.start();
                        // Let components know there is a tenant by sending an event.
                        // Note: this is temporary, so we can start updating core services to become tenant aware without breaking
                        // things. Ultimately, this event will no be send here, but when an instance actually gets assigned to a tenant.
                        tenancyLauncher.start();
                        disasterRecoveryLauncher.earlyStart();
                        clusteringChecklistLauncher.start();
                        clusteringLauncher.start();
                        pluginSystemLauncher.start(); //start Plugin System
                        consistencyChecker.initialise(ServletContextProvider.getServletContext());
                        activeServicesLauncher.start();
                        notificationInstanceKiller.deleteAfterDelay();
                        disasterRecoveryLauncher.start();
                    }
                }

            }, "Post database-configuration launchers");

            postDbFailedPluginsLauncher.start();
        }
    }

    @Override
    public void stop()
    {
        log.info("Stopping launchers");

        // We moved the clustering launcher here so the other node receives the signal immediately
        // and the other nodes don't fail while this node is shutting down.
        clusteringLauncher.stop();
        activeServicesLauncher.stop();
        consistencyChecker.destroy(ServletContextProvider.getServletContext());
        tenancyLauncher.stop();
        pluginSystemLauncher.stop();
        databaseLauncher.stop();
        startupChecklist.stop();
        //after next command PICO dies so we have to cache interesting components
        OpTimerFactory opTimerFactory = ComponentAccessor.getComponent(OpTimerFactory.class);
        componentContainerLauncher.stop();
        bootstrapContainerLauncher.stop();
        systemInfoLauncher.stop();
        preDbFailedPluginsLauncher.stop();
        postDbFailedPluginsLauncher.stop();

        cleanupAfterOurselves(opTimerFactory);
    }

    private void cleanupAfterOurselves(OpTimerFactory opTimerFactory)
    {
        cleanupThreadLocals(opTimerFactory);

        //
        // and shutdown lo4j for good measure
        LogManager.shutdown();
    }

    private void cleanupThreadLocals(OpTimerFactory opTimerFactory)
    {//
        // we do instrument on the main servlet context thread so we need to clean it up
        //
        Instrumentation.snapshotThreadLocalOperationsAndClear(opTimerFactory);

        //
        // clean up any TL that have done the right thing and registered themselves
        RegisteredThreadLocals.reset();
        //
        // now brute force any other thread locals for this class loader
        //
        BruteForceThreadLocalCleanup.cleanUp(getClass().getClassLoader());
    }

    private String getStartupName()
    {
        final String jvmInputArguments = StringUtils.defaultString(RuntimeInformationFactory.getRuntimeInformation().getJvmInputArguments());
        final boolean rebel = jvmInputArguments.contains("jrebel.jar");
        final boolean debugMode = jvmInputArguments.contains("-Xdebug");

        return "jira.startup" + (debugMode ? ".debug" : ".run") + (rebel ? ".jrebel" : "");
    }
}
