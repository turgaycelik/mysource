package com.atlassian.jira.plugins.share.rest;

import java.util.Set;

import javax.ws.rs.core.Response;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.plugins.share.ShareBean;
import com.atlassian.jira.plugins.share.ShareService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TestShareResource
{
    @Rule
    public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);
    @Rule
    public final InitMockitoMocks initMockitoMocks = new InitMockitoMocks(this);

    private ShareResource shareResource;

    @Mock
    private JiraAuthenticationContext authenticationContext;
    @Mock
    private SearchRequestService searchRequestService;
    @Mock
    private ShareService shareService;
    @Mock
    private IssueService issueService;
    @Mock
    private ApplicationUser user;
    @Mock
    private User directoryUser;
    @Mock
    private UserPreferencesManager userPreferencesManager;

    private ShareBean shareBean;


    @Before
    public void setUp()
    {
        shareResource = new ShareResource(
                authenticationContext,
                userPreferencesManager,
                searchRequestService,
                shareService,
                issueService);

        initShareBean();

        when(authenticationContext.getUser())
                .thenReturn(user);

        when(user.getDirectoryUser())
                .thenReturn(directoryUser);
    }

    private void initShareBean()
    {
        final Set<String> usernames = Sets.newHashSet();
        final Set<String> emails = Sets.newHashSet();
        final String message = "message";
        final String jql = "jql";
        this.shareBean = new ShareBean(usernames, emails, message, jql);
    }

    @Test
    public void shouldShareSearch()
    {
        //given
        final SearchRequest searchRequest = null;
        final ShareService.ValidateShareSearchRequestResult validationResult = mock(ShareService.ValidateShareSearchRequestResult.class);
        when(shareService.validateShareSearchRequest(user, shareBean, searchRequest))
                .thenReturn(validationResult);

        when(validationResult.isValid())
                .thenReturn(true);

        //when
        final Response response = shareResource.shareSearch(shareBean);

        //then
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        verify(shareService).shareSearchRequest(validationResult);
    }

    @Test
    public void shouldNotShareBecauseOfValidationFail()
    {
        //given
        final ShareService.ValidateShareSearchRequestResult validationResult = mock(ShareService.ValidateShareSearchRequestResult.class);
        when(shareService.validateShareSearchRequest(eq(user), eq(shareBean), isNull(SearchRequest.class)))
                .thenReturn(validationResult);

        when(validationResult.isValid())
                .thenReturn(false);

        when(validationResult.getErrorCollection())
                .thenReturn(new SimpleErrorCollection());

        //when
        final Response response = shareResource.shareSearch(shareBean);

        //then
        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        verify(shareService).validateShareSearchRequest(eq(user), eq(shareBean), isNull(SearchRequest.class));
        verifyNoMoreInteractions(shareService);
    }

    @Test
    public void shouldShareSearchRequest()
    {
        //given
        final long id = 123123l;
        final SearchRequest searchRequest = mock(SearchRequest.class);
        final ShareService.ValidateShareSearchRequestResult validationResult = mock(ShareService.ValidateShareSearchRequestResult.class);

        when(searchRequestService.getFilter(any(JiraServiceContext.class), eq(id)))
                .thenReturn(searchRequest);

        when(shareService.validateShareSearchRequest(user, shareBean, searchRequest))
                .thenReturn(validationResult);

        when(validationResult.isValid())
                .thenReturn(true);

        //when
        final Response response = shareResource.shareSearchRequest(id, shareBean);

        //then
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        verify(shareService).shareSearchRequest(validationResult);
    }

    @Test
    public void shouldReturnBadRequestWhenValidationOfSearchRequestFail()
    {
        //given
        final long id = 123123l;
        final SearchRequest searchRequest = mock(SearchRequest.class);
        final ShareService.ValidateShareSearchRequestResult validationResult = mock(ShareService.ValidateShareSearchRequestResult.class);

        when(searchRequestService.getFilter(any(JiraServiceContext.class), eq(id)))
                .thenReturn(searchRequest);

        when(shareService.validateShareSearchRequest(user, shareBean, searchRequest))
                .thenReturn(validationResult);

        when(validationResult.isValid())
                .thenReturn(false);

        when(validationResult.getErrorCollection())
                .thenReturn(new SimpleErrorCollection());

        //when
        final Response response = shareResource.shareSearchRequest(id, shareBean);

        //then
        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        verify(shareService, times(0)).shareSearchRequest(validationResult);
    }

    @Test
    public void shouldReturnBadRequestWhenFilterWithGivenIdNotExist()
    {
        //given
        final long id = 123123l;
        final SearchRequest searchRequest = null;
        final ShareService.ValidateShareSearchRequestResult validationResult = mock(ShareService.ValidateShareSearchRequestResult.class);

        final JiraServiceContext context = mock(JiraServiceContext.class);

        when(searchRequestService.getFilter(any(JiraServiceContext.class), eq(id)))
                .thenReturn(searchRequest);

        when(context.getErrorCollection())
                .thenReturn(new SimpleErrorCollection());

        //when
        final Response response = shareResource.shareSearchRequest(id, shareBean);

        //then
        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        verify(shareService, times(0)).shareSearchRequest(validationResult);
    }

    @Test
    public void shareIssueSuccessful()
    {
        //given
        final String issueKey = "issueKey";

        final IssueService.IssueResult issueResult = expectValidIssueResult(issueKey);
        final ShareService.ValidateShareIssueResult validationResult = expectValidShareIssueResult(issueResult);
        expectUserDoNotWantToNotifyOwnChanges(user);

        //when
        final Response response = shareResource.shareIssue(issueKey, shareBean);

        //then
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        verify(shareService).shareIssue(validationResult);
    }

    @Test
    public void shouldFailSharingIssueBecauseOfValidationFailed()
    {
        //given
        final String issueKey = "issueKey";

        final IssueService.IssueResult issueResult = expectValidIssueResult(issueKey);

        final ShareService.ValidateShareIssueResult validationResult = mock(ShareService.ValidateShareIssueResult.class);
        expectUserDoNotWantToNotifyOwnChanges(user);

        when(shareService.validateShareIssue(user, shareBean, issueResult.getIssue()))
                .thenReturn(validationResult);

        when(validationResult.isValid())
                .thenReturn(false);

        when(validationResult.getErrorCollection())
                .thenReturn(new SimpleErrorCollection());

        //when
        final Response response = shareResource.shareIssue(issueKey, shareBean);

        //then
        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        verify(shareService, times(0)).shareIssue(validationResult);
    }

    @Test
    public void shouldConvertUserNameToEmailWhenDoNotWantToNotifyOwnChanges()
    {
        //given
        final String issueKey = "issueKey";

        final IssueService.IssueResult issueResult = expectValidIssueResult(issueKey);
        final ShareService.ValidateShareIssueResult validationResult = expectValidShareIssueResult(issueResult);

        createUserWithNameAndEmail();
        expectUserDoNotWantToNotifyOwnChanges(user);

        shareBean.getUsernames().add(user.getName());

        //when
        final Response response = shareResource.shareIssue(issueKey, shareBean);

        //then
        verify(shareService).shareIssue(validationResult);
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(shareBean.getEmails(), hasItem(user.getEmailAddress()));
        assertThat(shareBean.getUsernames(), not(hasItem(user.getName())));
    }

    @Test
    public void shouldFailSharingIssueBecauseOfIssueResultNotValid()
    {
        //given
        final String issueKey = "issueKey";

        final IssueService.IssueResult issueResult = expectIssueResult(issueKey);

        when(issueResult.isValid())
                .thenReturn(false);

        when(issueResult.getErrorCollection())
                .thenReturn(new SimpleErrorCollection());


        //when
        final Response response = shareResource.shareIssue(issueKey, shareBean);

        //then
        assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        verifyNoMoreInteractions(shareService);
    }

    private void expectUserDoNotWantToNotifyOwnChanges(final ApplicationUser user)
    {
        final ExtendedPreferences preferences = mock(ExtendedPreferences.class);
        when(userPreferencesManager.getExtendedPreferences(user))
                .thenReturn(preferences);

        when(preferences.getBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES))
                .thenReturn(false);
    }

    private void createUserWithNameAndEmail()
    {
        String userName = "userName";
        String userMail = "userMail@example.com";
        when(user.getName())
                .thenReturn(userName);

        when(user.getEmailAddress())
                .thenReturn(userMail);
    }

    private IssueService.IssueResult expectValidIssueResult(final String issueKey)
    {
        final IssueService.IssueResult issueResult = expectIssueResult(issueKey);

        when(issueResult.isValid())
                .thenReturn(true);
        return issueResult;
    }

    private ShareService.ValidateShareIssueResult expectValidShareIssueResult(final IssueService.IssueResult issueResult)
    {
        final ShareService.ValidateShareIssueResult validationResult = mock(ShareService.ValidateShareIssueResult.class);
        when(shareService.validateShareIssue(user, shareBean, issueResult.getIssue()))
                .thenReturn(validationResult);

        when(validationResult.isValid())
                .thenReturn(true);
        return validationResult;
    }

    private IssueService.IssueResult expectIssueResult(final String issueKey)
    {
        final IssueService.IssueResult issueResult = mock(IssueService.IssueResult.class);
        when(issueService.getIssue(user.getDirectoryUser(), issueKey))
                .thenReturn(issueResult);

        final MutableIssue issue = mock(MutableIssue.class);
        when(issueResult.getIssue())
                .thenReturn(issue);
        return issueResult;
    }


}
