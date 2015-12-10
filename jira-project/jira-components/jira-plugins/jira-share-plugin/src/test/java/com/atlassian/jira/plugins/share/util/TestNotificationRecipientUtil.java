package com.atlassian.jira.plugins.share.util;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.plugins.share.ShareBean;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static com.atlassian.jira.plugins.share.ShareTestUtil.getShareBean;
import static com.atlassian.jira.plugins.share.ShareTestUtil.mockUserPreferencesManager;
import static com.atlassian.jira.plugins.share.ShareTestUtil.recipientFromEmail;
import static com.atlassian.jira.plugins.share.ShareTestUtil.recipientFromUserName;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestNotificationRecipientUtil
{
    @InjectMocks
    private NotificationRecipientUtil notificationRecipientUtil;
    @Mock
    private UserManager userManager;

    @Rule
    public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);
    @AvailableInContainer
    protected UserPreferencesManager userPreferencesManager = mockUserPreferencesManager();

    @Test
    public void shouldCreateNotificationRecipientsFromBean()
    {
        //given
        final Set<String> userNames = Sets.newHashSet("user1");
        final Set<String> emails = Sets.newHashSet("email@example.com");
        final ShareBean shareBean = getShareBean(userNames, emails);

        final ApplicationUser appUser = mock(ApplicationUser.class);
        when(userManager.getUserByName("user1"))
                .thenReturn(appUser);

        //when
        final List<NotificationRecipient> recipients = notificationRecipientUtil.getRecipients(shareBean);

        //then
        assertThat(extractUsersAndMails(recipients), hasItems(appUser, "email@example.com"));
    }

    @Test
    public void shouldIgnoreUserRecipientIfNoApplicationUserFoundForHim()
    {
        //given
        final Set<String> userNames = Sets.newHashSet("user1");
        final Set<String> emails = Sets.newHashSet("email@example.com");
        final ShareBean shareBean = getShareBean(userNames, emails);

        when(userManager.getUserByName("user1"))
                .thenReturn(null);

        //when
        final List<NotificationRecipient> recipients = notificationRecipientUtil.getRecipients(shareBean);

        //then
        assertThat(extractUsersAndMails(recipients), hasItems((Object) "email@example.com"));
        assertThat(recipients, hasSize(1));
    }

    @Test
    public void shouldFilterOutAuthorByEmail()
    {
        //given
        final NotificationRecipient notificationReceiver = recipientFromEmail("example@example.com");
        final NotificationRecipient someUserRecipient = recipientFromUserName("sampleUserName");
        final NotificationRecipient someEmailRecipient = recipientFromEmail("other@example.com");
        final NotificationRecipient authorRecipient = recipientFromEmail("author@email.com");
        final ApplicationUser authorUser = mockUserWithMail("author@email.com");
        final List<NotificationRecipient> allRecipients = Lists.newArrayList(authorRecipient, someUserRecipient, someEmailRecipient);

        //when
        final Set<NotificationRecipient> filtered = notificationRecipientUtil.filterOutAuthorAndReceiver(authorUser, allRecipients, notificationReceiver);

        //then
        assertThat(filtered, allOf(containsInAnyOrder(someUserRecipient, someEmailRecipient), not(hasItems(authorRecipient))));
    }

    @Test
    public void shouldFilterOutAuthorByUser()
    {
        //given
        final ApplicationUser authorUser = mockUserWithMail("author@email.com");
        final NotificationRecipient notificationReceiver = recipientFromEmail("example@example.com");
        final NotificationRecipient someUserRecipient = recipientFromUserName("sampleUserName");
        final NotificationRecipient someEmailRecipient = recipientFromEmail("other@example.com");
        final NotificationRecipient authorRecipient = recipientFromUser(authorUser);
        final List<NotificationRecipient> allRecipients = Lists.newArrayList(authorRecipient, someUserRecipient, someEmailRecipient);

        //when
        final Set<NotificationRecipient> filtered = notificationRecipientUtil.filterOutAuthorAndReceiver(authorUser, allRecipients, notificationReceiver);

        //then
        assertThat(filtered, allOf(containsInAnyOrder(someUserRecipient, someEmailRecipient), not(hasItems(authorRecipient))));
    }

    @Test
    public void shouldFilterOutNotificationReceiver()
    {
        //given
        final ApplicationUser authorUser = mockUserWithMail("author@email.com");
        final NotificationRecipient notificationReceiver = recipientFromEmail("example@example.com");
        final NotificationRecipient someUserRecipient = recipientFromUserName("sampleUserName");
        final NotificationRecipient someEmailRecipient = recipientFromEmail("other@example.com");
        final List<NotificationRecipient> allRecipients = Lists.newArrayList(notificationReceiver, someUserRecipient, someEmailRecipient);

        //when
        final Set<NotificationRecipient> filtered = notificationRecipientUtil.filterOutAuthorAndReceiver(authorUser, allRecipients, notificationReceiver);

        //then
        assertThat(filtered, containsInAnyOrder(someUserRecipient, someEmailRecipient));
    }

    private NotificationRecipient recipientFromUser(final ApplicationUser user)
    {


        return new NotificationRecipient(user);
    }

    private ApplicationUser mockUserWithMail(final String email)
    {
        ApplicationUser user = mock(ApplicationUser.class);
        when(user.getEmailAddress())
                .thenReturn(email);
        return user;
    }

    private Iterable<Object> extractUsersAndMails(final List<NotificationRecipient> recipients)
    {
        return Iterables.transform(recipients, new Function<NotificationRecipient, Object>()
        {
            @Override
            public Object apply(@Nullable final NotificationRecipient recipient)
            {
                final ApplicationUser user = recipient.getUser();
                if (user != null)
                { return user; }
                else
                { return recipient.getEmail(); }
            }
        });
    }
}
