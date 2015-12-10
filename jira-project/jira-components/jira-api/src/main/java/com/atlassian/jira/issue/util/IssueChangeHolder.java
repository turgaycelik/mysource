package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;

import java.util.List;

/**
 * This defines a simple object that can contain the changes that have occurred to an issue.
 */
public interface IssueChangeHolder
{
    /**
     * Get all the change items registered with this object.
     * @return a list of ChangeItemBean objects
     */
    public List<ChangeItemBean> getChangeItems();

    /**
     * Set the change items of this object to the provided param.
     * @param changeItems to set on this object
     */
    public void setChangeItems(List<ChangeItemBean> changeItems);

    /**
     * Add the provided list of change items to the current change items.
     * @param changeItems to add to this objects change items
     */
    public void addChangeItems(List<ChangeItemBean> changeItems);

    /**
     * Convenience method for adding a single ChangeItemBean to the list of change items.
     * @param changeItemBean the changeItemBean to add
     */
    public void addChangeItem(ChangeItemBean changeItemBean);

    /**
     * Set the comment associated with this change.
     * @param comment the Comment
     */
    public void setComment(Comment comment);

    /**
     * Get the comment associated with this group of changes.
     * @return the {@link Comment} that represents the comment for this group of changes.
     */
    public Comment getComment();

    /**
     * Returns true if any subtasks of this issue where updated and therefore need to be re-indexed.
     * Currently this is only necessary when changing the issue level security on a parent (as the subtask
     * has to have the same security level as the parent).
     * @return true if there are affected subtasks or false otherwise
     */
    boolean isSubtasksUpdated();

    /**
     * Sets whether or not subtasks have been updated.
     * @param subtasksUpdated true if this issue's subtasks have been updated.
     */
    void setSubtasksUpdated(boolean subtasksUpdated);
}
