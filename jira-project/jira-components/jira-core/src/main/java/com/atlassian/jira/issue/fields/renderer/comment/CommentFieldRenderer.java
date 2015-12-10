package com.atlassian.jira.issue.fields.renderer.comment;

import java.util.Map;

import com.atlassian.jira.plugin.webfragment.model.CommentHelper;

/**
 * Renders comments which are supplied by comment-field-renderers plugged to system.
 */
public interface CommentFieldRenderer
{
    public String getIssuePageEditHtml(Map<String, Object> context, CommentHelper commentHelper);

    public String getIssuePageViewHtml(Map<String, Object> context, CommentHelper commentHelper);

    public String getFieldEditHtml(Map<String, Object> context, CommentHelper commentHelper);

    public String getFieldViewHtml(Map<String, Object> context, CommentHelper commentHelper);

}
