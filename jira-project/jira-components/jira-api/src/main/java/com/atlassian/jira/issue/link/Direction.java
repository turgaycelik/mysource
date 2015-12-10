package com.atlassian.jira.issue.link;

import com.atlassian.annotations.PublicApi;

/**
 * Represents the direction of a link. Links may have a different name and description for each direction of the link.
 */
@PublicApi
public enum Direction
{
    /**
     * Going from this issue to the other issue.
     */
    OUT,

    /**
     * Going from the other issue to this issue.
     */
    IN
}
