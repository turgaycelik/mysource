package com.atlassian.jira.plugin.link.confluence;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.mock.component.MockComponentWorker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestConfluenceRemoteIssueLinkBuilder
{
    private ConfluenceRemoteIssueLinkBuilder builder;

    @Before
    public void setUp()
    {
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.addMock(ApplicationProperties.class, applicationPropertiesWithAnyEncoding());
        componentWorker.init();

        builder = new ConfluenceRemoteIssueLinkBuilder();
    }

    @Test
    public void buildReturnsARemoteIssueLinkCorrectlyConfigured()
    {
        String pageId = "aPageId";
        Long issueId = (long) 1;
        String appLinkId = UUID.randomUUID().toString();
        String appLinkName = "applicationLinkName";
        String appLinkRpcUrl = "http://test.com/";

        ApplicationLink appLink = applicationLinkWith(appLinkId, appLinkName, appLinkRpcUrl);
        RemoteIssueLink remoteIssueLink = builder.build(appLink, pageId, issueId);

        assertThat(remoteIssueLink.getIssueId(), is(issueId));
        assertThat(remoteIssueLink.getUrl(), is(appLinkRpcUrl + "pages/viewpage.action?pageId=" + pageId));
        assertThat(remoteIssueLink.getTitle(), is("Wiki Page"));
        assertThat(remoteIssueLink.getGlobalId(), is("appId=" + appLinkId + "&pageId=" + pageId));
        assertThat(remoteIssueLink.getRelationship(), is("Wiki Page"));
        assertThat(remoteIssueLink.getApplicationType(), is(RemoteIssueLink.APPLICATION_TYPE_CONFLUENCE));
        assertThat(remoteIssueLink.getApplicationName(), is(appLinkName));
    }

    @Test
    public void buildEncodesThePageIdOnTheUrl()
    {
        String pageId = "a page id";
        String encodedPageId = "a+page+id";

        RemoteIssueLink remoteIssueLink = builder.build(anyApplicationLink(), pageId, anyIssueId());

        assertThat(remoteIssueLink.getUrl(), containsString(encodedPageId));
    }

    @Test
    public void buildEncodesThePageIdOnTheGlobalId()
    {
        String pageId = "a page id";
        String encodedPageId = "a+page+id";

        RemoteIssueLink remoteIssueLink = builder.build(anyApplicationLink(), pageId, anyIssueId());

        assertThat(remoteIssueLink.getGlobalId(), containsString(encodedPageId));
    }

    private Long anyIssueId()
    {
        return 1L;
    }

    private ApplicationLink anyApplicationLink()
    {
        return applicationLinkWith(UUID.randomUUID().toString(), "", "");

    }

    private ApplicationLink applicationLinkWith(final String appLinkId, final String name, final String rpcUrl)
    {
        ApplicationLink appLink = mock(ApplicationLink.class);
        when(appLink.getId()).thenReturn(new ApplicationId(appLinkId));
        when(appLink.getName()).thenReturn(name);
        when(appLink.getRpcUrl()).thenReturn(uriFrom(rpcUrl));
        return appLink;
    }

    private URI uriFrom(final String asciiString)
    {
        URI uri = null;
        try
        {
            uri = new URI(asciiString);
        }
        catch (URISyntaxException e)
        {
            fail("We need to use a valid uri");
        }
        return uri;
    }

    private ApplicationProperties applicationPropertiesWithAnyEncoding()
    {
        ApplicationProperties applicationProperties = mock(ApplicationProperties.class);
        when(applicationProperties.getEncoding()).thenReturn("UTF-8");
        return applicationProperties;
    }
}
