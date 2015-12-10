package com.atlassian.jira.issue.search.managers;

import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroup;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.issue.search.managers.TestDefaultIssueSearcherManager}.
 *
 * @since v4.0
 */
public class TestDefaultIssueSearcherManager extends MockControllerTestCase
{
    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new DefaultIssueSearcherManager(null);
            fail("Shoudl not be able to pass in a null manager.");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void testGetSearchers() throws Exception
    {
        final List<IssueSearcher<?>> expectedSearchers = Collections.emptyList();
        final SearchContext context = mockController.getMock(SearchContext.class);
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);
        manager.getSearchers((User) null, context);
        mockController.setReturnValue(expectedSearchers);
        manager.getSearchers((User) null, null);
        mockController.setReturnValue(null);

        final DefaultIssueSearcherManager issueSearcherManager = mockController.instantiate(DefaultIssueSearcherManager.class);
        assertEquals(expectedSearchers, issueSearcherManager.getSearchers(null, context));
        assertNull(issueSearcherManager.getSearchers(null, null));

        mockController.verify();
    }

    @Test
    public void testGetSearcherGroups()
    {
        final SearcherGroup group = new SearcherGroup(SearcherGroupType.DATE, Collections.<IssueSearcher<?>>emptyList());
        final SearcherGroup group2 = new SearcherGroup(SearcherGroupType.ISSUE, Collections.<IssueSearcher<?>>emptyList());

        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);
        manager.getSearcherGroups();
        mockController.setReturnValue(Collections.singletonList(group));
        manager.getSearcherGroups();
        mockController.setReturnValue(CollectionBuilder.newBuilder(group, group2).asList());

        final DefaultIssueSearcherManager issueSearcherManager = mockController.instantiate(DefaultIssueSearcherManager.class);
        assertEquals(Collections.singletonList(group), issueSearcherManager.getSearcherGroups());
        assertEquals(CollectionBuilder.newBuilder(group, group2).asList(), issueSearcherManager.getSearcherGroups());
    }

    @Test
    public void testGetAllSearchers() throws Exception
    {
        final List<IssueSearcher<?>> expectedSearchers = Collections.emptyList();
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);
        manager.getAllSearchers();
        mockController.setReturnValue(expectedSearchers);
        manager.getSearchers((User) null, null);
        mockController.setReturnValue(null);

        final DefaultIssueSearcherManager issueSearcherManager = mockController.instantiate(DefaultIssueSearcherManager.class);
        assertEquals(expectedSearchers, issueSearcherManager.getAllSearchers());
        assertNull(issueSearcherManager.getSearchers(null, null));

        mockController.verify();
    }

    @Test
    public void testGetSearcher()
    {
        final String searcherId = "myid";
        final IssueSearcher<?> expectedSearcher = mockController.getMock(IssueSearcher.class);
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);
        manager.getSearcher(searcherId);
        mockController.setReturnValue(expectedSearcher);
        manager.getSearcher(null);
        mockController.setReturnValue(null);

        final DefaultIssueSearcherManager issueSearcherManager = mockController.instantiate(DefaultIssueSearcherManager.class);
        assertEquals(expectedSearcher, issueSearcherManager.getSearcher(searcherId));
        assertNull(issueSearcherManager.getSearcher(null));

        mockController.verify();
    }

    @Test
    public void testRefresh() throws Exception
    {
        final SearchHandlerManager manager = mockController.getMock(SearchHandlerManager.class);
        manager.refresh();

        final DefaultIssueSearcherManager issueSearcherManager = mockController.instantiate(DefaultIssueSearcherManager.class);
        issueSearcherManager.refresh();
        
        mockController.verify();
    }
}
