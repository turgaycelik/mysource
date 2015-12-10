package com.atlassian.jira.issue.fields.screen.issuetype;

import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.project.Project;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Set;

/**
 * A number of useful issue type screen scheme scheme methods, particularly to do with "active" field screen schemes.
 *
 * A field screen scheme is only considered "active" if it is currently actually being used by a project.
 * This means it is active if:
 *
 * <ol>
 *     <li>The field screen scheme is mapped to an issue type in the project's scheme and the project has that issue type.
 *     <li>The field screen scheme is a default for the project's scheme and there are some unmapped issue types.
 * </ol>
 *
 * Internal helper class for Project Configuration.
 *
 * @since v4.4
 */
public interface ProjectIssueTypeScreenSchemeHelper
{
    /**
     * Return a list of projects that use the issue type screen scheme with the passed query. Only projects that the
     * passed user can change the configuration for will be returned.
     *
     * @param issueTypeScreenScheme the issue type screen scheme. There is no concept of a magical null-id issue type screen scheme, all
     * of them, even the default, have IDs.
     *
     * @return a list of projects that use the issue type screen scheme. The list is sorted by project name. The list is mutable and
     * can be changed by the caller safely.
     */
    List<Project> getProjectsForScheme(IssueTypeScreenScheme issueTypeScreenScheme);

    /**
     * Like {@link #getProjectsForFieldScreenScheme(com.atlassian.jira.issue.fields.screen.FieldScreenScheme)}, but
     * takes a set of field screen schemes and returns a multimap of Field Screen Scheme -> Active projects
     *
     * @param fieldScreenSchemes field screen schemes to get projects for
     * @return multimap of Field Screen Scheme -> Active projects, sorted on {@link com.atlassian.jira.issue.comparator.ProjectNameComparator}
     */
    Multimap<FieldScreenScheme, Project> getProjectsForFieldScreenSchemes(final Set<FieldScreenScheme> fieldScreenSchemes);

    /**
     * Returns a list of the projects are currently using the passed screen scheme, and the screen scheme is active.
     *
     * Only projects that the current user can change the configuration for will be returned.
     *
     * @param fieldScreenScheme  the name of the field screen scheme to check.
     * @return a list of active projects. The list is sorted by project name. The list is mutable and
     * can be changed by the caller safely.
     */
    List<Project> getProjectsForFieldScreenScheme(final FieldScreenScheme fieldScreenScheme);
}
