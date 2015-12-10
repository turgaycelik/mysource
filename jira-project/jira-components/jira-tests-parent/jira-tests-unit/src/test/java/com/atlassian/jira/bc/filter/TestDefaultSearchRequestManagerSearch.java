package com.atlassian.jira.bc.filter;

import java.util.Collections;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.search.DefaultSearchRequestManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.sharing.search.SharedEntitySearcher;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.collect.MockCloseableIterable;
import com.atlassian.query.QueryImpl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test the {@link com.atlassian.jira.issue.search.DefaultSearchRequestManager} searching methods.
 *
 * @since v3.13
 */
public class TestDefaultSearchRequestManagerSearch extends MockControllerTestCase
{
    private ApplicationUser user;
    private SearchRequest searchRequest2;
    private SearchRequest searchRequest3;

    @Before
    public void setUp()
    {

        user = new MockApplicationUser("testSearchAsUser");

        searchRequest2 = new SearchRequest(new QueryImpl(), user, "two", "two description", 2L, 0L);
        searchRequest3 = new SearchRequest(new QueryImpl(), user, "three", "three description", 3L, 0L);
    }

    /**
     * Check what happens when null search parameters are passed.
     */
    @Test
    public void testSearchNullPrameters()
    {
        final SearchRequestManager searchRequestManager = instantiate(DefaultSearchRequestManager.class);
        try
        {
            searchRequestManager.search(null, (ApplicationUser) null, 0, 10);
            fail("An exception should be thrown on null search parametes.");
        }
        catch (final IllegalArgumentException expected)
        {

        }
    }

    /**
     * Check what happens when zero width is passed.
     */
    @Test
    public void testSearchZeroWidth()
    {
        final SearchRequestManager searchRequestManager = instantiate(DefaultSearchRequestManager.class);
        try
        {
            searchRequestManager.search(new SharedEntitySearchParametersBuilder().toSearchParameters(), (ApplicationUser) null, 0, 0);
            fail("An exception should be thrown on invalid width.");
        }
        catch (final IllegalArgumentException expected)
        {

        }
    }

    /**
     * Check what happens on illegal offset
     */
    @Test
    public void testSearchIllegalOffset()
    {
        final SearchRequestManager searchRequestManager = instantiate(DefaultSearchRequestManager.class);
        try
        {
            searchRequestManager.search(new SharedEntitySearchParametersBuilder().toSearchParameters(), (ApplicationUser) null, 0, -1);
            fail("An exception should be thrown on invalid width.");
        }
        catch (final IllegalArgumentException expected)
        {

        }
    }

    /**
     * Execute the search as a user.
     */
    @Test
    public void testSearchAsUser()
    {
        _testSearch(user, EasyList.build(searchRequest2, searchRequest3));
    }

    /**
     * Execute the search as a user and expect no results.
     */
    @Test
    public void testSearchAsUserWithNoResults()
    {
        _testSearch(user, Collections.EMPTY_LIST);
    }

    /**
     * Execute the search as the anonymous user.
     */
    @Test
    public void testSearchAsAnonymous()
    {
        _testSearch(null, EasyList.build(searchRequest2));
    }

    private void _testSearch(final ApplicationUser user, final List expectedPages)
    {
        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder();
        builder.setName("searchTest");
        builder.setSortColumn(SharedEntityColumn.ID, false);
        final SharedEntitySearchParameters searchParameters = builder.toSearchParameters();

        final SharedEntitySearchResult expectedResult = new SharedEntitySearchResult(new MockCloseableIterable(expectedPages), true, 100);

        final SharedEntitySearcher searcher = getMock(SharedEntitySearcher.class);
        expect(searcher.search(searchParameters, user, 0, 100)).andReturn(expectedResult);

        final SharedEntityIndexer indexer = getMock(SharedEntityIndexer.class);
        expect(indexer.getSearcher(this.<SharedEntity.TypeDescriptor<SharedEntity>>anyObject())).andReturn(searcher);

        final SearchRequestManager searchRequestManager = instantiate(DefaultSearchRequestManager.class);

        // run the search.
        final SharedEntitySearchResult actualResult = searchRequestManager.search(searchParameters, user, 0, 100);

        // make sure the result is as expected.
        assertEquals(expectedResult.hasMoreResults(), actualResult.hasMoreResults());
        assertEquals(expectedResult.getResults(), actualResult.getResults());
    }

}
