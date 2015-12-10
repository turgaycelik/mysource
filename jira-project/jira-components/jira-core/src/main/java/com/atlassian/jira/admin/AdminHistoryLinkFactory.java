package com.atlassian.jira.admin;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.webfragment.DefaultSimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.user.DefaultUserIssueHistoryManager;
import com.atlassian.jira.user.UserAdminHistoryManager;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import org.apache.log4j.Logger;
import webwork.action.ServletActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A SimpleLinkFactory that produces links to Administration Pages for recently viewed administration pages
 *
 * @since v4.1
 */
public class AdminHistoryLinkFactory implements SimpleLinkFactory
{
    private static final Logger log = Logger.getLogger(DefaultUserIssueHistoryManager.class);

    private final UserAdminHistoryManager userAdminHistoryManager;
    private final DefaultSimpleLinkManager defaultSimpleLinkManager;
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;

    private static final String SYSTEM_ADMIN_LOCATION = "system.admin";


    public AdminHistoryLinkFactory(UserAdminHistoryManager userAdminHistoryManager, DefaultSimpleLinkManager defaultSimpleLinkManager,
            PluginAccessor pluginAccessor, ApplicationProperties applicationProperties)
    {
        this.userAdminHistoryManager = userAdminHistoryManager;
        this.defaultSimpleLinkManager = defaultSimpleLinkManager;
        this.pluginAccessor = pluginAccessor;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void init(SimpleLinkFactoryModuleDescriptor descriptor)
    {
    }

    @Override
    public List<SimpleLink> getLinks(User user, Map<String, Object> params)
    {

        // We get the admin history pages without permission checks because we have no way of checking which links
        // should be displayed via the permission manager. The real permissions for determining which links are to be
        // shown are found in conditions specified by the plugin module descriptor (see system-admin-sections.xml).
        // Consequently, {@link DefaultSimpleLinkManager } should "do the right thing" and respect this, since
        // it's meant to render links after all.
        final List<UserHistoryItem> history = userAdminHistoryManager.getAdminPageHistoryWithoutPermissionChecks(user);
        final Map<String, SimpleLink> linkIdMap = new LinkedHashMap<String, SimpleLink>();

        if (history != null && !history.isEmpty())
        {
            final JiraHelper helper = new JiraHelper(ServletActionContext.getRequest());

            final List<SimpleLinkSection> section = defaultSimpleLinkManager.getSectionsForLocation(SYSTEM_ADMIN_LOCATION, user, helper);

            final List<String> moduleIdList = new ArrayList<String>();
            for (UserHistoryItem adminHistory : history)
            {
                for (SimpleLinkSection adminSection : section)
                {
                    final List<SimpleLink> item = defaultSimpleLinkManager.getLinksForSection(SYSTEM_ADMIN_LOCATION + "/" + adminSection.getId(), user, helper);
                    if (!item.isEmpty())
                    {
                        for (SimpleLink adminItem : item)
                        {
                            final String id = adminItem.getId();
                            final String label = adminItem.getLabel();
                            final String title = adminItem.getTitle();
                            final String url = adminItem.getUrl();

                            final String linkId = adminHistory.getEntityId();
                            final String linkUrl = adminHistory.getData();
                            if (linkId != null && linkId.equals(id) && linkUrl.endsWith(url))
                            {
                                linkIdMap.put(id, new SimpleLinkImpl(id, label, title, null, "admin-item-link", null,
                                        url, null));
                            }
                        }
                    }
                }
            }


            removeAdminTasks(linkIdMap, moduleIdList);
        }

        return limitListSize(linkIdMap);
    }

    private List<SimpleLink> limitListSize(final Map<String, SimpleLink> linkIdMap)
    {
        //Find the maximum number of items to display       
        int maxItems = UserAdminHistoryManager.DEFAULT_ADMIN_HISTORY_DROPDOWN_ITEMS;
        try
        {
            maxItems = Integer.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ADMIN_HISTORY_DROPDOWN_ITEMS));
        }
        catch (final NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max.history.dropdown.items'.  Should be a number.");
        }

        final List<SimpleLink> returnList = new ArrayList<SimpleLink>(linkIdMap.values());

        if (returnList.size() > maxItems)
        {
            returnList.subList(maxItems, returnList.size()).clear();
        }
        return returnList;
    }

    private void removeAdminTasks(final Map<String, SimpleLink> linkIdMap, final List<String> moduleIdList)
    {
        final Plugin selectedPlugin = pluginAccessor.getPlugin("jira.top.navigation.bar");

        if (pluginAccessor.isPluginEnabled(selectedPlugin.getKey()))
        {
            Collection<ModuleDescriptor<?>> moduleDescriptors = selectedPlugin.getModuleDescriptors();
            for (ModuleDescriptor<?> moduleDescriptor : moduleDescriptors)
            {
                if (pluginAccessor.isPluginModuleEnabled(moduleDescriptor.getCompleteKey()))
                {
                    if (moduleDescriptor instanceof WebItemModuleDescriptor)
                    {
                        moduleIdList.add(((WebItemModuleDescriptor) moduleDescriptor).getLink().getId());
                    }
                }
            }
            linkIdMap.keySet().removeAll(moduleIdList);
        }
    }

}