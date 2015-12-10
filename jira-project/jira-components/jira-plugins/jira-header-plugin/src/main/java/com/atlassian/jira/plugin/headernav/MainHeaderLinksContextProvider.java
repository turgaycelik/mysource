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
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainHeaderLinksContextProvider implements ContextProvider
{
    static final String SYSTEM_TOP_NAVIGATION_BAR = "system.top.navigation.bar";

    static final String LAZY_HEADER_LINKS_KEY = "lazyMainHeaderLinks";
    static final String DROPDOWN_SECTIONS_KEY = "dropdownSectionsMap";
    static final String DROPDOWN_LINKS_KEY = "dropdownLinksMap";
    static final String TOPLEVEL_ITEMS_KEY = "toplevelItems";
    static final String MORE_ITEMS_KEY = "moreItems";

    private final SimpleLinkManager simpleLinkManager;
    private final JiraAuthenticationContext authenticationContext;
    private Set<String> toplevelItems;

    public MainHeaderLinksContextProvider(@Nonnull final SimpleLinkManager simpleLinkManager, @Nonnull final JiraAuthenticationContext authenticationContext)
    {
        this.simpleLinkManager = Assertions.notNull(simpleLinkManager);
        this.authenticationContext = Assertions.notNull(authenticationContext);
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        toplevelItems = filterToplevelItems(params);
    }

    /**
     * Returns a Set of all link ids from the params map that should be toplevel items.
     *
     * @param params
     * @return
     */
    private Set<String> filterToplevelItems(Map<String, String> params)
    {
        return Sets.newHashSet(Maps.filterKeys(params, new Predicate<String>()
        {
            @Override
            public boolean apply(@Nullable String s)
            {
                return s.startsWith("toplevel-item");
            }
        }).values());
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final User user = authenticationContext.getLoggedInUser();
        final JiraHelper helper = (JiraHelper) context.get("helper");
        final List<SimpleLink> mainHeaderLinks = simpleLinkManager.getLinksForSection(SYSTEM_TOP_NAVIGATION_BAR, user, helper);

        Iterable<SimpleLink> displayedToplevelItems = Iterables.filter(mainHeaderLinks, isToplevelItem());
        Iterable<SimpleLink> displayedMoreItems = Iterables.filter(mainHeaderLinks, Predicates.not(isToplevelItem()));

        final Set<String> lazyMainHeaderLinks = collectLazyMainHeaderLinks(mainHeaderLinks, user, helper);
        final Set<String> nonLazyMainHeaderLinks = collectNonLazyMainHeaderLinks(mainHeaderLinks, lazyMainHeaderLinks, user, helper);
        final Map<String, List<SimpleLinkSection>> nonLazyMainHeaderLinkIdsToSectionList = collectSectionsForNonLazyMainHeaderLinks(nonLazyMainHeaderLinks, user, helper);
        final Map<String, List<SimpleLink>> sectionIdToLinksList = collectLinksForSections(nonLazyMainHeaderLinkIdsToSectionList, user, helper);
        return MapBuilder.<String, Object>newBuilder(context)
                .add(LAZY_HEADER_LINKS_KEY, lazyMainHeaderLinks)
                .add(DROPDOWN_SECTIONS_KEY, nonLazyMainHeaderLinkIdsToSectionList)
                .add(DROPDOWN_LINKS_KEY, sectionIdToLinksList)
                .add(TOPLEVEL_ITEMS_KEY, Lists.newArrayList(displayedToplevelItems))
                .add(MORE_ITEMS_KEY, Lists.newArrayList(displayedMoreItems))
                .toHashMap();
    }

    private Predicate<SimpleLink> isToplevelItem()
    {
        return new Predicate<SimpleLink>()
        {
            @Override
            public boolean apply(@Nullable SimpleLink simpleLink)
            {
                return true;
            }
        };
    }

    @Nonnull
    private Set<String> collectLazyMainHeaderLinks(@Nonnull final List<SimpleLink> mainHeaderLinks, @Nullable final User user, @Nonnull final JiraHelper helper)
    {
        final Set<String> result = new HashSet<String>();
        for (final SimpleLink mainHeaderLink : mainHeaderLinks)
        {
            final String mainHeaderLinkId = mainHeaderLink.getId();
            if (simpleLinkManager.shouldLocationBeLazy(mainHeaderLinkId, user, helper))
            {
                result.add(mainHeaderLinkId);
            }
        }
        return result;
    }

    @Nonnull
    private Set<String> collectNonLazyMainHeaderLinks(@Nonnull final List<SimpleLink> mainHeaderLinks, @Nonnull final Set<String> lazyMainHeaderLinks, @Nullable final User user, @Nonnull final JiraHelper helper)
    {
        if (mainHeaderLinks.size() == lazyMainHeaderLinks.size())
        {
            return Collections.emptySet();
        }

        final Set<String> result = new HashSet<String>();
        for (final SimpleLink mainHeaderLink : mainHeaderLinks)
        {
            final String mainHeaderLinkId = mainHeaderLink.getId();
            if (!lazyMainHeaderLinks.contains(mainHeaderLinkId))
            {
                result.add(mainHeaderLinkId);
            }
        }
        return result;
    }

    @Nonnull
    private Map<String, List<SimpleLinkSection>> collectSectionsForNonLazyMainHeaderLinks(@Nonnull final Set<String> nonLazyMainHeaderLinkIds, @Nullable final User user, @Nonnull final JiraHelper helper)
    {
        final Map<String, List<SimpleLinkSection>> result = new HashMap<String, List<SimpleLinkSection>>();
        for (final String mainHeaderLinkId : nonLazyMainHeaderLinkIds)
        {
            final List<SimpleLinkSection> sections = simpleLinkManager.getSectionsForLocation(mainHeaderLinkId, user, helper);
            result.put(mainHeaderLinkId, sections);
        }
        return result;
    }

    @Nonnull
    private Map<String, List<SimpleLink>> collectLinksForSections(@Nonnull final Map<String, List<SimpleLinkSection>> mainHeaderLinkIdsToSectionList, @Nullable final User user, @Nonnull final JiraHelper helper)
    {
        final Map<String, List<SimpleLink>> result = new HashMap<String, List<SimpleLink>>();
        for (final Map.Entry<String, List<SimpleLinkSection>> mainHeaderLinkIdToSectionList : mainHeaderLinkIdsToSectionList.entrySet())
        {
            final String mainHeaderLinkId = mainHeaderLinkIdToSectionList.getKey();
            final List<SimpleLinkSection> sectionList = mainHeaderLinkIdToSectionList.getValue();
            for (final SimpleLinkSection section : sectionList)
            {
                final String sectionKey = mainHeaderLinkId + "/" + section.getId();
                final List<SimpleLink> linksForSection = simpleLinkManager.getLinksForSection(sectionKey, user, helper);
                result.put(sectionKey, linksForSection);
            }

        }
        return result;
    }
}
