package com.atlassian.jira.mail;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ComponentManagerShutdownEvent;
import com.atlassian.jira.event.ComponentManagerStartedEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.util.concurrent.ThreadFactories;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.util.concurrent.ThreadFactories.Type.DAEMON;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Launcher for the Botocss cleanup thread.
 *
 * @since 6.2
 */
public class BotocssThreadLauncher implements Startable
{
    private final EventPublisher eventPublisher;
    private final AtomicReference<ScheduledExecutorService> executor = new AtomicReference<ScheduledExecutorService>();

    public BotocssThreadLauncher(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    /**
     * Schedules the cache cleanup task.
     */
    @EventListener
    public void  onComponentManagerStarted(final ComponentManagerStartedEvent startedEvent)
    {
        ScheduledExecutorService e = Executors.newSingleThreadScheduledExecutor(ThreadFactories
                .named("jira-botocss")
                .type(DAEMON)
                .build()
        );

        long cleanupSecs = BotoCssInliner.EXPIRE_SECS / 2;
        e.scheduleWithFixedDelay(new CacheCleaner(), cleanupSecs, cleanupSecs, SECONDS);
        executor.set(e);
    }

    @EventListener
    public void  onComponentManagerShutdown(final ComponentManagerShutdownEvent shutdownEvent)
    {
        ScheduledExecutorService e = executor.getAndSet(null);
        if (e != null) {
            e.shutdown();
        }
    }

    /**
     * Runnable that calls {@link com.google.common.cache.Cache#cleanUp()}.
     */
    private class CacheCleaner implements Runnable
    {
        @Override
        public void run()
        {
            BotoCssInliner botocssInliner = ComponentAccessor.getComponent(BotoCssInliner.class);
            if (botocssInliner != null)
            {
                botocssInliner.performCacheMaintenance();
            }
        }
    }
}
