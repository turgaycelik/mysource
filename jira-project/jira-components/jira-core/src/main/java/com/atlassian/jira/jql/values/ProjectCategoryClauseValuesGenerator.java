package com.atlassian.jira.jql.values;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.comparator.LocaleSensitiveGenericValueComparator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Generates values for project categories.
 *
 * @since v4.0
 */
public class ProjectCategoryClauseValuesGenerator implements ClauseValuesGenerator
{
    private final ProjectManager projectManager;

    public ProjectCategoryClauseValuesGenerator(final ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public Results getPossibleValues(final User searcher, final String jqlClauseName, final String valuePrefix, final int maxNumResults)
    {
        final List<GenericValue> projectCategories = new ArrayList<GenericValue>(projectManager.getProjectCategories());

        Collections.sort(projectCategories, new LocaleSensitiveGenericValueComparator(getLocale(searcher), "name"));

        final List<Result> results = new ArrayList<Result>();
        for (GenericValue projectCategoryGv : projectCategories)
        {
            if (results.size() == maxNumResults)
            {
                break;
            }
            final String categoryName = projectCategoryGv.getString("name");
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
