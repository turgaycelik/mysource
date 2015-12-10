package com.atlassian.jira.task;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.MockUser;

import org.junit.Before;
import org.junit.Test;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */

public class TestTaskDescriptorImpl
{
    private User testUser;
    private static final String PROGRESS_URL_TASK_ID = "progressURL?taskId=";
    private static final Long TASK_ID = 1L;
    private static final String TASK_DESC = "My Description";

    @Before
    public void setUp() throws Exception
    {
        testUser = new MockUser("TestTaskDescriptorImplUser");
    }

    private static class OurTaskContext implements TaskContext
    {
        public String buildProgressURL(Long taskId)
        {
            return PROGRESS_URL_TASK_ID + taskId;
        }
    }

    @Test
    public void testMainConstructor()
    {
        TaskProgressIndicator taskProgressIndicator = new NoOpTaskProgressIndicator();

        OurTaskContext ourTaskContext = new OurTaskContext();
        TaskDescriptorImpl taskDesc = new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, testUser.getName(), taskProgressIndicator, false);

        assertEquals(TASK_ID, taskDesc.getTaskId());
        assertEquals(TASK_DESC, taskDesc.getDescription());
        assertEquals(testUser.getName(), taskDesc.getUserName());
        assertEquals(taskProgressIndicator, taskDesc.getTaskProgressIndicator());
        assertSame(ourTaskContext, taskDesc.getTaskContext());
        assertNotNull(taskDesc.getSubmittedTimestamp());
        assertNull(taskDesc.getStartedTimestamp());
        assertNull(taskDesc.getFinishedTimestamp());
        assertFalse(taskDesc.isStarted());
        assertFalse(taskDesc.isFinished());

