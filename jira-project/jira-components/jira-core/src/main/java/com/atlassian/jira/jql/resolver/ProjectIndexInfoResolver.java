package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Index resolver that can find the index values for projects.
 *
 * @since v4.0
 */
public class ProjectIndexInfoResolver implements IndexInfoResolver<Project>
{
    private final NameResolver<Project> nameResolver;

    public ProjectIndexInfoResolver(final NameResolver<Project> nameResolver)
    {
        this.nameResolver = notNull("nameResolver", nameResolver);
    }

    public List<String> getIndexedValues(final String rawValue)
    {
        notNull("rawValue", rawValue);

        List<String> projects = nameResolver.getIdsFromName(rawValue);
        if (projects.isEmpty())
        {
            final Long projId = getValueAsLong(rawValue);
            if (projId != null)
            {
                Project project = nameResolver.get(projId);
                if (project != null)
                {
                    return Collections.singletonList(project.getId().toString());
                }
            }

        }
        return projects;
    }

    public List<String> getIndexedValues(final Long rawValue)
    {
        notNull("rawValue", rawValue);

        final Project project = nameResolver.get(rawValue);
        if (project == null)
        {
            return nameResolver.getIdsFromName(rawValue.toString());
        }
        return CollectionBuilder.newBuilder(project.getId().toString()).asList();
    }

    public String getIndexedValue(final Project project)
    {
        notNull("project", project);
        return project.getId().toString();
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
