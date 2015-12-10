package com.atlassian.jira.plugins.share.issue;

import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.notification.AdhocNotificationService;
import com.atlassian.jira.notification.MockAdhocNotificationService;
import com.atlassian.jira.notification.NotificationBuilder;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.plugins.share.ShareBean;
import com.atlassian.jira.plugins.share.ShareService;
import com.atlassian.jira.plugins.share.event.ShareIssueEvent;
import com.atlassian.jira.plugins.share.util.NotificationRecipientUtil;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.preferences.UserPreferencesManager;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;

import static com.atlassian.jira.notification.AdhocNotificationService.ValiationOption.CONTINUE_ON_NO_RECIPIENTS;
import static com.atlassian.jira.plugins.share.ShareServiceImpl.NO_PERMISSION_TO_BROWSE_USERS;
import static com.atlassian.jira.plugins.share.ShareTestUtil.getShareBean;
import static com.atlassian.jira.plugins.share.ShareTestUtil.getShareIssueValidationResult;
import static com.atlassian.jira.plugins.share.ShareTestUtil.mockUserPreferencesManager;
import static com.atlassian.jira.plugins.share.ShareTestUtil.recipientFromEmail;
import static com.atlassian.jira.plugins.share.ShareTestUtil.recipientFromUserName;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestShareIssueService
{
    private ShareIssueService shareIssueService;


    @Rule
    public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);
    @AvailableInContainer
    protected UserPreferencesManager userPreferencesManager = mockUserPreferencesManager();

    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private NotificationRecipientUtil notificationRecipientUtil;
    @Mock
    private AdhocNotificationService notificationService;
    @Mock
    private NotificationBuilderFactory notificationBuilderFactory;
    @Mock
    private ApplicationUser user;
    @Mock
    private User directoryUser;
    @Mock
    private Issue issue;
    private ShareBean shareBean = getShareBean();

    @Before
    public void setUp()
    {
        shareIssueService = new ShareIssueService(
                eventPublisher,
                notificationService,
                notificationBuilderFactory,
                notificationRecipientUtil);

        when(user.getDirectoryUser())
                .thenReturn(directoryUser);
    }

    @Test
    public void shouldPublishEventWhenShareIssueSent() throws Exception
    {
        //given
        final ShareService.ValidateShareIssueResult validationResult = getShareIssueValidationResult(user, shareBean, issue);

        //when
        shareIssueService.shareIssue(validationResult);

        //then
        verify(eventPublisher).publish(any(ShareIssueEvent.class));
    }

    @Test
    public void shouldSendMailToUsersAndEmailAddresses()
    {
        //given
        final ShareService.ValidateShareIssueResult validationResult = getShareIssueValidationResult(user, shareBean, issue);

        final NotificationRecipient userRecipient = recipientFromUserName("username");
        final NotificationRecipient mailRecipient = recipientFromEmail("sampleMail@example.com");
        final List<NotificationRecipient> notificationRecipients = Lists.newArrayList(userRecipient, mailRecipient);
        when(notificationRecipientUtil.getRecipients(shareBean))
                .thenReturn(notificationRecipients);

        final AdhocNotificationService.ValidateNotificationResult userNotification = expectNotificationToRecipient(userRecipient);
        final AdhocNotificationService.ValidateNotificationResult mailNotification = expectNotificationToRecipient(mailRecipient);

        //when
        shareIssueService.shareIssue(validationResult);

        //then
        verify(notificationService).sendNotification(userNotification);
        verify(notificationService).sendNotification(mailNotification);
    }

    @Test
    public void shouldNotSendEmailWhenValidationFailed()
    {
        //given
        final ShareService.ValidateShareIssueResult validationResult = getShareIssueValidationResult(user, shareBean, issue);

        final NotificationRecipient userRecipient = recipientFromUserName("username");
        final NotificationRecipient mailRecipient = recipientFromEmail("sampleMail@example.com");
        final List<NotificationRecipient> notificationRecipients = Lists.newArrayList(userRecipient, mailRecipient);
        when(notificationRecipientUtil.getRecipients(shareBean))
                .thenReturn(notificationRecipients);

        final AdhocNotificationService.ValidateNotificationResult userNotification = expectNotificationToRecipient(userRecipient);
        addSomeErrors(userNotification);

        final AdhocNotificationService.ValidateNotificationResult mailNotification = expectNotificationToRecipient(mailRecipient);
        addSomeErrors(mailNotification);

        //when
        shareIssueService.shareIssue(validationResult);

        //then
        verify(notificationService, times(0)).sendNotification(any(AdhocNotificationService.ValidateNotificationResult.class));
    }

    private void addSomeErrors(final AdhocNotificationService.ValidateNotificationResult notification)
    {
        notification.getErrorCollection().addError(NO_PERMISSION_TO_BROWSE_USERS, NO_PERMISSION_TO_BROWSE_USERS);
    }

    private AdhocNotificationService.ValidateNotificationResult expectNotificationToRecipient(final NotificationRecipient recipient)
    {
        final NotificationBuilder notificationBuilder = Mockito.mock(NotificationBuilder.class);
        when(notificationBuilderFactory.createNotificationBuilder(eq(shareBean.getMessage()), eq(recipient), any(Set.class)))
                .thenReturn(notificationBuilder);

        final AdhocNotificationService.ValidateNotificationResult validationResult = new MockAdhocNotificationService()
                .getSampleValidationResult(user.getDirectoryUser(), issue, notificationBuilder);

        when(notificationService.validateNotification(notificationBuilder, user.getDirectoryUser(), issue, CONTINUE_ON_NO_RECIPIENTS))
                .thenReturn(validationResult);

        return validationResult;
    }
}
