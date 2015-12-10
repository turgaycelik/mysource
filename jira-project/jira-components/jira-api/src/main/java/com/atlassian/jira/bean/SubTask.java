package com.atlassian.jira.bean;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import org.ofbiz.core.entity.GenericValue;

@PublicApi
public interface SubTask
{
    public Long getSequence();

    public Long getDisplaySequence();

    /**
     * Returns the Parent Issue.
     *
     * @return the Parent Issue.
     */
    public Issue getParent();

    /**
     * Returns the SubTask Issue.
     *
     * @return the SubTask Issue.
     */
    public Issue getSubTask();

    /**
     * Returns the SubTask Issue.
     *
     * @return the SubTask Issue.
     *
     * @deprecated Use {@link #getSubTask()} instead. Since v5.0.
     */
    public Issue getSubTaskIssueObject();

    /**
     * Returns the Parent Issue.
     *
     * @return the Parent Issue.
     *
     * @deprecated Use {@link #getParent()} instead. Since v5.0.
     */
    public GenericValue getParentIssue();
}
