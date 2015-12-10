package com.atlassian.jira.plugin.viewissue;

import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.model.WebFragmentBuilder;
import com.atlassian.plugin.web.api.provider.WebItemProvider;

import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;

/**
 * Factory to return the options for the different sorting options
 *
 * @since v5.0
 */
public class AttachmentSortingOptionsFactory implements WebItemProvider
{
    private static final String ITEM_SECTION = "com.atlassian.jira.jira-view-issue-plugin:attachmentmodule/drop/attachment-sorting-options";
    private final VelocityRequestContextFactory requestContextFactory;
    private final JiraAuthenticationContext authenticationContext;

    public AttachmentSortingOptionsFactory(VelocityRequestContextFactory requestContextFactory, JiraAuthenticationContext authenticationContext)
    {
        this.requestContextFactory = requestContextFactory;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public Iterable<WebItem> getItems(final Map<String, Object> context)
    {
        final VelocityRequestContext requestContext = requestContextFactory.getJiraVelocityRequestContext();
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        final Issue issue = (Issue) context.get("issue");


        final VelocityRequestSession session = requestContext.getSession();
        final String baseUrl = requestContext.getBaseUrl();
        final String sortingOrder = (String) session.getAttribute(SessionKeys.VIEWISSUE_ATTACHMENT_SORTBY);

        boolean sortedByName = "fileName".equals(sortingOrder) || StringUtils.isBlank(sortingOrder);


        final WebItem allLink = new WebFragmentBuilder(10).
                id("attachment-sort-key-name").
                label(i18n.getText("viewissue.attachments.sort.key.name")).
                title(i18n.getText("viewissue.subtasks.tab.show.all.name")).
                styleClass(sortedByName ? "aui-list-checked aui-checked" : "aui-list-checked").
                webItem(ITEM_SECTION).
                url(baseUrl + "/browse/" + issue.getKey() + "?attachmentSortBy=fileName#attachmentmodule").build();

        final WebItem openLink = new WebFragmentBuilder(20).
                id("attachment-sort-key-date").
                label(i18n.getText("viewissue.attachments.sort.key.date")).
                title(i18n.getText("viewissue.attachments.sort.key.date")).
                styleClass(!sortedByName ? "aui-list-checked aui-checked" : "aui-list-checked").
                webItem(ITEM_SECTION).
                url(baseUrl + "/browse/" + issue.getKey() + "?attachmentSortBy=dateTime#attachmentmodule").build();

        return Lists.newArrayList(allLink, openLink);
    }
}
