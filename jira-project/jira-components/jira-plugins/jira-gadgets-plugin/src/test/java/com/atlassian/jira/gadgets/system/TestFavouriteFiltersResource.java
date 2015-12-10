package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.query.QueryImpl;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

public class TestFavouriteFiltersResource extends TestCase
{
    private JiraAuthenticationContext jac;
    private SearchRequestService searchRequestService;
    private SearchProvider sp;
    private ApplicationUser user;

    public void testWithCount()
    {
        ArrayList<SearchRequest> arrayList = new ArrayList<SearchRequest>();
        SearchRequest request = new SearchRequest(new QueryImpl(), new MockApplicationUser("foo"), "bar", "troz");
        arrayList.add(request);
        expect(searchRequestService.getFavouriteFilters(user)).andReturn(arrayList);

        try
        {
            expect(sp.searchCount(request.getQuery(), user)).andReturn(3L);
        }
        catch (SearchException e)
        {
            throw new RuntimeException(e);
        }

        replay(jac, searchRequestService, sp);

        FavouriteFiltersResource resource = new FavouriteFiltersResource(jac, searchRequestService, sp, null, null);
        Response favouriteFilters = resource.getFavouriteFilters(true);
        assertEquals(200, favouriteFilters.getStatus());
        FavouriteFiltersResource.FilterList list = (FavouriteFiltersResource.FilterList) favouriteFilters.getEntity();
        Collection<FavouriteFiltersResource.Filter> filterCollection = list.getFilters();
        assertEquals(1, filterCollection.size());
        FavouriteFiltersResource.Filter filter = filterCollection.iterator().next();
        assertEquals("bar", filter.getLabel());
        assertEquals("troz", filter.getDescription());
        assertEquals(3L, (long) filter.getCount());
    }

    public void testWithOutCount()
    {

        EasyMock.expect(jac.getUser()).andReturn(user);

        ArrayList<SearchRequest> arrayList = new ArrayList<SearchRequest>();
        SearchRequest request = new SearchRequest(new QueryImpl(), new MockApplicationUser("foo"), "bar", "troz");
        arrayList.add(request);
        expect(searchRequestService.getFavouriteFilters(user)).andReturn(arrayList);

        replay(jac, searchRequestService);

        FavouriteFiltersResource resource = new FavouriteFiltersResource(jac, searchRequestService, sp, null, null);
        Response favouriteFilters = resource.getFavouriteFilters(true);
        assertEquals(200, favouriteFilters.getStatus());
        FavouriteFiltersResource.FilterList list = (FavouriteFiltersResource.FilterList) favouriteFilters.getEntity();
        Collection<FavouriteFiltersResource.Filter> filterCollection = list.getFilters();
        assertEquals(1, filterCollection.size());
        FavouriteFiltersResource.Filter filter = filterCollection.iterator().next();
        assertEquals("bar", filter.getLabel());
        assertEquals("troz", filter.getDescription());
    }

    public void testNoUser()
    {
        JiraAuthenticationContext jac = EasyMock.createMock(JiraAuthenticationContext.class);

        FavouriteFiltersResource.NotLoggedIn o = (FavouriteFiltersResource.NotLoggedIn) new FavouriteFiltersResource(jac, null, null, null, null).getFavouriteFilters(false).getEntity();
        assertFalse(o.isNotLoggedIn());
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        jac = EasyMock.createMock(JiraAuthenticationContext.class);
        searchRequestService = EasyMock.createMock(SearchRequestService.class);
        sp = EasyMock.createMock(SearchProvider.class);
        user = new MockApplicationUser("fooGuy");
        EasyMock.expect(jac.getUser()).andReturn(user);
    }

    protected void tearDown() throws Exception
    {
        jac = null;
        searchRequestService = null;
        sp = null;
        user = null;
        super.tearDown();
    }
}
