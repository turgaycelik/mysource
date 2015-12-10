package com.atlassian.jira.issue.status;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.status.category.StatusCategory;

import java.io.Serializable;

/**
 * Simplified, immutable version of {@link Status} . Contains all data necessary to display status of an issue.
 */
@PublicApi
public interface SimpleStatus extends Serializable
{

    String getId();

    String getName();

    String getDescription();

    StatusCategory getStatusCategory();

    /**
     * Temporal addition of iconUrl in order to achieve fluent transition from icons to lozenges.
     */
    String getIconUrl();

}
