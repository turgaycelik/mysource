package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.query.Query;
import webwork.action.Action;

import java.util.Map;

/**
 * Handles the rendering of field search information for the JIRA issue navigator. The html that is produced by
 * these methods will create request parameters that can be processed by a
 * {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer} to populate a SearchRequest.
 *
 * @since v4.0
 */
@PublicSpi
public interface SearchRenderer
{
    /**
     * Used to produce an HTML input that is rendered on the JIRA issue navigator. This HTML provides the UI
     * for searching a fields content. There will be a corresponding {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer}
     * that will know how to transform these input parameters into JIRA search objects.
     *
     * @param user performing this action.
     * @param searchContext the search context of the current search request that may be participating in rendering the
     * issue navigator.
     * @param fieldValuesHolder contains any request parameters that the HTML input may need to use to pre-populate
     * the input (e.g. if this is the priority renderer and the search request being rendered has two priorities already
     * selected these params will contain these request parameters). These parameters will have been populated via a
     * call to {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer#populateFromQuery(User,com.atlassian.jira.issue.transport.FieldValuesHolder,com.atlassian.query.Query,com.atlassian.jira.issue.search.SearchContext)}
     * if there is a SearchRequest involved in the rendering this IssueNavigator call.
     * @param displayParameters are a map of "hints" that can be passed from the caller to this code which can use these
     * hints to alter the way it renders the HTML.
     * @param action is the WebWork 1 action object that is rendering the Issue Navigator view. This can be used to
     * invoke methods on the view.
     * @return a String that contains HTML that can be rendered on the left-hand side of the JIRA issue navigator.
     */
    String getEditHtml(User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters, Action action);

    /**
     * Checks if the searcher should be shown in this context on the Issue Navigator.
     *
     * @param user performing this action.
     * @param searchContext the context of the search (i.e. projects and issue types selected).
     * @return true if the searcher will appear in the issue navigator, false otherwise.
     */
    boolean isShown(User user, SearchContext searchContext);

    /**
     * Used to produce the HTML that displays a searchers summary information (e.g. if this is the priority searcher
     * and a user has selected two priorities then this method will render HTML that shows something like 'priority: Major, Minor').
     *
     * @param user  performing this action.
     * @param searchContext the search context of the current search request that may be participating in rendering the
     * issue navigator.
     * @param fieldValuesHolder contains any request parameters that the HTML input may need to use to pre-populate
     * the input (e.g. if this is the priority renderer and the search request being rendered has two priorities already
     * selected these params will contain these request parameters). These parameters will have been populated via a
     * call to {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer#populateFromQuery(User,com.atlassian.jira.issue.transport.FieldValuesHolder,com.atlassian.query.Query,com.atlassian.jira.issue.search.SearchContext)}
     * if there is a SearchRequest involved in the rendering this IssueNavigator call.
     * @param displayParameters are a map of "hints" that can be passed from the caller to this code which can use these
     * hints to alter the way it renders the HTML.
     * @param action is the WebWork 1 action object that is rendering the Issue Navigator view. This can be used to
     * invoke methods on the view.
     * @return a String that contains HTML that can be rendered on the left-hand side of the issue navigator to show
     * a SearchRequest summary.
     */
    String getViewHtml(User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters, Action action);

    /**
     * Checks if the searchRequest object has a search representation that was created by the searcher and is
     * used to determine if the {@link #getViewHtml(User, com.atlassian.jira.issue.search.SearchContext, com.atlassian.jira.issue.transport.FieldValuesHolder, java.util.Map, webwork.action.Action)}
     * method should be called when rendering the search summary.
     *
     * @param user  performing this action.
     * @param query contains the search criteria used to determine if this query is relevevant to the searcher.
     * @return true if the query has relevant clauses to the searchers, false otherwise.
     */
    boolean isRelevantForQuery(User user, Query query);
}
