package com.atlassian.jira.bc.project.projectoperation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.projectoperation.PluggableProjectOperation;
import com.atlassian.jira.project.Project;

import java.util.List;

/**
 * Provides some logic to retrieve all project operations for a particular project.
 *
 * @since v3.12
 */
public interface ProjectOperationManager
{

    /**
     * Returns a list of {@link com.atlassian.jira.plugin.projectoperation.PluggableProjectOperation}s
     * @param project The project for which to retrieve the operations
     * @param user The logged in user
     * @return A sorted list of PluggableProjectOperations.  Empty list if none exist.
     */
    List<PluggableProjectOperation> getVisibleProjectOperations(Project project, User user);
}
