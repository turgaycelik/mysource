package com.atlassian.jira.jql.values;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.LocaleSensitiveProjectComponentNameComparator;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Gets all component values
 *
 * @since v4.0
 */
public class ComponentClauseValuesGenerator implements ClauseValuesGenerator
{
    private final ProjectComponentManager projectComponentManager;
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;

    public ComponentClauseValuesGenerator(ProjectComponentManager projectComponentManager, final ProjectManager projectManager, final PermissionManager permissionManager)
    {
        this.projectComponentManager = projectComponentManager;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
    }

    public Results getPossibleValues(final User searcher, final String jqlClauseName, final String valuePrefix, final int maxNumResults)
    {
        final List<ProjectComponent> projectComponents = new ArrayList<ProjectComponent>(projectComponentManager.findAll());

        // Lets sort the whole list of components by name
        Collections.sort(projectComponents, new LocaleSensitiveProjectComponentNameComparator(getLocale(searcher)));
        final ApplicationUser sercherApplicationUser = ApplicationUsers.from(searcher);
        final Set<Result> componentValues = new LinkedHashSet<Result>();
        for (ProjectComponent component : projectComponents)
        {
            if (componentValues.size() == maxNumResults)
            {
                break;
            }
            // Lets do the cheapest check first
            final String lowerCaseCompName = component.getName().toLowerCase();
            if (StringUtils.isBlank(valuePrefix) || lowerCaseCompName.startsWith(valuePrefix.toLowerCase()))
            {
                final Project project = projectManager.getProjectObj(component.getProjectId());
                if (project != null && permissionManager.hasPermission(Permissions.BROWSE, project, sercherApplicationUser))
                {
                    componentValues.add(new Result(component.getName()));
                }
            }
        }

        return new Results(new ArrayList<Result>(componentValues));
    }

    ///CLOVER:OFF
    Locale getLocale(final User searcher)
    {
        return new I18nBean(searcher).getLocale();
    }
    ///CLOVER:ON
}
