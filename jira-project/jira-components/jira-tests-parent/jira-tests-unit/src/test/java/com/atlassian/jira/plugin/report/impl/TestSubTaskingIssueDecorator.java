package com.atlassian.jira.plugin.report.impl;

import java.util.Collection;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.mock.issue.MockIssue;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestSubTaskingIssueDecorator
{
    @Test
    public void testNoSubTasksReturnedByIssueDecorator()
    {
        MockSubTaskedIssue.Factory issueFactory = new MockSubTaskedIssue.Factory();
        MockSubTaskedIssue issue = issueFactory.get();
        Collection subTasks = EasyList.build(issueFactory.get(), issueFactory.get(), issueFactory.get());
        issue.setSubTaskObjects(subTasks);

        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);
        assertEquals(0, issueDecorator.getSubTaskObjects().size());
        assertEquals(issue.getKey(), issueDecorator.getKey());
    }

    @Test
    public void testDecoratorThrowsUnsupportedExForStore()
    {
        MockSubTaskedIssue issue = new MockSubTaskedIssue.Factory().get();
        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);
        try
        {
            issueDecorator.store();
            fail("should throw UnsupportedEx");
        }
        catch (UnsupportedOperationException yay)
        {
            // expected
        }
    }

    @Test
    public void testDecoratorThrowsUnsupportedExForGetSubTaskGVs()
    {
        MockSubTaskedIssue issue = new MockSubTaskedIssue.Factory().get();
        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);
        try
        {
            issueDecorator.getSubTasks();
            fail("should throw UnsupportedEx");
        }
        catch (UnsupportedOperationException yay)
        {
            // expected
        }
    }

    @Test
    public void testDecoratorCorrectlyDelegatesToDecoratedIssueKey()
    {
        MockSubTaskedIssue issue = new MockSubTaskedIssue.Factory().get();
        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);

        assertEquals(issue.getKey(), issueDecorator.getKey());
        issue.setKey("BLAH-1");
        assertEquals(issue.getKey(), issueDecorator.getKey());
        assertEquals("BLAH-1", issueDecorator.getKey());
    }

    @Test
    public void testDecoratorCorrectlyDelegatesToDecoratedIssueId()
    {
        MockSubTaskedIssue issue = new MockSubTaskedIssue.Factory().get();
        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);

        assertEquals(issue.getId(), issueDecorator.getId());
        issue.setId(new Long(657));
        assertEquals(issue.getId(), issueDecorator.getId());
        assertEquals(new Long(657), issueDecorator.getId());
    }

    @Test
    public void testDecoratorCorrectlyDelegatesToDecoratedIssueSummary()
    {
        MockSubTaskedIssue issue = new MockSubTaskedIssue.Factory().get();
        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);

        assertEquals(issue.getSummary(), issueDecorator.getSummary());
        issue.setSummary("this is a summary");
        assertEquals(issue.getSummary(), issueDecorator.getSummary());
        assertEquals("this is a summary", issueDecorator.getSummary());
    }

    @Test
    public void testDecoratorCorrectlyDelegatesToDecoratedIssuePriority()
    {
        MockSubTaskedIssue issue = new MockSubTaskedIssue.Factory().get();
        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);

        assertEquals(issue.getPriorityObject(), issueDecorator.getPriorityObject());

        Priority priority = (Priority) DuckTypeProxy.getProxy(Priority.class, new Object());
        issue.setPriorityObject(priority);
        assertSame(issue.getPriorityObject(), issueDecorator.getPriorityObject());
        assertSame(priority, issueDecorator.getPriorityObject());
    }

    @Test
    public void testDecoratorCorrectlyDelegatesToDecoratedIssueStatus()
    {
        MockSubTaskedIssue issue = new MockSubTaskedIssue.Factory().get();
        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);

        assertEquals(issue.getStatusObject(), issueDecorator.getStatusObject());

        Status status = (Status) DuckTypeProxy.getProxy(Status.class, new Object());
        issue.setStatus(status);
        assertSame(issue.getStatusObject(), issueDecorator.getStatusObject());
        assertSame(status, issueDecorator.getStatusObject());
    }

    @Test
    public void testDecoratorCorrectlyDelegatesToDecoratedIssueType()
    {
        MockSubTaskedIssue issue = new MockSubTaskedIssue.Factory().get();
        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);

        assertEquals(issue.getIssueTypeObject(), issueDecorator.getIssueTypeObject());

        IssueType issueType = (IssueType) DuckTypeProxy.getProxy(IssueType.class, new Object());
        issue.setIssueTypeObject(issueType);
        assertSame(issue.getIssueTypeObject(), issueDecorator.getIssueTypeObject());
        assertSame(issueType, issueDecorator.getIssueTypeObject());
    }

    @Test
    public void testDecoratorCorrectlyDelegatesToDecoratedOriginalEstimate()
    {
        MockSubTaskedIssue issue = new MockSubTaskedIssue.Factory().get();
        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);

        assertEquals(issue.getOriginalEstimate(), issueDecorator.getOriginalEstimate());
        issue.setOriginalEstimate(new Long(652));
        assertEquals(issue.getOriginalEstimate(), issueDecorator.getOriginalEstimate());
        assertEquals(new Long(652), issueDecorator.getOriginalEstimate());
    }

    @Test
    public void testDecoratorCorrectlyDelegatesToDecoratedRemainingEstimate()
    {
        MockSubTaskedIssue issue = new MockSubTaskedIssue.Factory().get();
        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);

        assertEquals(issue.getEstimate(), issueDecorator.getEstimate());
        issue.setEstimate(new Long(653));
        assertEquals(issue.getEstimate(), issueDecorator.getEstimate());
        assertEquals(new Long(653), issueDecorator.getEstimate());
    }

    @Test
    public void testDecoratorCorrectlyDelegatesToDecoratedTimeSpent()
    {
        MockSubTaskedIssue issue = new MockSubTaskedIssue.Factory().get();
        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);

        assertEquals(issue.getTimeSpent(), issueDecorator.getTimeSpent());
        issue.setTimeSpent(new Long(654));
        assertEquals(issue.getTimeSpent(), issueDecorator.getTimeSpent());
        assertEquals(new Long(654), issueDecorator.getTimeSpent());
    }

    @Test
    public void testEquals()
    {
        MockSubTaskedIssue issue = new MockSubTaskedIssue.Factory().get();
        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);

        assertEquals(issue, issueDecorator);
        assertEquals(issueDecorator, issue);
    }

    @Test
    public void testAddSubtask()
    {
        MockSubTaskedIssue issue = new MockSubTaskedIssue.Factory().get();
        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);
        try
        {
            issueDecorator.addSubTask(null);
            fail("shouldn't be able to add nulls");
        }
        catch (IllegalArgumentException yay)
        {
            //expected
        }
        MockIssue subtask = new MockIssue(new Long(123));
        issueDecorator.addSubTask(subtask);
        try
        {
            issueDecorator.addSubTask(subtask);
            fail("Expected IllegalArgumentException because we already added this subtask");
        }
        catch (IllegalArgumentException yay)
        {
            //expected
        }
    }

    @Test
    public void testGetSubtaskObjects()
    {
        MockSubTaskedIssue issue = new MockSubTaskedIssue.Factory().get();

        SubTaskingIssueDecorator issueDecorator = new SubTaskingIssueDecorator(issue);
        MockIssue subtask1 = new MockIssue(new Long(1));
        MockIssue subtask2 = new MockIssue(new Long(2));
        MockIssue subtask3 = new MockIssue(new Long(3));
        MockIssue subtask4 = new MockIssue(new Long(4));
        issue.setSubTaskObjects(EasyList.build(subtask1, subtask2, subtask3, subtask4));

        issueDecorator.addSubTask(subtask3);
        issueDecorator.addSubTask(subtask4);

        // SubTaskingIssueDecorator should only contain explicitly added subtasks
        Collection decoratorsSubtasks = issueDecorator.getSubTaskObjects();
        assertEquals(2, decoratorsSubtasks.size());
        assertTrue(decoratorsSubtasks.containsAll(EasyList.build(subtask3, subtask4)));
    }
}
