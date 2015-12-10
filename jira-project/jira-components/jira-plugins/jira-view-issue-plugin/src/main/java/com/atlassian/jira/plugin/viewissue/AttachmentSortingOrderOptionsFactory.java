package com.atlassian.jira.plugin.viewissue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.model.WebFragmentBuilder;
import com.atlassian.plugin.web.api.provider.WebItemProvider;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Factory to return the options for the different sorting order options
 *
 * @since v5.0
 */
public class AttachmentSortingOrderOptionsFactory implements WebItemProvider
{
    private static final String ITEM_SECTION = "com.atlassian.jira.jira-view-issue-plugin:attachmentmodule/drop/attachment-sorting-order-options";
    private final VelocityRequestContextFactory requestContextFactory;
    private final JiraAuthenticationContext authenticationContext;

    public AttachmentSortingOrderOptionsFactory(VelocityRequestContextFactory requestContextFactory, JiraAuthenticationContext authenticationContext)
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
        final String sortingOrder = (String) session.getAttribute(SessionKeys.VIEWISSUE_ATTACHMENT_ORDER);

        boolean sortedAscending = "asc".equals(sortingOrder) || StringUtils.isBlank(sortingOrder);

        final WebItem allLink = new WebFragmentBuilder(10).
                id("attachment-sort-direction-asc").
                label(i18n.getText("viewissue.attachments.sort.direction.asc")).
                title(i18n.getText("viewissue.attachments.sort.direction.asc")).
                styleClass(sortedAscending ? "aui-list-checked aui-checked" : "aui-list-checked").
                webItem(ITEM_SECTION).
                url(baseUrl + "/browse/" + issue.getKey() + "?attachmentOrder=asc#attachmentmodule").build();

        final WebItem openLink = new WebFragmentBuilder(20).
                id("attachment-sort-direction-desc").
                label(i18n.getText("viewissue.attachments.sort.direction.desc")).
                title(i18n.getText("viewissue.attachments.sort.direction.desc")).
                styleClass(!sortedAscending ? "aui-list-checked aui-checked" : "aui-list-checked").
                webItem(ITEM_SECTION).
                url(baseUrl + "/browse/" + issue.getKey() + "?attachmentOrder=desc#attachmentmodule").build();

        return CollectionBuilder.list(allLink, openLink);
    }
}
