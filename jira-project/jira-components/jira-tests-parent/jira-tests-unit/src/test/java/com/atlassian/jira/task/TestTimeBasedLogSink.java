package com.atlassian.jira.task;

import com.atlassian.jira.logging.QuietCountingLogger;
import org.apache.log4j.Logger;
import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */

public class TestTimeBasedLogSink
{
    private static final String MESSAGE_A = "MessageA";
    private static final String SUB_TASK_A = "SubTaskA";

    @Test
    public void testTimeBasedLogSink()
    {
        try
        {
            new TimeBasedLogSink(null, "Description", 1000, TaskProgressSink.NULL_SINK);
            fail("Should throw a NPE.");
        }
        catch (RuntimeException e)
        {
            //expected.
        }

        try
        {
            new TimeBasedLogSink(createLogger(), null, 100, TaskProgressSink.NULL_SINK);
            fail("Should throw a NPE.");
        }
        catch (RuntimeException e)
        {
            //expected.
        }

        try
        {
            new TimeBasedLogSink(createLogger(), "Description", -10, TaskProgressSink.NULL_SINK);
            fail("Should throw an IllegalArgumentException.");
        }
        catch (RuntimeException e)
        {
            //expected.
        }

    }

    private Logger createLogger()
    {
        return Logger.getLogger(getClass());
    }

    @Test
    public void testMakeProgress()
    {

        MockControl mockSinkControl = MockControl.createControl(TaskProgressSink.class);
        TaskProgressSink mockSink = (TaskProgressSink) mockSinkControl.getMock();
        mockSink.makeProgress(50, SUB_TASK_A, MESSAGE_A);
        mockSink.makeProgress(50, SUB_TASK_A, null);
        mockSink.makeProgress(51, null, MESSAGE_A);

        mockSinkControl.replay();

        QuietCountingLogger quietCountingLogger = QuietCountingLogger.create(getClass().getName());

        TimeBasedLogSink timeSink = new TimeBasedLogSink(quietCountingLogger, "Description", Long.MAX_VALUE, mockSink);
        timeSink.makeProgress(50, SUB_TASK_A, MESSAGE_A);
        timeSink.makeProgress(50, SUB_TASK_A, null);
        timeSink.makeProgress(51, null, MESSAGE_A);

        mockSinkControl.verify();

        assertEquals(2, quietCountingLogger.getCount());
    }

    /**
     * With a time difference of ZERO, all events should be logged.
     */

    @Test
    public void testMakeProgressTimeZero()
    {

        MockControl mockSinkControl = MockControl.createControl(TaskProgressSink.class);
        TaskProgressSink mockSink = (TaskProgressSink) mockSinkControl.getMock();
        mockSink.makeProgress(50, SUB_TASK_A, MESSAGE_A);
        mockSink.makeProgress(50, SUB_TASK_A, null);
        mockSink.makeProgress(51, null, MESSAGE_A);

        mockSinkControl.replay();

        QuietCountingLogger quietCountingLogger = QuietCountingLogger.create(getClass().getName());

        TimeBasedLogSink timeSink = new TimeBasedLogSink(quietCountingLogger, "Description", 0, mockSink);
        timeSink.makeProgress(50, SUB_TASK_A, MESSAGE_A);
        timeSink.makeProgress(50, SUB_TASK_A, null);
        timeSink.makeProgress(51, null, MESSAGE_A);

        mockSinkControl.verify();

        assertEquals(3, quietCountingLogger.getCount());
    }
}
