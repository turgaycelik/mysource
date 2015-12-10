package com.atlassian.jira.notification.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserManager;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Basic setUp/tearDown common to most of the simple notification tests.
 *
 * @since v6.0
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractNotificationTestCase
{
    @Mock protected GroupManager groupManager;
    @Mock protected UserManager userManager;
    @Mock protected UserPreferencesManager userPreferencesManager;
    @Mock protected ExtendedPreferences preferences;

    protected MockComponentWorker worker;
    protected MockIssue issue;
    protected ApplicationUser user;

    @Before
    public final void setUp()
    {
        worker = new MockComponentWorker()
                .addMock(GroupManager.class, groupManager)
                .addMock(UserManager.class, userManager)
                .addMock(UserPreferencesManager.class, userPreferencesManager)
                .init();

        user = initUser();
        issue = new MockIssue();
        setUpTest();
    }

    @After
    public final void tearDown()
    {
        tearDownTest();
        worker = null;
        groupManager = null;
        userManager = null;
        userPreferencesManager = null;
        user = null;
        issue = null;
        preferences = null;
    }

    protected void setUpTest() {}
    protected void tearDownTest() {}



    protected ApplicationUser initUser()
    {
        final ApplicationUser dude = new MockApplicationUser("ID12345", "DudeUser", "Dude User", "dudeuser@example.com");
        final MockIssue issue = new MockIssue();
        issue.setReporterId(dude.getKey());

        when(userManager.getUserByKey(dude.getKey())).thenReturn(dude);
        when(userManager.getUserByName(dude.getUsername())).thenReturn(dude);
        when(userPreferencesManager.getExtendedPreferences(dude)).thenReturn(preferences);
        when(preferences.getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE)).thenReturn(NotificationRecipient.MIMETYPE_HTML);
        return dude;
    }

    protected Map<String,String> paramsWithLevel()
    {
        final Map<String,String> params = new HashMap<String,String>(4);
        params.put("level", "group1");
        return params;
    }

    protected void checkRecipients(List<NotificationRecipient> actualRecipients, ApplicationUser... expectedUsers)
    {
        final List<NotificationRecipient> expectedRecipients = new ArrayList<NotificationRecipient>(expectedUsers.length);
        for (ApplicationUser user : expectedUsers)
        {
            expectedRecipients.add(new NotificationRecipient(user));
        }
        assertEquals(expectedRecipients, actualRecipients);
    }
}
