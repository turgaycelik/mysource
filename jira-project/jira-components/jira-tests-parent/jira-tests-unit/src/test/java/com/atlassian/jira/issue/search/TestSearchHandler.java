package com.atlassian.jira.issue.search;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.DueDateIndexer;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.MockCustomFieldSearcher;
import com.atlassian.jira.jql.DefaultClauseHandler;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.mock.jql.MockClauseInformation;
import com.atlassian.jira.mock.jql.context.MockClauseContextFactory;
import com.atlassian.jira.mock.jql.query.MockClauseQueryFactory;
import com.atlassian.jira.mock.jql.validator.MockClausePermissionHandler;
import com.atlassian.jira.mock.jql.validator.MockClauseValidator;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.issue.search.SearchHandler}.
 *
 * @since v4.0
 */
public class TestSearchHandler
{
    @Test
    public void testClauseRegistrationConstructor() throws Exception
    {
        ClauseQueryFactory clauseFactory = new MockClauseQueryFactory();
        ClauseValidator clauseValidator = new MockClauseValidator();

        Set<String> otherNames = CollectionBuilder.newBuilder("brenden").asSet();
        final ClauseNames names = new ClauseNames("brenden", otherNames);
        final DefaultClauseHandler handler = new DefaultClauseHandler(new MockClauseInformation(names), clauseFactory, clauseValidator, new MockClausePermissionHandler(), new MockClauseContextFactory());

        SearchHandler.ClauseRegistration rego = new SearchHandler.ClauseRegistration(handler);
        assertEquals(handler, rego.getHandler());
        assertEquals(names, rego.getHandler().getInformation().getJqlClauseNames());

        rego = new SearchHandler.ClauseRegistration(new DefaultClauseHandler(new MockClauseInformation(names), clauseFactory, clauseValidator, new MockClausePermissionHandler(), new MockClauseContextFactory()));
        assertEquals(names, rego.getHandler().getInformation().getJqlClauseNames());
        assertEquals(clauseFactory, rego.getHandler().getFactory());
        assertEquals(clauseValidator, rego.getHandler().getValidator());
    }

