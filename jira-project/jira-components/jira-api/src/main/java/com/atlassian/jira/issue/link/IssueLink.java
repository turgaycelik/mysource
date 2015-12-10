package com.atlassian.jira.issue.link;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.ofbiz.OfBizValueWrapper;

@PublicApi
public interface IssueLink extends OfBizValueWrapper
{
    public Long getId();

    public Long getLinkTypeId();

    /**
     * Get the Issue ID (ie. {@link com.atlassian.jira.issue.Issue#getId();}) of the linked-from issue.
     */
    public Long getSourceId();

    /**
     * Get the issue ID (ie. {@link com.atlassian.jira.issue.Issue#getId();}) of the linked-to issue.
     */
    public Long getDestinationId();

    public Long getSequence();

    public IssueLinkType getIssueLinkType();

    /**
     * Get the source Issue of the link.
     */
    public Issue getSourceObject();

    /**
     * Get the destination Issue of the link.
     */
    public Issue getDestinationObject();

    /**
     * Checks if this link's type is a System Link type. A System Link Type is a link type
     * that is used by JIRA to denote special relationships. For example, a sub-task is linked to its
     * parent issue using a link type that is a System Link Type.
     */
    public boolean isSystemLink();
}
