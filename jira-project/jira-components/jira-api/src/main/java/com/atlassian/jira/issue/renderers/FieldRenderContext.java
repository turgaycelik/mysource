package com.atlassian.jira.issue.renderers;

import com.atlassian.jira.issue.Issue;

/**
 *
 */
public interface FieldRenderContext
{
    /**
     * @return  A field constant, as defined in {@link com.atlassian.jira.issue.IssueFieldConstants}
     */
    String getFieldId();

    Issue getIssue();

    /**
     * @return  The text to be rendered
     */
    String getBody();
}
