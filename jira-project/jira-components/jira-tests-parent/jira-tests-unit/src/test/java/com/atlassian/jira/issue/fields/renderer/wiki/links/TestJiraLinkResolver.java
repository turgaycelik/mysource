package com.atlassian.jira.issue.fields.renderer.wiki.links;

import java.util.List;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.plugin.contentlinkresolver.ContentLinkResolverDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.renderer.links.ContentLinkResolver;
import com.atlassian.renderer.links.GenericLinkParser;
import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.links.UnresolvedLink;
import com.atlassian.renderer.links.UrlLink;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestJiraLinkResolver
{
    @Mock
    private PluginAccessor mockPluginAccessor;

    @Mock
    private EventPublisher mockEventPublisher;

    private JiraLinkResolver tested;

    @Before
    public void setUpResolver()
    {
        EasyMockAnnotations.initMocks(this);
        mockEventPublisher.register(isA(JiraLinkResolver.LinkResolverProvider.class));
        expectLastCall();
        tested = new JiraLinkResolver(mockPluginAccessor, mockEventPublisher);
    }

    @Test
    public void testBuildContentLinkResolvers()
    {
        final MockContentLinkResolver r1 = new MockContentLinkResolver(null);
        final MockContentLinkResolver r2 = new MockContentLinkResolver(null);
        final MockContentLinkResolver r3 = new MockContentLinkResolver(null);

        List<ContentLinkResolverDescriptor> originalDescriptors = Lists.newArrayList(
                newContentLinkResolverDescriptor(null, r1, 10),
                newContentLinkResolverDescriptor(null, r2, 20),
                newContentLinkResolverDescriptor(null, r3, 30)
        );

        setUpMockAccessor(originalDescriptors);
        List<ContentLinkResolver> result = tested.getLinkResolverProvider().getLinkResolvers();
        assertNotNull(result);
        assertEquals(r1, result.get(0));
        assertEquals(r2, result.get(1));
        assertEquals(r3, result.get(2));
    }

    @Test
    public void testBuildContentLinkResolversOutOfOrder()
    {
        final MockContentLinkResolver r1 = new MockContentLinkResolver(null);
        final MockContentLinkResolver r2 = new MockContentLinkResolver(null);
        final MockContentLinkResolver r3 = new MockContentLinkResolver(null);

        List<ContentLinkResolverDescriptor> originalDescriptors = Lists.newArrayList(
                newContentLinkResolverDescriptor(null, r1, 20),
                newContentLinkResolverDescriptor(null, r2, 10),
                newContentLinkResolverDescriptor(null, r3, 30)
        );

        setUpMockAccessor(originalDescriptors);
        List<ContentLinkResolver> result = tested.getLinkResolverProvider().getLinkResolvers();
        assertNotNull(result);
        assertEquals(r2, result.get(0));
        assertEquals(r1, result.get(1));
        assertEquals(r3, result.get(2));
    }

    @Test
    public void testCreateLink()
    {
        MockLink mockLink = new MockLink(new GenericLinkParser("[foo|bar]"));
        final MockContentLinkResolver r1 = new MockContentLinkResolver(mockLink);
        setUpMockAccessor(ImmutableList.of(newContentLinkResolverDescriptor(null, r1, 10)));
        Link link = tested.createLink(null, "[foo|bar]");
        assertEquals(mockLink, link);
    }

    @Test
    public void testCreateLinkWithResolverFallThrough()
    {
        MockLink mockLink = new MockLink(new GenericLinkParser("[foo|bar]"));
        final MockContentLinkResolver r1 = new MockContentLinkResolver(null);
        final MockContentLinkResolver r2 = new MockContentLinkResolver(mockLink);
        setUpMockAccessor(ImmutableList.of(
                newContentLinkResolverDescriptor(null, r1, 10),
                newContentLinkResolverDescriptor(null, r2, 20)
        ));
        Link link = tested.createLink(null, "[foo|bar]");
        assertEquals(mockLink, link);
    }

    @Test
    public void testCreateLinkNoResolversFound()
    {
        final MockContentLinkResolver r1 = new MockContentLinkResolver(null);
        setUpMockAccessor(ImmutableList.of(newContentLinkResolverDescriptor(null, r1, 10)));
        Link link = tested.createLink(null, "[foo|bar]");
        assertNotNull(link);
        assertTrue("Unexpected link instance: " + link.getClass().getName(), link instanceof UnresolvedLink);
    }

    @Test
    public void testCreateLinkUrlLink()
    {
        final MockContentLinkResolver r1 = new MockContentLinkResolver(null);
        setUpMockAccessor(ImmutableList.of(newContentLinkResolverDescriptor(null, r1, 10)));
        Link link = tested.createLink(null, "http://jira.atlassian.com/");
        assertNotNull(link);
        assertTrue("Unexpected link instance: " + link.getClass().getName(), link instanceof UrlLink);
    }

    private void setUpMockAccessor(List<ContentLinkResolverDescriptor> originalDescriptors)
    {
        EasyMock.expect(mockPluginAccessor.getEnabledModuleDescriptorsByClass(ContentLinkResolverDescriptor.class)).andReturn(originalDescriptors);
        replay();
    }

    private void replay()
    {
        EasyMock.replay(mockPluginAccessor, mockEventPublisher);
    }

    private ContentLinkResolverDescriptor newContentLinkResolverDescriptor(final JiraAuthenticationContext authContext,
            final MockContentLinkResolver resolver, final int order)
    {
        return new ContentLinkResolverDescriptor(authContext, ModuleFactory.LEGACY_MODULE_FACTORY) {
            @Override
            public int getOrder()
            {
                return order;
            }

            @Override
            public ContentLinkResolver getModule()
            {
                return resolver;
            }
        };
    }

}
