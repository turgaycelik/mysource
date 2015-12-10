package com.atlassian.jira.cluster;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.user.ApplicationUser;

/**
 * A service relating to the state of nodes within a JIRA cluster.
 *
 * @since 6.1
 */
@ExperimentalApi
public interface NodeStateService
{
    /**
     * Activate the current node, provided the user has the necessary permissions.
     *
     * This method will block until the node becomes active.
     *
     * @param user the user performing the activation (required)
     * @return a non-null result
     */
    ServiceResult activate(ApplicationUser user);

    /**
     * Deactivate the node.
     *
     * This method will block until the node becomes inactive (passive).
     *
     * @param user the user performing the deactivation (required)
     * @return a non-null result
     */
    ServiceResult deactivate(ApplicationUser user);

    /**
     * Indicates whether the current node is active within the cluster.
     *
     * @return true if not clustered
     */
    boolean isActive();
}
