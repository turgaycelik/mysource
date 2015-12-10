package com.atlassian.jira.issue.search.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.Query;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchSortUtilImpl implements SearchSortUtil
{
    private static final Logger log = Logger.getLogger(SearchSortUtilImpl.class);

    private final SearchHandlerManager searchHandlerManager;
    private final FieldManager fieldManager;
    final static SearchSort DEFAULT_KEY_SORT = new SearchSort(SystemSearchConstants.forIssueKey().getJqlClauseNames().getPrimaryName(), SortOrder.DESC);

    public SearchSortUtilImpl(final SearchHandlerManager searchHandlerManager, final FieldManager fieldManager)
    {
        this.searchHandlerManager = searchHandlerManager;
        this.fieldManager = fieldManager;
    }

    public List<SearchSort> concatSearchSorts(final Collection<SearchSort> newSorts, final Collection<SearchSort> oldSorts, final int maxLength)
    {
        // like merge, except we don't bother checking for duplicates - just join the two collections together (new then old)
        Assertions.notNull("newSorts", newSorts);

        Iterator<SearchSort> source = newSorts.iterator();
        if (oldSorts != null)
        {
            source = IteratorUtils.chainedIterator(source, oldSorts.iterator());
        }

        final List<SearchSort> calcSorts = new ArrayList<SearchSort>();
        while (calcSorts.size() < maxLength && source.hasNext())
        {
            calcSorts.add(source.next());
        }

        return calcSorts;
    }

    public List<SearchSort> mergeSearchSorts(final User user, Collection<SearchSort> newSorts, final Collection<SearchSort> oldSorts, final int maxLength)
    {
        Assertions.notNull("newSorts", newSorts);

        // create an iterator that runs together new and old sorts
        Iterator<SearchSort> source = newSorts.iterator();
        if (oldSorts != null)
        {
            newSorts = convertNewSortsToKeepOldSortNames(user, newSorts, oldSorts);
            source = IteratorUtils.chainedIterator(newSorts.iterator(), oldSorts.iterator());
        }

        final Set<String> currentPrimaryClauseNames = new HashSet<String>();
        final List<SearchSort> calcSorts = new ArrayList<SearchSort>();
        while ((maxLength <= 0 || calcSorts.size() < maxLength) && source.hasNext())
        {
            final SearchSort sort = source.next();

            // get all the handlers represented by this clause
            final Collection<ClauseHandler> handlers = searchHandlerManager.getClauseHandler(user, sort.getField());

            // get the primary clause names for each of the handlers
            final List<String> primaryClauseNames = new ArrayList<String>(CollectionUtil.transform(handlers.iterator(), new Function<ClauseHandler, String>()
            {
                public String get(final ClauseHandler handler)
                {
                    return handler.getInformation().getJqlClauseNames().getPrimaryName();
                }
            }));
            Collections.sort(primaryClauseNames);

            // if the primary clause names from this clause are entirely new,
            // then we can just add the clause name as it came in
            if (Collections.disjoint(currentPrimaryClauseNames, primaryClauseNames))
            {
                calcSorts.add(sort);
                currentPrimaryClauseNames.addAll(primaryClauseNames);
            }
            else
            {
                // otherwise we have to split the input clause name into its primary clause names,
                // and only add those which have not already been used
                final Collection<String> primaryClauseNamesNotYetUsed = CollectionUtils.subtract(primaryClauseNames, currentPrimaryClauseNames);

                calcSorts.addAll(CollectionUtil.transform(primaryClauseNamesNotYetUsed.iterator(), new Function<String, SearchSort>()
                {
                    public SearchSort get(final String clauseName)
                    {
                        return new SearchSort(clauseName, sort.getSortOrder());
                    }
                }));
                currentPrimaryClauseNames.addAll(primaryClauseNamesNotYetUsed);
            }
        }

        return calcSorts;
    }

    /**
     * Converts any new sorts which refer to the same clause as any existing old sorts to use the old sort's name if it
     * is different to the new name.
     * <p>
     * For example, if your new sort is <code>Key DESC</code> and you have an old sort <code>issueKey ASC</code>, the new
     * sort will be converted to be <code>issueKey DESC</code>.
     * <p>
     * The point here is to preserve old JQL strings as much as possible (but only when it is functionally identical).
     * For more info see http://jira.atlassian.com/browse/JRA-17908.
     *
     * @param user the user performing the search
     * @param newSorts the new sorts. must not be null.
     * @param oldSorts the old sorts. must not be null.
     * @return the converted new sorts; never null.
     */
    Collection<SearchSort> convertNewSortsToKeepOldSortNames(final User user, final Collection<SearchSort> newSorts, final Collection<SearchSort> oldSorts)
    {
        final Collection<SearchSort> convertedSorts = new ArrayList<SearchSort>();
        for (SearchSort newSort : newSorts)
        {
            final Collection<ClauseHandler> newHandlers = searchHandlerManager.getClauseHandler(user, newSort.getField());
            if (newHandlers.size() != 1)
            {
                convertedSorts.add(newSort);
                continue;
            }
            final ClauseHandler newHandler = newHandlers.iterator().next();

            SearchSort convertedSort = newSort;
            for (SearchSort oldSort : oldSorts)
            {
                final Collection<ClauseHandler> oldHandlers = searchHandlerManager.getClauseHandler(user, oldSort.getField());
                if (oldHandlers.size() == 1)
                {
                    ClauseHandler handler = oldHandlers.iterator().next();
                    if (newHandler.getInformation().getJqlClauseNames().equals(handler.getInformation().getJqlClauseNames()))
                    {
                        if (!newSort.getField().equals(oldSort.getField()))
                        {
                            convertedSort = new SearchSort(oldSort.getField(), newSort.getSortOrder());
                        }
                    }
                }
            }
            convertedSorts.add(convertedSort);
        }
        return convertedSorts;
    }

    public List<SearchSort> getSearchSorts(final Query query)
    {
        List<SearchSort> sorts;
        // NOTE: when the whole query is null we fall back to the default sorts.
        if (query == null)
        {
            sorts = Collections.emptyList();
        }
        else
        {
            // NOTE: when the order by clause is null we use this condition to force us to use not sorts for our query at all.
            if (query.getOrderByClause() == null)
            {
                return null;
            }
            sorts = query.getOrderByClause().getSearchSorts();
        }

        // This is a special case where we want to put in the default JIRA sorts
        if (sorts.isEmpty())
        {
            //If we have a free text query, then we don't want any sorts so that Lucene's rank will work for us.
            if (query == null || !FreeTextVisitor.containsFreeTextCondition(query.getWhereClause()))
            {
                //By default we sort by the "Issue Key" when there is no text searcher to rank the results.
                sorts = Collections.singletonList(DEFAULT_KEY_SORT);
            }
        }

        return sorts;
    }

    public OrderBy  getOrderByClause(final Map parameterMap)
    {
        List<SearchSort> searchSorts = new ArrayList<SearchSort>();
        int minLength;
        final List orders = ParameterUtils.getListParam(parameterMap, SORTER_ORDER);
        final List fields = ParameterUtils.getListParam(parameterMap, SORTER_FIELD);

        // get min length
        // loop for i = 0 to min.length
        // add sort for order[i] && field[i]
        if ((orders == null) || (fields == null))
        {
            minLength = 0;
        }
        else
        {
            minLength = Math.min(orders.size(), fields.size());
        }

        for (int i = 0; i < minLength; i++)
        {
            final String order = (String) orders.get(i);
            final String field = (String) fields.get(i);

            if ((order != null) && (field != null))
            {
                // Lets convert the field name into a JQL primary clause name
                final Collection<ClauseNames> matchingClauseNames = searchHandlerManager.getJqlClauseNames(field);
                // We only need to take the first ClauseNames since they will all resolve to the same field, which, in
                // the end, will resolve to the same sort
                if (!matchingClauseNames.isEmpty())
                {
                    if (fieldManager.isNavigableField(field))
                    {
                        final ClauseNames names = matchingClauseNames.iterator().next();
                        searchSorts.add(new SearchSort(order, names.getPrimaryName()));
                    }
                    else
                    {
                        log.warn("Unable to create a search sort for the field name '" + field + "' as the field is able to be sorted.");
                    }
                }
                else
                {
                    log.warn("Unable to create a search sort for field name '" + field + "' as there is no associated JQL clause name.");
                }
            }
        }
        return new OrderByImpl(searchSorts);
    }

    public List<String> getSearchSortDescriptions(SearchRequest searchRequest, final I18nHelper i18nHelper, final User searcher)
    {
        Assertions.notNull("searchRequest", searchRequest);
        List<String> searchSortDescriptions = new ArrayList<String>();

        final List<SearchSort> searchSorts = this.getSearchSorts(searchRequest.getQuery());
        for (SearchSort searchSort : searchSorts)
        {
            final String sortClauseName = searchSort.getField();

            final List<String> fieldIds = new ArrayList<String>(searchHandlerManager.getFieldIds(searcher, sortClauseName));
            // sort to get consistent ordering of fields for clauses with multiple fields
            Collections.sort(fieldIds);

            for (String fieldId : fieldIds)
            {
                Field field = fieldManager.getField(fieldId);
                if (field != null)
                {
                    StringBuilder description = new StringBuilder();
                    description.append(i18nHelper.getText(field.getNameKey()));
                    final String orderDescription = getSearchSortOrderDescription(searchSort.getOrder(), field, i18nHelper);
                    if (!StringUtils.isBlank(orderDescription))
                    {
                        description.append(" ").append(orderDescription);
                    }
                    searchSortDescriptions.add(description.toString());
                }
                else
                {
                    log.info("Field '" + sortClauseName + "' is invalid as a search sort in SearchRequest " + searchRequest);
                }
            }
        }

        // now we know that every element in the list is valid, add in the ", then" to all but the last string
        for (int i = 0; i < searchSortDescriptions.size(); i++)
        {
            String description = searchSortDescriptions.get(i);
            if (i < searchSortDescriptions.size() - 1)
            {
                String newDescription = description + ", " + i18nHelper.getText("navigator.hidden.sortby.then");
                searchSortDescriptions.set(i, newDescription);
            }
        }
        return searchSortDescriptions;
    }

    private static String getSearchSortOrderDescription(String searchSortOrder, final Field field, final I18nHelper i18nHelper)
    {
        if (!(field instanceof NavigableField))
        {
            return "";
        }

        NavigableField navigableField = (NavigableField) field;

        searchSortOrder = StringUtils.isBlank(searchSortOrder) ? navigableField.getDefaultSortOrder() : searchSortOrder;
        if (NavigableField.ORDER_DESCENDING.equals(searchSortOrder))
        {
            return i18nHelper.getText("navigator.hidden.sortby.descending");
        }
        else
        {
            return i18nHelper.getText("navigator.hidden.sortby.ascending");
        }
    }
}
