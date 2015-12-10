package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.imports.project.taskprogress.EntityCountTaskProgressProcessor;
import com.atlassian.jira.imports.project.taskprogress.TaskProgressInterval;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.MockControl;
import org.junit.Test;

/**
 * @since v3.13
 */
public class TestEntityCountTaskProgressProcessor
{
    @Test
    public void testNullTaskProgressInterval()
    {
        EntityCountTaskProgressProcessor entityCountTaskProgressProcessor = new EntityCountTaskProgressProcessor(null, "Step 2 of 3. Testing", 1000, new MockI18nBean());
        entityCountTaskProgressProcessor.processTaskProgress("IssueType", 5, 40, 8);
        // That's all we do - just want to prove it doesn't throw NPE
    }

    @Test
    public void testProcessTaskProgress0Entities()
    {
        final MockControl mockTaskProgressSinkControl = MockControl.createStrictControl(TaskProgressSink.class);
        final TaskProgressSink mockTaskProgressSink = (TaskProgressSink) mockTaskProgressSinkControl.getMock();
        // 0 percent done in our section - 20% overall.
        mockTaskProgressSink.makeProgress(20, "Step 2 of 3. Testing. Processing IssueType", "Entity 0 of 0");
        mockTaskProgressSinkControl.replay();

        TaskProgressInterval taskProgressInterval = new TaskProgressInterval(mockTaskProgressSink, 20, 30);

        EntityCountTaskProgressProcessor entityCountTaskProgressProcessor = new EntityCountTaskProgressProcessor(taskProgressInterval, "Step 2 of 3. Testing", 0, new MockI18nBean());
        entityCountTaskProgressProcessor.processTaskProgress("IssueType", 1, 0, 0);

        mockTaskProgressSinkControl.verify();
    }

    @Test
    public void testProcessTaskProgress0PercentDone()
    {
        final MockControl mockTaskProgressSinkControl = MockControl.createStrictControl(TaskProgressSink.class);
        final TaskProgressSink mockTaskProgressSink = (TaskProgressSink) mockTaskProgressSinkControl.getMock();
        // 0 percent done in our section - 20% overall.
        mockTaskProgressSink.makeProgress(20, "Step 2 of 3. Testing. Processing IssueType", "Entity 0 of 1000");
        mockTaskProgressSinkControl.replay();

        TaskProgressInterval taskProgressInterval = new TaskProgressInterval(mockTaskProgressSink, 20, 30);

        EntityCountTaskProgressProcessor entityCountTaskProgressProcessor = new EntityCountTaskProgressProcessor(taskProgressInterval, "Step 2 of 3. Testing", 1000, new MockI18nBean());
        entityCountTaskProgressProcessor.processTaskProgress("IssueType", 1, 0, 0);

        mockTaskProgressSinkControl.verify();
    }

    @Test
    public void testProcessTaskProgress20PercentDone()
    {
        final MockControl mockTaskProgressSinkControl = MockControl.createStrictControl(TaskProgressSink.class);
        final TaskProgressSink mockTaskProgressSink = (TaskProgressSink) mockTaskProgressSinkControl.getMock();
        // 20 percent done in our section - 25% overall.
        mockTaskProgressSink.makeProgress(25, "Step 2 of 3. Testing. Processing IssueType", "Entity 250 of 1000");
        mockTaskProgressSinkControl.replay();

        TaskProgressInterval taskProgressInterval = new TaskProgressInterval(mockTaskProgressSink, 20, 40);

        EntityCountTaskProgressProcessor entityCountTaskProgressProcessor = new EntityCountTaskProgressProcessor(taskProgressInterval, "Step 2 of 3. Testing", 1000, new MockI18nBean());
        entityCountTaskProgressProcessor.processTaskProgress("IssueType", 5, 250, 8);

        mockTaskProgressSinkControl.verify();
    }

    @Test
    public void testProcessTaskProgress100PercentDone()
    {
        final MockControl mockTaskProgressSinkControl = MockControl.createStrictControl(TaskProgressSink.class);
        final TaskProgressSink mockTaskProgressSink = (TaskProgressSink) mockTaskProgressSinkControl.getMock();
        // 100 percent done in our section - 30% overall.
        mockTaskProgressSink.makeProgress(30, "Step 2 of 3. Testing. Processing IssueType", "Entity 1000 of 1000");
        mockTaskProgressSinkControl.replay();

        TaskProgressInterval taskProgressInterval = new TaskProgressInterval(mockTaskProgressSink, 20, 30);

        EntityCountTaskProgressProcessor entityCountTaskProgressProcessor = new EntityCountTaskProgressProcessor(taskProgressInterval, "Step 2 of 3. Testing", 1000, new MockI18nBean());
        entityCountTaskProgressProcessor.processTaskProgress("IssueType", 20, 1000, 8);

        mockTaskProgressSinkControl.verify();
    }

}
