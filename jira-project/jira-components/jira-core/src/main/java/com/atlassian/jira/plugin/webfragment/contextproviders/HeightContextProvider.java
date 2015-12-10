package com.atlassian.jira.plugin.webfragment.contextproviders;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.opensymphony.util.TextUtils;

import java.util.List;
import java.util.Map;

public class HeightContextProvider extends AbstractJiraContextProvider
{
    private final ApplicationProperties applicationProperties;
    private final UserIssueHistoryManager userHistoryManager;

    public HeightContextProvider(ApplicationProperties applicationProperties, UserIssueHistoryManager userHistoryManager)
    {
        this.applicationProperties = applicationProperties;
        this.userHistoryManager = userHistoryManager;
    }

    @Override
    public Map getContextMap(User user, JiraHelper jiraHelper)
    {
        final List<Issue> history = userHistoryManager.getShortIssueHistory(user);

        int logoHeight = TextUtils.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_HEIGHT));
        String historyHeight = String.valueOf(80 + logoHeight + (25 * history.size()));
        String filterHeight = String.valueOf(205 + logoHeight);
        return EasyMap.build("historyWindowHeight", historyHeight,
                "filtersWindowHeight", filterHeight);
    }
}
