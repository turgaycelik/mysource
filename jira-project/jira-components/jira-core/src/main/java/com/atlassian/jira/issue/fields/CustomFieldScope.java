package com.atlassian.jira.issue.fields;

import com.atlassian.jira.issue.context.IssueContext;

/**
 * Represents the scope of a given custom field. The scope is defined as the projects/issue types for which a custom
 * field is visible.
 */
public interface CustomFieldScope
{
    /**
     * Checks whether the custom field corresponding to this scope is in the scope of the given {@link IssueContext}.
     * <p/>
     * If the project on the IssueContext is null, then it is treated as a wildcard. If the issueTypeId on the
     * IssueContext is null or an empty list, then it is treated as a wildcard.
     *
     * @param issueContext The issue context.
     * @return Whether the custom field is in the scope of the given {@link IssueContext}.
     */
    boolean isIncludedIn(final IssueContext issueContext);
}
