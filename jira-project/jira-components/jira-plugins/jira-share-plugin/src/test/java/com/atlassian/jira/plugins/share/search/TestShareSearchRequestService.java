package com.atlassian.jira.plugins.share.search;

import java.util.Map;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.plugins.share.ShareBean;
import com.atlassian.jira.plugins.share.ShareService;
import com.atlassian.jira.plugins.share.event.ShareJqlEvent;
import com.atlassian.jira.plugins.share.event.ShareSearchRequestEvent;
import com.atlassian.jira.user.ApplicationUser;

import com.opensymphony.util.TextUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.atlassian.jira.plugins.share.ShareTestUtil.createSearchValidationResult;
import static com.atlassian.jira.plugins.share.ShareTestUtil.getShareBean;
import static com.atlassian.jira.plugins.share.ShareTestUtil.mockSearchRequest;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestShareSearchRequestService
{
    @Rule
    public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    private ShareSearchRequestService shareSearchRequestService;
    @Mock
    private ShareSearchEmailsSender emailsSender;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    @AvailableInContainer
    private ApplicationProperties applicationProperties;
    @Captor
    private ArgumentCaptor<Map<String, Object>> paramsCaptor;
    @Mock
    private ApplicationUser user;

    private SearchRequest searchRequest = mockSearchRequest();
    private ShareBean shareBean = getShareBean();
    private ShareService.ValidateShareSearchRequestResult searchValidationResult;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        shareSearchRequestService = new ShareSearchRequestService(eventPublisher, emailsSender);

        when(ComponentAccessor.getApplicationProperties().getEncoding())
                .thenReturn("UTF-8");

        searchValidationResult = createSearchValidationResult(user, shareBean, searchRequest);
    }

    @Test
    public void shouldPublishEventWhenJqlShareSent()
    {
        shareSearchRequestService.shareSearchRequest(searchValidationResult);

        verify(eventPublisher).publish(any(ShareJqlEvent.class));
    }

    @Test
    public void shouldPublishEventWhenShareSearchSent()
    {
        searchRequest = new SearchRequest();

        shareSearchRequestService.shareSearchRequest(searchValidationResult);

        verify(eventPublisher).publish(any(ShareSearchRequestEvent.class));
    }

    @Test
    public void shouldSendEmailsWithSearchRequestParams()
    {
        mockSearchRequest();
        createSearchValidationResult(user, shareBean, searchRequest);

        shareSearchRequestService.shareSearchRequest(searchValidationResult);

        verify(emailsSender).sendShareSearchEmails(eq(searchValidationResult), paramsCaptor.capture());
        final Map<String, Object> params = paramsCaptor.getValue();

        assertThat(params, hasEntry("savedSearchLinkUrlParams", (Object) ("?mode=hide&requestId=" + searchRequest.getId())));
        assertThat(params, hasEntry("filterName", (Object) searchRequest.getName()));
        assertThat((String) (params.get("jqlSearchLinkUrlParams")), allOf(
                notNullValue(),
                startsWith("?"),
                containsString("reset=true"),
                containsString("jqlQuery=" + searchRequest.getQuery().getQueryString())));
    }

    @Test
    public void shouldSendEmailsWithJqlParams()
    {
        final ShareService.ValidateShareSearchRequestResult searchValidationResultWithoutSearchRequest = createSearchValidationResult(user, shareBean, null);
        shareSearchRequestService.shareSearchRequest(searchValidationResultWithoutSearchRequest);

        verify(emailsSender).sendShareSearchEmails(eq(searchValidationResultWithoutSearchRequest), paramsCaptor.capture());
        final Map<String, Object> params = paramsCaptor.getValue();

        assertThat((String) (params.get("jqlSearchLinkUrlParams")), allOf(
                notNullValue(),
                startsWith("?"),
                containsString("reset=true"),
                containsString("jqlQuery=" + shareBean.getJql())));
    }

    @Test
    public void shouldSetUserAndMessageParams()
    {
        shareSearchRequestService.shareSearchRequest(searchValidationResult);

        verify(emailsSender).sendShareSearchEmails(eq(searchValidationResult), paramsCaptor.capture());
        final Map<String, Object> params = paramsCaptor.getValue();

        assertThat(params, hasEntry("remoteUser", (Object) user));
        assertThat(params, hasEntry("comment", (Object) shareBean.getMessage()));
        assertThat(params, hasEntry("htmlComment", (Object) TextUtils.htmlEncode(shareBean.getMessage())));
    }


}
