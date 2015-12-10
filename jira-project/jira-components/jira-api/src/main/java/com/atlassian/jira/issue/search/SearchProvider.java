/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import org.apache.lucene.search.Collector;

import java.io.IOException;

/**
 * A SearchProvider in JIRA allows users to run structured searches against JIRA Lucene index as opposed
 * to database (SQL) based queries.
 *
 * All search methods take a {@link com.atlassian.query.Query} which defines the criteria of the search,
 * including any sort information.
 */
public interface SearchProvider
{
    /**
     * Search the index, and only return issues that are in the pager's range.
     * <em>Note: that this method returns read only {@link com.atlassian.jira.issue.Issue} objects, and should not be
     * used where you need the issue for update</em>.
     *
     * Also note that if you are only after the number of search results use
     * {@link #searchCount(com.atlassian.query.Query ,User)} as it provides better performance.
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search, which will be used to create a permission filter that filters out
     * any of the results the user is not able to see and will be used to provide context for the search.
     * @param pager Pager filter (use {@link com.atlassian.jira.web.bean.PagerFilter#getUnlimitedFilter()} to get all issues).
     *
     * @return A {@link SearchResults} containing the resulting issues.
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    SearchResults search(Query query, User searcher, PagerFilter pager) throws SearchException;

    /**
     * Search the index, and only return issues that are in the pager's range.
     * <em>Note: that this method returns read only {@link com.atlassian.jira.issue.Issue} objects, and should not be
     * used where you need the issue for update</em>.
     *
     * Also note that if you are only after the number of search results use
     * {@link #searchCount(com.atlassian.query.Query ,User)} as it provides better performance.
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search, which will be used to create a permission filter that filters out
     * any of the results the user is not able to see and will be used to provide context for the search.
     * @param pager Pager filter (use {@link com.atlassian.jira.web.bean.PagerFilter#getUnlimitedFilter()} to get all issues).
     *
     * @return A {@link SearchResults} containing the resulting issues.
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    SearchResults search(Query query, ApplicationUser searcher, PagerFilter pager) throws SearchException;

    /**
     * Search the index, and only return issues that are in the pager's range while AND'ing the raw lucene query
     * to the generated query from the provided searchQuery.
     *
     * <em>Note that this method returns read only {@link com.atlassian.jira.issue.Issue} objects, and should not be
     * used where you need the issue for update</em>.
     *
     * Also note that if you are only after the number of search results use
     * {@link #searchCount(com.atlassian.query.Query ,User)} as it provides better performance.
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search, which will be used to create a permission filter that filters out
     * any of the results the user is not able to see and will be used to provide context for the search.
     * @param pager Pager filter (use {@link com.atlassian.jira.web.bean.PagerFilter#getUnlimitedFilter()} to get all issues).
     * @param andQuery raw lucene Query to AND with the request.
     *
     * @return A {@link SearchResults} containing the resulting issues.
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    SearchResults search(Query query, User searcher, PagerFilter pager, org.apache.lucene.search.Query andQuery) throws SearchException;

    /**
     * Search the index, and only return issues that are in the pager's range while AND'ing the raw lucene query
     * to the generated query from the provided searchQuery.
     *
     * <em>Note that this method returns read only {@link com.atlassian.jira.issue.Issue} objects, and should not be
     * used where you need the issue for update</em>.
     *
     * Also note that if you are only after the number of search results use
     * {@link #searchCount(com.atlassian.query.Query ,User)} as it provides better performance.
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search, which will be used to create a permission filter that filters out
     * any of the results the user is not able to see and will be used to provide context for the search.
     * @param pager Pager filter (use {@link com.atlassian.jira.web.bean.PagerFilter#getUnlimitedFilter()} to get all issues).
     * @param andQuery raw lucene Query to AND with the request.
     *
     * @return A {@link SearchResults} containing the resulting issues.
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    SearchResults search(Query query, ApplicationUser searcher, PagerFilter pager, org.apache.lucene.search.Query andQuery) throws SearchException;

    /**
     * Search the index, and only return issues that are in the pager's range while AND'ing the raw lucene query
     * to the generated query from the provided searchQuery, not taking into account any security
     * constraints.
     *
     * Do not use this method, user {@link #search(com.atlassian.query.Query , User, com.atlassian.jira.web.bean.PagerFilter, org.apache.lucene.search.Query)}
     * instead, this should only be used when performing administrative tasks where you need to know ALL the issues
     * that will be affected.
     *
     * <em>Note that this method returns read only {@link com.atlassian.jira.issue.Issue} objects, and should not be
     * used where you need the issue for update</em>.  Also note that if you are only after the number of search
     * results use {@link #searchCount(com.atlassian.query.Query, User)} as it provides better performance.
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     * @param pager Pager filter (use {@link com.atlassian.jira.web.bean.PagerFilter#getUnlimitedFilter()} to get all issues).
     * @param andQuery raw lucene Query to AND with the request.
     *
     * @return A {@link SearchResults} containing the resulting issues.
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    SearchResults searchOverrideSecurity(Query query, User searcher, PagerFilter pager, org.apache.lucene.search.Query andQuery) throws SearchException;

    /**
     * Search the index, and only return issues that are in the pager's range while AND'ing the raw lucene query
     * to the generated query from the provided searchQuery, not taking into account any security
     * constraints.
     *
     * Do not use this method, user {@link #search(com.atlassian.query.Query , User, com.atlassian.jira.web.bean.PagerFilter, org.apache.lucene.search.Query)}
     * instead, this should only be used when performing administrative tasks where you need to know ALL the issues
     * that will be affected.
     *
     * <em>Note that this method returns read only {@link com.atlassian.jira.issue.Issue} objects, and should not be
     * used where you need the issue for update</em>.  Also note that if you are only after the number of search
     * results use {@link #searchCount(com.atlassian.query.Query, User)} as it provides better performance.
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     * @param pager Pager filter (use {@link com.atlassian.jira.web.bean.PagerFilter#getUnlimitedFilter()} to get all issues).
     * @param andQuery raw lucene Query to AND with the request.
     *
     * @return A {@link SearchResults} containing the resulting issues.
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    SearchResults searchOverrideSecurity(Query query, ApplicationUser searcher, PagerFilter pager, org.apache.lucene.search.Query andQuery) throws SearchException;

    /**
     * Return the number of issues matching the provided search criteria.
     * <b>Note:</b> This does not load all results into memory and provides better performance than
     * {@link #search(com.atlassian.query.Query ,User, com.atlassian.jira.web.bean.PagerFilter)}
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     *
     * @return number of matching results.
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    long searchCount(Query query, User searcher) throws SearchException;

    /**
     * Return the number of issues matching the provided search criteria.
     * <b>Note:</b> This does not load all results into memory and provides better performance than
     * {@link #search(com.atlassian.query.Query ,User, com.atlassian.jira.web.bean.PagerFilter)}
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     *
     * @return number of matching results.
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    long searchCount(Query query, ApplicationUser searcher) throws SearchException;

    /**
     * Return the number of issues matching the provided search criteria, overridding any security constraints.
     *
     * Do not use this method, use {@link #searchCount(com.atlassian.query.Query , User)}
     * instead, this should only be used when performing administrative tasks where you need to know ALL the issues
     * that will be affected.
     *
     * <b>Note:</b> This does not load all results into memory and provides better performance than
     * {@link #search(com.atlassian.query.Query ,User, com.atlassian.jira.web.bean.PagerFilter)}
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     *
     * @return number of matching results.
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    long searchCountOverrideSecurity(Query query, User searcher) throws SearchException;

    /**
     * Return the number of issues matching the provided search criteria, overridding any security constraints.
     *
     * Do not use this method, use {@link #searchCount(com.atlassian.query.Query , User)}
     * instead, this should only be used when performing administrative tasks where you need to know ALL the issues
     * that will be affected.
     *
     * <b>Note:</b> This does not load all results into memory and provides better performance than
     * {@link #search(com.atlassian.query.Query ,User, com.atlassian.jira.web.bean.PagerFilter)}
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     *
     * @return number of matching results.
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    long searchCountOverrideSecurity(Query query, ApplicationUser searcher) throws SearchException;

    /**
     * Run a search based on the provided search criteria and, for each match, call Collector.collect().
     * Collectors are low level Lucene classes, but they allow issues to be placed into buckets very quickly. Many of
     * JIRA's graphs and stats are generated in this manner. This method is useful if you need to execute a query in
     * constant-memory (i.e. you do not want to load the results of your complete search into memory) and the query
     * generated via JQL needs to be augmented with some custom Lucene query.
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     * @param collector the Lucene object that will have collect called for each match.
     * @param andQuery additional Lucene query to be anded with the lucene query that will be generated from JQL
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query
     * produces lucene that is too complex to be processed.
     */
    void search(Query query, User searcher, Collector collector, org.apache.lucene.search.Query andQuery)
            throws SearchException;

