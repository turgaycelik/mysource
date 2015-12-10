/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.IndexingConfiguration;
import com.atlassian.jira.event.ListenerManager;
import com.atlassian.jira.index.ha.ReplicatedIndexManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.johnson.event.Event;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * This index manager ignores calls to {@link #reIndex(org.ofbiz.core.entity.GenericValue) },
 *  {@link #deIndex(org.ofbiz.core.entity.GenericValue) } and
 * {@link #reIndexIssues(Collection)}.
 * <p>
 * <b>It should only be used where the indexer will manually call {@link #reIndexAll()}!</b>
 */
public class BulkOnlyIndexManager extends DefaultIndexManager
{
    /**
     * private only for testing purposes (use the factory method to get an instance)
     */
    public BulkOnlyIndexManager(final IndexingConfiguration properties, final IssueIndexer issueIndexer, final IndexPathManager indexPathManager,
            final ReindexMessageManager reindexMessageManager, final EventPublisher eventPublisher, ListenerManager listenerManager, ProjectManager projectManager, TaskManager taskManager, OfBizDelegator ofBizDelegator, ReplicatedIndexManager replicatedIndexManager, final IssueManager issueManager)
    {
        super(properties, issueIndexer, indexPathManager, reindexMessageManager, eventPublisher, listenerManager, projectManager, issueManager, taskManager, ofBizDelegator, replicatedIndexManager);
    }

    @Override
    public void deIndex(final Issue issue) throws IndexException
    {
    //do nothing
    }

    @Override
    public void deIndex(final GenericValue entity) throws IndexException
    {
    //do nothing
    }

    @Override
    public void reIndex(final GenericValue entity) throws IndexException
    {
    //do nothing
    }

    @Override
    public void reIndex(final Issue entity) throws IndexException
    {
    //do nothing
    }

    @Override
    public long reIndexAll() throws IndexException
    {
        return -1; //do nothing
    }

    public long reIndexAll(final Event event) throws IndexException
    {
        return -1; //do nothing
    }

    @Override
    public long reIndexIssueObjects(final Collection<? extends Issue> issueObjects) throws IndexException
    {
        return -1; //do nothing
    }

    public long reIndexIssues(final IssuesIterable issuesIterable, final Event event) throws IndexException
    {
        return -1; //do nothing
    }

    @Override
    public long reIndexIssues(final Collection<GenericValue> issues) throws IndexException
    {
        return -1; //do nothing
    }

    @Override
    public void hold()
    {
        return;  //do nothing
    }

    @Override
    public boolean isHeld()
    {
        return false;
    }

    @Override
    public long release() throws IndexException
    {
        return -1; //do nothing
    }

    @Override
    public long optimize()
    {
        return -1;
    }

    @Override
    public String toString()
    {
        return "BulkOnlyIndexManager: paths: " + getAllIndexPaths();
    }
}