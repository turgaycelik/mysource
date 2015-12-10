package com.atlassian.jira.issue.security;

import com.atlassian.annotations.PublicApi;

/**
 * Defines an issue security level in JIRA.
 *
 * @since v4.0
 */
@PublicApi
public interface IssueSecurityLevel
{
    /**
     * @return the unique identifier for this issue security level
     */
    Long getId();

    /**
     * @return the user provided name for this issue security level
     */
    String getName();

    /**
     * @return the user provided description for this issue security level
     */
    String getDescription();

    /**
     * @return the unique identifier of the IssueSecurityScheme that this issue security level is associated with
     */
    Long getSchemeId();
}
