package com.atlassian.jira.plugin.viewissue.issuelink;

import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.plugin.issuelink.IssueLinkRenderer;
import com.atlassian.jira.plugin.issuelink.IssueLinkRendererModuleDescriptor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Converts a remote link to a LinkSource that is used by the velocity macro to display web links.
 *
 * @since v5.0
 */
public class RemoteIssueLinkUtils
{
    public static final String DEFAULT_RELATIONSHIP_I18N_KEY = "issuelinking.remote.link.relationship.default";

    private RemoteIssueLinkUtils() {}

    public static Map<String, List<IssueLinkContext>> convertToIssueLinkContexts(List<RemoteIssueLink> remoteIssueLinks, Long issueId, String baseUrl, I18nHelper i18n, PluginAccessor pluginAccessor)
    {
        Map<String, List<IssueLinkContext>> contextsMap = Maps.newHashMap();
        for (RemoteIssueLink remoteIssueLink : remoteIssueLinks)
        {
            final IssueLinkRendererModuleDescriptor descriptor = getIssueLinkRendererModuleDescriptor(pluginAccessor, remoteIssueLink.getApplicationType());
            final boolean requiresAsyncLoading;
            final String html;
            IssueLinkRenderer issueLinkRenderer = null;
            try
            {
                issueLinkRenderer = descriptor.getModule();
                if (!issueLinkRenderer.shouldDisplay(remoteIssueLink))
                {
                    continue;
                }

                requiresAsyncLoading = issueLinkRenderer.requiresAsyncLoading(remoteIssueLink);
                html = descriptor.getInitialHtml(remoteIssueLink);
            }
            catch (Throwable t)
            {
                SafePluginPointAccess.handleException(t, descriptor, issueLinkRenderer);
                continue;
            }

            String relationship = StringUtils.defaultIfEmpty(remoteIssueLink.getRelationship(), i18n.getText(DEFAULT_RELATIONSHIP_I18N_KEY));
            final List<IssueLinkContext> contexts;
            if (contextsMap.containsKey(relationship))
            {
                contexts = contextsMap.get(relationship);
            }
            else
            {
                contexts = Lists.newArrayList();
                contextsMap.put(relationship, contexts);
            }

            final String deleteUrl = String.format(baseUrl + "/secure/DeleteRemoteIssueLink.jspa?id=%d&remoteIssueLinkId=%d", issueId, remoteIssueLink.getId());

            contexts.add(IssueLinkContext.newRemoteIssueLinkContext("remote-" + remoteIssueLink.getId(), deleteUrl, true, html, remoteIssueLink.getId(), requiresAsyncLoading));
        }

        return contextsMap;
    }

    /**
     * Returns the final HTML for the remote issue link.
     *
     * @param remoteIssueLink remote issue link
     * @param pluginAccessor plugin accessor
     *
     * @return final HTML for the remote issue link
     *
     * @throws java.lang.IllegalStateException when the plugin providing the renderer is in invalid state
     * @throws java.lang.RuntimeException propagating from underlying RemoteIssueLinkRenderer implementation
     */
    public static String getFinalHtml(RemoteIssueLink remoteIssueLink, PluginAccessor pluginAccessor)
    {
        final IssueLinkRendererModuleDescriptor descriptor = getIssueLinkRendererModuleDescriptor(pluginAccessor, remoteIssueLink.getApplicationType());
        try
        {
            return descriptor.getFinalHtml(remoteIssueLink);
        }
        catch (Error e)
        {
            SafePluginPointAccess.handleError(e, descriptor);
            throw new IllegalStateException(e);
        }
    }

    @VisibleForTesting
    static IssueLinkRendererModuleDescriptor getIssueLinkRendererModuleDescriptor(final PluginAccessor pluginAccessor, final String applicationType)
    {
        final List<IssueLinkRendererModuleDescriptor> descriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(IssueLinkRendererModuleDescriptor.class);

        if (StringUtils.isNotBlank(applicationType))
        {
            final Option<IssueLinkRendererModuleDescriptor> specific = Iterables.findFirst(descriptors, SafePluginPointAccess.safe(new Predicate<IssueLinkRendererModuleDescriptor>()
            {
                @Override
                public boolean apply(IssueLinkRendererModuleDescriptor descriptor)
                {
                    return descriptor.handlesApplicationType(applicationType);
                }
            }));

            if (specific.isDefined())
            {
                return specific.get();
            }
        }

        return getDefaultLinkRendererModuleDescriptor(descriptors);
    }

    private static IssueLinkRendererModuleDescriptor getDefaultLinkRendererModuleDescriptor(List<IssueLinkRendererModuleDescriptor> descriptors)
    {
        final Option<IssueLinkRendererModuleDescriptor> defaultDescriptor = Iterables.findFirst(descriptors, SafePluginPointAccess.safe(new Predicate<IssueLinkRendererModuleDescriptor>()
        {
            @Override
            public boolean apply(IssueLinkRendererModuleDescriptor descriptor)
            {
                return descriptor.isDefaultHandler();
            }
        }));

        if (defaultDescriptor.isEmpty())
        {
            throw new IllegalStateException("No default issue link renderer module descriptor found");
        }

        return defaultDescriptor.get();
    }
}
