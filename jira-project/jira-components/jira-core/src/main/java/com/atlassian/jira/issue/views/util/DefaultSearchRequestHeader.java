package com.atlassian.jira.issue.views.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroup;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class prints the description of a search request, including a bordered box.  If you wish to use it,
 * then the moduledescriptor that calls it must have the 'searchrequest-description-header' as a resource named
 * {@link #DESCRIPTION_TEMPLATE_NAME}.
 */
public class DefaultSearchRequestHeader implements SearchRequestHeader
{
    private static final String DESCRIPTION_TEMPLATE_NAME = "descriptionTable";

    private final IssueSearcherManager issueSearcherManager;
    private final ApplicationProperties applicationProperties;
    private final SearchProvider searchProvider;
    private final JiraAuthenticationContext authenticationContext;
    private final SearchSortUtil searchSortUtil;
    private final SearchService searchService;
    private final DateTimeFormatter dateTimeFormatter;

    public DefaultSearchRequestHeader(final IssueSearcherManager issueSearcherManager, final ApplicationProperties applicationProperties,
            final SearchProvider searchProvider, final JiraAuthenticationContext authenticationContext,
            final SearchSortUtil searchSortUtil, final SearchService searchService, DateTimeFormatter dateTimeFormatter)
    {
        this.issueSearcherManager = issueSearcherManager;
        this.applicationProperties = applicationProperties;
        this.searchProvider = searchProvider;
        this.authenticationContext = authenticationContext;
        this.searchSortUtil = searchSortUtil;
        this.searchService = searchService;
        this.dateTimeFormatter = dateTimeFormatter != null ? dateTimeFormatter.forLoggedInUser().withStyle(DateTimeStyle.COMPLETE) : null;
    }

    /* (non-Javadoc)
     * @see com.atlassian.jira.issue.views.util.SearchRequestHeader#getHeader(com.atlassian.jira.issue.search.SearchRequest, com.atlassian.jira.web.bean.PagerFilter, com.atlassian.jira.plugin.JiraResourcedModuleDescriptor)
     */
    public String getHeader(final SearchRequest searchRequest, final PagerFilter pagerFilter, final JiraResourcedModuleDescriptor<?> descriptor)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        try
        {
            params.put("title", SearchRequestViewUtils.getTitle(searchRequest, applicationProperties.getString(APKeys.JIRA_TITLE)));

            final boolean fitsNavigator = isSearchRequestFitsNavigator(searchRequest);
            params.put("isSearchRequestFitsNavigator", fitsNavigator);
            if (fitsNavigator)
            {
                params.put("searcherDescriptions", getSearcherViewHtmlDescriptions(searchRequest));
            }
            else
            {
                params.put("searchRequestJqlString", getSearchRequestJqlString(searchRequest));
            }
            
            params.put("searchSortDescriptions", getSearchSortDescriptions(searchRequest));
            final long searchResultsTotalCount = searchProvider.searchCount(searchRequest.getQuery(), authenticationContext.getLoggedInUser());
            params.put("searchResultsTotalCount", Long.valueOf(searchResultsTotalCount).intValue());
            params.put("searchResultsStart", getNiceStart(pagerFilter.getStart(), searchResultsTotalCount));
            params.put("searchResultsEnd", Math.min(pagerFilter.getStart() + pagerFilter.getMax(), searchResultsTotalCount));
            params.put("i18n", authenticationContext.getI18nHelper());
            params.put("now", dateTimeFormatter.format(new Date()));
            final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
            params.put("link", SearchRequestViewUtils.getLink(searchRequest, velocityRequestContext.getBaseUrl(), authenticationContext.getLoggedInUser()));
        }
        catch (final SearchException e)
        {
            throw new DataAccessException(e);
        }
        return descriptor.getHtml(DESCRIPTION_TEMPLATE_NAME, params);
    }

    private int getNiceStart(final int start, final long total)
    {
        if (total == 0)
        {
            return 0;
        }
        else
        {
            return start + 1;
        }
    }

    private Collection<String> getSearchSortDescriptions(final SearchRequest searchRequest)
    {
        return searchSortUtil.getSearchSortDescriptions(searchRequest, authenticationContext.getI18nHelper(), authenticationContext.getLoggedInUser());
    }

    private Collection<String> getSearcherViewHtmlDescriptions(final SearchRequest searchRequest)
    {
        final List<String> descriptions = new ArrayList<String>();
        final Collection<SearcherGroup> searcherGroups = issueSearcherManager.getSearcherGroups();
        for (final SearcherGroup searcherGroup : searcherGroups)
        {
            for (final Object element : searcherGroup.getSearchers())
            {
                final IssueSearcher issueSearcher = (IssueSearcher) element;
                descriptions.add(getSearcherViewHtml(issueSearcher, searchRequest));
            }
        }
        return descriptions;
    }

    private boolean isSearchRequestFitsNavigator(final SearchRequest searchRequest)
    {
        return searchService.doesQueryFitFilterForm(authenticationContext.getLoggedInUser(), (searchRequest != null) ? searchRequest.getQuery() : null);
    }

    private String getSearchRequestJqlString(SearchRequest searchRequest)
    {
        if (searchRequest != null)
        {
            return searchService.getJqlString(searchRequest.getQuery());
        }
        else
        {
            return "";
        }
    }

    private String getSearcherViewHtml(final IssueSearcher searcher, final SearchRequest searchRequest)
    {
        final SearchRenderer searchRenderer = searcher.getSearchRenderer();
        if ((searchRequest != null) && searchRenderer.isRelevantForQuery(authenticationContext.getLoggedInUser(), searchRequest.getQuery()))
        {
            // We can pass a null action because none of the searchers views use the action that is passed in. It should
            // probably be removed from the API as it does not make a ton of sense for the view html to try to attach
            // errors to the action.
            final Action action = null;
            final Query query = searchRequest.getQuery();
            final SearchContext searchContext = searchService.getSearchContext(authenticationContext.getLoggedInUser(), query);
            return searchRenderer.getViewHtml(authenticationContext.getLoggedInUser(), searchContext, getFieldValuesHolder(authenticationContext.getLoggedInUser(), query, searchContext),
                new HashMap(), action);
        }
        else
        {
            return "";
        }
    }

    private FieldValuesHolder getFieldValuesHolder(final User searcher, final Query query, final SearchContext searchContext)
    {
        final FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        populateFieldValuesHolderFromSR(searcher, fieldValuesHolder, query, searchContext);

        return fieldValuesHolder;
    }

    private void populateFieldValuesHolderFromSR(final User searcher, final FieldValuesHolder fieldValuesHolder, final Query query, final SearchContext searchContext)
    {

        final Collection<IssueSearcher<?>> searchers = issueSearcherManager.getAllSearchers();
        for (final IssueSearcher<?> issueSearcher : searchers)
        {
            issueSearcher.getSearchInputTransformer().populateFromQuery(searcher, fieldValuesHolder, query, searchContext);
        }
    }
}
