package com.atlassian.jira.task;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.fail;

public class TestScalingTaskProgessSink
{

    private static final String SUB_TASK_A = "SubTaskA";
    private static final String MESSAGE_A = "MessageA";

    @Test
    public void testScalingTaskProgessSinkConstructorNullSink()
    {
        try
        {
            new ScalingTaskProgessSink(10, 90, null);
            fail("IllegalArgument exception expected.");
        }
        catch (IllegalArgumentException e)
        {
            //this is expected.
        }

    }

    @Test
    public void testScalingTaskProgessSinkConstructorActualStartGreaterThanEnd()
    {
        try
        {
            new ScalingTaskProgessSink(101, 90, TaskProgressSink.NULL_SINK);
            fail("An illegal argument exception should be found.");
        }
        catch (IllegalArgumentException e)
        {
            //this is expected.
        }

    }

    @Test
    public void testScalingTaskProgessSinkConstructorVirtualStartGreaterThanEnd()
    {
        try
        {
            new ScalingTaskProgessSink(0, 100, 101, 100, TaskProgressSink.NULL_SINK);
            fail("An illegal argument exception should be found.");
        }
        catch (IllegalArgumentException e)
        {
            //this is expected.
        }
    }

    @Test
    public void testMakeProgress()
    {
        MockControl control = MockControl.createControl(TaskProgressSink.class);
        TaskProgressSink mockSink = (TaskProgressSink) control.getMock();

        //check the simple percentage operations.
        mockSink.makeProgress(0, SUB_TASK_A, MESSAGE_A);
        mockSink.makeProgress(5, SUB_TASK_A, null);
        mockSink.makeProgress(10, null, null);
        mockSink.makeProgress(0, null, MESSAGE_A);
        mockSink.makeProgress(10, null, null);

        control.replay();

        TaskProgressSink scaleSink = new ScalingTaskProgessSink(0, 10, mockSink);
        scaleSink.makeProgress(0, SUB_TASK_A, MESSAGE_A);
        scaleSink.makeProgress(50, SUB_TASK_A, null);
        scaleSink.makeProgress(100, null, null);
        scaleSink.makeProgress(-1, null, MESSAGE_A);
        scaleSink.makeProgress(20000, null, null);
        control.verify();

        control.reset();
        mockSink.makeProgress(90, SUB_TASK_A, MESSAGE_A);
        mockSink.makeProgress(95, SUB_TASK_A, null);
        mockSink.makeProgress(100, null, null);
        control.replay();

        scaleSink = new ScalingTaskProgessSink(90, 100, mockSink);
        scaleSink.makeProgress(0, SUB_TASK_A, MESSAGE_A);
        scaleSink.makeProgress(50, SUB_TASK_A, null);
        scaleSink.makeProgress(100, null, null);
        control.verify();

        control.reset();
        mockSink.makeProgress(27, SUB_TASK_A, MESSAGE_A);
        mockSink.makeProgress(27, SUB_TASK_A, null);
        mockSink.makeProgress(27, null, null);
        control.replay();

        scaleSink = new ScalingTaskProgessSink(27, 27, mockSink);
        scaleSink.makeProgress(0, SUB_TASK_A, MESSAGE_A);
        scaleSink.makeProgress(50, SUB_TASK_A, null);
        scaleSink.makeProgress(100, null, null);
        control.verify();

        //does it deal with negative values correctly.

        control.reset();
        mockSink.makeProgress(-12, SUB_TASK_A, MESSAGE_A);
        mockSink.makeProgress(-11, SUB_TASK_A, null);
        mockSink.makeProgress(-10, null, null);
        control.replay();

        scaleSink = new ScalingTaskProgessSink(-12, -8, mockSink);
        scaleSink.makeProgress(0, SUB_TASK_A, MESSAGE_A);
        scaleSink.makeProgress(25, SUB_TASK_A, null);
        scaleSink.makeProgress(50, null, null);
        control.verify();

        //does it scale up correctly?

        control.reset();
        mockSink.makeProgress(200, SUB_TASK_A, MESSAGE_A);
        mockSink.makeProgress(300, SUB_TASK_A, null);
        mockSink.makeProgress(400, null, null);
        control.replay();

        scaleSink = new ScalingTaskProgessSink(200, 400, 0, 10, mockSink);
        scaleSink.makeProgress(0, SUB_TASK_A, MESSAGE_A);
        scaleSink.makeProgress(5, SUB_TASK_A, null);
        scaleSink.makeProgress(10, null, null);
        control.verify();

        //does it work with zero ranges?

        control.reset();
        mockSink.makeProgress(27, SUB_TASK_A, MESSAGE_A);
        mockSink.makeProgress(27, SUB_TASK_A, null);
        mockSink.makeProgress(27, null, null);
        control.replay();

        scaleSink = new ScalingTaskProgessSink(27, 27, 100, 100, mockSink);
        scaleSink.makeProgress(100, SUB_TASK_A, MESSAGE_A);
        scaleSink.makeProgress(100, SUB_TASK_A, null);
        scaleSink.makeProgress(100, null, null);
        control.verify();

        //does it work with negative.
        control.reset();
        mockSink.makeProgress(-20, SUB_TASK_A, MESSAGE_A);
        mockSink.makeProgress(-15, SUB_TASK_A, null);
        mockSink.makeProgress(-11, null, null);
        mockSink.makeProgress(-20, SUB_TASK_A, null);
        mockSink.makeProgress(-10, null, MESSAGE_A);
        control.replay();

        scaleSink = new ScalingTaskProgessSink(-20, -10, 0, 20, mockSink);
        scaleSink.makeProgress(0, SUB_TASK_A, MESSAGE_A);
        scaleSink.makeProgress(10, SUB_TASK_A, null);
        scaleSink.makeProgress(18, null, null);
        scaleSink.makeProgress(Long.MIN_VALUE, SUB_TASK_A, null);
        scaleSink.makeProgress(Long.MAX_VALUE, null, MESSAGE_A);
        control.verify();
    }
}
