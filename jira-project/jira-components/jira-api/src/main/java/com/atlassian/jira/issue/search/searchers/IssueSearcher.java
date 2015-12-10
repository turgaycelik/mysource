package com.atlassian.jira.issue.search.searchers;

import com.atlassian.annotations.PublicApi;
import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.transport.ActionParams;
import org.apache.log4j.Logger;

/**
 * The interface defines an object responsible for all search related activities in the Issue Navigator.
 * The interface operates similar to the {@link Field} objects (e.g. {@link OrderableField}. It is responsible for
 * populating itself from {@link ActionParams} and {@link SearchRequest} as well as all rendering related activities.
 * <p/>
 * {@link CustomField} searchers should still extend the sub-interface {@link CustomFieldSearcher}.
 * @since JIRA 3.3
 */
@PublicApi
@PublicSpi
public interface IssueSearcher<T extends SearchableField>
{
    // ------------------------------------------------------------------------------------------------------- Constants
    static final Logger log = Logger.getLogger(IssueSearcher.class);

    // ------------------------------------------------------------------------------------------ Initialisation Methods
    /**
     * Initialises the searcher with a given field.
     *
     * @param field the field object. This <strong>may</strong> be null. (So you can have searchers on non-fields)
     */
    void init(T field);

    /**
     * Provides an object that contains information about the Searcher.
     *
     * @return the search information provider for this searcher.
     */
    SearcherInformation<T> getSearchInformation();

    /**
     * Provides an object that will allow you to transform raw request parameters to field holder values and
     * field holder values to {@link com.atlassian.query.clause.Clause} search representations.
     *
     * @return the search input handler for this searcher.
     */
    SearchInputTransformer getSearchInputTransformer();

    /**
     * Provides an object that will allow you to render the edit and view html for a searcher. This also provides
     * methods that indicate if the view and edit methods should be invoked.
     *
     * @return the search renderer for this searcher.
     */
    SearchRenderer getSearchRenderer();
}
