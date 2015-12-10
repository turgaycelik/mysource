package com.atlassian.jira.plugin.webfragment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.web.api.DynamicWebInterfaceManager;
import com.atlassian.plugin.web.api.model.WebFragmentBuilder;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 */
@RunWith (ListeningMockitoRunner.class)
public class TestDefaultSimpleLinkManager
{
    @Mock
    private DynamicWebInterfaceManager dynamicWebInterfaceManager;
    @Mock
    private SimpleLinkFactoryModuleDescriptors simpleLinkFactoryModuleDescriptors;
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    private VelocityRequestContextFactory velocityRequestContextFactory;
    @Mock
    private User user;
    @Mock
    private WebResourceUrlProvider webResourceUrlProvider;
    private JiraHelper jiraHelper;
    private DefaultSimpleLinkManager simpleLinkManager;

    @Before
    public void setUp() throws Exception
    {
        simpleLinkManager = new DefaultSimpleLinkManager(dynamicWebInterfaceManager, simpleLinkFactoryModuleDescriptors, jiraAuthenticationContext, webResourceUrlProvider, velocityRequestContextFactory);
        jiraHelper = new JiraHelper();
    }

    @Test
    public void testGetLinksWithNoIdUsesKey() throws Exception
    {
        when(dynamicWebInterfaceManager.getDisplayableWebItems(eq("section"), anyMap())).thenReturn(Arrays.asList(new WebFragmentBuilder(0).id("mykey").webItem("").url("http://url").build()));
        when(simpleLinkFactoryModuleDescriptors.get()).thenReturn(Collections.<SimpleLinkFactoryModuleDescriptor>emptyList());
        when(webResourceUrlProvider.getStaticResourcePrefix("NONE", UrlMode.RELATIVE)).thenReturn("/s/none/_/");

        List<SimpleLink> links = simpleLinkManager.getLinksForSection("section", user, jiraHelper);
        assertEquals(1, links.size());
        assertEquals("You have broken something Studio depends on (JRADEV-6587)", "mykey", links.get(0).getId());
    }

    @Test
    public void testGetLinksWithId() throws Exception
    {
        when(dynamicWebInterfaceManager.getDisplayableWebItems(eq("section"), anyMap())).thenReturn(Arrays.asList(new WebFragmentBuilder("mykey", 0).id("myid").webItem("").url("http://url").build()));
        when(simpleLinkFactoryModuleDescriptors.get()).thenReturn(Collections.<SimpleLinkFactoryModuleDescriptor>emptyList());
        when(webResourceUrlProvider.getStaticResourcePrefix("NONE", UrlMode.RELATIVE)).thenReturn("/s/none/_/");

        List<SimpleLink> links = simpleLinkManager.getLinksForSection("section", user, jiraHelper);
        assertEquals(1, links.size());
        assertEquals("myid", links.get(0).getId());
    }

    @Test
    public void testIconLinkPrefix() throws Exception
    {
        when(dynamicWebInterfaceManager.getDisplayableWebItems(eq("section"), anyMap())).thenReturn(Arrays.asList(
                new WebFragmentBuilder("mykey", 0).id("myid").addParam("iconUrl", "blah").webItem("").url("http://url").build()));
        when(simpleLinkFactoryModuleDescriptors.get()).thenReturn(Collections.<SimpleLinkFactoryModuleDescriptor>emptyList());
        when(webResourceUrlProvider.getStaticResourcePrefix(UrlMode.RELATIVE)).thenReturn("/s/a/b/c/_/");

        List<SimpleLink> links = simpleLinkManager.getLinksForSection("section", user, jiraHelper, true);
        assertEquals(1, links.size());
        assertEquals("myid", links.get(0).getId());
        assertEquals("/s/a/b/c/_/blah", links.get(0).getIconUrl());

        VelocityRequestContext vrc = mock(VelocityRequestContext.class);
        when(vrc.getBaseUrl()).thenReturn("thebaseurl/");
        when(velocityRequestContextFactory.getJiraVelocityRequestContext()).thenReturn(vrc);

        links = simpleLinkManager.getLinksForSection("section", user, jiraHelper, false);
        assertEquals(1, links.size());
        assertEquals("myid", links.get(0).getId());
        assertEquals("thebaseurl/blah", links.get(0).getIconUrl());
    }

    @Test
    public void testIconLinkPrefixBaseUrlNotDuplicated() throws Exception
    {
        when(dynamicWebInterfaceManager.getDisplayableWebItems(eq("section"), anyMap())).thenReturn(Arrays.asList(
                new WebFragmentBuilder("mykey", 0).id("myid").addParam("iconUrl", "thebaseurl/blah").webItem("").url("http://url").build()));
        when(simpleLinkFactoryModuleDescriptors.get()).thenReturn(Collections.<SimpleLinkFactoryModuleDescriptor>emptyList());
        when(webResourceUrlProvider.getStaticResourcePrefix(UrlMode.RELATIVE)).thenReturn("/s/a/b/c/_/");

        VelocityRequestContext vrc = mock(VelocityRequestContext.class);
        when(vrc.getBaseUrl()).thenReturn("thebaseurl/");
        when(velocityRequestContextFactory.getJiraVelocityRequestContext()).thenReturn(vrc);

        final List<SimpleLink> links = simpleLinkManager.getLinksForSection("section", user, jiraHelper, false);
        assertEquals(1, links.size());
        assertEquals("myid", links.get(0).getId());
        assertEquals("thebaseurl/blah", links.get(0).getIconUrl());
    }

}
