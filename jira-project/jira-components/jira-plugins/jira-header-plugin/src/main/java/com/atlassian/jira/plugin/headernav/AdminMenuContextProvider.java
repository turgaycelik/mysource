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

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class AdminMenuContextProvider implements ContextProvider
{
    static final String ADMIN_TOP_NAVIGATION_BAR_LOCATION = "system.admin.top";
    static final String CONTEXT_SECTIONS_KEY = "adminHeaderSections";
    static final String CONTEXT_ROOT_ADMIN_MENU_KEY = "rootAdminMenuLink";

    private final SimpleLinkManager simpleLinkManager;
    private final JiraAuthenticationContext authenticationContext;

    public AdminMenuContextProvider(
            final SimpleLinkManager simpleLinkManager,
            final JiraAuthenticationContext authenticationContext)
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
        final Map<SimpleLinkSection, List<SimpleLink>> adminHeaderSections = getAdminHeaderSections(user, helper);
        return MapBuilder.newBuilder(context)
                .add(CONTEXT_SECTIONS_KEY, adminHeaderSections)
                .add(CONTEXT_ROOT_ADMIN_MENU_KEY, getFirstItem(adminHeaderSections))
                .toMap();
    }

    private static SimpleLink getFirstItem(final Map<SimpleLinkSection, List<SimpleLink>> sections)
    {
        if (!sections.isEmpty())
        {
            final List<SimpleLink> firstSection = getFirst(sections.values());
            if (!firstSection.isEmpty())
            {
                return getFirst(firstSection);
            }
        }
        return null;
    }

    private static <T> T getFirst(final Iterable<? extends T> iterable) throws NoSuchElementException
    {
        return iterable.iterator().next();
    }

    private Map<SimpleLinkSection, List<SimpleLink>> getAdminHeaderSections(final User user, final JiraHelper helper)
    {
        final ImmutableMap.Builder<SimpleLinkSection, List<SimpleLink>> builder = ImmutableMap.builder();
        for (final SimpleLinkSection adminHeaderSection : simpleLinkManager.getNotEmptySectionsForLocation(ADMIN_TOP_NAVIGATION_BAR_LOCATION, user, helper))
        {
            builder.put(adminHeaderSection, getAdminHeaderLinks(adminHeaderSection, user, helper));
        }
        return builder.build();
    }

    private List<SimpleLink> getAdminHeaderLinks(final SimpleLinkSection section, final User user, final JiraHelper helper)
    {
        final ImmutableList.Builder<SimpleLink> builder = ImmutableList.builder();
        for (final SimpleLinkSection subsection : simpleLinkManager.getNotEmptySectionsForLocation(section.getId(), user, helper))
        {
            // ROTP-117: put a project into its own side menu
            if (!subsection.getId().equals("admin_project_current"))
            {
                builder.add(new SimpleLinkSectionWithLink(subsection, getRelevantLink(subsection, user, helper)));
            }
        }
        return builder.build();
    }

    private SimpleLink getRelevantLink(final SimpleLinkSection section, final User user, final JiraHelper helper)
    {
        SimpleLink relevantLink = findFirstLinkForSubSection(section, user, helper);
        if (relevantLink == null)
        {
            relevantLink = simpleLinkManager.getLinksForSection(section.getId(), user, helper).get(0);
        }
        return relevantLink;
    }

    private SimpleLink findFirstLinkForSubSection(final SimpleLinkSection section, final User user, final JiraHelper helper)
    {
        final List<SimpleLinkSection> nonEmptySubSections = simpleLinkManager.getNotEmptySectionsForLocation(section.getId(), user, helper);
        for (final SimpleLinkSection subSection : nonEmptySubSections)
        {
            if ("atl.jira.proj.config".equals(subSection.getId()))
            {
                continue;
            }
            final String subSectionKey = section.getId() + "/" + subSection.getId();
            final List<SimpleLink> links = simpleLinkManager.getLinksForSection(subSectionKey, user, helper);
            if (!links.isEmpty())
            {
                return links.get(0);
            }
            final SimpleLink firstLinkOfSubSection = findFirstLinkForSubSection(subSection, user, helper);
            if (firstLinkOfSubSection != null)
            {
                return firstLinkOfSubSection;
            }
        }
        return null;
    }

    private static class SimpleLinkSectionWithLink implements SimpleLink
    {
        private final SimpleLinkSection section;
        private final SimpleLink link;

        private SimpleLinkSectionWithLink(final SimpleLinkSection section, final SimpleLink link)
        {
            this.section = section;
            this.link = link;
        }

        @Override
        public String getUrl()
        {
            return link.getUrl();
        }

        @Override
        public String getAccessKey()
        {
            return link.getAccessKey();
        }

        @Override
        public String getLabel()
        {
            return section.getLabel();
        }

        @Override
        public String getTitle()
        {
            return section.getTitle();
        }

        @Override
        public String getIconUrl()
        {
            return section.getIconUrl();
        }

        @Override
        public String getStyleClass()
        {
            return section.getStyleClass();
        }

        @Override
        public String getId()
        {
            return section.getId();
        }

        @Override
        public Map<String, String> getParams()
        {
            return section.getParams();
        }

        @Override
        public Integer getWeight()
        {
            return section.getWeight();
        }
    }
}
