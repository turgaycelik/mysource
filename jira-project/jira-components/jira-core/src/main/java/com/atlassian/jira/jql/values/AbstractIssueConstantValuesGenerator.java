package com.atlassian.jira.jql.values;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.comparator.LocaleSensitiveStringComparator;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Generates autocomplete values for IssueConstants.
 *
 * @since v4.0
 */
public abstract class AbstractIssueConstantValuesGenerator implements ClauseValuesGenerator
{

    public ClauseValuesGenerator.Results getPossibleValues(final User searcher, final String jqlClauseName, final String valuePrefix, final int maxNumResults)
    {
        final List<String> constantNames = getAllConstantNames();

        Collections.sort(constantNames, new LocaleSensitiveStringComparator(getLocale(searcher)));

        final List<ClauseValuesGenerator.Result> suggestions = new ArrayList<ClauseValuesGenerator.Result>();
        for (String constantName : constantNames)
        {
            if (suggestions.size() == maxNumResults)
            {
                break;
            }
            final String lowerCaseConstName = constantName.toLowerCase();
            if (StringUtils.isBlank(valuePrefix) ||
                lowerCaseConstName.startsWith(valuePrefix.toLowerCase()))
            {
                suggestions.add(new ClauseValuesGenerator.Result(constantName));
            }
        }

        return new ClauseValuesGenerator.Results(suggestions);
    }

    ///CLOVER:OFF
    Locale getLocale(final User searcher)
    {
        return new I18nBean(searcher).getLocale();
    }
    ///CLOVER:ON

    /**
     * @return a modifiable list of all the constants
     */
    protected abstract List<IssueConstant> getAllConstants();

    /**
     * @return a modifiable list of all the constant names
     */
    protected List<String> getAllConstantNames()
    {
        return new ArrayList<String>(CollectionUtil.transform(getAllConstants().iterator(), constantToNameFunction));
    }

    private static final Function<IssueConstant, String> constantToNameFunction = new Function<IssueConstant, String>()
    {
        public String get(final IssueConstant input)
        {
            return input.getName();
        }
    };
}
