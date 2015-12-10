package com.atlassian.jira.jql.resolver;

import com.atlassian.fugue.Option;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.query.operand.EmptyOperand;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Resolves Status Category objects
 *
 * @since v6.2
 */
public class StatusCategoryResolver
{
    private final StatusCategoryManager statusCategoryManager;

    public StatusCategoryResolver(StatusCategoryManager statusCategoryManager)
    {
        this.statusCategoryManager = statusCategoryManager;
    }

    public Set<StatusCategory> getStatusCategories(Collection<QueryLiteral> queryLiterals)
    {
        final Set<StatusCategory> categories = new HashSet<StatusCategory>(queryLiterals.size());

        for (QueryLiteral rawValue : queryLiterals)
        {
            final Option<StatusCategory> category = getStatusCategory(rawValue);
            if (category.isDefined())
            {
                categories.add(category.get());
            }
        }

        return categories;
    }

    public Option<StatusCategory> getStatusCategory(QueryLiteral rawValue)
    {
        if ((rawValue.getSourceOperand() instanceof EmptyOperand))
        {
            return Option.some(statusCategoryManager.getDefaultStatusCategory());
        }
        else
        {
            return forId(rawValue.getLongValue())
                    .orElse(forKey(rawValue.getStringValue()))
                    .orElse(forName(rawValue.getStringValue()));

        }
    }

    public Option<StatusCategory> forId(Long id)
    {
        return id == null ? Option.<StatusCategory>none() : Option.option(statusCategoryManager.getStatusCategory(id));
    }

    public Option<StatusCategory> forKey(String key)
    {
        return key == null ? Option.<StatusCategory>none() : Option.option(statusCategoryManager.getStatusCategoryByKey(key));
    }

    public Option<StatusCategory> forName(String name)
    {
        return name == null ? Option.<StatusCategory>none() : Option.option(statusCategoryManager.getStatusCategoryByName(name));
    }
}