    /**
     * Run a search based on the provided search criteria and, for each match, call Collector.collect().
     * Collectors are low level Lucene classes, but they allow issues to be placed into buckets very quickly. Many of
     * JIRA's graphs and stats are generated in this manner. This method is useful if you need to execute a query in
     * constant-memory (i.e. you do not want to load the results of your complete search into memory) and the query
     * generated via JQL needs to be augmented with some custom Lucene query.
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     * @param collector the Lucene object that will have collect called for each match.
     * @param andQuery additional Lucene query to be anded with the lucene query that will be generated from JQL
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query
     * produces lucene that is too complex to be processed.
     */
    void search(Query query, ApplicationUser searcher, Collector collector, org.apache.lucene.search.Query andQuery)
            throws SearchException;

    /**
     * Run a search based on the provided search criteria and, for each match, call Collector.collect().
     * Collectors are low level Lucene classes, but they allow issues to be placed into buckets very quickly.
     * Many of JIRA's graphs and stats are generated in this manner. This method is useful if you need to execute a
     * query in constant-memory (i.e. you do not want to load the results of your complete search into memory).
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     * @param collector the Lucene object that will have collect called for each match.
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    void search(Query query, User searcher, Collector collector) throws SearchException;

    /**
     * Run a search based on the provided search criteria and, for each match, call Collector.collect().
     * Collectors are low level Lucene classes, but they allow issues to be placed into buckets very quickly.
     * Many of JIRA's graphs and stats are generated in this manner. This method is useful if you need to execute a
     * query in constant-memory (i.e. you do not want to load the results of your complete search into memory).
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     * @param collector the Lucene object that will have collect called for each match.
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    void search(Query query, ApplicationUser searcher, Collector collector) throws SearchException;

    /**
     * Run a search based on the provided search criteria and, for each match, call Collector.collect() not taking
     * into account any security constraints.
     *
     * Do not use this method, use {@link #search(com.atlassian.query.Query , User, org.apache.lucene.search.Collector)}
     * instead, this should only be used when performing administrative tasks where you need to know ALL the issues
     * that will be affected.
     *
     * Collectors are low level Lucene classes, but they allow issues to be placed into buckets very quickly.
     * Many of JIRA's graphs and stats are generated in this manner. This method is useful if you need to execute a
     * query in constant-memory (i.e. you do not want to load the results of your complete search into memory).
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     * @param collector the Lucene object that will have collect called for each match.
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    void searchOverrideSecurity(Query query, User searcher, Collector collector) throws SearchException;

    /**
     * Run a search based on the provided search criteria and, for each match, call Collector.collect() not taking
     * into account any security constraints.
     *
     * Do not use this method, use {@link #search(com.atlassian.query.Query , User, org.apache.lucene.search.Collector)}
     * instead, this should only be used when performing administrative tasks where you need to know ALL the issues
     * that will be affected.
     *
     * Collectors are low level Lucene classes, but they allow issues to be placed into buckets very quickly.
     * Many of JIRA's graphs and stats are generated in this manner. This method is useful if you need to execute a
     * query in constant-memory (i.e. you do not want to load the results of your complete search into memory).
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     * @param collector the Lucene object that will have collect called for each match.
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    void searchOverrideSecurity(Query query, ApplicationUser searcher, Collector collector) throws SearchException;

    /**
     * Run a search based on the provided search criteria and, for each match call Collector.collect(). This method
     * is for Collectors that need the search results to be sorted.
     *
     * <b>Note:</b> this is much slower than using {@link #search(com.atlassian.query.Query ,User, org.apache.lucene.search.Collector)}.
     *
     * You may limit the number of results being collected by the Collector using the PagerFilter parameter.
     * This method is useful if you need to execute a query in constant-memory (i.e. you do not want to load
     * the results of your complete search into memory).
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     * @param collector the Lucene object that will have collect called for each match.
     * @param pager Pager filter (use {@link com.atlassian.jira.web.bean.PagerFilter#getUnlimitedFilter()} to get all issues).
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    void searchAndSort(Query query, User searcher, Collector collector, PagerFilter pager) throws SearchException;

    /**
     * Run a search based on the provided search criteria and, for each match call Collector.collect(). This method
     * is for Collectors that need the search results to be sorted.
     *
     * <b>Note:</b> this is much slower than using {@link #search(com.atlassian.query.Query ,User, org.apache.lucene.search.Collector)}.
     *
     * You may limit the number of results being collected by the Collector using the PagerFilter parameter.
     * This method is useful if you need to execute a query in constant-memory (i.e. you do not want to load
     * the results of your complete search into memory).
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     * @param collector the Lucene object that will have collect called for each match.
     * @param pager Pager filter (use {@link com.atlassian.jira.web.bean.PagerFilter#getUnlimitedFilter()} to get all issues).
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    void searchAndSort(Query query, ApplicationUser searcher, Collector collector, PagerFilter pager) throws SearchException;

    /**
     * Run a search based on the provided search criteria and, for each match call Collector.collect(). This method
     * is for Collectors that need the search results to be sorted.
     *
     * Do not use this method, user {@link #searchAndSort(com.atlassian.query.Query , User, org.apache.lucene.search.Collector, com.atlassian.jira.web.bean.PagerFilter)}
     * instead, this should only be used when performing administrative tasks where you need to know ALL the issues
     * that will be effected.
     *
     * <b>Note:</b> this is much slower than using {@link #search(com.atlassian.query.Query ,User, org.apache.lucene.search.Collector)}.
     *
     * You may limit the number of results being collected by the Collector using the PagerFilter parameter.
     * This method is useful if you need to execute a query in constant-memory (i.e. you do not want to load
     * the results of your complete search into memory).
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     * @param collector the Lucene object that will have collect called for each match.
     * @param pager Pager filter (use {@link com.atlassian.jira.web.bean.PagerFilter#getUnlimitedFilter()} to get all issues).
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    void searchAndSortOverrideSecurity(Query query, User searcher, Collector collector, PagerFilter pager) throws SearchException;
    
    /**
     * Run a search based on the provided search criteria and, for each match call Collector.collect(). This method
     * is for Collectors that need the search results to be sorted.
     *
     * Do not use this method, user {@link #searchAndSort(com.atlassian.query.Query , User, org.apache.lucene.search.Collector, com.atlassian.jira.web.bean.PagerFilter)}
     * instead, this should only be used when performing administrative tasks where you need to know ALL the issues
     * that will be effected.
     *
     * <b>Note:</b> this is much slower than using {@link #search(com.atlassian.query.Query ,User, org.apache.lucene.search.Collector)}.
     *
     * You may limit the number of results being collected by the Collector using the PagerFilter parameter.
     * This method is useful if you need to execute a query in constant-memory (i.e. you do not want to load
     * the results of your complete search into memory).
     *
     * @param query contains the information required to perform the search.
     * @param searcher the user performing the search which will be used to provide context for the search.
     * @param collector the Lucene object that will have collect called for each match.
     * @param pager Pager filter (use {@link com.atlassian.jira.web.bean.PagerFilter#getUnlimitedFilter()} to get all issues).
     *
     * @throws SearchException thrown if there is a severe problem encountered with lucene when searching (wraps an
     * IOException).
     * @throws com.atlassian.jira.issue.search.ClauseTooComplexSearchException if the query or part of the query produces
     * lucene that is too complex to be processed.
     *  @since v4.3
     */
    void searchAndSortOverrideSecurity(Query query, ApplicationUser searcher, Collector collector, PagerFilter pager) throws SearchException;
}