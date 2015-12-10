package com.atlassian.jira.rest.v2.issue.component;

import com.atlassian.jira.bc.project.component.ProjectComponent;

/**
 * Simple factory used to create component issue counts bean from components and count data.
 *
 * @since v4.4
 */
public interface ComponentIssueCountsBeanFactory
{
    /**
     * Create a ComponentBean given the passed Component.
     *
     * @param component the component to convert.
     * @param issueCount the component to convert.
     * @return the ComponentIssueCountsBean from the passed Component.
     */
    ComponentIssueCountsBean createComponentBean(ProjectComponent component, long issueCount);
}
