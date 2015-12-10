package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.InjectableComponent;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A class for resolving {@link com.atlassian.jira.jql.operand.QueryLiteral}s into project categories and projects.
 *
 * @since v4.0
 */
@InjectableComponent
public class ProjectCategoryResolver
{
    private final ProjectManager projectManager;

    public ProjectCategoryResolver(final ProjectManager projectManager)
    {
        this.projectManager = notNull("projectManager", projectManager);
    }

    /**
     * @param literal the query literal to resolve; must not be null.
     * @return the GenericValue representing the project category; null if the literal was empty or if the literal
     * did not resolve successfully.
     */
    public GenericValue getProjectCategory(QueryLiteral literal)
    {
        notNull("literal", literal);
        if (literal.getStringValue() != null)
        {
            return getProjectCategoryForString(literal.getStringValue());
        }
        else if (literal.getLongValue() != null)
        {
            return getProjectCategoryForLong(literal.getLongValue());
        }
        else
        {
            return null;
        }
    }

    /**
     * @param literal the query literal to resolve; must not be null.
     * @return the {@link Project}s belonging to the represented category - if the literal was empty then these will be
     * the projects with no category set. If the literal did not resolve to a category, this will be an empty set.
     */
    public Collection<Project> getProjectsForCategory(QueryLiteral literal)
    {
        notNull("literal", literal);

        if (literal.isEmpty())
        {
            return projectManager.getProjectObjectsWithNoCategory();
        }
        else
        {
            final GenericValue category = getProjectCategory(literal);
            return category != null ? projectManager.getProjectObjectsFromProjectCategory(category.getLong("id")) : Collections.<Project>emptySet();
        }
    }

    private GenericValue getProjectCategoryForString(final String rawValue)
    {
        GenericValue category = getProjectCategoryByName(rawValue);
        if (category == null)
        {
            final Long valueAsLong = getValueAsLong(rawValue);
            if (valueAsLong != null)
            {
                // Try to look up the category by id
                category = getProjectCategoryById(valueAsLong);
            }
        }

        return category;
    }

    private GenericValue getProjectCategoryForLong(final Long longValue)
    {
        GenericValue category = getProjectCategoryById(longValue);
        if (category == null)
        {
            // Try to look up the category by name
            category = getProjectCategoryByName(longValue.toString());
        }

        return category;
    }

    private GenericValue getProjectCategoryByName(final String rawValue)
    {
        return projectManager.getProjectCategoryByNameIgnoreCase(rawValue);
    }

    private GenericValue getProjectCategoryById(final Long rawValue)
    {
        return projectManager.getProjectCategory(rawValue);
    }

    private Long getValueAsLong(final String singleValueOperand)
    {
        try
        {
            return new Long(singleValueOperand);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
