package com.atlassian.jira.plugin.report.impl;

import com.atlassian.jira.issue.Issue;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.collections.set.ListOrderedSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class transforms a flat set of issues into a set of issues ({@link SubTaskingIssueDecorator ), where sub-tasks
 * that have a parent issue in this set are accessible via their parent issues rather than the resulting set directly.
 *
 * @since v3.11
 */
class IssueSubTaskTransformer
{
    private final Predicate includeSubTasksPredicate;

    public IssueSubTaskTransformer(Predicate includeSubTasksPredicate)
    {
        this.includeSubTasksPredicate = includeSubTasksPredicate;
    }

    /**
     * Returns a Set of issues are transformed such that any subtasks whose parent issues are also present are
     * accessible via the parent's {@link com.atlassian.jira.issue.Issue#getSubTaskObjects()} method. No subtasks
     * that are not present in the original set are available from the resulting issue's
     * {@link com.atlassian.jira.issue.Issue#getSubTaskObjects()} method.
     *
     * @param issues issues to transform
     * @return a Set of transformed issues, never null
     */
    Set /*<Issue>*/ getIssues(Set /*<Issue>*/ issues)
    {
        // we need to have a map of issues by ids that we can use to check and get the parents
        Map /*<Long, Issue>*/ inputIssuesById = new HashMap/*<Long, Issue>*/();
        for (final Object issue2 : issues)
        {
            Issue issue = (Issue) issue2;
            inputIssuesById.put(issue.getId(), issue);
        }
        Map /*<Long, SubTaskingIssueDecorator>*/ resultIssueMap = new ListOrderedMap();
        for (final Object issue1 : issues)
        {
            Issue issue = (Issue) issue1;
            if (!issue.isSubTask())
            {
                // issue, not a sub-task
                if (!resultIssueMap.containsKey(issue.getId()))
                {
                    resultIssueMap.put(issue.getId(), new SubTaskingIssueDecorator(issue));
                }
            }
            else if (includeSubTasksPredicate.evaluate(issue)) // test that we actually want it, otherwise discard
            {
                if (!inputIssuesById.containsKey(issue.getParentId()))
                {
                    // orphan subtask
                    resultIssueMap.put(issue.getId(), new SubTaskingIssueDecorator(issue));
                }
                else
                {
                    // subtask
                    final Long parentId = issue.getParentId();
                    final SubTaskingIssueDecorator issueDecorator;
                    if (resultIssueMap.containsKey(parentId))
                    {
                        issueDecorator = (SubTaskingIssueDecorator) resultIssueMap.get(parentId);
                    }
                    else
                    {
                        final Issue parentIssue = (Issue) inputIssuesById.get(parentId);
                        resultIssueMap.put(parentId, issueDecorator = new SubTaskingIssueDecorator(parentIssue));
                    }
                    issueDecorator.addSubTask(issue);
                }
            }
        }
        Set result = new ListOrderedSet();
        result.addAll(resultIssueMap.values());
        return Collections.unmodifiableSet(result);
    }
}