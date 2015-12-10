package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of a change holder.
 */
public class DefaultIssueChangeHolder implements IssueChangeHolder
{
    private Comment comment;
    private List<ChangeItemBean> changeItems = new ArrayList<ChangeItemBean>();
    private boolean subtasksUpdated = false;

    public List<ChangeItemBean> getChangeItems()
    {
        return changeItems;
    }

    public void setChangeItems(List<ChangeItemBean> changeItems)
    {
        if (changeItems != null)
        {
            this.changeItems = changeItems;
        }
    }

    public void addChangeItems(List<ChangeItemBean> changeItems)
    {
        if (changeItems != null)
        {
            this.changeItems.addAll(changeItems);
        }
    }

    public void addChangeItem(ChangeItemBean changeItemBean)
    {
        if (changeItemBean != null)
        {
            this.changeItems.add(changeItemBean);
        }
    }

    public void setComment(Comment comment)
    {
        this.comment = comment;
    }

    public Comment getComment()
    {
        return comment;
    }

    public boolean isSubtasksUpdated()
    {
        return subtasksUpdated;
    }

    public void setSubtasksUpdated(boolean subtasksUpdated)
    {
        this.subtasksUpdated = subtasksUpdated;
    }

    @SuppressWarnings ("RedundantIfStatement")
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final DefaultIssueChangeHolder that = (DefaultIssueChangeHolder) o;

        if (changeItems != null ? !changeItems.equals(that.changeItems) : that.changeItems != null)
        {
            return false;
        }
        if (comment != null ? !comment.equals(that.comment) : that.comment != null)
        {
            return false;
        }
        if (subtasksUpdated != that.subtasksUpdated)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (changeItems != null ? changeItems.hashCode() : 0);
        result = 31 * result + (subtasksUpdated ? 1 : 0);
        return result;
    }
}