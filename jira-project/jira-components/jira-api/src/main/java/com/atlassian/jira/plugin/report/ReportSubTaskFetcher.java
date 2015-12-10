package com.atlassian.jira.plugin.report;

import java.util.List;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Responsible for getting SubTasks for parent Issues.
 *
 * @since v6.3
 */
@InjectableComponent
public interface ReportSubTaskFetcher
{
    /**
     * Given a list of parent issues, returns a list of subtasks visible to the user,
     * subject to the subtask inclusion policy at {@link SubTaskInclusionOption}.
     * <p/>
     * Will return an empty list if nothing found.
     *
     * @param user             For permission checks
     * @param parentIssues     A list of parent issues
     * @param subtaskInclusion A String that is one of the {@link SubTaskInclusionOption}.
     *                         If subtaskInclusion is {@code null}, an empty list is returned.
     * @param onlyIncludeUnresolved Whether to only include unresolved, or to include both resolved and unresolved issues
     *
     * @return a List of Issues that are subtasks.
     *
     * @throws SearchException if the search subsystem fails.
     */
    List<Issue> getSubTasks(ApplicationUser user, List<Issue> parentIssues, SubTaskInclusionOption subtaskInclusion, boolean onlyIncludeUnresolved) throws SearchException;

    /**
     * Given a list of parent issues, returns a list of subtasks visible to the user,
     * subject to the subtask inclusion policy at {@link SubTaskInclusionOption}.
     * <p/>
     * Will return an empty list if nothing found.
     *
     * @param user             for permission checks
     * @param parentIssues     a List of Issues
     * @param subtaskInclusion @Nullable a String that is one of the {@link SubTaskInclusionOption}
     *                         If subtaskInclusion is {@code null}, an empty list is returned.
     * @param onlyIncludeUnresolved whether to only include unresolved, or to include both resolved and unresolved issues

     * @return a List of Issues that are subtasks.
     *
     * @throws SearchException if the search subsystem fails.
     */
    List<Issue> getSubTasksForUser(ApplicationUser user, List<Issue> parentIssues, SubTaskInclusionOption subtaskInclusion, boolean onlyIncludeUnresolved) throws SearchException;
}
