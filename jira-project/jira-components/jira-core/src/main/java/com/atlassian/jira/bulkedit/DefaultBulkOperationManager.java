/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit;

import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.bulkedit.operation.BulkDeleteOperation;
import com.atlassian.jira.bulkedit.operation.BulkEditOperation;
import com.atlassian.jira.bulkedit.operation.BulkMigrateOperation;
import com.atlassian.jira.bulkedit.operation.BulkOperation;
import com.atlassian.jira.bulkedit.operation.BulkUnwatchOperation;
import com.atlassian.jira.bulkedit.operation.BulkWatchOperation;
import com.atlassian.jira.bulkedit.operation.BulkWorkflowTransitionOperation;
import com.atlassian.jira.bulkedit.operation.ProgressAwareBulkOperation;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class DefaultBulkOperationManager implements BulkOperationManager
{
    private static final Logger log = Logger.getLogger(DefaultBulkOperationManager.class);
    private final WatcherService watcherService;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    private final Map<String, ProgressAwareBulkOperation> systemBulkOperations;
    private final Map<String, ProgressAwareBulkOperation> systemBulkWatchOperations;
    private final Map<String, ProgressAwareBulkOperation> pluginProvidedBulkOperations = Maps.newLinkedHashMap();

    public DefaultBulkOperationManager(final JiraAuthenticationContext jiraAuthenticationContext,
                                       final WatcherService watcherService,
                                       final BulkEditOperation bulkEditOperation,
                                       final BulkMigrateOperation bulkMigrateOperation,
                                       final BulkWorkflowTransitionOperation bulkWorkflowTransitionOperation,
                                       final BulkDeleteOperation bulkDeleteOperation,
                                       final BulkWatchOperation bulkWatchOperation,
                                       final BulkUnwatchOperation bulkUnwatchOperation)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.watcherService = watcherService;

        systemBulkOperations = ImmutableMap.of(bulkEditOperation.getNameKey(), bulkEditOperation,
                bulkMigrateOperation.getNameKey(), bulkMigrateOperation,
                bulkWorkflowTransitionOperation.getNameKey(), bulkWorkflowTransitionOperation,
                bulkDeleteOperation.getNameKey(), bulkDeleteOperation);

        systemBulkWatchOperations = ImmutableMap.of(
                bulkWatchOperation.getNameKey(), bulkWatchOperation,
                bulkUnwatchOperation.getNameKey(), bulkUnwatchOperation);

    }

    @Override
    public Collection<BulkOperation> getBulkOperations()
    {
        Collection<BulkOperation> legacyBulkOperations = new ArrayList<BulkOperation>();
        for (ProgressAwareBulkOperation bulkOperation : getBulkOperationsMap().values())
        {
            if (bulkOperation instanceof ProgressAwareBulkOperationWrapper)
                legacyBulkOperations.add(((ProgressAwareBulkOperationWrapper) bulkOperation).getLegacyBulkOperation());
        }
        return legacyBulkOperations;
    }

    @Override
    public Collection<ProgressAwareBulkOperation> getProgressAwareBulkOperations()
    {
        return getBulkOperationsMap().values();
    }

    @Override
    public BulkOperation getOperation(final String operationName)
    {
        ProgressAwareBulkOperation bulkOperation = getBulkOperationsMap().get(operationName);
        if (bulkOperation instanceof ProgressAwareBulkOperationWrapper)
            return ((ProgressAwareBulkOperationWrapper) bulkOperation).getLegacyBulkOperation();
        return null;
    }

    @Override
    public ProgressAwareBulkOperation getProgressAwareOperation(final String operationName)
    {
        return getBulkOperationsMap().get(operationName);
    }

    public boolean isValidOperation(final String operationName)
    {
        return getBulkOperationsMap().containsKey(operationName);
    }

    public void addBulkOperation(final String operationName, final Class<? extends BulkOperation> componentClass)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Adding Bulk Operation " + operationName + " with class " + componentClass);
        }
        BulkOperation legacyBulkOperation = JiraUtils.loadComponent(componentClass);
        pluginProvidedBulkOperations.put(operationName, new ProgressAwareBulkOperationWrapper(legacyBulkOperation));
    }

    public void addProgressAwareBulkOperation(final String operationName,
                                              final Class<? extends ProgressAwareBulkOperation> componentClass)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Adding Bulk Operation " + operationName + " with class " + componentClass);
        }
        pluginProvidedBulkOperations.put(operationName, JiraUtils.loadComponent(componentClass));
    }

    protected Map<String, ProgressAwareBulkOperation> getBulkOperationsMap()
    {
        final MapBuilder<String, ProgressAwareBulkOperation> results = MapBuilder.newBuilder();

        results.addAll(systemBulkOperations);

        // Only add watch/unwatch to the list if we're logged in and watching is enabled on the instance
        if (jiraAuthenticationContext.isLoggedInUser() && watcherService.isWatchingEnabled())
        {
            results.addAll(systemBulkWatchOperations);
        }

        results.addAll(pluginProvidedBulkOperations);
        return results.toListOrderedMap();
    }
}
