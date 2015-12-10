package com.atlassian.jira.task;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.fail;

public class TestStepTaskProgressSink
{

    private static final String SUB_TASK_A = "SubTaskA";
    private static final String MESSAGE_A = "MessageA";

    @Test
    public void testStepTaskProcessSinkCtorNullSinkDelegate()
    {
        try
        {
            new StepTaskProgressSink(10, 100, 90, null);
            fail("An illegal argument exception should be thrown");
        }
        catch (IllegalArgumentException e)
        {
            //this is expected.
        }
    }

    @Test
    public void testStepTaskProcessSinkCtorStartProgressGreaterThanEnd()
    {
        try
        {
            new StepTaskProgressSink(100, 10, 90, TaskProgressSink.NULL_SINK);
            fail("An illegal argument exception should be thrown");
        }
        catch (IllegalArgumentException e)
        {
            //this is expected.
        }

    }

    @Test
    public void testStepTaskProcessSinkCtorNegativeActions()
    {
        try
        {
            new StepTaskProgressSink(10, 100, -9, TaskProgressSink.NULL_SINK);
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
        mockSink.makeProgress(33, null, MESSAGE_A);
        mockSink.makeProgress(67, null, null);
        mockSink.makeProgress(100, SUB_TASK_A, null);
        mockSink.makeProgress(0, null, null);
        mockSink.makeProgress(100, null, null);

        control.replay();

        TaskProgressSink actionSink = new StepTaskProgressSink(0, 100, 3, mockSink);
        actionSink.makeProgress(0, SUB_TASK_A, MESSAGE_A);
        actionSink.makeProgress(1, null, MESSAGE_A);
        actionSink.makeProgress(2, null, null);
        actionSink.makeProgress(3, SUB_TASK_A, null);
        actionSink.makeProgress(Long.MIN_VALUE, null, null);
        actionSink.makeProgress(Long.MAX_VALUE, null, null);

        control.verify();
        control.reset();

        mockSink.makeProgress(-10, SUB_TASK_A, MESSAGE_A);
        mockSink.makeProgress(-6, null, MESSAGE_A);
        mockSink.makeProgress(-5, null, null);
        mockSink.makeProgress(-8, SUB_TASK_A, null);

        control.replay();

        actionSink = new StepTaskProgressSink(-10, -5, 7, mockSink);
        actionSink.makeProgress(0, SUB_TASK_A, MESSAGE_A);
        actionSink.makeProgress(5, null, MESSAGE_A);
        actionSink.makeProgress(7, null, null);
        actionSink.makeProgress(3, SUB_TASK_A, null);

        control.verify();
    }

}
