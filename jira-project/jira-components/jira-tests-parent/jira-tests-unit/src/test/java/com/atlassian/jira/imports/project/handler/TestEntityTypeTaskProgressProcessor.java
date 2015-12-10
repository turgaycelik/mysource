package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.imports.project.taskprogress.EntityTypeTaskProgressProcessor;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.MockControl;
import org.junit.Test;

/**
 * @since v3.13
 */
public class TestEntityTypeTaskProgressProcessor
{
    @Test
    public void testNullTaskProgressSink()
    {
        EntityTypeTaskProgressProcessor entityTypeTaskProgressProcessor = new EntityTypeTaskProgressProcessor(20, null, new MockI18nBean());
        entityTypeTaskProgressProcessor.processTaskProgress("IssueTypes", 5, 40, 8);
        // That's all we do - just want to prove it doesn't throw NPE
    }

    @Test
    public void testProcessTaskProgress0PercentDone()
    {
        final MockControl mockTaskProgressSinkControl = MockControl.createStrictControl(TaskProgressSink.class);
        final TaskProgressSink mockTaskProgressSink = (TaskProgressSink) mockTaskProgressSinkControl.getMock();
        // We are up to entity Type 1, which means we have completed 0/20 = 0%
        mockTaskProgressSink.makeProgress(0, "Processing IssueType", "8");
        mockTaskProgressSinkControl.replay();

        EntityTypeTaskProgressProcessor entityTypeTaskProgressProcessor = new EntityTypeTaskProgressProcessor(20, mockTaskProgressSink, new MockI18nBean());
        entityTypeTaskProgressProcessor.processTaskProgress("IssueType", 1, 8, 8);

        mockTaskProgressSinkControl.verify();
    }

    @Test
    public void testProcessTaskProgress20PercentDone()
    {
        final MockControl mockTaskProgressSinkControl = MockControl.createStrictControl(TaskProgressSink.class);
        final TaskProgressSink mockTaskProgressSink = (TaskProgressSink) mockTaskProgressSinkControl.getMock();
        // We are up to entity Type 5, which means we have completed 4/20 = 20%
        mockTaskProgressSink.makeProgress(20, "Processing IssueType", "8");
        mockTaskProgressSinkControl.replay();

        EntityTypeTaskProgressProcessor entityTypeTaskProgressProcessor = new EntityTypeTaskProgressProcessor(20, mockTaskProgressSink, new MockI18nBean());
        entityTypeTaskProgressProcessor.processTaskProgress("IssueType", 5, 40, 8);

        mockTaskProgressSinkControl.verify();
    }

    @Test
    public void testProcessTaskProgress95PercentDone()
    {
        final MockControl mockTaskProgressSinkControl = MockControl.createStrictControl(TaskProgressSink.class);
        final TaskProgressSink mockTaskProgressSink = (TaskProgressSink) mockTaskProgressSinkControl.getMock();
        // We are up to entity Type 20, which means we have completed 19/20 = 95%
        mockTaskProgressSink.makeProgress(95, "Processing IssueType", "8");
        mockTaskProgressSinkControl.replay();

        EntityTypeTaskProgressProcessor entityTypeTaskProgressProcessor = new EntityTypeTaskProgressProcessor(20, mockTaskProgressSink, new MockI18nBean());
        entityTypeTaskProgressProcessor.processTaskProgress("IssueType", 20, 40, 8);

        mockTaskProgressSinkControl.verify();
    }
}
