package com.atlassian.jira.plugin.link.remotejira;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.plugin.link.applinks.RemoteResponse;
import com.atlassian.jira.plugin.link.remotejira.RemoteJiraRestService.RestVersion;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.sal.api.net.ResponseException;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JiraRemoteIssueLinkDecoratingServiceImpl}.
 *
 * @since v5.0
 */
public class JiraRemoteIssueLinkDecoratingServiceImplTest
{
    private static final String BASE_URL = "http://jira.example.com";
    private static final Long ID = 42L;
    private static final Long ISSUE_ID = 101L;
    private static final String ISSUE_KEY = "PROJ-123";
    private static final String GLOBAL_ID = "I'm forever blowing bubbles\nPretty bubbles in the air...";
    private static final String TITLE = ISSUE_KEY;
    private static final String SUMMARY = "Needs moar web 2.0!";
    private static final String URL = BASE_URL + "/browse/PROJ-123";
    private static final String ICON_URL = BASE_URL + "/old_icon.png";
    private static final String ICON_TITLE = "Old Issue Type";
    private static final String RELATIONSHIP = "is having a passionate affair with";
    private static final Boolean RESOLVED = false;
    private static final String STATUS_ICON_URL = BASE_URL + "/old_status_icon.png";
    private static final String STATUS_ICON_TITLE = "Old Status";
    private static final String STATUS_ICON_LINK = BASE_URL + "/browse/PROJ-123/history";
    private static final String APPLICATION_TYPE = "com.atlassian.jira";
    private static final String APPLICATION_NAME = "Mythical JIRA Instance";

    private static final String UPDATED_SUMMARY = "Updated summary";
    private static final String UPDATED_URL = BASE_URL + "/browse/BLAH-789";
    private static final String UPDATED_ICON_URL = BASE_URL + "/updated_icon.png";
    private static final String UPDATED_ICON_TITLE = "Updated Issue Type";
    private static final Boolean UPDATED_RESOLVED = true;
    private static final String UPDATED_STATUS_ICON_URL = BASE_URL + "/updated_status_icon.png";
    private static final String UPDATED_STATUS_ICON_TITLE = "Updated Status";
    private static final String UPDATED_APPLICATION_NAME = "Updated JIRA Instance";

    private static URI uri(final String uri)
    {
        try
        {
            return new URI(uri);
        }
        catch (final URISyntaxException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    private static void assertRemoteLinkDecorated(RemoteIssueLink jiraRemoteLink)
    {
        assertEquals(ID, jiraRemoteLink.getId());
        assertEquals(ISSUE_ID, jiraRemoteLink.getIssueId());
        assertEquals(GLOBAL_ID, jiraRemoteLink.getGlobalId());
        assertEquals(TITLE, jiraRemoteLink.getTitle());
        assertEquals(UPDATED_SUMMARY, jiraRemoteLink.getSummary());
        assertEquals(UPDATED_URL, jiraRemoteLink.getUrl());
        assertEquals(UPDATED_ICON_URL, jiraRemoteLink.getIconUrl());
        assertEquals(UPDATED_ICON_TITLE, jiraRemoteLink.getIconTitle());
        assertEquals(RELATIONSHIP, jiraRemoteLink.getRelationship());
        assertEquals(UPDATED_RESOLVED, jiraRemoteLink.isResolved());
        assertEquals(UPDATED_STATUS_ICON_URL, jiraRemoteLink.getStatusIconUrl());
        assertEquals(UPDATED_STATUS_ICON_TITLE, jiraRemoteLink.getStatusIconTitle());
        assertEquals(STATUS_ICON_LINK, jiraRemoteLink.getStatusIconLink());
        assertEquals(APPLICATION_TYPE, jiraRemoteLink.getApplicationType());
        assertEquals(UPDATED_APPLICATION_NAME, jiraRemoteLink.getApplicationName());
    }

    private final JiraAuthenticationContext authContext = mock(JiraAuthenticationContext.class);
    private final RemoteJiraRestService remoteJiraRestService = mock(RemoteJiraRestService.class);
    private final RemoteIssueLink remoteIssueLink = new RemoteIssueLink(ID, ISSUE_ID, GLOBAL_ID, TITLE, SUMMARY, URL, ICON_URL, ICON_TITLE, RELATIONSHIP, RESOLVED, STATUS_ICON_URL, STATUS_ICON_TITLE, STATUS_ICON_LINK, APPLICATION_TYPE, APPLICATION_NAME);
    private final ApplicationLink applicationLink = mock(ApplicationLink.class);
    @SuppressWarnings ( { "unchecked" })
    private final RemoteResponse<RemoteJiraIssue> restResponse = mock(RemoteResponse.class);
    private final RemoteJiraGlobalIdFactoryImpl remoteJiraGlobalIdFactory = mock(RemoteJiraGlobalIdFactoryImpl.class);
    private final RemoteJiraGlobalId remoteJiraGlobalId = mock(RemoteJiraGlobalId.class);
    private final ComponentAccessor.Worker componentAccessorWorker = mock(ComponentAccessor.Worker.class);

    {
        ComponentAccessor.initialiseWorker(componentAccessorWorker);
        when(componentAccessorWorker.getComponent(RemoteJiraGlobalIdFactoryImpl.class)).thenReturn(remoteJiraGlobalIdFactory);
    }

    private final JiraRemoteIssueLinkDecoratingServiceImpl jiraRemoteLinkDecoratingService = new JiraRemoteIssueLinkDecoratingServiceImpl(remoteJiraRestService, remoteJiraGlobalIdFactory, authContext);

    @Test
    public void testDecorateUsingAppLinks() throws Exception
    {
        when(remoteJiraGlobalIdFactory.decode(GLOBAL_ID)).thenReturn(remoteJiraGlobalId);
        when(remoteJiraGlobalId.getApplicationLink()).thenReturn(applicationLink);
        when(remoteJiraGlobalId.getRemoteIssueId()).thenReturn(ISSUE_ID);
        when(applicationLink.getName()).thenReturn(UPDATED_APPLICATION_NAME);
        when(applicationLink.getDisplayUrl()).thenReturn(uri(BASE_URL));
        when(applicationLink.getRpcUrl()).thenReturn(uri(BASE_URL));
        when(remoteJiraRestService.getIssue(applicationLink, String.valueOf(ISSUE_ID), RestVersion.VERSION_2)).thenReturn(restResponse);
        expectSuccessfulResponse();

        final RemoteIssueLink jiraRemoteIssueLink = jiraRemoteLinkDecoratingService.decorate(remoteIssueLink);

        assertRemoteLinkDecorated(jiraRemoteIssueLink);
    }

    private void expectSuccessfulResponse() throws ResponseException
    {
        when(restResponse.isSuccessful()).thenReturn(true);

        final RemoteJiraIssue remoteJiraIssue = new RemoteJiraIssueBuilder()
                .id(ISSUE_ID)
                .key(ISSUE_KEY)
                .summary(UPDATED_SUMMARY)
                .iconUrl(UPDATED_ICON_URL)
                .iconTitle(UPDATED_ICON_TITLE)
                .statusIconUrl(UPDATED_STATUS_ICON_URL)
                .statusIconTitle(UPDATED_STATUS_ICON_TITLE)
                .resolved(true)
                .browseUrl(UPDATED_URL)
                .build();

         when(restResponse.getEntity()).thenReturn(remoteJiraIssue);
    }
}
