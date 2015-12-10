package com.atlassian.jira.issue.fields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.comparator.ProjectNameComparator;
import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since v4.4
 */
public class DefaultProjectFieldLayoutSchemeHelper implements ProjectFieldLayoutSchemeHelper
{

    private final ProjectService projectService;
    private final FieldLayoutManager fieldLayoutManager;
    private final JiraAuthenticationContext authenticationContext;

    public DefaultProjectFieldLayoutSchemeHelper(final ProjectService projectService, final FieldLayoutManager fieldLayoutManager,
            final JiraAuthenticationContext authenticationContext)
    {
        this.projectService = projectService;
        this.fieldLayoutManager = fieldLayoutManager;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public List<Project> getProjectsForScheme(Long schemeId)
    {
        final Set<Project> matchingProjects = Sets.newTreeSet(ProjectNameComparator.COMPARATOR);

        final FieldConfigurationScheme fieldConfigurationScheme = fieldLayoutManager.getFieldConfigurationScheme(schemeId);
        for (final Project project : getProjectsForUser(authenticationContext.getLoggedInUser()))
        {
            final FieldConfigurationScheme projectScheme = fieldLayoutManager.getFieldConfigurationScheme(project);
            if((isSystemDefaultScheme(fieldConfigurationScheme) && isSystemDefaultScheme(projectScheme)) ||
               fieldConfigurationScheme != null && projectScheme != null && fieldConfigurationScheme.getId().equals(projectScheme.getId()))
            {
                matchingProjects.add(project);
            }
        }

        return Lists.newArrayList(matchingProjects);
    }

    @Override
    public Multimap<FieldLayout, Project> getProjectsForFieldLayouts(final Set<FieldLayout> fieldLayouts)
    {
        final Map<FieldLayout, Collection<Project>> backingMap = Maps.newHashMap();
        final Multimap<FieldLayout,Project> fieldLayoutMultimap = Multimaps.newSetMultimap(backingMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return Sets.newTreeSet(ProjectNameComparator.COMPARATOR);
            }
        });

        final List<Project> projectsForUser = getProjectsForUser(authenticationContext.getLoggedInUser());
        for (final Project project : projectsForUser)
        {
            for(final IssueType issueType : project.getIssueTypes())
            {
                final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(project, issueType.getId());
                if(fieldLayouts.contains(fieldLayout))
                {
                    fieldLayoutMultimap.put(fieldLayout, project);
                }
            }

        }
        return fieldLayoutMultimap;
    }

    @Override
    public List<Project> getProjectsForFieldLayout(final FieldLayout fieldLayout)
    {
        final Multimap<FieldLayout, Project> projectsForFieldLayouts =
                getProjectsForFieldLayouts(Collections.singleton(fieldLayout));
        return Lists.newArrayList(projectsForFieldLayouts.get(fieldLayout));
    }

    private boolean isSystemDefaultScheme(final FieldConfigurationScheme fieldConfigurationScheme)
    {
        return (fieldConfigurationScheme == null || fieldConfigurationScheme.getId() == null);
    }

    private List<Project> getProjectsForUser(final User user)
    {
        final ServiceOutcome<List<Project>> allProjectsForAction = projectService.getAllProjectsForAction(user, ProjectAction.EDIT_PROJECT_CONFIG);
        if(allProjectsForAction.isValid())
        {
            return allProjectsForAction.getReturnedValue();
        }
        else
        {
            return Collections.emptyList();
        }
    }

}
