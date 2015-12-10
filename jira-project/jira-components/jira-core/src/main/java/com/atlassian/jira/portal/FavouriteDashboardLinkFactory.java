package com.atlassian.jira.portal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.model.WebFragmentBuilder;
import com.atlassian.plugin.web.api.provider.WebItemProvider;

import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;

/**
 * A SimpleLinkFactory that generates a list of SimpleLinks that point to the Favourite Filters of a user.
 *
 * @since v4.0
 */
public class FavouriteDashboardLinkFactory implements WebItemProvider
{
    private static final int MAX_LABEL_LENGTH = 30;
    private static final String ITEM_SECTION = "home_link/dashboard_link_main";

    private final PortalPageService portalPageService;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final I18nHelper.BeanFactory i18nFactory;
    private final UserHistoryManager userHistoryManager;

    public FavouriteDashboardLinkFactory(PortalPageService portalPageService, VelocityRequestContextFactory velocityRequestContextFactory,
            I18nHelper.BeanFactory i18nFactory, final UserHistoryManager userHistoryManager)
    {
        this.portalPageService = portalPageService;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.i18nFactory = i18nFactory;
        this.userHistoryManager = userHistoryManager;
    }

    @Override
    public Iterable<WebItem> getItems(final Map<String, Object> context)
    {
        final User user = (User) context.get("user");
        final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();

        final Collection<PortalPage> portalPages = portalPageService.getFavouritePortalPages(user);
        // Need to ensure they contain the baseurl in case they are loaded via ajax/rest
        final String baseUrl = requestContext.getBaseUrl();
        final I18nHelper i18n = i18nFactory.getInstance(user);
        final List<WebItem> links = Lists.newArrayList();

        int weight = 10;
        if (portalPages == null || portalPages.isEmpty())
        {
            links.add(new WebFragmentBuilder(weight += 10).
                    id("dash_lnk_system").
                    label(i18n.getText("menu.dashboard.view.system")).
                    title(i18n.getText("menu.dashboard.view.system.title")).
                    webItem(ITEM_SECTION).
                    url(baseUrl + "/secure/Dashboard.jspa").build());
        }
        else
        {
            final Long currentDash = getCurrentDashboard(user);

            for (PortalPage portalPage : portalPages)
            {
                String style = null;
                final Long pageId = portalPage.getId();
                final String description = portalPage.getDescription();
                final String name = portalPage.getName();
                String shortName = name;
                if (shortName.length() > MAX_LABEL_LENGTH)
                {
                    shortName = shortName.substring(0, MAX_LABEL_LENGTH) + "...";
                }

                final String title = StringUtils.isBlank(description) ? name : i18n.getText("menu.dashboard.title", name, description);

                if (portalPages.size() > 1 && pageId.equals(currentDash))
                {
                    style = "bolded";
                }

                links.add(new WebFragmentBuilder(weight += 10).
                        id("dash_lnk_" + pageId).
                        label(shortName).
                        title(title).
                        styleClass(style).
                        webItem(ITEM_SECTION).
                        url(baseUrl + "/secure/Dashboard.jspa?selectPageId=" + pageId).build());
            }
        }
        return links;
    }

    private Long getCurrentDashboard(final User user)
    {
        final List<UserHistoryItem> history = userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        if (!history.isEmpty())
        {
            return Long.valueOf(history.get(0).getEntityId());
        }

        return null;
    }
}
