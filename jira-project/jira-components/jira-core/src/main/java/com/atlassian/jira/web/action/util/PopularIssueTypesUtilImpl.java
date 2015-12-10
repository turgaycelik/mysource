package com.atlassian.jira.web.action.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.IssueConstantFactory;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of PopularIssueTypesUtil
 *
 * @since v4.0
 */
public class PopularIssueTypesUtilImpl implements PopularIssueTypesUtil
{
    private static final Logger log = Logger.getLogger(PopularIssueTypesUtilImpl.class);
    /**
     * By default, return only the top 2 popular issue types
     */
    private static final int POPULAR_ISSUE_TYPE_COUNT = 2;
    /**
     * By default, look for issue types used in the past 2 weeks
     */
    private static final String PERIOD_OF_CREATION = "-2w";

    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final IssueConstantFactory factory;

    public PopularIssueTypesUtilImpl(final IssueTypeSchemeManager issueTypeSchemeManager, IssueConstantFactory factory)
    {
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.factory = factory;
    }

    public List<IssueType> getPopularIssueTypesForProject(final Project project, final User user)
    {
        return getPopularAndOtherIssueTypesForProject(project, user).getPopularIssueTypes();

    }

    public List<IssueType> getOtherIssueTypesForProject(final Project project, final User user)
    {
        return getPopularAndOtherIssueTypesForProject(project, user).getOtherIssueTypes();
    }

    public PopularIssueTypesHolder getPopularAndOtherIssueTypesForProject(final Project project, final User user)
    {
        Collection<IssueType> allTypes = issueTypeSchemeManager.getNonSubTaskIssueTypesForProject(project);
        // create a search request that returns us the relevant issues
        Set<IssueType> types = getPopularIssueTypes(project, user, allTypes);

        // otherwise, get as many as we can, and fill the rest with issue types from the scheme
        int limit = POPULAR_ISSUE_TYPE_COUNT;
        if (allTypes.size() == POPULAR_ISSUE_TYPE_COUNT + 1)
        {
            limit++;
        }

        types = limitTypes(allTypes, types, limit);
        allTypes.removeAll(types);

        return new PopularIssueTypesHolder(new ArrayList<IssueType>(types), new ArrayList<IssueType>(allTypes));
    }

    private Set<IssueType> limitTypes(Collection<IssueType> allTypes, Set<IssueType> types, int limit)
    {
        if (types.size() >= limit)
        {
            types = joinCollectionsWithLimit(types, Collections.<IssueType>emptyList(), limit);
        }
        else
        {
            types = joinCollectionsWithLimit(types, allTypes, limit);
        }
        return types;
    }

    private Set<IssueType> getPopularIssueTypes(Project project, User user, Collection<IssueType> allTypes)
    {
        Set<IssueType> types = getPopularIssueTypesFromSearch(project, user, PERIOD_OF_CREATION);
        types = removeSubtasks(types);

        // if we have no results with a user specified, try again without the user
        if (types.size() < POPULAR_ISSUE_TYPE_COUNT && !(types.size() == allTypes.size()))
        {
            types.addAll(removeSubtasks(getPopularIssueTypesFromSearch(project, user, null)));
        }

        if (user != null)
        {
            // if we have no results with a user specified, try again without the user
            if (types.size() < POPULAR_ISSUE_TYPE_COUNT && !(types.size() == allTypes.size()))
            {
                types.addAll(removeSubtasks(getPopularIssueTypesFromSearch(project, null, PERIOD_OF_CREATION)));
            }

            // if we have no results with a user specified, try again without the user
            if (types.size() < POPULAR_ISSUE_TYPE_COUNT && !(types.size() == allTypes.size()))
            {
                types.addAll(removeSubtasks(getPopularIssueTypesFromSearch(project, null, null)));
            }
        }
        return types;
    }

