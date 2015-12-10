package com.atlassian.jira.issue.history;

import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.model.WebFragmentBuilder;
import com.atlassian.plugin.web.api.provider.WebItemProvider;

import com.google.common.collect.Lists;

import org.apache.log4j.Logger;

/**
 * Simple Link Factory for creating links to recently view issues.
 *
 * @since v4.0
 */
public class IssueHistoryLinkFactory implements WebItemProvider
{
    private static final Logger log = Logger.getLogger(IssueHistoryLinkFactory.class);

    private final UserIssueHistoryManager userHistoryManager;
    private final ApplicationProperties applicationProperties;
    private final I18nHelper.BeanFactory i18nFactory;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private static final int MAX_LABEL_LENGTH = 30;

    public IssueHistoryLinkFactory(VelocityRequestContextFactory velocityRequestContextFactory, UserIssueHistoryManager userHistoryManager,
            ApplicationProperties applicationProperties, I18nHelper.BeanFactory i18nFactory)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.userHistoryManager = userHistoryManager;
        this.applicationProperties = applicationProperties;
        this.i18nFactory = i18nFactory;
    }

    @Override
    public Iterable<WebItem> getItems(final Map<String, Object> context)
    {
        final User user = (User) context.get("user");
        final List<Issue> history = userHistoryManager.getShortIssueHistory(user);
        final List<WebItem> links = Lists.newArrayList();

        if (!history.isEmpty())
        {
            final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
            // Need ot ensure they contain the baseurl in case they are loaded via ajax/rest
            final String baseUrl = requestContext.getBaseUrl();

            final int maxItems = getMaxDropdownItems();

            int weight = 0;
            // we actually display one less so we know when to add a more link
            for (int i = 0; i < maxItems - 1 && i < history.size(); i++)
            {
                final Issue issue = history.get(i);
                final String label = issue.getKey() + " " + issue.getSummary();
                String shortLabel = label;
                if (shortLabel.length() > MAX_LABEL_LENGTH)
                {
                    shortLabel = shortLabel.substring(0, MAX_LABEL_LENGTH) + "...";
                }
                String iconUrl = issue.getIssueTypeObject().getIconUrl();
                if (!iconUrl.startsWith("http://") && !iconUrl.startsWith("https://"))
                {
                    iconUrl = baseUrl + iconUrl;
                }

                links.add(new WebFragmentBuilder(weight += 10).
                        id("issue_lnk_" + issue.getId()).
                        label(shortLabel).
                        title(label).
                        addParam("class", "issue-link").
                        addParam("data-issue-key", issue.getKey()).
                        addParam("iconUrl", iconUrl).
                        webItem("find_link/issues_history_main").
                        url(baseUrl + "/browse/" + issue.getKey()).
                        build()
                );
            }

            if (history.size() >= maxItems)
            {
                final I18nHelper i18n = i18nFactory.getInstance(user);
                links.add(new WebFragmentBuilder(weight + 10).
                        id("issue_lnk_more").
                        label(i18n.getText("menu.issues.history.more")).
                        title(i18n.getText("menu.issues.history.more.desc")).
                        addParam("class", "filter-link").
                        addParam("data-filter-id", "-3").
                        webItem("find_link/issues_history_main").
                        url(baseUrl + "/issues/?filter=-3").
                        build()
                );
            }
        }
        return links;
    }

    private int getMaxDropdownItems()
    {
        int maxItems = UserIssueHistoryManager.DEFAULT_ISSUE_HISTORY_DROPDOWN_ITEMS;

        try
        {
            maxItems = Integer.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS));
        }
        catch (NumberFormatException e)
        {
            log.warn("Incorrect format of property 'jira.max.history.dropdown.items'.  Should be a number.");
        }

        return maxItems;
    }
}