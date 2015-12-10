/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.admin.workflow;

import java.util.Collections;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestWorkflowMigrationSuccess
{
    @Test
    public void testSuccessState()
    {
        WorkflowMigrationResult migrationResult = new WorkflowMigrationSuccess(null);
        assertEquals(WorkflowMigrationResult.SUCCESS, migrationResult.getResult());
        assertEquals(0, migrationResult.getNumberOfFailedIssues());
        assertFalse(migrationResult.getErrorCollection().hasAnyErrors());

        migrationResult = new WorkflowMigrationSuccess(Collections.EMPTY_MAP);
        assertEquals(WorkflowMigrationResult.SUCCESS, migrationResult.getResult());
        assertEquals(0, migrationResult.getNumberOfFailedIssues());
        assertFalse(migrationResult.getErrorCollection().hasAnyErrors());

        Map failedMap = EasyMap.build(new Long(1), "abc", new Long(200), "some thing");
        migrationResult = new WorkflowMigrationSuccess(failedMap);
        assertEquals(WorkflowMigrationResult.SUCCESS, migrationResult.getResult());
        assertEquals(2, migrationResult.getNumberOfFailedIssues());
        assertFalse(migrationResult.getErrorCollection().hasAnyErrors());
        assertEquals(failedMap, migrationResult.getFailedIssues());
    }
}
