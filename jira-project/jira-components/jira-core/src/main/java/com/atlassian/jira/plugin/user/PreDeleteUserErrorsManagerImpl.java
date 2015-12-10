package com.atlassian.jira.plugin.user;

import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.apache.log4j.Logger;


/**
 * @since 6.0
 */
public class PreDeleteUserErrorsManagerImpl implements PreDeleteUserErrorsManager
{
    private final PluginModuleTracker<PreDeleteUserErrors, PreDeleteUserErrorsModuleDescriptor> deleteUserPluginTracker;
    private static final Logger log = Logger.getLogger(PreDeleteUserErrorsManagerImpl.class);

    public PreDeleteUserErrorsManagerImpl(final PluginAccessor pluginAccessor, final PluginEventManager pluginEventManager)
    {
        deleteUserPluginTracker = createTracker(pluginAccessor, pluginEventManager);
    }

    @VisibleForTesting
    PluginModuleTracker<PreDeleteUserErrors, PreDeleteUserErrorsModuleDescriptor> createTracker(PluginAccessor pluginAccessor, PluginEventManager pluginEventManager)
    {
        return DefaultPluginModuleTracker.create(pluginAccessor, pluginEventManager, PreDeleteUserErrorsModuleDescriptor.class);
    }

    @VisibleForTesting
    Iterable<PreDeleteUserErrors> enabledModules()
    {
        return Iterables.filter(deleteUserPluginTracker.getModules(), new Predicate<PreDeleteUserErrors>()
        {
            @Override
            public boolean apply(@Nullable PreDeleteUserErrors input)
            {
                return input != null;
            }
        });
    }


    @Override
    public ImmutableList<WebErrorMessage> getWarnings(final User user)
    {
        final ImmutableList.Builder<WebErrorMessage> result = ImmutableList.builder();
        for (final PreDeleteUserErrors module : enabledModules())
        {
            try
            {
                final List<WebErrorMessage> moduleErrors = module.getPreDeleteUserErrors(user);
                if (moduleErrors != null)
                {
                    result.addAll(moduleErrors);
                }
            }
            catch (RuntimeException e)
            {
                log.error("Unexpected error while checking delete conditions for user: " + user.getName(), e);
            }
        }
        return result.build();
    }
}
