package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.search.SearchRequestUtils;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.Query;

import java.util.Iterator;
import java.util.List;

/**
 * This is an implementation of SearchRequestAppender suitable for handling custom field values. This is typically used
 * by subclasses of AbstractCustomFieldStatisticsMapper which also implement SearchRequestAppender.Factory.
 *
 * @see SearchRequestAppender
 * @see AbstractCustomFieldStatisticsMapper
 * @see SearchRequestAppender.Factory
 * @see CustomField
 * @since v6.0
 */
public class CustomFieldSearchRequestAppender implements SearchRequestAppender
{
    private final CustomField customField;
    private final AbstractCustomFieldStatisticsMapper customFieldStatisticsMapper;

    public CustomFieldSearchRequestAppender(CustomField customField, AbstractCustomFieldStatisticsMapper customFieldStatisticsMapper)
    {
        this.customField = Assertions.notNull(customField);
        this.customFieldStatisticsMapper = Assertions.notNull(customFieldStatisticsMapper);
    }

    @Override
    public SearchRequest appendInclusiveSingleValueClause(Object value, SearchRequest searchRequest)
    {
        if (searchRequest != null)
        {
            final JqlQueryBuilder newQueryBuilder = JqlQueryBuilder.newBuilder(searchRequest.getQuery());
            JqlClauseBuilder whereClauseBuilder = newQueryBuilder.where().defaultAnd();
            if (!populateValueClause(value, searchRequest.getQuery(), whereClauseBuilder))
            {
                return null;
            }
            return new SearchRequest(whereClauseBuilder.buildQuery());

        }
        else
        {
            return null;
        }
    }

    @Override
    public SearchRequest appendExclusiveMultiValueClause(Iterable values, SearchRequest searchRequest)
    {
        if (searchRequest != null)
        {
            final JqlQueryBuilder newQueryBuilder = JqlQueryBuilder.newBuilder(searchRequest.getQuery());
            JqlClauseBuilder whereClauseBuilder = newQueryBuilder.where().and().not().sub();

            Iterator<Object> iter = values.iterator();
            while (iter.hasNext())
            {
                Object value = iter.next();
                whereClauseBuilder.sub().defaultAnd();
                if (!populateValueClause(value, searchRequest.getQuery(), whereClauseBuilder))
                {
                    return null;
                }
                whereClauseBuilder.endsub();
                if (iter.hasNext())
                {
                    whereClauseBuilder.or();
                }
            }
            whereClauseBuilder.endsub();
            return new SearchRequest(whereClauseBuilder.buildQuery());
        }
        else
        {
            return null;
        }
    }

    private boolean populateValueClause(Object value, Query baseQuery, JqlClauseBuilder whereClauseBuilder)
    {
        if (value != null)
        {
            final SearchService searchService = ComponentAccessor.getComponent(SearchService.class);
            final User user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
            SearchContext searchRequestContext = searchService.getSearchContext(user, baseQuery);
            SearchContext possibleContext = customFieldStatisticsMapper.getSearchContextFromValue(value);
            SearchContext combinedSearchContext = SearchRequestUtils.getCombinedSearchContext(searchRequestContext, possibleContext);

            CustomFieldSearcher searcher = customField.getCustomFieldSearcher();
            if (searcher.getSearchRenderer().isShown(user, combinedSearchContext))
            {
                // Include the project/issue types from the context
                final List<Long> projectIds = combinedSearchContext.getProjectIds();
                if (projectIds != null && !projectIds.isEmpty())
                {
                    whereClauseBuilder.project().inNumbers(projectIds);
                }
                final List<String> issueTypeIds = combinedSearchContext.getIssueTypeIds();
                if (issueTypeIds != null && !issueTypeIds.isEmpty())
                {
                    whereClauseBuilder.issueType().inStrings(issueTypeIds);
                }

                // Now lets and all of the query with the current custom field value we are looking for

                whereClauseBuilder.addStringCondition(JqlCustomFieldId.toString(customField.getIdAsLong()), customFieldStatisticsMapper.getSearchValue(value));

                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
}