package com.atlassian.jira.plugin.user;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v6.1
 */
public class PasswordPolicyManagerImpl implements PasswordPolicyManager
{
    private final PluginModuleTracker<PasswordPolicy, PasswordPolicyModuleDescriptor> passwordPolicyPluginTracker;
    private static final Logger log = Logger.getLogger(PasswordPolicyManagerImpl.class);

    public PasswordPolicyManagerImpl(final PluginAccessor pluginAccessor, final PluginEventManager pluginEventManager)
    {
        passwordPolicyPluginTracker = createTracker(pluginAccessor, pluginEventManager);
    }

    @Override
    @Nonnull
    public Collection<WebErrorMessage> checkPolicy(@Nonnull final ApplicationUser user,
            @Nullable final String oldPassword, @Nonnull final String newPassword)
    {
        return checkPolicy(notNull("user", user).getDirectoryUser(), oldPassword, newPassword);
    }

    @Override
    @Nonnull
    public Collection<WebErrorMessage> checkPolicy(@Nonnull final String username,
            @Nullable final String displayName, @Nullable final String emailAddress, @Nonnull final String newPassword)
    {
        final User template = new ImmutableUser(-1L, username, displayName, emailAddress, false);
        return checkPolicy(template, null, newPassword);
    }


    @Override
    @Nonnull
    public List<String> getPolicyDescription(boolean hasOldPassword)
    {
        final ImmutableList.Builder<String> result = ImmutableList.builder();
        for (final PasswordPolicy module : enabledModules())
        {
            try
            {
                final List<String> advice = module.getPolicyDescription(hasOldPassword);
                if (advice != null)
                {
                    result.addAll(advice);
                }
            }
            catch (RuntimeException e)
            {
                log.error("Unexpected error while building password policy description", e);
            }
        }
        return result.build();
    }



    @VisibleForTesting
    PluginModuleTracker<PasswordPolicy, PasswordPolicyModuleDescriptor> createTracker(PluginAccessor pluginAccessor, PluginEventManager pluginEventManager)
    {
        return DefaultPluginModuleTracker.create(pluginAccessor, pluginEventManager, PasswordPolicyModuleDescriptor.class);
    }

    @VisibleForTesting
    Iterable<PasswordPolicy> enabledModules()
    {
        return Iterables.filter(passwordPolicyPluginTracker.getModules(), new Predicate<PasswordPolicy>()
        {
            @Override
            public boolean apply(@Nullable PasswordPolicy input)
            {
                return input != null;
            }
        });
    }



    private Collection<WebErrorMessage> checkPolicy(@Nonnull final User user, @Nullable final String oldPassword,
            @Nonnull final String newPassword)
    {
        final ImmutableList.Builder<WebErrorMessage> result = ImmutableList.builder();
        for (final PasswordPolicy module : enabledModules())
        {
            try
            {
                final Collection<WebErrorMessage> moduleErrors = module.validatePolicy(user, oldPassword, newPassword);
                if (moduleErrors != null)
                {
                    result.addAll(moduleErrors);
                }
            }
            catch (RuntimeException e)
            {
                log.error("Unexpected error while checking password policies for user: " + user.getName(), e);
            }
        }
        return result.build();
    }
}

