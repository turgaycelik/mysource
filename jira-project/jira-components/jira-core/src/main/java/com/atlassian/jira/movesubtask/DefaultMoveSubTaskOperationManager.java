/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.movesubtask;

import com.atlassian.jira.movesubtask.operation.MoveSubTaskOperation;
import com.atlassian.jira.movesubtask.operation.MoveSubTaskParentOperation;
import com.atlassian.jira.movesubtask.operation.MoveSubTaskTypeOperation;
import com.atlassian.jira.util.JiraUtils;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.Collection;
import java.util.Map;

public class DefaultMoveSubTaskOperationManager implements MoveSubTaskOperationManager
{
    private Map moveSubTaskOperations;

    public DefaultMoveSubTaskOperationManager()
    {
        moveSubTaskOperations = new ListOrderedMap();
        moveSubTaskOperations.put(MoveSubTaskTypeOperation.NAME_KEY, JiraUtils.loadComponent(MoveSubTaskTypeOperation.class));
        moveSubTaskOperations.put(MoveSubTaskParentOperation.NAME_KEY, JiraUtils.loadComponent(MoveSubTaskParentOperation.class));
    }

    public Collection getMoveSubTaskOperations()
    {
        return moveSubTaskOperations.values();
    }

    public MoveSubTaskOperation getOperation(String operationName)
    {
        return (MoveSubTaskOperation) getBulkOperationsMap().get(operationName);
    }

    public boolean isValidOperation(String operationName)
    {
        return getBulkOperationsMap().containsKey(operationName);
    }

    protected Map getBulkOperationsMap()
    {
        return moveSubTaskOperations;
    }
}
