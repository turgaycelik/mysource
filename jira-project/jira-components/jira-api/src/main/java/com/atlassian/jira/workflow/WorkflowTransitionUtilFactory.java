package com.atlassian.jira.workflow;

import com.atlassian.annotations.PublicApi;

/**
 * A Factory class to create WorkflowTransitionUtil objects.
 *
 * @since v4.4
 */
@PublicApi
public interface WorkflowTransitionUtilFactory
{
    /**
     * Creates a new instance of WorkflowTransitionUtil.
     *
     * @return a new instance of WorkflowTransitionUtil.
     */
    WorkflowTransitionUtil create();
}
