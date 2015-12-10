package com.atlassian.jira.jql.values;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generates values for status categories.
 *
 * @since v6.2
 */
public class StatusCategoryClauseValuesGenerator implements ClauseValuesGenerator
{
    private final StatusCategoryManager statusCategoryManager;

    public StatusCategoryClauseValuesGenerator(StatusCategoryManager statusCategoryManager)
    {
        this.statusCategoryManager = statusCategoryManager;
    }

    public Results getPossibleValues(final User searcher, final String jqlClauseName, final String valuePrefix, final int maxNumResults)
    {
        final List<StatusCategory> statusCategories = statusCategoryManager.getUserVisibleStatusCategories();

        final List<Result> results = new ArrayList<Result>(Math.min(statusCategories.size(), maxNumResults));
        for (StatusCategory statusCategory : statusCategories)
        {
            if (results.size() == maxNumResults)
            {
                break;
            }

            final String categoryName = statusCategory.getName();
            final String lowerCaseCategoryName = categoryName.toLowerCase();
            if (StringUtils.isBlank(valuePrefix) || lowerCaseCategoryName.startsWith(valuePrefix.toLowerCase()))
            {
                results.add(new Result(categoryName));
            }
        }

        return new Results(results);
    }

    ///CLOVER:OFF
    Locale getLocale(final User searcher)
    {
        return new I18nBean(searcher).getLocale();
    }
    ///CLOVER:ON

}
