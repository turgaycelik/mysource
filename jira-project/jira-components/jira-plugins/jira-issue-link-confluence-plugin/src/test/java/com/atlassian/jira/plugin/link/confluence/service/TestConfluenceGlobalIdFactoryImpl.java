package com.atlassian.jira.plugin.link.confluence.service;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.plugin.link.confluence.ConfluenceGlobalId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import static com.atlassian.jira.plugin.link.confluence.service.ConfluenceGlobalIdFactoryImpl.encode;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.stub;

@RunWith(MockitoJUnitRunner.class)
public class TestConfluenceGlobalIdFactoryImpl
{
    private ConfluenceGlobalIdFactoryImpl factory;

    @Mock
    private ApplicationLinkService service;

    @Mock
    private ApplicationLink applicationLink1;
    
    @Mock
    private ApplicationLink applicationLink2;
    
    @Mock
    private ApplicationLink applicationLink3;

    private ApplicationId wrongId = new ApplicationId("0396313d-4c02-4754-b031-6cfc1f3f4381");
    
    @Before
    public void setup()
    {
        stub(applicationLink1.getDisplayUrl()).toReturn(URI.create("http://www.example.com/more"));
        stub(applicationLink1.getId()).toReturn(new ApplicationId("0396313d-4c02-4754-b031-6cfc1f3f4380"));

        stub(applicationLink2.getDisplayUrl()).toReturn(URI.create("http://www.example.com/more/andmore"));
        stub(applicationLink2.getId()).toReturn(new ApplicationId("7b40cf76-1e82-481a-9057-66bb675b6dd1"));

        stub(applicationLink3.getDisplayUrl()).toReturn(URI.create("http://www.example.com"));
        stub(applicationLink3.getId()).toReturn(new ApplicationId("87a55663-f4f7-40e6-98f9-682c47f624b9"));

        stub(service.getApplicationLinks(ConfluenceApplicationType.class))
                .toReturn(asList(applicationLink1, applicationLink2, applicationLink3));

        factory = new ConfluenceGlobalIdFactoryImpl(service);
    }

    @Test
    public void testCreateWithValidApplicationId()
    {
        final String pageId = "12345";
        final RemoteIssueLink issueLink = new RemoteIssueLinkBuilder()
                .globalId(encode(applicationLink3.getId(), pageId))
                .url(applicationLink1.getDisplayUrl().toASCIIString()).build();

        final ConfluenceGlobalId globalId = factory.create(issueLink);

        assertThat(globalId.getPageId(), equalTo(pageId));
        assertThat(globalId.getApplicationLink(), is(applicationLink3));
    }
    
    @Test
    public void testCreateWithInValidApplicationIdButValidUrl()
    {
        final String pageId = "12345";
        final RemoteIssueLink issueLink = new RemoteIssueLinkBuilder()
                .globalId(encode(wrongId, pageId))
                .url(applicationLink1.getDisplayUrl().toASCIIString() + "/something/else")
                .build();

        final ConfluenceGlobalId globalId = factory.create(issueLink);

        assertThat(globalId.getPageId(), equalTo(pageId));
        assertThat(globalId.getApplicationLink(), is(applicationLink1));
    }

    @Test
    public void testCreateWithInValidApplicationIdButValidUrlMultiple()
    {
        final String pageId = "12345";
        final RemoteIssueLink issueLink = new RemoteIssueLinkBuilder()
                .globalId(encode(wrongId, pageId))
                .url(applicationLink2.getDisplayUrl().toASCIIString() +  "/something/else")
                .build();

        final ConfluenceGlobalId globalId = factory.create(issueLink);

        assertThat(globalId.getPageId(), equalTo(pageId));
        assertThat(globalId.getApplicationLink(), is(applicationLink2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNothingMatches()
    {
        final String pageId = "12345";
        final RemoteIssueLink issueLink = new RemoteIssueLinkBuilder()
                .globalId(encode(wrongId, pageId))
                .url("www.example.biz").build();

        factory.create(issueLink);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBadUrl()
    {
        final String pageId = "12345";
        final RemoteIssueLink issueLink = new RemoteIssueLinkBuilder()
                .globalId(encode(wrongId, pageId))
                .url("1234://example.com:3848").build();

        factory.create(issueLink);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNoUrl()
    {
        final String pageId = "12345";
        final RemoteIssueLink issueLink = new RemoteIssueLinkBuilder()
                .globalId(encode(wrongId, pageId))
                .build();

        factory.create(issueLink);
    }
}
