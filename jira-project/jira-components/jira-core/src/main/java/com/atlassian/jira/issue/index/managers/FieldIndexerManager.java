package com.atlassian.jira.issue.index.managers;

import com.atlassian.jira.issue.index.indexers.FieldIndexer;

import java.util.Collection;

public interface FieldIndexerManager
{
    Collection<FieldIndexer> getAllIssueIndexers();

    void refresh();
}
