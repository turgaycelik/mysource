package com.atlassian.jira.index.ha;

import java.util.Collection;
import java.util.Set;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.sharing.SharedEntity;

/**
 * Writes ReplicatedIndexOperations to the database - as this class is called from the finally in the indexManagers
 * you must not throw any exceptions.  Log them and return
 *
 * @since v6.1
 */
public interface ReplicatedIndexManager
{
    void reindexIssues(IssuesIterable issuesIterable);

    void reindexComments(Collection<Comment> comments);

    void deIndexIssues(Set<Issue> issuesToDelete);

    void indexSharedEntity(SharedEntity entity);

    void deIndexSharedEntity(SharedEntity entity);

    void reindexProject(Project project);
}
