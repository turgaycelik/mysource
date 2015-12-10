package com.atlassian.jira.diagnostic;

import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.web.monitor.dump.Dumper;
import com.atlassian.jira.web.monitor.dump.HeapDumper;
import com.atlassian.plugin.IllegalPluginStateException;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginContainerFailedEvent;
import com.atlassian.plugin.osgi.event.PluginServiceDependencyWaitTimedOutEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Introduced in JRADEV-14163 to help diagnose what is going on in the system when a plugin fails to start up during XML
 * Restore.
 *
 * @since v5.2
 */
public class PluginDiagnostics
{
    private static final Logger log = LoggerFactory.getLogger(PluginDiagnostics.class);

    public static final String HEAP_DUMP_FILE = "jira.heap.dump.location";

    public PluginDiagnostics(final PluginEventManager pluginEventManager, final JiraProperties jiraSystemProperties)
    {
        if (jiraSystemProperties.isDevMode() || jiraSystemProperties.getBoolean("jira.dump"))
        {
            log.warn("Registering PluginDiagnostics with the event manager.");
            pluginEventManager.register(new DiagnosticListener(jiraSystemProperties));
        }

    }

    public static class DiagnosticListener
    {
        private final AtomicBoolean dumped = new AtomicBoolean();
        private final JiraProperties jiraSystemProperties;

        public DiagnosticListener(final JiraProperties jiraSystemProperties)
        {

            this.jiraSystemProperties = jiraSystemProperties;
        }

        @PluginEventListener
        public void onSpringContextFailed(final PluginContainerFailedEvent event) throws IllegalPluginStateException
        {
            // dump all threads to stdout
            dump(String.format("Plugin %s failed", event.getPluginKey()));
        }


        @PluginEventListener
        public void onDependencyTimedOut(final PluginServiceDependencyWaitTimedOutEvent timedOutEvent)
        {
            dump(String.format("Service dependency in plugin %s on %s timed out after %d",
                    timedOutEvent.getPluginKey(),
                    timedOutEvent.getBeanName(),
                    timedOutEvent.getElapsedTime()));
        }

        private void dump(final String reason)
        {
            if (dumped.compareAndSet(false, true))
            {
                log.warn("Creating a thread and heap dump because {}", reason);
                new Dumper().dumpThreads(null);
                HeapDumper.dumpHeap(jiraSystemProperties.getProperty(HEAP_DUMP_FILE, "jira-heap-dump.hprof"), true);
            }
        }

    }


}
