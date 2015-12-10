package com.atlassian.jira.util;

import org.apache.log4j.Logger;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.0
 */
public class CompositeShutdown implements Shutdown
{
    private static final Logger LOGGER = Logger.getLogger(Shutdown.class);

    private final Iterable<Shutdown> list;

    public CompositeShutdown(final Shutdown... shutdowns)
    {
        list = unmodifiableList(asList(shutdowns));
    }

    public void shutdown()
    {
        for (final Shutdown shutdown : list)
        {
            if (shutdown != null)
            {
                try
                {
                    shutdown.shutdown();
                }
                catch (final RuntimeException e)
                {
                    LOGGER.error("Cannot execute shutdown for: " + shutdown, e);
                }
            }
        }
    }
}
