package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchContextFactory;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestUtils;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;

import java.util.Comparator;
import java.util.List;

@PublicSpi
public abstract class AbstractCustomFieldStatisticsMapper implements StatisticsMapper
{
    protected final CustomField customField;

    public AbstractCustomFieldStatisticsMapper(CustomField customField)
    {
        this.customField = customField;
    }

    public String getDocumentConstant()
    {
        return customField.getId();
    }

    public boolean isValidValue(Object value)
    {
        return true;
    }

    public SearchRequest getSearchUrlSuffix(Object value, SearchRequest searchRequest)
    {
        if (searchRequest == null)
        {
            return null;
        }
        else
        {
            if (value != null)
            {
                final SearchService searchService = ComponentAccessor.getComponent(SearchService.class);
                final User user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
                SearchContext searchRequestContext = searchService.getSearchContext(user, searchRequest.getQuery());
                SearchContext possibleContext = getSearchContextFromValue(value);
                SearchContext combinedSearchContext = SearchRequestUtils.getCombinedSearchContext(searchRequestContext, possibleContext);

                CustomFieldSearcher searcher = customField.getCustomFieldSearcher();
                if (searcher.getSearchRenderer().isShown(user, combinedSearchContext))
                {
                    JqlClauseBuilder whereClauseBuilder = JqlQueryBuilder.newClauseBuilder(searchRequest.getQuery()).defaultAnd();

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

                    whereClauseBuilder.addStringCondition(JqlCustomFieldId.toString(customField.getIdAsLong()), getSearchValue(value));

                    return new SearchRequest(whereClauseBuilder.buildQuery());
                }
                else
                {
                    // Custom field cannot be shown on the issue navigator at this stage.
                    return null;
                }
            }
            else
            {
                // This hopes that the value will never be null, the previous impl never catered for a null value...
                return null;
            }
        }
    }

    protected SearchContext getSearchContextFromValue(Object value)
    {
        List projectIds = null;
        List issueTypeIds = null;

        List associatedProjects = customField.getAssociatedProjects();
        if (associatedProjects != null && !associatedProjects.isEmpty() && associatedProjects.get(0) != null)
        {
            projectIds = GenericValueUtils.transformToLongIdsList(associatedProjects);
        }

        List associatedIssueTypes = customField.getAssociatedIssueTypes();
        if (associatedIssueTypes != null && !associatedIssueTypes.isEmpty() && associatedIssueTypes.get(0) != null)
        {
            issueTypeIds = GenericValueUtils.transformToStringIdsList(associatedIssueTypes);
        }
        SearchContextFactory searchContextFactory = ComponentAccessor.getComponent(SearchContextFactory.class);
        return searchContextFactory.create(null, projectIds, issueTypeIds);
    }

    /**
     * String value to be passed to the search request to construct a new request URL
     *
     * @param value returned from {@link #getValueFromLuceneField}
     * @return String
     */
    protected abstract String getSearchValue(Object value);

    public Comparator getComparator()
    {
        return new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                if (o1 == null && o2 == null)
                {
                    return 0;
                }
                else if (o1 == null)
                {
                    return 1;
                }
                else if (o2 == null)
                {
                    return -1;
                }
                return ((String) o1).compareTo((String) o2);
            }
        };
    }

    public boolean isFieldAlwaysPartOfAnIssue()
    {
        return false;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final AbstractCustomFieldStatisticsMapper that = (AbstractCustomFieldStatisticsMapper) o;

        return (getDocumentConstant() != null ? getDocumentConstant().equals(that.getDocumentConstant()) : that.getDocumentConstant() == null);
    }

    public int hashCode()
    {
        return (getDocumentConstant() != null ? getDocumentConstant().hashCode() : 0);
    }

}
