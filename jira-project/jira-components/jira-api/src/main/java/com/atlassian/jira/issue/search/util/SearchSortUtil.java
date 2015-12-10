package com.atlassian.jira.issue.search.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.util.I18nHelper;
import javax.annotation.Nonnull;
import com.atlassian.query.Query;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.SearchSort;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Looks at the current search sorts on a query and will add the default JIRA search sorts (issue key or none if there
 * is a text search included in the query) if there are no user specified sorts.
 *
 * @since v4.0
 */
public interface SearchSortUtil
{
    String SORTER_ORDER = "sorter/order";
    String SORTER_FIELD = "sorter/field";

    /**
     * Combine the new search sorts and the old search sorts returning a list of sorts that is only of size maxLength.
     * Old sorts will fall off the end of the list first. If there are any sorts that are duplicated (the field is
     * mentioned again, sort order not taken into account), then the old sort reference will not be mentioned and will
     * be replaced with the new sort in the correct position in the list.
     *
     * @param user performing the search
     * @param newSorts the new sorts that should go in the front of the sort list; must not be null
     * @param oldSorts the old sorts that should be in the end of the sort list; may be null
     * @param maxLength the max size of the produced list
     * @return a list of search sorts that contains the newest and oldest sorts respecting the max length.
     */
    @Nonnull
    List<SearchSort> mergeSearchSorts(User user, Collection<SearchSort> newSorts, Collection<SearchSort> oldSorts, int maxLength);

    List<SearchSort> getSearchSorts(Query query);

    /**
     * This method is used to convert incomming, request-style, parameters into SearchSort objects.
     *
     * @param parameterMap contains 0 or many "sorter/order" and "sorter/field" parameters that will
     *                     be converted into a search sort. The field is the System/Custom field name and will be converted
     *                     by this method into the JQL Primary clause name. The reason for this is that we need to support
     *                     "old (pre 4.0)" URL parameters and these contain the field id, not the clause name. Since the
     *                     UI is the only thing producing these parameters we decided to leave it generating the field
     *                     id. When sorts are specified in JQL they will be in clause names.
     *
     * @return an OrderBy that can be used to populate a {@link com.atlassian.query.Query} which contains alist
     * of SearchSort's that relate to the passed in parameters. Will be an order by with empty sorts if there are no
     * search sorts in the parameters.
     */
    @Nonnull
    OrderBy getOrderByClause(Map parameterMap);

    /**
     * Concatenate the new search sorts and the old search sorts returning a list of sorts that is only of size maxLength.
     * Old sorts will fall off the end of the list first.
     *
     * @param newSorts the new sorts that should go in the front of the sort list; must not be null
     * @param oldSorts the old sorts that should be in the end of the sort list; may be null
     * @param maxLength the max size of the produced list
     * @return a list of search sorts that contains the newest and oldest sorts respecting the max length.
     */
    @Nonnull
    List<SearchSort> concatSearchSorts(Collection<SearchSort> newSorts, Collection<SearchSort> oldSorts, int maxLength);

    /**
     * Returns a list of the descriptions of each sorter defined in the search request.
     *
     * If one of the sorters references a field which does not exist, it will be skipped.
     *
     * @param searchRequest the search request containing the sorts; must not be null.
     * @param i18nHelper i18n helper
     * @param searcher the user making the request
     * @return a list of strings describing the sorters; never null.
     *
     * @since v5.0 (copied from SearchRequestUtils)
     */
    List<String> getSearchSortDescriptions(SearchRequest searchRequest, final I18nHelper i18nHelper, final User searcher);
}
