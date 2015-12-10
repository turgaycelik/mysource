package com.atlassian.jira.studio.startup;

import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Tries to find a {@link com.atlassian.jira.studio.startup.StudioStartupHooks} using a {@link ServiceLoader}.
 *
 * @since v4.4.1
 */
class ServiceLocator implements Locator
{
    private static final Logger log = Logger.getLogger(ServiceLocator.class);

    @Override
    public StudioStartupHooks locate(@Nonnull ClassLoader loader)
    {
        try
        {
            ServiceLoader<StudioStartupHooks> locator = ServiceLoader.load(StudioStartupHooks.class, loader);
            Iterator<StudioStartupHooks> iter = locator.iterator();
            if (iter.hasNext())
            {
                StudioStartupHooks factory = iter.next();
                if (iter.hasNext())
                {
                    log.warn("Found more than one StudioStartupHooks. Using '" + factory.getClass().getName() + "'.");
                }
                return factory;
            }
        }
        catch (ServiceConfigurationError e)
        {
            log.warn("Error occured while looking for a StudioStartupHooks.", e);
        }
        return null;
    }
}
