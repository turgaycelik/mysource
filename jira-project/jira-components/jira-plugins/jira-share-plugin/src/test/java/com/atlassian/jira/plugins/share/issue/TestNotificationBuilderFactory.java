package com.atlassian.jira.plugins.share.issue;

import java.util.Set;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.notification.AdhocNotificationService;
import com.atlassian.jira.notification.NotificationBuilder;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.user.preferences.UserPreferencesManager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.opensymphony.util.TextUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.atlassian.jira.plugins.share.ShareTestUtil.mockUserPreferencesManager;
import static com.atlassian.jira.plugins.share.ShareTestUtil.recipientFromEmail;
import static com.atlassian.jira.plugins.share.ShareTestUtil.recipientFromUserName;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestNotificationBuilderFactory
{
    @Rule
    public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    private NotificationBuilderFactory notificationBuilderFactory;
    @Mock
    private AdhocNotificationService notificationService;
    @Mock
    private NotificationBuilder builder;
    @AvailableInContainer
    protected UserPreferencesManager userPreferencesManager = mockUserPreferencesManager();

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        notificationBuilderFactory = new NotificationBuilderFactory(notificationService);

        when(notificationService.makeBuilder())
                .thenReturn(builder);
    }

    @Test
    public void createBuilderForUserRecipient()
    {
        //given
        final String userName = "userName";
        final NotificationRecipient recipient = recipientFromUserName(userName);
        final String message = "some message";
        final Set<NotificationRecipient> otherRecipients = Sets.newHashSet();

        //when
        notificationBuilderFactory.createNotificationBuilder(message, recipient, otherRecipients);

        //then
        verify(builder).addToUser(userName);
    }

    @Test
    public void createBuilderForMailRecipient()
    {
        //given
        final String email = "sample@example.com";
        final NotificationRecipient recipient = recipientFromEmail(email);
        final String message = "some message";
        final Set<NotificationRecipient> otherRecipients = Sets.newHashSet();

        //when
        notificationBuilderFactory.createNotificationBuilder(message, recipient, otherRecipients);

        //then
        verify(builder).addToEmail(email);
    }

    @Test
    public void shouldSetTemplateForBuilder()
    {
        //given
        final String email = "sample@example.com";
        final NotificationRecipient recipient = recipientFromEmail(email);
        final String message = "some message";
        final Set<NotificationRecipient> otherRecipients = Sets.newHashSet();

        //when
        notificationBuilderFactory.createNotificationBuilder(message, recipient, otherRecipients);

        //then
        verify(builder).setTemplate(any(String.class));
    }

    @Test
    public void shouldSetParams()
    {
        //given
        final String email = "sample@example.com";
        final NotificationRecipient recipient = recipientFromEmail(email);
        final NotificationRecipient otherRecipient = recipientFromUserName("user");
        final String message = "some message";
        final Set<NotificationRecipient> otherRecipients = Sets.newHashSet(otherRecipient);

        //when
        notificationBuilderFactory.createNotificationBuilder(message, recipient, otherRecipients);


        ArgumentCaptor<ImmutableMap<String, Object>> paramsCaptor = ArgumentCaptor.forClass((Class) ImmutableMap.class);
        //then
        verify(builder).setTemplateParams(paramsCaptor.capture());
        final ImmutableMap<String, Object> params = paramsCaptor.getValue();

        assertThat(params, hasEntry("comment", (Object) message));
        assertThat(params, hasEntry("htmlComment", (Object) TextUtils.htmlEncode(message)));
        assertThat(params, hasEntry("recipient", (Object) recipient));
        assertThat(params, hasEntry("involvedUsers", (Object) otherRecipients));
    }
}
