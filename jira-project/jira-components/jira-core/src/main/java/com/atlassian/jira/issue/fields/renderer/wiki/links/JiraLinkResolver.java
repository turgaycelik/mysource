package com.atlassian.jira.issue.fields.renderer.wiki.links;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.plugin.contentlinkresolver.ContentLinkResolverDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.ContentLinkResolver;
import com.atlassian.renderer.links.GenericLinkParser;
import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.links.LinkResolver;
import com.atlassian.renderer.links.UnresolvedLink;
import com.atlassian.renderer.links.UrlLink;
import com.atlassian.renderer.util.UrlUtil;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/** The default implementation for Jira used to resolve wiki style links to things that Jira understands. */
public class JiraLinkResolver implements LinkResolver
{
    private static final Logger log = LoggerFactory.getLogger(JiraLinkResolver.class);

    private final LinkResolverProvider linkResolverProvider;

    public JiraLinkResolver(PluginAccessor pluginAccessor, EventPublisher eventPublisher)
    {
        this.linkResolverProvider = new LinkResolverProvider(pluginAccessor);
        eventPublisher.register(linkResolverProvider);
    }

    public Link createLink(RenderContext context, String linkText)
    {
        try
        {
            GenericLinkParser parser = new GenericLinkParser(linkText);
            if (TextUtils.stringSet(parser.getNotLinkBody()))
            {
                if (UrlUtil.startsWithUrl(parser.getNotLinkBody()) || parser.getNotLinkBody().startsWith("//") || parser
                        .getNotLinkBody().startsWith("\\\\"))
                {
                    return new UrlLink(parser);
                }

                parser.parseAsContentLink();

                for (ContentLinkResolver resolver : linkResolverProvider.getLinkResolvers())
                {
                    Link link = resolver.createContentLink(context, parser);
                    if (link != null)
                    {
                        return link;
                    }
                }
            }
        }
        catch (Exception e)
        {
            // fall through to unresolved link returner
            Issue issue = (Issue) context.getParam(AtlassianWikiRenderer.ISSUE_CONTEXT_KEY);
            log.debug("Unable to create a link for issue: {}", ((issue == null) ? "null" : issue.getKey()));
        }

        return new UnresolvedLink(linkText);
    }

    public List extractLinks(RenderContext context, String links)
    {
        return null;
    }

    public List extractLinkTextList(String pageContent)
    {
        return null;
    }

    public String removeLinkBrackets(String linkText)
    {
        if (TextUtils.stringSet(linkText) && linkText.startsWith("[") && linkText.endsWith("]"))
        {
            return linkText.substring(1, linkText.length() - 1);
        }
        return linkText;
    }

    /**
     * For tests.
     *
     * @return underlying link resolver provider
     */
    LinkResolverProvider getLinkResolverProvider()
    {
        return linkResolverProvider;
    }

    public static class LinkResolverProvider
    {
        private final PluginAccessor accessor;

        private final ResettableLazyReference<List<ContentLinkResolver>> linkResolvers = new ResettableLazyReference<List<ContentLinkResolver>>()
        {
            @Override
            protected List<ContentLinkResolver> create() throws Exception
            {
                final List<ContentLinkResolverDescriptor> descriptors = Lists.newArrayList(accessor.getEnabledModuleDescriptorsByClass(ContentLinkResolverDescriptor.class));
                Collections.sort(descriptors, ModuleDescriptorComparator.COMPARATOR);
                return ImmutableList.copyOf(Collections2.transform(descriptors, new Function<ContentLinkResolverDescriptor, ContentLinkResolver>()
                {
                    @Override
                    public ContentLinkResolver apply(ContentLinkResolverDescriptor from)
                    {
                        return from.getModule();
                    }
                }
                ));
            }
        };

        //We don't want anyone to instantiate this guy. It's only public for the events to work.
        private LinkResolverProvider(PluginAccessor accessor)
        {
            this.accessor = accessor;
        }

        List<ContentLinkResolver> getLinkResolvers()
        {
            return linkResolvers.get();
        }

        @EventListener
        public void onPluginModuleEnabled(PluginModuleEnabledEvent pluginModuleEnabledEvent)
        {
            onPluginModuleEvent(pluginModuleEnabledEvent.getModule());
        }

        @EventListener
        public void onPluginModuleDisabled(PluginModuleDisabledEvent pluginModuleDisabledEvent)
        {
            onPluginModuleEvent(pluginModuleDisabledEvent.getModule());
        }

        private void onPluginModuleEvent(ModuleDescriptor<?> descriptor)
        {
            if (descriptor instanceof ContentLinkResolverDescriptor)
            {
                linkResolvers.reset();
            }
        }


    }
}
