package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.map.MultiValueMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Quick search handler for components. Note that this handler needs to run after the Project Handler has run.
 *
 * @since v3.13
 */
public class ComponentQuickSearchHandler extends PrefixedSingleWordQuickSearchHandler
{
    private static final String PREFIX = "c:";

    private final ProjectComponentManager projectComponentManager;
    private final ProjectAwareQuickSearchHandler projectAwareQuickSearchHandler;

    public ComponentQuickSearchHandler(final ProjectComponentManager projectComponentManager, final ProjectManager projectManager, final PermissionManager permissionManager, final JiraAuthenticationContext authenticationContext)
    {
        this.projectComponentManager = projectComponentManager;
        projectAwareQuickSearchHandler = new ProjectAwareQuickSearchHandlerImpl(projectManager, permissionManager, authenticationContext);
    }

    protected Map/*<String, String>*/handleWordSuffix(final String wordSuffix, final QuickSearchResult searchResult)
    {
        final List possibleProjects = projectAwareQuickSearchHandler.getProjects(searchResult);

        String projectId = projectAwareQuickSearchHandler.getSingleProjectIdFromSearch(searchResult);
        boolean hasProjectInSearch = projectId != null;
        List<String> projectsWithComponents = Lists.newArrayList();

        final MultiValueMap/*<String, String>*/components = new MultiValueMap();
        for (final Object possibleProject : possibleProjects)
        {
            final GenericValue project = (GenericValue) possibleProject;
            getComponentsByName(project, wordSuffix, components, hasProjectInSearch, projectsWithComponents);
        }

        if (projectsWithComponents.size() == 1)
        {
            String singleProjectWithComponents = projectsWithComponents.get(0);

            projectAwareQuickSearchHandler.addProject(singleProjectWithComponents, searchResult);
        }

        return components;
    }

    protected String getPrefix()
    {
        return PREFIX;
    }

    private void getComponentsByName(final GenericValue project, final String word, final MultiValueMap/*<String, String>*/components,
            boolean hasProjectInSearch, List<String> projectsWithComponents)
    {
        Collection<ProjectComponent> projectComponents = projectComponentManager.findAllForProject(project.getLong("id"));
        Set<String> matchingComponents = getAllNamesMatchingSubstring(projectComponents, word);
        for (String component : matchingComponents)
        {
            if (!components.containsValue("component", component))
            {
                components.put("component", component);
            }
        }

        if (!hasProjectInSearch && !matchingComponents.isEmpty())
        {
            projectsWithComponents.add(project.getString("id"));
        }
    }

    protected Set<String> getAllNamesMatchingSubstring(final Collection/*<ProjectComponent>*/projectComponents, final String name)
    {
        Set<String> matchingComponents = Sets.newLinkedHashSet();

        for (final Object projectComponent1 : projectComponents)
        {
            final ProjectComponent projectComponent = (ProjectComponent) projectComponent1;
            final String componentName = projectComponent.getName();
            if (componentName != null)
            {
                final StringTokenizer st = new StringTokenizer(componentName, " ");
                while (st.hasMoreTokens())
                {
                    final String word = st.nextToken();
                    if (name.equalsIgnoreCase(word))
                    {
                        matchingComponents.add(projectComponent.getName());
                    }
                }
            }
        }

        return matchingComponents;
    }
}
