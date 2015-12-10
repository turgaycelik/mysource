package com.atlassian.jira.issue.search;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.model.WebFragmentBuilder;
import com.atlassian.plugin.web.api.provider.WebItemProvider;

import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Simple Link Factory for creating links to favourite filters
 *
 * @since v4.0
 */
public class FavouriteFilterLinkFactory implements WebItemProvider
{
    private static final Logger log = Logger.getLogger(FavouriteFilterLinkFactory.class);

    private static final int DEFAULT_FILTER_DROPDOWN_ITEMS = 10;
    private static final int MAX_LABEL_LENGTH = 30;

    private final SearchRequestService searchRequestService;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ApplicationProperties applicationProperties;
    private final I18nHelper.BeanFactory i18nFactory;

    public FavouriteFilterLinkFactory(SearchRequestService searchRequestService, VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties, I18nHelper.BeanFactory i18nFactory)
    {
        this.searchRequestService = searchRequestService;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.applicationProperties = applicationProperties;
        this.i18nFactory = i18nFactory;
    }

    @Override
    public Iterable<WebItem> getItems(final Map<String, Object> context)
    {
        final User user = (User) context.get("user");
        final Collection<SearchRequest> filters = searchRequestService.getFavouriteFilters(ApplicationUsers.from(user));
        final List<WebItem> links = Lists.newArrayList();

        final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
        final I18nHelper i18n = i18nFactory.getInstance(user);

        // Need ot ensure they contain the baseurl in case they are loaded via ajax/rest
        final String baseUrl = requestContext.getBaseUrl();
        final int maxItems = getMaxDropdownItems();

        int weight = 0;
        if (user != null)
        {
            final String myFilter = i18n.getText("issue.nav.filters.my.open.issues");
            links.add(new WebFragmentBuilder(weight += 10).
                    id("filter_lnk_my").
                    label(myFilter).
                    title(myFilter).
                    addParam("class", "filter-link").
                    addParam("data-filter-id", "-1").
                    webItem("find_link/issues_filter_main").
                    url(baseUrl + "/issues/?filter=-1").
                    build());

            final String reportedFilter = i18n.getText("issue.nav.filters.reported.by.me");
            links.add(new WebFragmentBuilder(weight += 10).
                    id("filter_lnk_reported").
                    label(reportedFilter).
                    title(reportedFilter).
                    addParam("class", "filter-link").
                    addParam("data-filter-id", "-2").
                    webItem("find_link/issues_filter_main").
                    url(baseUrl + "/issues/?filter=-2").
                    build());
        }

        if (filters != null && !filters.isEmpty())
        {

            final Iterator<SearchRequest> filterIterator = filters.iterator();
            for (int i = 0; i < maxItems && filterIterator.hasNext(); i++)
            {
                final SearchRequest filter = filterIterator.next();

                final String name = filter.getName();
                String shortName = name;
                if (shortName.length() > MAX_LABEL_LENGTH)
                {
                    shortName = shortName.substring(0, MAX_LABEL_LENGTH) + "...";
                }
                final String title = StringUtils.isBlank(filter.getDescription()) ? name : i18n.getText("menu.issues.filter.title", name, filter.getDescription());
                links.add(new WebFragmentBuilder(weight += 10).
                        id("filter_lnk_" + filter.getId()).
                        label(shortName).
                        title(title).
                        addParam("class", "filter-link").
                        addParam("data-filter-id", filter.getId().toString()).
                        webItem("find_link/issues_filter_main").
                        url(baseUrl + "/secure/IssueNavigator.jspa?mode=hide&requestId=" + filter.getId()).
                        build());
            }

            if (filters.size() > maxItems)
            {
                final String url = baseUrl + "/secure/ManageFilters.jspa?filterView=favourites";
                links.add(new WebFragmentBuilder(weight + 10).
                        id("filter_lnk_more").
                        label(i18n.getText("menu.issues.filter.more")).
                        title(i18n.getText("menu.issues.filter.more.desc")).
                        webItem("find_link/issues_filter_main").
                        url(url).
                        build());
            }
        }
        return links;
    }

    private int getMaxDropdownItems()
    {
        int maxItems = DEFAULT_FILTER_DROPDOWN_ITEMS;
        try
        {
            maxItems = Integer.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS));
        }
        catch (NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max.issue.filter.dropdown.items'.  Should be a number.");
        }

        return maxItems;
    }
}
