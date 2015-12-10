package com.atlassian.jira.bc.issue.watcher;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.MockComment;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestAutoWatchService
{
    @Rule public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock private EventPublisher eventPublisher;
    @Mock private WatcherService watcherService;
    @Mock private UserPreferencesManager userPreferencesManager;
    @Mock private ExtendedPreferences preferences;

    @Mock
    @AvailableInContainer
    private ApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
        when(watcherService.isWatchingEnabled()).thenReturn(true);
    }

    @Test
    public void testIssueCreatedIsAutowatched()
    {
        isAutowatchEvent(true, EventType.ISSUE_CREATED_ID);
    }

    @Test
    public void testIssueCommentedIsAutowatched()
    {
        isAutowatchEvent(true, EventType.ISSUE_COMMENTED_ID);
    }

    @Test
    public void testIssueUpdatedIsNotAutowatched()
    {
        isAutowatchEvent(false, EventType.ISSUE_UPDATED_ID);
    }

    @Test
    public void testIssueUpdatedWithCommentIsAutowatched()
    {
        isAutowatchEvent(true, EventType.ISSUE_UPDATED_ID, new MockComment("", ""));
    }

    @Test
    public void testIsEnabledWhenFalseSetting()
    {
        when(preferences.getBoolean(PreferenceKeys.USER_AUTOWATCH_DISABLED)).thenReturn(false);
        isAutowatchEvent(true);
    }

    @Test
    public void testNullUser()
    {
        isAutowatchEvent(false, null, null, null);
    }

    @Test
    public void testIsEnabledWhenGlobalSettingIsEnabledAndUserSettingIsNotSet()
    {
        isAutowatchEnabled(true, true, null);
    }

    @Test
    public void testIsEnabledWhenGlobalSettingIsDisabledAndUserSettingIsNotSet()
    {
        isAutowatchEnabled(false, false, null);
    }

    @Test
    public void testIsEnabledWhenGlobalSettingIsSetAndUserSettingIsEnabled()
    {
        isAutowatchEnabled(true, false, true);
    }

    @Test
    public void testIsEnabledWhenGlobalSettingIsSetAndUserSettingIsDisabled()
    {
        isAutowatchEnabled(false, true, false);
    }

    private void isAutowatchEvent(boolean expected)
    {
        isAutowatchEvent(expected, EventType.ISSUE_COMMENTED_ID);
    }

    private void isAutowatchEvent(boolean expected, Long eventType)
    {
        isAutowatchEvent(expected, eventType, null);
    }

    private void isAutowatchEvent(boolean expected, Long eventType, Comment comment)
    {
        isAutowatchEvent(expected, eventType, comment, new MockApplicationUser("user"));
    }

    private void isAutowatchEvent(boolean expected, Long eventType, Comment comment, ApplicationUser user)
    {
        final IssueEvent event = new IssueEvent(new MockIssue(), ApplicationUsers.toDirectoryUser(user), comment, null, null, null, eventType);
        when(userPreferencesManager.getExtendedPreferences(user)).thenReturn(preferences);
        new AutoWatchService(eventPublisher, watcherService, userPreferencesManager).onIssueEvent(event);
        verify(watcherService, times(expected ? 1 : 0)).addWatcher(event.getIssue(), event.getUser(), event.getUser());
    }

    private void isAutowatchEnabled(boolean expected, boolean appAutoWatch, Boolean userAutoWatch)
    {
        final ApplicationUser user = new MockApplicationUser("user");
        final ExtendedPreferences userPreferences = Mockito.mock(ExtendedPreferences.class);
        if (userAutoWatch != null)
        {
            when(userPreferences.containsValue(PreferenceKeys.USER_AUTOWATCH_DISABLED)).thenReturn(true);
            when(userPreferences.getBoolean(PreferenceKeys.USER_AUTOWATCH_DISABLED)).thenReturn(!userAutoWatch);
        }
        else
        {
            when(userPreferences.containsValue(PreferenceKeys.USER_AUTOWATCH_DISABLED)).thenReturn(false);
        }
        when(userPreferencesManager.getExtendedPreferences(user)).thenReturn(userPreferences);
        when(applicationProperties.getOption(PreferenceKeys.USER_AUTOWATCH_DISABLED)).thenReturn(!appAutoWatch);
        final AutoWatchService autoWatchService = new AutoWatchService(eventPublisher, watcherService, userPreferencesManager);
        Assert.assertEquals(expected, autoWatchService.isAutoWatchEnabledForUser(user.getDirectoryUser()));
    }

}
