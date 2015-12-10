package com.atlassian.jira.bean;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import org.ofbiz.core.entity.GenericValue;

public class SubTaskImpl implements SubTask
{
    private Long sequence;
    private Issue subTaskIssue;
    private Issue parentIssue;

    public SubTaskImpl(Long sequence, Issue subTaskIssue, Issue parentIssue)
    {
        this.sequence = sequence;
        this.subTaskIssue = subTaskIssue;
        this.parentIssue = parentIssue;
    }

    /**
     * Deprecated Constructor
     * @param sequence
     * @param subTaskIssue
     * @param parentIssue
     *
     * @deprecated Use {@link #SubTaskImpl(Long, com.atlassian.jira.issue.Issue, com.atlassian.jira.issue.Issue)} instead. Since v5.0.
     */
    public SubTaskImpl(Long sequence, GenericValue subTaskIssue, GenericValue parentIssue)
    {
        this.sequence = sequence;
        this.subTaskIssue = getIssueObjectFor(subTaskIssue);
        this.parentIssue = getIssueObjectFor(parentIssue);
    }

    public Long getSequence()
    {
        return sequence;
    }

    public Long getDisplaySequence()
    {
        // Add one as in display the sequences should start with 1 and not 0
        return getSequence() + 1;
    }

    /**
     * Returns the Parent Issue.
     *
     * @return the Parent Issue.
     */
    public Issue getParent()
    {
        return parentIssue;
    }

    /**
     * Returns the SubTask Issue.
     *
     * @return the SubTask Issue.
     */
    public Issue getSubTask()
    {
        return subTaskIssue;
    }

    /**
     * Returns the SubTask Issue.
     *
     * @return the SubTask Issue.
     *
     * @deprecated Use {@link #getSubTask()} instead. Since v5.0.
     */
    public Issue getSubTaskIssueObject()
    {
        return subTaskIssue;
    }

    /**
     * Returns the Parent Issue.
     *
     * @return the Parent Issue.
     *
     * @deprecated Use {@link #getParent()} instead.
     */
    public GenericValue getParentIssue()
    {
        return parentIssue.getGenericValue();
    }

    private Issue getIssueObjectFor(GenericValue issueGV)
    {
        return ComponentAccessor.getIssueFactory().getIssue(issueGV);
    }

    @SuppressWarnings ( { "RedundantIfStatement" })
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SubTaskImpl)) return false;

        final SubTaskImpl subTask = (SubTaskImpl) o;

        if (parentIssue != null ? !parentIssue.equals(subTask.parentIssue) : subTask.parentIssue != null) return false;
        if (sequence != null ? !sequence.equals(subTask.sequence) : subTask.sequence != null) return false;
        if (subTaskIssue != null ? !subTaskIssue.equals(subTask.subTaskIssue) : subTask.subTaskIssue != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (sequence != null ? sequence.hashCode() : 0);
        result = 29 * result + (subTaskIssue != null ? subTaskIssue.hashCode() : 0);
        result = 29 * result + (parentIssue != null ? parentIssue.hashCode() : 0);
        return result;
    }
}
