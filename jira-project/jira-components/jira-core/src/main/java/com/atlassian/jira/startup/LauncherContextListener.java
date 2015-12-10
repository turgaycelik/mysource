package com.atlassian.jira.startup;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentAccessorWorker;
import com.atlassian.jira.studio.startup.StudioStartupHooks;
import com.atlassian.jira.studio.startup.StudioStartupHooksLocator;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static com.atlassian.jira.util.concurrent.ThreadFactories.namedThreadFactory;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * This class is the entry point for JIRA.  It takes care of initialising the application through the use of various
 * {@link com.atlassian.jira.startup.JiraLauncher}s.  Additionally this class will also launch a deadlock detector
 * thread to check for deadlocks during startup.
 *
 * @since v4.3
 */
public class LauncherContextListener implements ServletContextListener
{
    private static final Logger log = LoggerFactory.getLogger(LauncherContextListener.class);

    private static final String STARTUP_UNEXPECTED = "startup-unexpected";
    private static final String LOG4J = "log4j.properties";
    private static final int DEADLOCK_DETECTION_PERIOD = 5;

    private JiraLauncher launcher;
    private final ScheduledExecutorService deadlockDetectionService =
            newSingleThreadScheduledExecutor(namedThreadFactory("DeadlockDetection"));

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        log.debug("Startup deadlock detector launched...");
        final ScheduledFuture<?> deadLockDetector =
                deadlockDetectionService.scheduleAtFixedRate(new DeadlockDetector(), 0, DEADLOCK_DETECTION_PERIOD, TimeUnit.SECONDS);
        try
        {
            log.debug("Launching JIRA");

            StudioStartupHooks startupHooks = StudioStartupHooksLocator.getStudioStartupHooks();
            configureLog4j(startupHooks);
            startupHooks.beforeJiraStart();
            initialiseJiraApi();

            launcher = new DefaultJiraLauncher();
            launcher.start();
            startupHooks.afterJiraStart();
        }
        catch (Exception e)
        {
            log.error("Unable to start JIRA.", e);
            JohnsonEventContainer.get(sce.getServletContext()).addEvent(new Event(
                    EventType.get(STARTUP_UNEXPECTED), "Unexpected exception during JIRA startup. This JIRA instance "
                    + "will not be able to recover. Please check the logs for details",
                    EventLevel.get("fatal")));
        }
        catch (Error e)
        {
            log.error("Unable to start JIRA due to Java Error.", e);
            throw e;
        }
        finally
        {
            deadLockDetector.cancel(false);
            deadlockDetectionService.shutdown();
            log.debug("Startup deadlock detector finished.");
        }
    }

    private void initialiseJiraApi()
    {
        log.debug("Initing Jira");
        // Initialise the ComponentAccessor with a Worker - this insulates the API from the implementation classes.
        ComponentAccessor.initialiseWorker(new ComponentAccessorWorker());
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        if (launcher == null)
        {
            throw new IllegalStateException("Context destroyed without being initialized first. JIRA launcher is confused.");
        }
        else
        {
            launcher.stop();
            launcher = null;
        }
    }

    private void configureLog4j(StudioStartupHooks startupHooks)
    {
        Properties properties = new Properties();
        InputStream resource = this.getClass().getClassLoader().getResourceAsStream(LOG4J);
        if (resource != null)
        {
            try
            {
                properties.load(resource);
            }
            catch (IOException e)
            {
                log.warn("Unable read current log4j configuration. Assuming blank configuration.", e);
            }
            finally
            {
                IOUtils.closeQuietly(resource);
            }
        }
        else
        {
            log.warn("Unable to find '" + LOG4J + "' on class path.");
        }

        Properties newConfig = startupHooks.getLog4jConfiguration(properties);
        if (newConfig != null)
        {
            PropertyConfigurator.configure(newConfig);
        }
    }

    private static class DeadlockDetector implements Runnable
    {
        private static final String DEAD_LOCK_DETECTOR_KB_URL =
                "https://confluence.atlassian.com/display/JIRAKB/Deadlock+detected+on+startup+error+in+logfile";

        private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        @Override
        public void run()
        {
            long[] threadIds = threadMXBean.findDeadlockedThreads();
            if (threadIds != null)
            {
                final List<String> threadInfoStrings = newArrayList();
                for (ThreadInfo threadInfo : threadMXBean.getThreadInfo(threadIds, 0))
                {
                    threadInfoStrings.add(trim(threadInfo.toString()));
                }
                log.error(format("A deadlock has been detected on JIRA startup for the following threads: %s", threadInfoStrings));

                for (ThreadInfo threadInfo : threadMXBean.getThreadInfo(threadIds, MAX_VALUE))
                {
                    log.error(generateStackTrace(threadInfo));
                }
                log.error(format("Further troubleshooting information about this issue is available in the KB article at: %s", DEAD_LOCK_DETECTOR_KB_URL));

                throw new DeadlockDetectedException();  //stops this thread from being executed by the ScheduledExecutorService
            }
        }

        private String generateStackTrace(final ThreadInfo threadInfo)
        {
            final StringBuilder stackTraceString = new StringBuilder();
            stackTraceString.append(trim(threadInfo.toString())).append(":\n");
            final StackTraceElement[] stackTrace = threadInfo.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace)
            {
                stackTraceString.append("\t").append(stackTraceElement.toString()).append("\n");
            }
            return stackTraceString.toString();
        }
    }

    private final static class DeadlockDetectedException extends RuntimeException
    {
    }
}
