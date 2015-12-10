package com.atlassian.jira.admin;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.plugin.web.api.DynamicWebInterfaceManager;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.WebSection;
import com.atlassian.plugin.web.api.provider.WebItemProvider;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import webwork.action.ServletActionContext;

/**
 * Returns links for all legacy plugins that still define web-items in the system.admin section section.
 *
 * @since v4.4
 */
public class LegacyAdminLinkFactory implements WebItemProvider
{
    private static final String SYSTEM_ADMIN_LOCATION = "system.admin";

    private final DynamicWebInterfaceManager webInterfaceManager;

    public LegacyAdminLinkFactory(final DynamicWebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = webInterfaceManager;
    }

    @Override
    public Iterable<WebItem> getItems(final Map<String, Object> context)
    {
        final JiraHelper helper = new JiraHelper(ServletActionContext.getRequest());
        final List<WebItem> ret = Lists.newArrayList();

        final Map<String, Object> contextParams = helper.getContextParams();
        contextParams.putAll(context);
        final Iterable<WebSection> sections = webInterfaceManager.getDisplayableWebSections(SYSTEM_ADMIN_LOCATION, contextParams);
        for (WebSection section : sections)
        {
            Iterables.addAll(ret, webInterfaceManager.getDisplayableWebItems(SYSTEM_ADMIN_LOCATION + "/" + section.getId(), contextParams));
        }
        return ret;
    }
}