    /**
     * Uses a {@link SearchRequest} and a {@link StatisticAccessorBean} to retrieve the most popular issue types created
     * by the specified user in the specified project within a certain time frame.
     *
     * @param project the project to query
     * @param period  How long back should we go.  Null indicates no restriction.
     * @param user    the reporter to query; use null if reporter is not important
     * @return a set of the issue types, ordered by popularity descending. Should never be null.
     */
    ///CLOVER:OFF
    Set<IssueType> getPopularIssueTypesFromSearch(final Project project, final User user, final String period)
    {
        final SearchRequest sr = getSearchRequest(project, user, period);

        // get the stats of these issues grouped by issue type
        final StatisticAccessorBean stats = new StatisticAccessorBean(null, sr, true);
        final StatisticMapWrapper mapWrapper;
        try
        {
            mapWrapper = stats.getAllFilterBy(FilterStatisticsValuesGenerator.ISSUETYPE, StatisticAccessorBean.OrderBy.TOTAL, StatisticAccessorBean.Direction.DESC);
        }
        catch (SearchException e)
        {
            log.error("Error trying to find popular issue types for project '" + project + "' and user '" + user + "'.", e);
            throw new RuntimeException(e);
        }
        return mapWrapper.keySet();
    }
    ///CLOVER:ON

    /**
     * Converts the issue type {@link GenericValue} objects returned by the {@link StatisticMapWrapper} into {@link
     * IssueType} objects.
     *
     * @param issueTypeGVs the generic values to convert
     * @return a new set of IssueType objects.
     */
    ///CLOVER:OFF
    private Set<IssueType> getIssueTypesFromStatsMap(final Collection<GenericValue> issueTypeGVs)
    {
        Set<IssueType> types = new LinkedHashSet<IssueType>(issueTypeGVs.size());
        for (GenericValue issueTypeGV : issueTypeGVs)
        {
            types.add(factory.createIssueType(issueTypeGV));
        }
        return types;
    }
    ///CLOVER:ON

    /**
     * @param types input collection
     * @return a new list containing all the old issue types, minus the ones which are sub tasks.
     */
    ///CLOVER:OFF
    private Set<IssueType> removeSubtasks(Collection<IssueType> types)
    {
        Set<IssueType> typesWithoutSubtasks = new LinkedHashSet<IssueType>(types.size());
        for (IssueType type : types)
        {
            if (!type.isSubTask())
            {
                typesWithoutSubtasks.add(type);
            }
        }
        return typesWithoutSubtasks;
    }
    ///CLOVER:ON

    /**
     * Returns a {@link SearchRequest} that will return all the issues created in the specified project by the specified
     * user in a designated time frame.
     * <p/>
     * If no user is specified, the reporter parameter is ignored.
     *
     * @param project the project to query
     * @param user    the reporter; use null if reporter is not important
     * @param period  How long back should we go.  Null indicates no restriction.
     * @return the search request
     */
    ///CLOVER:OFF
    private SearchRequest getSearchRequest(final Project project, final User user, final String period)
    {
        final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().project(project.getId());
        if (!StringUtils.isBlank(period))
        {
            builder.and().createdAfter(period);
        }

        if (user != null && user.getName() != null)
        {
            builder.and().reporterUser(user.getName());
        }
        return new SearchRequest(builder.buildQuery());
    }
    ///CLOVER:ON

    /**
     * Returns a set of maximum size limit, which contains all the unique elements of a, followed by all the unique
     * elements of b that are not already in a. As soon as the limit is reached, the result is returned.
     *
     * @param a     the initial collection to copy in
     * @param b     the second collection to copy in. If empty collection is passed in, this method can be used to simply
     *              get the first limit elements of collection a.
     * @param limit the maximum number of elements in the resulting set
     * @return a set which contains at most limit elements; never null.
     */
    private Set<IssueType> joinCollectionsWithLimit(Collection<IssueType> a, Collection<IssueType> b, int limit)
    {
        int i = 0;
        Set<IssueType> result = new LinkedHashSet<IssueType>(limit);
        for (IssueType type : a)
        {
            if (!result.contains(type))
            {
                result.add(type);
                if (++i >= limit)
                {
                    return result;
                }
            }
        }

        for (IssueType type : b)
        {
            if (!result.contains(type))
            {
                result.add(type);
                if (++i >= limit)
                {
                    return result;
                }
            }
        }

        return result;
    }
}