        assertEquals(0, taskDesc.getElapsedRunTime());
        assertEquals(PROGRESS_URL_TASK_ID + taskDesc.getTaskId(), taskDesc.getProgressURL());

    }

    @Test
    public void testMainConstructorNullTaskId()
    {
        TaskProgressIndicator taskProgressIndicator = new NoOpTaskProgressIndicator();
        OurTaskContext ourTaskContext = new OurTaskContext();
        // test exception throwing
        try
        {
            new TaskDescriptorImpl(null, TASK_DESC, ourTaskContext, testUser.getName(), taskProgressIndicator, false);
            fail("Expecting IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    @Test
    public void testMainConstructorNullTaskDescriptor()
    {
        TaskProgressIndicator taskProgressIndicator = new NoOpTaskProgressIndicator();
        OurTaskContext ourTaskContext = new OurTaskContext();
        try
        {
            new TaskDescriptorImpl(TASK_ID, null, ourTaskContext, testUser.getName(), taskProgressIndicator, false);
            fail("Expecting IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    @Test
    public void testMainConstructorNullUser()
    {
        TaskProgressIndicator taskProgressIndicator = new NoOpTaskProgressIndicator();
        OurTaskContext ourTaskContext = new OurTaskContext();
        try
        {
            new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, null, taskProgressIndicator, false);
        }
        catch (IllegalArgumentException e)
        {
            fail("User is optional");
        }
    }

    @Test
    public void testMainConstructorNullTaskIndicator()
    {
        OurTaskContext ourTaskContext = new OurTaskContext();
        try
        {
            new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, testUser.getName(), null, false);
        }
        catch (IllegalArgumentException e)
        {
            fail("TaskProgressInformation should be optional");
        }

    }

    @Test
    public void testMainConstructorNullTaskContext()
    {
        TaskProgressIndicator taskProgressIndicator = new NoOpTaskProgressIndicator();
        try
        {
            new TaskDescriptorImpl(TASK_ID, TASK_DESC, null, testUser.getName(), taskProgressIndicator, false);
            fail("TaskContext should not be optional");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

    }

    @Test
    public void testTaskDescriptorImpl()
    {
        OurTaskContext ourTaskContext = new OurTaskContext();
        // try the case where we have null TaskContext
        TaskDescriptorImpl taskDesc = new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, null, null, false);

        assertEquals(TASK_ID, taskDesc.getTaskId());
        assertEquals(TASK_DESC, taskDesc.getDescription());
        assertNull(taskDesc.getUserName());
        assertNull(taskDesc.getTaskProgressIndicator());
        assertSame(ourTaskContext, taskDesc.getTaskContext());
        assertEquals(PROGRESS_URL_TASK_ID + taskDesc.getTaskId(), taskDesc.getProgressURL());

        assertNotNull(taskDesc.getSubmittedTimestamp());
        assertNull(taskDesc.getStartedTimestamp());
        assertNull(taskDesc.getFinishedTimestamp());
        assertFalse(taskDesc.isStarted());
        assertFalse(taskDesc.isFinished());

        assertEquals(0, taskDesc.getElapsedRunTime());

        // test that finished cant be set before started
        try
        {
            taskDesc.setFinishedTimestamp();
            fail("Should not allow finsihed to be set before started");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    @Test
    public void testTaskDescriptorImplContructorNull() throws Exception
    {
        try
        {
            new TaskDescriptorImpl(null);
            fail("Should not allow null params");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testCopyContructor() throws Exception
    {
        OurTaskContext ourTaskContext = new OurTaskContext();
        TaskDescriptorImpl taskDesc = new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, testUser.getName(), null, false);

        TaskDescriptorImpl taskClone = new TaskDescriptorImpl(taskDesc);

        assertEquals(taskDesc.getTaskId(), taskClone.getTaskId());
        assertEquals(taskDesc.getDescription(), taskClone.getDescription());
        assertSame(taskDesc.getUserName(), taskClone.getUserName());
        assertNull(taskClone.getTaskProgressIndicator());
        assertSame(ourTaskContext, taskClone.getTaskContext());
        assertEquals(taskDesc.getProgressURL(), taskClone.getProgressURL());

        assertEquals(taskDesc.getSubmittedTimestamp(), taskClone.getSubmittedTimestamp());
        assertEquals(taskDesc.getStartedTimestamp(), taskClone.getStartedTimestamp());
        assertEquals(taskDesc.getFinishedTimestamp(), taskClone.getFinishedTimestamp());
        assertEquals(taskDesc.isStarted(), taskClone.isStarted());
        assertEquals(taskDesc.isFinished(), taskClone.isFinished());

        assertEquals(taskDesc.getElapsedRunTime(), taskClone.getElapsedRunTime());

        // check that mutable stuff to one does not affect the clone
        taskDesc.setStartedTimestamp();

        assertFalse(taskClone.isStarted());
        assertNull(taskClone.getStartedTimestamp());

        assertFalse(taskClone.isFinished());
        assertNull(taskClone.getFinishedTimestamp());

        taskDesc.setFinishedTimestamp();

        assertFalse(taskClone.isStarted());
        assertNull(taskClone.getStartedTimestamp());

        assertFalse(taskClone.isFinished());
        assertNull(taskClone.getFinishedTimestamp());
    }

    @Test
    public void testGetElapsedTime() throws Exception
    {
        OurTaskContext ourTaskContext = new OurTaskContext();
        TaskDescriptorImpl taskDesc = new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, testUser.getName(), null, false);

        //there should be no elapsed time when started.
        long oldElapsedTime = taskDesc.getElapsedRunTime();
        assertEquals(0, oldElapsedTime);
        oldElapsedTime = taskDesc.getElapsedRunTime();
        assertEquals(0, oldElapsedTime);
        oldElapsedTime = taskDesc.getElapsedRunTime();
        assertEquals(0, oldElapsedTime);

        //elapsed time should increase while the task is started but not finished.
        taskDesc.setStartedTimestamp();
        long newElapsedTime = taskDesc.getElapsedRunTime();
        assertTrue(oldElapsedTime <= newElapsedTime);
        oldElapsedTime = newElapsedTime;

        //once finished, the elapsed time should remain static.
        taskDesc.setFinishedTimestamp();
        newElapsedTime = taskDesc.getElapsedRunTime();
        assertTrue(oldElapsedTime <= newElapsedTime);
        oldElapsedTime = newElapsedTime;

        newElapsedTime = taskDesc.getElapsedRunTime();
        assertEquals(oldElapsedTime, newElapsedTime);
        newElapsedTime = taskDesc.getElapsedRunTime();
        assertEquals(oldElapsedTime, newElapsedTime);

        taskDesc = new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, testUser.getName(), null, false);
        try
        {
            taskDesc.setFinishedTimestamp();
            fail("Should not be able to set the finished time before it is started.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        taskDesc.setStartedTimestamp();
        try
        {
            taskDesc.setStartedTimestamp();
            fail("Should not be able to set the started time twice.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        taskDesc.setFinishedTimestamp();
        try
        {
            taskDesc.setFinishedTimestamp();
            fail("Should not be able to set the finished time twice.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }
    }

    @Test
    public void instancesShouldBeSerializable()
    {
        TaskProgressIndicator taskProgressIndicator = new TaskProgressAdapter();
        OurTaskContext ourTaskContext = new OurTaskContext();
        TaskDescriptorImpl taskDesc = new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, testUser.getName(), taskProgressIndicator, false);

        final FutureTask<Object> futureTask = new FutureTask<Object>(new Callable<Object>()
        {
            @Override
            public Object call() throws Exception
            {
                throw new UnsupportedOperationException("Not implemented");
            }
        });

        // Invoke
        final TaskDescriptorImpl roundTrippedTaskDesc = (TaskDescriptorImpl) deserialize(serialize(taskDesc));

        // Equals is not implemented so we just assert we got something new.
        assertNotSame(taskDesc, roundTrippedTaskDesc);
    }

    class NoOpTaskProgressIndicator implements TaskProgressIndicator
    {
        public void addListener(TaskProgressListener listener)
        {
        }

        public void removeListener (TaskProgressListener listener)
        {
        }

        public Collection /*TaskProgressEvent*/ getLastProgressEvents()
        {
            return null;
        }

        public TaskProgressEvent getLastProgressEvent()
        {
            return null;
        }
    }
}
