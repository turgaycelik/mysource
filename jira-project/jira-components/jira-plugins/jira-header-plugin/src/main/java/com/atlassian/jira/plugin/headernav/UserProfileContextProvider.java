package com.atlassian.jira.plugin.headernav;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileContextProvider implements ContextProvider
{
    static final String SYSTEM_USER_OPTIONS = "system.user.options";
    static final String SECTIONS_KEY = "displayableSections";
    static final String SECTION_ITEMS_KEY = "completeSectionKeyToDisplayableItems";
    static final String HELP_WEB_SECTION_KEY = "jira-help";

    private final JiraAuthenticationContext authenticationContext;

    public UserProfileContextProvider(@Nonnull final JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = Assertions.notNull(authenticationContext);
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final User user = authenticationContext.getLoggedInUser();
        final JiraHelper helper = (JiraHelper) context.get("helper");
        final JiraWebInterfaceManager webInterfaceManager = (JiraWebInterfaceManager) context.get("webInterfaceManager");
        final List<WebSectionModuleDescriptor> displayableSections = collectDisplayableSections(user, helper, webInterfaceManager);
        final Map<String, List> sectionsToDisplayableItems = collectDisplayableItemsForDisplayableSections(displayableSections, user, helper, webInterfaceManager);
        return MapBuilder.<String, Object>newBuilder(context)
                .add(SECTIONS_KEY, displayableSections)
                .add(SECTION_ITEMS_KEY, sectionsToDisplayableItems)
                .toHashMap();
    }

    private List<WebSectionModuleDescriptor> collectDisplayableSections(@Nullable final User user, @Nonnull final JiraHelper helper, @Nonnull final JiraWebInterfaceManager webInterfaceManager)
    {
        //noinspection unchecked
        final List<WebSectionModuleDescriptor> displayableSections = webInterfaceManager.getDisplayableSections(SYSTEM_USER_OPTIONS, user, helper);
        return Lists.newArrayList(Iterables.filter(displayableSections, new Predicate<WebSectionModuleDescriptor>()
        {
            @Override
            public boolean apply(@Nullable final WebSectionModuleDescriptor webSection)
            {
                return webSection != null && !HELP_WEB_SECTION_KEY.equals(webSection.getKey());
            }
        }));
    }

    @Nonnull
    private Map<String, List> collectDisplayableItemsForDisplayableSections(@Nonnull final List<WebSectionModuleDescriptor> displayableSections, @Nonnull final User user, @Nonnull final JiraHelper helper, @Nonnull final JiraWebInterfaceManager webInterfaceManager)
    {
        final Map<String, List> sectionsToDisplayableItems = new HashMap<String, List>();
        for (final WebSectionModuleDescriptor displayableSection : displayableSections)
        {
            final String sectionKey = SYSTEM_USER_OPTIONS + "/" + displayableSection.getKey();
            final List displayableItems = webInterfaceManager.getDisplayableItems(sectionKey, user, helper);
            sectionsToDisplayableItems.put(displayableSection.getCompleteKey(), displayableItems);

        }
        return sectionsToDisplayableItems;
    }
}
