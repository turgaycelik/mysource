package com.atlassian.jira.bc.issue.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;

import java.util.Collection;

/**
 * Service that is used to get a list of {@link com.atlassian.jira.issue.Issue} for the Issue Picker.
 * This uses {@link com.atlassian.jira.bc.issue.search.IssuePickerSearchProvider}s to get Issues.
 */
@PublicApi
public interface IssuePickerSearchService
{

    /**
     * Gets a list of {@link com.atlassian.jira.issue.Issue} based on query string.
     * Limited by maxIssueCount.
     *
     * @param context           Jira Service Context
     * @param issuePickerParams params for picking issues
     * @return Collection of {@link com.atlassian.jira.issue.Issue} matching search criteria.  These can be any type of issues.
     *         This collection is never nulll.
     */
    Collection<IssuePickerResults> getResults(JiraServiceContext context, IssuePickerParameters issuePickerParams);

    /**
     * Class for passing around IssuePicker parameters.
     */
    @PublicApi
    public static class IssuePickerParameters
    {
        private final String query;
        private final String currentJQL;
        private final Issue currentIssue;
        private final Project currentProject;
        private final boolean showSubTasks;
        private final boolean showSubTaskParent;
        private final int limit;

        /**
         * Constructor that takes all parameters
         *
         * @param query             the query that was inputed
         * @param currentJQL        the current JQL
         * @param currentIssue      the current issue
         * @param currentProject    the current project
         * @param showSubTasks      whether or not to show sub-tasks
         * @param showSubTaskParent whether or not to show parent
         * @param limit             how many items to display
         */
        public IssuePickerParameters(final String query, final String currentJQL, final Issue currentIssue, final Project currentProject, final boolean showSubTasks, final boolean showSubTaskParent, final int limit)
        {
            this.query = query;
            this.currentJQL = currentJQL;
            this.currentIssue = currentIssue;
            this.currentProject = currentProject;
            this.showSubTasks = showSubTasks;
            this.showSubTaskParent = showSubTaskParent;
            this.limit = limit;
        }

        public String getQuery()
        {
            return query;
        }

        public String getCurrentJQL()
        {
            return currentJQL;
        }

        public Issue getCurrentIssue()
        {
            return currentIssue;
        }

        public Project getCurrentProject()
        {
            return currentProject;
        }

        public boolean showSubTasks()
        {
            return showSubTasks;
        }

        public boolean showSubTaskParent()
        {
            return showSubTaskParent;
        }

        public int getLimit()
        {
            return limit;
        }
    }
}
