package com.atlassian.jira.plugin.link.confluence;

import com.atlassian.applinks.api.ApplicationLink;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestConfluencePageUrl
{
    @Test(expected = IllegalArgumentException.class)
    public void buildThrowsExceptionIfUrlIsNotSyntacticallyCorrect()
    {
        ApplicationLink anyAppLink = null;
        String malformedUrl = "\\";

        ConfluencePageUrl.build(malformedUrl, anyAppLink);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void buildThrowsExceptionIfUrlIsNotBasedOnTheAppLinkDisplayOrRpcUrls()
    {
        ApplicationLink appLink = applicationLinkWith("http://display.com", "http://rpc.com");
        String pageUrl = "http://different-base.com";

        ConfluencePageUrl.build(pageUrl, appLink).getUrlRebasedToRpcUrl();
    }

    @Test
    public void getUrlRebasedToRpcUrlReturnsOriginalPageUrlIfItWasAlreadyBasedOnRpcUrl()
    {
        String rpcUrl = "http://rpc.com";
        String displayUrl = "http://display.com";
        ApplicationLink appLink = applicationLinkWith(displayUrl, rpcUrl);

        String pageUrl = rpcUrl + "/some/path";
        String rebasedUrl = ConfluencePageUrl.build(pageUrl, appLink).getUrlRebasedToRpcUrl();

        assertThat(rebasedUrl, is(pageUrl));
    }

    @Test
    public void getUrlRebasedToRpcUrlReturnsPageUrlRebasedToRpcUrlIfPageUrlStartsWithDisplayUrl()
    {
        String rpcUrl = "http://rpc.com";
        String displayUrl = "http://display.com";
        ApplicationLink appLink = applicationLinkWith(displayUrl, rpcUrl);

        String pageUrl = displayUrl + "/some/path";
        String rebasedUrl = ConfluencePageUrl.build(pageUrl, appLink).getUrlRebasedToRpcUrl();

        assertThat(rebasedUrl, is(rpcUrl + "/some/path"));
    }

    private ApplicationLink applicationLinkWith(final String displayUrl, final String rpcUrl)
    {
        ApplicationLink appLink = mock(ApplicationLink.class);
        when(appLink.getDisplayUrl()).thenReturn(uriWith(displayUrl));
        when(appLink.getRpcUrl()).thenReturn(uriWith(rpcUrl));
        return appLink;
    }

    private URI uriWith(final String uri)
    {
        try
        {
            return new URI(uri);
        }
        catch (URISyntaxException e)
        {
            fail("We need an uri that is syntactically correct");
            return null;
        }
    }
}
