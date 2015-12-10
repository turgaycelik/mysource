package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;

/**
 * Used to convert input parameters as submitted by a {@link com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer}
 * into the intermediate form, as stored in a {@link com.atlassian.jira.issue.transport.FieldValuesHolder} and then
 * from a FieldValuesHolder into an object form that is stored in the
 * {@link com.atlassian.jira.issue.search.SearchRequest} that is used to execute a search in JIRA.
 *
 * @since v4.0
 */
@PublicSpi
public interface SearchInputTransformer
{
    /**
     * Populate {@link FieldValuesHolder} object with whatever values the searcher is interested in from the
     * {@link com.atlassian.jira.issue.transport.ActionParams}. This transforms the "raw" request parameters
     * into a form that the other processing methods can handle (usually a mapping of the fields name as the key
     * and a list of the values as the value).
     *
     * @param user performing this action.
     * @param fieldValuesHolder is the object that should have its values set by this method and that will contain
     * any other values that have been set by other SearchInputTransformers.
     * @param actionParams params from the webwork front end that contains a String[] of values as submitted via the
     */
    void populateFromParams(User user, FieldValuesHolder fieldValuesHolder, ActionParams actionParams);

    /**
     * Adds error messages to the errors object if values in the fieldValuesHolder fails validation. This should be
     * called once the fieldValuesHolder has been populated.
     *
     * @param user performing this action.
     * @param searchContext the context of the search (i.e. projects and issue types selected).
     * @param fieldValuesHolder contains values populated by the populate methods of this input transformer.
     * @param i18nHelper used to internationalize error messages that we want to display to the users.
     * @param errors the ErrorCollection that contains the messages we want to display to the users.
     */
    void validateParams(User user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, I18nHelper i18nHelper, ErrorCollection errors);

    /**
     * This method transforms any query information contained in the query that is relevant to this
     * SearchInputTransformer into the values that the HTML rendering expects. This should
     * populate the {@link com.atlassian.jira.issue.transport.FieldValuesHolder} from the a query information in the
     * query.
     * <br/>
     * The query elements that are considered "relevant" to this method would be those that are produced by the
     * {@link #getSearchClause(User, com.atlassian.jira.issue.transport.FieldValuesHolder)} method.
     *
     * @param user performing this action.
     * @param fieldValuesHolder is the object that should have its values set by this method and that will contain
     * any other values that have been set by other SearchInputTransformers.
     * @param query the search criteria used to populate the field values holder.
     * @param searchContext contains the projects and issue types that the search and filter form is restricted to
     */
    void populateFromQuery(User user, FieldValuesHolder fieldValuesHolder, Query query, final SearchContext searchContext);

    /**
     * Tells the caller whether or not the relevant clauses from the passed query can be represented on the issue
     * navigator. Implementers of this method needs to ensure that it can represent *ALL* related clauses on the
     * navigator, and that the clauses' structure conforms to the simple navigator structure.
     * <p/>
     * The method should only be concerned with the clauses related to this transformer. Other irrelevant clauses should
     * be ignored. 
     *
     * @param user performing this action.
     * @param query to check if it can fit in the simple (GUI form based) issue navigator.
     * @param searchContext contains the projects and issue types that the search and filter form is restricted to
     * @return true if the query can be represented on navigator.
     */
    boolean doRelevantClausesFitFilterForm(User user, Query query, SearchContext searchContext);

    /**
     * Gets the portion of the Search Query that this searcher is responsible for.
     *
     * @param user performing this action.
     * @param fieldValuesHolder contains values populated by the searchers
     * @return a {@link com.atlassian.query.clause.Clause} that represents the users search based on the fieldValuesHolder;
     * null if this searcher has no responsibility in the given input.
     */
    Clause getSearchClause(User user, FieldValuesHolder fieldValuesHolder);

}
