package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;

import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Resolves Component objects and ids from their names.
 *
 * @since v4.0
 */
public class ComponentResolver implements NameResolver<ProjectComponent>
{
    final private ProjectComponentManager componentManager;

    public ComponentResolver(final ProjectComponentManager componentManager)
    {
        this.componentManager = componentManager;
    }

    public List<String> getIdsFromName(final String name)
    {
        notNull("name", name);
        Collection<ProjectComponent> components = componentManager.findByComponentNameCaseInSensitive(name);

        Function<ProjectComponent, String> function = new Function<ProjectComponent, String>()
        {
            public String get(final ProjectComponent input)
            {
                return input.getId().toString();
            }
        };

        return CollectionUtil.transform(components, function);
    }

    public boolean nameExists(final String name)
    {
        notNull("name", name);
        Collection<ProjectComponent> components = componentManager.findByComponentNameCaseInSensitive(name);
        return !components.isEmpty();
    }

    public boolean idExists(final Long id)
    {
        notNull("id", id);
        return get(id) != null;
    }

    public ProjectComponent get(final Long id)
    {
        try
        {
            return componentManager.find(id);
        }
        catch (EntityNotFoundException e)
        {
            return null;
        }
    }

    ///CLOVER:OFF
    public Collection<ProjectComponent> getAll()
    {
        return componentManager.findAll();
    }
    ///CLOVER:ON
}