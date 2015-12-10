package com.atlassian.jira.notification;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.type.NotificationType;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * @since v6.0
 */
public class NotificationFilterManagerImpl implements NotificationFilterManager
{

    private static final Logger log = Logger.getLogger(NotificationFilterManagerImpl.class);

    private final PluginModuleTracker<NotificationFilter, NotificationFilterModuleDescriptor> pluginModuleTracker;

    public NotificationFilterManagerImpl(PluginAccessor pluginAccessor, PluginEventManager pluginEventManager)
    {
        pluginModuleTracker = createTracker(pluginAccessor, pluginEventManager);
    }

    @VisibleForTesting
    PluginModuleTracker<NotificationFilter, NotificationFilterModuleDescriptor> createTracker(PluginAccessor pluginAccessor, PluginEventManager pluginEventManager)
    {
        return DefaultPluginModuleTracker.create(pluginAccessor, pluginEventManager, NotificationFilterModuleDescriptor.class);
    }


    @Override
    public Iterable<NotificationRecipient> recomputeRecipients(final Iterable<NotificationRecipient> recipients, final NotificationFilterContext context)
    {
        final Iterable<NotificationFilter> notificationFilters = enabledNotificationFilters();

        // call add first for all plugins
        Iterable<NotificationRecipient> added = Iterables.concat(recipients, addRecipients(notificationFilters, context));

        // then call remove next for all plugins
        return Iterables.filter(added, new Predicate<NotificationRecipient>()
        {
            @Override
            public boolean apply(@Nullable NotificationRecipient input)
            {
                // filtering filters on true so we want those that are NOT to be removed
                return !removeRecipient(notificationFilters, input, context);
            }
        });
    }

    @Override
    public boolean filtered(NotificationRecipient recipient, NotificationFilterContext context)
    {
        return removeRecipient(enabledNotificationFilters(), recipient, context);
    }

    @Override
    public NotificationFilterContext makeContextFrom(NotificationReason reason)
    {
        return new NotificationFilterContext(reason);
    }

    @Override
    public NotificationFilterContext makeContextFrom(NotificationReason reason, Issue issue)
    {
        return new NotificationFilterContext(reason, issue);
    }

    @Override
    public NotificationFilterContext makeContextFrom(NotificationReason reason, IssueEvent issueEvent)
    {
        return new IssueEventNotificationFilterContext(reason, issueEvent, null);
    }

    @Override
    public NotificationFilterContext makeContextFrom(NotificationFilterContext copy, NotificationType notificationType)
    {
        if (copy instanceof IssueEventNotificationFilterContext)
        {
            return new IssueEventNotificationFilterContext((IssueEventNotificationFilterContext) copy, notificationType);
        }
        else
        {
            return new NotificationFilterContext(copy);
        }
    }

    private Iterable<NotificationRecipient> addRecipients(Iterable<NotificationFilter> notificationFilters, final NotificationFilterContext context)
    {
        Set<NotificationRecipient> allRecipients = Sets.newHashSet();
        for (NotificationFilter notificationFilter : notificationFilters)
        {
            try
            {
                Iterable<NotificationRecipient> recipients = notificationFilter.addRecipient(context, ImmutableSet.copyOf(allRecipients));
                if (recipients != null && ! Iterables.isEmpty(recipients))
                {
                    Iterables.addAll(allRecipients, recipients);
                    if (log.isDebugEnabled())
                    {
                        log.debug(String.format("Added extra recipients via plugin '%s'", notificationFilter.getClass().getName()));
                    }
                }
            }
            catch (RuntimeException e)
            {
                // ignore them if they dont have the civility to not throw exceptions as a plugin
                log.error(String.format("Ignoring notification filter of type '%s' because of '%s'", notificationFilter.getClass().getName(), e.getMessage()));
            }
        }
        return allRecipients;
    }

    private boolean removeRecipient(Iterable<NotificationFilter> notificationFilters, NotificationRecipient recipient, final NotificationFilterContext context)
    {
        for (NotificationFilter notificationFilter : notificationFilters)
        {
            try
            {
                if (notificationFilter.removeRecipient(recipient, context))
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug(String.format("Removed '%s' as a notification recipient via plugin '%s'", recipient, notificationFilter.getClass().getName()));
                    }
                    return true;
                }
            }
            catch (RuntimeException e)
            {
                // ignore them if they dont have the civility to not throw exceptions as a plugin
                log.error(String.format("Ignoring notification filter of type '%s' because of '%s'", notificationFilter.getClass().getName(), e.getMessage()));
            }
        }
        return false;
    }

    @VisibleForTesting
    Iterable<NotificationFilter> enabledNotificationFilters()
    {
        return Iterables.filter(pluginModuleTracker.getModules(), new Predicate<NotificationFilter>()
        {
            @Override
            public boolean apply(@Nullable NotificationFilter input)
            {
                return input != null;
            }
        });
    }
}
