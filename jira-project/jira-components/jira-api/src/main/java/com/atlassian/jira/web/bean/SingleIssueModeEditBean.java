package com.atlassian.jira.web.bean;

/**
 * This is used in order to be able to move an individual issue that has sub-tasks
 * via the bulk move process.
 *
 * @see JRA-17312
 */
public interface SingleIssueModeEditBean
{

    /**
     * Sets this bean to "single issue mode".
     *
     * Set single issue key to be moved via bulk edit workflow.
     * @param issueKey key for single issue to be moved
     */
    public void setSingleIssueKey(String issueKey);

    /**
     * Gets the issue key for this bean as previously set
     * by {@link #setSingleIssueKey(String)}.
     *
     * @return the issue key or <code>null</code> if there is none.
     */
    public String getSingleIssueKey();

    /**
     * Indicates where this bean is being used to move a single issue with subtasks.
     *
     * @return <code>true</code> if this bulk edit bean is used for single issue move operation
     */
    public boolean isSingleMode();
}
