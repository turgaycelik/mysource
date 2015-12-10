package com.atlassian.jira.issue.security;

import com.atlassian.jira.issue.Issue;

/**
 * Provides security level related utility methods.
 *
 * @since v3.13
 */
public interface IssueSecurityHelper
{
    /**
     * Checks if we possibly want to change the Security Level of an issue that is being moved.
     * See {@link com.atlassian.jira.issue.fields.SecurityLevelSystemField#needsMove(java.util.Collection, com.atlassian.jira.issue.Issue, com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem)}
     * for the actual logic performing the check.
     *
     * @param sourceIssue The original issue
     * @param targetIssue The new issue we're moving to.  Needs to have the new project set
     * @return true if the security level should be reset on the target issue.
     */
    boolean securityLevelNeedsMove(Issue sourceIssue, Issue targetIssue);
}
