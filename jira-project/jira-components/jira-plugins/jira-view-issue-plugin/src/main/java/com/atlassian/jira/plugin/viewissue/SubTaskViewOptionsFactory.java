package com.atlassian.jira.plugin.viewissue;

import java.util.Map;

import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.issue.Issue;
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

/**
 * Factory to return the options for the different views for subtask list (All, Unresolved)
 *
 * @since v4.4
 */
public class SubTaskViewOptionsFactory implements WebItemProvider
{
    private static final String ITEM_SECTION = "com.atlassian.jira.jira-view-issue-plugin:view-subtasks/drop/subtask-view-options";

    private final VelocityRequestContextFactory requestContextFactory;
    private final JiraAuthenticationContext authenticationContext;

    public SubTaskViewOptionsFactory(VelocityRequestContextFactory requestContextFactory, JiraAuthenticationContext authenticationContext)
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
        final String subTaskView = (String) session.getAttribute(SessionKeys.SUB_TASK_VIEW);
        boolean showingAll = SubTaskBean.SUB_TASK_VIEW_DEFAULT.equals(SubTaskBean.SUB_TASK_VIEW_ALL);
        if (StringUtils.isNotBlank(subTaskView))
        {
            showingAll = subTaskView.equals(SubTaskBean.SUB_TASK_VIEW_ALL);
        }

        final WebItem allLink = new WebFragmentBuilder(10).
                id("subtasks-show-all").
                label(i18n.getText("viewissue.subtasks.tab.show.all.subtasks")).
                title(i18n.getText("viewissue.subtasks.tab.show.all.subtasks")).
                styleClass(showingAll ? "aui-list-checked aui-checked" : "aui-list-checked").
                webItem(ITEM_SECTION).
                url(baseUrl + "/browse/" + issue.getKey() + "?subTaskView=all#issuetable").build();
        final WebItem openLink = new WebFragmentBuilder(20).
                id("subtasks-show-open").
                label(i18n.getText("viewissue.subtasks.tab.show.open.subtasks")).
                title(i18n.getText("viewissue.subtasks.tab.show.open.subtasks")).
                styleClass(!showingAll ? "aui-list-checked aui-checked" : "aui-list-checked").
                webItem(ITEM_SECTION).
                url(baseUrl + "/browse/" + issue.getKey() + "?subTaskView=unresolved#issuetable").build();

        return CollectionBuilder.list(allLink, openLink);
    }
}
