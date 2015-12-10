package com.atlassian.jira.plugins.share;

import java.util.Set;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.query.Query;

import com.google.common.collect.Sets;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ShareTestUtil
{
    public static final String QUERY = "searchRequestQueryString";

    public static final UserPreferencesManager preferencesManager = mock(UserPreferencesManager.class);

    public static UserPreferencesManager mockUserPreferencesManager()
    {
        final ExtendedPreferences extendedPreferences = mock(ExtendedPreferences.class);
        when(preferencesManager.getExtendedPreferences(any(ApplicationUser.class)))
                .thenReturn(extendedPreferences);

        final Preferences preferences = mock(Preferences.class);
        when(preferencesManager.getPreferences(any(User.class)))
                .thenReturn(preferences);
        return preferencesManager;
    }

    public static NotificationRecipient recipientFromUserName(String userName)
    {
        final User mockUser = mock(User.class);
        when(mockUser.getName())
                .thenReturn(userName);

        ApplicationUser user = new DelegatingApplicationUser("userName", mockUser);

        return new NotificationRecipient(user);
    }

    public static NotificationRecipient recipientFromEmail(String email)
    {
        return new NotificationRecipient(email);
    }

    public static ShareBean getShareBean()
    {
        final Set<String> usernames = Sets.newHashSet("user");
        final Set<String> emails = Sets.newHashSet("mail");
        return getShareBean(usernames, emails);
    }

    public static ShareBean getShareBean(Set<String> usernames, Set<String> emails)
    {
        final String message = "message";
        final String jql = "OnlyJQL";
        return new ShareBean(usernames, emails, message, jql);
    }

    public static ShareService.ValidateShareIssueResult getShareIssueValidationResult(final ApplicationUser user, final ShareBean shareBean, final Issue issue)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        return new ShareService.ValidateShareIssueResult(errors, user, shareBean, issue);
    }

    public static ShareService.ValidateShareSearchRequestResult createSearchValidationResult(final ApplicationUser user, final ShareBean shareBean, final SearchRequest searchRequest)
    {
        final ErrorCollection errorCOllection = new SimpleErrorCollection();
        return new ShareService.ValidateShareSearchRequestResult(errorCOllection, user, shareBean, searchRequest);
    }

    public static SearchRequest mockSearchRequest()
    {
        final long id = 20;
        final Query query = mock(Query.class);

        when(query.getQueryString())
                .thenReturn(QUERY);

        return new SearchRequest(query, mock(ApplicationUser.class), "searchName", "description", id, 10);
    }

    public static NotificationRecipient recipientFromUserNameWithEmailFormat(String userName, String emailFormat)
    {
        final ApplicationUser mockUser = mock(ApplicationUser.class);
        when(mockUser.getName())
                .thenReturn(userName);

        final UserPreferencesManager preferencesManager = mockUserPreferencesManager();
        when(preferencesManager.getExtendedPreferences(mockUser).getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE))
                .thenReturn(emailFormat);

        return new NotificationRecipient(mockUser);
    }
}