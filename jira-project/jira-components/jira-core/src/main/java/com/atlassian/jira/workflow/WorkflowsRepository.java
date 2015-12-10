package com.atlassian.jira.workflow;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Provides a collection like interface over the underlying {@link JiraWorkflow} persistance implementation.
 *
 * All the methods on this abstraction take in or retrieve JiraWorflow objects. The repository is responsible to fetch
 * these objects from the underlying persistance implementation based on a given criteria, objects should be able to be
 * added and removed from the repository as well.
 *
 * See <a href="http://martinfowler.com/eaaCatalog/repository.html">Repository Pattern Definition</a> and
 * the &quot;Domain Driven Design&quot; book for more information.
 *
 * @since v5.1
 */
@Internal
@InjectableComponent
public interface WorkflowsRepository
{
    /**
     * Returns <tt>true</tt> if this repository contains a workflow with the specified name.
     * @param workflowName The name of the workflow whose presence in this repository is to be tested.
     *
     * @return <tt>true</tt> if this repository contains a workflow with the specified name.
     */
    public boolean contains(final String workflowName);
}
