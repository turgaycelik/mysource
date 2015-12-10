package com.atlassian.jira.issue.fields.screen.issuetype;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.comparator.ProjectNameComparator;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since v4.4
 */
public class DefaultProjectIssueTypeScreenSchemeHelper implements ProjectIssueTypeScreenSchemeHelper
{
    private static final Logger log = Logger.getLogger(DefaultProjectIssueTypeScreenSchemeHelper.class);

    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final ProjectService projectService;
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;
    private final ProjectFactory projectFactory;

    public DefaultProjectIssueTypeScreenSchemeHelper(final ProjectService projectService,
            final JiraAuthenticationContext authenticationContext,
            final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager,
            final PermissionManager permissionManager,
            final ProjectFactory projectFactory)
    {
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.projectService = projectService;
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.projectFactory = projectFactory;
    }

    @Override
    public Multimap<FieldScreenScheme, Project> getProjectsForFieldScreenSchemes(final Set<FieldScreenScheme> fieldScreenSchemes)
    {
        // Find all the screen schemes, so we can pop them up in the "other projects using this scheme" hover
        final Map<FieldScreenScheme, Collection<Project>> backingMap = Maps.newHashMap();

        final Multimap<FieldScreenScheme, Project> fieldScreenSchemeToProjectMapping = Multimaps.newSetMultimap(backingMap, new Supplier<Set<Project>>()
        {
            @Override
            public Set<Project> get()
            {
                return Sets.newTreeSet(ProjectNameComparator.COMPARATOR);
            }
        });

        final List<Project> projects = getAllProjects();
        for (Project project : projects)
        {
            final IssueTypeScreenScheme issueTypeScreenScheme =
                    issueTypeScreenSchemeManager.getIssueTypeScreenScheme(project);
            if (issueTypeScreenScheme == null)
            {
                // JRADEV-8833: Harden the code against data corruption from other projects
                log.warn("Unable to find IssueTypeScreenScheme for project " + project.getKey());
                continue;
            }
            final IssueTypeScreenSchemeEntity defaultEntity = issueTypeScreenScheme.getEntity(null);
            if (defaultEntity == null)
            {
                // JRADEV-8833: Harden the code against data corruption from other projects
                log.warn("Unable to find default IssueTypeScreenSchemeEntity for project " + project.getKey());
                continue;
            }
            final FieldScreenScheme defaultFieldScreenScheme = defaultEntity.getFieldScreenScheme();

            for (final IssueType issueType : project.getIssueTypes())
            {
                final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(issueType.getId());
                FieldScreenScheme fieldScreenScheme;
                if(issueTypeScreenSchemeEntity == null)
                {
                    fieldScreenScheme = defaultFieldScreenScheme;
                }
                else
                {
                    fieldScreenScheme = issueTypeScreenSchemeEntity.getFieldScreenScheme();
                }
                if(fieldScreenSchemes.contains(fieldScreenScheme))
                {
                    fieldScreenSchemeToProjectMapping.put(fieldScreenScheme, project);
                }
            }
        }

        return fieldScreenSchemeToProjectMapping;

    }

    @Override
    public List<Project> getProjectsForFieldScreenScheme(FieldScreenScheme fieldScreenScheme)
    {
        final Multimap<FieldScreenScheme, Project> projectsForFieldScreenSchemes = getProjectsForFieldScreenSchemes(Collections.singleton(fieldScreenScheme));
        return Lists.newArrayList(projectsForFieldScreenSchemes.get(fieldScreenScheme));
    }

    @Override
    public List<Project> getProjectsForScheme(IssueTypeScreenScheme issueTypeScreenScheme)
    {
        final Set<Project> sharedProjects = Sets.newTreeSet(ProjectNameComparator.COMPARATOR);
        final List<Project> projects = projectFactory.getProjects(issueTypeScreenScheme.getProjects());
        for (final Project project : projects)
        {
            if(hasEditPermission(authenticationContext.getLoggedInUser(), project))
            {
                sharedProjects.add(project);
            }
        }
        return Lists.newArrayList(sharedProjects);
    }

    boolean hasEditPermission(final User user, final Project project)
    {
        return ProjectAction.EDIT_PROJECT_CONFIG.hasPermission(permissionManager, user, project);
    }

    private List<Project> getAllProjects()
    {
        ServiceOutcome<List<Project>> projectsForAction = projectService
                .getAllProjectsForAction(authenticationContext.getLoggedInUser(), ProjectAction.EDIT_PROJECT_CONFIG);

        if (projectsForAction.isValid())
        {
            return projectsForAction.getReturnedValue();
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
