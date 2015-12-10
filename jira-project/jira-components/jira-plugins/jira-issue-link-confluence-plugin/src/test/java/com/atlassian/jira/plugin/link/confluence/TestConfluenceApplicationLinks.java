package com.atlassian.jira.plugin.link.confluence;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;
import com.atlassian.fugue.Option;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestConfluenceApplicationLinks
{
    @Mock
    private ApplicationLinkService applicationLinksService;

    private ConfluenceApplicationLinks confluenceApplicationLinks;

    @Before
    public void setUp()
    {
        confluenceApplicationLinks = new ConfluenceApplicationLinks(applicationLinksService);
    }

    @Test
    public void getAppLinksDelegatesOnApplicationLinkService()
    {
        Collection<ApplicationLink> expectedLinks = new ArrayList<ApplicationLink>();
        when(applicationLinksService.getApplicationLinks(ConfluenceApplicationType.class)).thenReturn(expectedLinks);

        Collection<ApplicationLink> actualLinks = confluenceApplicationLinks.getAppLinks();

        assertThat(actualLinks, is(expectedLinks));
    }

    @Test
    public void getAppLinkWhenThereAreNoAppLinksToConfluenceShouldReturnAnEmptyResult()
    {
        List<ApplicationLink> appLinks = Collections.emptyList();
        when(applicationLinksService.getApplicationLinks(ConfluenceApplicationType.class)).thenReturn(appLinks);

        Option<ApplicationLink> appLink = confluenceApplicationLinks.forPage(anyPageUri());

        assertFalse(appLink.isDefined());
    }

    @Test
    public void getAppLinkWhenThereAreNoAppLinksMatchingThePageUriByDisplayNorRpcUrlsShouldReturnEmpty()
    {
        List<ApplicationLink> appLinks = Arrays.asList(
                appLinkWith("http://display1.com", "http://rpc1.com"),
                appLinkWith("http://display2.com", "http://rpc2.com")
        );
        when(applicationLinksService.getApplicationLinks(ConfluenceApplicationType.class)).thenReturn(appLinks);

        Option<ApplicationLink> appLink = confluenceApplicationLinks.forPage(uriWith("http://url.com"));

        assertFalse(appLink.isDefined());
    }

    @Test
    public void getAppLinkWhenThereIsOneAppLinkMatchingThePageUriByDisplayUrlShouldReturnThatApplink()
    {
        List<ApplicationLink> appLinks = Arrays.asList(
                appLinkWith("http://display1.com", "http://rpc1.com"),
                appLinkWith("http://display2.com", "http://rpc2.com")
        );
        when(applicationLinksService.getApplicationLinks(ConfluenceApplicationType.class)).thenReturn(appLinks);

        Option<ApplicationLink> actualAppLink = confluenceApplicationLinks.forPage(uriWith("http://display2.com"));

        assertTrue(actualAppLink.isDefined());
        assertThat(actualAppLink.get(), is(appLinks.get(1)));
    }

    @Test
    public void getAppLinkWhenThereIsOneAppLinkMatchingThePageUriByRpcUrlShouldReturnThatAppLink()
    {
        List<ApplicationLink> appLinks = Arrays.asList(
                appLinkWith("http://display1.com", "http://rpc1.com"),
                appLinkWith("http://display2.com", "http://rpc2.com")
        );
        when(applicationLinksService.getApplicationLinks(ConfluenceApplicationType.class)).thenReturn(appLinks);

        Option<ApplicationLink> actualAppLink = confluenceApplicationLinks.forPage(uriWith("http://rpc2.com"));

        assertTrue(actualAppLink.isDefined());
        assertThat(actualAppLink.get(), is(appLinks.get(1)));
    }
    
    @Test
    public void getAppLinkWhenThereAreSeveralAppLinksMatchingThePageUriByDisplayUrlShouldReturnTheFirstMatchingAppLinkIfThereIsNoPrimaryLink()
    {
        List<ApplicationLink> appLinks = Arrays.asList(
                appLinkWith("http://display.com", "http://rpc1.com"),
                appLinkWith("http://display.com", "http://rpc2.com"),
                appLinkWith("http://display.com", "http://rpc3.com")
        );
        when(applicationLinksService.getApplicationLinks(ConfluenceApplicationType.class)).thenReturn(appLinks);

        Option<ApplicationLink> actualAppLink = confluenceApplicationLinks.forPage(uriWith("http://display.com"));

        assertTrue(actualAppLink.isDefined());
        assertThat(actualAppLink.get(), is(appLinks.get(0)));
    }

    @Test
    public void getAppLinkWhenThereAreSeveralAppLinksMatchingThePageUriByDisplayUrlShouldReturnThePrimaryLinkIfThereIsOne()
    {
        ApplicationLink expectedAppLink = primaryAppLinkWith("http://display.com", "http://rpc3.com");
        List<ApplicationLink> appLinks = Arrays.asList(
                appLinkWith("http://display.com", "http://rpc1.com"),
                appLinkWith("http://display.com", "http://rpc2.com"),
                expectedAppLink
        );
        when(applicationLinksService.getApplicationLinks(ConfluenceApplicationType.class)).thenReturn(appLinks);

        Option<ApplicationLink> actualAppLink = confluenceApplicationLinks.forPage(uriWith("http://display.com"));

        assertTrue(actualAppLink.isDefined());
        assertThat(actualAppLink.get(), is(expectedAppLink));
    }

    @Test
    public void getAppLinkWhenThereAreSeveralAppLinksMatchingThePageUriByRpcUrlShouldReturnTheFirstMatchingAppLinkIfThereIsNoPrimaryLink()
    {
        List<ApplicationLink> appLinks = Arrays.asList(
                appLinkWith("http://display1.com", "http://rpc.com"),
                appLinkWith("http://display2.com", "http://rpc.com"),
                appLinkWith("http://display3.com", "http://rpc.com")
        );
        when(applicationLinksService.getApplicationLinks(ConfluenceApplicationType.class)).thenReturn(appLinks);

        Option<ApplicationLink> actualAppLink = confluenceApplicationLinks.forPage(uriWith("http://rpc.com"));

        assertTrue(actualAppLink.isDefined());
        assertThat(actualAppLink.get(), is(appLinks.get(0)));
    }

    @Test
    public void getAppLinkWhenThereAreSeveralAppLinksMatchingThePageUriByRpcUrlShouldReturnThePrimaryLinkIfThereIsOne()
    {
        ApplicationLink expectedAppLink = primaryAppLinkWith("http://display3.com", "http://rpc.com");
        List<ApplicationLink> appLinks = Arrays.asList(
                appLinkWith("http://display1.com", "http://rpc.com"),
                appLinkWith("http://display2.com", "http://rpc.com"),
                expectedAppLink
        );
        when(applicationLinksService.getApplicationLinks(ConfluenceApplicationType.class)).thenReturn(appLinks);

        Option<ApplicationLink> actualAppLink = confluenceApplicationLinks.forPage(uriWith("http://rpc.com"));

        assertTrue(actualAppLink.isDefined());
        assertThat(actualAppLink.get(), is(expectedAppLink));
    }

    private ApplicationLink appLinkWith(final String displayUri, final String rpcUri)
    {
        return mockApplicationLink(displayUri, rpcUri, false);
    }

    private ApplicationLink primaryAppLinkWith(final String displayUri, final String rpcUri)
    {
        return mockApplicationLink(displayUri, rpcUri, true);
    }

    private ApplicationLink mockApplicationLink(final String displayUri, final String rpcUri, final boolean isPrimary)
    {
        ApplicationLink appLink = mock(ApplicationLink.class);
        when(appLink.getDisplayUrl()).thenReturn(uriWith(displayUri));
        when(appLink.getRpcUrl()).thenReturn(uriWith(rpcUri));
        when(appLink.isPrimary()).thenReturn(isPrimary);
        return appLink;
    }

    private URI anyPageUri()
    {
        return uriWith("http://anything.com");
    }

    private URI uriWith(final String uri)
    {
        try
        {
            return new URI(uri);
        }
        catch (URISyntaxException e)
        {
            fail("We need a syntactically valid URI");
            return null;
        }
    }
}
