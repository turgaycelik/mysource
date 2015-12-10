package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Index resolver that can find the index values for components.
 *
 * @since v4.0
 */
public class ComponentIndexInfoResolver implements IndexInfoResolver<ProjectComponent>
{
    private final NameResolver<ProjectComponent> componentResolver;

    public ComponentIndexInfoResolver(NameResolver<ProjectComponent> componentResolver)
    {
        this.componentResolver = componentResolver;
    }

    public List<String> getIndexedValues(final String rawValue)
    {
        notNull("rawValue", rawValue);
        List<String> components = componentResolver.getIdsFromName(rawValue);
        if (components.isEmpty())
        {
            final Long componentId = getValueAsLong(rawValue);
            if (componentId != null && componentResolver.idExists(componentId))
            {
                components = Collections.singletonList(rawValue);
            }
        }
        return components;
    }

    public List<String> getIndexedValues(final Long rawValue)
    {
        notNull("rawValue", rawValue);
        if (componentResolver.idExists(rawValue))
        {
            return CollectionBuilder.newBuilder(rawValue.toString()).asList();
        }
        else
        {
            return componentResolver.getIdsFromName(rawValue.toString());
        }
    }

    public String getIndexedValue(final ProjectComponent component)
    {
        notNull("component", component);
        return getIdAsString(component);
    }

    private String getIdAsString(final ProjectComponent component)
    {
        return component.getId().toString();
    }

    private Long getValueAsLong(final String value)
    {
        try
        {
            return new Long(value);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}