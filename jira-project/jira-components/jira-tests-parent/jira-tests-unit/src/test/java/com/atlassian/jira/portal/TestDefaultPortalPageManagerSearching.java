package com.atlassian.jira.portal;

import java.util.Collections;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.sharing.search.SharedEntitySearcher;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.collect.MockCloseableIterable;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test the searching methods of the {@link com.atlassian.jira.portal.PortalPageManager}.
 *
 * @since v3.13
 */
public class TestDefaultPortalPageManagerSearching extends MockControllerTestCase
{
    private PortalPage portalPage1;
    private PortalPage portalPage2;

    private MockApplicationUser user;

    @Before
    public void setUp() throws Exception
    {
        user = new MockApplicationUser("testSearchAsUser");

        portalPage1 = PortalPage.id(1L).name("one").description("one description").owner(user).build();
        portalPage1 = PortalPage.id(2L).name("two").description("two description").owner(user).build();
    }

    /**
     * Check what happens when null search parameters are passed.
     */
    @Test
    public void testSearchNullPrameters()
    {
        final PortalPageManager pageManager = createPortalPageManager();
        try
        {
            pageManager.search(null, (ApplicationUser) null, 0, 10);
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
        final PortalPageManager pageManager = createPortalPageManager();
        try
        {
            pageManager.search(new SharedEntitySearchParametersBuilder().toSearchParameters(), (ApplicationUser) null, 0, 0);
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
        final PortalPageManager pageManager = createPortalPageManager();
        try
        {
            pageManager.search(new SharedEntitySearchParametersBuilder().toSearchParameters(), (ApplicationUser) null, 0, -1);
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
        _testSearch(user, EasyList.build(portalPage1, portalPage2));
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
        _testSearch(null, EasyList.build(portalPage2));
    }

    private void _testSearch(final ApplicationUser user, final List expectedPages)
    {
        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder().setName("searchTest").setSortColumn(SharedEntityColumn.ID, false).toSearchParameters();

        final SharedEntitySearchResult expectedResult = new SharedEntitySearchResult(new MockCloseableIterable(expectedPages), true, expectedPages.size() + 100);

        final SharedEntitySearcher searcher = (SharedEntitySearcher) mockController.getMock(SharedEntitySearcher.class);
        final SharedEntityIndexer indexer = (SharedEntityIndexer) mockController.getNiceMock(SharedEntityIndexer.class);
        indexer.getSearcher(PortalPage.ENTITY_TYPE);
        mockController.setDefaultReturnValue(searcher);
        searcher.search(searchParameters, user, 0, 100);
        mockController.setReturnValue(expectedResult);

        final PortalPageManager portalPageManager = createPortalPageManager();

        // run the search.
        final SharedEntitySearchResult actualResult = portalPageManager.search(searchParameters, user, 0, 100);

        // make sure the result is as expected.
        assertEquals(expectedResult.hasMoreResults(), actualResult.hasMoreResults());
        assertEquals(expectedResult.getResults(), actualResult.getResults());

        mockController.verify();
    }

    private PortalPageManager createPortalPageManager()
    {
        return (PortalPageManager) mockController.instantiate(DefaultPortalPageManager.class);
    }
}
