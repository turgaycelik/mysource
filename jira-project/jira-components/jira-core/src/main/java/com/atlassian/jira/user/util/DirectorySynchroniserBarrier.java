package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.api.ApplicationFactory;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.directory.RemoteDirectorySynchronisedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.google.common.annotations.VisibleForTesting;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Class that tries to wait for any "Crowd Embedded" synchronisations to finish. This class is *not* thread safe, only
 * one thread may await on this class.
 *
 * NOTE: This is a complete hack. It does not work in all cases. It would be better if we could tell "Crowd Embedded"
 * to shutdownAndWait be we can't currently so this is here to catch a majority of cases where this is a problem.
 *
 * @since v5.0
 */
@NotThreadSafe
public class DirectorySynchroniserBarrier
{
    private final ApplicationFactory applicationFactory;
    private final CrowdDirectoryService crowdDirectoryService;
    private final EventPublisher publisher;
    private final long pollingPeriod;

    @SuppressWarnings ({ "MismatchedQueryAndUpdateOfCollection" })
    private final BlockingQueue<Long> queue = new LinkedBlockingQueue<Long>();

    public DirectorySynchroniserBarrier(ApplicationFactory applicationFactory,
            CrowdDirectoryService crowdDirectoryService, EventPublisher publisher)
    {
        this(applicationFactory, crowdDirectoryService, publisher, SECONDS.toMillis(1));
    }

    @VisibleForTesting
    DirectorySynchroniserBarrier(ApplicationFactory applicationFactory, CrowdDirectoryService crowdDirectoryService,
            EventPublisher publisher, long pollingPeriod)
    {
        this.applicationFactory = applicationFactory;
        this.crowdDirectoryService = crowdDirectoryService;
        this.publisher = publisher;
        this.pollingPeriod = pollingPeriod;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="RV_RETURN_VALUE_IGNORED",
            justification="We don't care about return value from queue.poll(), queue is used as a barrier")
    public boolean await(long timeout, TimeUnit unit)
    {
        if (applicationFactory.getApplication() == null)
        {
            //We are probably in setup. Anyways, with no application Crowd Embedded will not work so we assume
            //there is no synchronization currently occuring.
            return true;
        }

        long current = System.currentTimeMillis();
        final long deadLine = System.currentTimeMillis() + unit.toMillis(timeout);
        publisher.register(this);
        try
        {
            boolean synchroniseRunning = isSynchroniseRunning();
            while (synchroniseRunning && current < deadLine)
            {
                queue.poll(Math.min(pollingPeriod, deadLine - current), TimeUnit.MILLISECONDS);

                synchroniseRunning = isSynchroniseRunning();
                current = System.currentTimeMillis();
            }
            return !synchroniseRunning;
        }
        catch (InterruptedException e)
        {
            return !isSynchroniseRunning();
        }
        finally
        {
            publisher.unregister(this);
        }
    }

    @EventListener
    public void onDirectoryFinished(RemoteDirectorySynchronisedEvent finished)
    {
        try
        {
            queue.put(finished.getRemoteDirectory().getDirectoryId());
        }
        catch (InterruptedException e)
        {
            //Let the polling catch the change.
        }
    }

    private boolean isSynchroniseRunning()
    {
        for (Directory directory : crowdDirectoryService.findAllDirectories())
        {
            if (directory.isActive() && crowdDirectoryService.isDirectorySynchronising(directory.getId()))
            {
                return true;
            }
        }
        return false;
    }
}
