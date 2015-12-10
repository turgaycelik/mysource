package com.atlassian.jira.issue.search.searchers.information;

import com.atlassian.annotations.PublicApi;
import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;

import java.util.List;

/**
 * Identifies a searcher by name and provides a display name that is i18n'ed.
 *
 * @since v4.0
 */
@PublicApi
@PublicSpi
public interface SearcherInformation<T extends SearchableField>
{
    /**
     * The unique id of the searcher.
     * @return unique id of the searcher.
     */
    String getId();

    /**
     * The i18n key that is used to lookup the searcher's name when it is displayed.
     * @return i18n key that is used to lookup the searcher's name when it is displayed.
     */
    String getNameKey();

    /**
     * @return the field that this searcher was initialised with. If the searcher has not yet been initialised,
     * this will return null.
     */
    T getField();

    /**
     * Returns a list of {@link com.atlassian.jira.issue.index.indexers.FieldIndexer} objects. The objects should be initialised and ready for action
     * @return {@link java.util.List} of {@link com.atlassian.jira.issue.index.indexers.FieldIndexer} objects. Must not be null. Return an empty list if none available
     */
    List<FieldIndexer> getRelatedIndexers();

    /**
     * The searcher group the searcher should be placed in. Really only useful for system fields as custom
     * fields are forced into the {@link com.atlassian.jira.issue.search.searchers.SearcherGroupType#CUSTOM}
     * group.
     *
     * @return the group the searcher should be associated with. Cannot not be null.
     */
    SearcherGroupType getSearcherGroupType();
    
}
