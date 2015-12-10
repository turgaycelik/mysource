package com.atlassian.jira.studio;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.plugin.studio.StudioHooks;
import com.atlassian.jira.plugin.studio.StudioLicenseHooks;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.util.concurrent.ResettableLazyReference;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * An implementation of {@link StudioHooks} that delegates its all calls to a plugin that implements the
 * {@link StudioHooks} interface. All calls are delegated to {@link VanillaStudioHooks} such a plugin
 * does not exist.
 *
 * @since v4.4.2
 */
public class PluginStudioHooks implements StudioHooks
{
    private final Cache cache;

    public PluginStudioHooks(PluginAccessor accessor, EventPublisher publisher)
    {
        cache = new Cache(accessor);
        publisher.register(cache);
    }

    @Nonnull
    @Override
    public StudioLicenseHooks getLicenseHooks()
    {
        return cache.get().getLicenseHooks();
    }

    //Needs to be public so that @EventListener methods can be called. Wouldn't be a problem if we used
    //interfaces :-)
    public static class Cache
    {
        private final PluginAccessor accessor;

        @ClusterSafe("Refreshing handled via plugin events.")
        private final ResettableLazyReference<StudioHooks> ref = new ResettableLazyReference<StudioHooks>()
        {
            @Override
            protected StudioHooks create() throws Exception
            {
                final Iterator<StudioHooks> hooksIterator = accessor.getEnabledModulesByClass(StudioHooks.class).iterator();
                if (!hooksIterator.hasNext())
                {
                    return VanillaStudioHooks.getInstance();
                }
                else
                {
                    StudioHooks hooks = hooksIterator.next();
                    if (!hooksIterator.hasNext())
                    {
                        return hooks;
                    }
                    else
                    {
                        //We currently don't support more than one hooks implementation. We could probably implement a
                        //CompositeStudioHooks but there is little point for this hack.
                        throw new IllegalStateException("Found more than one 'StudioHooks' implementation. We currently only support 0..1.");
                    }
                }
            }
        };

        public Cache(PluginAccessor accessor)
        {
            this.accessor = accessor;
        }

        @SuppressWarnings ( { "UnusedDeclaration" })
        @EventListener
        public void onModuleEnabled(PluginModuleEnabledEvent event)
        {
            if (isEventRelevant(event.getModule()))
            {
                ref.reset();
            }
        }

        @SuppressWarnings ( { "UnusedDeclaration" })
        @EventListener
        public void onModuleDisabled(PluginModuleDisabledEvent event)
        {
            if (isEventRelevant(event.getModule()))
            {
                ref.reset();
            }
        }

        @SuppressWarnings ( { "UnusedParameters" })
        @EventListener
        public void clearCache(ClearCacheEvent event)
        {
            ref.reset();
        }

        StudioHooks get()
        {
            try
            {
                return ref.get();
            }
            catch (LazyReference.InitializationException e)
            {
                if (e.getCause() instanceof RuntimeException)
                {
                    throw (RuntimeException)e.getCause();
                }
                else
                {
                    throw e;
                }
            }
        }

        private boolean isEventRelevant(ModuleDescriptor<?> module)
        {
            Class<?> moduleClass = module.getModuleClass();
            return moduleClass != null && StudioHooks.class.isAssignableFrom(moduleClass);
        }
    }
}
