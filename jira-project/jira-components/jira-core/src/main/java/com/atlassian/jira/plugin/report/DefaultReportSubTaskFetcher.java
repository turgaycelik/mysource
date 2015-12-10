package com.atlassian.jira.plugin.report;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Predicates;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.web.bean.PagerFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jira.plugin.report.SubTaskInclusionOption.ASSIGNED_AND_UNASSIGNED;
import static com.atlassian.jira.plugin.report.SubTaskInclusionOption.ONLY_ASSIGNED;
import static com.atlassian.jira.plugin.report.SubTaskInclusionOption.ONLY_SELECTED_VERSION;
import static com.atlassian.jira.plugin.report.SubTaskInclusionOption.SELECTED_AND_BLANK_VERSIONS;

public class DefaultReportSubTaskFetcher implements ReportSubTaskFetcher
{
    private static final Logger log = LoggerFactory.getLogger(ReportSubTaskFetcher.class);

    private final SearchProvider searchProvider;

    public DefaultReportSubTaskFetcher(SearchProvider searchProvider)
    {
        this.searchProvider = searchProvider;
    }

    @Override
    public List<Issue> getSubTasks(ApplicationUser user, List<Issue> parentIssues, SubTaskInclusionOption subtaskInclusion, boolean onlyIncludeUnresolved) throws SearchException
    {
        if (subtaskInclusion == null || subtaskInclusion.equals(ONLY_SELECTED_VERSION))
        {
            return Collections.emptyList();
        }
        if (parentIssues == null || parentIssues.isEmpty())
        {
            return Collections.emptyList();
        }

        JqlClauseBuilder queryBuilder = getQueryForSubTasks(parentIssues, onlyIncludeUnresolved);

        if (subtaskInclusion.equals(SELECTED_AND_BLANK_VERSIONS))
        {
            queryBuilder.and().fixVersionIsEmpty();
        }
        final SearchResults subtaskSearchResults = searchProvider.search(queryBuilder.buildQuery(), user, new PagerFilter(Integer.MAX_VALUE));
        return subtaskSearchResults.getIssues();
    }

    @Override
    public List<Issue> getSubTasksForUser(ApplicationUser user, List<Issue> parentIssues, SubTaskInclusionOption subtaskInclusion, boolean onlyIncludeUnresolved) throws SearchException
    {
        if (subtaskInclusion == null || subtaskInclusion.equals(ONLY_ASSIGNED))
        {
            return Collections.emptyList();
        }
        if (!subtaskInclusion.equals(ASSIGNED_AND_UNASSIGNED))
        {
            log.info("Unknown Subtask Inclusion parameter: " + subtaskInclusion);
            return Collections.emptyList();
        }
        if (parentIssues == null || parentIssues.isEmpty())
        {
            return Collections.emptyList();
        }

        final JqlClauseBuilder whereClauseBuilder = getQueryForSubTasks(parentIssues, onlyIncludeUnresolved);
        whereClauseBuilder.and().assigneeIsEmpty();
        final SearchResults subtaskSearchResults = searchProvider.search(whereClauseBuilder.buildQuery(), user, new PagerFilter(Integer.MAX_VALUE));
        return subtaskSearchResults.getIssues();
    }

    private JqlClauseBuilder getQueryForSubTasks(List<Issue> parentIssues, final boolean onlyIncludeUnresolved)
    {
        final List<Long> parentIssueIds = CollectionUtil.transform(
                CollectionUtil.filter(parentIssues, Predicates.notNull()), new Function<Issue, Long>()
        {
            public Long get(final Issue input)
            {
                return input.getId();
            }
        });
        JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().issueParent().inNumbers(parentIssueIds);
        if (onlyIncludeUnresolved)
        {
            builder = builder.and().unresolved();
        }
        return builder;
    }
}
