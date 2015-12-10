package com.atlassian.jira.jql.values;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.comparator.LocaleSensitiveGenericValueComparator;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Generates the possible values of security levels that the user can see.
 *
 * @since v4.0
 */
public class SecurityLevelClauseValuesGenerator implements ClauseValuesGenerator
{
    private final IssueSecurityLevelManager issueSecurityLevelManager;

    public SecurityLevelClauseValuesGenerator(final IssueSecurityLevelManager issueSecurityLevelManager)
    {
        this.issueSecurityLevelManager = issueSecurityLevelManager;
    }

    public Results getPossibleValues(final User searcher, final String jqlClauseName, final String valuePrefix, final int maxNumResults)
    {

        final List<GenericValue> securityLevels = getRelevantSecurityLevels(searcher);

        Collections.sort(securityLevels, new LocaleSensitiveGenericValueComparator(getLocale(searcher), "name"));

        final Set<Result> results = new LinkedHashSet<Result>();
        for (GenericValue securityLevel : securityLevels)
        {
            if (results.size() == maxNumResults)
            {
                break;
            }
            final String levelName = securityLevel.getString("name");
            final String lowerCaseLevelName = levelName.toLowerCase();
            if (StringUtils.isBlank(valuePrefix) || lowerCaseLevelName.startsWith(valuePrefix.toLowerCase()))
            {
                results.add(new Result(levelName));
            }
        }

        return new Results(new ArrayList<Result>(results));
    }

    List<GenericValue> getRelevantSecurityLevels(final User searcher)
    {
        List<GenericValue> securityLevels;
        try
        {
            final Collection<GenericValue> secGvs = issueSecurityLevelManager.getAllUsersSecurityLevels(searcher);
            if (secGvs != null)
            {
                securityLevels = new ArrayList<GenericValue>(secGvs);
            }
            else
            {
                securityLevels = Collections.emptyList();
            }
        }
        catch (GenericEntityException e)
        {
            securityLevels = Collections.emptyList();
        }
        return securityLevels;
    }

    ///CLOVER:OFF
    Locale getLocale(final User searcher)
    {
        return new I18nBean(searcher).getLocale();
    }
    ///CLOVER:ON

}
