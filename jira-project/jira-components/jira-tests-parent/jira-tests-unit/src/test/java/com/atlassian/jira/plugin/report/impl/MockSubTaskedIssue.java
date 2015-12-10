package com.atlassian.jira.plugin.report.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.mock.issue.MockIssue;

class MockSubTaskedIssue extends MockIssue
{
    private Issue parent;
    private Priority priority;
    private Status status;

    MockSubTaskedIssue(final Long id)
    {
        super(id);
    }

    void setParent(final Issue issue)
    {
        parent = issue;
    }

    @Override
    public Issue getParentObject()
    {
        return parent;
    }

    @Override
    public Long getParentId()
    {
        return parent.getId();
    }

    @Override
    public Collection getSubTaskObjects()
    {
        final Collection subTaskObjects = super.getSubTaskObjects();
        return (subTaskObjects == null) ? Collections.EMPTY_LIST : subTaskObjects;
    }

    @Override
    public boolean isSubTask()
    {
        return parent != null;
    }

    @Override
    public Priority getPriorityObject()
    {
        return priority;
    }

    public void setPriorityObject(final Priority priority)
    {
        this.priority = priority;
    }

    @Override
    public Status getStatusObject()
    {
        return status;
    }

    void setStatus(final Status status)
    {
        this.status = status;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }

        final Issue issue = (Issue) o;

        return !(getKey() != null ? !getKey().equals(issue.getKey()) : issue.getKey() != null);
    }

    @Override
    public int hashCode()
    {
        return getKey().hashCode();
    }

    @Override
    public String toString()
    {
        return getKey() + (parent == null ? "" : (" -> " + parent));
    }

    private static class IDGen
    {
        AtomicLong idGen = new AtomicLong();

        Long get()
        {
            return new Long(idGen.incrementAndGet());
        }
    }

    /**
     * handy factory for these guys that automatically generates the id and key for us.
     */
    static class Factory
    {
        final IDGen gen = new IDGen();
        final String prefix = "HSP-";

        MockSubTaskedIssue get()
        {
            final Long id = gen.get();
            final MockSubTaskedIssue result = new MockSubTaskedIssue(id);
            result.setKey(prefix + id);
            return result;
        }
    }
}