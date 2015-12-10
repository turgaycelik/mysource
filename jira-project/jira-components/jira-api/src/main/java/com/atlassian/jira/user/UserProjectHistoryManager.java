package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.project.Project;
import javax.annotation.Nonnull;

import java.util.List;

/**
 * A wrapper of the {@link UserHistoryManager} that allows you to deal directly with Project objects
 *
 * @since v4.0
 */
public interface UserProjectHistoryManager
{
    /**
     * Add a {@link com.atlassian.jira.project.Project} to the user hsitory list.
     * A null users history should still be stored, even if only for duration of session
     *
     * @param user  The user to add the history item to
     * @param project The project to add to the history list
     */
    void addProjectToHistory(User user, Project project);


    /**
     * Determines whether the user has a current project history.
     * This method also performs permission checks against the project to ensure that user can see atleast 1 project.
     *
     * @param permission the permission to check against
     * @param user The user to check for.
     * @return true if the user has at least 1 project in their project history queue that they can see, false otherwise
     */
    boolean hasProjectHistory(int permission, User user);

    /**
     * Gets the last viewed project that the user visted and still has permission to see.
     * This method also performs permission checks against the project to ensure that user can see it.
     *
     * @param permission the permission the user must have for the project
     * @param user The user to get teh history for.
     * @return the last project the use visited.
     */
    Project getCurrentProject(int permission, User user);

    /**
     * Retreive the user's project history queue.
     * The list is returned ordered by DESC lastViewed date (i.e. newest is first).
     * This method performs no permission checks.  And is extremely fast.
     *
     * @param user The user to get the history project items for.
     * @return a list of history project items sort by desc lastViewed date.
     */
    @Nonnull
    List<UserHistoryItem> getProjectHistoryWithoutPermissionChecks(User user);

    /**
     * Retreive the user's project history queue.
     * The list is returned ordered by DESC lastViewed date (i.e. newest is first).
     * This method performs permission checks.
     *
     * @param permission The permission the user must have for the project
     * @param user The user to get the history project items for.
     * @return a list of projects sort by desc lastViewed date.
     *
     * @deprecated since 4.4, use {#getProjectHistoryWithPermissionChecks(ProjectAction, User)}
     */
    @Nonnull
    List<Project> getProjectHistoryWithPermissionChecks(int permission, User user);

    /**
     * Retreive the user's project history queue.
     * The list is returned ordered by DESC lastViewed date (i.e. newest is first).
     * This method performs permission checks.
     *
     * @param projectAction The projectAction the user must have for the project
     * @param user The user to get the history project items for.
     * @return a list of projects sort by desc lastViewed date.
     */
    @Nonnull
    List<Project> getProjectHistoryWithPermissionChecks(ProjectAction projectAction, User user);
}