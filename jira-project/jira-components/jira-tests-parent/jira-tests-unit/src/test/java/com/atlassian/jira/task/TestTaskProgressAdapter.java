package com.atlassian.jira.task;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.MockUser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** @since v3.13 */

public class TestTaskProgressAdapter
{
    @Test
    public void testProgressEvents()
    {

        final TaskProgressAdapter adapter = new TaskProgressAdapter();
        adapter.setTaskDescriptor(createTD());

        // nothing should be in the pipe to start with
        assertNull(adapter.getLastProgressEvent());

        adapter.makeProgress(27, "some subtasks", "Some progress with no listeners");
        assertNotNull(adapter.getLastProgressEvent());

        final AtomicLong callCount = new AtomicLong(0);
        final String msg = "A task message";
        final String subTask = "The current subtask";
        final long progress = 34;
        final Date then = new Date();

        final TaskProgressListener tpl1 = new TaskProgressListener()
        {
            public void onProgressMade(final TaskProgressEvent event)
            {
                assertEquals(msg, event.getMessage());
                assertEquals(subTask, event.getCurrentSubTask());
                assertEquals(progress, event.getTaskProgress());
                assertNotNull(event.getCreationTimeStamp());
                assertTrue(event.getCreationTimeStamp().getTime() >= then.getTime());
                callCount.incrementAndGet();
            }
        };
        adapter.addListener(tpl1);
        adapter.makeProgress(progress, subTask, msg);
        assertEquals(1, callCount.get());

        assertNotNull(adapter.getLastProgressEvent());

        // we allow the same listener object to be registered twice and called twice
        callCount.set(0);
        adapter.addListener(tpl1);
        adapter.makeProgress(progress, subTask, msg);
        assertEquals(2, callCount.get());

        assertNotNull(adapter.getLastProgressEvent());

        // should still call the second registered instance
        callCount.set(0);
        adapter.removeListener(tpl1);
        adapter.makeProgress(progress, subTask, msg);
        assertEquals(1, callCount.get());

        assertNotNull(adapter.getLastProgressEvent());

        // should now be empty of listeners but have the event in the list of previous progress events
        callCount.set(0);
        adapter.removeListener(tpl1);
        adapter.makeProgress(progress, subTask, msg);
        assertEquals(0, callCount.get());

        assertNotNull(adapter.getLastProgressEvent());
    }

    private TaskDescriptor createTD()
    {
        final TaskContext taskContext = new TaskContext()
        {

            public String buildProgressURL(final Long taskId)
            {
                return null;
            }
        };

        final User mockUser = new MockUser("TestTaskProgressAdapter");

        return new TaskDescriptorImpl(new Long(123), "Static TD", taskContext, mockUser.getName(), null, false);
    }
}
