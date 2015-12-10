package com.atlassian.jira.issue.renderers;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.comments.Comment;

/**
 *
 */
public class CommentFieldRenderContext implements FieldRenderContext
{
    private final Comment comment;

    public CommentFieldRenderContext(Comment comment)
    {
        this.comment = comment;
    }

    public String getFieldId()
    {
        return IssueFieldConstants.COMMENT;
    }

    public Issue getIssue()
    {
        return comment.getIssue();
    }

    public String getBody()
    {
        return comment.getBody();
    }
}
