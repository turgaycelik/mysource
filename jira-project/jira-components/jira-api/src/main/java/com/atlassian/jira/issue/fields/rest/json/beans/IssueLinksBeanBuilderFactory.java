package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.issue.Issue;

import javax.annotation.Nonnull;

/**
 * @since v5.0
 */
public interface IssueLinksBeanBuilderFactory
{
    /**
     * Returns a new instance of an IssueLinkBeanBuilder.
     *
     * @param issue the Issue that owns the links
     * @return an IssueLinkBeanBuilder
     */
    @Nonnull
    IssueLinksBeanBuilder newIssueLinksBeanBuilder(final Issue issue);
}
