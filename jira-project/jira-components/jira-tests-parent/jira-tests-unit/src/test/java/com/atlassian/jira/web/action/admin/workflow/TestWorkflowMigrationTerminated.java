/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.admin.workflow;

import java.util.Collections;
import java.util.Map;

import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestWorkflowMigrationTerminated
{
    @Test
    public void testTerminatedState()
    {
        Map<Long, String> failedMap = ImmutableMap.of(1L, "abc", 200L, "some thing");
        WorkflowMigrationResult migrationResult = new WorkflowMigrationTerminated(failedMap);
        assertEquals(WorkflowMigrationResult.TERMINATED, migrationResult.getResult());
        assertEquals(2, migrationResult.getNumberOfFailedIssues());
        assertFalse(migrationResult.getErrorCollection().hasAnyErrors());
        assertEquals(failedMap, migrationResult.getFailedIssues());

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        String errorMessage = "some error message";
        errorCollection.addErrorMessage(errorMessage);
        String errorName = "error name";
        String errorValue = "error value";
        errorCollection.addError(errorName, errorValue);
        migrationResult = new WorkflowMigrationTerminated(errorCollection);
        assertEquals(WorkflowMigrationResult.TERMINATED, migrationResult.getResult());
        assertEquals(0, migrationResult.getNumberOfFailedIssues());
        assertTrue(migrationResult.getErrorCollection().hasAnyErrors());
        assertEquals(errorMessage, migrationResult.getErrorCollection().getErrorMessages().iterator().next());
        assertEquals(errorValue, migrationResult.getErrorCollection().getErrors().get(errorName));


        try
        {
            new WorkflowMigrationTerminated((Map<Long, String>) null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Must contain failures.", e.getMessage());
        }

        try
        {
            Map<Long, String> emptyMap = Collections.emptyMap();
            new WorkflowMigrationTerminated(emptyMap);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Must contain failures.", e.getMessage());
        }

        try
        {
            new WorkflowMigrationTerminated((SimpleErrorCollection) null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Must contain errors.", e.getMessage());
        }

        try
        {
            new WorkflowMigrationTerminated(new SimpleErrorCollection());
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Must contain errors.", e.getMessage());
        }
    }

    @Test
    public void testTerminatedStateIsSerializable()
    {
        final ImmutableMap<Long, String> map = ImmutableMap.of(6969L, "Wrong", 8787L, "Error", 0L, "Mistake", -1L, "Fault");
        final WorkflowMigrationTerminated instance = new WorkflowMigrationTerminated(map);
        final WorkflowMigrationTerminated roundtrip = (WorkflowMigrationTerminated) deserialize(serialize(instance));

        assertEquals(instance.getErrorCollection(), roundtrip.getErrorCollection());
        assertEquals(instance.getResult(), roundtrip.getResult());
        assertNotSame(instance, roundtrip);
    }

}
