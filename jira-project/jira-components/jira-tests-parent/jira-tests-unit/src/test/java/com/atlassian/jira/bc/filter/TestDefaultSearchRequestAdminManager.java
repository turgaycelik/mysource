package com.atlassian.jira.bc.filter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.search.DefaultSearchRequestAdminManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAdminManager;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.search.SearchRequestStore;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.MockCloseableIterable;
import com.atlassian.query.QueryImpl;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDefaultSearchRequestAdminManager extends MockControllerTestCase
{
    private SearchRequest searchRequest1;
    private SearchRequest searchRequest2;
    private SearchRequest searchRequest3;

    @Before
    public void setUp() throws Exception
    {
        searchRequest1 = new SearchRequest(new QueryImpl(), new MockApplicationUser("admin"), "one", "one description", 1L, 0L);
        searchRequest2 = new SearchRequest(new QueryImpl(), new MockApplicationUser("admin"), "two", "two description", 2L, 0L);
        searchRequest3 = new SearchRequest(new QueryImpl(), new MockApplicationUser("admin"), "three", "three description", 3L, 0L);

        getMock(SearchRequestManager.class);
        getMock(SearchRequestStore.class);
        getMock(ShareManager.class);
    }

    @Test
    public void testGetSearchRequestsForProjectNullProject()
    {
        final SearchRequestAdminManager searchRequestManager = createDefaultSearchRequestManager();
        try
        {
            searchRequestManager.getSearchRequests((Project) null);
            fail("Should not accept null project.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testGetSearchRequestsForProjectNoRequests()
    {
        final Project project = new MockProject(new Long(1), "TEST", "TEST");

        final SearchRequestStore store = getMock(SearchRequestStore.class);
        expect(store.getSearchRequests(project)).andReturn(new MockCloseableIterable<SearchRequest>(Collections.<SearchRequest> emptyList()));

        final SearchRequestAdminManager searchRequestManager = createDefaultSearchRequestManager();

        final EnclosedIterable<SearchRequest> results = searchRequestManager.getSearchRequests(project);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetSearchRequestsForProjectHasRequests()
    {
        final Project project = new MockProject(new Long(1), "TEST", "TEST");

        final List<SearchRequest> returnedList = Lists.newArrayList(searchRequest1, searchRequest2, searchRequest3);

        final SearchRequestStore store = getMock(SearchRequestStore.class);
        expect(store.getSearchRequests(project)).andReturn(new MockCloseableIterable<SearchRequest>(returnedList));

        final SearchRequestAdminManager searchRequestManager = createDefaultSearchRequestManager();

        final EnclosedIterable<SearchRequest> results = searchRequestManager.getSearchRequests(project);

        assertNotNull(results);
        assertEquals(3, results.size());

        final Iterator<SearchRequest> iterator = returnedList.iterator();
        results.foreach(new Consumer<SearchRequest>()
        {
            public void consume(final SearchRequest element)
            {
                assertEquals(iterator.next(), element);
            }
        });
    }

    @Test
    public void testGetSearchRequestSharedWithGroupWithNullGroup()
    {
        final SearchRequestAdminManager searchRequestManager = createDefaultSearchRequestManager();
        try
        {
            searchRequestManager.getSearchRequests((Group) null);
            fail("Should not accept null group.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testGetSearchRequestsSharedWithGroup()
    {
        final Group group = new MockGroup("jira-user");

        final SearchRequestStore store = getMock(SearchRequestStore.class);
        expect(store.getSearchRequests(group)).andReturn(new MockCloseableIterable<SearchRequest>(Lists.newArrayList(searchRequest1)));

        final SearchRequestAdminManager searchRequestManager = createDefaultSearchRequestManager();
        final EnclosedIterable<SearchRequest> results = searchRequestManager.getSearchRequests(group);

        assertEquals(1, results.size());
        results.foreach(new Consumer<SearchRequest>()
        {
            public void consume(final SearchRequest element)
            {
                assertEquals(searchRequest1, element);
            }
        });
    }

    @Test
    public void testGetSearchRequestsSharedWithGroupAndNoShares()
    {
        final Group group = new MockGroup("jira-administrators");

        final SearchRequestStore store = getMock(SearchRequestStore.class);
        expect(store.getSearchRequests(group)).andReturn(new MockCloseableIterable<SearchRequest>(Collections.<SearchRequest> emptyList()));

        final SearchRequestAdminManager searchRequestManager = createDefaultSearchRequestManager();
        final EnclosedIterable<SearchRequest> results = searchRequestManager.getSearchRequests(group);

        assertTrue(results.isEmpty());
    }

    private SearchRequestAdminManager createDefaultSearchRequestManager()
    {
        return instantiate(DefaultSearchRequestAdminManager.class);
    }

}
