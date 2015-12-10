package com.atlassian.velocity;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.ResourceCacheImpl;
import org.apache.velocity.runtime.resource.ResourceManagerImpl;

import static com.atlassian.jira.component.ComponentAccessor.getComponent;
import static java.lang.String.format;

/**
 * Listens for plugin events, and clears the velocity resource cache accordingly.
 * <p>
 * The resource cache contains the velocity templates that have already been rendered by the underlying
 * {@link org.apache.velocity.app.VelocityEngine}.
 * </p>
 */
public class PluginAwareResourceManager extends ResourceManagerImpl
{
    /**
     * Registers this class with the {@link EventPublisher}, and delegates to the super class initialize method.
     *
     * @param rs RuntimeServices instance
     * @throws Exception
     */
    @Override
    public void initialize(final RuntimeServices rs) throws Exception
    {
        super.initialize(rs);
        getEventPublisher().register(this);
    }

    EventPublisher getEventPublisher()
    {
        return getComponent(EventPublisher.class);
    }

    /**
     * Clears the velocity resource cache when a plugin is disabled.
     *
     * @param event The event instance fired when a plugin is disabled.
     */
    @EventListener
    public void clearCacheOnDisable(final PluginDisabledEvent event)
    {
        if (globalCache instanceof Cache)
        {
            ((Cache) globalCache).clear();
        }
        else
        {
            log.error
                    (
                            format
                                    (
                                            "Unable to clear the velocity resource cache as it is not an instance of: %s\n "
                                                    + "Any changes to velocity resources in the plugin %s will not be "
                                                    + "loaded until JIRA is restarted", Cache.class, event.getPlugin()
                                    )
                    );
        }

    }

    public static class Cache extends ResourceCacheImpl
    {
        public void clear()
        {
            cache.clear();
        }
    }
}