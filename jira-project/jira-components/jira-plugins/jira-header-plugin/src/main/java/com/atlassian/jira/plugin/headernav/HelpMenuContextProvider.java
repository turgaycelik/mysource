package com.atlassian.jira.plugin.headernav;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelpMenuContextProvider implements ContextProvider
{
    static final String SYSTEM_USER_OPTIONS = "system.user.options";
    static final String HELP_SECTION_ID = "jira-help";

    static final String SECTIONS_KEY = "displayableSections";
    static final String SECTION_LINKS_KEY = "completeSectionKeyToDisplayableItems";
    static final String HELP_LINK_KEY = "rootHelpMenuItem";

    private final SimpleLinkManager simpleLinkManager;
    private final JiraAuthenticationContext authenticationContext;

    public HelpMenuContextProvider(@Nonnull final SimpleLinkManager simpleLinkManager, @Nonnull final JiraAuthenticationContext authenticationContext)
    {
        this.simpleLinkManager = Assertions.notNull(simpleLinkManager);
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
        final List<SimpleLinkSection> displayableSections = collectDisplayableSections(user, helper);
        final Map<String, List<SimpleLink>> sectionToDisplayableItems = collectDisplayableItemsForSections(displayableSections, user, helper);
        final SimpleLink rootHelpMenuItem = getRootHelpMenuItem(displayableSections, sectionToDisplayableItems);
        return MapBuilder.<String, Object>newBuilder(context)
                .add("user", user)
                .add(SECTIONS_KEY, displayableSections)
                .add(SECTION_LINKS_KEY, sectionToDisplayableItems)
                .add(HELP_LINK_KEY, rootHelpMenuItem)
                .toHashMap();
    }

    @Nonnull
    private List<SimpleLinkSection> collectDisplayableSections(@Nullable final User user, @Nonnull final JiraHelper helper)
    {
        final List<SimpleLinkSection> notEmptySectionsForLocation = simpleLinkManager.getNotEmptySectionsForLocation(SYSTEM_USER_OPTIONS, user, helper);
        return Lists.newArrayList(Iterables.filter(notEmptySectionsForLocation, new Predicate<SimpleLinkSection>()
        {
            @Override
            public boolean apply(@Nullable final SimpleLinkSection section)
            {
                return section != null && HELP_SECTION_ID.equals(section.getId());
            }
        }));
    }

    @Nonnull
    private Map<String, List<SimpleLink>> collectDisplayableItemsForSections(@Nonnull final List<SimpleLinkSection> displayableSections, @Nullable final User user, @Nonnull final JiraHelper helper)
    {
        final Map<String, List<SimpleLink>> result = new HashMap<String, List<SimpleLink>>();
        for (SimpleLinkSection displayableSection : displayableSections)
        {
            final String sectionKey = SYSTEM_USER_OPTIONS + "/" + displayableSection.getId();
            final List<SimpleLink> displayableItems = simpleLinkManager.getLinksForSection(sectionKey, user, helper);
            result.put(displayableSection.getId(), displayableItems);
        }
        return result;
    }

    @Nullable
    private SimpleLink getRootHelpMenuItem(@Nonnull final List<SimpleLinkSection> displayableSections, @Nonnull final Map<String, List<SimpleLink>> sectionToDisplayableItems)
    {
        for (final SimpleLinkSection displayableSection : displayableSections)
        {
            final List<SimpleLink> firstWebSectionItems = sectionToDisplayableItems.get(displayableSection.getId());
            if (firstWebSectionItems != null && !firstWebSectionItems.isEmpty())
            {
                return firstWebSectionItems.get(0);
            }
        }
        return null;
    }
}
