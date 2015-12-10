package com.atlassian.jira.plugin.webfragment;

import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.api.DynamicWebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;

import com.google.common.annotations.VisibleForTesting;

/**
 * Jira wrapper for the {@link WebInterfaceManager}. If generating simple links for menus, use {@link
 * com.atlassian.jira.plugin.webfragment.SimpleLinkManager} as then consumers can insert {@link
 * com.atlassian.jira.plugin.webfragment.SimpleLinkFactory} into the link generation process.
 *
 * @deprecated since v6.3 - use {@link com.atlassian.plugin.web.api.DynamicWebInterfaceManager} directly if possible.
 */
public class JiraWebInterfaceManager
{
    public static final String CONTEXT_KEY_USER = "user";
    public static final String CONTEXT_KEY_USERNAME = "username";
    public static final String CONTEXT_KEY_HELPER = "helper";
    public static final String CONTEXT_KEY_LOCATION = "location";
    public static final String CONTEXT_KEY_I18N = "i18n";

    private WebInterfaceManager webInterfaceManager;

    public JiraWebInterfaceManager(WebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = webInterfaceManager;
    }

    public boolean hasSectionsForLocation(String location)
    {
        return webInterfaceManager.hasSectionsForLocation(location);
    }

    public List<WebSectionModuleDescriptor> getSections(String location)
    {
        return webInterfaceManager.getSections(location);
    }

    public List<WebSectionModuleDescriptor> getDisplayableSections(String location, User remoteUser, JiraHelper jiraHelper)
    {
        return webInterfaceManager.getDisplayableSections(location, makeContext(remoteUser, jiraHelper));
    }

    public List<WebItemModuleDescriptor> getItems(String section)
    {
        return webInterfaceManager.getItems(section);
    }

    public List<WebItemModuleDescriptor> getDisplayableItems(String section, User remoteUser, JiraHelper jiraHelper)
    {
        return webInterfaceManager.getDisplayableItems(section, makeContext(remoteUser, jiraHelper));
    }

    public void refresh()
    {
        webInterfaceManager.refresh();
    }

    public WebFragmentHelper getWebFragmentHelper()
    {
        return webInterfaceManager.getWebFragmentHelper();
    }

    @VisibleForTesting
    protected Map<String, Object> makeContext(User remoteUser, JiraHelper jiraHelper)
    {
        final Map<String, Object> params = jiraHelper.getContextParams();
        params.put(CONTEXT_KEY_USER, remoteUser);
        params.put(CONTEXT_KEY_HELPER, jiraHelper);

        return params;
    }
}
