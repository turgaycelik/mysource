package com.atlassian.jira.jql.resolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.MockSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.CollectionEnclosedIterable;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestSavedFilterResolver extends MockControllerTestCase
{
    private SearchRequestManager searchRequestManager;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        searchRequestManager = mockController.getMock(SearchRequestManager.class);
    }

    @Test
    public void testGetSearchRequestEmptyLiteral() throws Exception
    {
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> result = filterResolver.getSearchRequest(theUser, Collections.singletonList(new QueryLiteral()));
        assertTrue(result.isEmpty());
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestEmptyString() throws Exception
    {
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);

        List<SearchRequest> result = filterResolver.getSearchRequest(theUser, Collections.singletonList(createLiteral("")));
        assertTrue(result.isEmpty());

        result = filterResolver.getSearchRequest(theUser, Collections.singletonList(createLiteral("    ")));
        assertTrue(result.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetSearchRequestNoSearchRequestById() throws Exception
    {
        ApplicationUser theAppUser = theAppUser();
        searchRequestManager.getSearchRequestById(theAppUser, 123L);
        mockController.setReturnValue(null);
        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().setName("123").setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.EXACT);
        searchRequestManager.search(builder.toSearchParameters(), theAppUser, 0, Integer.MAX_VALUE);
        mockController.setReturnValue(new SharedEntitySearchResult(CollectionEnclosedIterable.from(Collections.<SearchRequest>emptyList()), false, 0));
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        filterResolver.getSearchRequest(theUser, Collections.singletonList(createLiteral(123L)));
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestNoSearchRequestByName() throws Exception
    {
        ApplicationUser theAppUser = theAppUser();
        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().setName("NotAnId").setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.EXACT);
        searchRequestManager.search(builder.toSearchParameters(), theAppUser, 0, Integer.MAX_VALUE);
        mockController.setReturnValue(new SharedEntitySearchResult(CollectionEnclosedIterable.from(Collections.<SearchRequest>emptyList()), false, 0));
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        filterResolver.getSearchRequest(ApplicationUsers.toDirectoryUser(theAppUser), Collections.singletonList(createLiteral("NotAnId")));
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestFoundSearchRequestByNameForId() throws Exception
    {
        ApplicationUser theAppUser = theAppUser();
        final MockSearchRequest searchRequest = new MockSearchRequest("dude");

        searchRequestManager.getSearchRequestById(theAppUser, 123L);
        mockController.setReturnValue(null);
        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().setName("123").setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.EXACT);
        searchRequestManager.search(builder.toSearchParameters(), theAppUser, 0, Integer.MAX_VALUE);
        mockController.setReturnValue(new SharedEntitySearchResult(CollectionEnclosedIterable.from(CollectionBuilder.<SearchRequest>newBuilder(searchRequest).asList()), false, 0));
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> list = filterResolver.getSearchRequest(theUser, Collections.singletonList(createLiteral(123L)));
        assertTrue(list.contains(searchRequest));
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestFoundSearchRequestByIdForName() throws Exception
    {
        ApplicationUser theAppUser = theAppUser();
        final MockSearchRequest searchRequest = new MockSearchRequest("dude");

        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().setName("123").setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.EXACT);
        searchRequestManager.search(builder.toSearchParameters(), theAppUser, 0, Integer.MAX_VALUE);
        mockController.setReturnValue(new SharedEntitySearchResult(CollectionEnclosedIterable.from(Collections.emptyList()), false, 0));
        searchRequestManager.getSearchRequestById(theAppUser, 123L);
        mockController.setReturnValue(searchRequest);
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> list = filterResolver.getSearchRequest(theUser, Collections.singletonList(createLiteral("123")));
        assertTrue(list.contains(searchRequest));
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestFoundSearchRequestById() throws Exception
    {
        final MockSearchRequest searchRequest = new MockSearchRequest("dude");
        ApplicationUser theAppUser = theAppUser();

        searchRequestManager.getSearchRequestById(theAppUser, 123L);
        mockController.setReturnValue(searchRequest);
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> list = filterResolver.getSearchRequest(theUser, Collections.singletonList(createLiteral(123L)));
        assertTrue(list.contains(searchRequest));
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestFoundSearchRequestByIdOverrideSecurity() throws Exception
    {
        final MockSearchRequest searchRequest1 = new MockSearchRequest("dude", 1L);

        EasyMock.expect(searchRequestManager.getSearchRequestById(1L))
                .andReturn(searchRequest1);
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> list = filterResolver.getSearchRequestOverrideSecurity(Collections.singletonList(createLiteral(1L)));
        assertTrue(list.contains(searchRequest1));
        assertEquals(1, list.size());
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestNoneFoundByIdOverrideSecurity() throws Exception
    {
        EasyMock.expect(searchRequestManager.getSearchRequestById(3L))
                .andReturn(null);
        EasyMock.expect(searchRequestManager.findByNameIgnoreCase("3"))
                .andReturn(Collections.<SearchRequest>emptyList());
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> list = filterResolver.getSearchRequestOverrideSecurity(Collections.singletonList(createLiteral(3L)));
        assertTrue(list.isEmpty());
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestFoundMultipleSearchRequestByName() throws Exception
    {
        ApplicationUser theAppUser = theAppUser();
        final MockSearchRequest searchRequest1 = new MockSearchRequest("dude");
        final MockSearchRequest searchRequest2 = new MockSearchRequest("sweet");

        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().setName("123").setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.EXACT);
        searchRequestManager.search(builder.toSearchParameters(), theAppUser, 0, Integer.MAX_VALUE);
        mockController.setReturnValue(new SharedEntitySearchResult(CollectionEnclosedIterable.from(CollectionBuilder.<SearchRequest>newBuilder(searchRequest1, searchRequest2).asList()), false, 0));
        mockController.replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> list = filterResolver.getSearchRequest(ApplicationUsers.toDirectoryUser(theAppUser), Collections.singletonList(createLiteral("123")));
        assertTrue(list.contains(searchRequest1));
        assertTrue(list.contains(searchRequest2));
        mockController.verify();
    }

    @Test
    public void testGetSearchRequestFoundByNameOverrideSecurity() throws Exception
    {
        final MockSearchRequest searchRequest1 = new MockSearchRequest("dude", 1L, "filter1");

        EasyMock.expect(searchRequestManager.findByNameIgnoreCase("FILTER1"))
                .andReturn(Arrays.<SearchRequest>asList(searchRequest1));

        replay();

        final SavedFilterResolver filterResolver = new SavedFilterResolver(searchRequestManager);
        final List<SearchRequest> list = filterResolver.getSearchRequestOverrideSecurity(Collections.singletonList(createLiteral("FILTER1")));
        assertTrue(list.contains(searchRequest1));
        assertEquals(1, list.size());

        verify();
    }

    private ApplicationUser theAppUser()
    {
        return theUser == null ? null : new DelegatingApplicationUser(theUser.getName(), theUser);
    }
}
