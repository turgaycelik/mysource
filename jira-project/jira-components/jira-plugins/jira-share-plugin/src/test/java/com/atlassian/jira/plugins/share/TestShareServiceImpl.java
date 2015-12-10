package com.atlassian.jira.plugins.share;

import java.util.Set;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.plugins.share.issue.ShareIssueService;
import com.atlassian.jira.plugins.share.search.ShareSearchRequestService;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.atlassian.jira.plugins.share.ShareTestUtil.createSearchValidationResult;
import static com.atlassian.jira.plugins.share.ShareTestUtil.getShareBean;
import static com.atlassian.jira.plugins.share.ShareTestUtil.getShareIssueValidationResult;
import static com.atlassian.jira.plugins.share.ShareTestUtil.mockSearchRequest;
import static com.atlassian.jira.plugins.share.ShareTestUtil.mockUserPreferencesManager;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TestShareServiceImpl
{
    private static final String NO_USERS_OR_MAILS_ERROR = "no users or mails error";
    private static final String NO_PERMISSION_TO_BROWSE_USERS_ERROR = "no permission to browse users error";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);
    @AvailableInContainer
    protected UserPreferencesManager userPreferencesManager = mockUserPreferencesManager();

    private ShareServiceImpl shareService;
    @Mock
    private I18nHelper i18nHelper;
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private ShareIssueService shareIssueService;
    @Mock
    private ShareSearchRequestService shareSearchRequestService;
    @Mock
    private ApplicationUser user;
    @Mock
    private Issue issue;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        shareService = new ShareServiceImpl(
                i18nHelper,
                permissionManager,
                shareIssueService,
                shareSearchRequestService
        );

        mockI18nHelper();
    }

    @Test
    public void shouldValidateWithErrorsWhenNoUsersOrEmailsProvided()
    {
        //given
        final Set<String> userNames = Sets.newHashSet();
        final Set<String> emails = Sets.newHashSet();
        final ShareBean shareBean = getShareBean(userNames, emails);

        //when
        final ShareService.ValidateShareIssueResult validationResult = shareService.validateShareIssue(user, shareBean, issue);

        //then
        assertThat(validationResult.getErrorCollection().getErrorMessages(), hasItem(NO_USERS_OR_MAILS_ERROR));
    }

    @Test
    public void shouldValidateWithErrorsWhenRemoteUserHaveNoPermissionToBrowseUsers()
    {
        //given
        when(permissionManager.hasPermission(Permissions.USER_PICKER, user))
                .thenReturn(false);

        //when
        final ShareService.ValidateShareIssueResult validationResult = shareService.validateShareIssue(user, getShareBean(), issue);

        //then
        assertThat(validationResult.getErrorCollection().getErrorMessages(), hasItem(NO_PERMISSION_TO_BROWSE_USERS_ERROR));
    }

    @Test
    public void shouldValidateWithoutErrors()
    {
        //given
        when(permissionManager.hasPermission(Permissions.USER_PICKER, user))
                .thenReturn(true);

        //when
        final ShareService.ValidateShareIssueResult validationResult = shareService.validateShareIssue(user, getShareBean(), issue);

        //then
        assertThat(validationResult.getErrorCollection().getErrorMessages(), hasSize(0));
    }

    @Test
    public void shouldValidateShareSearchWithoutErrors()
    {
        //given
        when(permissionManager.hasPermission(Permissions.USER_PICKER, user))
                .thenReturn(true);

        //when
        final SearchRequest searchRequest = mockSearchRequest();
        final ShareService.ValidateShareSearchRequestResult validationResult = shareService.validateShareSearchRequest(user, getShareBean(), searchRequest);

        //then
        assertThat(validationResult.getErrorCollection().getErrorMessages(), hasSize(0));
    }

    @Test
    public void shouldShareIssue()
    {
        //given
        final ShareService.ValidateShareIssueResult validationResult = getShareIssueValidationResult(user, null, null);

        //when
        shareService.shareIssue(validationResult);

        //then
        verify(shareIssueService).shareIssue(validationResult);
    }

    @Test
    public void shouldNotShareIssueBecauseOfValidationErrors() throws Exception
    {
        //given
        final ShareService.ValidateShareIssueResult validationResult = getShareIssueValidationResultWithErrors();

        expectedException.expect(IllegalStateException.class);
        shareService.shareIssue(validationResult);

        verifyNoMoreInteractions(shareIssueService);
    }

    @Test
    public void shouldShareSearch()
    {
        //given
        final ShareService.ValidateShareSearchRequestResult validationResult = createSearchValidationResult(user, null, null);

        //when
        shareService.shareSearchRequest(validationResult);

        //than
        verify(shareSearchRequestService).shareSearchRequest(validationResult);
    }

    @Test
    public void shouldNotShareSearchBecauseOfValidationErrors() throws Exception
    {
        //given
        final ShareService.ValidateShareSearchRequestResult validationResult = createSearchValidationResultWithErrors();

        //when
        expectedException.expect(IllegalStateException.class);
        shareService.shareSearchRequest(validationResult);

        verifyNoMoreInteractions(shareSearchRequestService);
    }

    private ShareService.ValidateShareSearchRequestResult createSearchValidationResultWithErrors()
    {
        final ShareService.ValidateShareSearchRequestResult validationResult = createSearchValidationResult(user, null, null);
        validationResult.getErrorCollection().addError(ShareServiceImpl.NO_PERMISSION_TO_BROWSE_USERS, NO_PERMISSION_TO_BROWSE_USERS_ERROR);
        return validationResult;
    }

    private ShareService.ValidateShareIssueResult getShareIssueValidationResultWithErrors()
    {
        final ShareService.ValidateShareIssueResult validationResult = getShareIssueValidationResult(user, null, null);
        validationResult.getErrorCollection().addError(ShareServiceImpl.NO_PERMISSION_TO_BROWSE_USERS, NO_PERMISSION_TO_BROWSE_USERS_ERROR);
        return validationResult;
    }

    private void mockI18nHelper()
    {
        when(i18nHelper.getText(ShareServiceImpl.NO_USERS_OR_EMAILS_PROVIDED))
                .thenReturn(NO_USERS_OR_MAILS_ERROR);

        when(i18nHelper.getText(ShareServiceImpl.NO_PERMISSION_TO_BROWSE_USERS))
                .thenReturn(NO_PERMISSION_TO_BROWSE_USERS_ERROR);
    }
}
