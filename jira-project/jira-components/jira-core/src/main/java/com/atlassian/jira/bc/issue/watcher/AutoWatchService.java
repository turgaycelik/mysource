package com.atlassian.jira.bc.issue.watcher;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;

import static com.atlassian.jira.event.type.EventType.ISSUE_COMMENTED_ID;
import static com.atlassian.jira.event.type.EventType.ISSUE_CREATED_ID;

/**
 * Service for automatically watching Issues that are created or commented on a by a user assuming they haven't disabled
 * it in their preferences.
 *
 * @since v5.0.2
 */
public class AutoWatchService implements Startable
{
    private final EventPublisher eventPublisher;
    private final WatcherService watcherService;
    private final UserPreferencesManager userPreferencesManager;

    public AutoWatchService(EventPublisher eventPublisher, WatcherService watcherService, UserPreferencesManager userPreferencesManager)
    {
        this.eventPublisher = eventPublisher;
        this.watcherService = watcherService;
        this.userPreferencesManager = userPreferencesManager;
    }

    @Override
    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onIssueEvent(final IssueEvent event)
    {
        if (watcherService.isWatchingEnabled())
        {
            final User user = event.getUser();
            if (user != null && isAutoWatchEnabledForUser(user) && isAutowatchEvent(event))
            {
                watcherService.addWatcher(event.getIssue(), user, user);
            }
        }
    }

    private boolean isAutowatchEvent(final IssueEvent event)
    {
        final Long eventTypeId = event.getEventTypeId();
        final boolean createOrComment = eventTypeId != null && (eventTypeId.equals(ISSUE_CREATED_ID) || eventTypeId.equals(ISSUE_COMMENTED_ID));
        return createOrComment || event.getComment() != null;
    }

    public boolean isAutoWatchEnabledForUser(final User user)
    {
        final ExtendedPreferences preferences = userPreferencesManager.getExtendedPreferences(ApplicationUsers.from(user));
        boolean autoWatchForUserSet = preferences.containsValue(PreferenceKeys.USER_AUTOWATCH_DISABLED);
        if (autoWatchForUserSet)
        {
            return !preferences.getBoolean(PreferenceKeys.USER_AUTOWATCH_DISABLED);
        }
        final ApplicationProperties jiraProperties = ComponentAccessor.getApplicationProperties();
        return !jiraProperties.getOption(PreferenceKeys.USER_AUTOWATCH_DISABLED);
    }

}