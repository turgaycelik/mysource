package com.atlassian.jira.web.action.history;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.util.json.JSONEscaper;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An action to show recent issue history.  Used by the history drop down menu
 *
 * @since v3.13
 */
public class RecentIssueHistory extends JiraWebActionSupport
{
    private boolean json;
    final private UserIssueHistoryManager userHistoryManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public RecentIssueHistory(UserIssueHistoryManager userHistoryManager, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.userHistoryManager = userHistoryManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public boolean isJson()
    {
        return json;
    }

    public void setJson(final boolean json)
    {
        this.json = json;
    }

    /**
     * @return a collection of IssueHistoryBean objects that represent recent issue history
     */
    public Collection<IssueHistoryBean> getHistoryItems()
    {
        final List<IssueHistoryBean> recentHistory = new ArrayList<IssueHistoryBean>();

        final List<Issue> history = userHistoryManager.getShortIssueHistory(jiraAuthenticationContext.getLoggedInUser());
        for (Issue issue : history)
        {
            recentHistory.add(new IssueHistoryBean(issue.getKey(), issue.getSummary()));
        }
        return recentHistory;
    }

    /**
     * A simple presentation bean to make the JSP markup easier
     *
     * @since v3.13
     */
    public static class IssueHistoryBean
    {
        private final String issueKey;
        private final String summary;
        private final String shortSummary;

        public IssueHistoryBean(final String issueKey, final String summary)
        {
            this.issueKey = issueKey;
            this.summary = summary;
            //
            // make sure we crop first before we pass it through the
            // web work HTML encoding.  This will fix JRA-14401
            //
            //this.shortSummary = StringUtils.crop(this.summary, 80, " ...");
            shortSummary = this.summary;
        }

        public String getIssueKey()
        {
            return issueKey;
        }

        public String getSummary()
        {
            return summary;
        }

        public String getShortSummary()
        {
            return shortSummary;
        }
    }

    /**
     * Encodes the String from a JSON point of view
     *
     * @param jsonStr a String to JSON escape
     * @return an escaped string
     */
    public String jsonEscape(final String jsonStr)
    {
        return JSONEscaper.escape(jsonStr);
    }
}
