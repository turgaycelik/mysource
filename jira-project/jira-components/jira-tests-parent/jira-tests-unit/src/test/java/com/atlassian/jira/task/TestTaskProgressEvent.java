package com.atlassian.jira.task;

import java.util.Date;

import org.junit.Test;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * Test for the {@link com.atlassian.jira.task.TaskProgressEvent} class.
 *
 * @since v3.13
 */

public class TestTaskProgressEvent
{
    private static final Long TASK_ID = new Long(1);

    @Test
    public void testTaskProcessEvent()
    {
        String message = "MY test event";
        String subTask = "Sub task";
        long elapsedTime = 102L;
        long progress = 99;
        Date startDate = new Date();

        TaskProgressEvent event = new TaskProgressEvent(TASK_ID, elapsedTime, progress, subTask, message);

        assertEquals(TASK_ID, event.getTaskId());
        assertEquals(message, event.getMessage());
        assertEquals(subTask, event.getCurrentSubTask());
        assertEquals(elapsedTime, event.getElapsedRunTime());
        assertEquals(progress, event.getTaskProgress());

        //make sure it is created after the test started.
        assertTrue(event.getCreationTimeStamp().compareTo(startDate) >= 0);
    }

    @Test
    public void instancesShouldBeSerializable()
    {
        // Set up
        String message = "MY test event";
        String subTask = "Sub task";
        long elapsedTime = 102L;
        long progress = 99;
        final TaskProgressEvent event = new TaskProgressEvent(TASK_ID, elapsedTime, progress, subTask, message);
        // Invoke
        final TaskProgressEvent roundTrippedEvent = (TaskProgressEvent) deserialize(serialize(event));

        // Check
        assertEquals(event.getTaskId(), roundTrippedEvent.getTaskId());
        assertEquals(event.getMessage(), roundTrippedEvent.getMessage());
        assertEquals(event.getElapsedRunTime(), roundTrippedEvent.getElapsedRunTime());
        assertEquals(event.getCurrentSubTask(), roundTrippedEvent.getCurrentSubTask());
        assertEquals(event.getCreationTimeStamp(), roundTrippedEvent.getCreationTimeStamp());
        assertEquals(event.getTaskProgress(), roundTrippedEvent.getTaskProgress());
        assertNotSame(event, roundTrippedEvent);
    }
}
