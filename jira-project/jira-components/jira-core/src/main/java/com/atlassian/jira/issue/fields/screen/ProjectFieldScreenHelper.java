package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

import java.util.List;

/**
 * An internal helper class for Project Configuration.
 *
 * @since v4.4
 */
public interface ProjectFieldScreenHelper
{
    /**
     * Gets the projects using a given {@link FieldScreen}. A project uses a given {@link FieldScreen} if it:
     *
     * <ol>
     *     <li>uses a workflow that has the {@link FieldScreen} as a transition screen, or</li>
     *     <li>uses a field screen scheme thas has a {@link FieldScreen} for one of its issue operations</li>
     * </ol>
     *
     * Only projects for which the requesting user has {@link com.atlassian.jira.bc.project.ProjectAction#EDIT_PROJECT_CONFIG}
     * permissions are returned.
     *
     * @param fieldScreen field screen to find associated projects for.
     * @return list of projects which use the given field screen. Sorted by {@link com.atlassian.jira.issue.comparator.ProjectNameComparator#COMPARATOR}
     */

    List<Project> getProjectsForFieldScreen(final FieldScreen fieldScreen);

    /**
     * Whether the provided user can view the field screen for the project.
     *
     *
     * @param user the user to check to access for. Must not be null.
     * @param fieldScreen the field screen to query view access on. Must not be null.
     * @param project the project the field screen should be on. Must not be null.
     * @return if the provided user can view the field screen for the project
     */
    boolean canUserViewFieldScreenForProject(final ApplicationUser user, final FieldScreen fieldScreen, final Project project);
}
