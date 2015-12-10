package com.atlassian.jira.plugins.share.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mail.MailService;
import com.atlassian.jira.notification.JiraNotificationReason;
import com.atlassian.jira.notification.NotificationFilterContext;
import com.atlassian.jira.notification.NotificationFilterManager;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.plugins.share.ShareBean;
import com.atlassian.jira.plugins.share.ShareService;
import com.atlassian.jira.plugins.share.util.NotificationRecipientUtil;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.preferences.UserPreferencesManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import static com.atlassian.jira.notification.NotificationRecipient.MIMETYPE_HTML;
import static com.atlassian.jira.plugins.share.ShareTestUtil.createSearchValidationResult;
import static com.atlassian.jira.plugins.share.ShareTestUtil.getShareBean;
import static com.atlassian.jira.plugins.share.ShareTestUtil.mockSearchRequest;
import static com.atlassian.jira.plugins.share.ShareTestUtil.mockUserPreferencesManager;
import static com.atlassian.jira.plugins.share.ShareTestUtil.recipientFromEmail;
import static com.atlassian.jira.plugins.share.ShareTestUtil.recipientFromUserName;
import static com.atlassian.jira.plugins.share.ShareTestUtil.recipientFromUserNameWithEmailFormat;
import static com.atlassian.jira.plugins.share.search.ShareSearchEmailsSender.BODY_TEMPLATE_PATH;
import static com.atlassian.jira.plugins.share.search.ShareSearchEmailsSender.PATH_SEPARATOR;
import static com.atlassian.jira.plugins.share.search.ShareSearchEmailsSender.SAVED_SEARCH_TEMPLATE;
import static com.atlassian.jira.plugins.share.search.ShareSearchEmailsSender.SHARE_JQL_TEMPLATE;
import static com.atlassian.jira.plugins.share.search.ShareSearchEmailsSender.SUBJECT_TEMPLATE_PATH;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TestShareSearchEmailsSender
{

    private ShareSearchEmailsSender shareSearchEmailsSender;
    @Mock
    private MailService mailService;
    @Mock
    private ShareManager shareManager;
    @Mock
    private NotificationRecipientUtil notificationRecipientUtil;
    @Mock
    private NotificationFilterManager notificationFilterManager;
    @Captor
    private ArgumentCaptor<String> bodyTemplateCaptor;
    @Captor
    private ArgumentCaptor<String> subjectTemplateCaptor;
    @Mock
    private ApplicationUser user;

    @Rule
    public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);
    @AvailableInContainer
    protected UserPreferencesManager userPreferencesManager = mockUserPreferencesManager();

    private NotificationRecipient userRecipient;
    private NotificationRecipient emailRecipient;
    private List<NotificationRecipient> allRecipients;
    private Map<String, Object> params;
    private SearchRequest searchRequest = mockSearchRequest();
    private ShareBean shareBean = getShareBean();
    private ShareService.ValidateShareSearchRequestResult searchValidationResult;

    @Before
    public void setUp()
    {
        shareSearchEmailsSender = new ShareSearchEmailsSender(
                mailService,
                shareManager,
                notificationFilterManager,
                notificationRecipientUtil);

        searchValidationResult = createSearchValidationResult(user, shareBean, searchRequest);
        userRecipient = recipientFromUserName("userName");
        emailRecipient = recipientFromEmail("email@example.com");
        params = new HashMap<String, Object>();
    }

    @Test
    public void shouldSetSharedWithParams()
    {
        //given
        expectMappingToNotificationRecipients(userRecipient);
        expectFilteringOutCurrentReceiverAndAuthor();

        //when
        final ShareService.ValidateShareSearchRequestResult searchValidationResult = createSearchValidationResult(user, shareBean, mockSearchRequest());
        shareSearchEmailsSender.sendShareSearchEmails(searchValidationResult, params);

        //then
        assertThat((NotificationRecipient) (params.get("recipient")), equalTo(userRecipient));
        assertThatOtherRecipientsDoNotContainCurrentRecipient(emailRecipient);
    }

    @Test
    public void shouldUseJqlTemplateWhenIsNoSearchRequest()
    {
        boolean isSearchSharable = false;
        final String expectedMailFormat = MIMETYPE_HTML;
        testGivenTemplateUsed(SHARE_JQL_TEMPLATE, isSearchSharable, expectedMailFormat);
    }

    @Test
    public void shouldUseSearchRequestTemplate()
    {
        mockSearchRequest();
        createSearchValidationResult(user, shareBean, searchRequest);
        boolean isSearchSharable = true;
        final String expectedMailFormat = MIMETYPE_HTML;
        testGivenTemplateUsed(SAVED_SEARCH_TEMPLATE, isSearchSharable, expectedMailFormat);
    }

    @Test
    public void shouldUseJqlWhenIsSearchRequestButNotSharable()
    {
        createSearchValidationResult(user, shareBean, searchRequest);
        boolean isSearchSharable = false;
        final String expectedMailFormat = MIMETYPE_HTML;
        testGivenTemplateUsed(SHARE_JQL_TEMPLATE, isSearchSharable, expectedMailFormat);
    }

    private void testGivenTemplateUsed(final String template, final Boolean isSearchShareable, String expectedMailFormat)
    {
        //given
        userRecipient = getUserWithMailFormat(expectedMailFormat);
        expectMappingToNotificationRecipients(userRecipient);
        expectFilteringOutCurrentReceiverAndAuthor();

        when(shareManager.isSharedWith(userRecipient.getUser(), searchRequest))
                .thenReturn(isSearchShareable);

        //when
        shareSearchEmailsSender.sendShareSearchEmails(searchValidationResult, params);

        //then
        verify(mailService).sendRenderedMail(eq(user.getDirectoryUser()), eq(userRecipient), subjectTemplateCaptor.capture(), bodyTemplateCaptor.capture(), eq(params));
        assertThat(subjectTemplateCaptor.getValue(), equalTo(SUBJECT_TEMPLATE_PATH + template));

        String expectedBodyTemplatePath = BODY_TEMPLATE_PATH + expectedMailFormat + PATH_SEPARATOR + template;
        assertThat(bodyTemplateCaptor.getValue(), equalTo(expectedBodyTemplatePath));
    }

    @Test
    public void shouldIgnoreRecipientThatAreFilteredOut()
    {
        //given
        expectMappingToNotificationRecipients(userRecipient);
        expectFilteringOutCurrentReceiverAndAuthor();

        final NotificationFilterContext context = mock(NotificationFilterContext.class);
        when(notificationFilterManager.makeContextFrom(JiraNotificationReason.SHARED))
                .thenReturn(context);

        when(notificationFilterManager.filtered(userRecipient, context))
                .thenReturn(true);

        //when
        shareSearchEmailsSender.sendShareSearchEmails(searchValidationResult, params);

        //then
        verifyNoMoreInteractions(mailService);
    }

    private void expectMappingToNotificationRecipients(NotificationRecipient... userRecipients)
    {
        allRecipients = Lists.newArrayList(userRecipients);
        when(notificationRecipientUtil.getRecipients(shareBean))
                .thenReturn(allRecipients);
    }

    private void expectFilteringOutCurrentReceiverAndAuthor()
    {
        when(notificationRecipientUtil.filterOutAuthorAndReceiver(user, allRecipients, emailRecipient))
                .thenReturn(Sets.newHashSet(userRecipient));

        when(notificationRecipientUtil.filterOutAuthorAndReceiver(user, allRecipients, userRecipient))
                .thenReturn(Sets.newHashSet(emailRecipient));
    }

    private void assertThatOtherRecipientsDoNotContainCurrentRecipient(final NotificationRecipient expectedInvolvedUser)
    {
        assertThat((Collection<NotificationRecipient>) (params.get("involvedUsers")), containsInAnyOrder(expectedInvolvedUser));
    }

    private NotificationRecipient getUserWithMailFormat(final String expectedMailFormat)
    {
        return recipientFromUserNameWithEmailFormat("userName", expectedMailFormat);
    }
}