    @Test
    public void testClauseRegistrationConstructorBad() throws Exception
    {
        final ClauseNames goodNames = new ClauseNames("dude", CollectionBuilder.newBuilder("jack").asSet());
        ClauseQueryFactory clauseFactory = new MockClauseQueryFactory();
        ClauseValidator clauseValidator = new MockClauseValidator();
        final MockClauseContextFactory clauseContextFactory = new MockClauseContextFactory();

        try
        {
            new SearchHandler.ClauseRegistration(null);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            new SearchHandler.ClauseRegistration(new DefaultClauseHandler(new MockClauseInformation(goodNames), clauseFactory, null, null, clauseContextFactory));
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            new SearchHandler.ClauseRegistration(new DefaultClauseHandler(new MockClauseInformation(goodNames), null, clauseValidator, null, clauseContextFactory));
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testSearcherRegistrationConstructor() throws Exception
    {
        final ClauseNames goodNames = new ClauseNames("dude", CollectionBuilder.newBuilder("jack").asSet());
        final SearchHandler.ClauseRegistration clauseRego = new SearchHandler.ClauseRegistration(new DefaultClauseHandler(new MockClauseInformation(goodNames), new MockClauseQueryFactory(), new MockClauseValidator(), new MockClausePermissionHandler(), new MockClauseContextFactory()));
        final IssueSearcher<?> issueSearcher = new MockCustomFieldSearcher("issueSearcher");

        SearchHandler.SearcherRegistration rego = new SearchHandler.SearcherRegistration(issueSearcher, clauseRego);
        assertEquals(issueSearcher, rego.getIssueSearcher());
        assertEquals(Collections.singletonList(clauseRego), rego.getClauseHandlers());

        rego = new SearchHandler.SearcherRegistration(issueSearcher, Collections.singletonList(clauseRego));
        assertEquals(issueSearcher, rego.getIssueSearcher());
        assertEquals(Collections.singletonList(clauseRego), rego.getClauseHandlers());
    }

    @Test
    public void testSearcherRegistrationConstructorBad() throws Exception
    {
        final IssueSearcher<?> issueSearcher = new MockCustomFieldSearcher("issueSearcher");
        final ClauseNames goodNames = new ClauseNames("dude", CollectionBuilder.newBuilder("jack").asSet());
        final SearchHandler.ClauseRegistration clauseRego = new SearchHandler.ClauseRegistration(new DefaultClauseHandler(new MockClauseInformation(goodNames), new MockClauseQueryFactory(), new MockClauseValidator(), new MockClausePermissionHandler(), new MockClauseContextFactory()));
        final List<SearchHandler.ClauseRegistration> goodRegoList = Collections.singletonList(clauseRego);
        final List<SearchHandler.ClauseRegistration> badRegoList = Collections.singletonList(null);

        try
        {
            new SearchHandler.SearcherRegistration(null, clauseRego);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            new SearchHandler.SearcherRegistration(issueSearcher, (SearchHandler.ClauseRegistration) null);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            new SearchHandler.SearcherRegistration(issueSearcher, (List<SearchHandler.ClauseRegistration>) null);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            new SearchHandler.SearcherRegistration(issueSearcher, badRegoList);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            new SearchHandler.SearcherRegistration(null, goodRegoList);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testConstructor() throws Exception
    {
        final IssueSearcher<?> issueSearcher = new MockCustomFieldSearcher("issueSearcher");
        final ClauseNames goodNames = new ClauseNames("dude", CollectionBuilder.newBuilder("jack").asSet());
        final SearchHandler.ClauseRegistration clauseRego = new SearchHandler.ClauseRegistration(new DefaultClauseHandler(new MockClauseInformation(goodNames), new MockClauseQueryFactory(), new MockClauseValidator(), new MockClausePermissionHandler(), new MockClauseContextFactory()));
        final List<SearchHandler.ClauseRegistration> goodRegoList = Collections.singletonList(clauseRego);
        final List<FieldIndexer> goodFields = Collections.<FieldIndexer>singletonList(new DueDateIndexer(null));

        final SearchHandler.SearcherRegistration searcherRego = new SearchHandler.SearcherRegistration(issueSearcher, clauseRego);
        SearchHandler searchHandler = new SearchHandler(goodFields, null, goodRegoList);
        assertEquals(goodFields, searchHandler.getIndexers());
        assertEquals(goodRegoList, searchHandler.getClauseRegistrations());
        assertNull(searchHandler.getSearcherRegistration());

        searchHandler = new SearchHandler(goodFields, searcherRego, goodRegoList);
        assertEquals(goodFields, searchHandler.getIndexers());
        assertEquals(goodRegoList, searchHandler.getClauseRegistrations());
        assertEquals(searcherRego, searchHandler.getSearcherRegistration());

        searchHandler = new SearchHandler(Collections.<FieldIndexer>emptyList(), searcherRego, Collections.<SearchHandler.ClauseRegistration>emptyList());
        assertTrue(searchHandler.getIndexers().isEmpty());
        assertTrue(searchHandler.getClauseRegistrations().isEmpty());
        assertEquals(searcherRego, searchHandler.getSearcherRegistration());

        searchHandler = new SearchHandler(goodFields, searcherRego);
        assertEquals(goodFields, searchHandler.getIndexers());
        assertTrue(searchHandler.getClauseRegistrations().isEmpty());
        assertEquals(searcherRego, searchHandler.getSearcherRegistration());

        searchHandler = new SearchHandler(Collections.<FieldIndexer>emptyList(), null);
        assertTrue(searchHandler.getIndexers().isEmpty());
        assertTrue(searchHandler.getClauseRegistrations().isEmpty());
        assertNull(searchHandler.getSearcherRegistration());
    }

    @Test
    public void testConstructorBad() throws Exception
    {
        final ClauseNames goodNames = new ClauseNames("dude", CollectionBuilder.newBuilder("jack").asSet());
        final SearchHandler.ClauseRegistration clauseRego = new SearchHandler.ClauseRegistration(new DefaultClauseHandler(new MockClauseInformation(goodNames), new MockClauseQueryFactory(), new MockClauseValidator(), new MockClausePermissionHandler(), new MockClauseContextFactory()));
        final List<SearchHandler.ClauseRegistration> goodRegoList = Collections.singletonList(clauseRego);
        final List<SearchHandler.ClauseRegistration> badRegoList = Collections.singletonList(null);
        final List<FieldIndexer> goodFields = Collections.<FieldIndexer>singletonList(new DueDateIndexer(null));
        final List<FieldIndexer> badFields = Collections.singletonList(null);

        try
        {
            new SearchHandler(null, null, goodRegoList);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new SearchHandler(badFields, null, goodRegoList);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new SearchHandler(goodFields, null, null);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new SearchHandler(goodFields, null, badRegoList);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new SearchHandler(null, null);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new SearchHandler(badFields, null);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

    }
}
