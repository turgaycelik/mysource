package com.atlassian.jira.user.profile;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.ozymandias.PluginPointVisitor;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WeightedDescriptorComparator;
import com.atlassian.plugin.web.model.WebPanel;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @since v5.2
 */
public class PlugableUserProfileFragment extends AbstractUserProfileFragment
{
    final static private String DETAILS_PANEL_LOCATION = "webpanels.user.profile.summary.details";
    final static private String PREFERENCES_PANEL_LOCATION = "webpanels.user.profile.summary.preferences";
    final static private String ASSIGNED_PANEL_LOCATION = "webpanels.user.profile.summary.assigned";
    final static private String CUSTOM_PANEL_LOCATION = "webpanels.user.profile.summary.custom";

    public static final WeightedDescriptorComparator WEIGHTED_DESCRIPTOR_COMPARATOR = new WeightedDescriptorComparator();

    private final WebInterfaceManager webInterfaceManager;

    public PlugableUserProfileFragment(final JiraAuthenticationContext jiraAuthenticationContext, final VelocityTemplatingEngine templatingEngine, final VelocityParamFactory velocityParamFactory, final WebInterfaceManager webInterfaceManager)
    {
        super(jiraAuthenticationContext, templatingEngine, velocityParamFactory);
        this.webInterfaceManager = webInterfaceManager;
    }

    @Override
    public String getId()
    {
        return "user-profile-plugable-panel";
    }

    @Override
    public String getFragmentHtml(final User profileUser, final User currentUser)
    {
        final Map<String, Object> context = ImmutableMap.<String,Object>of("profileUser",profileUser, "currentUser", currentUser);

        final List<WebPanelModuleDescriptor> descriptors = Lists.newArrayList();
                descriptors.add(getFirstDescriptor(DETAILS_PANEL_LOCATION, context));
                descriptors.add(getFirstDescriptor(PREFERENCES_PANEL_LOCATION, context));
                descriptors.add(getFirstDescriptor(ASSIGNED_PANEL_LOCATION, context));
                descriptors.addAll(webInterfaceManager.getDisplayableWebPanelDescriptors(CUSTOM_PANEL_LOCATION, context));

        final List<WebPanelModuleDescriptor> filteredDescriptors = Lists.newArrayList(Iterables.filter(descriptors, Predicates.notNull()));
        Collections.sort(filteredDescriptors, WEIGHTED_DESCRIPTOR_COMPARATOR);

        return getHtmlFromDescriptors(filteredDescriptors, context);
    }

    private String getHtmlFromDescriptors(final List<WebPanelModuleDescriptor> descriptors, final Map<String, Object> context)
    {
        final StringBuilder sb = new StringBuilder();

        SafePluginPointAccess.to().descriptors(descriptors, new PluginPointVisitor<WebPanelModuleDescriptor, WebPanel>()
        {
            @Override
            public void visit(final WebPanelModuleDescriptor webPanelModuleDescriptor, final WebPanel webPanel)
            {
                sb.append(webPanelModuleDescriptor.getModule().getHtml(context));
            }
        });

        return sb.toString();
    }

    private WebPanelModuleDescriptor getFirstDescriptor(final String location, final Map<String, Object> context) {
        final List<WebPanelModuleDescriptor> descriptors = webInterfaceManager.getDisplayableWebPanelDescriptors(location, context);
        if (!descriptors.isEmpty())
        {
            return descriptors.get(0);
        } else {
            return null;
        }
    }
}
