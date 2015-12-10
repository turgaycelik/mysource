package com.atlassian.jira.issue.fields;

import com.atlassian.annotations.PublicApi;

/**
 * Represents the Project System Field.
 *
 * @since v4.3
 */
@PublicApi
public interface ProjectField extends OrderableField, NavigableField, HideableField, RequirableField
{
}
