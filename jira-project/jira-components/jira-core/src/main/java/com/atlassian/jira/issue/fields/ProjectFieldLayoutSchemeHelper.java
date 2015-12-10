package com.atlassian.jira.issue.fields;

import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.project.Project;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Set;

/**
 * A number of useful field layout scheme methods, particularly to do with "active" field layouts.
 *
 * A field layout is only considered "active" if it is currently actually being used by a project. This means it is active if:
 *
 * <ol>
 *     <li>The field layout is mapped to an issue type in the project's scheme and the project has that issue type.
 *     <li>The field layout is a default for the project's scheme and there are some unmapped issue types.
 * </ol>
 *
 * Internal helper class for Project Configuration.
 *
 * @since v4.4
 */
public interface ProjectFieldLayoutSchemeHelper
{
    /**
     * Return a list of projects that use the field layout scheme with the passed query. Only projects that the passed user
     * can change the configuration for will be returned.
     *
     * @param schemeId the scheme id query. Null can be passed to search for the magical system default field layout scheme.
     *
     * @return a list of projects that use the scheme. The list is sorted by project name. The list is mutable and
     * can be changed by the caller safely.
     */
    List<Project> getProjectsForScheme(Long schemeId);

    /**
     * Like {@link #getProjectsForFieldLayout(com.atlassian.jira.issue.fields.layout.field.FieldLayout)}, but takes a set of fieldLayouts
     * and returns a multimap of FieldLayouts -> Active projects
     *
     * @param fieldLayouts Field layouts to get projects for
     * @return multimap of FieldLayouts -> Active projects, sorted on {@link com.atlassian.jira.issue.comparator.ProjectNameComparator}
     */
    Multimap<FieldLayout, Project> getProjectsForFieldLayouts(Set<FieldLayout> fieldLayouts);

    /**
     * Returns a list of the projects are currently using the passed field layout, and the field layout is active.
     *
     * Only projects that the current user can change the configuration for will be returned.
     *
     * @param fieldLayout the name of the field layout the check.
     * @return a list of active projects. The list is sorted by project name. The list is mutable and
     * can be changed by the caller safely.
     */
    List<Project> getProjectsForFieldLayout(FieldLayout fieldLayout);



}
