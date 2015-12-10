/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.movesubtask;

import com.atlassian.jira.movesubtask.operation.MoveSubTaskOperation;

import java.util.Collection;

public interface MoveSubTaskOperationManager
{
    public Collection getMoveSubTaskOperations();

    public boolean isValidOperation(String operationName);

    MoveSubTaskOperation getOperation(String operationName);
}
