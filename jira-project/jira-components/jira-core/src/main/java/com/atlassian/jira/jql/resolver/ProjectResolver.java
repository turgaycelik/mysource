package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.InjectableComponent;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Resolves Project objects and ids from their names.
 *
 * @since v4.0
 */
@InjectableComponent
public class ProjectResolver implements NameResolver<Project>
{
    private final ProjectManager projectManager;

    public ProjectResolver(final ProjectManager projectManager)
    {
        this.projectManager = notNull("projectManager", projectManager);
    }

    public List<String> getIdsFromName(final String name)
    {
        notNull("name", name);

        Project project = projectManager.getProjectObjByKeyIgnoreCase(name);
        if (project == null)
        {
            project = projectManager.getProjectObjByName(name);
        }

        if (project != null)
        {
            return Collections.singletonList(project.getId().toString());
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public boolean nameExists(final String name)
    {
        notNull("name", name);

        Project project = projectManager.getProjectObjByKeyIgnoreCase(name);
        if (project == null)
        {
            project = projectManager.getProjectObjByName(name);
        }
        return project != null;
    }

    public boolean idExists(final Long id)
    {
        notNull("id", id);
        final Project project = projectManager.getProjectObj(id);
        return project != null;
    }

    public Project get(final Long id)
    {
        return projectManager.getProjectObj(id);
    }

    ///CLOVER:OFF
    public Collection<Project> getAll()
    {
        return projectManager.getProjectObjects();
    }
    ///CLOVER:ON

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ProjectResolver that = (ProjectResolver) o;

        if (!projectManager.equals(that.projectManager))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return projectManager.hashCode();
    }
}
